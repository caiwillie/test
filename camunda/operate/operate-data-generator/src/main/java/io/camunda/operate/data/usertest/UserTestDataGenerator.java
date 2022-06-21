package io.camunda.operate.data.usertest;

import io.camunda.operate.data.AbstractDataGenerator;
import io.camunda.operate.data.util.DecisionDataUtil;
import io.camunda.operate.data.util.NameGenerator;
import io.camunda.operate.util.PayloadUtil;
import io.camunda.operate.util.ThreadUtil;
import io.camunda.operate.util.ZeebeTestUtil;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientException;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.command.FailJobCommandStep1;
import io.camunda.zeebe.client.api.command.FinalCommandStep;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("dataGenerator")
@Profile({"usertest-data"})
public class UserTestDataGenerator extends AbstractDataGenerator {
   private static final Logger logger = LoggerFactory.getLogger(UserTestDataGenerator.class);
   public static final int JOB_WORKER_TIMEOUT = 5;
   protected Random random = new Random();
   protected List processInstanceKeys = new ArrayList();
   protected List doNotTouchProcessInstanceKeys = new ArrayList();
   protected List jobWorkers = new ArrayList();
   @Autowired
   protected PayloadUtil payloadUtil;
   @Autowired
   @Qualifier("esClient")
   private RestHighLevelClient esClient;
   @Autowired
   private DecisionDataUtil testUtil;

   public boolean createZeebeData(boolean manuallyCalled) {
      if (!super.createZeebeData(manuallyCalled)) {
         return false;
      } else {
         logger.debug("Test data will be generated");
         this.createProcessWithoutInstances();
         this.createProcessWithInstancesThatHasOnlyIncidents(5 + this.random.nextInt(17), 5 + this.random.nextInt(17));
         this.createProcessWithInstancesWithoutIncidents(5 + this.random.nextInt(23), 5 + this.random.nextInt(23));
         this.createAndStartProcessWithLargeVariableValue();
         this.createAndStartProcessWithLotOfVariables();
         this.deployVersion1();
         this.createSpecialDataV1();
         this.startProcessInstances(1);
         this.deployVersion2();
         this.createSpecialDataV2();
         this.startProcessInstances(2);
         this.deployVersion3();
         this.startProcessInstances(3);
         this.createOperations();
         this.progressProcessInstances();
         return true;
      }
   }

   private void createAndStartProcessWithLargeVariableValue() {
      logger.debug("Deploy and start process with large variable value >32kb");
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/single-task.bpmn"});
      String jsonString = this.payloadUtil.readStringFromClasspath("/usertest/large-payload.json");
      ZeebeTestUtil.startProcessInstance(this.client, "bigVarProcess", jsonString);
   }

   private void createAndStartProcessWithLotOfVariables() {
      StringBuffer vars = new StringBuffer("{");

      for(char letter1 = 'a'; letter1 <= 'z'; ++letter1) {
         for(char letter2 = 'a'; letter2 <= 'z'; ++letter2) {
            if (vars.length() > 1) {
               vars.append(",\n");
            }

            String var10000 = Character.toString(letter1);
            String str = var10000 + Character.toString(letter2);
            vars.append("\"").append(str).append("\": \"value_").append(str).append("\"");
         }
      }

      vars.append("}");
      ZeebeTestUtil.startProcessInstance(this.client, "bigVarProcess", vars.toString());
   }

