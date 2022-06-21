package io.camunda.operate.logging.stackdriver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
final class SourceLocation {
   @JsonProperty("function")
   private String function;
   @JsonProperty("file")
   private String file;
   @JsonProperty("line")
   private int line;

   public String getFile() {
      return this.file;
   }

   public void setFile(String file) {
      this.file = file;
   }

   public String getFunction() {
      return this.function;
   }

   public void setFunction(String function) {
      this.function = function;
   }

   public int getLine() {
      return this.line;
   }

   public void setLine(int line) {
      this.line = line;
   }
}
