package com.hider.vclub.controller;

import com.hider.vclub.entity.User;
import com.hider.vclub.service.UserService;
import com.hider.vclub.util.VclubContant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements VclubContant {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,请查收激活邮件!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));

            return "/site/register";
        }
    }

    @RequestMapping(value = "/activate/{userId}/{code}", method = RequestMethod.GET)
    private String activate(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activate(userId, code);
        if (result == ACTIVATE_SUCCESS) {
            model.addAttribute("msg", "激活成功!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATE_REPEAT) {
            // 已经激活了
            model.addAttribute("msg", "无效操作!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";

    }
}