   public void createSpecialDataV1() {
      this.doNotTouchProcessInstanceKeys.add(this.startLoanProcess());
      long instanceKey2 = this.startLoanProcess();
      this.completeTask(instanceKey2, "reviewLoanRequest", (String)null);
      this.failTask(instanceKey2, "checkSchufa", "Schufa system is not accessible");
      this.doNotTouchProcessInstanceKeys.add(instanceKey2);
      long instanceKey3 = this.startLoanProcess();
      this.completeTask(instanceKey3, "reviewLoanRequest", (String)null);
      this.completeTask(instanceKey3, "checkSchufa", (String)null);
      ZeebeTestUtil.cancelProcessInstance(this.client, instanceKey3);
      this.doNotTouchProcessInstanceKeys.add(instanceKey3);
      long instanceKey4 = this.startLoanProcess();
      this.completeTask(instanceKey4, "reviewLoanRequest", (String)null);
      this.completeTask(instanceKey4, "checkSchufa", (String)null);
      this.completeTask(instanceKey4, "sendTheLoanDecision", (String)null);
      this.doNotTouchProcessInstanceKeys.add(instanceKey4);
      this.doNotTouchProcessInstanceKeys.add(this.startOrderProcess());
      long instanceKey5 = this.startOrderProcess();
      this.completeTask(instanceKey5, "checkPayment", "{\"paid\":true,\"orderStatus\": \"PAID\"}");
      this.failTask(instanceKey5, "shipArticles", "Cannot connect to server delivery05");
      this.doNotTouchProcessInstanceKeys.add(instanceKey5);
      long instanceKey6 = this.startOrderProcess();
      this.completeTask(instanceKey6, "checkPayment", "{\"paid\":false}");
      ZeebeTestUtil.cancelProcessInstance(this.client, instanceKey6);
      this.doNotTouchProcessInstanceKeys.add(instanceKey6);
      long instanceKey7 = this.startOrderProcess();
      this.completeTask(instanceKey7, "checkPayment", "{\"paid\":true,\"orderStatus\": \"PAID\"}");
      this.completeTask(instanceKey7, "shipArticles", "{\"orderStatus\":\"SHIPPED\"}");
      this.doNotTouchProcessInstanceKeys.add(instanceKey7);
      this.doNotTouchProcessInstanceKeys.add(this.startFlightRegistrationProcess());
      long instanceKey8 = this.startFlightRegistrationProcess();
      this.completeTask(instanceKey8, "registerPassenger", (String)null);
      this.doNotTouchProcessInstanceKeys.add(instanceKey8);
      long instanceKey9 = this.startFlightRegistrationProcess();
      this.completeTask(instanceKey9, "registerPassenger", (String)null);
      this.failTask(instanceKey9, "registerCabinBag", "No more stickers available");
      this.doNotTouchProcessInstanceKeys.add(instanceKey9);
      long instanceKey10 = this.startFlightRegistrationProcess();
      this.completeTask(instanceKey10, "registerPassenger", (String)null);
      this.completeTask(instanceKey10, "registerCabinBag", "{\"luggage\":true}");
      ZeebeTestUtil.cancelProcessInstance(this.client, instanceKey10);
      this.doNotTouchProcessInstanceKeys.add(instanceKey10);
      long instanceKey11 = this.startFlightRegistrationProcess();
      this.completeTask(instanceKey11, "registerPassenger", (String)null);
      this.completeTask(instanceKey11, "registerCabinBag", "{\"luggage\":false}");
      this.completeTask(instanceKey11, "printOutBoardingPass", (String)null);
      this.doNotTouchProcessInstanceKeys.add(instanceKey11);
   }

