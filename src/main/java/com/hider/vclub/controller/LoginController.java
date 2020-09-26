package com.hider.vclub.controller;

import com.google.code.kaptcha.Producer;
import com.hider.vclub.entity.User;
import com.hider.vclub.service.UserService;
import com.hider.vclub.util.VclubContant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements VclubContant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

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

    //验证码
    @RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //验证码存入session
        session.setAttribute("kaptcha", text);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);

        } catch (IOException e) {
            logger.error("响应验证码失败:", e.getMessage());

        }

    }
}
