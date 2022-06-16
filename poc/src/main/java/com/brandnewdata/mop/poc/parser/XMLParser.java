package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caiwillie
 */
@Slf4j
public class XMLParser {

    private String modelKey;

    private String name;

    private static final Namespace BPMN_NAMESPACE =
            DocumentHelper.createNamespace(BPMNNamespace.BPMN.getPrefix(), BPMNNamespace.BPMN.getUri());

    private static final Namespace BRANDNEWDATA_NAMESPACE =
            DocumentHelper.createNamespace(BPMNNamespace.BRANDNEWDATA.getPrefix(), BPMNNamespace.BRANDNEWDATA.getUri());

    private static final Namespace ZEEBE_NAMESPACE =
            DocumentHelper.createNamespace(BPMNNamespace.ZEEBE.getPrefix(), BPMNNamespace.ZEEBE.getUri());

    private static QName BPMN_PROCESS_QNAME = DocumentHelper.createQName("process", BPMN_NAMESPACE);
    private static QName BPMN_TASK_QNAME = DocumentHelper.createQName("task", BPMN_NAMESPACE);

    private static QName BPMN_SERVICE_TASK_QNAME = DocumentHelper.createQName("serviceTask", BPMN_NAMESPACE);

    private static QName BPMN_EXTENSION_ELEMENTS_QNAME = DocumentHelper.createQName("extensionElements", BPMN_NAMESPACE);

    private static QName BRANDNEWDATA_TASK_DEFINITION_QNAME = DocumentHelper.createQName("taskDefinition", BRANDNEWDATA_NAMESPACE);
    private static QName BRANDNEWDATA_INPUT_QNAME = DocumentHelper.createQName("input", BRANDNEWDATA_NAMESPACE);

    private static QName BRANDNEWDATA_OUTPUT_QNAME = DocumentHelper.createQName("output", BRANDNEWDATA_NAMESPACE);

    private static QName BRANDNEWDATA_EXTENSION_QNAME = DocumentHelper.createQName("extension", BRANDNEWDATA_NAMESPACE);
    private static QName ZEEBE_IO_MAPPING_QNAME = DocumentHelper.createQName("ioMapping", ZEEBE_NAMESPACE);
    private static QName ZEEBE_INPUT_QNAME = DocumentHelper.createQName("input", ZEEBE_NAMESPACE);

    private static QName ZEEBE_OUTPUT_QNAME = DocumentHelper.createQName("output", ZEEBE_NAMESPACE);

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
        if(modelKey != null || name != null) {
            return;
        }
        QName qName = element.getQName();
        if(!StrUtil.equals(element.getQualifiedName(), BPMN_PROCESS_QNAME.getQualifiedName())) {
            // 不是 bpmn:process，直接跳过
            return;
        }

        modelKey = element.attributeValue("id");
        Assert.notBlank(modelKey, "BPMN解析错误：模型标识不能为空");

        name = element.attributeValue("name");
        Assert.notBlank(name,"BPMN解析错误：模型名称不能为空");

