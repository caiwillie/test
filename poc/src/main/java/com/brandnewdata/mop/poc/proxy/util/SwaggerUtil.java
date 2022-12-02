package com.brandnewdata.mop.poc.proxy.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.proxy.dto.old.Endpoint;
import com.brandnewdata.mop.poc.proxy.dto.old.ImportDTO;
import com.brandnewdata.mop.poc.proxy.dto.old.Proxy;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class SwaggerUtil {

    public static ImportDTO parse(String content) {
        if(StrUtil.isEmpty(content)) {
            throw new RuntimeException("文件内容不能为空");
        }
        SwaggerParseResult parseResult = new OpenAPIParser().readContents(content, null, null);
        if (CollUtil.isNotEmpty(parseResult.getMessages())) {
            // 解析异常
            String messages = StrUtil.join(",", parseResult.getMessages());
            String errorMessage = StrUtil.format("[openapi 文件解析异常], 错误信息：{}", messages);
            log.warn(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        // the parsed POJO
        OpenAPI openAPI = parseResult.getOpenAPI();
        if(openAPI == null) {
            throw new RuntimeException("openapi 文件为空");
        }

        ImportDTO ret = new ImportDTO();

        // 获取 proxy
        Info info = openAPI.getInfo();
        Proxy proxy = new Proxy();
        proxy.setName(info.getTitle());
        proxy.setVersion(info.getVersion());
        proxy.setDescription(info.getDescription());

        // 获取endpoint list
        List<Endpoint> endpoints = new ArrayList<>();
        Paths paths = openAPI.getPaths();
        if(CollUtil.isNotEmpty(paths)) {
            for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
                String location = entry.getKey();
                Endpoint endpoint = new Endpoint();
                endpoint.setLocation(location);
                endpoints.add(endpoint);
            }
        }

        ret.setProxy(proxy);
        ret.setEndpointList(endpoints);

        return ret;
    }

    public static void main(String[] args) {
        String content = ResourceUtil.readUtf8Str("case.yaml");
        SwaggerUtil.parse(content);
    }

}
