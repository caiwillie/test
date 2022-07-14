package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.service.ServiceUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.util.RawValue;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static com.brandnewdata.mop.poc.parser.XMLConstants.*;
import static com.brandnewdata.mop.poc.parser.XMLConstants.ZEEBE_INPUT_QNAME;

@Slf4j
public class XMLParser3 implements XMLParseStep1, XMLParseStep1.XMLParseStep2, XMLParseStep1.XMLParseStep3 {

    private String modelKey;

    private String name;

    private String xml;

    private String triggerFullId;

    private String protocol;
    private ObjectNode requestParamConfigs;

    private Document document;

    public XMLParser3(String modelKey, String name) {
        this.modelKey = ServiceUtil.convertModelKey(modelKey);
        this.name = name;
    }

    public XMLParser3() {
    }

    @Override
    public XMLParseStep2 parse(String xml) {
        this.xml = xml;
        try (StringReader stringReader = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            document = reader.read(stringReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Element root = document.getRootElement();
        // 底层是调用 Namespace.get(), 会全局缓存新创建的nmaespcae
        // 在根元素上加上namespace，下面的所有其他元素都可以使用这个Namespace
        root.addNamespace(ZEEBE_NAMESPACE.getPrefix(), ZEEBE_NAMESPACE.getURI());

        // 从根节点开始遍历
        walkTree(root);

        return this;
    }

    @Override
    public XMLParseStep3 replaceGeneralTrigger() {
        Element startEvent = getRootStartEvent();

        Iterator<Element> iterator = startEvent.elementIterator();

        while(iterator.hasNext()) {
            // 删除outgoing以外的其他元素
            Element element = iterator.next();
            if(!StrUtil.equals(element.getQualifiedName(), BPMN_OUTGOING_QNAME.getQualifiedName())) {
                iterator.remove();
            }
        }

        return this;
    }

    @Override
    public XMLParseStep3 replaceCustomTrigger() {
        Element startEvent = getRootStartEvent();
        Element root = startEvent.getParent();

        Element callActivity = createCallActivity(root, startEvent);

        createSequenceFlow(root, startEvent, callActivity);

        // 处理 request mapping
        handleRequestMapping(startEvent);



        return this;
    }

    private Element createCallActivity(Element root, Element startEvent) {
        List<Node> outgoingList = startEvent.selectNodes(BPMN_OUTGOING_QNAME.getQualifiedName());
        List<Node> sequenceFlows = root.selectNodes(BPMN_SEQUENCE_FLOW_QNAME.getQualifiedName());

        Element taskDefinition = getBPMNTaskDefinition(startEvent);
        Element zeebeIOMapping = getZeebeIOMapping(startEvent);

        triggerFullId = taskDefinition.attributeValue(TYPE_ATTR);
        taskDefinition.getParent().remove(taskDefinition);

        Element callActivity = DocumentHelper.createElement(BPMN_CALL_ACTIVITY_QNAME);
        String callActivityId = StrUtil.format("Activity_{}", RandomUtil.randomString(9));
        callActivity.addAttribute(ID_ATTR, callActivityId);
        callActivity.addAttribute(NAME_ATTR, "执行自定义触发器");
        callActivity.setParent(root);
        root.content().add(callActivity);

        Element extensionElements = DocumentHelper.createElement(BPMN_EXTENSION_ELEMENTS_QNAME);
        extensionElements.setParent(callActivity);
        callActivity.content().add(extensionElements);

        Element callElement = DocumentHelper.createElement(ZEEBE_CALLED_ELEMENT_QNAME);
        callElement.addAttribute(PROCESS_ID_ATTR, ServiceUtil.convertModelKey(triggerFullId));
        callElement.addAttribute(PROPAGATE_ALL_CHILD_VARIABLES_ATTR, Constants.TYPE_BOOLEAN_FALSE);
        callElement.setParent(extensionElements);
        extensionElements.content().add(callElement);

        // 放入zeebeIOMapping
        zeebeIOMapping.getParent().remove(zeebeIOMapping);
        zeebeIOMapping.setParent(extensionElements);
        extensionElements.content().add(zeebeIOMapping);

        // 放入outgoing
        Set<String> outgoingNameSet = new HashSet<>();
        for (int i = 0; i < outgoingList.size(); i++) {
            Element outgoing = (Element) outgoingList.get(i);
            outgoing.getParent().remove(outgoing);
            outgoing.setParent(callActivity);
            callActivity.content().add(outgoing);
            outgoingNameSet.add(outgoing.getText());
        }

        // 修改 sequence flow 的 source ref
        for (int i = 0; i < sequenceFlows.size(); i++) {
            Element sequenceFlow = (Element) sequenceFlows.get(i);
            if(outgoingNameSet.contains(sequenceFlow.attributeValue(ID_ATTR))) {
                // 修改 sequence flow 的 SOURCE_REF
                sequenceFlow.attribute(SOURCE_REF_ATTR).setValue(callActivityId);
            }
        }

        Node startExtensionElements = startEvent.selectSingleNode(BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName());
        if(startExtensionElements != null) {
            startExtensionElements.getParent().remove(startExtensionElements);
        }

        return callActivity;
    }

    private Element createSequenceFlow(Element root, Element source, Element target) {
        String sourceRef = source.attributeValue(ID_ATTR);
        String targetRef = target.attributeValue(ID_ATTR);

        Element sequenceFlow = DocumentHelper.createElement(BPMN_SEQUENCE_FLOW_QNAME);
        String sequenceFlowId = StrUtil.format("FLOW_{}", RandomUtil.randomString(9));
        sequenceFlow.addAttribute(ID_ATTR, sequenceFlowId);
        sequenceFlow.addAttribute(SOURCE_REF_ATTR, sourceRef);
        sequenceFlow.addAttribute(TARGET_REF_ATTR, targetRef);
        sequenceFlow.setParent(root);
        root.content().add(sequenceFlow);

        Element outgoing = DocumentHelper.createElement(BPMN_OUTGOING_QNAME);
        outgoing.addText(sequenceFlowId);
        outgoing.setParent(source);
        source.content().add(outgoing);

        Element incoming = DocumentHelper.createElement(BPMN_INCOMING_QNAME);
        incoming.addText(sequenceFlowId);
        incoming.setParent(target);
        target.content().add(incoming);

        return incoming;
    }

    private Element getRootStartEvent() {
        Element root = document.getRootElement();

        // 从 bpmn:process 开始计算
        XPath startEventXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_PROCESS_QNAME.getQualifiedName(),
                BPMN_START_EVENT_QNAME.getQualifiedName()));

        Element startEvent = (Element) startEventXPATH.selectSingleNode(root);

        if(startEvent == null) {
            throw new IllegalArgumentException("没有定义触发器");
        }

        return startEvent;
    }

