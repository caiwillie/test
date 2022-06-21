package io.camunda.operate.data.develop;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.data.usertest.UserTestDataGenerator;
import io.camunda.operate.entities.OperationType;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.templates.FlowNodeInstanceTemplate;
import io.camunda.operate.schema.templates.ListViewTemplate;
import io.camunda.operate.util.ZeebeTestUtil;
import io.camunda.operate.util.rest.StatefulRestTemplate;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;
import javax.annotation.PostConstruct;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("dataGenerator")
@Profile({"dev-data"})
public class DevelopDataGenerator extends UserTestDataGenerator {
   private static final String OPERATE_HOST = "localhost";
   private static final int OPERATE_PORT = 8080;
   private static final String OPERATE_USER = "demo";
   private static final String OPERATE_PASSWORD = "demo";
   private List processInstanceKeys = new ArrayList();
   @Autowired
   private BiFunction statefulRestTemplateFactory;
   private StatefulRestTemplate restTemplate;
   @Autowired
   private RestHighLevelClient esClient;
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private FlowNodeInstanceTemplate flowNodeInstanceTemplate;
   @Autowired
   private ListViewTemplate listViewTemplate;
   private Random random = new Random();

   @PostConstruct
   private void initRestTemplate() {
      this.restTemplate = (StatefulRestTemplate)this.statefulRestTemplateFactory.apply("localhost", 8080);
   }

   public void createSpecialDataV1() {
      int orderId = this.random.nextInt(10);
      long instanceKey = ZeebeTestUtil.startProcessInstance(this.client, "interruptingBoundaryEvent", "{\"orderId\": \"" + orderId + "\"\n}");
      this.doNotTouchProcessInstanceKeys.add(instanceKey);
      this.sendMessages("interruptTask1", "{\"messageVar\": \"someValue\"\n}", 1, String.valueOf(orderId));
      orderId = this.random.nextInt(10);
      instanceKey = ZeebeTestUtil.startProcessInstance(this.client, "interruptingBoundaryEvent", "{\"orderId\": \"" + orderId + "\"\n}");
      this.doNotTouchProcessInstanceKeys.add(instanceKey);
      this.sendMessages("interruptTask1", "{\"messageVar\": \"someValue\"\n}", 1, String.valueOf(orderId));
      this.completeTask(instanceKey, "task2", (String)null);
      orderId = this.random.nextInt(10);
      instanceKey = ZeebeTestUtil.startProcessInstance(this.client, "nonInterruptingBoundaryEvent", "{\"orderId\": \"" + orderId + "\"\n}");
      this.doNotTouchProcessInstanceKeys.add(instanceKey);
      this.sendMessages("messageTask1", "{\"messageVar\": \"someValue\"\n}", 1, String.valueOf(orderId));
      orderId = this.random.nextInt(10);
      instanceKey = ZeebeTestUtil.startProcessInstance(this.client, "nonInterruptingBoundaryEvent", "{\"orderId\": \"" + orderId + "\"\n}");
      this.doNotTouchProcessInstanceKeys.add(instanceKey);
      this.sendMessages("messageTask1", "{\"messageVar\": \"someValue\"\n}", 1, String.valueOf(orderId));
      this.failTask(instanceKey, "task1", "error");
      orderId = this.random.nextInt(10);
      instanceKey = ZeebeTestUtil.startProcessInstance(this.client, "nonInterruptingBoundaryEvent", "{\"orderId\": \"" + orderId + "\"\n}");
      this.doNotTouchProcessInstanceKeys.add(instanceKey);
      this.sendMessages("messageTask1", "{\"messageVar\": \"someValue\"\n}", 1, String.valueOf(orderId));
      this.completeTask(instanceKey, "task1", (String)null);
   }

