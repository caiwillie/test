package com.brandnewdata.mop.poc.config;

import com.brandnewdata.mop.poc.proxy.servlet.ReverseProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReverseProxyServletConfiguration {

    @Bean
    public ServletRegistrationBean<ReverseProxyServlet> servletRegistrationBean(ReverseProxyServlet servlet) {
        return new ServletRegistrationBean<>(servlet, "/forward/*");
    }

}
