package io.camunda.operate.es.dao.response;

import java.util.List;

public class SearchResponse implements DAOResponse {
   private boolean error;
   private List hits;
   private int size;

   private SearchResponse() {
   }

   public SearchResponse(boolean error) {
      this(error, (List)null);
   }

   public SearchResponse(boolean error, List hits) {
      this.error = error;
      this.hits = hits;
      if (hits == null) {
         this.size = 0;
      } else {
         this.size = hits.size();
      }

   }

   public boolean hasError() {
      return this.error;
   }
}
