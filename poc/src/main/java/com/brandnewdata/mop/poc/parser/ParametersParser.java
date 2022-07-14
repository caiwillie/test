package com.brandnewdata.mop.poc.parser;

import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.common.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
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

        boolean isEmpty = true;
        List<Element> elements = root.elements();
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if(!StrUtil.equals(element.getQualifiedName(), qName)) {
                // 如果 qName 没匹配上，就直接跳过
                continue;
            }
            String name = element.attributeValue(nameAttribute);
            Object value = parseEach(element);
            if(value != null) {
                if(isEmpty) isEmpty = false;
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
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if(!StrUtil.equals(element.getQualifiedName(), qName)) {
                // 如果 qName 没匹配上，就直接跳过
                continue;
            }
            Object value = parseEach(element);
            ret.addPOJO(value);
        }

        return ret;
    }

}
