package com.brandnewdata.mop.poc.parser;

import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;

public interface XMLConstants {

    static final Namespace BPMN_NAMESPACE =
            DocumentHelper.createNamespace(BPMNNamespace.BPMN.getPrefix(), BPMNNamespace.BPMN.getUri());

    static final Namespace BRANDNEWDATA_NAMESPACE =
            DocumentHelper.createNamespace(BPMNNamespace.BRANDNEWDATA.getPrefix(), BPMNNamespace.BRANDNEWDATA.getUri());

    static final Namespace ZEEBE_NAMESPACE =
            DocumentHelper.createNamespace(BPMNNamespace.ZEEBE.getPrefix(), BPMNNamespace.ZEEBE.getUri());

    static QName BPMN_PROCESS_QNAME = DocumentHelper.createQName("process", BPMN_NAMESPACE);

    static QName BPMN_TASK_QNAME = DocumentHelper.createQName("task", BPMN_NAMESPACE);

    static QName BPMN_SERVICE_TASK_QNAME = DocumentHelper.createQName("serviceTask", BPMN_NAMESPACE);

    static QName BPMN_CALL_ACTIVITY_TASK_QNAME = DocumentHelper.createQName("callActivity", BPMN_NAMESPACE);

    static QName BPMN_EXTENSION_ELEMENTS_QNAME = DocumentHelper.createQName("extensionElements", BPMN_NAMESPACE);

    static QName BRANDNEWDATA_TASK_DEFINITION_QNAME = DocumentHelper.createQName("taskDefinition", BRANDNEWDATA_NAMESPACE);

    static QName BRANDNEWDATA_INPUT_QNAME = DocumentHelper.createQName("input", BRANDNEWDATA_NAMESPACE);

    static QName BRANDNEWDATA_OUTPUT_QNAME = DocumentHelper.createQName("output", BRANDNEWDATA_NAMESPACE);

    static QName BRANDNEWDATA_EXTENSION_QNAME = DocumentHelper.createQName("extension", BRANDNEWDATA_NAMESPACE);

    static QName ZEEBE_TASK_DEFINITION_QNAME = DocumentHelper.createQName("taskDefinition", ZEEBE_NAMESPACE);

    static QName ZEEBE_CALLED_ELEMENT_QNAME = DocumentHelper.createQName("calledElement", ZEEBE_NAMESPACE);

    static QName ZEEBE_IO_MAPPING_QNAME = DocumentHelper.createQName("ioMapping", ZEEBE_NAMESPACE);

    static QName ZEEBE_INPUT_QNAME = DocumentHelper.createQName("input", ZEEBE_NAMESPACE);

    static QName ZEEBE_OUTPUT_QNAME = DocumentHelper.createQName("output", ZEEBE_NAMESPACE);


}
