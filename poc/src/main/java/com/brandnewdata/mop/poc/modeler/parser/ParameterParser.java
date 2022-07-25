package com.brandnewdata.mop.poc.modeler.parser;

import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.common.Constants;
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

    public String qName;

    private final String nameAttribute;

    private final String valueAttribute;

    private final String labelAttribute;

    private final String typeAttribute;

    private final String innerTypeAttribute;

    public ParameterParser(String qName,
                            String nameAttribute,
                            String valueAttribute,
                            String labelAttribute,
                            String typeAttribute,
                            String innerTypeAttribute) {
        this.qName = qName;
        this.nameAttribute = nameAttribute;
        this.valueAttribute = valueAttribute;
        this.labelAttribute = labelAttribute;
        this.typeAttribute = typeAttribute;
        this.innerTypeAttribute = innerTypeAttribute;
    }


    public ObjectNode parse(Element root) {
        return parseStruct(root);
    }

    private Object parseEach(Element root) {
        Object ret = null;
        String type = root.attributeValue(typeAttribute);
        if(StrUtil.equals(type, Constants.TYPE_STRUCT)) {
            ret = parseStruct(root);
        } else if (StrUtil.equals(type, Constants.TYPE_LIST)) {
            ret = parseList(root);
        } else if (StrUtil.equals(type, Constants.TYPE_DICT)) {

        } else if (StrUtil.equals(type, Constants.TYPE_ENUM)) {

        } else {
            String value = root.attributeValue(valueAttribute);
            if(StrUtil.isNotBlank(value)) {
                // 基础类型直接放入的是 rawValue
                ret = new RawValue(value);
            }
        }

        return ret;
    }

    private ObjectNode parseStruct(Element root) {
        ObjectNode ret = OM.createObjectNode();
        if(root == null ) {
            return null;
        }

        List<Element> elements = root.elements();
        for (Element element : elements) {
            if (!StrUtil.equals(element.getQualifiedName(), qName)) {
                // 如果 qName 没匹配上，就直接跳过
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

    private ArrayNode parseList(Element root) {
        ArrayNode ret = OM.createArrayNode();
        if(root == null) {
            return null;
        }

        List<Element> elements = root.elements();
        for (Element element : elements) {
            if (!StrUtil.equals(element.getQualifiedName(), qName)) {
                // 如果 qName 没匹配上，就直接跳过
                continue;
            }
            Object value = parseEach(element);
            ret.addPOJO(value);
        }

        return ret;
    }
}
