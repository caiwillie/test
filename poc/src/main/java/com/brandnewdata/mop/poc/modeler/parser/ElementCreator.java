package com.brandnewdata.mop.poc.modeler.parser;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.modeler.parser.constants.AttributeConstants;
import com.brandnewdata.mop.poc.modeler.parser.constants.QNameConstants;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ElementCreator {

    private static String FLOW_TEMPLATE = "Flow_{}";

    private static String ACTIVITY_TEMPLATE = "Activity_{}";

    public static Element createBpmnSequenceFlow() {
        Element e = DocumentHelper.createElement(QNameConstants.BPMN_SEQUENCE_FLOW_QNAME);
        e.addAttribute(AttributeConstants.ID_ATTRIBUTE, generateId(FLOW_TEMPLATE));
        return e;
    }


    public static Element createCallActivity() {
        Element e = DocumentHelper.createElement(QNameConstants.BPMN_CALL_ACTIVITY_QNAME);
        e.addAttribute(AttributeConstants.ID_ATTRIBUTE, generateId(ACTIVITY_TEMPLATE));
        return e;
    }

    private static String randomString() {
        return RandomUtil.randomString(16);
    }

    private static String generateId(String template) {
        return StrUtil.format(template, randomString());
    }

}