        element.attribute("isExecutable").setValue("true");

    }

    private void handleConnector(Element element) {

        if(!(StrUtil.equals(element.getQualifiedName(), BPMN_TASK_QNAME.getQualifiedName())
            && StrUtil.isNotBlank(element.attributeValue(BRANDNEWDATA_EXTENSION_QNAME)))) {
            // task 并且 brandnewdata:extension 不为空，计算为false, 就跳过
            return;
        }

        element.setQName(BPMN_SERVICE_TASK_QNAME);

        // 处理任务定义
        String type = handleTaskDefinition(element);

        // 处理出入参数映射
        handleIOMapping(element, type);

        return;
    }

    private String handleTaskDefinition(Element task) {

        XPath taskDefinitionXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(), BRANDNEWDATA_TASK_DEFINITION_QNAME.getQualifiedName()));

        Node $taskDefinition = taskDefinitionXPATH.selectSingleNode(task);

        if(!($taskDefinition instanceof Element)) {
            throw new IllegalArgumentException(StrUtil.format("服务任务 {} 下未定义任务类型", task.getName()));
        }

        Element taskDefinition = (Element) $taskDefinition;

        String type = taskDefinition.attributeValue("type");
        Assert.notBlank(type, "服务任务 {} 下未定义任务类型", task.getName());

        // 转换并且解析成zeebe
        Element taskDefinitionNew = DocumentHelper.createElement(
                DocumentHelper.createQName("taskDefinition", ZEEBE_NAMESPACE));

        taskDefinitionNew.addAttribute("type", type);

        Element parent = taskDefinition.getParent();

        int index = parent.indexOf(taskDefinition);
        parent.content().set(index, taskDefinitionNew);
        return type;
    }

    private void handleIOMapping(Element task, String type) {

        XPath inputXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(), BRANDNEWDATA_INPUT_QNAME.getQualifiedName()));

        XPath outputXPATH = DocumentHelper.createXPath(StrUtil.format("./{}/{}",
                BPMN_EXTENSION_ELEMENTS_QNAME.getQualifiedName(), BRANDNEWDATA_OUTPUT_QNAME.getQualifiedName()));

        // 父级节点的容器树
        List<Node> content = null;

        Element input = null;
        Element output = null;
        if(inputXPATH.selectSingleNode(task) instanceof Element
                || outputXPATH.selectSingleNode(task) instanceof Element) {
            input = (Element) inputXPATH.selectSingleNode(task);
            output = (Element) outputXPATH.selectSingleNode(task);
            content = input != null ? input.getParent().content() : output.getParent().content();
        } else {
            // input 和 output 都不存在
            return;
        }

        Element ioMapping = DocumentHelper.createElement(ZEEBE_IO_MAPPING_QNAME);


        content.add(ioMapping);

        List<Node> zeebeInputs = new ArrayList<>();
        List<Node> zeebeOutputs = new ArrayList<>();

        // 处理每一个不同任务的出入参数
        if(StrUtil.equals(type, "mail-service")) {
            zeebeInputs = handleSendMailInput(input);
            zeebeOutputs = handleSendMailOutput(output);
        } else if (StrUtil.equals(type, "sms-service")) {
            zeebeInputs = handleSendSMSInput(input);
        }

        // 目前这版定义只有一个输入
        content.remove(input);

        List<Node> ioMappingContent = new ArrayList<>();
        if(CollUtil.isNotEmpty(zeebeInputs)) {
            ioMappingContent.addAll(zeebeInputs);
        }
        if(CollUtil.isNotEmpty(zeebeOutputs)) {
            ioMappingContent.addAll(zeebeOutputs);
        }
        ioMapping.setContent(ioMappingContent);

    }


    private List<Node> handleSendMailInput(Element input) {
        // 适配成具体的zeebe任务
        List<Node> elements = new ArrayList<>();
        // 出餐
        String receiver = input.attributeValue("receive");
        Assert.notBlank(receiver, "邮件接收人不能为空");

        String message = input.attributeValue("content");
        Assert.notBlank(message, "邮件内容不能为空");

        Element input1 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        input1.addAttribute("target", "toMail");
        input1.addAttribute("source", receiver);

        Element input2 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        input2.addAttribute("target", "content");
        input2.addAttribute("source", message);

        elements.add(input1);
        elements.add(input2);
        return elements;
    }

    private List<Node> handleSendSMSInput(Element input) {
        // 适配成具体的zeebe任务
        List<Node> elements = new ArrayList<>();
        // 出餐
        String receiver = input.attributeValue("receive");
        Assert.notBlank(receiver, "短信接收人不能为空");

        String message = input.attributeValue("content");
        Assert.notBlank(message, "短信内容不能为空");

        Element input1 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        input1.addAttribute("target", "phone");
        input1.addAttribute("source", receiver);

        Element input2 = DocumentHelper.createElement(ZEEBE_INPUT_QNAME);
        input2.addAttribute("target", "name");
        input2.addAttribute("source", message);

        elements.add(input1);
        elements.add(input2);
        return elements;
    }

    private List<Node> handleSendMailOutput(Element output) {
        List<Node> outputs = new ArrayList<>();
        Element output1 = DocumentHelper.createElement(ZEEBE_OUTPUT_QNAME);
        output1.addAttribute("target", "email_result");
        output1.addAttribute("source", "= mail_result");

        outputs.add(output1);

        return outputs;

    }



}
