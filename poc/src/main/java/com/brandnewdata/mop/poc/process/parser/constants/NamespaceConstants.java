package com.brandnewdata.mop.poc.process.parser.constants;

import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;

public interface NamespaceConstants {
    Namespace BPMN2_NAMESPACE = DocumentHelper.createNamespace("bpmn2",
            "http://www.omg.org/spec/BPMN/20100524/MODEL");

    Namespace XSI_NAMESPACE = DocumentHelper.createNamespace("xsi",
            "http://www.w3.org/2001/XMLSchema-instance");

    Namespace BPMN_NAMESPACE = DocumentHelper.createNamespace("bpmn",
            "http://www.omg.org/spec/BPMN/20100524/MODEL");

    Namespace BPMNDI_NAMESPACE = DocumentHelper.createNamespace("bpmndi",
            "http://www.omg.org/spec/BPMN/20100524/DI");

    Namespace DI_NAMESPACE = DocumentHelper.createNamespace("di",
            "http://www.omg.org/spec/DD/20100524/DI");

    Namespace DC_NAMESPACE = DocumentHelper.createNamespace("dc",
            "http://www.omg.org/spec/DD/20100524/DC");

    Namespace BRANDNEWDATA_NAMESPACE = DocumentHelper.createNamespace("brandnewdata",
            "https://www.brandnewdata.com");

    Namespace ZEEBE_NAMESPACE = DocumentHelper.createNamespace("zeebe",
            "http://camunda.org/schema/zeebe/1.0");
}
