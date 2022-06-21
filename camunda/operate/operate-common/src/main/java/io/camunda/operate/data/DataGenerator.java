package io.camunda.operate.data;

public interface DataGenerator {
   DataGenerator DO_NOTHING = (manuallyCalled) -> {
   };

   void createZeebeDataAsync(boolean var1);
}