   protected void progressProcessInstances() {
      super.progressProcessInstances();
      this.jobWorkers.add(this.progressTaskA());
      this.jobWorkers.add(this.progressSimpleTask("taskB"));
      this.jobWorkers.add(this.progressSimpleTask("taskC"));
      this.jobWorkers.add(this.progressSimpleTask("taskD"));
      this.jobWorkers.add(this.progressSimpleTask("taskE"));
      this.jobWorkers.add(this.progressSimpleTask("taskF"));
      this.jobWorkers.add(this.progressSimpleTask("taskG"));
      this.jobWorkers.add(this.progressSimpleTask("taskH"));
      this.jobWorkers.add(this.progressSimpleTask("upperTask"));
      this.jobWorkers.add(this.progressSimpleTask("lowerTask"));
      this.jobWorkers.add(this.progressSimpleTask("subprocessTask"));
      this.jobWorkers.add(this.progressSimpleTask("messageTask"));
      this.jobWorkers.add(this.progressSimpleTask("afterMessageTask"));
      this.jobWorkers.add(this.progressSimpleTask("messageTaskInterrupted"));
      this.jobWorkers.add(this.progressSimpleTask("timerTask"));
      this.jobWorkers.add(this.progressSimpleTask("afterTimerTask"));
      this.jobWorkers.add(this.progressSimpleTask("timerTaskInterrupted"));
      this.jobWorkers.add(this.progressSimpleTask("lastTask"));
      this.jobWorkers.add(this.progressSimpleTask("task1"));
      this.jobWorkers.add(this.progressSimpleTask("task2"));
      this.jobWorkers.add(this.progressSimpleTask("called-task"));
      this.jobWorkers.add(this.progressSimpleTask("parentProcessTask"));
      this.jobWorkers.add(this.progressSimpleTask("subprocessTask"));
      this.jobWorkers.add(this.progressSimpleTask("subSubprocessTask"));
      this.jobWorkers.add(this.progressSimpleTask("eventSupbprocessTask"));
      this.jobWorkers.add(this.progressBigProcessTaskA());
      this.jobWorkers.add(this.progressBigProcessTaskB());
      this.jobWorkers.add(this.progressErrorTask());
      this.sendMessages("clientMessage", "{\"messageVar\": \"someValue\"}", 20);
      this.sendMessages("interruptMessageTask", "{\"messageVar2\": \"someValue2\"}", 20);
      this.sendMessages("dataReceived", "{\"messageVar3\": \"someValue3\"}", 20);
   }

