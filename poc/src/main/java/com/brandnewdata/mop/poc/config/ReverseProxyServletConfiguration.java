package com.brandnewdata.mop.poc.config;

import com.brandnewdata.mop.poc.proxy.servlet.ReverseProxyServlet;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReverseProxyServletConfiguration {

    @Bean
    public ServletRegistrationBean<ReverseProxyServlet> servletRegistrationBean(ReverseProxyServlet servlet) {
        ServletRegistrationBean<ReverseProxyServlet> servletRegistrationBean = new ServletRegistrationBean<>(servlet, "/proxy/*");
        servletRegistrationBean.addInitParameter(ProxyServlet.P_TARGET_URI, "http://www.brandnewdata.com");
        return servletRegistrationBean;
    }

}
