package io.camunda.operate.data.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.OperateEntity;
import io.camunda.operate.entities.dmn.DecisionInstanceEntity;
import io.camunda.operate.entities.dmn.DecisionInstanceInputEntity;
import io.camunda.operate.entities.dmn.DecisionInstanceOutputEntity;
import io.camunda.operate.entities.dmn.DecisionInstanceState;
import io.camunda.operate.entities.dmn.DecisionType;
import io.camunda.operate.entities.dmn.definition.DecisionDefinitionEntity;
import io.camunda.operate.entities.dmn.definition.DecisionRequirementsEntity;
import io.camunda.operate.exceptions.PersistenceException;
import io.camunda.operate.schema.indices.DecisionIndex;
import io.camunda.operate.schema.indices.DecisionRequirementsIndex;
import io.camunda.operate.schema.templates.DecisionInstanceTemplate;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.util.PayloadUtil;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DecisionDataUtil {
   public static final String DECISION_INSTANCE_ID_1_1 = "12121212-1";
   public static final String DECISION_INSTANCE_ID_1_2 = "12121212-2";
   public static final String DECISION_INSTANCE_ID_1_3 = "12121212-3";
   public static final String DECISION_INSTANCE_ID_2_1 = "13131313-1";
   public static final String DECISION_INSTANCE_ID_2_2 = "13131313-2";
   public static final String DECISION_DEFINITION_ID_1 = "decisionDef1";
   public static final String DECISION_DEFINITION_ID_2 = "decisionDef2";
   public static final long PROCESS_INSTANCE_ID = 555555L;
   public static final String DECISION_DEFINITION_NAME_1 = "Assign Approver Group";
   public static final String DECISION_ID_1 = "invoice-assign-approver";
   public static final String DECISION_ID_2 = "invoiceClassification";
   private Map entityToESAliasMap;
   private Random random = new Random();
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   protected RestHighLevelClient esClient;
   @Autowired
   private DecisionInstanceTemplate decisionInstanceTemplate;
   @Autowired
   private DecisionRequirementsIndex decisionRequirementsIndex;
   @Autowired
   private DecisionIndex decisionIndex;
   @Autowired
   private PayloadUtil payloadUtil;

   public List createDecisionDefinitions() {
      List decisionEntities = new ArrayList();
      decisionEntities.add(((DecisionRequirementsEntity)((DecisionRequirementsEntity)(new DecisionRequirementsEntity()).setId("1111")).setKey(1111L)).setDecisionRequirementsId("invoiceBusinessDecisions").setName("Invoice Business Decisions").setVersion(1).setXml(this.payloadUtil.readStringFromClasspath("/usertest/invoiceBusinessDecisions_v_1.dmn")).setResourceName("invoiceBusinessDecisions_v_1.dmn"));
      decisionEntities.add(((DecisionDefinitionEntity)((DecisionDefinitionEntity)(new DecisionDefinitionEntity()).setId("1222")).setKey(1222L)).setDecisionId("invoiceClassification").setName("Invoice Classification").setVersion(1).setDecisionRequirementsId("invoiceBusinessDecisions").setDecisionRequirementsKey(1111L));
      decisionEntities.add(((DecisionDefinitionEntity)((DecisionDefinitionEntity)(new DecisionDefinitionEntity()).setId("1333")).setKey(1333L)).setDecisionId("invoice-assign-approver").setName("Assign Approver Group").setVersion(1).setDecisionRequirementsId("invoiceBusinessDecisions").setDecisionRequirementsKey(1111L));
      decisionEntities.add(((DecisionRequirementsEntity)((DecisionRequirementsEntity)(new DecisionRequirementsEntity()).setId("2222")).setKey(2222L)).setDecisionRequirementsId("invoiceBusinessDecisions").setName("Invoice Business Decisions").setVersion(2).setXml(this.payloadUtil.readStringFromClasspath("/usertest/invoiceBusinessDecisions_v_2.dmn")).setResourceName("invoiceBusinessDecisions_v_2.dmn"));
      decisionEntities.add(((DecisionDefinitionEntity)((DecisionDefinitionEntity)(new DecisionDefinitionEntity()).setId("2222")).setKey(2222L)).setDecisionId("invoiceClassification").setName("Invoice Classification").setVersion(2).setDecisionRequirementsId("invoiceBusinessDecisions").setDecisionRequirementsKey(2222L));
      decisionEntities.add(((DecisionDefinitionEntity)((DecisionDefinitionEntity)(new DecisionDefinitionEntity()).setId("2333")).setKey(2333L)).setDecisionId("invoice-assign-approver").setName("Assign Approver Group").setVersion(2).setDecisionRequirementsId("invoiceBusinessDecisions").setDecisionRequirementsKey(2222L));
      return decisionEntities;
   }

   public List createDecisionInstances() {
      List result = new ArrayList();
      result.add(this.createDecisionInstance("12121212-1", DecisionInstanceState.EVALUATED, "Assign Approver Group", OffsetDateTime.now(), "decisionDef1", 1, "invoice-assign-approver", 35467L, 555555L));
      result.add(this.createDecisionInstance("12121212-2", DecisionInstanceState.EVALUATED, "Invoice Classification", OffsetDateTime.now(), "decisionDef2", 1, "invoiceClassification", 35467L, (long)this.random.nextInt(1000)));
      result.add(this.createDecisionInstance("12121212-3", DecisionInstanceState.EVALUATED, "Invoice Classification", OffsetDateTime.now(), "decisionDef2", 2, "invoiceClassification", 35467L, (long)this.random.nextInt(1000)));
      result.add(this.createDecisionInstance("13131313-1", DecisionInstanceState.FAILED, "Assign Approver Group", OffsetDateTime.now(), "decisionDef1", 1, "invoice-assign-approver", 35467L, 555555L));
      result.add(this.createDecisionInstance("13131313-2", DecisionInstanceState.FAILED, "Invoice Classification", OffsetDateTime.now(), "decisionDef2", 2, "invoiceClassification", 35467L, (long)this.random.nextInt(1000)));
      return result;
   }

   public DecisionInstanceEntity createDecisionInstance(OffsetDateTime evaluationDate) {
      return this.createDecisionInstance(this.random.nextInt(1) == 0 ? DecisionInstanceState.EVALUATED : DecisionInstanceState.FAILED, UUID.randomUUID().toString(), evaluationDate, this.random.nextInt(1) == 0 ? "decisionDef1" : "decisionDef2", 1, UUID.randomUUID().toString(), (long)this.random.nextInt(1000), (long)this.random.nextInt(1000));
   }

   public DecisionInstanceEntity createDecisionInstance(DecisionInstanceState state, String decisionName, OffsetDateTime evaluationDate, String decisionDefinitionId, int decisionVersion, String decisionId, long processDefinitionKey, long processInstanceKey) {
      return this.createDecisionInstance(String.valueOf(this.random.nextInt(1000)) + "-1", state, decisionName, evaluationDate, decisionDefinitionId, decisionVersion, decisionId, processDefinitionKey, processInstanceKey);
   }

   private DecisionInstanceEntity createDecisionInstance(String decisionInstanceId, DecisionInstanceState state, String decisionName, OffsetDateTime evaluationDate, String decisionDefinitionId, int decisionVersion, String decisionId, long processDefinitionKey, long processInstanceKey) {
      List inputs = new ArrayList();
      inputs.add((new DecisionInstanceInputEntity()).setId("InputClause_0og2hn3").setName("Invoice Classification").setValue("day-to-day expense"));
      inputs.add((new DecisionInstanceInputEntity()).setId("InputClause_0og2hn3").setName("Invoice Classification").setValue("budget"));
      List outputs = new ArrayList();
      outputs.add((new DecisionInstanceOutputEntity()).setId("OutputClause_1cthd0w").setName("Approver Group").setValue("budget").setRuleIndex(2).setRuleId("row-49839158-5"));
      outputs.add((new DecisionInstanceOutputEntity()).setId("OutputClause_1cthd0w").setName("Approver Group").setValue("sales").setRuleIndex(1).setRuleId("row-49839158-6"));
      outputs.add((new DecisionInstanceOutputEntity()).setId("OutputClause_1cthd0w").setName("Approver Group").setValue("accounting").setRuleIndex(1).setRuleId("row-49839158-1"));
      String evaluationFailure = null;
      if (state == DecisionInstanceState.FAILED) {
         evaluationFailure = "Variable not found: invoiceClassification";
      }

      return ((DecisionInstanceEntity)((DecisionInstanceEntity)(new DecisionInstanceEntity()).setId(decisionInstanceId)).setKey(Long.valueOf(decisionInstanceId.split("-")[0]))).setExecutionIndex(Integer.valueOf(decisionInstanceId.split("-")[1])).setState(state).setEvaluationFailure(evaluationFailure).setDecisionName(decisionName).setDecisionVersion(decisionVersion).setDecisionType(DecisionType.DECISION_TABLE).setEvaluationDate(evaluationDate).setDecisionDefinitionId(decisionDefinitionId).setDecisionId(decisionId).setDecisionRequirementsId("1111").setDecisionRequirementsKey(1111L).setElementId("taskA").setElementInstanceKey(76543L).setEvaluatedInputs(inputs).setEvaluatedOutputs(outputs).setPosition(1000L).setProcessDefinitionKey(processDefinitionKey).setProcessInstanceKey(processInstanceKey).setResult("{\"total\": 100.0}");
   }

   public void persistOperateEntities(List operateEntities) throws PersistenceException {
      try {
         BulkRequest bulkRequest = new BulkRequest();
         Iterator var3 = operateEntities.iterator();

         while(var3.hasNext()) {
            OperateEntity entity = (OperateEntity)var3.next();
            String alias = (String)this.getEntityToESAliasMap().get(entity.getClass());
            if (alias == null) {
               throw new RuntimeException("Index not configured for " + entity.getClass().getName());
            }

            IndexRequest indexRequest = (new IndexRequest(alias)).id(entity.getId()).source(this.objectMapper.writeValueAsString(entity), XContentType.JSON);
            bulkRequest.add(indexRequest);
         }

         ElasticsearchUtil.processBulkRequest(this.esClient, bulkRequest, true);
      } catch (Exception var7) {
         throw new PersistenceException(var7);
      }
   }

   public Map getEntityToESAliasMap() {
      if (this.entityToESAliasMap == null) {
         this.entityToESAliasMap = new HashMap();
         this.entityToESAliasMap.put(DecisionInstanceEntity.class, this.decisionInstanceTemplate.getFullQualifiedName());
         this.entityToESAliasMap.put(DecisionRequirementsEntity.class, this.decisionRequirementsIndex.getFullQualifiedName());
         this.entityToESAliasMap.put(DecisionDefinitionEntity.class, this.decisionIndex.getFullQualifiedName());
      }

      return this.entityToESAliasMap;
   }
}
