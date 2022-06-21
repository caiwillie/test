package io.camunda.operate.property;

import io.camunda.operate.util.ConversionUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

public class ElasticsearchProperties {
   public static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
   public static final String ELS_DATE_FORMAT_DEFAULT = "date_time";
   private String clusterName = "elasticsearch";
   /** @deprecated */
   @Deprecated
   private String host = "localhost";
   /** @deprecated */
   @Deprecated
   private int port = 9200;
   private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
   private String elsDateFormat = "date_time";
   private int batchSize = 200;
   private Integer socketTimeout;
   private Integer connectTimeout;
   private boolean createSchema = true;
   private String url;
   private String username;
   private String password;
   @NestedConfigurationProperty
   private SslProperties ssl;

   public String getClusterName() {
      return this.clusterName;
   }

   public void setClusterName(String clusterName) {
      this.clusterName = clusterName;
   }

   /** @deprecated */
   @Deprecated
   public String getHost() {
      return (String)this.getFromURIorDefault(URI::getHost, this.host);
   }

   /** @deprecated */
   @Deprecated
   public void setHost(String host) {
      this.host = host;
   }

   /** @deprecated */
   @Deprecated
   public int getPort() {
      return (Integer)this.getFromURIorDefault(URI::getPort, this.port);
   }

   private Object getFromURIorDefault(Function<URI, Object> valueFromURI, Object defaultValue) {
      if (!ConversionUtils.stringIsEmpty(this.url)) {
         try {
            return valueFromURI.apply(new URI(this.url));
         } catch (URISyntaxException var4) {
            return defaultValue;
         }
      } else {
         return defaultValue;
      }
   }

   /** @deprecated */
   @Deprecated
   public void setPort(int port) {
      this.port = port;
   }

   public String getDateFormat() {
      return this.dateFormat;
   }

   public void setDateFormat(String dateFormat) {
      this.dateFormat = dateFormat;
   }

   public String getElsDateFormat() {
      return this.elsDateFormat;
   }

   public void setElsDateFormat(String elsDateFormat) {
      this.elsDateFormat = elsDateFormat;
   }

   public int getBatchSize() {
      return this.batchSize;
   }

   public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
   }

   public boolean isCreateSchema() {
      return this.createSchema;
   }

   public void setCreateSchema(boolean createSchema) {
      this.createSchema = createSchema;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getUrl() {
      return ConversionUtils.stringIsEmpty(this.url) ? String.format("http://%s:%d", this.getHost(), this.getPort()) : this.url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public Integer getSocketTimeout() {
      return this.socketTimeout;
   }

   public void setSocketTimeout(Integer socketTimeout) {
      this.socketTimeout = socketTimeout;
   }

   public Integer getConnectTimeout() {
      return this.connectTimeout;
   }

   public void setConnectTimeout(Integer connectTimeout) {
      this.connectTimeout = connectTimeout;
   }

   public SslProperties getSsl() {
      return this.ssl;
   }

   public void setSsl(SslProperties ssl) {
      this.ssl = ssl;
   }
}
