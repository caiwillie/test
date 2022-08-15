package com.brandnewdata.mop.poc.config;

import com.brandnewdata.mop.poc.process.service.IProcessDeployService;
import com.brandnewdata.mop.poc.proxy.service.BackendService;
import com.brandnewdata.mop.poc.proxy.servlet.ReverseProxyServlet;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReverseProxyServletConfiguration {

    // @Bean
    public ServletRegistrationBean<ReverseProxyServlet> servletRegistrationBean(IProcessDeployService processDeployService, BackendService backendService) {
        ReverseProxyServlet reverseProxyServlet = new ReverseProxyServlet(processDeployService, backendService);
        ServletRegistrationBean<ReverseProxyServlet> servletRegistrationBean = new ServletRegistrationBean<>(reverseProxyServlet, "/proxy/*");
        servletRegistrationBean.addInitParameter(ProxyServlet.P_TARGET_URI, "http://www.brandnewdata.com");
        return servletRegistrationBean;
    }

}
