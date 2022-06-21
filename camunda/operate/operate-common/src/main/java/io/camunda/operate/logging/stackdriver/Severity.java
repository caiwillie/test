package io.camunda.operate.logging.stackdriver;

public enum Severity {
   DEFAULT(0),
   DEBUG(100),
   INFO(200),
   NOTICE(300),
   WARNING(400),
   ERROR(500),
   CRITICAL(600),
   ALERT(700),
   EMERGENCY(800);

   private final int level;

   private Severity(int level) {
      this.level = level;
   }

   public int getLevel() {
      return this.level;
   }
}
