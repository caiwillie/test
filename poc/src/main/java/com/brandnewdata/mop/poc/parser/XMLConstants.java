package com.brandnewdata.mop.poc.parser;

import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;

public interface XMLConstants {

    Namespace BPMN_NAMESPACE = DocumentHelper.createNamespace(BPMNNamespace.BPMN.getPrefix(), BPMNNamespace.BPMN.getUri());

    Namespace BRANDNEWDATA_NAMESPACE = DocumentHelper.createNamespace(BPMNNamespace.BRANDNEWDATA.getPrefix(), BPMNNamespace.BRANDNEWDATA.getUri());

    Namespace ZEEBE_NAMESPACE = DocumentHelper.createNamespace(BPMNNamespace.ZEEBE.getPrefix(), BPMNNamespace.ZEEBE.getUri());


    QName BPMN_DEFINITIONS_QNAME = DocumentHelper.createQName("definitions", BPMN_NAMESPACE);

    QName BPMN_PROCESS_QNAME = DocumentHelper.createQName("process", BPMN_NAMESPACE);

    QName BPMN_START_EVENT_QNAME = DocumentHelper.createQName("startEvent", BPMN_NAMESPACE);

    QName BPMN_TASK_QNAME = DocumentHelper.createQName("task", BPMN_NAMESPACE);

    QName BPMN_SERVICE_TASK_QNAME = DocumentHelper.createQName("serviceTask", BPMN_NAMESPACE);

    QName BPMN_CALL_ACTIVITY_TASK_QNAME = DocumentHelper.createQName("callActivity", BPMN_NAMESPACE);

    QName BPMN_EXTENSION_ELEMENTS_QNAME = DocumentHelper.createQName("extensionElements", BPMN_NAMESPACE);

    QName BPMN_OUTGOING_QNAME = DocumentHelper.createQName("outgoing", BPMN_NAMESPACE);
    QName BRANDNEWDATA_TASK_DEFINITION_QNAME = DocumentHelper.createQName("taskDefinition", BRANDNEWDATA_NAMESPACE);

    QName BRANDNEWDATA_INPUT_QNAME = DocumentHelper.createQName("input", BRANDNEWDATA_NAMESPACE);
    QName BRANDNEWDATA_INPUT_MAPPING_QNAME = DocumentHelper.createQName("inputMapping", BRANDNEWDATA_NAMESPACE);

    QName BRANDNEWDATA_OUTPUT_MAPPING_QNAME = DocumentHelper.createQName("outputMapping", BRANDNEWDATA_NAMESPACE);
    QName BRANDNEWDATA_OUTPUT_QNAME = DocumentHelper.createQName("output", BRANDNEWDATA_NAMESPACE);

    QName BRANDNEWDATA_EXTENSION_QNAME = DocumentHelper.createQName("extension", BRANDNEWDATA_NAMESPACE);

    QName ZEEBE_TASK_DEFINITION_QNAME = DocumentHelper.createQName("taskDefinition", ZEEBE_NAMESPACE);

    QName ZEEBE_CALLED_ELEMENT_QNAME = DocumentHelper.createQName("calledElement", ZEEBE_NAMESPACE);

    QName ZEEBE_IO_MAPPING_QNAME = DocumentHelper.createQName("ioMapping", ZEEBE_NAMESPACE);

    QName ZEEBE_INPUT_QNAME = DocumentHelper.createQName("input", ZEEBE_NAMESPACE);

    QName ZEEBE_OUTPUT_QNAME = DocumentHelper.createQName("output", ZEEBE_NAMESPACE);


}
