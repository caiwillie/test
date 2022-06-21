package io.camunda.operate.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryOperation<T> {
   private static final Logger logger = LoggerFactory.getLogger(RetryOperation.class);
   private RetryConsumer<T> retryConsumer;
   private int noOfRetry;
   private int delayInterval;
   private TimeUnit timeUnit;
   private RetryPredicate retryPredicate;
   private List<Class> exceptionList;
   private String message;

   public static <T> OperationBuilder<T> newBuilder() {
      return new OperationBuilder();
   }

   private RetryOperation(RetryConsumer<T> retryConsumer,
                          int noOfRetry,
                          int delayInterval,
                          TimeUnit timeUnit,
                          RetryPredicate retryPredicate,
                          List exceptionList,
                          String message) {
      this.retryConsumer = retryConsumer;
      this.noOfRetry = noOfRetry;
      this.delayInterval = delayInterval;
      this.timeUnit = timeUnit;
      this.retryPredicate = retryPredicate;
      this.exceptionList = exceptionList;
      this.message = message;
   }

   public T retry() throws Exception {
      T result = null;
      int retries = 0;

      while(retries < this.noOfRetry) {
         try {
            result = this.retryConsumer.evaluate();
            if (!Objects.nonNull(this.retryPredicate)) {
               return result;
            }

            boolean shouldItRetry = this.retryPredicate.shouldRetry(result);
            if (!shouldItRetry) {
               return result;
            }

            retries = this.increaseRetryCountAndSleep(retries);
         } catch (Exception var4) {
            logger.warn(var4.getMessage());
            retries = this.handleException(retries, var4);
         }
      }

      return result;
   }

   private int handleException(int retries, Exception e) throws Exception {
      if (!this.exceptionList.isEmpty() && !this.exceptionList.stream().anyMatch((ex) -> {
         return ex.isAssignableFrom(e.getClass());
      })) {
         throw e;
      } else {
         retries = this.increaseRetryCountAndSleep(retries);
         if (retries == this.noOfRetry) {
            throw e;
         } else {
            return retries;
         }
      }
   }

   private int increaseRetryCountAndSleep(int retries) {
      ++retries;
      if (retries < this.noOfRetry && this.delayInterval > 0) {
         try {
            if (retries % 20 == 0) {
               logger.info("{} - Waiting {} {}. {}/{}", new Object[]{this.message, this.delayInterval, this.timeUnit, retries, this.noOfRetry});
            } else {
               logger.debug("{} - Waiting {} {}. {}/{}", new Object[]{this.message, this.delayInterval, this.timeUnit, retries, this.noOfRetry});
            }

            this.timeUnit.sleep((long)this.delayInterval);
         } catch (InterruptedException var3) {
            Thread.currentThread().interrupt();
         }
      }

      return retries;
   }

   public static class OperationBuilder<T> {
      private RetryConsumer<T> iRetryConsumer;
      private int iNoOfRetry;
      private int iDelayInterval;
      private TimeUnit iTimeUnit;
      private RetryPredicate iRetryPredicate;
      private Class[] exceptionClasses;
      private String message = "";

      private OperationBuilder() {
      }

      public OperationBuilder<T> retryConsumer(RetryConsumer<T> retryConsumer) {
         this.iRetryConsumer = retryConsumer;
         return this;
      }

      public OperationBuilder<T> noOfRetry(int noOfRetry) {
         this.iNoOfRetry = noOfRetry;
         return this;
      }

      public OperationBuilder<T> delayInterval(int delayInterval, TimeUnit timeUnit) {
         this.iDelayInterval = delayInterval;
         this.iTimeUnit = timeUnit;
         return this;
      }

      public OperationBuilder<T> retryPredicate(RetryPredicate<T> retryPredicate) {
         this.iRetryPredicate = retryPredicate;
         return this;
      }

      @SafeVarargs
      public final OperationBuilder<T> retryOn(Class... exceptionClasses) {
         this.exceptionClasses = exceptionClasses;
         return this;
      }

      public OperationBuilder<T> message(String message) {
         this.message = message;
         return this;
      }

      public RetryOperation<T> build() {
         if (Objects.isNull(this.iRetryConsumer)) {
            throw new RuntimeException("'#retryConsumer:RetryConsumer<T>' not set");
         } else {
            List exceptionList = new ArrayList();
            if (Objects.nonNull(this.exceptionClasses) && this.exceptionClasses.length > 0) {
               exceptionList = Arrays.asList(this.exceptionClasses);
            }

            this.iNoOfRetry = this.iNoOfRetry == 0 ? 1 : this.iNoOfRetry;
            this.iTimeUnit = Objects.isNull(this.iTimeUnit) ? TimeUnit.MILLISECONDS : this.iTimeUnit;
            return new RetryOperation(this.iRetryConsumer, this.iNoOfRetry, this.iDelayInterval, this.iTimeUnit, this.iRetryPredicate, (List)exceptionList, this.message);
         }
      }
   }

   public interface RetryPredicate<T> {
      boolean shouldRetry(T var1);
   }

   public interface RetryConsumer<T> {
      T evaluate() throws Exception;
   }
}
