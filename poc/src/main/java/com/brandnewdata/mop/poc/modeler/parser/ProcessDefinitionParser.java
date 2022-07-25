package com.brandnewdata.mop.poc.modeler.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.modeler.parser.constants.StringPool;
import com.brandnewdata.mop.poc.parser.IOMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.brandnewdata.mop.poc.modeler.parser.constants.AttributeConstants.*;
import static com.brandnewdata.mop.poc.modeler.parser.constants.LocalNameConstants.INPUTS_LOCALNAME;
import static com.brandnewdata.mop.poc.modeler.parser.constants.NamespaceConstants.BPMN2_NAMESPACE;
import static com.brandnewdata.mop.poc.modeler.parser.constants.NamespaceConstants.BPMN_NAMESPACE;
import static com.brandnewdata.mop.poc.modeler.parser.constants.QNameConstants.*;

@Slf4j
public class ProcessDefinitionParser implements
        ProcessDefinitionParseStep1, ProcessDefinitionParseStep2 {
    private String oldDocument;

    private String processId;

    private String name;

    private Document document;

    private static final ParameterParser INPUT_PARSER = new ParameterParser(
            BRANDNEWDATA_INPUT_QNAME.getQualifiedName(),
            NAME_ATTRIBUTE, VALUE_ATTRIBUTE, LABEL_ATTRIBUTE, TYPE_ATTRIBUTE, DATA_TYPE_ATTRIBUTE);

    private static final ParameterParser OUTPUT_PARSER = new ParameterParser(
            BRANDNEWDATA_OUTPUT_QNAME.getQualifiedName(),
            NAME_ATTRIBUTE, VALUE_ATTRIBUTE, LABEL_ATTRIBUTE, TYPE_ATTRIBUTE, DATA_TYPE_ATTRIBUTE);

    private static final IOMapParser IO_MAP_PARSER = new IOMapParser();

    // 工厂模式
    private ProcessDefinitionParser(ProcessDefinition processDefinition) {
        init(processDefinition);
    }

    public static ProcessDefinitionParseStep1 newInstance(ProcessDefinition processDefinition) {
        return new ProcessDefinitionParser(processDefinition);
    }

    private void init(ProcessDefinition processDefinition) {
        this.document = readDocument(processDefinition.getXml());
        this.oldDocument = serialize(document);
        this.processId = processDefinition.getProcessId();
        this.name = processDefinition.getName();
        convertZeebe2Namespace();
        parseProcessIdAndName();
    }

    private Document readDocument(String xml) {
        try (StringReader reader = new StringReader(xml)) {
            SAXReader saxReader = new SAXReader();
            return saxReader.read(reader);
        } catch (Exception e) {
            throw new RuntimeException("读取 XML 错误", e);
        }
    }

    /**
     * 替换 zeebe2 namespace
     */
    private void convertZeebe2Namespace() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN2_ALL_QNAME));
        List<Node> nodes = path.selectNodes(document);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if(node instanceof Element) {
                Element e = (Element) node;
                QName oldQName = e.getQName();
                QName qName = DocumentHelper.createQName(oldQName.getName(), BPMN_NAMESPACE);
                e.setQName(qName);
            }
        }
        Element root = document.getRootElement();
        root.add(BPMN_NAMESPACE);
        Namespace bpmn2 = root.getNamespaceForPrefix(BPMN2_NAMESPACE);
        if(bpmn2 != null) {
            root.remove(bpmn2);
        }

    }

    /**
     * 解析 process id 和 name
     */
    private void parseProcessIdAndName() {
        if(processId != null && name != null) {
            return;
        }
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                BPMN_DEFINITIONS_QNAME.getQualifiedName(),
                BPMN_PROCESS_QNAME.getQualifiedName()));

        Element node = (Element) path.selectSingleNode(document);
        processId = node.attributeValue(ID_ATTRIBUTE);
        name = node.attributeValue(NAME_ATTRIBUTE);
    }

    private String serialize(Document document) {

        // 关闭底层字符串Writer, XMLWriter
        try (FastStringWriter stringWriter = new FastStringWriter()) {

            OutputFormat outformat = OutputFormat.createPrettyPrint();
            //  Warning: using your own Writer may cause the writer's preferred character encoding to be ignored.
            // 使用自定义的writer，可能导致outformat中设置的字符串编码设置失效
            XMLWriter writer = new XMLWriter(stringWriter, outformat);
            writer.write(document);
            writer.flush();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 替换 bpmn:serviceTask/bpmn:extensionElements/brandnewdata:taskDefinition
     */
    private void replaceServiceTask() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(document);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }

        for (Node node : nodes) {
            // 替换 brandnewdata:taskDefinition
            Element oldE = (Element) node;
            Element parent = oldE.getParent();
            List<Node> content = parent.content();
            int index = content.indexOf(oldE);
            Element newE = DocumentHelper.createElement(ZEEBE_TASK_DEFINITION_QNAME);
            newE.addAttribute(TYPE_ATTRIBUTE, oldE.attributeValue(TYPE_ATTRIBUTE));
            newE.setParent(parent);
            content.set(index, newE);
        }
    }

    private void replaceInputMapping() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_INPUT_MAPPING_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(document);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            Element oldE = (Element) node;
            Element parent = oldE.getParent();
            // 获取 zeebe:ioMapping
            Element ioMapping = getIoMapping(parent);
            ObjectNode parameters = INPUT_PARSER.parse(oldE);

            Iterator<Map.Entry<String, JsonNode>> iterator = parameters.fields();
            while(iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                String target = StrUtil.format("{}.{}", INPUTS_LOCALNAME, entry.getKey());
                String source = StrUtil.format("{} {}", StringPool.EQUALS, entry.getValue());
                Element newE = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
                newE.addAttribute(TARGET_ATTRIBUTE, target);
                newE.addAttribute(SOURCE_ATTRIBUTE, source);
                newE.setParent(ioMapping);
                ioMapping.content().add(newE);
            }
            // 移除 brandnewdata:inputMapping
            parent.remove(oldE);
        }
    }

    private void replaceOutputMapping() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_OUTPUT_MAPPING_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(document);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            Element oldE = (Element) node;
            Element parent = oldE.getParent();
            // 获取 zeebe:ioMapping
            Element ioMapping = getIoMapping(parent);
            ObjectNode parameters = INPUT_PARSER.parse(oldE);

            List<IOMap> ioMapList = IO_MAP_PARSER.parse(parameters);
            for (IOMap ioMap : ioMapList) {
                Element newE = DocumentHelper.createElement(ZEEBE_OUTPUT_QNAME);
                newE.addAttribute(TARGET_ATTRIBUTE, ioMap.getTarget());
                newE.addAttribute(SOURCE_ATTRIBUTE, ioMap.getSource());
                newE.setParent(ioMapping);
                ioMapping.content().add(newE);
            }
            parent.remove(oldE);
        }

    }

    private Element getIoMapping(Element parent) {
        Element ioMapping = (Element) parent.selectSingleNode(ZEEBE_IO_MAPPING_QNAME.getQualifiedName());

        if(ioMapping == null) {
            ioMapping = DocumentHelper.createElement(ZEEBE_IO_MAPPING_QNAME);
            ioMapping.setParent(parent);
            parent.content().add(ioMapping);
        }
        return ioMapping;
    }

    @Override
    public ProcessDefinition build() {
        String current = serialize(document);
        String TEMPLATE =
                "\n======================= 转换前 xml =======================\n {}" +
                        "\n======================= 转换后 xml =======================\n {}";
        log.info(StrUtil.format(TEMPLATE, oldDocument, current));
        ProcessDefinition ret = new ProcessDefinition();
        ret.setProcessId(processId);
        ret.setName(name);
        ret.setXml(current);
        return ret;
    }

    @Override
    public ProcessDefinitionParseStep2 parseStep1() {
        replaceServiceTask();
        replaceInputMapping();
        return this;
    }
}
