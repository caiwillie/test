package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.service.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static com.brandnewdata.mop.poc.parser.XMLConstants.*;

/**
 * @author caiwillie
 */
@Slf4j
public class XMLParser2 {

    private String modelKey;

    private String name;

    public XMLParser2 (String modelKey, String name) {
        this.modelKey = modelKey;
        this.name = name;
    }

    public XMLParser2() {

    }

    public XMLDTO parse(String xml) {
        XMLDTO ret = new XMLDTO();
        Document document;
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

        // 处理出入参数映射
        handleIOMapping(element, type);

        return;
    }

    private String handleTaskDefinition(Element task) {

        XPath taskDefinitionXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(), ZEEBE_TASK_DEFINITION_QNAME.getQualifiedName()));

        Node node = taskDefinitionXPATH.selectSingleNode(task);

        if(!(node instanceof Element)) {
            throw new IllegalArgumentException(StrUtil.format("服务任务 {} 下未定义任务类型", task.getName()));
        }

        Element taskDefinition = (Element) node;

        String type = taskDefinition.attributeValue("type");
        Assert.notBlank(type, "服务任务 {} 下未定义任务类型", task.getName());

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
        ((Element) node).attributeValue("type");

        List<Node> parent = node.getParent().content();

        int index = parent.indexOf(node);

        // 创建 call element，并设置 processId = type
        Element callElement = DocumentHelper.createElement(ZEEBE_CALLED_ELEMENT_QNAME);
        String processId = ServiceUtil.convertModelKey(type);
        callElement.addAttribute("processId", processId);
        callElement.addAttribute("propagateAllChildVariables", "false");

        parent.set(index, callElement);

    }

    private void handleIOMapping(Element task, String type) {
        if(!type.startsWith("com.brandnewdata")) {
            return;
        }


        XPath ioMappingXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                ZEEBE_IO_MAPPING_QNAME.getQualifiedName()));

        XPath inputXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(),
                ZEEBE_IO_MAPPING_QNAME.getQualifiedName(),
                ZEEBE_INPUT_QNAME.getQualifiedName()));

        Element ioMapping = (Element) ioMappingXPATH.selectSingleNode(task);

        if(StrUtil.containsAny(type, "http", "datasource")) {
            // 只有当 http 和 dataaSource 时才能够处理inputs
            List<Node> nodes = inputXPATH.selectNodes(task);

            if(CollUtil.isNotEmpty(nodes)) {
                for (Node node : nodes) {
                    if(!(node instanceof Element)) {
                        throw new IllegalArgumentException(StrUtil.format("服务任务 {} 下入参配置有误", task.getName()));
                    }
                    Element input = (Element) node;
                    String target = input.attributeValue("target");
                    target = "inputs." + target;
                    input.addAttribute("target", target);
                }
            }
        }

        List<Node> properties = null;

        if(type.startsWith("com.brandnewdata:database")) {
            properties =  handleDatabaseProperties(task, type);
        }

        if(CollUtil.isNotEmpty(properties)) {
            ioMapping.content().addAll(properties);
        }


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

        Element properties1 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties1.addAttribute("target", "properties.databaseType");
        properties1.addAttribute("source", "= &#34;mysql&#34;");
        ret.add(properties1);


        Element properties2 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties2.addAttribute("target", "properties.host");
        properties2.addAttribute("source", "= &#34;10.101.53.4&#34;");
        ret.add(properties2);


        Element properties3 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties3.addAttribute("target", "properties.port");
        properties3.addAttribute("source", "= 3306");
        ret.add(properties3);

        Element properties4 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties4.addAttribute("target", "properties.databaseName");
        properties4.addAttribute("source", "= &#34;brand_connector&#34;");
        ret.add(properties4);

        Element properties5 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties5.addAttribute("target", "properties.username");
        properties5.addAttribute("source", "= &#34;root&#34;");
        ret.add(properties5);

        Element properties6 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        properties6.addAttribute("target", "properties.password");
        properties6.addAttribute("source", "= &#34;Brand@123456&#34;");
        ret.add(properties6);

        return ret;
    }

}
