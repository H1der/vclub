package com.hider.vclub.controller;

import com.hider.vclub.entity.User;
import com.hider.vclub.service.FollowService;
import com.hider.vclub.service.UserService;
import com.hider.vclub.util.HostHolder;
import com.hider.vclub.util.VclubContant;
import com.hider.vclub.util.VclubUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements VclubContant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 关注
    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        return VclubUtil.getJSONString(200, "已关注");
    }

    // 取消关注
    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);

        return VclubUtil.getJSONString(200, "已取消关注");
    }

    // 某用户关注的人
    @RequestMapping(value = "/follows/{userId}", method = RequestMethod.GET)
    public String getFollows(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        List<Map<String, Object>> userList = followService.findFollows(userId, 0, 20);
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollow", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followed";

    }

    // 某用户的粉丝
    @RequestMapping(value = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        List<Map<String, Object>> userList = followService.findFollowers(userId, 0, 20);
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollow", hasFollowed(u.getId()));
            }
        }

        model.addAttribute("users", userList);

        return "/site/follower";

    }

    // 判断是否关注
    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}
