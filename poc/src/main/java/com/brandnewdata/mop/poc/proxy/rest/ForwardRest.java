package com.brandnewdata.mop.poc.proxy.rest;

import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping
public class ForwardRest {

    @RequestMapping("/forward/**")
    public void entry(HttpServletRequest request, HttpServletResponse response) {
        return;
    }

}
