package xyz.jiangsier.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.jiangsier.auth.user.SysUserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Controller
@SuppressWarnings("unused")
public class MainController {
    private static final List<String> FAVICON_STYLES = Arrays.asList("dark", "light");

    @RequestMapping("/")
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication)) {
            if (authentication.getPrincipal() instanceof SysUserDetails sysUser) {
                String name = sysUser.getNickname();
                if (StringUtils.isBlank(name)) {
                    name = sysUser.getUsername();
                }
                model.addAttribute("name", name);
            } else {
                model.addAttribute("name", authentication.getName());
            }
        }
        return "hello";
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
