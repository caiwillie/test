package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.lang.Assert;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.brandnewdata.mop.poc.parser.XMLConstants.*;
import static com.brandnewdata.mop.poc.parser.XMLConstants.ZEEBE_INPUT_QNAME;

@Slf4j
public class XMLParser3 implements XMLParseStep1, XMLParseStep1.XMLParseStep2, XMLParseStep1.XMLParseStep3 {

    private String modelKey;

    private String name;

    private String xml;

    private Document document;

    public XMLParser3(String modelKey, String name) {
        this.modelKey = modelKey;
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
        Element root = document.getRootElement();

        // 从 bpmn:process 开始计算
        XPath startEventXPATH = DocumentHelper.createXPath(StrUtil.format("{}/{}",
                BPMN_PROCESS_QNAME.getQualifiedName(),
                BPMN_START_EVENT_QNAME.getQualifiedName()));

        Element startEvent = (Element) startEventXPATH.selectSingleNode(root);

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
        return null;
    }

    @Override
    public XMLDTO build() {
        XMLDTO ret = new XMLDTO();
        ret.setName(name);
        ret.setModelKey(modelKey);

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

        // 处理namespace
        handleNamespace(element);

        // 处理 model key 与 名称
        handleRoot(element);

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

    private void handleNamespace(Element element) {
        // 替换 namespace
        if (StrUtil.equals(element.getNamespacePrefix(), BPMNNamespace.BPMN2.getPrefix())) {
            // 会逐级向上查找namespace
            element.setQName(QName.get(element.getName(), BPMN_NAMESPACE));
        }
    }

    private void handleRoot(Element element) {
        QName qName = element.getQName();
        if(!StrUtil.equals(element.getQualifiedName(), BPMN_PROCESS_QNAME.getQualifiedName())) {
            // 不是 bpmn:process，直接跳过
            return;
        }


        if(modelKey == null) {
            modelKey = element.attributeValue("id");
            Assert.notBlank(modelKey, "BPMN解析错误：模型标识不能为空");
        } else {
            // 修改
            element.addAttribute("id", modelKey);
        }

        if(name == null) {
            name = element.attributeValue("name");
            Assert.notBlank(name,"BPMN解析错误：模型名称不能为空");
        } else {
            element.addAttribute("name", name);
        }

        // isExecutable="false"
        element.addAttribute("isExecutable", "true");

    }

    private void handleConnector(Element element) {

        if(!StrUtil.equals(element.getQualifiedName(), BPMN_SERVICE_TASK_QNAME.getQualifiedName())) {
            // task 并且 brandnewdata:extension 不为空，计算为false, 就跳过
            return;
        }

        // 处理任务定义
        String type = handleTaskDefinition(element);

        // 处理call activity
        handleCallActivity(element, type);

        // 处理入参数映射
        handleInputMapping(element, type);

        // 处理出参映射
        handleOutputMapping(element, type);

        return;
    }

    private String handleTaskDefinition(Element task) {

        XPath taskDefinitionXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(), BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));

        Node node = taskDefinitionXPATH.selectSingleNode(task);

        if(!(node instanceof Element)) {
            throw new IllegalArgumentException(StrUtil.format("服务任务 {} 下未定义任务类型", task.getName()));
        }

        Element taskDefinition = (Element) node;

        String type = taskDefinition.attributeValue("type");
        Assert.notBlank(type, "服务任务 {} 下未定义任务类型", task.getName());

        // 转换并且解析成zeebe
        Element taskDefinitionNew = DocumentHelper.createElement(ZEEBE_TASK_DEFINITION_QNAME);

        taskDefinitionNew.addAttribute("type", type);

        Element parent = taskDefinition.getParent();

