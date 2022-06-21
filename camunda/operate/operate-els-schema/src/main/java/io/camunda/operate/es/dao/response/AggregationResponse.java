package io.camunda.operate.es.dao.response;

import java.util.List;

public class AggregationResponse implements DAOResponse {
   private boolean error;
   private List hits;
   private int size;
   private long sumOfTotalDocs;

   private AggregationResponse() {
   }

   public AggregationResponse(boolean error) {
      this(error, (List)null, 0L);
   }

   public AggregationResponse(boolean error, List hits, long sumOfTotalDocs) {
      this.error = error;
      this.hits = hits;
      this.sumOfTotalDocs = sumOfTotalDocs;
      if (hits == null) {
         this.size = 0;
      } else {
         this.size = hits.size();
      }

   }

   public boolean hasError() {
      return this.error;
   }

   public List getHits() {
      return this.hits;
   }

   public int getSize() {
      return this.size;
   }

   public long getSumOfTotalDocs() {
      return this.sumOfTotalDocs;
   }

   public static class AggregationValue {
      private String key;
      private long count;

      public AggregationValue(String key, long count) {
         this.key = key;
         this.count = count;
      }

      public String getKey() {
         return this.key;
      }

      public void setKey(String key) {
         this.key = key;
      }

      public long getCount() {
         return this.count;
      }

      public void setCount(long count) {
         this.count = count;
      }
   }
}
