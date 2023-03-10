package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.manager.ConnectorManager;
import com.brandnewdata.mop.poc.process.manager.dto.ConfigInfo;
import com.brandnewdata.mop.poc.process.parser.constants.StringPool;
import com.brandnewdata.mop.poc.process.parser.dto.*;
import com.brandnewdata.mop.poc.process.util.ProcessUtil;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.brandnewdata.mop.poc.process.parser.constants.AttributeConstants.*;
import static com.brandnewdata.mop.poc.process.parser.constants.BusinessConstants.*;
import static com.brandnewdata.mop.poc.process.parser.constants.NamespaceConstants.*;
import static com.brandnewdata.mop.poc.process.parser.constants.QNameConstants.*;

@Slf4j
public class ProcessDefinitionParser implements
        ProcessDefinitionParseStep1, ProcessDefinitionParseStep2 {

    private Document originalDocument;

    private String originalDocumentStr;

    private Document zeebeDocument;

    private String zeebeDocumentStr;

    private String processId;

    private String name;

    private String protocol;

    private Action trigger;

    private ObjectNode requestParams;

    private ObjectNode responseParams;

    private Map<String, String> configs = new HashMap<>();

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


    public static ProcessDefinitionParseStep1 step1(String processId, String name, String xml) {
        return new ProcessDefinitionParser(processId, name, xml);
    }

    @Override
    public ProcessDefinitionParseStep1 parseConfig() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(zeebeDocument);
        if(CollUtil.isEmpty(nodes)) {
            return this;
        }

        for (Node node : nodes) {
            Element oldE = (Element) node;
            String type = oldE.attributeValue(TYPE_ATTRIBUTE);
            String configId = oldE.attributeValue(CONFIG_ID_ATTRIBUTE);
            if(StrUtil.isBlank(configId)) {
                continue;
            }

            // ??????????????????????????????
            configs.put(configId, type);
        }

        return this;
    }

    @Override
    public ProcessDefinitionParseStep1 replConfigId(Map<String, String> configMapping) {
        return null;
    }

    @Override
    public ProcessDefinitionParseStep1 replServiceTask(boolean replConfig, ConnectorManager manager) {
        // ???????????????????????????????????????
        if(replConfig) {
            // ????????????????????????????????? manager ?????????
            replEle_Config(manager);
        }

        replEleBndImInZbSt();

        replEleBndOmInZbSt();

        replEleBndTdInBndSt();

        replEleConntoZbCa();

        // ==============================

        // ?????? service task ???????????????
        clearServiceTask();

        return this;
    }

    @Override
    public ProcessDefinitionParseStep1 replAttr() {
        replAttrXt();
        return this;
    }

    @Override
    public ProcessDefinitionParseStep2 step2() {
        return this;
    }

    @Override
    public Step1Result step1Result() {
        // ??????
        clearNamespace();
        clearServiceTask();
        // ???xml??????
        serializeXml();
        Step1Result ret = new Step1Result();
        ret.setProcessId(processId);
        ret.setProcessName(name);
        ret.setOriginalXml(originalDocumentStr);
        ret.setZeebeXml(zeebeDocumentStr);
        ret.setConnectorConfigMap(configs);
        return ret;
    }

    @Override
    public ProcessDefinitionParseStep2 replEleTriggerSe(ConnectorManager manager) {
        // ??????????????????
        Element startEvent = parseSE();

        // ?????? brandnewdata:taskDefinition
        Element bndTD = parseBndTD(startEvent);
        Assert.notNull(bndTD, "[0x01] ???????????????????????????");

        parseTrigger(bndTD);

        // ???????????????????????????????????????????????????
        protocol = manager.getProtocol(trigger.getConnectorId());

        // ?????? request params
        requestParams = parseReq(startEvent);

        responseParams = parseResp(startEvent);

        // ????????? ???????????????
        replEleSeToNone(startEvent);
        return this;
    }

    @Override
    public ProcessDefinitionParseStep2 replEleOperateSe() {
        Element startEvent = parseSE();
        replEleSeToNone(startEvent);
        return this;
    }

    @Override
    public ProcessDefinitionParseStep2 replEleSceneSe(ConnectorManager manager) {
        // ??????????????????
        Element startEvent = parseSE();

        // ?????? brandnewdata:taskDefinition
        Element bndTD = parseBndTD(startEvent);

        // ??????brandnewdata:taskDefinition, ??????BPMN?????????????????????????????????
        if(bndTD == null) return this;

        parseTrigger(bndTD);

        if(StrUtil.equals(trigger.getConnectorGroup(), BRANDNEWDATA_DOMAIN)) {
            // ???????????????
            replEleSceneGseToNse(manager, bndTD);
        } else {
            // ??????????????????
            replEleSceneCseToNse(manager, bndTD);
        }

        return this;
    }


    @Override
    public Step2Result step2Result() {
        Step1Result step1Result = step1Result();
        Step2Result ret = new Step2Result();
        BeanUtil.copyProperties(step1Result, ret);
        ret.setProtocol(protocol);
        ret.setTrigger(trigger);
        ret.setRequestParams(requestParams);
        ret.setResponseParams(responseParams);
        return ret;
    }

    // ????????????
    private ProcessDefinitionParser(String processId, String name, String xml) {
        init(processId, name, xml);
    }

    /**
     * ?????????
     * @param name
     * @param processId
     * @param xml
     */
    private void init(String processId, String name, String xml) {
        this.processId = processId;
        this.name = name;
        this.originalDocument = readRoot(xml);
        this.zeebeDocument = readRoot(xml);

        // ??????namespace zeebe2 => zeebe
        replNsZeebe2();

        // ?????? namespace
        addNs();

        // ?????????????????? ??? id
        parseIdName();

        // ??????????????????
        set_exec();
    }

    /**
     * ???????????????
     * @param xml
     * @return
     */
    private Document readRoot(String xml) {
        try (StringReader reader = new StringReader(xml)) {
            SAXReader saxReader = new SAXReader();
            return saxReader.read(reader);
        } catch (Exception e) {
            throw new RuntimeException("?????? XML ??????", e);
        }
    }

    /**
     * ?????? namespace zeebe2 => namespace
     */
    private void replNsZeebe2() {
        Namespace namespace = zeebeDocument.getRootElement().getNamespaceForPrefix(BPMN2_NAMESPACE.getPrefix());
        // bpmn2 namespace ???????????????????????????
        if(namespace == null) return;

        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN2_ALL_QNAME));
        List<Node> nodes = path.selectNodes(zeebeDocument);
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
        Element root = zeebeDocument.getRootElement();
        root.add(BPMN_NAMESPACE);
        Namespace bpmn2 = root.getNamespaceForPrefix(BPMN2_NAMESPACE.getPrefix());
        if(bpmn2 != null) {
            root.remove(bpmn2);
        }
    }

    private void addNs() {
        addNs(ZEEBE_NAMESPACE);
        addNs(BRANDNEWDATA_NAMESPACE);
    }

    /**
     * ?????? namespace zeebe
     */
    private void addNs(Namespace ns) {
        Element root = zeebeDocument.getRootElement();
        Namespace namespace = root.getNamespaceForPrefix(ns.getPrefix());
        if(namespace == null) {
            root.add(ns);
        }
    }

    /**
     * ?????? process id ??? name
     */
    private void parseIdName() {
        Element bpProcess = getBpProcess();
        Element originalBpProcess = getOriginalBpProcess();

        if(processId == null) {
            processId = bpProcess.attributeValue(ID_ATTRIBUTE);
        }

        processId = ProcessUtil.convertProcessId(processId);
        Assert.notEmpty(processId, "???????????????????????????id????????????");

        // ????????????????????????
        originalBpProcess.addAttribute(ID_ATTRIBUTE, processId);
        bpProcess.addAttribute(ID_ATTRIBUTE, processId);

        if(name == null) {
            name = bpProcess.attributeValue(NAME_ATTRIBUTE);
        }

        if(name == null) {
            name = processId;
        }

        originalBpProcess.addAttribute(ID_ATTRIBUTE, processId);
        bpProcess.addAttribute(NAME_ATTRIBUTE, name);
    }

    /**
     * ?????? bpmn:serviceTask/bpmn:extensionElements/brandnewdata:taskDefinition ??????configId???
     * ????????????????????????????????????zeebe:ioMapping/input ???
     * @param manager ?????????
     * @return
     */
    private ProcessDefinitionParseStep1 replEle_Config(ConnectorManager manager) {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(zeebeDocument);
        if(CollUtil.isEmpty(nodes)) {
            return this;
        }

        for (Node node : nodes) {
            Element oldE = (Element) node;
            Element parent = oldE.getParent();
            Element ioMapping = getOrCreateIoMapping(parent);
            String configId = oldE.attributeValue(CONFIG_ID_ATTRIBUTE);
            if(StrUtil.isBlank(configId)) {
                continue;
            }
            ConfigInfo configInfo = manager.getConfigInfo(configId);
            Assert.notNull(configInfo, "???????????? {} ?????????", configId);
            String properties = configInfo.getConfigs();
            if(StrUtil.isNotBlank(configInfo.getConfigs())) {
                Element newE = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
                newE.addAttribute(TARGET_ATTRIBUTE, PROPERTIES_PREFIX);
                newE.addAttribute(SOURCE_ATTRIBUTE, StrUtil.format("{} {}", StringPool.EQUALS, properties));
                newE.setParent(ioMapping);
                ioMapping.content().add(newE);
            }
        }

        return this;
    }

    /**
     * ?????? bpmn:serviceTask/bpmn:extensionElements/brandnewdata:taskDefinition
     * ?????? bpmn:serviceTask/bpmn:extensionElements/zeebe:taskDefinition
     */
    private void replEleBndTdInBndSt() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(zeebeDocument);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }

        for (Node node : nodes) {
            // ?????? brandnewdata:taskDefinition
            Element oldE = (Element) node;
            replEleBndTdToZbTd(oldE);
        }
    }

    /**
     * ?????? bpmn:serviceTask/bpmn:extensionElements/brandnewdata:taskDefinition
     * ?????? bpmn:serviceTask/bpmn:extensionElements/zeebe:taskDefinition
     */
    private Element replEleBndTdToZbTd(Element taskDefinition) {
        Element parent = taskDefinition.getParent();
        List<Node> content = parent.content();
        Element newE = DocumentHelper.createElement(ZEEBE_TASK_DEFINITION_QNAME);
        // ???????????? type, isBrandnewdataConnector
        newE.addAttribute(TYPE_ATTRIBUTE, taskDefinition.attributeValue(TYPE_ATTRIBUTE));
        newE.addAttribute(TYPE_IS_BND_CONNECTOR, StringPool.TRUE);
        newE.setParent(parent);
        // ?????? brandnewdata:taskDefinition ????????? zeebe:taskDefinition
        content.set(content.indexOf(taskDefinition), newE);
        return newE;
    }

    /**
     * ?????? //bpmn:serviceTask/bpmn:extensionElements/brandnewdata:inputMapping
     * ?????? //bpmn:serviceTask/bpmn:extensionElements/zeebe:ioMapping/zeebe:input
     */
    private void replEleBndImInZbSt() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_INPUT_MAPPING_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(zeebeDocument);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            Element oldE = (Element) node;
            replEleBndImToZbImi(oldE);
        }
    }

    /**
     * ?????? //bpmn:serviceTask/bpmn:extensionElements/brandnewdata:outputMapping
     * ?????? //bpmn:serviceTask/bpmn:extensionElements/zeebe:ioMapping/zeebe:output
     */
    private void replEleBndOmInZbSt() {
        //bpmn:serviceTask/bpmn:extensionElements/brandnewdata
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_OUTPUT_MAPPING_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(zeebeDocument);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            Element oldE = (Element) node;
            replEleBndOmToZbImo(oldE);
        }
    }

    /**
     * ?????? brandnewdata:inputMapping
     * ?????? zeebe:ioMapping/zeebe:input
     * @param inputMapping
     * @return
     */
    private ObjectNode replEleBndImToZbImi(Element inputMapping) {
        if(inputMapping == null) return null;
        Element parent = inputMapping.getParent();
        // ??????????????? zeebe:ioMapping
        Element ioMapping = getOrCreateIoMapping(parent);
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
        // ?????? brandnewdata:inputMapping
        parent.remove(inputMapping);
        return parameters;
    }

    /**
     * ?????? brandnewdata:outputMapping
     * ?????? zeebe:ioMapping/zeebe:output
     * @param outputMapping
     * @return
     */
    private ObjectNode replEleBndOmToZbImo(Element outputMapping) {
        if(outputMapping == null) return null;
        Element parent = outputMapping.getParent();
        // ?????? zeebe:ioMapping
        Element ioMapping = getOrCreateIoMapping(parent);
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
     * ?????? //bpmn:serviceTask/bpmn:extensionElements/zeebe:taskDefinition ??? type?????? brandnewdata.com ??? serviceTask
     * ?????? //bpmn:callActivity
     */
    private void replEleConntoZbCa() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName(),
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                ZEEBE_TASK_DEFINITION_QNAME.getQualifiedName()));

        List<Node> nodes = path.selectNodes(zeebeDocument);
        if(CollUtil.isEmpty(nodes)) {
            return;
        }

        for (Node node : nodes) {
            Element oldE = (Element) node;
            String value = oldE.attributeValue(TYPE_IS_BND_CONNECTOR);
            if(!StrUtil.equals(value, StringPool.TRUE)) {
                // ????????? ?????????????????????????????????
                continue;
            }

            Action action = parseActionAndReplType(oldE, false);

            if(StrUtil.equalsAny(action.getConnectorGroup(), BRANDNEWDATA_DOMAIN)) {
                // ????????????????????? service ??????????????????
                clearAttribute(oldE, TYPE_ATTRIBUTE);
                // ???????????????????????????
               continue;
            }

            parseActionAndReplType(oldE, true);

            Element serviceTask = oldE.getParent().getParent();

            // ??? bpmn:serviceTask ????????? bpmn:callActivity
            serviceTask.setQName(BPMN_CALL_ACTIVITY_QNAME);
            // ??????????????????
            clearAttribute(serviceTask, ID_ATTRIBUTE, NAME_ATTRIBUTE);

            // ??? zeebe:taskDefinition ????????? zeebe:calledElement
            replEleZbTdToZbCe(oldE);
        }
    }


    /**
     * ?????? zeebe:taskDefinition ?????? type ??????????????? Action, ????????????type
     */
    private Action parseActionAndReplType(Element taskDefinition, boolean replType) {
        String type = taskDefinition.attributeValue(TYPE_ATTRIBUTE);
        Action action = ProcessUtil.parseAction(type);
        // ?????????action????????????type
        String newType = ProcessUtil.convertProcessId(type);
        ProcessUtil.checkProcessId(newType);
        if(replType) {
            taskDefinition.addAttribute(TYPE_ATTRIBUTE, newType);
        }
        return action;
    }


    /**
     * ?????? //zeebe:taskDefinition
     * ?????? //zeebe:calledElement
     */
    private void replEleZbTdToZbCe(Element taskDefinition) {
        String type = taskDefinition.attributeValue(TYPE_ATTRIBUTE);
        Element parent = taskDefinition.getParent();
        // ?????? called element
        Element newE = DocumentHelper.createElement(ZEEBE_CALLED_ELEMENT_QNAME);
        newE.addAttribute(PROCESS_ID_ATTRIBUTE, type);
        newE.addAttribute(PROPAGATE_ALL_CHILD_VARIABLES_ATTRIBUTE, StringPool.FALSE);
        newE.setParent(parent);
        // ????????? calledElement
        List<Node> content = parent.content();
        content.set(content.indexOf(taskDefinition), newE);
    }

    /**
     * ?????? xsi:type = "bpmn2:tFormalExpression"
     * ?????? xsi:type = "bpmn:tFormalExpression"
     */
    private void replAttrXt() {
        Namespace bpmn2Namespace = zeebeDocument.getRootElement().getNamespaceForPrefix(BPMN2_NAMESPACE.getPrefix());
        if(bpmn2Namespace == null) return;

        XPath path = DocumentHelper.createXPath(StrUtil.format("//*[@{}='{}']",
                XSI_TYPE_QNAME.getQualifiedName(), BPMN2_T_FORMAL_EXPRESSION_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(zeebeDocument);
        if(CollUtil.isEmpty(nodes)) return;

        for (Node node : nodes) {
            Element e = (Element) node;
            Attribute attribute = e.attribute(XSI_TYPE_QNAME);
            // ?????? xsi:type ??? bpmn:tFormalExpression
            attribute.setValue(BPMN_T_FORMAL_EXPRESSION_QNAME.getQualifiedName());
        }
    }

    /**
     * ????????????????????????????????????????????? zeebe:ioMapping
     * @param parent
     * @return
     */
    private Element getOrCreateIoMapping(Element parent) {
        Element ioMapping = (Element) parent.selectSingleNode(ZEEBE_IO_MAPPING_QNAME.getQualifiedName());

        if(ioMapping == null) {
            ioMapping = DocumentHelper.createElement(ZEEBE_IO_MAPPING_QNAME);
            ioMapping.setParent(parent);
            parent.content().add(ioMapping);
        }
        return ioMapping;
    }

    /**
     * ??????xml
     */
    private void serializeXml() {
        originalDocumentStr = serialize(originalDocument);
        zeebeDocumentStr = serialize(zeebeDocument);
        String TEMPLATE =
                "\n======================= original xml =======================\n{}" +
                        "\n======================= zeebe xml =======================\n{}";
        log.debug(StrUtil.format(TEMPLATE, originalDocumentStr, zeebeDocumentStr));
    }

    /**
     * ???????????????
     * @param document
     * @return
     */
    private String serialize(Document document) {

        // ?????????????????????Writer, XMLWriter
        try (FastStringWriter stringWriter = new FastStringWriter()) {

            OutputFormat outformat = OutputFormat.createPrettyPrint();
            //  Warning: using your own Writer may cause the writer's preferred character encoding to be ignored.
            // ??????????????????writer???????????????outformat???????????????????????????????????????
            XMLWriter writer = new XMLWriter(stringWriter, outformat);
            writer.write(document);
            writer.flush();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ?????? namespace
     */
    private void clearNamespace() {
        Element root = zeebeDocument.getRootElement();
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
     * ?????? service task
     */
    private void clearServiceTask() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_SERVICE_TASK_QNAME.getQualifiedName()));

        List<Node> nodes = Opt.ofNullable(path.selectNodes(zeebeDocument)).orElse(ListUtil.empty());

        for (Node node : nodes) {
            Element oldE = (Element) node;
            clearAttribute(oldE, ID_ATTRIBUTE, NAME_ATTRIBUTE);
        }
    }

    /**
     * ????????????
     *
     * @param e ??????
     * @param attributes ??????????????????
     */
    private void clearAttribute(Element e, String... attributes) {
        Iterator<Attribute> iterator = e.attributeIterator();
        // ?????? id, name ?????????????????????
        while(iterator.hasNext()) {
            Attribute attribute = iterator.next();
            if(!StrUtil.equalsAny(attribute.getName(), attributes)) {
                iterator.remove();
            }
        }
    }

    /**
     * ?????????????????? type ?????? trigger
     * @param bndTD
     */
    private void parseTrigger(Element bndTD) {
        String type = bndTD.attributeValue(TYPE_ATTRIBUTE);
        trigger = ProcessUtil.parseAction(type);
    }

    /**
     * ??????????????????
     * @param startEvent
     * @return
     */
    private ObjectNode parseReq(Element startEvent) {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_REQUEST_MAPPING_QNAME.getQualifiedName()));
        Element oldE = (Element) path.selectSingleNode(startEvent);
        // ??????????????????
        Assert.notNull(oldE, ErrorMessage.NOT_NULL("????????????"));
        return REQUEST_PARSER.parse(oldE);
    }

    /**
     * ??????????????????
     * @param startEvent
     * @return
     */
    private ObjectNode parseResp(Element startEvent) {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_RESPONSE_MAPPING_QNAME.getQualifiedName()));
        Element oldE = (Element) path.selectSingleNode(startEvent);
        // ??????????????????
        if(oldE == null) {
            return null;
        } else {
            return RESPONSE_PARSER.parse(oldE);
        }
    }

    /**
     * ????????????????????????????????????
     */
    private void replEleSeToNone(Element startEvent) {
        Element parent = startEvent.getParent();

        // ???????????????
        Element newE = DocumentHelper.createElement(BPMN_START_EVENT_QNAME);
        newE.addAttribute(ID_ATTRIBUTE, startEvent.attributeValue(ID_ATTRIBUTE));
        newE.addAttribute(NAME_ATTRIBUTE, startEvent.attributeValue(NAME_ATTRIBUTE));
        newE.setParent(parent);

        List<Node> content = parent.content();
        content.set(content.indexOf(startEvent), newE);
    }

    /**
     * ?????? bpmn:startEvent
     * @return
     */
    private Element parseSE() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                StringPool.SLASH,
                BPMN_START_EVENT_QNAME.getQualifiedName()));
        List<Node> nodes = path.selectNodes(zeebeDocument);
        Assert.isTrue(CollUtil.size(nodes) == 1, ErrorMessage.CHECK_ERROR("??????????????????????????????", null));
        return (Element) nodes.get(0);
    }

    /**
     * ???????????????????????? bpmn:extensionElement/brandnewdata:taskDefinition
     * @param element
     * @return
     */
    private Element parseBndTD(Element element) {
        return (Element) element.selectSingleNode(StrUtil.join(StringPool.SLASH,
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));
    }

    /**
     * ???????????????????????????????????????????????????
     * @param manager
     * @param bndTaskDefinition
     */
    private void replEleSceneGseToNse(ConnectorManager manager, Element bndTaskDefinition) {
        // ??????????????????, ?????? startEvent
        Element startEvent = bndTaskDefinition.getParent().getParent();

        // ?????? ???????????? ??? ????????????
        requestParams = parseReq(startEvent);
        responseParams = parseResp(startEvent);

        // ????????????
        protocol = manager.getProtocol(trigger.getConnectorId());

        // ????????? ???????????????
        replEleSeToNone(startEvent);
    }

    /**
     * ??????????????????????????????????????????????????????
     * @param manager
     * @param bndTaskDefinition
     */
    private void replEleSceneCseToNse(ConnectorManager manager, Element bndTaskDefinition) {
        String xml = manager.getTriggerXML(trigger);

        // ?????????????????????????????????????????????
        Step2Result step2Result = ProcessDefinitionParser
                .step1(trigger.getFullId(), null, xml)
                .replServiceTask(false, null)
                .replAttr()
                .step2()
                .replEleTriggerSe(manager)
                .step2Result();

        // ?????????????????????
        protocol = step2Result.getProtocol();
        requestParams = step2Result.getRequestParams();
        responseParams = step2Result.getResponseParams();

        // ????????? call activity
        BndStartEvent bndStartEvent = replEle_BpmnSeToZbCa(bndTaskDefinition);

        Element callActivity = bndStartEvent.getCallActivity();

        // ?????????????????????
        Element bpProcess = getBpProcess();
        Element noneStartEvent = ElementCreator.createBpStartEvent();
        noneStartEvent.setParent(bpProcess);
        bpProcess.content().add(noneStartEvent);

        // ?????? none start event ??? callActivity
        connectTwoElement(noneStartEvent, callActivity);

    }

    /**
     * ?????? //bpmn:startEvent
     * ?????? //zeebe:callActivity
     * @param bndTaskDefinition
     * @return
     */
    private BndStartEvent replEle_BpmnSeToZbCa(Element bndTaskDefinition) {
        Element startEvent = bndTaskDefinition.getParent().getParent();

        Element inputMapping = (Element) startEvent.selectSingleNode(StrUtil.join(StringPool.SLASH,
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_INPUT_MAPPING_QNAME.getQualifiedName()));
        ObjectNode inputs = replEleBndImToZbImi(inputMapping);
        // ???????????????????????? ?????? requestMapping ??????????????????
        evalRequestParams(inputs);

        Element outputMapping = (Element) startEvent.selectSingleNode(StrUtil.join(StringPool.SLASH,
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_OUTPUT_MAPPING_QNAME.getQualifiedName()));
        replEleBndOmToZbImo(outputMapping);

        Element zbTaskDefinition = replEleBndTdToZbTd(bndTaskDefinition);

        parseActionAndReplType(zbTaskDefinition, true);

        // ??? zeebe:taskDefinition ????????? zeebe:calledElement
        replEleZbTdToZbCe(zbTaskDefinition);

        // ??????????????????
        clearAttribute(startEvent, ID_ATTRIBUTE, NAME_ATTRIBUTE);

        String oldId = startEvent.attributeValue(ID_ATTRIBUTE);
        String newId = ElementCreator.generateActivityId();

        // ?????? startEvent ??? qName
        startEvent.setQName(BPMN_CALL_ACTIVITY_QNAME);
        // ???????????? id
        startEvent.addAttribute(ID_ATTRIBUTE, newId);

        // ?????? bpmndi:BPMNShape
        XPath shapePath = DocumentHelper.createXPath(StrUtil.format("//{}[@{}='{}']",
                BPMNDI_BPMN_SHAPE_QNAME.getQualifiedName(), BPMN_ELEMENT_ATTRIBUTE, oldId));
        Element shape = (Element) shapePath.selectSingleNode(zeebeDocument);
        shape.addAttribute(ID_ATTRIBUTE, StrUtil.format("{}_di", newId));
        shape.addAttribute(BPMN_ELEMENT_ATTRIBUTE, newId);

        // ?????? x, y ?????????
        alignRectangle(shape);

        ShapeCenter shapeCenter = getShapeCenter(shape);
        long[] rightCenter = shapeCenter.getRightCenter();

        // ?????? sequence ????????? source ref
        XPath sequencePath = DocumentHelper.createXPath(StrUtil.format("//{}[@{}='{}']",
                BPMN_SEQUENCE_FLOW_QNAME.getQualifiedName(), SOURCE_REF_ATTRIBUTE, oldId));
        List<Node> nodes = sequencePath.selectNodes(zeebeDocument);
        if(CollUtil.isNotEmpty(nodes)) {
            for (Node node : nodes) {
                Element sequence = (Element) node;
                sequence.addAttribute(SOURCE_REF_ATTRIBUTE, newId);
                String sequenceId = sequence.attributeValue(ID_ATTRIBUTE);
                XPath edgePath = DocumentHelper.createXPath(StrUtil.format("//{}[@{}='{}']",
                        BPMNDI_BPMN_EDGE_QNAME.getQualifiedName(), BPMN_ELEMENT_ATTRIBUTE, sequenceId));
                Element edge = (Element) edgePath.selectSingleNode(zeebeDocument);
                List<Node> wayPoints = edge.selectNodes(DI_WAYPOINT_QNAME.getQualifiedName());
                Element firstWayPoint = (Element) wayPoints.get(0);
                // ?????????????????????????????? ???????????????
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
        // ????????????????????????????????????????????????????????? token ????????? null
        Map<String, Object> values = FeelUtil.convertMap(inputs);
        String expression = JacksonUtil.to(requestParams);
        Object obj = FeelUtil.evalExpression(expression, values);
        // ????????????????????????????????? requestParams
        requestParams = FeelUtil.convertValue(obj, ObjectNode.class);
    }

    private void alignRectangle(Element shape) {
        // ?????? bpmndi:BPMNShape ?????? weight, height, x, y

        // ??????????????????
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

        // ! incoming outgoing ?????????????????????incoming ?????????outgoing??????
        Element outgoing = ElementCreator.createOutgoing();
        outgoing.setText(sequenceId);
        outgoing.setParent(source);
        source.content().add(outgoing);


        Element incoming = ElementCreator.createIncoming();
        incoming.setText(sequenceId);
        incoming.setParent(target);

        Element firstOutGoing = (Element) target.selectSingleNode(BPMN_OUTGOING_QNAME.getQualifiedName());
        List<Node> content = target.content();
        // ! ??? incoming ?????????????????? outcoimg??????
        content.add(content.indexOf(firstOutGoing), incoming);
    }

    private Element getBpProcess() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                BPMN_DEFINITIONS_QNAME.getQualifiedName(),
                BPMN_PROCESS_QNAME.getQualifiedName()));
        return (Element) path.selectSingleNode(zeebeDocument);
    }

    private Element getOriginalBpProcess() {
        XPath path = DocumentHelper.createXPath(StrUtil.join(StringPool.SLASH,
                BPMN2_DEFINITIONS_QNAME.getQualifiedName(),
                BPMN2_PROCESS_QNAME.getQualifiedName()));
        return (Element) path.selectSingleNode(originalDocument);
    }

    private void set_exec() {
        Element process = getBpProcess();
        process.addAttribute(IS_EXECUTABLE_ATTRIBUTE, StringPool.TRUE);
    }


}
