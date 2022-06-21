package io.camunda.operate.webapp.security.sso;

import com.auth0.IdentityVerificationException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Profile({"sso-auth"})
public class SSOController {
   protected final Logger logger = LoggerFactory.getLogger(this.getClass());
   @Autowired
   private Auth0Service auth0Service;

   @RequestMapping(
      value = {"/api/login"},
      method = {RequestMethod.GET, RequestMethod.POST}
   )
   public String login(HttpServletRequest req, HttpServletResponse res) {
      String authorizeUrl = this.auth0Service.getAuthorizeUrl(req, res);
      this.logger.debug("Redirect Login to {}", authorizeUrl);
      return "redirect:" + authorizeUrl;
   }

   @GetMapping({"/sso-callback"})
   public void loggedInCallback(HttpServletRequest req, HttpServletResponse res) throws IOException {
      this.logger.debug("Called back by auth0 with {} {} and SessionId: {}", new Object[]{req.getRequestURI(), req.getQueryString(), req.getSession().getId()});

      try {
         this.auth0Service.authenticate(req, res);
         this.redirectToPage(req, res);
      } catch (InsufficientAuthenticationException var4) {
         this.clearContextAndRedirectToNoPermission(req, res, var4);
      } catch (Auth0ServiceException var5) {
         this.handleAuth0Exception(var5, req, res);
      }

   }

   private void handleAuth0Exception(Auth0ServiceException ase, HttpServletRequest req, HttpServletResponse res) throws IOException {
      Throwable cause = ase.getCause();
      if (cause != null) {
         if (cause instanceof InsufficientAuthenticationException) {
            this.logoutAndRedirectToNoPermissionPage(req, res);
         } else if (!(cause instanceof IdentityVerificationException) && !(cause instanceof AuthenticationException)) {
            this.logout(req, res);
         } else {
            this.clearContextAndRedirectToNoPermission(req, res, cause);
         }
      } else {
         this.logout(req, res);
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

   @RequestMapping({"/noPermission"})
   @ResponseBody
   public String noPermissions() {
      return "No permission for Operate - Please check your operate configuration or cloud configuration.";
   }

   @RequestMapping({"/api/logout"})
   public void logout(HttpServletRequest req, HttpServletResponse res) throws IOException {
      this.logger.debug("logout user");
      this.cleanup(req);
      this.logoutFromAuth0(res, this.auth0Service.getRedirectURI(req, "/"));
   }

   protected void clearContextAndRedirectToNoPermission(HttpServletRequest req, HttpServletResponse res, Throwable t) throws IOException {
      this.logger.error("Error in authentication callback: ", t);
      this.cleanup(req);
      res.sendRedirect(this.auth0Service.getRedirectURI(req, "/noPermission"));
   }

   protected void logoutAndRedirectToNoPermissionPage(HttpServletRequest req, HttpServletResponse res) throws IOException {
      this.logger.warn("User is authenticated but there are no permissions.");
      this.cleanup(req);
      this.logoutFromAuth0(res, this.auth0Service.getRedirectURI(req, "/noPermission"));
   }

   protected void cleanup(HttpServletRequest req) {
      req.getSession().invalidate();
      SecurityContextHolder.clearContext();
   }

   protected void logoutFromAuth0(HttpServletResponse res, String returnTo) throws IOException {
      res.sendRedirect(this.auth0Service.getLogoutUrlFor(returnTo));
   }
}
