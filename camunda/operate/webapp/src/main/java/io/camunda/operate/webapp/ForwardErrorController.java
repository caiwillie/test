/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.util.ConversionUtils
 *  io.camunda.operate.webapp.security.OperateProfileService
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.web.servlet.error.ErrorController
 *  org.springframework.http.HttpStatus
 *  org.springframework.security.authentication.AnonymousAuthenticationToken
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.context.SecurityContextHolder
 *  org.springframework.stereotype.Controller
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.servlet.ModelAndView
 */
package io.camunda.operate.webapp;

import io.camunda.operate.util.ConversionUtils;
import io.camunda.operate.webapp.security.OperateProfileService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ForwardErrorController
implements ErrorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForwardErrorController.class);
    @Autowired
    private OperateProfileService operateProfileService;

    @RequestMapping(value={"/error"})
    public ModelAndView handleError(HttpServletRequest request, HttpServletResponse response) {
        String requestedURI = (String)request.getAttribute("javax.servlet.forward.request_uri");
        if (requestedURI == null) {
            return this.forwardToRootPage();
        }
        if (this.operateProfileService.isLoginDelegated() && !requestedURI.contains("/api/login") && this.isNotLoggedIn()) {
            return this.saveRequestAndRedirectToLogin(request, requestedURI);
        }
        if (!requestedURI.contains("/api/")) return this.forwardToRootPage();
        ModelAndView modelAndView = new ModelAndView();
        Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception)request.getAttribute("org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR");
        modelAndView.addObject("message", (Object)this.operateProfileService.getMessageByProfileFor(exception));
        modelAndView.setStatus(HttpStatus.valueOf((int)statusCode));
        return modelAndView;
    }

    private ModelAndView forwardToRootPage() {
        ModelAndView modelAndView = new ModelAndView("forward:/");
        modelAndView.setStatus(HttpStatus.OK);
        return modelAndView;
    }

    private ModelAndView saveRequestAndRedirectToLogin(HttpServletRequest request, String requestedURI) {
        LOGGER.warn("Requested path {}, but not authenticated. Redirect to  {} ", (Object)requestedURI, (Object)"/api/login");
        String queryString = request.getQueryString();
        if (ConversionUtils.stringIsEmpty((String)queryString)) {
            request.getSession(true).setAttribute("requestedUrl", (Object)requestedURI);
        } else {
            request.getSession(true).setAttribute("requestedUrl", (Object)(requestedURI + "?" + queryString));
        }
        ModelAndView modelAndView = new ModelAndView("redirect:/api/login");
        modelAndView.setStatus(HttpStatus.FOUND);
        return modelAndView;
    }

    private boolean isNotLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated();
    }
}
