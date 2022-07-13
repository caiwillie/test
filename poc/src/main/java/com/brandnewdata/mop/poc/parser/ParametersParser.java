package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dom4j.Element;

import java.util.List;

public class ParametersParser {

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

    private String nameAttribute;

    private String valueAttribute;

    private String labelAttribute;

    private String typeAttribute;

    private String innerTypeAttribute;

    public ParametersParser(String qName,
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


    private JsonNode parse(Element root) {
        return parseStruct(root);
    }



    private JsonNode parseEach(Element root) {
        JsonNode ret = null;
        String type = root.attributeValue(typeAttribute);
        if(StrUtil.equals(type, Constants.TYPE_STRUCT)) {
            ret = parseStruct(root);
        } else if (StrUtil.equals(type, Constants.TYPE_LIST)) {
            ret = parseList(root);
        } else if (StrUtil.equals(type, Constants.TYPE_DICT)) {

        } else if (StrUtil.equals(type, Constants.TYPE_ENUM)) {

        } else {
            String value = root.attributeValue(valueAttribute);
            ret = OM.convertValue(value, JsonNode.class);
        }

        return ret;
    }

    private JsonNode parseStruct(Element root) {
        ObjectNode ret = OM.createObjectNode();
        List<Element> elements = root.elements();
        if(CollUtil.isEmpty(elements)) {
            return ret;
        }

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if(!StrUtil.equals(element.getQualifiedName(), qName)) {
                // 如果 qName 没匹配上，就直接跳过
                continue;
            }
            String name = root.attributeValue(nameAttribute);
            JsonNode value = parseEach(element);
            ret.set(name, value);
        }
        return ret;
    }

    private JsonNode parseList(Element root) {
        ArrayNode ret = OM.createArrayNode();
        List<Element> elements = root.elements();
        if(CollUtil.isEmpty(elements)) {
            return ret;
        }

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if(!StrUtil.equals(element.getQualifiedName(), qName)) {
                // 如果 qName 没匹配上，就直接跳过
                continue;
            }
            JsonNode value = parseEach(element);
            ret.add(value);
        }

        return ret;
    }

}
