package io.camunda.operate.property;

public class ClusterNodeProperties {
   private Integer[] partitionIds = new Integer[0];
   private Integer nodeCount;
   private Integer currentNodeId;

   public Integer[] getPartitionIds() {
      return this.partitionIds;
   }

   public void setPartitionIds(Integer[] partitionIds) {
      this.partitionIds = partitionIds;
   }

   public Integer getNodeCount() {
      return this.nodeCount;
   }

   public void setNodeCount(Integer nodeCount) {
      this.nodeCount = nodeCount;
   }

   public Integer getCurrentNodeId() {
      return this.currentNodeId;
   }

   public void setCurrentNodeId(Integer currentNodeId) {
      this.currentNodeId = currentNodeId;
   }
}
