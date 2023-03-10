package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import org.dom4j.Element;

import java.util.List;

public class ParameterParser {
    /**
     * string
     * int
     * float
     * boolean
     * datetime
     * date
     * time
     * decimal
     *
     * struct
     * dict
     * list
     * enum
     */

    private static final ObjectMapper OM = new ObjectMapper();

    private final String qName;

    private final String nameAttribute;

    private final String expressionAttribute;

    private final String labelAttribute;

    private final String typeAttribute;

    private final String innerTypeAttribute;

    public ParameterParser(String qName,
                            String nameAttribute,
                            String expressionAttribute,
                            String labelAttribute,
                            String typeAttribute,
                            String innerTypeAttribute) {
        this.qName = qName;
        this.nameAttribute = nameAttribute;
        this.expressionAttribute = expressionAttribute;
        this.labelAttribute = labelAttribute;
        this.typeAttribute = typeAttribute;
        this.innerTypeAttribute = innerTypeAttribute;
    }


    public ObjectNode parse(Element root) {
        return (ObjectNode) parseStruct(root);
    }

    private Object parseEach(Element root) {
        Object ret = null;
        String type = root.attributeValue(typeAttribute);
        if(StrUtil.equals(type, ProcessConst.TYPE_STRUCT)) {
            ret = parseStruct(root);
        } else if (StrUtil.equals(type, ProcessConst.TYPE_LIST)) {
            ret = parseList(root);
        } else if (StrUtil.equals(type, ProcessConst.TYPE_DICT)) {
            ret = parseStruct(root);
        } else if (StrUtil.equals(type, ProcessConst.TYPE_ENUM)) {
            ret = parseList(root);
        } else {
            String value = root.attributeValue(expressionAttribute);
            if(StrUtil.isNotBlank(value)) {
                // ?????????????????????????????? rawValue
                ret = new RawValue(value);
            }
        }

        return ret;
    }

    private Object parseStruct(Element root) {
        if(root == null ) {
            return null;
        }
        String expression = root.attributeValue(expressionAttribute);
        if(StrUtil.isNotBlank(expression)) {
            return new RawValue(expression);
        }
        ObjectNode ret = OM.createObjectNode();

        List<Element> elements = root.elements();
        for (Element element : elements) {
            if (!StrUtil.equals(element.getQualifiedName(), qName)) {
                // ?????? qName ??????????????????????????????
                continue;
            }
            String name = element.attributeValue(nameAttribute);
            Object value = parseEach(element);
            if (value != null) {
                ret.putPOJO(name, value);
            }
        }
        return ret;
    }

    private Object parseList(Element root) {
        if(root == null) {
            return null;
        }
        String expression = root.attributeValue(expressionAttribute);
        if(StrUtil.isNotBlank(expression)) {
            return new RawValue(expression);
        }
        ArrayNode ret = OM.createArrayNode();

        List<Element> elements = root.elements();
        for (Element element : elements) {
            if (!StrUtil.equals(element.getQualifiedName(), qName)) {
                // ?????? qName ??????????????????????????????
                continue;
            }
            Object value = parseEach(element);
            ret.addPOJO(value);
        }

        return ret;
    }
}
