package com.brandnewdata.mop.poc.modeler.parser.constants;

import com.brandnewdata.mop.poc.parser.BPMNNamespace;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;

public interface NamespaceConstants {

    Namespace BPMN_NAMESPACE = DocumentHelper.createNamespace("bpmn",
            "http://www.omg.org/spec/BPMN/20100524/MODEL");

    Namespace BRANDNEWDATA_NAMESPACE = DocumentHelper.createNamespace("brandnewdata",
            "https://www.brandnewdata.com");

    Namespace ZEEBE_NAMESPACE = DocumentHelper.createNamespace("zeebe",
            "http://camunda.org/schema/zeebe/1.0");
}