    private void handleRequestMapping(Element startEvent) {
        XPath requestMappingXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_REQUEST_MAPPING_QNAME.getQualifiedName()));

        Element requestMapping = (Element) requestMappingXPATH.selectSingleNode(startEvent);

        // brandnewdata:requestMapping 不存在，就直接返回
        if(requestMapping == null) return;

        ParametersParser parametersParser = new ParametersParser(BRANDNEWDATA_REQUEST_QNAME.getQualifiedName(),
                NAME_ATTR, VALUE_ATTR, LABEL_ATTR, TYPE_ATTR, DATA_TYPE_ATTR);

        requestParamConfigs = parametersParser.parse(requestMapping);

        requestMapping.getParent().remove(requestMapping);
    }

    @Override
    public XMLDTO build() {
        XMLDTO ret = new XMLDTO();
        ret.setName(name);
        ret.setModelKey(modelKey);
        ret.setRequestParamConfigs(requestParamConfigs);
        ret.setTriggerFullId(triggerFullId);

        String zeebeXML = serializa(document);
        ret.setZeebeXML(zeebeXML);

        log.info("\n" +
                "==============================转换前的 xml 内容============================\n" +
                "{} \n" +
                "==============================转换后的 xml 内容============================\n" +
                "{}", xml, zeebeXML);

        return ret;
    }

    private void walkTree(Element element) {
        List<Element> elements = element.elements();

        if(CollUtil.isNotEmpty(elements)) {
            // 递归循环
            for (Element e : elements) {
                walkTree(e);
            }
        }

        // 处理 bpmn2 namespace
        replaceBPMN2Namespace(element);

        // 处理 model key 与 名称
        handleBPMNProcess(element);

        // 处理 brandnewdata:taskDefinition
        replaceBNDTaskDefinition(element);

        // 处理 brandnewdata:inputMapping
        replaceBNDInputMapping(element);

        // 处理 brandnewdata:outputMapping
        replaceBNDOutputMapping(element);

        // 处理连接器
        handleConnector(element);
    }

    private String serializa(Document document) {

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

    private void replaceBPMN2Namespace(Element element) {
        // 替换 namespace
        if (StrUtil.equals(element.getNamespacePrefix(), BPMNNamespace.BPMN2.getPrefix())) {
            // 会逐级向上查找namespace
            element.setQName(QName.get(element.getName(), BPMN_NAMESPACE));
        }
    }

    private void handleBPMNProcess(Element element) {
        // 只处理 bpmn:process，直接跳过
        if(!StrUtil.equals(element.getQualifiedName(), BPMN_PROCESS_QNAME.getQualifiedName())) {
            return;
        }

        if(modelKey == null) {
            modelKey = element.attributeValue(ID_ATTR);
            Assert.notEmpty(modelKey, "BPMN解析错误：模型标识不能为空");
        } else {
            element.addAttribute(ID_ATTR, modelKey);
        }

        if(name == null) {
            name = element.attributeValue(NAME_ATTR);
            Assert.notBlank(name,"BPMN解析错误：模型名称不能为空");
        } else {
            element.addAttribute(NAME_ATTR, name);
        }

        // isExecutable="false"
        element.addAttribute(IS_EXECUTABLE_ATTR, Constants.TYPE_BOOLEAN_TRUE);

    }

    private void replaceBNDTaskDefinition(Element element) {
        // 替换 brandnewdata:taskDefinition to zeebe:taskDefinition
        if (StrUtil.equals(element.getQualifiedName(), BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName())) {
            // 会逐级向上查找namespace
            Element parent = element.getParent();

            Element zeebeTaskDefinition = DocumentHelper.createElement(ZEEBE_TASK_DEFINITION_QNAME);
            zeebeTaskDefinition.addAttribute(TYPE_ATTR, element.attributeValue(TYPE_ATTR));
            zeebeTaskDefinition.setParent(parent);
            List<Node> content = parent.content();
            content.set(content.indexOf(element), zeebeTaskDefinition);
        }
    }

    private void replaceBNDInputMapping(Element element) {
        if(!StrUtil.equalsAny(element.getQualifiedName(),
                BPMN_START_EVENT_QNAME.getQualifiedName(),
                BPMN_SERVICE_TASK_QNAME.getQualifiedName())) {
            return;
        }

        XPath inputMappingXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_INPUT_MAPPING_QNAME.getQualifiedName()));

        Element inputMapping = (Element) inputMappingXPATH.selectSingleNode(element);

        // brandnewdata:inputMapping 不存在，就直接返回
        if(inputMapping == null) return;

        Element zeebeIOMapping = getZeebeIOMapping(element);

        ParametersParser parametersParser = new ParametersParser(BRANDNEWDATA_INPUT_QNAME.getQualifiedName(),
                NAME_ATTR, VALUE_ATTR, LABEL_ATTR, TYPE_ATTR, DATA_TYPE_ATTR);

        ObjectNode parameters = parametersParser.parse(inputMapping);

        Iterator<Map.Entry<String, JsonNode>> iterator = parameters.fields();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String target = StrUtil.format("{}.{}", Constants.INPUTS, entry.getKey());
            // 加上 = 号代表表达式
            String source = StrUtil.format("{} {}", Constants.EQUALS, entry.getValue());
            Element input = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
            input.addAttribute(TARGET_ATTR, target);
            input.addAttribute(SOURCE_ATTR, source);
            input.setParent(zeebeIOMapping);
            // 新增 zeebe:input
            zeebeIOMapping.content().add(input);
        }

        // 移除inputMapping
        inputMapping.getParent().remove(inputMapping);
    }

    private void replaceBNDOutputMapping(Element element) {
        if(!StrUtil.equalsAny(element.getQualifiedName(),
                BPMN_START_EVENT_QNAME.getQualifiedName(),
                BPMN_SERVICE_TASK_QNAME.getQualifiedName())) {
            return;
        }

        XPath inputMappingXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_OUTPUT_MAPPING_QNAME.getQualifiedName()));

        Element outputMapping = (Element) inputMappingXPATH.selectSingleNode(element);

        // brandnewdata:outMapping 不存在，就直接返回
        if(outputMapping == null) return;

        Element zeebeIOMapping = getZeebeIOMapping(element);


        ParametersParser parametersParser = new ParametersParser(BRANDNEWDATA_OUTPUT_QNAME.getQualifiedName(),
                NAME_ATTR, VALUE_ATTR, LABEL_ATTR, TYPE_ATTR, DATA_TYPE_ATTR);

        ObjectNode parameters = parametersParser.parse(outputMapping);

        List<IOMap> ioMaps = parseObjectIOMapList(parameters, null);

        for (IOMap ioMap : ioMaps) {
            Element output = DocumentHelper.createElement(ZEEBE_OUTPUT_QNAME);
            output.addAttribute(TARGET_ATTR, ioMap.getTarget());
            output.addAttribute(SOURCE_ATTR, ioMap.getSource());
            output.setParent(zeebeIOMapping);
            // 新增 zeebe:output
            zeebeIOMapping.content().add(output);
        }

        outputMapping.getParent().remove(outputMapping);
    }

    private Element getZeebeIOMapping(Element element) {
        XPath extensionElementsXPATH = DocumentHelper.createXPath(StrUtil.format("./{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName()));

        Element extensionElements = (Element) extensionElementsXPATH.selectSingleNode(element);

        Element ioMapping = (Element) extensionElements.selectSingleNode(ZEEBE_IO_MAPPING_QNAME.getQualifiedName());

        if(ioMapping == null) {
            ioMapping = DocumentHelper.createElement(ZEEBE_IO_MAPPING_QNAME);
            ioMapping.setParent(extensionElements);
            extensionElements.content().add(ioMapping);
        }

        return ioMapping;
    }

    private Element getBPMNTaskDefinition(Element element) {
        XPath path = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                ZEEBE_TASK_DEFINITION_QNAME.getQualifiedName()));

        Element taskDefinition = (Element) path.selectSingleNode(element);
        if(taskDefinition == null) {
            throw new IllegalArgumentException(StrUtil.format("元素 {} 下未定义 taskDefinition",
                    element.attributeValue(ID_ATTR)));
        }

        if(taskDefinition.attributeValue(TYPE_ATTR) == null) {
            throw new IllegalArgumentException(StrUtil.format("元素 {} 下未定义 taskDefinition.type",
                    element.attributeValue(ID_ATTR)));
        }

        return taskDefinition;
    }

    private void handleConnector(Element element) {

        // 不是 bpmn:serviceTask 跳过
        if(!StrUtil.equals(element.getQualifiedName(), BPMN_SERVICE_TASK_QNAME.getQualifiedName())) return;

        Element taskDefinition = getBPMNTaskDefinition(element);

        // 处理call activity
        handleCallActivity(element, taskDefinition);

        // 处理入参数映射
        // handleGeneralConnectorInputs(element, taskDefinition);
    }

    private void handleCallActivity(Element task, Element taskDefinition) {
        // 除了 com.brandnewdata 开头外的所有其他serviceTask都当作 call activity 处理
        String type = taskDefinition.attributeValue(TYPE_ATTR);
        if(type.startsWith(Constants.DOMAIN_BND)) return;

        // 修改 bpmn:serviceTask to bpmn:callActivityTask
        task.setQName(BPMN_CALL_ACTIVITY_QNAME);

        Element parent = taskDefinition.getParent();;

        // 创建 callElement，并设置 processId = type
        Element callElement = DocumentHelper.createElement(ZEEBE_CALLED_ELEMENT_QNAME);
        String processId = ServiceUtil.convertModelKey(type);
        callElement.addAttribute(PROCESS_ID_ATTR, processId);
        callElement.addAttribute(PROPAGATE_ALL_CHILD_VARIABLES_ATTR, Constants.TYPE_BOOLEAN_FALSE);
        callElement.setParent(parent);
        List<Node> content = parent.content();
        content.set(content.indexOf(taskDefinition), callElement);
    }



    private void handleGeneralConnectorInputs(Element task, Element taskDefinition) {
        XPath inputXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                ZEEBE_IO_MAPPING_QNAME.getQualifiedName(),
                ZEEBE_INPUT_QNAME.getQualifiedName()));

        // 如果不是 通用连接器 那就不处理
        if(!StrUtil.startWith(taskDefinition.attributeValue(TYPE_ATTR), Constants.DOMAIN_BND)) return;

        List<Node> inputs = inputXPATH.selectNodes(task);

        if(CollUtil.isEmpty(inputs)) return;

        for (int i = 0; i < inputs.size(); i++) {
            Element input = (Element) inputs.get(i);
            // 通用连接器 target 前面加上 inputs
            Attribute attribute = input.attribute(TARGET_ATTR);
            attribute.setValue(Constants.INPUTS + "." + attribute.getValue());
        }

    }

    private List<IOMap> parseEachIOMapList(JsonNode jsonNode, String parent) {
        List<IOMap> ret = new ArrayList<>();
        if(jsonNode == null) {
            return ret;
        }

        POJONode node = (POJONode) jsonNode;
        Object pojo = node.getPojo();
        if(pojo instanceof ObjectNode) {
            ret.addAll(parseObjectIOMapList((ObjectNode) pojo, parent));
        } else if (pojo instanceof ArrayNode) {
            ret.addAll(parseArrayIOMapList((ArrayNode) pojo, parent));
        } else {
            IOMap ioMap = parseRawValueIOMapList((RawValue) pojo, parent);
            if(ioMap != null) {
                ret.add(ioMap);
            }
        }
        return ret;
    }

    private List<IOMap> parseObjectIOMapList(ObjectNode parameters, String parent) {
        List<IOMap> ret = new ArrayList<>();
        if(parameters == null) {
            return ret;
        }

        Iterator<Map.Entry<String, JsonNode>> iterator = parameters.fields();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String name = entry.getKey();
            JsonNode value = entry.getValue();
            // 判断是否一级参数
            String path = StrUtil.isBlank(parent) ? name : parent + "." + name;
            List<IOMap> ioMaps = parseEachIOMapList(value, path);
            ret.addAll(ioMaps);
        }
        return ret;
    }

    private List<IOMap> parseArrayIOMapList(ArrayNode parameters, String parent) {
        List<IOMap> ret = new ArrayList<>();
        if(parameters == null) {
            return ret;
        }
        Iterator<JsonNode> iterator = parameters.iterator();
        int index = 1;
        while(iterator.hasNext()) {
            JsonNode node = iterator.next();
            // parent不可能为空
            String path = StrUtil.format("{}[{}]", parent, index);
            ret.addAll(parseEachIOMapList(node, path));
            index++;
        }

        return ret;
    }

    private IOMap parseRawValueIOMapList(RawValue parameter, String parent) {
        IOMap ret = null;
        if(parameter == null) {
            return ret;
        }
        // 添加 = 表示这是表达式
        ret = new IOMap("=" + parent, parameter.rawValue().toString());
        return ret;
    }

}
