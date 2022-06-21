package io.camunda.operate.util;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientException;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.command.DeployProcessCommandStep1;
import io.camunda.zeebe.client.api.command.FailJobCommandStep1;
import io.camunda.zeebe.client.api.response.*;
import io.camunda.zeebe.client.api.response.Process;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZeebeTestUtil {
   private static final Logger logger = LoggerFactory.getLogger(ZeebeTestUtil.class);
   public static final Logger ALL_EVENTS_LOGGER = LoggerFactory.getLogger("io.camunda.operate.ALL_EVENTS");

   public static Long deployProcess(ZeebeClient client, String... classpathResources) {
      if (classpathResources.length == 0) {
         return null;
      } else {
         DeployProcessCommandStep1 deployProcessCommandStep1 = client.newDeployCommand();
         String[] var3 = classpathResources;
         int var4 = classpathResources.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String classpathResource = var3[var5];
            deployProcessCommandStep1 = ((DeployProcessCommandStep1)deployProcessCommandStep1).addResourceFromClasspath(classpathResource);
         }

         DeploymentEvent deploymentEvent = (DeploymentEvent)((DeployProcessCommandStep1.DeployProcessCommandBuilderStep2)deployProcessCommandStep1).send().join();
         logger.debug("Deployment of resource [{}] was performed", (Object[])classpathResources);
         return ((Process)deploymentEvent.getProcesses().get(classpathResources.length - 1)).getProcessDefinitionKey();
      }
   }

   public static void deployDecision(ZeebeClient client, String... classpathResources) {
      if (classpathResources.length != 0) {
         DeployProcessCommandStep1 deployProcessCommandStep1 = client.newDeployCommand();
         String[] var3 = classpathResources;
         int var4 = classpathResources.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String classpathResource = var3[var5];
            deployProcessCommandStep1 = ((DeployProcessCommandStep1)deployProcessCommandStep1).addResourceFromClasspath(classpathResource);
         }

         ((DeployProcessCommandStep1.DeployProcessCommandBuilderStep2)deployProcessCommandStep1).send().join();
         logger.debug("Deployment of resource [{}] was performed", (Object[])classpathResources);
      }
   }

   public static Long deployProcess(ZeebeClient client, BpmnModelInstance processModel, String resourceName) {
      DeployProcessCommandStep1 deployProcessCommandStep1 = client.newDeployCommand().addProcessModel(processModel, resourceName);
      DeploymentEvent deploymentEvent = (DeploymentEvent)((DeployProcessCommandStep1.DeployProcessCommandBuilderStep2)deployProcessCommandStep1).send().join();
      logger.debug("Deployment of resource [{}] was performed", resourceName);
      return ((Process)deploymentEvent.getProcesses().get(0)).getProcessDefinitionKey();
   }

   public static long startProcessInstance(ZeebeClient client, String bpmnProcessId, String payload) {
      CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3 createProcessInstanceCommandStep3 = client.newCreateInstanceCommand().bpmnProcessId(bpmnProcessId).latestVersion();
      if (payload != null) {
         createProcessInstanceCommandStep3.variables(payload);
      }

      ProcessInstanceEvent processInstanceEvent = null;

      try {
         processInstanceEvent = (ProcessInstanceEvent)createProcessInstanceCommandStep3.send().join();
         logger.debug("Process instance created for process [{}]", bpmnProcessId);
      } catch (ClientException var6) {
         ThreadUtil.sleepFor(300L);
         processInstanceEvent = (ProcessInstanceEvent)createProcessInstanceCommandStep3.send().join();
         logger.debug("Process instance created for process [{}]", bpmnProcessId);
      }

      return processInstanceEvent.getProcessInstanceKey();
   }

   public static void cancelProcessInstance(ZeebeClient client, long processInstanceKey) {
      client.newCancelInstanceCommand(processInstanceKey).send().join();
   }

   public static void completeTask(ZeebeClient client, String jobType, String workerName, String payload) {
      completeTask(client, jobType, workerName, payload, 1);
   }

   public static void completeTask(ZeebeClient client, String jobType, String workerName, String payload, int count) {
      handleTasks(client, jobType, workerName, count, (jobClient, job) -> {
         CompleteJobCommandStep1 command = jobClient.newCompleteCommand(job.getKey());
         if (payload != null) {
            command.variables(payload);
         }

         command.send().join();
      });
   }

   public static Long failTask(ZeebeClient client, String jobType, String workerName, int numberOfFailures, String errorMessage) {
      return (Long)handleTasks(client, jobType, workerName, numberOfFailures, (jobClient, job) -> {
         FailJobCommandStep1.FailJobCommandStep2 failCommand = jobClient.newFailCommand(job.getKey()).retries(job.getRetries() - 1);
         if (errorMessage != null) {
            failCommand.errorMessage(errorMessage);
         }

         failCommand.send().join();
      }).get(0);
   }

   public static Long throwErrorInTask(ZeebeClient client, String jobType, String workerName, int numberOfFailures, String errorCode, String errorMessage) {
      return (Long)handleTasks(client, jobType, workerName, numberOfFailures, (jobClient, job) -> {
         jobClient.newThrowErrorCommand(job.getKey()).errorCode(errorCode).errorMessage(errorMessage).send().join();
      }).get(0);
   }

   private static List handleTasks(ZeebeClient client, String jobType, String workerName, int jobCount, BiConsumer<ZeebeClient, ActivatedJob> jobHandler) {
      List jobKeys = new ArrayList();

      while(jobKeys.size() < jobCount) {
         ((ActivateJobsResponse)client.newActivateJobsCommand().jobType(jobType).maxJobsToActivate(jobCount - jobKeys.size()).workerName(workerName).timeout(Duration.ofSeconds(2L)).send().join()).getJobs().forEach((job) -> {
            jobHandler.accept(client, job);
            jobKeys.add(job.getKey());
         });
      }

      return jobKeys;
   }

   public static void resolveIncident(ZeebeClient client, Long jobKey, Long incidentKey) {
      client.newUpdateRetriesCommand(jobKey).retries(3).send().join();
      client.newResolveIncidentCommand(incidentKey).send().join();
   }

   public static void updateVariables(ZeebeClient client, Long scopeKey, String newPayload) {
      client.newSetVariablesCommand(scopeKey).variables(newPayload).local(true).send().join();
   }
}