   protected void createOperations() {
      this.restTemplate.loginWhenNeeded("demo", "demo");
      int operationsCount = this.random.nextInt(20) + 90;

      for(int i = 0; i < operationsCount; ++i) {
         int no = this.random.nextInt(operationsCount);
         Long processInstanceKey = (Long)this.processInstanceKeys.get(no);
         OperationType type = this.getType(i);
         Map request = this.getCreateBatchOperationRequestBody(processInstanceKey, type);
         RequestEntity requestEntity = RequestEntity.method(HttpMethod.POST, this.restTemplate.getURL("/api/process-instances/batch-operation")).contentType(MediaType.APPLICATION_JSON).body(request);
         ResponseEntity response = this.restTemplate.exchange(requestEntity, String.class);
         if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw new OperateRuntimeException(String.format("Unable to create operations. REST response: %s", response));
         }
      }

   }

   private Map getCreateBatchOperationRequestBody(Long processInstanceKey, OperationType type) {
      Map request = new HashMap();
      Map listViewRequest = new HashMap();
      listViewRequest.put("running", true);
      listViewRequest.put("active", true);
      listViewRequest.put("ids", new Long[]{processInstanceKey});
      request.put("query", listViewRequest);
      request.put("operationType", type.toString());
      return request;
   }

   private OperationType getType(int i) {
      return i % 2 == 0 ? OperationType.CANCEL_PROCESS_INSTANCE : OperationType.RESOLVE_INCIDENT;
   }

   private void sendMessages(String messageName, String payload, int count, String correlationKey) {
      for(int i = 0; i < count; ++i) {
         this.client.newPublishMessageCommand().messageName(messageName).correlationKey(correlationKey).variables(payload).timeToLive(Duration.ofSeconds(30L)).messageId(UUID.randomUUID().toString()).send().join();
      }

   }

   private void sendMessages(String messageName, String payload, int count) {
      this.sendMessages(messageName, payload, count, String.valueOf(this.random.nextInt(7)));
   }

   protected JobWorker progressOrderProcessCheckPayment() {
      return this.client.newWorker().jobType("checkPayment").handler((jobClient, job) -> {
         int scenario = this.random.nextInt(6);
         switch (scenario) {
            case 0:
               throw new RuntimeException("Payment system not available.");
            case 1:
               jobClient.newCompleteCommand(job.getKey()).variables("{\"paid\":false}").send().join();
               break;
            case 2:
            case 3:
            case 4:
               jobClient.newCompleteCommand(job.getKey()).variables("{\"paid\":true}").send().join();
               break;
            case 5:
               jobClient.newCompleteCommand(job.getKey()).send().join();
         }

      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressSimpleTask(String taskType) {
      return this.client.newWorker().jobType(taskType).handler((jobClient, job) -> {
         int scenarioCount = this.random.nextInt(3);
         switch (scenarioCount) {
            case 0:
            default:
               break;
            case 1:
               jobClient.newCompleteCommand(job.getKey()).send().join();
               break;
            case 2:
               jobClient.newFailCommand(job.getKey()).retries(0).send().join();
         }

      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressTaskA() {
      return this.client.newWorker().jobType("taskA").handler((jobClient, job) -> {
         int scenarioCount = this.random.nextInt(2);
         switch (scenarioCount) {
            case 0:
               jobClient.newCompleteCommand(job.getKey()).send().join();
            case 1:
            default:
         }
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressBigProcessTaskA() {
      return this.client.newWorker().jobType("bigProcessTaskA").handler((jobClient, job) -> {
         Map varMap = job.getVariablesAsMap();
         Integer i = (Integer)varMap.get("i");
         varMap.put("i", i == null ? 1 : i + 1);
         jobClient.newCompleteCommand(job.getKey()).variables(varMap).send().join();
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressBigProcessTaskB() {
      int[] countBeforeIncident = new int[]{0};
      return this.client.newWorker().jobType("bigProcessTaskB").handler((jobClient, job) -> {
         if (countBeforeIncident[0] <= 45) {
            jobClient.newCompleteCommand(job.getKey()).send().join();
            int var10002 = countBeforeIncident[0]++;
         } else {
            if (this.random.nextBoolean()) {
               jobClient.newFailCommand(job.getKey()).retries(0).send().join();
            } else {
               jobClient.newCompleteCommand(job.getKey()).send().join();
            }

            countBeforeIncident[0] = 0;
         }

      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressErrorTask() {
      return this.client.newWorker().jobType("errorTask").handler((jobClient, job) -> {
         String errorCode = (String)job.getVariablesAsMap().getOrDefault("errorCode", "error");
         jobClient.newThrowErrorCommand(job.getKey()).errorCode(errorCode).errorMessage("Job worker throw error with error code: " + errorCode).send().join();
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   protected void deployVersion1() {
      super.deployVersion1();
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/complexProcess_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/eventBasedGatewayProcess_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/subProcess.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/interruptingBoundaryEvent_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/nonInterruptingBoundaryEvent_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/timerProcess_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/callActivityProcess.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/eventSubProcess_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/bigProcess.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/errorProcess.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/error-end-event.bpmn"});
   }

   protected void startProcessInstances(int version) {
      super.startProcessInstances(version);
      if (version == 1) {
         this.createBigProcess(40, 250);
      }

      int instancesCount = this.random.nextInt(30) + 30;

      for(int i = 0; i < instancesCount; ++i) {
         if (version == 1) {
            this.sendMessages("newClientMessage", "{\"clientId\": \"" + this.random.nextInt(10) + "\"\n}", 1);
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "call-activity-process", "{\"var\": " + this.random.nextInt(10) + "}"));
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "eventSubprocessProcess", "{\"clientId\": \"" + this.random.nextInt(10) + "\"}"));
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "errorProcess", "{\"errorCode\": \"boundary\"}"));
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "errorProcess", "{\"errorCode\": \"subProcess\"}"));
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "errorProcess", "{\"errorCode\": \"unknown\"}"));
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "error-end-process", (String)null));
         }

         if (version == 2) {
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "interruptingBoundaryEvent", (String)null));
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "nonInterruptingBoundaryEvent", (String)null));
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "call-activity-process", "{\"var\": " + this.random.nextInt(10) + "}"));
         }

         if (version < 2) {
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "prWithSubprocess", (String)null));
         }

         if (version < 3) {
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "complexProcess", "{\"clientId\": \"" + this.random.nextInt(10) + "\"}"));
         }

         if (version == 3) {
            this.processInstanceKeys.add(ZeebeTestUtil.startProcessInstance(this.client, "complexProcess", "{\"goUp\": " + this.random.nextInt(5) + "}"));
            List var10000 = this.processInstanceKeys;
            ZeebeClient var10001 = this.client;
            int var10003 = this.random.nextInt(10);
            var10000.add(ZeebeTestUtil.startProcessInstance(var10001, "call-activity-process", "{\"orders\": [" + var10003 + ", " + this.random.nextInt(10) + "]}"));
         }
      }

   }

   private void createBigProcess(int loopCardinality, int numberOfClients) {
      XContentBuilder builder = null;

      try {
         builder = XContentFactory.jsonBuilder().startObject().field("loopCardinality", loopCardinality).field("clients").startArray();

         for(int j = 0; j <= numberOfClients; ++j) {
            builder.value(j);
         }

         builder.endArray().endObject();
         ZeebeTestUtil.startProcessInstance(this.client, "bigProcess", Strings.toString(builder));
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      }
   }

   protected void deployVersion2() {
      super.deployVersion2();
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/complexProcess_v_2.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/eventBasedGatewayProcess_v_2.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/interruptingBoundaryEvent_v_2.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/nonInterruptingBoundaryEvent_v_2.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/calledProcess.bpmn"});
   }

   protected void deployVersion3() {
      super.deployVersion3();
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/complexProcess_v_3.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"develop/calledProcess_v_2.bpmn"});
   }

   public void setClient(ZeebeClient client) {
      this.client = client;
   }
}
