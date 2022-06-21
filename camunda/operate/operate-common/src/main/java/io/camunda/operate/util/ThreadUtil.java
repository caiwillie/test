package io.camunda.operate.util;

public class ThreadUtil {
   public static long sleepFor(long milliseconds) {
      try {
         Thread.sleep(milliseconds);
      } catch (InterruptedException var3) {
         Thread.currentThread().interrupt();
      }

      return milliseconds;
   }
}
