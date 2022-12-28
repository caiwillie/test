package com.brandnewdata.mop.poc.proxy.servlet;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.Method;
import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.constant.ProxyConst;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointSceneBo;
import com.brandnewdata.mop.poc.proxy.bo.ProxyEndpointServerBo;
import com.brandnewdata.mop.poc.proxy.dto.ProxyDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointCallDto;
import com.brandnewdata.mop.poc.proxy.dto.ProxyEndpointDto;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointCallAService;
import com.brandnewdata.mop.poc.proxy.service.atomic.IProxyEndpointSceneAService;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import com.brandnewdata.mop.poc.scene.service.atomic.IVersionProcessAService;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIUtils;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

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

@Slf4j
@WebServlet(urlPatterns = "/proxy/*", initParams = {
@WebInitParam(name = ProxyServlet.P_TARGET_URI, value = "http://www.brandnewdata.com")})
public class ReverseProxyServlet extends ProxyServlet {
    private final IProxyAService proxyAService;

    private final IProxyEndpointAService proxyEndpointAService;

    private final IProxyEndpointSceneAService proxyEndpointSceneAService;

    private final IProxyEndpointCallAService proxyEndpointCallAService;

    private final IProcessDeployService deployService;

    private final IVersionProcessAService versionProcessAService;

    private static final String ATTR_TARGET_URI =
            ProxyServlet.class.getSimpleName() + ".targetUri";

    private static final String ATTR_TARGET_HOST =
            ProxyServlet.class.getSimpleName() + ".targetHost";

    public ReverseProxyServlet(IProxyAService proxyAService,
                               IProxyEndpointAService proxyEndpointAService,
                               IProxyEndpointSceneAService proxyEndpointSceneAService,
                               IProxyEndpointCallAService proxyEndpointCallAService,
                               IProcessDeployService deployService,
                               IVersionProcessAService versionProcessAService) {
        this.proxyAService = proxyAService;
        this.proxyEndpointAService = proxyEndpointAService;
        this.proxyEndpointSceneAService = proxyEndpointSceneAService;
        this.proxyEndpointCallAService = proxyEndpointCallAService;
        this.deployService = deployService;
        this.versionProcessAService = versionProcessAService;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ProxyEndpointDto endpointDto = null;
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
            Assert.notNull(domain,"domain not exist");
            log.info("proxy g2-domain: {}, uri: {}, queryString {}", domain, uri, queryString);

            ProxyDto proxyDto = proxyAService.fetchByDomain(domain);
            Assert.notNull(proxyDto, "domain not found: {}", domain);

            endpointDto = proxyEndpointAService.fetchByProxyIdAndLocation(proxyDto.getId(), uri);
            Assert.notNull(endpointDto, "path not found: {}", uri);
        } catch(Exception e) {
            String errorMessage = e.getMessage();
            if(StrUtil.isBlank(errorMessage)) {
                errorMessage = e.toString();
            }
            ServletUtil.write(response, errorMessage, ContentType.TEXT_PLAIN.getValue());
            return;
        }

        RepeatReadHttpRequest repeatReadHttpRequest = new RepeatReadHttpRequest(request);
        ProxyEndpointCallDto proxyEndpointCallDto = new ProxyEndpointCallDto();
        proxyEndpointCallDto.setEndpointId(endpointDto.getId());
        // 更新 endpoint call dto
        updateProxyEndpointCallDto(proxyEndpointCallDto, repeatReadHttpRequest);

