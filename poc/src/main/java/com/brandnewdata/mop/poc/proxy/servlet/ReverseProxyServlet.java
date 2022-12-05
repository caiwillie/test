package com.brandnewdata.mop.poc.proxy.servlet;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointServerBo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.dto.old.ProcessConfig;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointSceneAService;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIUtils;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@WebServlet(urlPatterns = "/proxy/*", initParams = {
@WebInitParam(name = ProxyServlet.P_TARGET_URI, value = "http://www.brandnewdata.com")})
public class ReverseProxyServlet extends ProxyServlet {
    @Autowired
    private IProcessDeployService deployService;

    private final IProxyAService proxyAService;

    private final IProxyEndpointAService proxyEndpointAService;

    private final IProxyEndpointSceneAService proxyEndpointSceneAService;

    private static final String ATTR_TARGET_URI =
            ProxyServlet.class.getSimpleName() + ".targetUri";

    private static final String ATTR_TARGET_HOST =
            ProxyServlet.class.getSimpleName() + ".targetHost";

    public ReverseProxyServlet(IProcessDeployService deployService,
                               IProxyAService proxyAService,
                               IProxyEndpointAService proxyEndpointAService,
                               IProxyEndpointSceneAService proxyEndpointSceneAService) {
        this.deployService = deployService;
        this.proxyAService = proxyAService;
        this.proxyEndpointAService = proxyEndpointAService;
        this.proxyEndpointSceneAService = proxyEndpointSceneAService;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LocalDateTime startTime = LocalDateTime.now();
        ProxyEndpointCallDto proxyEndpointCallDto = new ProxyEndpointCallDto();
        try {
            /*
             * getRequestUri() 和 getPathInfo() 的区别：
             * https://www.baeldung.com/http-servlet-request-requesturi-pathinfo
             * */
            String uri = request.getPathInfo();

            // http request query
            String queryString = request.getQueryString();

            // http request domain
            String domain = request.getHeader("g2-domain");
            log.info("proxy g2-domain: {}, uri: {}, queryString {}", domain, uri, queryString);

            ProxyDto proxyDto = proxyAService.fetchByDomain(domain);
            Assert.notNull(proxyDto, "domain not found: {}", domain);

            ProxyEndpointDto endpointDto = proxyEndpointAService.fetchByProxyIdAndLocation(proxyDto.getId(), uri);
            Assert.notNull(endpointDto, "path not found: {}", uri);

            Integer backendType = endpointDto.getBackendType();
            String backendConfig = endpointDto.getBackendConfig();

            if(!NumberUtil.equals(backendType, ProxyConst.BACKEND_TYPE__SCENE)
                    && !NumberUtil.equals(backendType, ProxyConst.BACKEND_TYPE__SERVER)) {
                throw new RuntimeException("backend type not support: " + endpointDto.getBackendType());
            }
            // 更新 endpoint call dto
            updateFrom(proxyEndpointCallDto, request);

            if (NumberUtil.equals(endpointDto.getBackendType(), ProxyConst.BACKEND_TYPE__SERVER)) {
                ProxyEndpointServerBo proxyEndpointServerBo = proxyEndpointAService.parseServerConfig(backendConfig);
                forward(request, response, proxyEndpointServerBo);
            } else {
                proxyEndpointSceneAService.parseConfig(backendConfig);
            }

            int status = response.getStatus();


        } catch (Exception e) {
            log.error("ReverseProxyServlet.service error", e);
            proxyEndpointCallDto.setHttpStatus("500");
            proxyEndpointCallDto.setHttpBody(e.getMessage());
        } finally {
            long time = LocalDateTimeUtil.between(startTime, LocalDateTime.now()).toMillis();
            proxyEndpointCallDto.setTimeConsuming((int)time);
        }

    }

    @SneakyThrows
    private void updateFrom(ProxyEndpointCallDto dto, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String httpMethod = request.getMethod();
        if(!StrUtil.equalsAny(httpMethod, Method.POST.name(), Method.GET.name())) {
            throw new RuntimeException("http method not support: " + httpMethod);
        }

        String body = null;
        if (StrUtil.equals(httpMethod, Method.POST.name())) {
            String contentType = request.getHeader("Content-Type");
            Assert.isTrue(StrUtil.equals(contentType, "application/json"), "Content-Type must be application/json");
            body = ServletUtil.getBody(request);
            // reset reader
            request.getReader().reset();
        }

        String queryString = request.getQueryString();

        dto.setUserAgent(userAgent);
        dto.setHttpMethod(httpMethod);
        dto.setHttpQuery(queryString);
        dto.setHttpBody(body);
    }


    private void startProcess(HttpServletRequest request, HttpServletResponse response, ProcessConfig config) {
        String body = ServletUtil.getBody(request);

        String contentType = HttpUtil.getContentTypeByRequestBody(body);
        Map<String, Object> variables = new HashMap<>();

        // 将query参数放入map中
        variables.putAll(ServletUtil.getParamMap(request));

        // 如果content type是JSON，将body参数放入map中
        if(StrUtil.equals(contentType, ContentType.JSON.toString())
                && StrUtil.isNotBlank(body)) {
            // 如果是json类型，并且是object
            variables.putAll(JacksonUtil.fromMap(body));
        }

        String errorMessage = null;
        String processId = config.getProcessId();
        Map<String, Object> result = deployService.startWithResult(processId, variables);
        log.info("response is: {}", JacksonUtil.to(result));
        result = Optional.ofNullable(result).orElse(MapUtil.empty());
        ServletUtil.write(response, JacksonUtil.to(result), "application/json;charset=utf-8");
    }

    @SneakyThrows
    private void forward(HttpServletRequest request, HttpServletResponse response, ProxyEndpointServerBo config) {
        request.setAttribute(ATTR_TARGET_URI, "");
        request.setAttribute(ATTR_TARGET_HOST, URIUtils.extractHost(new URI(config.getBaseUrl())));

        super.service(request, response);
    }

}
