package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.common.constant.TriggerProtocolConstant;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.manager.ConnectorManager;
import com.brandnewdata.mop.poc.parser.IOMap;
import com.brandnewdata.mop.poc.process.dto.*;
import com.brandnewdata.mop.poc.process.parser.constants.StringPool;
import com.brandnewdata.mop.poc.service.ServiceUtil;
import com.dxy.library.json.jackson.JacksonUtil;
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

import static com.brandnewdata.mop.poc.process.parser.constants.AttributeConstants.*;
import static com.brandnewdata.mop.poc.process.parser.constants.BusinessConstants.*;
import static com.brandnewdata.mop.poc.process.parser.constants.NamespaceConstants.*;
import static com.brandnewdata.mop.poc.process.parser.constants.QNameConstants.*;

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

    private ObjectNode responseParams;

    private static final ParameterParser INPUT_PARSER = new ParameterParser(
            BRANDNEWDATA_INPUT_QNAME.getQualifiedName(),
            NAME_ATTRIBUTE, VALUE_ATTRIBUTE, LABEL_ATTRIBUTE, TYPE_ATTRIBUTE, DATA_TYPE_ATTRIBUTE);

    private static final ParameterParser OUTPUT_PARSER = new ParameterParser(
            BRANDNEWDATA_OUTPUT_QNAME.getQualifiedName(),
            NAME_ATTRIBUTE, VALUE_ATTRIBUTE, LABEL_ATTRIBUTE, TYPE_ATTRIBUTE, DATA_TYPE_ATTRIBUTE);

    private static final ParameterParser REQUEST_PARSER = new ParameterParser(
            BRANDNEWDATA_REQUEST_QNAME.getQualifiedName(),
            NAME_ATTRIBUTE, VALUE_ATTRIBUTE, LABEL_ATTRIBUTE, TYPE_ATTRIBUTE, DATA_TYPE_ATTRIBUTE);

    private static final ParameterParser RESPONSE_PARSER = new ParameterParser(
            BRANDNEWDATA_RESPONSE_QNAME.getQualifiedName(),
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
        // 转换namespace zeebe2 =》 zeebe
        convertZeebe2Namespace();
        // 设置流程名称 和 id
        parseProcessIdAndName();
        // 设置为可执行
        setExecutable();
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
        Element bpProcess = getBpProcess();
        if(processId != null && name != null) {
            bpProcess.addAttribute(ID_ATTRIBUTE, ServiceUtil.convertModelKey(processId));
            bpProcess.addAttribute(NAME_ATTRIBUTE, name);
        } else {
            processId = bpProcess.attributeValue(ID_ATTRIBUTE);
            name = bpProcess.attributeValue(NAME_ATTRIBUTE);
            Assert.notEmpty(processId, ErrorMessage.NOT_NULL("流程 id"));
            Assert.notEmpty(name, ErrorMessage.NOT_NULL("流程名称"));
        }
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
            replaceBndTaskDefinitionToZbTaskDefinition(oldE);
        }
    }

    private Element replaceBndTaskDefinitionToZbTaskDefinition(Element taskDefinition) {
        Element parent = taskDefinition.getParent();
        List<Node> content = parent.content();
        Element newE = DocumentHelper.createElement(ZEEBE_TASK_DEFINITION_QNAME);
        newE.addAttribute(TYPE_ATTRIBUTE, taskDefinition.attributeValue(TYPE_ATTRIBUTE));
        newE.setParent(parent);
        // 将 brandnewdata:taskDefinition替换成 zeebe:taskDefinition
        content.set(content.indexOf(taskDefinition), newE);
        return newE;
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
            replaceBndInputMappingToZbIoMapping(oldE);
        }
    }

    private ObjectNode replaceBndInputMappingToZbIoMapping(Element inputMapping) {
        if(inputMapping == null) return null;
        Element parent = inputMapping.getParent();
        // 获取 zeebe:ioMapping
        Element ioMapping = getZbIoMapping(parent);
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
        return parameters;
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
            replaceBndOutputMappingToZbIoMapping(oldE);
        }
    }

    private ObjectNode replaceBndOutputMappingToZbIoMapping(Element outputMapping) {
        if(outputMapping == null) return null;
        Element parent = outputMapping.getParent();
        // 获取 zeebe:ioMapping
        Element ioMapping = getZbIoMapping(parent);
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
        return parameters;
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

            Element serviceTask = oldE.getParent().getParent();

            // 将 bpmn:serviceTask 替换成 bpmn:callActivity
            serviceTask.setQName(BPMN_CALL_ACTIVITY_QNAME);

            // 将 zeebe:taskDefinition 替换成 zeebe:calledElement
            replaceZbTaskDefinitionToCalledElement(oldE);
        }
    }

    private void replaceZbTaskDefinitionToCalledElement(Element taskDefinition) {
        String type = taskDefinition.attributeValue(TYPE_ATTRIBUTE);
        Element parent = taskDefinition.getParent();
        // 创建 called element
        Element newE = DocumentHelper.createElement(ZEEBE_CALLED_ELEMENT_QNAME);
        // 替换特殊字符
        String processId = ServiceUtil.convertModelKey(type);
        newE.addAttribute(PROCESS_ID_ATTRIBUTE, processId);
        newE.addAttribute(PROPAGATE_ALL_CHILD_VARIABLES_ATTRIBUTE, StringPool.FALSE);
        newE.setParent(parent);
        // 替换成 calledElement
        List<Node> content = parent.content();
        content.set(content.indexOf(taskDefinition), newE);
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
            clearAttribute(oldE, ID_ATTRIBUTE, NAME_ATTRIBUTE);
        }
    }

    /**
     * 清除多余属性
     *
     * @param e 元素
     * @param attributes 保留属性列表
     */
    private void clearAttribute(Element e, String... attributes) {
        Iterator<Attribute> iterator = e.attributeIterator();
        // 删除 id, name 之外的其他属性
        while(iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if(!StrUtil.equalsAny(attribute.getName(), attributes)) {
                iterator.remove();
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
        arr = arr[1].split("\\.");
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

    private Element getZbIoMapping(Element parent) {
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
            if(StrUtil.equalsAny(namespace.getPrefix(), DI_NAMESPACE.getPrefix(), DC_NAMESPACE.getPrefix(),
                    BPMNDI_NAMESPACE.getPrefix(), ZEEBE_NAMESPACE.getPrefix(), BPMN_NAMESPACE.getPrefix())) {
                continue;
            }
            root.remove(namespace);
        }
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

    private ObjectNode getResponseParams(Element startEvent) {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_RESPONSE_MAPPING_QNAME.getQualifiedName()));
        Element oldE = (Element) path.selectSingleNode(startEvent);
        // 缺少监听配置
        if(oldE == null) {
            return null;
        } else {
            return RESPONSE_PARSER.parse(oldE);
        }
    }

    private void replaceSceneGeneralStartEventToNoneStartEvent(Element bndTaskDefinition) {
        // 获取监听参数, 获取 startEvent
        Element startEvent = bndTaskDefinition.getParent().getParent();

        // 获取 监听配置 和 响应配置
        requestParams = getRequestParams(startEvent);

        responseParams = getResponseParams(startEvent);

        protocol = TriggerProtocolConstant.getProtocolByConnectorId(trigger.getConnectorId());

        // 替换成 空启动事件
        replaceNoneStartEvent(startEvent);
    }

    /**
     * 根据 bnd task definition 解析 trigger
     * @param bndTaskDefinition
     */
    private void parseTrigger(Element bndTaskDefinition) {
        String type = bndTaskDefinition.attributeValue(TYPE_ATTRIBUTE);
        trigger = getTriggerOrOperate(type);
    }

    private void replaceSceneCustomStartEventToNoneStartEvent(Element bndTaskDefinition, ConnectorManager manager) {
        String xml = manager.getTriggerXML(trigger);

        // 解析自定义触发器内的通用触发器
        ProcessDefinition _processDefintion = new ProcessDefinition();
        _processDefintion.setProcessId(trigger.getFullId());
        _processDefintion.setXml(xml);
        TriggerProcessDefinition triggerProcessDefinition = ProcessDefinitionParser.newInstance(_processDefintion)
                .replaceStep1().replaceTriggerStartEvent().buildTriggerProcessDefinition();

        // 替换成真实协议
        protocol = triggerProcessDefinition.getProtocol();
        requestParams = triggerProcessDefinition.getRequestParams();
        responseParams = triggerProcessDefinition.getResponseParams();

        // 替换成 call activity
        BndStartEvent bndStartEvent = replaceBndStartEventToCallActivity(bndTaskDefinition);

        Element callActivity = bndStartEvent.getCallActivity();

        // 新增空开始事件
        Element bpProcess = getBpProcess();
        Element noneStartEvent = ElementCreator.createBpStartEvent();
        noneStartEvent.setParent(bpProcess);
        bpProcess.content().add(noneStartEvent);

        // 连接 none start event 与 callActivity
        connectTwoElement(noneStartEvent, callActivity);

    }

    private BndStartEvent replaceBndStartEventToCallActivity(Element bndTaskDefinition) {
        Element startEvent = bndTaskDefinition.getParent().getParent();

        Element inputMapping = (Element) startEvent.selectSingleNode(StrUtil.join(StringPool.SLASH,
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_INPUT_MAPPING_QNAME.getQualifiedName()));
        ObjectNode inputs = replaceBndInputMappingToZbIoMapping(inputMapping);
        // 根据触发器的输入 进行 requestMapping 的表达式计算
        evalRequestParams(inputs);

        Element outputMapping = (Element) startEvent.selectSingleNode(StrUtil.join(StringPool.SLASH,
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_OUTPUT_MAPPING_QNAME.getQualifiedName()));
        replaceBndOutputMappingToZbIoMapping(outputMapping);

        Element zbTaskDefinition = replaceBndTaskDefinitionToZbTaskDefinition(bndTaskDefinition);

        // 将 zeebe:taskDefinition 替换成 zeebe:calledElement
        replaceZbTaskDefinitionToCalledElement(zbTaskDefinition);

        // 清除多余属性
        clearAttribute(startEvent, ID_ATTRIBUTE, NAME_ATTRIBUTE);

        String oldId = startEvent.attributeValue(ID_ATTRIBUTE);
        String newId = ElementCreator.generateActivityId();

        // 修改 startEvent 的 qName
        startEvent.setQName(BPMN_CALL_ACTIVITY_QNAME);
        // 替换成新 id
        startEvent.addAttribute(ID_ATTRIBUTE, newId);

        // 替换 bpmndi:BPMNShape
        XPath shapePath = DocumentHelper.createXPath(StrUtil.format("//{}[@{}='{}']",
                BPMNDI_BPMN_SHAPE_QNAME.getQualifiedName(), BPMN_ELEMENT_ATTRIBUTE, oldId));
        Element shape = (Element) shapePath.selectSingleNode(document);
        shape.addAttribute(ID_ATTRIBUTE, StrUtil.format("{}_di", newId));
        shape.addAttribute(BPMN_ELEMENT_ATTRIBUTE, newId);

        // 转换 x, y 的节点
        alignRectangle(shape);

        ShapeCenter shapeCenter = getShapeCenter(shape);
        long[] rightCenter = shapeCenter.getRightCenter();

        // 替换 sequence 的属性 source ref
        XPath sequencePath = DocumentHelper.createXPath(StrUtil.format("//{}[@{}='{}']",
                BPMN_SEQUENCE_FLOW_QNAME.getQualifiedName(), SOURCE_REF_ATTRIBUTE, oldId));
        List<Node> nodes = sequencePath.selectNodes(document);
        if(CollUtil.isNotEmpty(nodes)) {
            for (Node node : nodes) {
                Element sequence = (Element) node;
                sequence.addAttribute(SOURCE_REF_ATTRIBUTE, newId);
                String sequenceId = sequence.attributeValue(ID_ATTRIBUTE);
                XPath edgePath = DocumentHelper.createXPath(StrUtil.format("//{}[@{}='{}']",
                        BPMNDI_BPMN_EDGE_QNAME.getQualifiedName(), BPMN_ELEMENT_ATTRIBUTE, sequenceId));
                Element edge = (Element) edgePath.selectSingleNode(document);
                List<Node> wayPoints = edge.selectNodes(DI_WAYPOINT_QNAME.getQualifiedName());
                Element firstWayPoint = (Element) wayPoints.get(0);
                // 修改所有连线的节点到 右边界中点
                firstWayPoint.addAttribute(X_ATTRIBUTE, String.valueOf(rightCenter[0]));
                firstWayPoint.addAttribute(Y_ATTRIBUTE, String.valueOf(rightCenter[1]));
            }
        }

        BndStartEvent ret = new BndStartEvent();
        ret.setInputs(inputs);
        ret.setCallActivity(startEvent);

        return ret;
    }

    public void evalRequestParams(ObjectNode inputs) {
        if(inputs == null) return;
        // 先序列化成字符串，反序列化时，未识别的 token 转换为 null
        Map<String, Object> values = FeelUtil.convertMap(inputs);
        String expression = JacksonUtil.to(requestParams);
        Object obj = FeelUtil.evalExpression(expression, values);
        // 将计算完成的结果赋值给 requestParams
        requestParams = FeelUtil.convertValue(obj, ObjectNode.class);
    }

    private void alignRectangle(Element shape) {
        // 修改 bpmndi:BPMNShape 中的 weight, height, x, y

        // 获取中心节点
        ShapeCenter shapeCenter = getShapeCenter(shape);
        long[] rightCenter = shapeCenter.getRightCenter();
        alignRight(shape, rightCenter[0], rightCenter[1], 100, 80);
    }

    private ShapeCenter getShapeCenter(Element shape) {
        Element bounds = (Element) shape.selectSingleNode(DC_BOUNDS_QNAME.getQualifiedName());

        long x = Long.parseLong(bounds.attributeValue(X_ATTRIBUTE));
        long y = Long.parseLong(bounds.attributeValue(Y_ATTRIBUTE));
        long width = Long.parseLong(bounds.attributeValue(WIDTH_ATTRIBUTE));
        long height = Long.parseLong(bounds.attributeValue(HEIGHT_ATTRIBUTE));

        ShapeCenter ret = new ShapeCenter();

        ret.setGeometryCenter(new long[] {(long)(x + 0.5 * width), (long)(y + 0.5 * height)});
        ret.setUpCenter(new long[] {(long)(x + 0.5 * width), y + height});
        ret.setRightCenter(new long[] {x + width, (long)(y + 0.5 * height)});
        ret.setDownCenter(new long[] {(long)(x + 0.5 * width), y});
        ret.setLeftCenter(new long[] {x, (long)(y + 0.5 * height)});
        return ret;
    }

    private void alignRight(Element shape, long rx, long ry, long width, long height) {
        Element bounds = (Element) shape.selectSingleNode(DC_BOUNDS_QNAME.getQualifiedName());
        bounds.addAttribute(WIDTH_ATTRIBUTE, String.valueOf(width));
        bounds.addAttribute(HEIGHT_ATTRIBUTE, String.valueOf(height));
        long x = (long) (rx - width);
        long y = (long) (ry - 0.5 * height);
        bounds.addAttribute(X_ATTRIBUTE, String.valueOf(x));
        bounds.addAttribute(Y_ATTRIBUTE, String.valueOf(y));
    }

    private void connectTwoElement(Element source, Element target) {
        Element sequenceFlow = ElementCreator.createBpSequenceFlow();
        Element bpProcess = getBpProcess();
        sequenceFlow.setParent(bpProcess);
        bpProcess.content().add(sequenceFlow);

        sequenceFlow.addAttribute(SOURCE_REF_ATTRIBUTE, source.attributeValue(ID_ATTRIBUTE));
        sequenceFlow.addAttribute(TARGET_REF_ATTRIBUTE, target.attributeValue(ID_ATTRIBUTE));

        String sequenceId = sequenceFlow.attributeValue(ID_ATTRIBUTE);

        Element outgoing = ElementCreator.createOutgoing();
        outgoing.setText(sequenceId);
        outgoing.setParent(source);
        source.content().add(outgoing);

        Element incoming = ElementCreator.createIncoming();
        incoming.setText(sequenceId);
        incoming.setParent(target);
        target.content().add(incoming);
    }

    private Element getBpProcess() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                BPMN_DEFINITIONS_QNAME.getQualifiedName(),
                BPMN_PROCESS_QNAME.getQualifiedName()));
        return (Element) path.selectSingleNode(document);
    }

    private void setExecutable() {
        Element process = getBpProcess();
        process.addAttribute(IS_EXECUTABLE_ATTRIBUTE, StringPool.TRUE);
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
        Element bndTaskDefinition = (Element) nodes.get(0);

        parseTrigger(bndTaskDefinition);

        // 触发器的开始事件都是底层通用触发器
        protocol = TriggerProtocolConstant.getProtocolByConnectorId(trigger.getConnectorId());

        Element startEvent = (Element) bndTaskDefinition.getParent().getParent();

        // 获取 request params
        requestParams = getRequestParams(startEvent);

        responseParams = getResponseParams(startEvent);

        // 替换成 空启动事件
        replaceNoneStartEvent(startEvent);
        return this;
    }

    @Override
    public ProcessDefinitionParseStep3 replaceOperateStartEvent() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_START_EVENT_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(document);
        Assert.isTrue(CollUtil.size(nodes) == 1, ErrorMessage.CHECK_ERROR("有且只能有一个触发器", null));

        Element startEvent = (Element) nodes.get(0);
        replaceNoneStartEvent(startEvent);

        return this;
    }

    @Override
    public ProcessDefinitionParseStep3 replaceSceneStartEvent(ConnectorManager manager) {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_START_EVENT_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(document);
        Assert.isTrue(CollUtil.size(nodes) == 1, ErrorMessage.CHECK_ERROR("有且只能有一个触发器", null));

        Element oldE = (Element) nodes.get(0);

        parseTrigger(oldE);

        if(StrUtil.equals(trigger.getGroupId(), BRANDNEWDATA_DOMAIN)) {
            // 通用触发器
            replaceSceneGeneralStartEventToNoneStartEvent(oldE);
        } else {
            // 自定义触发器
            replaceSceneCustomStartEventToNoneStartEvent(oldE, manager);
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
    public ProcessDefinitionParseStep1 replaceProperties(ConnectorManager manager) {
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
            Element ioMapping = getZbIoMapping(parent);
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
        // 移除无用信息
        removeUnusedNamespace();
        // 打xml日志
        logXML();
        TriggerProcessDefinition ret = new TriggerProcessDefinition();
        ret.setProcessId(processId);
        ret.setName(name);
        ret.setXml(documentStr);
        ret.setProtocol(protocol);
        ret.setTrigger(trigger);
        ret.setRequestParams(requestParams);
        ret.setResponseParams(responseParams);
        return ret;
    }
}
