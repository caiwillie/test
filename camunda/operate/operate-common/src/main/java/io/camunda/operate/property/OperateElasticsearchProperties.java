package io.camunda.operate.property;

public class OperateElasticsearchProperties extends ElasticsearchProperties {
   public static final String DEFAULT_INDEX_PREFIX = "operate";
   private static final int DEFAULT_NUMBER_OF_SHARDS = 1;
   private static final int DEFAULT_NUMBER_OF_REPLICAS = 0;
   private static final String DEFAULT_REFRESH_INTERVAL = "1s";
   private String indexPrefix = "operate";
   private int numberOfShards = 1;
   private int numberOfReplicas = 0;
   private String refreshInterval = "1s";

   public String getIndexPrefix() {
      return this.indexPrefix;
   }

   public void setIndexPrefix(String indexPrefix) {
      this.indexPrefix = indexPrefix;
   }

   public void setDefaultIndexPrefix() {
      this.setIndexPrefix("operate");
   }

   public int getNumberOfShards() {
      return this.numberOfShards;
   }

   public void setNumberOfShards(int numberOfShards) {
      this.numberOfShards = numberOfShards;
   }

   public int getNumberOfReplicas() {
      return this.numberOfReplicas;
   }

   public void setNumberOfReplicas(int numberOfReplicas) {
      this.numberOfReplicas = numberOfReplicas;
   }

   public void setRefreshInterval(String refreshInterval) {
      this.refreshInterval = refreshInterval;
   }

   public String getRefreshInterval() {
      return this.refreshInterval;
   }
}