   public void createSpecialDataV2() {
      long instanceKey4 = this.startOrderProcess();
      this.completeTask(instanceKey4, "checkPayment", "{\"paid\":true,\"orderStatus\": \"PAID\"}");
      this.completeTask(instanceKey4, "checkItems", "{\"smthIsMissing\":false,\"orderStatus\":\"AWAITING_SHIPMENT\"}");
      this.doNotTouchProcessInstanceKeys.add(instanceKey4);
      long instanceKey5 = this.startOrderProcess();
      this.completeTask(instanceKey5, "checkPayment", "{\"paid\":true,\"orderStatus\": \"PAID\"}");
      this.failTask(instanceKey5, "checkItems", "Order information is not complete");
      this.doNotTouchProcessInstanceKeys.add(instanceKey5);
      long instanceKey3 = this.startOrderProcess();
      this.completeTask(instanceKey3, "checkPayment", "{\"paid\":true,\"orderStatus\": \"PAID\"}");
      this.completeTask(instanceKey3, "checkItems", "{\"smthIsMissing\":false,\"orderStatus\":\"AWAITING_SHIPMENT\"}");
      this.failTask(instanceKey3, "shipArticles", "Cannot connect to server delivery05");
      this.doNotTouchProcessInstanceKeys.add(instanceKey3);
      long instanceKey2 = this.startOrderProcess();
      this.completeTask(instanceKey2, "checkPayment", "{\"paid\":true,\"orderStatus\": \"PAID\"}");
      this.completeTask(instanceKey2, "checkItems", "{\"smthIsMissing\":false,\"orderStatus\":\"AWAITING_SHIPMENT\"}");
      this.failTask(instanceKey2, "shipArticles", "Order information is not complete");
      this.doNotTouchProcessInstanceKeys.add(instanceKey2);
      long instanceKey1 = this.startOrderProcess();
      this.completeTask(instanceKey1, "checkPayment", "{\"paid\":true,\"orderStatus\": \"PAID\"}");
      this.completeTask(instanceKey1, "checkItems", "{\"smthIsMissing\":false,\"orderStatus\":\"AWAITING_SHIPMENT\"}");
      this.failTask(instanceKey1, "shipArticles", "Cannot connect to server delivery05");
      this.doNotTouchProcessInstanceKeys.add(instanceKey1);
      long instanceKey7 = this.startOrderProcess();
      this.completeTask(instanceKey7, "checkPayment", "{\"paid\":true,\"orderStatus\": \"PAID\"}");
      this.completeTask(instanceKey7, "checkItems", "{\"smthIsMissing\":false,\"orderStatus\":\"AWAITING_SHIPMENT\"}");
      this.completeTask(instanceKey7, "shipArticles", "{\"orderStatus\":\"SHIPPED\"}");
      this.doNotTouchProcessInstanceKeys.add(instanceKey7);
      long instanceKey6 = this.startOrderProcess();
      this.completeTask(instanceKey6, "checkPayment", "{\"paid\":false}");
      ZeebeTestUtil.cancelProcessInstance(this.client, instanceKey6);
      this.doNotTouchProcessInstanceKeys.add(instanceKey6);
      this.doNotTouchProcessInstanceKeys.add(this.startFlightRegistrationProcess());
      long instanceKey8 = this.startFlightRegistrationProcess();
      this.completeTask(instanceKey8, "registerPassenger", (String)null);
      this.doNotTouchProcessInstanceKeys.add(instanceKey8);
      long instanceKey9 = this.startFlightRegistrationProcess();
      this.completeTask(instanceKey9, "registerPassenger", (String)null);
      this.failTask(instanceKey9, "registerCabinBag", "Cannot connect to server fly-host");
      this.doNotTouchProcessInstanceKeys.add(instanceKey9);
      long instanceKey10 = this.startFlightRegistrationProcess();
      this.completeTask(instanceKey10, "registerPassenger", (String)null);
      this.completeTask(instanceKey10, "registerCabinBag", "{\"luggage\":true}");
      ZeebeTestUtil.cancelProcessInstance(this.client, instanceKey10);
      this.doNotTouchProcessInstanceKeys.add(instanceKey10);
      long instanceKey11 = this.startFlightRegistrationProcess();
      this.completeTask(instanceKey11, "registerPassenger", (String)null);
      this.completeTask(instanceKey11, "registerCabinBag", "{\"luggage\":true}");
      this.completeTask(instanceKey11, "determineLuggageWeight", "{\"luggageWeight\":21}");
      this.completeTask(instanceKey11, "registerLuggage", (String)null);
      this.completeTask(instanceKey11, "printOutBoardingPass", (String)null);
      this.doNotTouchProcessInstanceKeys.add(instanceKey11);
   }

