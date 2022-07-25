package com.brandnewdata.mop.poc.modeler.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.modeler.dto.ProcessDefinition;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static com.brandnewdata.mop.poc.modeler.parser.constants.AttributeConstants.ID;
import static com.brandnewdata.mop.poc.modeler.parser.constants.AttributeConstants.NAME;
import static com.brandnewdata.mop.poc.modeler.parser.constants.NamespaceConstants.BPMN_NAMESPACE;
import static com.brandnewdata.mop.poc.modeler.parser.constants.QNameConstants.BPMN_DEFINITIONS_QNAME;
import static com.brandnewdata.mop.poc.modeler.parser.constants.QNameConstants.BPMN_PROCESS_QNAME;

@Slf4j
public class ProcessDefinitionParser implements ProcessDefinitionParseStep1 {

    private ProcessDefinition processDefinition;

    private Document oldDocument;

    private String processId;

    private String name;

    private Document document;

    // 工厂模式
    private ProcessDefinitionParser(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        init();
    }

    public static ProcessDefinitionParseStep1 newInstance(ProcessDefinition processDefinition) {
        return new ProcessDefinitionParser(processDefinition);
    }

    private void init() {
        this.oldDocument = readDocument(processDefinition.getXml());
        this.document = oldDocument;
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
        XPath path = DocumentHelper.createXPath("//bpmn2:*");
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
        Namespace bpmn2 = root.getNamespaceForPrefix("bpmn2");
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
        XPath path = DocumentHelper.createXPath(StrUtil.join("/",
                BPMN_DEFINITIONS_QNAME.getQualifiedName(),
                BPMN_PROCESS_QNAME.getQualifiedName()));

        Element node = (Element) path.selectSingleNode(document);
        processId = node.attributeValue(ID);
        name = node.attributeValue(NAME);
    }

    @Override
    public ProcessDefinition build() {
        String original = serialize(oldDocument);
        String current = serialize(document);
        String TEMPLATE =
                "\n======================= 转换前 xml =======================\n {}" +
                "\n======================= 转换后 xml =======================\n {}";
        log.info(StrUtil.format(TEMPLATE, original, current));
        ProcessDefinition ret = new ProcessDefinition();
        ret.setProcessId(processId);
        ret.setName(name);
        ret.setXml(current);
        return ret;
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
}
