package com.brandnewdata.mop.poc.config;

import com.brandnewdata.mop.poc.process.service.IProcessDeployService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/proxy/*")
public class MyServlet extends HttpServlet {

    @Resource
    private IProcessDeployService processDeployService;

    @PostConstruct
    private void postConstruct() {
        return;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }
}