        int index = parent.indexOf(taskDefinition);
        parent.content().set(index, taskDefinitionNew);
        return type;
    }

    private void handleCallActivity(Element task, String type) {
        if(type.startsWith("com.brandnewdata")) {
            // 是com.brandnewdata就不当作call activity处理
            return;
        }

        // 修改Qname
        task.setQName(BPMN_CALL_ACTIVITY_TASK_QNAME);

        XPath taskDefinitionXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                ZEEBE_TASK_DEFINITION_QNAME.getQualifiedName()));

        // 获取taskDefinition的index
        Node node = taskDefinitionXPATH.selectSingleNode(task);

        Element parent = node.getParent();;

        // 创建 call element，并设置 processId = type
        Element callElement = DocumentHelper.createElement(ZEEBE_CALLED_ELEMENT_QNAME);
        String processId = ServiceUtil.convertModelKey(type);
        callElement.addAttribute("processId", processId);
        callElement.addAttribute("propagateAllChildVariables", "false");
        callElement.setParent(parent);

        List<Node> content = parent.content();
        int index = content.indexOf(node);

        content.set(index, callElement);

    }

    private void handleInputMapping(Element task, String type) {

        XPath extensionElementsXPATH = DocumentHelper.createXPath(StrUtil.format("./{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName()));

        XPath ioMappingXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                ZEEBE_IO_MAPPING_QNAME.getQualifiedName()));

        XPath inputMappingXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_INPUT_MAPPING_QNAME.getQualifiedName()));

        Element extensionElements = (Element) extensionElementsXPATH.selectSingleNode(task);

        Element ioMapping = (Element) ioMappingXPATH.selectSingleNode(task);

        if(ioMapping == null) {
            ioMapping = DocumentHelper.createElement(ZEEBE_IO_MAPPING_QNAME);
            ioMapping.setParent(extensionElements);
            extensionElements.content().add(ioMapping);
        }

        Element inputMapping = (Element) inputMappingXPATH.selectSingleNode(task);

        ParametersParser parametersParser = new ParametersParser("brandnewdata:input", "name",
                "value", "label",
                "type", "dataType");

        ObjectNode parameters = parametersParser.parse(inputMapping);

        Iterator<Map.Entry<String, JsonNode>> iterator = parameters.fields();
        while(iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String target = entry.getKey();
            // 加上 = 号代表表达式
            String source =  "=" + entry.getValue();
            if(!type.startsWith("com.brandnewdata")) {
                // 通用连接器的参数需要加上input
                target = "inputs." + target;
                Element element = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
                element.addAttribute("target", target);
                element.addAttribute("source", source);
                element.setParent(ioMapping);
                // 新增 zeebe:input
                ioMapping.content().add(element);
            }
        }

        // 移除inputMapping
        extensionElements.remove(inputMapping);

        List<Node> properties = null;

        /*
        if(type.startsWith("com.brandnewdata:database")) {
            properties =  handleDatabaseProperties(task, type);
        }

        if(CollUtil.isNotEmpty(properties)) {
            inputMapping.content().addAll(properties);
        }
        */


    }


    private void handleOutputMapping(Element task, String type) {
        XPath extensionElementsXPATH = DocumentHelper.createXPath(StrUtil.format("./{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName()));

        XPath ioMappingXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                ZEEBE_IO_MAPPING_QNAME.getQualifiedName()));

        XPath outputMappingXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                BRANDNEWDATA_OUTPUT_MAPPING_QNAME.getQualifiedName()));

        Element extensionElements = (Element) extensionElementsXPATH.selectSingleNode(task);

        Element ioMapping = (Element) ioMappingXPATH.selectSingleNode(task);

        if(ioMapping == null) {
            ioMapping = DocumentHelper.createElement(ZEEBE_IO_MAPPING_QNAME);
            ioMapping.setParent(extensionElements);
            extensionElements.content().add(ioMapping);
        }

        Element outputMapping = (Element) outputMappingXPATH.selectSingleNode(task);

        ParametersParser parametersParser = new ParametersParser("brandnewdata:output", "name",
                "value", "label",
                "type", "dataType");

        ObjectNode parameters = parametersParser.parse(outputMapping);

        List<IOMap> ioMaps = parseObjectIOMapList(parameters, null);

        for (IOMap ioMap : ioMaps) {
            Element element = DocumentHelper.createElement(ZEEBE_OUTPUT_QNAME);
            element.addAttribute("target", ioMap.getTarget());
            element.addAttribute("source", ioMap.getSource());
            element.setParent(ioMapping);
            // 新增 zeebe:output
            ioMapping.content().add(element);
        }

        extensionElements.remove(outputMapping);
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


    private void handleCustomIOMapping(Element task, String type) {

    }

    private List<Node> handleHttpProperties(Element task, String type) {
        List<Node> ret = new ArrayList<>();
        /*
        Element properties1 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties1.addAttribute("target", "properties.baseUrl");
        properties1.addAttribute("source", "= &#34;https://api04.aliyun.venuscn.com&#34;");
        ret.add(properties1);
        */

        return ret;
    }

    private List<Node> handleDatabaseProperties(Element task, String type) {
        List<Node> ret = new ArrayList<>();
        /*
          <zeebe:input source="= &#34;mysql&#34;" target="properties.databaseType" />
          <zeebe:input source="= &#34;10.101.53.4&#34;" target="properties.host" />
          <zeebe:input source="= 3306" target="properties.port" />
          <zeebe:input source="= &#34;brand_connector&#34;" target="properties.databaseName" />
          <zeebe:input source="= &#34;root&#34;" target="properties.username" />
          <zeebe:input source="= &#34;Brand@123456&#34;" target="properties.password" />
        * */

        /*
         * dom4j框架会负责转义，直接输入字面量就行
         * */
        Element properties1 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties1.addAttribute("target", "properties.databaseType");
        properties1.addAttribute("source", "= \"mysql\"");
        ret.add(properties1);

        Element properties2 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties2.addAttribute("target", "properties.host");
        properties2.addAttribute("source", "= \"10.101.53.4\"");
        ret.add(properties2);

        Element properties3 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties3.addAttribute("target", "properties.port");
        properties3.addAttribute("source", "= 3306");
        ret.add(properties3);

        Element properties4 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties4.addAttribute("target", "properties.databaseName");
        properties4.addAttribute("source", "= \"brand_connector\"");
        ret.add(properties4);

        Element properties5 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties5.addAttribute("target", "properties.username");
        properties5.addAttribute("source", "= \"root\"");
        ret.add(properties5);

        Element properties6 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties6.addAttribute("target", "properties.password");
        properties6.addAttribute("source", "= \"Brand@123456\"");
        ret.add(properties6);

        return ret;
    }


}
