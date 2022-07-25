package com.brandnewdata.mop.poc.modeler.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.common.constant.TriggerProtocolConstant;
import com.brandnewdata.connector.api.IConnectorConfFeign;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import com.brandnewdata.mop.poc.modeler.dto.TriggerOrOperate;
import com.brandnewdata.mop.poc.modeler.dto.TriggerProcessDefinition;
import com.brandnewdata.mop.poc.modeler.parser.constants.StringPool;
import com.brandnewdata.mop.poc.parser.IOMap;
import com.brandnewdata.mop.poc.service.ServiceUtil;
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
import static com.brandnewdata.mop.poc.modeler.parser.constants.BusinessConstants.*;
import static com.brandnewdata.mop.poc.modeler.parser.constants.NamespaceConstants.*;
import static com.brandnewdata.mop.poc.modeler.parser.constants.QNameConstants.*;

@Slf4j
public class ProcessDefinitionParser implements
        ProcessDefinitionParseStep1, ProcessDefinitionParseStep2, ProcessDefinitionParseStep3 {
    private String oldDocument;

    private String processId;

    private String name;

    private Document document;

    private String documentStr;

    private String protocol;

    private TriggerOrOperate trigger;

    private ObjectNode requestParams;

    private static final ParameterParser INPUT_PARSER = new ParameterParser(
            BRANDNEWDATA_INPUT_QNAME.getQualifiedName(),
            NAME_ATTRIBUTE, VALUE_ATTRIBUTE, LABEL_ATTRIBUTE, TYPE_ATTRIBUTE, DATA_TYPE_ATTRIBUTE);

    private static final ParameterParser OUTPUT_PARSER = new ParameterParser(
            BRANDNEWDATA_OUTPUT_QNAME.getQualifiedName(),
            NAME_ATTRIBUTE, VALUE_ATTRIBUTE, LABEL_ATTRIBUTE, TYPE_ATTRIBUTE, DATA_TYPE_ATTRIBUTE);

    private static final ParameterParser REQUEST_PARSER = new ParameterParser(
            BRANDNEWDATA_REQUEST_QNAME.getQualifiedName(),
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
        Namespace bpmn2 = root.getNamespaceForPrefix(BPMN2_NAMESPACE_PRIFIX);
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

    private void replaceServiceTaskInputMapping() {
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
            replaceBrandnewdataInputMapping(oldE);
        }
    }

    private void replaceBrandnewdataInputMapping(Element inputMapping) {
        Element parent = inputMapping.getParent();
        // 获取 zeebe:ioMapping
        Element ioMapping = getIoMapping(parent);
        ObjectNode parameters = INPUT_PARSER.parse(inputMapping);

        Iterator<Map.Entry<String, JsonNode>> iterator = parameters.fields();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String target = StrUtil.format("{}.{}", INPUTS_PREFIX, entry.getKey());
            String source = StrUtil.format("{} {}", StringPool.EQUALS, entry.getValue());
            Element newE = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
            newE.addAttribute(TARGET_ATTRIBUTE, target);
            newE.addAttribute(SOURCE_ATTRIBUTE, source);
            newE.setParent(ioMapping);
            ioMapping.content().add(newE);
        }
        // 移除 brandnewdata:inputMapping
        parent.remove(inputMapping);
    }

    private void replaceServiceTaskOutputMapping() {
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
            replaceBrandnewdataOutputMapping(oldE);
        }
    }

    private void replaceBrandnewdataOutputMapping(Element outputMapping) {
        Element parent = outputMapping.getParent();
        // 获取 zeebe:ioMapping
        Element ioMapping = getIoMapping(parent);
        ObjectNode parameters = OUTPUT_PARSER.parse(outputMapping);

        List<IOMap> ioMapList = IO_MAP_PARSER.parse(parameters);
        for (IOMap ioMap : ioMapList) {
            Element newE = DocumentHelper.createElement(ZEEBE_OUTPUT_QNAME);
            newE.addAttribute(TARGET_ATTRIBUTE, ioMap.getTarget());
            newE.addAttribute(SOURCE_ATTRIBUTE, ioMap.getSource());
            newE.setParent(ioMapping);
            ioMapping.content().add(newE);
        }
        parent.remove(outputMapping);
    }

    /**
     * 将自定义连接器替换成 bpmn:callActivity
     */
    private void replaceCustomServiceTask() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                ZEEBE_TASK_DEFINITION_QNAME.getQualifiedName()));

        List<Node> nodes = path.selectNodes(document);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }

        for (Node node : nodes) {
            Element oldE = (Element) node;
            String type = oldE.attributeValue(TYPE_ATTRIBUTE);
            TriggerOrOperate triggerOrOperate = getTriggerOrOperate(type);

            if(StrUtil.equalsAny(triggerOrOperate.getGroupId(), BRANDNEWDATA_DOMAIN)) {
                // 通用连接器直接跳过
               continue;
            }

            Element extensionElements = oldE.getParent();
            Element serviceTask = extensionElements.getParent();

            // 将 bpmn:serviceTask 替换成 bpmn:callActivity
            serviceTask.setQName(BPMN_CALL_ACTIVITY_QNAME);

            Element newE = DocumentHelper.createElement(ZEEBE_CALLED_ELEMENT_QNAME);
            String processId = ServiceUtil.convertModelKey(type);
            newE.addAttribute(PROCESS_ID_ATTRIBUTE, processId);
            newE.addAttribute(PROPAGATE_ALL_CHILD_VARIABLES_ATTRIBUTE, StringPool.FALSE);
            newE.setParent(extensionElements);

            // 替换成 calledElement
            List<Node> content = extensionElements.content();
            content.set(content.indexOf(oldE), newE);
        }
    }

    private void clearServiceTask() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName()));

        List<Node> nodes = path.selectNodes(document);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }

        for (Node node : nodes) {
            Element oldE = (Element) node;
            Iterator<Attribute> iterator = oldE.attributeIterator();
            // 删除 id, name 之外的其他属性
            while(iterator.hasNext()) {
                Attribute attribute = iterator.next();
                if(!StrUtil.equalsAny(attribute.getName(), ID_ATTRIBUTE, NAME_ATTRIBUTE)) {
                    iterator.remove();
                }
            }
        }
    }

    private TriggerOrOperate getTriggerOrOperate(String type) {
        String[] arr = type.split(":");
        Assert.isTrue(arr.length == 3, ErrorMessage.CHECK_ERROR("触发器或者连接器类型错误", type));
        String groupId = arr[0];
        Assert.notEmpty(groupId, ErrorMessage.NOT_NULL("开发者"));
        String version = arr[2];
        Assert.notEmpty(version, ErrorMessage.NOT_NULL("连接器版本"));

        // 解析 连接器id.操作或触发器id
        arr = arr[1].split(".");
        Assert.isTrue(arr.length == 2, ErrorMessage.CHECK_ERROR("触发器或者连接器类型错误", type));
        String connectorId = arr[0];
        Assert.notEmpty(connectorId, ErrorMessage.NOT_NULL("连接器 id"));
        String triggerOrOperateId = arr[1];
        Assert.notEmpty(triggerOrOperateId, ErrorMessage.NOT_NULL("触发器 id"));

        TriggerOrOperate ret = new TriggerOrOperate();
        ret.setGroupId(groupId);
        ret.setConnectorId(connectorId);
        ret.setTriggerOrOperateId(triggerOrOperateId);
        ret.setVersion(version);

        return ret;
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


    private void logXML() {
        documentStr = serialize(document);
        String TEMPLATE =
                "\n======================= 转换前 xml =======================\n {}" +
                        "\n======================= 转换后 xml =======================\n {}";
        log.info(StrUtil.format(TEMPLATE, oldDocument, documentStr));
    }

    private void removeUnusedNamespace() {
        Element root = document.getRootElement();
        List<Namespace> namespaces = root.declaredNamespaces();
        for (Namespace namespace : namespaces) {
            if(StrUtil.equalsAny(namespace.getPrefix(), DI_NAMESPACE_PRIFIX, DC_NAMESPACE_PRIFIX,
                    BPMNDI_NAMESPACE_PRIFIX, ZEEBE_NAMESPACE.getPrefix(), BPMN_NAMESPACE.getPrefix())) {
                continue;
            }
            root.remove(namespace);
        }
    }

    @Override
    public ProcessDefinition buildProcessDefinition() {
        // 移除无用信息
        removeUnusedNamespace();
        // 打xml日志
        logXML();
        ProcessDefinition ret = new ProcessDefinition();
        ret.setProcessId(processId);
        ret.setName(name);
        ret.setXml(documentStr);
        return ret;
    }

    @Override
    public ProcessDefinitionParseStep3 replaceTriggerStartEvent() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_START_EVENT_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(document);
        Assert.isTrue(CollUtil.size(nodes) == 1, ErrorMessage.CHECK_ERROR("有且只能有一个触发器", null));

        Element oldE = (Element) nodes.get(0).getParent().getParent();
        replaceNoneStartEvent(oldE);

        requestParams = getRequestParams(oldE);

        return this;
    }

    @Override
    public ProcessDefinitionParseStep3 replaceConnectorStartEvent() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_START_EVENT_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(document);
        Assert.isTrue(CollUtil.size(nodes) == 1, ErrorMessage.CHECK_ERROR("有且只能有一个触发器", null));

        Element oldE = (Element) nodes.get(0);
        replaceNoneStartEvent(oldE);

        return this;
    }


    /**
     * 替换成空启动事件
     */
    private void replaceNoneStartEvent(Element startEvent) {
        Element parent = startEvent.getParent();

        // 空启动事件
        Element newE = DocumentHelper.createElement(BPMN_START_EVENT_QNAME);
        newE.addAttribute(ID_ATTRIBUTE, startEvent.attributeValue(ID_ATTRIBUTE));
        newE.addAttribute(NAME_ATTRIBUTE, startEvent.attributeValue(NAME_ATTRIBUTE));
        newE.setParent(parent);

        List<Node> content = parent.content();
        content.set(content.indexOf(startEvent), newE);
    }

    private ObjectNode getRequestParams(Element startEvent) {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_REQUEST_MAPPING_QNAME.getQualifiedName()));
        Element oldE = (Element) path.selectSingleNode(startEvent);
        // 缺少监听配置
        Assert.notNull(oldE, ErrorMessage.NOT_NULL("监听配置"));
        return REQUEST_PARSER.parse(oldE);
    }

    private void replaceSceneGeneralStartEvent(Element startEvent) {
        // 获取监听参数
        requestParams = getRequestParams(startEvent);
        String connectorId = trigger.getConnectorId();
        protocol = TriggerProtocolConstant.getProtocolByConnectorId(connectorId);
    }

    private void replaceSceneCustomStartEvent(Element startEvent) {

    }

    @Override
    public ProcessDefinitionParseStep3 replaceSceneStartEvent() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_START_EVENT_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(document);
        Assert.isTrue(CollUtil.size(nodes) == 1, ErrorMessage.CHECK_ERROR("有且只能有一个触发器", null));

        Element oldE = (Element) nodes.get(0);

        String type = oldE.attributeValue(TYPE_ATTRIBUTE);

        trigger = getTriggerOrOperate(type);

        Element startEvent = oldE.getParent().getParent();

        if(StrUtil.equals(trigger.getGroupId(), BRANDNEWDATA_DOMAIN)) {
            // 通用触发器
            replaceSceneGeneralStartEvent(startEvent);
        } else {
            // 自定义触发器


        }

        return this;
    }

    @Override
    public ProcessDefinitionParseStep2 replaceStep1() {
        clearServiceTask();
        replaceServiceTask();
        replaceServiceTaskInputMapping();
        replaceServiceTaskOutputMapping();
        replaceCustomServiceTask();
        return this;
    }

    @Override
    public ProcessDefinitionParseStep2 replaceProperties(ConnectorManager manager) {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(document);
        if(CollUtil.isEmpty(nodes)) {
            return this;
        }

        for (Node node : nodes) {
            Element oldE = (Element) node;
            Element parent = oldE.getParent();
            Element ioMapping = getIoMapping(parent);
            String configId = oldE.attributeValue(CONFIG_ID_ATTRIBUTE);
            if(StrUtil.isBlank(configId)) {
                continue;
            }
            String properties = manager.getProperties(configId);
            if(StrUtil.isNotBlank(properties)) {
                Element newE = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
                newE.addAttribute(TARGET_ATTRIBUTE, PROPERTIES_PREFIX);
                newE.addAttribute(SOURCE_ATTRIBUTE, StrUtil.format("{} {}", StringPool.EQUALS, properties));
                newE.setParent(ioMapping);
                ioMapping.content().add(newE);
            }
        }

        // 这里不需要删除 brandnewdata:taskDefinition
        return this;
    }

    @Override
    public TriggerProcessDefinition buildTriggerProcessDefinition() {
        return null;
    }
}
