package io.camunda.operate.webapp;

import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
   @Autowired
   private ServletContext context;

   @GetMapping({"/index.html"})
   public String index(Model model) {
      model.addAttribute("contextPath", this.context.getContextPath() + "/");
      return "index";
   }
}
