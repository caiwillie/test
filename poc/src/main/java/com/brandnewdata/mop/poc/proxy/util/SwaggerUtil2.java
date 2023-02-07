package com.brandnewdata.mop.poc.proxy.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.proxy.dto.ImportDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
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
public class SwaggerUtil2 {

    public static ImportDto parse(String content) {
        ImportDto ret = new ImportDto();
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
        OpenAPI openApi = parseResult.getOpenAPI();
        if(openApi == null) {
            throw new RuntimeException("openapi 文件为空");
        }

        // 获取 proxy
        Info info = openApi.getInfo();
        ProxyDto proxyDto = new ProxyDto();
        proxyDto.setName(info.getTitle());
        proxyDto.setVersion(info.getVersion());
        proxyDto.setDescription(info.getDescription());

        // 获取endpoint list
        List<ProxyEndpointDto> endpoints = new ArrayList<>();
        Paths paths = openApi.getPaths();
        if(CollUtil.isNotEmpty(paths)) {
            for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
                String location = entry.getKey();
                ProxyEndpointDto endpoint = new ProxyEndpointDto();
                endpoint.setLocation(location);
                endpoints.add(endpoint);
            }
        }

        ret.setProxy(proxyDto);
        ret.setEndpointList(endpoints);

        return ret;
    }

}
