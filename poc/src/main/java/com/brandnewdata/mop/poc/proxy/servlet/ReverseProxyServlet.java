package com.brandnewdata.mop.poc.proxy.servlet;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.proxy.dto.Backend;
import com.brandnewdata.mop.poc.proxy.dto.ForwardConfig;
import com.brandnewdata.mop.poc.proxy.dto.ProcessConfig;
import com.brandnewdata.mop.poc.proxy.service.BackendService;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIUtils;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MimeTypeUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@WebServlet(urlPatterns = "/proxy/*", initParams = {
@WebInitParam(name = ProxyServlet.P_TARGET_URI, value = "http://www.brandnewdata.com")})
public class ReverseProxyServlet extends ProxyServlet {
    @Autowired
    private IProcessDeployService deployService;

    @Autowired
    private BackendService backendService;

    private static final String ATTR_TARGET_URI =
            ProxyServlet.class.getSimpleName() + ".targetUri";

    private static final String ATTR_TARGET_HOST =
            ProxyServlet.class.getSimpleName() + ".targetHost";

    public ReverseProxyServlet(IProcessDeployService deployService, BackendService backendService) {
        this.deployService = deployService;
        this.backendService = backendService;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /*
        * getRequestUri() 和 getPathInfo() 的区别：
        * https://www.baeldung.com/http-servlet-request-requesturi-pathinfo
        * */

        String uri = req.getPathInfo();

        String queryString = req.getQueryString();
        // 去掉前缀
        String domain = req.getHeader("g2-domain");
        log.info("api proxy g2-domain: {}, uri: {}, queryString {}", domain, uri, queryString);

        Backend backend = backendService.getBackend(domain, uri);

        if (backend == null) {
            // 后端配置未找到，直接返回
            ServletUtil.write(resp, "API Not Found", MimeTypeUtils.TEXT_PLAIN_VALUE);
            return;
        }

        // 不同类型，不同处理
        if (backend.getType() == 1) {
            // 代理启动流程
            ProcessConfig config = (ProcessConfig) backend.getData();
            startProcess(req, resp, config);
        } else if (backend.getType() == 2) {
            // 代理第三方
            ForwardConfig config = (ForwardConfig) backend.getData();
            forward(req, resp, config);
        }
    }

    private void startProcess(HttpServletRequest req, HttpServletResponse resp, ProcessConfig config) {
        String body = ServletUtil.getBody(req);
        String contentType = HttpUtil.getContentTypeByRequestBody(body);
        Map<String, Object> variables = new HashMap<>();

        // 将query参数放入map中
        variables.putAll(ServletUtil.getParamMap(req));

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
        ServletUtil.write(resp, JacksonUtil.to(result), "application/json;charset=utf-8");
    }

    @SneakyThrows
    private void forward(HttpServletRequest req, HttpServletResponse resp, ForwardConfig config) {
        req.setAttribute(ATTR_TARGET_URI, "");
        req.setAttribute(ATTR_TARGET_HOST, URIUtils.extractHost(new URI(config.getBaseUrl())));

        super.service(req, resp);
    }

}
