/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.identity.sdk.Identity
 *  io.camunda.identity.sdk.authentication.dto.AuthCodeDto
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.webapp.security.identity.IdentityAuthentication
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.springframework.beans.factory.BeanFactory
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.Profile
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.context.SecurityContextHolder
 *  org.springframework.stereotype.Controller
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestMethod
 *  org.springframework.web.bind.annotation.ResponseBody
 */
package io.camunda.operate.webapp.security.identity;

import io.camunda.identity.sdk.Identity;
import io.camunda.identity.sdk.authentication.dto.AuthCodeDto;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.webapp.security.identity.IdentityAuthentication;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Profile(value={"identity-auth"})
public class IdentityController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected OperateProperties operateProperties;
    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private Identity identity;

    @RequestMapping(value={"/api/login"}, method={RequestMethod.GET, RequestMethod.POST})
    public String login(HttpServletRequest req) {
        String authorizeUrl = this.identity.authentication().authorizeUriBuilder(IdentityAuthentication.getRedirectURI((HttpServletRequest)req, (String)"/identity-callback")).build().toString();
        this.logger.debug("Redirect Login to {}", (Object)authorizeUrl);
        return "redirect:" + authorizeUrl;
    }

    @RequestMapping(value={"/identity-callback"}, method={RequestMethod.GET})
    public void loggedInCallback(HttpServletRequest req, HttpServletResponse res, AuthCodeDto authCodeDto) throws IOException {
        this.logger.debug("Called back by identity with {} {}, SessionId: {} and AuthCode {}", req.getRequestURI(), req.getQueryString(), req.getSession().getId(), authCodeDto.getCode());
        try {
            IdentityAuthentication authentication = (IdentityAuthentication)this.beanFactory.getBean(IdentityAuthentication.class);
            authentication.authenticate(req, authCodeDto);
            SecurityContextHolder.getContext().setAuthentication((Authentication)authentication);
            this.redirectToPage(req, res);
        }
        catch (Exception iae) {
            this.clearContextAndRedirectToNoPermission(req, res, iae);
        }
    }

    private void redirectToPage(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Object originalRequestUrl = req.getSession().getAttribute("requestedUrl");
        if (originalRequestUrl != null) {
            res.sendRedirect(originalRequestUrl.toString());
        } else {
            res.sendRedirect("/");
        }
    }

    @RequestMapping(value={"/noPermission"})
    @ResponseBody
    public String noPermissions() {
        return "No permission for Operate - Please check your operate configuration or cloud configuration.";
    }

    @RequestMapping(value={"/api/logout"})
    public void logout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        this.logger.debug("logout user");
        try {
            IdentityAuthentication authentication = (IdentityAuthentication)SecurityContextHolder.getContext().getAuthentication();
            this.identity.authentication().revokeToken(authentication.getTokens().getRefreshToken());
        }
        catch (Exception e) {
            this.logger.error("An error occurred in logout process", e);
        }
        this.cleanup(req);
    }

    protected void clearContextAndRedirectToNoPermission(HttpServletRequest req, HttpServletResponse res, Throwable t) throws IOException {
        this.logger.error("Error in authentication callback: ", t);
        this.cleanup(req);
        res.sendRedirect("/noPermission");
    }

    protected void cleanup(HttpServletRequest req) {
        req.getSession().invalidate();
        SecurityContextHolder.clearContext();
    }
}
