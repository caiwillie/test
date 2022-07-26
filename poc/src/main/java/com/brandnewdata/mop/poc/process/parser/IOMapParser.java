package com.brandnewdata.mop.poc.process.parser;

import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.parser.IOMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.util.RawValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IOMapParser {

    public List<IOMap> parse(ObjectNode parameters) {
        return parseObjectIOMapList(parameters, null);
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

}
