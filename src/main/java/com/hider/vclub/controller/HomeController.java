package com.hider.vclub.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hider.vclub.entity.DiscussPost;
import com.hider.vclub.entity.User;
import com.hider.vclub.service.DiscussPostService;
import com.hider.vclub.service.LikeService;
import com.hider.vclub.service.UserService;
import com.hider.vclub.util.VclubContant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements VclubContant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, @RequestParam(required = false, defaultValue = "1", value = "pageNum") Integer pageNum,
                               @RequestParam(defaultValue = "10", value = "pageSize") Integer pageSize) {
//        page.setRows(discussPostService.findDiscussPostRows(0));
//        page.setPath("/index");
        PageHelper.startPage(pageNum, pageSize);


        List<DiscussPost> list = discussPostService.findDiscussPosts(0);
        PageInfo<DiscussPost> pageInfo = new PageInfo<DiscussPost>(list, pageSize);

        List<Map<String, Object>> discussPosts = new ArrayList<>();
        // 如果list不为空,把user对象也查询出来,然后添加进map里.最后塞进集合里
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }

//        System.out.println(discussPosts);
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("pageInfo", pageInfo);

        return "/index";
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }
}
