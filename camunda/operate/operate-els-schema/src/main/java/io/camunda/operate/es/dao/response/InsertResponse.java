package io.camunda.operate.es.dao.response;

public class InsertResponse implements DAOResponse {
   private boolean error;

   public static InsertResponse success() {
      return buildInsertResponse(false);
   }

   public static InsertResponse failure() {
      return buildInsertResponse(true);
   }

   private static InsertResponse buildInsertResponse(boolean error) {
      InsertResponse insertResponse = new InsertResponse();
      insertResponse.error = error;
      return insertResponse;
   }

   public boolean hasError() {
      return this.error;
   }
}