   public void completeTask(long processInstanceKey, String jobType, String payload) {
      CompleteJobHandler completeJobHandler = new CompleteJobHandler(payload, processInstanceKey);
      JobWorker jobWorker = this.client.newWorker().jobType(jobType).handler(completeJobHandler).name("operate").timeout(Duration.ofSeconds(3L)).pollInterval(Duration.ofMillis(100L)).open();

      int attempts;
      for(attempts = 0; !completeJobHandler.isTaskCompleted() && attempts < 10; ++attempts) {
         ThreadUtil.sleepFor(200L);
      }

      if (attempts == 10) {
         logger.debug("Could not complete the task {} for process instance id {}", jobType, processInstanceKey);
      }

      jobWorker.close();
   }

   public void failTask(long processInstanceKey, String jobType, String errorMessage) {
      FailJobHandler failJobHandler = new FailJobHandler(processInstanceKey, errorMessage);
      JobWorker jobWorker = this.client.newWorker().jobType(jobType).handler(failJobHandler).name("operate").timeout(Duration.ofSeconds(3L)).pollInterval(Duration.ofMillis(100L)).open();

      int attempts;
      for(attempts = 0; !failJobHandler.isTaskFailed() && attempts < 10; ++attempts) {
         ThreadUtil.sleepFor(200L);
      }

      if (attempts == 10) {
         logger.debug("Could not fail the task {} for process instance id {}", jobType, processInstanceKey);
      }

      jobWorker.close();
   }

   protected void progressProcessInstances() {
      this.jobWorkers.add(this.progressReviewLoanRequestTask());
      this.jobWorkers.add(this.progressCheckSchufaTask());
      this.jobWorkers.add(this.progressSimpleTask("sendTheLoanDecision"));
      this.jobWorkers.add(this.progressSimpleTask("requestPayment"));
      this.jobWorkers.add(this.progressOrderProcessCheckPayment());
      this.jobWorkers.add(this.progressOrderProcessShipArticles());
      this.jobWorkers.add(this.progressOrderProcessCheckItems());
      this.jobWorkers.add(this.progressSimpleTask("requestWarehouse"));
      this.jobWorkers.add(this.progressSimpleTask("registerPassenger"));
      this.jobWorkers.add(this.progressFlightRegistrationRegisterCabinBag());
      this.jobWorkers.add(this.progressSimpleTask("registerLuggage"));
      this.jobWorkers.add(this.progressSimpleTask("printOutBoardingPass"));
      this.jobWorkers.add(this.progressSimpleTask("registerLuggage"));
      this.jobWorkers.add(this.progressFlightRegistrationDetermineWeight());
      this.jobWorkers.add(this.progressSimpleTask("processPayment"));
      this.jobWorkers.add(this.progressAlwaysFailingTask());
      this.jobWorkers.add(this.progressSimpleTask("peterTask"));
      this.jobWorkers.add(this.progressSimpleTask("checkItems"));
      this.jobWorkers.addAll(this.progressMultiInstanceTasks());
      this.scheduler.schedule(() -> {
         this.startProcessInstances(3);
      }, 1L, TimeUnit.MINUTES);
      this.scheduler.schedule(() -> {
         Iterator var1 = this.jobWorkers.iterator();

         while(var1.hasNext()) {
            JobWorker jobWorker = (JobWorker)var1.next();
            jobWorker.close();
         }

      }, 500L, TimeUnit.SECONDS);
      this.scheduler.schedule(() -> {
         this.cancelSomeInstances();
      }, 510L, TimeUnit.SECONDS);
   }

