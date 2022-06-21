package io.camunda.operate.util.rest;

import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.util.RetryOperation;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.ElasticsearchException;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Scope("prototype")
public class StatefulRestTemplate extends RestTemplate {
   private static final String LOGIN_URL_PATTERN = "/api/login?username=%s&password=%s";
   private static final String CSRF_TOKEN_HEADER_NAME = "OPERATE-X-CSRF-TOKEN";
   private static final String USERNAME_DEFAULT = "demo";
   private static final String PASSWORD_DEFAULT = "demo";
   private final String host;
   private final Integer port;
   private final HttpClient httpClient;
   private final CookieStore cookieStore;
   private final HttpContext httpContext;
   private final StatefulHttpComponentsClientHttpRequestFactory statefulHttpComponentsClientHttpRequestFactory;
   private String csrfToken;
   private String contextPath;

   public StatefulRestTemplate(String host, Integer port, String contextPath) {
      this.host = host;
      this.port = port;
      this.contextPath = contextPath;
      this.httpClient = HttpClientBuilder.create().build();
      this.cookieStore = new BasicCookieStore();
      this.httpContext = new BasicHttpContext();
      this.httpContext.setAttribute("http.cookie-store", this.getCookieStore());
      this.statefulHttpComponentsClientHttpRequestFactory = new StatefulHttpComponentsClientHttpRequestFactory(this.httpClient, this.httpContext);
      super.setRequestFactory(this.statefulHttpComponentsClientHttpRequestFactory);
   }

   public HttpClient getHttpClient() {
      return this.httpClient;
   }

   public CookieStore getCookieStore() {
      return this.cookieStore;
   }

   public HttpContext getHttpContext() {
      return this.httpContext;
   }

   public ResponseEntity exchange(RequestEntity requestEntity, Class responseType) throws RestClientException {
      ResponseEntity responseEntity = super.exchange(requestEntity, responseType);
      this.saveCSRFTokenWhenAvailable(responseEntity);
      return responseEntity;
   }

   public ResponseEntity postForEntity(URI url, Object request, Class responseType) throws RestClientException {
      RequestEntity requestEntity = RequestEntity.method(HttpMethod.POST, url)
              .headers(this.getCsrfHeader()).contentType(MediaType.APPLICATION_JSON).body(request);
      ResponseEntity tResponseEntity = this.exchange(requestEntity, responseType);
      this.saveCSRFTokenWhenAvailable(tResponseEntity);
      return tResponseEntity;
   }

   public StatefulHttpComponentsClientHttpRequestFactory getStatefulHttpClientRequestFactory() {
      return this.statefulHttpComponentsClientHttpRequestFactory;
   }

   public void loginWhenNeeded() {
      this.loginWhenNeeded("demo", "demo");
   }

   public void loginWhenNeeded(String username, String password) {
      if (this.getCookieStore().getCookies().isEmpty()) {
         ResponseEntity response = this.tryLoginAs(username, password);
         if (!response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new OperateRuntimeException(String.format("Unable to login user %s to %s:%s. Response: %s", username, this.host, this.port, response));
         }

         this.saveCSRFTokenWhenAvailable(response);
      }

   }

   private ResponseEntity tryLoginAs(String username, String password) {
      try {
         return (ResponseEntity)RetryOperation.newBuilder().retryConsumer(() -> {
            return this.postForEntity(this.getURL(String.format("/api/login?username=%s&password=%s", username, password)), (Object)null, Object.class);
         }).noOfRetry(50).delayInterval(6, TimeUnit.SECONDS).retryOn(IOException.class, RestClientException.class, ElasticsearchException.class).build().retry();
      } catch (Exception var4) {
         throw new OperateRuntimeException("Unable to connect to Operate ", var4);
      }
   }

   private ResponseEntity saveCSRFTokenWhenAvailable(ResponseEntity response) {
      List csrfHeaders = response.getHeaders().get("OPERATE-X-CSRF-TOKEN");
      if (csrfHeaders != null && !csrfHeaders.isEmpty()) {
         this.csrfToken = (String)csrfHeaders.get(0);
      }

      return response;
   }

   public URI getURL(String urlPart) {
      try {
         return (new URL(String.format("http://%s:%s%s", this.host, this.port, this.contextPath + urlPart))).toURI();
      } catch (MalformedURLException | URISyntaxException var3) {
         throw new RuntimeException("Error occurred while constructing URL", var3);
      }
   }

   public URI getURL(String urlPart, String urlParams) {
      if (StringUtils.isEmpty(urlParams)) {
         return this.getURL(urlPart);
      } else {
         try {
            return (new URL(String.format("%s?%s", this.getURL(urlPart), urlParams))).toURI();
         } catch (MalformedURLException | URISyntaxException var4) {
            throw new RuntimeException("Error occurred while constructing URL", var4);
         }
      }
   }

   public Consumer<HttpHeaders> getCsrfHeader() {
      return (header) -> {
         header.add("OPERATE-X-CSRF-TOKEN", this.csrfToken);
      };
   }
}
