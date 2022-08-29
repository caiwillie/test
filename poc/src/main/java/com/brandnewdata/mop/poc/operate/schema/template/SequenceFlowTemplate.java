package com.brandnewdata.mop.poc.operate.schema.template;

import org.springframework.stereotype.Component;

@Component
public class SequenceFlowTemplate extends AbstractTemplateDescriptor {
    public static final String INDEX_NAME = "sequence-flow";
    public static final String ID = "id";
    public static final String KEY = "key";
    public static final String PROCESS_INSTANCE_KEY = "processInstanceKey";
    public static final String ACTIVITY_ID = "activityId";

    @Override
    public String getIndexName() {
        return "sequence-flow";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
