package io.camunda.operate.logging.stackdriver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public final class ReportLocation {
   @JsonProperty("functionName")
   private String functionName;
   @JsonProperty("filePath")
   private String filePath;
   @JsonProperty("lineNumber")
   private int lineNumber;

   public String getFilePath() {
      return this.filePath;
   }

   public void setFilePath(String filePath) {
      this.filePath = filePath;
   }

   public String getFunctionName() {
      return this.functionName;
   }

   public void setFunctionName(String functionName) {
      this.functionName = functionName;
   }

   public int getLineNumber() {
      return this.lineNumber;
   }

   public void setLineNumber(int lineNumber) {
      this.lineNumber = lineNumber;
   }
}
