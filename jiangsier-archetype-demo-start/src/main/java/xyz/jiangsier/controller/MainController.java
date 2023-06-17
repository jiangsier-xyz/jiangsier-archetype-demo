package xyz.jiangsier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

@Controller
@SuppressWarnings("unused")
public class MainController {
    private static final List<String> FAVICON_STYLES = Arrays.asList("dark", "light");

    @RequestMapping("/")
    public String index(Model model) {
        return "redirect:/swagger-ui/index.html";
    }

    @GetMapping("/public/check/live")
    @ResponseBody
    public String live() {
        return "OK";
    }

    @GetMapping("/public/check/ready")
    @ResponseBody
    public String ready() {
        // TODO: check services statuses
        return "OK";
    }
}
