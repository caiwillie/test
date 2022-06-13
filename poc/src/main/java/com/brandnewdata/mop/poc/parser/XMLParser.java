package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastStringWriter;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import org.dom4j.*;
import org.dom4j.io.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caiwillie
 */
public class XMLParser {

    private static final Namespace BPMN_NAMESPACE =
            DocumentHelper.createNamespace(BPMNNamespace.BPMN.getPrefix(), BPMNNamespace.BPMN.getUri());

    private static final Namespace ZEEBE_NAMESPACE =
            DocumentHelper.createNamespace(BPMNNamespace.ZEEBE.getPrefix(), BPMNNamespace.ZEEBE.getUri());
    public static void parse() {
        try (InputStream stream = ResourceUtil.getStream("demo.bpmn")) {
            SAXReader reader = new SAXReader();
            Document document = reader.read(stream);
            treeWalk(document);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }


    public static void treeWalk(Document document) {
        Element root = document.getRootElement();

        // 底层是调用 Namespace.get(), 会全局缓存新创建的nmaespcae
        // root.addNamespace(BPMNNamespace.BPMN.getPrefix(), BPMNNamespace.BPMN.getUri());

        // root.addNamespace(BPMNNamespace.ZEEBE.getPrefix(), BPMNNamespace.ZEEBE.getUri());

        // 在根元素上加上namespace，下面的所有其他元素都可以使用这个Namespace
        root.addNamespace(ZEEBE_NAMESPACE.getPrefix(), ZEEBE_NAMESPACE.getURI());

        treeWalk(root);


        Namespace BPMN2_NAMESPACE = root.getNamespaceForPrefix(BPMNNamespace.BPMN2.getPrefix());
        root.remove(BPMN2_NAMESPACE);

        String result = write(document);
        System.out.println(result);
    }

    private static String write(Document document) {

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

    public static void treeWalk(Element element) {

        // 新建element，源列表可能会被修改
        List<Element> elements = CollUtil.newArrayList(element.elements());

        handleConnector(element);

        // 递归循环
        for (Element e : elements) {
            treeWalk(e);
        }

        // 处理namespace
        handleNamespace(element);
    }

    private static void handleNamespace(Element element) {
        // 替换 namespace
        if (StrUtil.equals(element.getNamespacePrefix(), BPMNNamespace.BPMN2.getPrefix())) {
            // 会逐级向上查找namespace
            element.setQName(QName.get(element.getName(), BPMN_NAMESPACE));
        }
    }

    private static void handleConnector(Element element) {
        if(!StrUtil.equals(element.getName(), "serviceTask")) {
            // 不是service task，直接跳过
            return;
        }

        List<Element> elements = element.elements();
        String id = element.attributeValue("id");

        if(StrUtil.equals(id, "Activity_0fnu1xs")) {
            QName taskDefinition = DocumentHelper.createQName("taskDefinition", ZEEBE_NAMESPACE);
            Element e = DocumentHelper.createElement(taskDefinition);

            elements.add(0, e);
        }


        return;
    }


    public static void transfer() throws Exception {
        // load the transformer using JAXP
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(new StreamSource());

        // now lets style the given document
        DocumentSource source = new DocumentSource(null);
        DocumentResult result = new DocumentResult();
        transformer.transform(source, result);

        // return the transformed document
        Document transformedDoc = result.getDocument();
        return;
    }


}
