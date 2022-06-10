package com.brandnewdata.mop.example;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeVariablesAsType;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.stereotype.Service;

/**
 * @author caiwillie
 */
@Service
public class WorkerService {

    @ZeebeWorker(type = ProcessConstants.TEST_WORKER, autoComplete = true)
    public void doWork(final JobClient client, final ActivatedJob job, @ZeebeVariablesAsType POJOBean variables) {
        // do whatever you need to do
        // but no need to call client.newCompleteCommand()...
    }

}
