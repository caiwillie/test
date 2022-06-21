package io.camunda.operate.property;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties("camunda.operate")
public class OperateProperties {
   public static final String PREFIX = "camunda.operate";
   public static final long BATCH_OPERATION_MAX_SIZE_DEFAULT = 1000000L;
   private boolean importerEnabled = true;
   private boolean archiverEnabled = true;
   private boolean webappEnabled = true;
   private boolean persistentSessionsEnabled = false;
   /** @deprecated */
   @Deprecated
   private boolean csrfPreventionEnabled = true;
   private String userId = "demo";
   private String displayName = "demo";
   private String password = "demo";
   private List roles = List.of("OWNER");
   private Long batchOperationMaxSize = 1000000L;
   private boolean enterprise = false;
   @NestedConfigurationProperty
   private OperateElasticsearchProperties elasticsearch = new OperateElasticsearchProperties();
   @NestedConfigurationProperty
   private ZeebeElasticsearchProperties zeebeElasticsearch = new ZeebeElasticsearchProperties();
   @NestedConfigurationProperty
   private ZeebeProperties zeebe = new ZeebeProperties();
   @NestedConfigurationProperty
   private OperationExecutorProperties operationExecutor = new OperationExecutorProperties();
   @NestedConfigurationProperty
   private ImportProperties importer = new ImportProperties();
   @NestedConfigurationProperty
   private ArchiverProperties archiver = new ArchiverProperties();
   @NestedConfigurationProperty
   private ClusterNodeProperties clusterNode = new ClusterNodeProperties();
   @NestedConfigurationProperty
   private LdapProperties ldap = new LdapProperties();
   @NestedConfigurationProperty
   private Auth0Properties auth0 = new Auth0Properties();
   @NestedConfigurationProperty
   private IdentityProperties identity = new IdentityProperties();
   @NestedConfigurationProperty
   private AlertingProperties alert = new AlertingProperties();
   @NestedConfigurationProperty
   private CloudProperties cloud = new CloudProperties();
   @NestedConfigurationProperty
   private OAuthClientProperties client = new OAuthClientProperties();

   public boolean isImporterEnabled() {
      return this.importerEnabled;
   }

   public void setImporterEnabled(boolean importerEnabled) {
      this.importerEnabled = importerEnabled;
   }

   public boolean isArchiverEnabled() {
      return this.archiverEnabled;
   }

   public void setArchiverEnabled(boolean archiverEnabled) {
      this.archiverEnabled = archiverEnabled;
   }

   public boolean isWebappEnabled() {
      return this.webappEnabled;
   }

   public void setWebappEnabled(boolean webappEnabled) {
      this.webappEnabled = webappEnabled;
   }

   public Long getBatchOperationMaxSize() {
      return this.batchOperationMaxSize;
   }

   public void setBatchOperationMaxSize(Long batchOperationMaxSize) {
      this.batchOperationMaxSize = batchOperationMaxSize;
   }

   /** @deprecated */
   @Deprecated
   public boolean isCsrfPreventionEnabled() {
      return this.csrfPreventionEnabled;
   }

   /** @deprecated */
   @Deprecated
   public void setCsrfPreventionEnabled(boolean csrfPreventionEnabled) {
      this.csrfPreventionEnabled = csrfPreventionEnabled;
   }

   public OperateElasticsearchProperties getElasticsearch() {
      return this.elasticsearch;
   }

   public void setElasticsearch(OperateElasticsearchProperties elasticsearch) {
      this.elasticsearch = elasticsearch;
   }

   public ZeebeElasticsearchProperties getZeebeElasticsearch() {
      return this.zeebeElasticsearch;
   }

   public void setZeebeElasticsearch(ZeebeElasticsearchProperties zeebeElasticsearch) {
      this.zeebeElasticsearch = zeebeElasticsearch;
   }

   public ZeebeProperties getZeebe() {
      return this.zeebe;
   }

   public void setZeebe(ZeebeProperties zeebe) {
      this.zeebe = zeebe;
   }

   public LdapProperties getLdap() {
      return this.ldap;
   }

   public void setLdap(LdapProperties ldap) {
      this.ldap = ldap;
   }

   public String getUserId() {
      return this.userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public OperationExecutorProperties getOperationExecutor() {
      return this.operationExecutor;
   }

   public void setOperationExecutor(OperationExecutorProperties operationExecutor) {
      this.operationExecutor = operationExecutor;
   }

   public ImportProperties getImporter() {
      return this.importer;
   }

   public void setImporter(ImportProperties importer) {
      this.importer = importer;
   }

   public ArchiverProperties getArchiver() {
      return this.archiver;
   }

   public void setArchiver(ArchiverProperties archiver) {
      this.archiver = archiver;
   }

   public ClusterNodeProperties getClusterNode() {
      return this.clusterNode;
   }

   public void setClusterNode(ClusterNodeProperties clusterNode) {
      this.clusterNode = clusterNode;
   }

   public boolean isEnterprise() {
      return this.enterprise;
   }

   public void setEnterprise(boolean enterprise) {
      this.enterprise = enterprise;
   }

   public Auth0Properties getAuth0() {
      return this.auth0;
   }

   public OperateProperties setAuth0(Auth0Properties auth0) {
      this.auth0 = auth0;
      return this;
   }

   public IdentityProperties getIdentity() {
      return this.identity;
   }

   public void setIdentity(IdentityProperties identity) {
      this.identity = identity;
   }

   public AlertingProperties getAlert() {
      return this.alert;
   }

   public OperateProperties setAlert(AlertingProperties alert) {
      this.alert = alert;
      return this;
   }

   public CloudProperties getCloud() {
      return this.cloud;
   }

   public OperateProperties setCloud(CloudProperties cloud) {
      this.cloud = cloud;
      return this;
   }

   public boolean isPersistentSessionsEnabled() {
      return this.persistentSessionsEnabled;
   }

   public OperateProperties setPersistentSessionsEnabled(boolean persistentSessionsEnabled) {
      this.persistentSessionsEnabled = persistentSessionsEnabled;
      return this;
   }

   public List getRoles() {
      return this.roles;
   }

   public void setRoles(List roles) {
      this.roles = roles;
   }

   public OAuthClientProperties getClient() {
      return this.client;
   }

   public void setClient(OAuthClientProperties client) {
      this.client = client;
   }
}
