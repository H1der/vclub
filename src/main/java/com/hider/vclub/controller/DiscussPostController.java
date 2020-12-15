package com.hider.vclub.controller;

import com.hider.vclub.entity.DiscussPost;
import com.hider.vclub.entity.User;
import com.hider.vclub.service.DiscussPostService;
import com.hider.vclub.service.UserService;
import com.hider.vclub.util.HostHolder;
import com.hider.vclub.util.VclubUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    UserService userService;


    // 添加帖子
    @RequestMapping(value = "add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return VclubUtil.getJSONString(403, "未登录!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // todo 报错处理
        return VclubUtil.getJSONString(200, "发布成功");
    }

    // 帖子详情
    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        return "/site/discuss-detail";

    }
}
