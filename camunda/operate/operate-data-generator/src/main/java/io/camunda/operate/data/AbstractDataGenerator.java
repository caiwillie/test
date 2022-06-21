package io.camunda.operate.data;

import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.ThreadUtil;
import io.camunda.zeebe.client.ZeebeClient;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class AbstractDataGenerator implements DataGenerator {
   private static final Logger logger = LoggerFactory.getLogger(AbstractDataGenerator.class);
   private boolean shutdown = false;
   @Autowired
   protected ZeebeClient client;
   @Autowired
   @Qualifier("zeebeEsClient")
   private RestHighLevelClient zeebeEsClient;
   @Autowired
   private OperateProperties operateProperties;
   protected boolean manuallyCalled = false;
   protected ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

   @PreDestroy
   public void shutdown() {
      logger.info("Shutdown DataGenerator");
      this.shutdown = true;
      if (this.scheduler != null && !this.scheduler.isShutdown()) {
         this.scheduler.shutdown();

         try {
            if (!this.scheduler.awaitTermination(200L, TimeUnit.MILLISECONDS)) {
               this.scheduler.shutdownNow();
            }
         } catch (InterruptedException var2) {
            this.scheduler.shutdownNow();
         }
      }

   }

   public void createZeebeDataAsync(boolean manuallyCalled) {
      this.scheduler.execute(() -> {
         Boolean zeebeDataCreated = null;

         while(zeebeDataCreated == null && !this.shutdown) {
            try {
               zeebeDataCreated = this.createZeebeData(manuallyCalled);
            } catch (Exception var4) {
               logger.error(String.format("Error occurred when creating demo data: %s. Retrying...", var4.getMessage()), var4);
               ThreadUtil.sleepFor(2000L);
            }
         }

      });
   }

   public boolean createZeebeData(boolean manuallyCalled) {
      this.manuallyCalled = manuallyCalled;
      return this.shouldCreateData(manuallyCalled);
   }

   public boolean shouldCreateData(boolean manuallyCalled) {
      if (!manuallyCalled) {
         try {
            GetIndexRequest request = new GetIndexRequest(new String[]{this.operateProperties.getZeebeElasticsearch().getPrefix() + "*"});
            request.indicesOptions(IndicesOptions.fromOptions(true, false, true, false));
            boolean exists = this.zeebeEsClient.indices().exists(request, RequestOptions.DEFAULT);
            if (exists) {
               logger.debug("Data already exists in Zeebe.");
               return false;
            }
         } catch (IOException var4) {
            logger.debug("Error occurred while checking existance of data in Zeebe: {}. Demo data won't be created.", var4.getMessage());
            return false;
         }
      }

      return true;
   }
}
