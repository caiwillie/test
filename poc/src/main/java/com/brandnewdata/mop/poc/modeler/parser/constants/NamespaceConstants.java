package com.brandnewdata.mop.poc.modeler.parser.constants;

import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;

public interface NamespaceConstants {

    String BPMN2_NAMESPACE_PRIFIX = "bpmn2";

    String BPMNDI_NAMESPACE_PRIFIX = "bpmndi";

    String DI_NAMESPACE_PRIFIX = "di";

    String DC_NAMESPACE_PRIFIX = "dc";



    Namespace BPMN_NAMESPACE = DocumentHelper.createNamespace("bpmn",
            "http://www.omg.org/spec/BPMN/20100524/MODEL");

    Namespace BRANDNEWDATA_NAMESPACE = DocumentHelper.createNamespace("brandnewdata",
            "https://www.brandnewdata.com");

    Namespace ZEEBE_NAMESPACE = DocumentHelper.createNamespace("zeebe",
            "http://camunda.org/schema/zeebe/1.0");
}
