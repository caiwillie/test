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
public class ForwardErrorController implements ErrorController {
   private static final Logger LOGGER = LoggerFactory.getLogger(ForwardErrorController.class);
   @Autowired
   private OperateProfileService operateProfileService;

   @RequestMapping({"/error"})
   public ModelAndView handleError(HttpServletRequest request, HttpServletResponse response) {
      String requestedURI = (String)request.getAttribute("javax.servlet.forward.request_uri");
      if (requestedURI == null) {
         return this.forwardToRootPage();
      } else if (this.operateProfileService.isLoginDelegated() && !requestedURI.contains("/api/login") && this.isNotLoggedIn()) {
         return this.saveRequestAndRedirectToLogin(request, requestedURI);
      } else if (requestedURI.contains("/api/")) {
         ModelAndView modelAndView = new ModelAndView();
         Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
         Exception exception = (Exception)request.getAttribute("org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR");
         modelAndView.addObject("message", this.operateProfileService.getMessageByProfileFor(exception));
         modelAndView.setStatus(HttpStatus.valueOf(statusCode));
         return modelAndView;
      } else {
         return this.forwardToRootPage();
      }
   }

   private ModelAndView forwardToRootPage() {
      ModelAndView modelAndView = new ModelAndView("forward:/");
      modelAndView.setStatus(HttpStatus.OK);
      return modelAndView;
   }

   private ModelAndView saveRequestAndRedirectToLogin(HttpServletRequest request, String requestedURI) {
      LOGGER.warn("Requested path {}, but not authenticated. Redirect to  {} ", requestedURI, "/api/login");
      String queryString = request.getQueryString();
      if (ConversionUtils.stringIsEmpty(queryString)) {
         request.getSession(true).setAttribute("requestedUrl", requestedURI);
      } else {
         request.getSession(true).setAttribute("requestedUrl", requestedURI + "?" + queryString);
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
