package com.hider.vclub.controller;

import com.hider.vclub.annotation.LoginRequired;
import com.hider.vclub.entity.User;
import com.hider.vclub.service.UserService;
import com.hider.vclub.util.HostHolder;
import com.hider.vclub.util.VclubUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${vclub.path.upload}")
    private String uploadPath;

    @Value("${vclub.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;


    @LoginRequired
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
//        System.out.println(uploadPath);
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "没有选择图片!");
            return "";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确!");
            return "";
        }

        // 生成随机文件名
        filename = VclubUtil.generateUUID() + suffix;
        File dest = new File(this.getClass().getClassLoader().getResource("static").getFile() + "/" + filename);

        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器异常!", e);
        }

        // 更新当前头像url
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;

        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";

    }

    @RequestMapping(value = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String fileName, HttpServletResponse response) {
        fileName = this.getClass().getClassLoader().getResource("static").getFile() + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("/image");
        try (OutputStream os = response.getOutputStream();
             FileInputStream fis = new FileInputStream(fileName);) {
            // 字节流写入 每次写入1024字节
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }


        } catch (IOException e) {
            logger.error("读取头像失败:" + e.getMessage());
        }

    }

    // 登录提交
    @LoginRequired
    @RequestMapping(value = "/change", method = RequestMethod.POST)
    public String change(String originalPassword, String password, String confirmPassword, Model model) {
        User user = hostHolder.getUser();
//        System.out.println("originalPassword:" + originalPassword + "password:" + password + "confirmPassword:" + confirmPassword);
        Map<String, Object> map = userService.changePassword(user.getId(), originalPassword, password, confirmPassword);
//        return "redirect:/index";
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "密码修改成功");
            return "redirect:/index";
        } else {
            model.addAttribute("passwordMsg", map.get("passwordMsg"));

            return "/site/setting";
        }

    }
}