        try {
            Integer backendType = endpointDto.getBackendType();
            String backendConfig = endpointDto.getBackendConfig();

            if(!NumberUtil.equals(backendType, ProxyConst.BACKEND_TYPE__SCENE)
                    && !NumberUtil.equals(backendType, ProxyConst.BACKEND_TYPE__SERVER)) {
                throw new RuntimeException("backend type not support: " + endpointDto.getBackendType());
            }

            if (NumberUtil.equals(endpointDto.getBackendType(), ProxyConst.BACKEND_TYPE__SERVER)) {
                ProxyEndpointServerBo config = proxyEndpointAService.parseServerConfig(backendConfig);
                forward(repeatReadHttpRequest, response, config);
            } else {
                ProxyEndpointSceneBo config = proxyEndpointSceneAService.parseConfig(backendConfig);
                callProcess(repeatReadHttpRequest, response, config);
            }

            proxyEndpointCallDto.setExecuteStatus("success");
        } catch (Exception e) {
            log.error("ReverseProxyServlet.service error", e);
            proxyEndpointCallDto.setExecuteStatus(ProxyConst.CALL_EXECUTE_STATUS__FAIL);
            response.setStatus(HttpStatus.HTTP_INTERNAL_ERROR);
            String errorMessage = e.getMessage();
            if(StrUtil.isBlank(errorMessage)) {
                errorMessage = e.toString();
            }
            proxyEndpointCallDto.setErrorMessage(e.getMessage());
            ServletUtil.write(response, errorMessage, ContentType.TEXT_PLAIN.getValue());
        } finally {
            if (proxyEndpointCallDto.getEndpointId() != null) {
                long time = LocalDateTimeUtil.between(proxyEndpointCallDto.getStartTime(), LocalDateTime.now()).toMillis();
                proxyEndpointCallDto.setTimeConsuming((int)time);
                proxyEndpointCallAService.save(proxyEndpointCallDto);
            }
        }

    }

    @SneakyThrows
    private void updateProxyEndpointCallDto(ProxyEndpointCallDto dto, HttpServletRequest request) {
        LocalDateTime startTime = LocalDateTime.now();
        String httpMethod = request.getMethod();
        dto.setStartTime(startTime);
        String clientIp = ServletUtil.getClientIP(request);
        dto.setIpAddress(clientIp);
        dto.setHttpMethod(httpMethod);
        dto.setUserAgent(request.getHeader("User-Agent"));
        dto.setRequestQuery(request.getQueryString());
        if(!StrUtil.equalsAny(httpMethod, Method.POST.name(), Method.GET.name())) {
            throw new RuntimeException("http method not support: " + httpMethod);
        }

        String body = null;
        if (StrUtil.equals(httpMethod, Method.POST.name())) {
            String contentType = request.getHeader("Content-Type");
            Assert.isTrue(StrUtil.equals(contentType, "application/json"), "Content-Type must be application/json");
            body = ServletUtil.getBody(request);
        }
        dto.setRequestBody(body);
    }

    private void callProcess(HttpServletRequest request, HttpServletResponse response, ProxyEndpointSceneBo config) {
        Map<String, Object> variables = getVariables(request);

        Long envId = config.getEnvId();
        String processId = config.getProcessId();
        String processName = config.getProcessName();
        VersionProcessDto versionProcessDto = versionProcessAService.fetchOneByProcessId(ListUtil.of(processId)).get(processId);
        Assert.notNull(versionProcessDto, "process not found: {}", processId);
        String processXml = versionProcessDto.getProcessXml();

        BpmnXmlDto bpmnXmlDto = new BpmnXmlDto(processId, processName, processXml);

        Map<String, Object> result = deployService.startSync(bpmnXmlDto, variables, envId, ProcessConst.PROCESS_BIZ_TYPE__SCENE);
        ServletUtil.write(response, JacksonUtil.to(result), "application/json;charset=utf-8");
    }

    private Map<String, Object> getVariables(HttpServletRequest request) {
        Map<String, Object> ret = new HashMap<>();
        Map<String, String> paramMap = ServletUtil.getParamMap(request);
        if(CollUtil.isNotEmpty(paramMap)) ret.putAll(paramMap);

        if (StrUtil.equals(request.getMethod(), Method.POST.name())) {
            String contentType = request.getHeader("Content-Type");
            Assert.isTrue(StrUtil.equals(contentType, "application/json"), "Content-Type must be application/json");
            String body = ServletUtil.getBody(request);
            Map<String, Object> bodyMap = JacksonUtil.fromMap(body);
            if(CollUtil.isNotEmpty(bodyMap)) ret.putAll(bodyMap);
        }
        return ret;
    }

    @SneakyThrows
    private void forward(HttpServletRequest request, HttpServletResponse response, ProxyEndpointServerBo config) {
        request.setAttribute(ATTR_TARGET_URI, "");
        request.setAttribute(ATTR_TARGET_HOST, URIUtils.extractHost(new URI(config.getBaseUrl())));

        super.service(request, response);
    }

}
