package io.camunda.operate.es;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.property.ElasticsearchProperties;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.property.SslProperties;
import io.camunda.operate.util.RetryOperation;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class ElasticsearchConnector {
   private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConnector.class);
   @Autowired
   private OperateProperties operateProperties;

   @Bean
   public RestHighLevelClient esClient() {
      System.setProperty("es.set.netty.runtime.available.processors", "false");
      return this.createEsClient(this.operateProperties.getElasticsearch());
   }

   @Bean({"zeebeEsClient"})
   public RestHighLevelClient zeebeEsClient() {
      System.setProperty("es.set.netty.runtime.available.processors", "false");
      return this.createEsClient(this.operateProperties.getZeebeElasticsearch());
   }

   public static void closeEsClient(RestHighLevelClient esClient) {
      if (esClient != null) {
         try {
            esClient.close();
         } catch (IOException var2) {
            logger.error("Could not close esClient", var2);
         }
      }

   }

   public RestHighLevelClient createEsClient(ElasticsearchProperties elsConfig) {
      logger.debug("Creating Elasticsearch connection...");
      RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost[]{this.getHttpHost(elsConfig)}).setHttpClientConfigCallback((httpClientBuilder) -> {
         return this.configureHttpClient(httpClientBuilder, elsConfig);
      });
      if (elsConfig.getConnectTimeout() != null || elsConfig.getSocketTimeout() != null) {
         restClientBuilder.setRequestConfigCallback((configCallback) -> {
            return this.setTimeouts(configCallback, elsConfig);
         });
      }

      RestHighLevelClient esClient = new RestHighLevelClient(restClientBuilder);
      if (!this.checkHealth(esClient)) {
         logger.warn("Elasticsearch cluster is not accessible");
      } else {
         logger.debug("Elasticsearch connection was successfully created.");
      }

      return esClient;
   }

   private HttpAsyncClientBuilder configureHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder, ElasticsearchProperties elsConfig) {
      this.setupAuthentication(httpAsyncClientBuilder, elsConfig);
      if (elsConfig.getSsl() != null) {
         this.setupSSLContext(httpAsyncClientBuilder, elsConfig.getSsl());
      }

      return httpAsyncClientBuilder;
   }

   private void setupSSLContext(HttpAsyncClientBuilder httpAsyncClientBuilder, SslProperties sslConfig) {
      try {
         httpAsyncClientBuilder.setSSLContext(this.getSSLContext(sslConfig));
         if (!sslConfig.isVerifyHostname()) {
            httpAsyncClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
         }
      } catch (Exception var4) {
         logger.error("Error in setting up SSLContext", var4);
      }

   }

   private SSLContext getSSLContext(SslProperties sslConfig) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
      KeyStore truststore = this.loadCustomTrustStore(sslConfig);
      TrustStrategy trustStrategy = sslConfig.isSelfSigned() ? new TrustSelfSignedStrategy() : null;
      return truststore.size() > 0 ? SSLContexts.custom().loadTrustMaterial(truststore, trustStrategy).build() : SSLContext.getDefault();
   }

   private KeyStore loadCustomTrustStore(SslProperties sslConfig) {
      String serverCertificate;
      try {
         KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
         trustStore.load((KeyStore.LoadStoreParameter)null);
         serverCertificate = sslConfig.getCertificatePath();
         if (serverCertificate != null) {
            this.setCertificateInTrustStore(trustStore, serverCertificate);
         }

         return trustStore;
      } catch (Exception var4) {
         serverCertificate = "Could not create certificate trustStore for the secured Elasticsearch Connection!";
         throw new OperateRuntimeException(serverCertificate, var4);
      }
   }

   private void setCertificateInTrustStore(KeyStore trustStore, String serverCertificate) {
      try {
         Certificate cert = this.loadCertificateFromPath(serverCertificate);
         trustStore.setCertificateEntry("elasticsearch-host", cert);
      } catch (Exception var5) {
         String message = "Could not load configured server certificate for the secured Elasticsearch Connection!";
         throw new OperateRuntimeException(message, var5);
      }
   }

   private Certificate loadCertificateFromPath(String certificatePath) throws IOException, CertificateException {
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(certificatePath));

      Certificate cert;
      try {
         CertificateFactory cf = CertificateFactory.getInstance("X.509");
         if (bis.available() <= 0) {
            throw new OperateRuntimeException("Could not load certificate from file, file is empty. File: " + certificatePath);
         }

         cert = cf.generateCertificate(bis);
         logger.debug("Found certificate: {}", cert);
      } catch (Throwable var7) {
         try {
            bis.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      bis.close();
      return cert;
   }

   private RequestConfig.Builder setTimeouts(RequestConfig.Builder builder, ElasticsearchProperties elsConfig) {
      if (elsConfig.getSocketTimeout() != null) {
         builder.setSocketTimeout(elsConfig.getSocketTimeout());
      }

      if (elsConfig.getConnectTimeout() != null) {
         builder.setConnectTimeout(elsConfig.getConnectTimeout());
      }

      return builder;
   }

   private HttpHost getHttpHost(ElasticsearchProperties elsConfig) {
      try {
         URI uri = new URI(elsConfig.getUrl());
         return new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
      } catch (URISyntaxException var3) {
         throw new OperateRuntimeException("Error in url: " + elsConfig.getUrl(), var3);
      }
   }

   private void setupAuthentication(HttpAsyncClientBuilder builder, ElasticsearchProperties elsConfig) {
      String username = elsConfig.getUsername();
      String password = elsConfig.getPassword();
      if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
         CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
         credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
         builder.setDefaultCredentialsProvider(credentialsProvider);
      } else {
         logger.warn("Username and/or password for are empty. Basic authentication for elasticsearch is not used.");
      }
   }

   public boolean checkHealth(RestHighLevelClient esClient) {
      ElasticsearchProperties elsConfig = this.operateProperties.getElasticsearch();

      try {
         return (Boolean)RetryOperation.newBuilder().noOfRetry(50).retryOn(IOException.class, ElasticsearchException.class).delayInterval(3, TimeUnit.SECONDS).message(String.format("Connect to Elasticsearch cluster [%s] at %s", elsConfig.getClusterName(), elsConfig.getUrl())).retryConsumer(() -> {
            ClusterHealthResponse clusterHealthResponse = esClient.cluster().health(new ClusterHealthRequest(), RequestOptions.DEFAULT);
            return clusterHealthResponse.getClusterName().equals(elsConfig.getClusterName());
         }).build().retry();
      } catch (Exception var4) {
         throw new OperateRuntimeException("Couldn't connect to Elasticsearch. Abort.", var4);
      }
   }

   public static class CustomInstantDeserializer extends JsonDeserializer {
      public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
         return Instant.ofEpochMilli(Long.valueOf(parser.getText()));
      }
   }

   public static class CustomOffsetDateTimeDeserializer extends JsonDeserializer {
      private DateTimeFormatter formatter;

      public CustomOffsetDateTimeDeserializer(DateTimeFormatter formatter) {
         this.formatter = formatter;
      }

      public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
         OffsetDateTime parsedDate;
         try {
            parsedDate = OffsetDateTime.parse(parser.getText(), this.formatter);
         } catch (DateTimeParseException var5) {
            parsedDate = ZonedDateTime.parse(parser.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault())).toOffsetDateTime();
         }

         return parsedDate;
      }
   }

   public static class CustomOffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {
      private DateTimeFormatter formatter;

      public CustomOffsetDateTimeSerializer(DateTimeFormatter formatter) {
         this.formatter = formatter;
      }

      @Override
      public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
         gen.writeString(value.format(this.formatter));
      }

   }
}
