package io.camunda.operate.zeebe;

import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.util.CollectionUtil;
import io.camunda.operate.util.ThreadUtil;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Topology;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PartitionHolder {
   public static final long WAIT_TIME_IN_MS = 1000L;
   public static final int MAX_RETRY = 60;
   private static final Logger logger = LoggerFactory.getLogger(PartitionHolder.class);
   private List partitionIds = new ArrayList();
   @Autowired
   private OperateProperties operateProperties;
   @Autowired
   private ZeebeClient zeebeClient;

   public List getPartitionIds() {
      return this.getPartitionIdsWithWaitingTimeAndRetries(1000L, 60);
   }

   private List getPartitionIdsWithWaitingTimeAndRetries(long waitingTimeInMilliseconds, int maxRetries) {
      int retries = 0;

      while(this.partitionIds.isEmpty() && retries <= maxRetries) {
         if (retries > 0) {
            this.sleepFor(waitingTimeInMilliseconds);
         }

         ++retries;
         Optional zeebePartitionIds = this.getPartitionIdsFromZeebe();
         if (zeebePartitionIds.isPresent()) {
            this.partitionIds = this.extractCurrentNodePartitions((List)zeebePartitionIds.get());
         } else if (retries <= maxRetries) {
            logger.info("Partition ids can't be fetched from Zeebe. Try next round ({}).", retries);
         } else {
            logger.info("Partition ids can't be fetched from Zeebe. Return empty partition ids list.");
         }
      }

      return this.partitionIds;
   }

   protected List extractCurrentNodePartitions(List partitionIds) {
      Integer[] configuredIds = this.operateProperties.getClusterNode().getPartitionIds();
      if (configuredIds != null && configuredIds.length > 0) {
         partitionIds.retainAll(Arrays.asList(configuredIds));
      } else if (this.operateProperties.getClusterNode().getNodeCount() != null && this.operateProperties.getClusterNode().getCurrentNodeId() != null) {
         Integer nodeCount = this.operateProperties.getClusterNode().getNodeCount();
         Integer nodeId = this.operateProperties.getClusterNode().getCurrentNodeId();
         if (nodeId >= nodeCount) {
            logger.warn("Misconfiguration: nodeId [{}] must be strictly less than nodeCount [{}]. No partitions will be selected.", nodeId, nodeCount);
         }

         partitionIds = CollectionUtil.splitAndGetSublist(partitionIds, nodeCount, nodeId);
      }

      return partitionIds;
   }

   protected Optional getPartitionIdsFromZeebe() {
      logger.debug("Requesting partition ids from Zeebe client");

      try {
         Topology topology = (Topology)this.zeebeClient.newTopologyRequest().send().join();
         int partitionCount = topology.getPartitionsCount();
         if (partitionCount > 0) {
            return Optional.of(CollectionUtil.fromTo(1, partitionCount));
         }
      } catch (Exception var3) {
         logger.warn("Error occurred when requesting partition ids from Zeebe client: " + var3.getMessage(), var3);
      }

      return Optional.empty();
   }

   protected void sleepFor(long milliseconds) {
      ThreadUtil.sleepFor(milliseconds);
   }

   public void setZeebeClient(ZeebeClient zeebeClient) {
      this.zeebeClient = zeebeClient;
   }
}