   private JobWorker progressAlwaysFailingTask() {
      return this.client.newWorker().jobType("alwaysFailingTask").handler((jobClient, job) -> {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         (new Throwable()).printStackTrace(pw);
         String errorMessage = "Something went wrong. \n" + sw.toString();
         jobClient.newFailCommand(job.getKey()).retries(0).errorMessage(errorMessage).send().join();
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private List progressMultiInstanceTasks() {
      Random random = new Random();
      JobHandler handler = (c, j) -> {
         if (random.nextBoolean()) {
            c.newCompleteCommand(j.getKey()).send().join();
         } else {
            c.newFailCommand(j.getKey()).retries(0).send().join();
         }

      };
      List workers = new ArrayList();
      workers.add(this.client.newWorker().jobType("filter").handler(handler).open());
      workers.add(this.client.newWorker().jobType("map").handler(handler).open());
      workers.add(this.client.newWorker().jobType("reduce").handler(handler).open());
      return workers;
   }

   private void cancelSomeInstances() {
      for(Iterator iterator = this.processInstanceKeys.iterator(); iterator.hasNext(); iterator.remove()) {
         long processInstanceKey = (Long)iterator.next();
         if (this.random.nextInt(15) == 1) {
            try {
               this.client.newCancelInstanceCommand(processInstanceKey).send().join();
            } catch (ClientException var5) {
               logger.error("Error occurred when cancelling process instance:", var5);
            }
         }
      }

   }

   protected void createOperations() {
   }

   protected JobWorker progressOrderProcessCheckPayment() {
      return this.client.newWorker().jobType("checkPayment").handler((jobClient, job) -> {
         if (this.canProgress(job.getProcessInstanceKey())) {
            int scenario = this.random.nextInt(5);
            switch (scenario) {
               case 0:
                  throw new RuntimeException("Payment system not available.");
               case 1:
                  jobClient.newCompleteCommand(job.getKey()).variables("{\"paid\":false}").send().join();
                  break;
               case 2:
               case 3:
               case 4:
                  jobClient.newCompleteCommand(job.getKey()).variables("{\"paid\":true,\"orderStatus\": \"PAID\"}").send().join();
            }

         }
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressOrderProcessCheckItems() {
      return this.client.newWorker().jobType("checkItems").handler((jobClient, job) -> {
         if (this.canProgress(job.getProcessInstanceKey())) {
            int scenario = this.random.nextInt(4);
            switch (scenario) {
               case 0:
               case 1:
               case 2:
                  jobClient.newCompleteCommand(job.getKey()).variables("{\"smthIsMissing\":false,\"orderStatus\":\"AWAITING_SHIPMENT\"}").send().join();
                  break;
               case 3:
                  jobClient.newCompleteCommand(job.getKey()).variables("{\"smthIsMissing\":true}").send().join();
            }

         }
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressOrderProcessShipArticles() {
      return this.client.newWorker().jobType("shipArticles").handler((jobClient, job) -> {
         if (this.canProgress(job.getProcessInstanceKey())) {
            int scenario = this.random.nextInt(2);
            switch (scenario) {
               case 0:
                  jobClient.newCompleteCommand(job.getKey()).variables("{\"orderStatus\":\"SHIPPED\"}").send().join();
                  break;
               case 1:
                  jobClient.newFailCommand(job.getKey()).retries(0).errorMessage("Cannot connect to server delivery05").send().join();
            }

         }
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressFlightRegistrationRegisterCabinBag() {
      return this.client.newWorker().jobType("registerCabinBag").handler((jobClient, job) -> {
         if (this.canProgress(job.getProcessInstanceKey())) {
            int scenario = this.random.nextInt(4);
            switch (scenario) {
               case 0:
               case 1:
               case 2:
                  jobClient.newCompleteCommand(job.getKey()).variables("{\"luggage\":false}").send().join();
                  break;
               case 3:
                  jobClient.newCompleteCommand(job.getKey()).variables("{\"luggage\":true}").send().join();
            }

         }
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressFlightRegistrationDetermineWeight() {
      return this.client.newWorker().jobType("determineLuggageWeight").handler((jobClient, job) -> {
         if (this.canProgress(job.getProcessInstanceKey())) {
            CompleteJobCommandStep1 var10000 = jobClient.newCompleteCommand(job.getKey());
            int var10001 = this.random.nextInt(10);
            var10000.variables("{\"luggageWeight\":" + (var10001 + 20) + "}").send().join();
         }
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressSimpleTask(String taskType) {
      return this.client.newWorker().jobType(taskType).handler((jobClient, job) -> {
         if (this.canProgress(job.getProcessInstanceKey())) {
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

         }
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressReviewLoanRequestTask() {
      return this.client.newWorker().jobType("reviewLoanRequest").handler((jobClient, job) -> {
         if (this.canProgress(job.getProcessInstanceKey())) {
            int scenarioCount = this.random.nextInt(3);
            switch (scenarioCount) {
               case 0:
                  jobClient.newCompleteCommand(job.getKey()).variables("{\"loanRequestOK\": " + this.random.nextBoolean() + "}").send().join();
               case 1:
               default:
                  break;
               case 2:
                  jobClient.newFailCommand(job.getKey()).retries(0).errorMessage("Loan request does not contain all the required data").send().join();
            }

         }
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private JobWorker progressCheckSchufaTask() {
      return this.client.newWorker().jobType("checkSchufa").handler((jobClient, job) -> {
         if (this.canProgress(job.getProcessInstanceKey())) {
            int scenarioCount = this.random.nextInt(3);
            switch (scenarioCount) {
               case 0:
                  jobClient.newCompleteCommand(job.getKey()).variables("{\"schufaOK\": " + this.random.nextBoolean() + "}").send().join();
               case 1:
               default:
                  break;
               case 2:
                  jobClient.newFailCommand(job.getKey()).retries(0).errorMessage("Schufa system is not accessible").send().join();
            }

         }
      }).name("operate").timeout(Duration.ofSeconds(5L)).open();
   }

   private boolean canProgress(long key) {
      return !this.doNotTouchProcessInstanceKeys.contains(key);
   }

   protected void createProcessWithoutInstances() {
      Long processDefinitionKeyVersion1 = ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/withoutInstancesProcess_v_1.bpmn"});
      Long processDefinitionKeyVersion2 = ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/withoutInstancesProcess_v_2.bpmn"});
      logger.info("Created process 'withoutInstancesProcess' version 1: {} and version 2: {}", processDefinitionKeyVersion1, processDefinitionKeyVersion2);
   }

   protected void createProcessWithInstancesThatHasOnlyIncidents(int forVersion1, int forVersion2) {
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/onlyIncidentsProcess_v_1.bpmn"});

      int i;
      Long processInstanceKey;
      for(i = 0; i < forVersion1; ++i) {
         processInstanceKey = ZeebeTestUtil.startProcessInstance(this.client, "onlyIncidentsProcess", (String)null);
         this.failTask(processInstanceKey, "alwaysFails", "No memory left.");
      }

      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/onlyIncidentsProcess_v_2.bpmn"});

      for(i = 0; i < forVersion2; ++i) {
         processInstanceKey = ZeebeTestUtil.startProcessInstance(this.client, "onlyIncidentsProcess", (String)null);
         this.failTask(processInstanceKey, "alwaysFails", "No space left on device.");
         this.failTask(processInstanceKey, "alwaysFails2", "No space left on device.");
      }

      logger.info("Created process 'onlyIncidentsProcess' with {} instances for version 1 and {} instances for version 2", forVersion1, forVersion2);
   }

   protected void createProcessWithInstancesWithoutIncidents(int forVersion1, int forVersion2) {
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/withoutIncidentsProcess_v_1.bpmn"});

      int i;
      for(i = 0; i < forVersion1; ++i) {
         ZeebeTestUtil.startProcessInstance(this.client, "withoutIncidentsProcess", (String)null);
      }

      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/withoutIncidentsProcess_v_2.bpmn"});

      for(i = 0; i < forVersion2; ++i) {
         Long processInstanceKey = ZeebeTestUtil.startProcessInstance(this.client, "withoutIncidentsProcess", (String)null);
         this.completeTask(processInstanceKey, "neverFails", (String)null);
      }

      logger.info("Created process 'withoutIncidentsProcess' with {} instances for version 1 and {} instances for version 2", forVersion1, forVersion2);
   }

   protected void deployVersion1() {
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/orderProcess_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/loanProcess_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/registerPassenger_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/multiInstance_v_1.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/manual-task.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/intermediate-message-throw-event.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/intermediate-none-event.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/message-end-event.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/invoice.bpmn"});
   }

   protected void startProcessInstances(int version) {
      int instancesCount = this.random.nextInt(50) + 10;

      for(int i = 0; i < instancesCount; ++i) {
         this.processInstanceKeys.add(this.startDMNInvoice());
         if (version < 2) {
            this.processInstanceKeys.add(this.startLoanProcess());
            this.processInstanceKeys.add(this.startManualProcess());
            this.processInstanceKeys.add(this.startIntermediateMessageThrowEventProcess());
            this.processInstanceKeys.add(this.startIntermediateNoneEventProcess());
            this.processInstanceKeys.add(this.startMessageEndEventProcess());
         }

         if (version < 3) {
            this.processInstanceKeys.add(this.startOrderProcess());
            this.processInstanceKeys.add(this.startFlightRegistrationProcess());
            this.processInstanceKeys.add(this.startMultiInstanceProcess());
         }
      }

   }

   private long startFlightRegistrationProcess() {
      ZeebeClient var10000 = this.client;
      String var10002 = NameGenerator.getRandomFirstName();
      return ZeebeTestUtil.startProcessInstance(var10000, "flightRegistration", "{\n  \"firstName\": \"" + var10002 + "\",\n  \"lastName\": \"" + NameGenerator.getRandomLastName() + "\",\n  \"passNo\": \"PS" + (this.random.nextInt(1000000) + (this.random.nextInt(9) + 1) * 1000000) + "\",\n  \"ticketNo\": \"" + this.random.nextInt(1000) + "\"\n}");
   }

   private long startOrderProcess() {
      float price1 = (float)(Math.round(this.random.nextFloat() * 100000.0F) / 100);
      float price2 = (float)(Math.round(this.random.nextFloat() * 10000.0F) / 100);
      ZeebeClient var10000 = this.client;
      Double var10002 = (double)price1;
      return ZeebeTestUtil.startProcessInstance(var10000, "orderProcess", "{\n  \"clientNo\": \"CNT-1211132-02\",\n  \"orderNo\": \"CMD0001-01\",\n  \"items\": [\n    {\n      \"code\": \"123.135.625\",\n      \"name\": \"Laptop Lenovo ABC-001\",\n      \"quantity\": 1,\n      \"price\": " + var10002 + "\n    },\n    {\n      \"code\": \"111.653.365\",\n      \"name\": \"Headset Sony QWE-23\",\n      \"quantity\": 2,\n      \"price\": " + (double)price2 + "\n    }\n  ],\n  \"mwst\": " + (double)(price1 + price2) * 0.19 + ",\n  \"total\": " + (double)(price1 + price2) + ",\n  \"orderStatus\": \"NEW\"\n}");
   }

   private long startLoanProcess() {
      ZeebeClient var10000 = this.client;
      int var10002 = this.random.nextInt(10000) + 20000;
      return ZeebeTestUtil.startProcessInstance(var10000, "loanProcess", "{\"requestId\": \"RDG123000001\",\n  \"amount\": " + var10002 + ",\n  \"applier\": {\n    \"firstname\": \"Max\",\n    \"lastname\": \"Muster\",\n    \"age\": " + (this.random.nextInt(30) + 18) + "\n  },\n  \"newClient\": false,\n  \"previousRequestIds\": [\"RDG122000001\", \"RDG122000501\", \"RDG122000057\"],\n  \"attachedDocs\": [\n    {\n      \"docType\": \"ID\",\n      \"number\": 123456789\n    },\n    {\n      \"docType\": \"APPLICATION_FORM\",\n      \"number\": 321547\n    }\n  ],\n  \"otherInfo\": null\n}");
   }

   private long startDMNInvoice() {
      String[] invoiceCategories = new String[]{"Misc", "Travel Expenses", "Software License Costs"};
      if (this.random.nextInt(3) > 0) {
         ZeebeClient var10000 = this.client;
         int var10002 = this.random.nextInt(1200);
         return ZeebeTestUtil.startProcessInstance(var10000, "invoice", "{\"amount\": " + var10002 + ",\n  \"invoiceCategory\": \"" + invoiceCategories[this.random.nextInt(3)] + "\"\n}");
      } else {
         return ZeebeTestUtil.startProcessInstance(this.client, "invoice", (String)null);
      }
   }

   private long startManualProcess() {
      return ZeebeTestUtil.startProcessInstance(this.client, "manual-task-process", (String)null);
   }

   private Long startIntermediateNoneEventProcess() {
      return ZeebeTestUtil.startProcessInstance(this.client, "intermediate-none-event-process", (String)null);
   }

   private Long startIntermediateMessageThrowEventProcess() {
      return ZeebeTestUtil.startProcessInstance(this.client, "intermediate-message-throw-event-process", (String)null);
   }

   private Long startMessageEndEventProcess() {
      return ZeebeTestUtil.startProcessInstance(this.client, "message-end-event-process", (String)null);
   }

   private long startMultiInstanceProcess() {
      return ZeebeTestUtil.startProcessInstance(this.client, "multiInstanceProcess", "{\"items\": [1, 2, 3]}");
   }

   protected void deployVersion2() {
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/orderProcess_v_2.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/registerPassenger_v_2.bpmn"});
      ZeebeTestUtil.deployProcess(this.client, new String[]{"usertest/multiInstance_v_2.bpmn"});
      ZeebeTestUtil.deployDecision(this.client, new String[]{"usertest/invoiceBusinessDecisions_v_1.dmn"});
   }

   protected void deployVersion3() {
      ZeebeTestUtil.deployDecision(this.client, new String[]{"usertest/invoiceBusinessDecisions_v_2.dmn"});
   }

   private static class FailJobHandler implements JobHandler {
      private final long processInstanceKey;
      private final String errorMessage;
      private boolean taskFailed = false;

      public FailJobHandler(long processInstanceKey, String errorMessage) {
         this.processInstanceKey = processInstanceKey;
         this.errorMessage = errorMessage;
      }

      public void handle(JobClient jobClient, ActivatedJob job) {
         if (!this.taskFailed && this.processInstanceKey == job.getProcessInstanceKey()) {
            FinalCommandStep failCmd = jobClient.newFailCommand(job.getKey()).retries(0);
            if (this.errorMessage != null) {
               failCmd = ((FailJobCommandStep1.FailJobCommandStep2)failCmd).errorMessage(this.errorMessage);
            }

            failCmd.send().join();
            this.taskFailed = true;
         }

      }

      public boolean isTaskFailed() {
         return this.taskFailed;
      }
   }

   private static class CompleteJobHandler implements JobHandler {
      private final String payload;
      private final long processInstanceKey;
      private boolean taskCompleted = false;

      public CompleteJobHandler(String payload, long processInstanceKey) {
         this.payload = payload;
         this.processInstanceKey = processInstanceKey;
      }

      public void handle(JobClient jobClient, ActivatedJob job) {
         if (!this.taskCompleted && this.processInstanceKey == job.getProcessInstanceKey()) {
            if (this.payload == null) {
               jobClient.newCompleteCommand(job.getKey()).variables(job.getVariables()).send().join();
            } else {
               jobClient.newCompleteCommand(job.getKey()).variables(this.payload).send().join();
            }

            this.taskCompleted = true;
         }

      }

      public boolean isTaskCompleted() {
         return this.taskCompleted;
      }
   }
}
