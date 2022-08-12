package com.brandnewdata.mop.poc.proxy.servlet;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.proxy.dto.Backend;
import com.brandnewdata.mop.poc.proxy.dto.ForwardConfig;
import com.brandnewdata.mop.poc.proxy.dto.ProcessConfig;
import com.brandnewdata.mop.poc.proxy.service.BackendService;
import com.dxy.library.json.jackson.JacksonUtil;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URIUtils;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Component
public class ReverseProxyServlet extends ProxyServlet {

    @Autowired
    private IProcessDeployService deployService;

    @Autowired
    private BackendService backendService;

    /*private ProxyServlet proxyServlet;

    @SneakyThrows
    @PostConstruct
    private void postConstruct() {
        proxyServlet = new ProxyServlet();
        proxyServlet.init(this);
    }*/

    private static final String ATTR_TARGET_URI =
            ProxyServlet.class.getSimpleName() + ".targetUri";
    private static final String ATTR_TARGET_HOST =
            ProxyServlet.class.getSimpleName() + ".targetHost";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        // 去掉前缀
        uri = StrUtil.removePrefix(uri, "/proxy");
        String domain = req.getHeader("g2-domain");

        Backend backend = backendService.getBackend(domain, uri);

        if (backend == null) {
            // 后端配置未找到，直接返回
            ServletUtil.write(resp, "未配置该API", MimeTypeUtils.TEXT_PLAIN_VALUE);
            return;
        }

        // 不同类型，不同处理
        if (backend.getType() == 1) {
            ProcessConfig config = (ProcessConfig) backend.getData();
            startProcess(resp, config);
        } else if (backend.getType() == 2) {
            ForwardConfig config = (ForwardConfig) backend.getData();
            forward(req, resp,  uri, config);
        }
    }


    private void startProcess(HttpServletResponse resp, ProcessConfig config) {
        String processId = config.getProcessId();
        Map<String, Object> result = deployService.startWithResult(processId, MapUtil.empty());
        result = Optional.ofNullable(result).orElse(MapUtil.empty());
        ServletUtil.write(resp, JacksonUtil.to(result), MimeTypeUtils.APPLICATION_JSON_VALUE);
    }

    @SneakyThrows
    private void forward(HttpServletRequest req, HttpServletResponse resp, String uri, ForwardConfig config) {
        req.setAttribute(ATTR_TARGET_URI, uri);
        req.setAttribute(ATTR_TARGET_HOST, URIUtils.extractHost(new URI(config.getBaseUrl())));
        super.service(req, resp);
    }

}
