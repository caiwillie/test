package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.process.parser.constants.AttributeConstants;
import com.brandnewdata.mop.poc.process.parser.constants.QNameConstants;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import static com.brandnewdata.mop.poc.parser.XMLConstants.BPMN_INCOMING_QNAME;
import static com.brandnewdata.mop.poc.parser.XMLConstants.BPMN_OUTGOING_QNAME;

public class ElementCreator {

    private static String FLOW_TEMPLATE = "Flow_{}";

    private static String ACTIVITY_TEMPLATE = "Activity_{}";

    private static String EVENT_TEMPLATE = "Event_{}";

    public static Element createBpSequenceFlow() {
        Element e = DocumentHelper.createElement(QNameConstants.BPMN_SEQUENCE_FLOW_QNAME);
        e.addAttribute(AttributeConstants.ID_ATTRIBUTE, generateFlowId());
        return e;
    }

    public static Element createBpCallActivity() {
        Element e = DocumentHelper.createElement(QNameConstants.BPMN_CALL_ACTIVITY_QNAME);
        e.addAttribute(AttributeConstants.ID_ATTRIBUTE, generateActivityId());
        return e;
    }

    public static Element createBpStartEvent() {
        Element e = DocumentHelper.createElement(QNameConstants.BPMN_START_EVENT_QNAME);
        e.addAttribute(AttributeConstants.ID_ATTRIBUTE, generateEventId());
        return e;
    }

    public static Element createIncoming() {
        return DocumentHelper.createElement(BPMN_INCOMING_QNAME);
    }

    public static Element createOutgoing() {
        return DocumentHelper.createElement(BPMN_OUTGOING_QNAME);
    }

    public static String generateFlowId() {
        return generateId(FLOW_TEMPLATE);
    }

    public static String generateActivityId() {
        return generateId(ACTIVITY_TEMPLATE);
    }

    public static String generateEventId() {
        return generateId(EVENT_TEMPLATE);
    }

    private static String randomString() {
        return RandomUtil.randomString(16);
    }

    private static String generateId(String template) {
        return StrUtil.format(template, randomString());
    }


}
