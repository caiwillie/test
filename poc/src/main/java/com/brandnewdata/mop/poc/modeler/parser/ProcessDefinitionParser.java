package com.brandnewdata.mop.poc.modeler.parser;

import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import io.camunda.zeebe.client.ZeebeClient;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import javax.xml.namespace.NamespaceContext;
import java.io.StringReader;

public class ProcessDefinitionParser implements ProcessDefinitionParseStep1 {

    private ProcessDefinition processDefinition;

    private Document document;

    private ZeebeClient client;

    // 工厂模式
    private ProcessDefinitionParser(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        this.document = readDocument(processDefinition.getXml());
    }

    public static ProcessDefinitionParseStep1 newInstance(ProcessDefinition processDefinition) {
        return new ProcessDefinitionParser(processDefinition);
    }

    private Document readDocument(String xml) {
        try (StringReader reader = new StringReader(xml)) {
            SAXReader saxReader = new SAXReader();
            return saxReader.read(reader);
        } catch (Exception e) {
            throw new RuntimeException("读取 XML 错误", e);
        }
    }

    private void convertZeebe2Namespace() {
        XPath path = DocumentHelper.createXPath("*");
        Element element = document.getRootElement();


    }

}
