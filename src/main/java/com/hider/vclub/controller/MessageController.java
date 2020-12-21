package com.hider.vclub.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hider.vclub.entity.Message;
import com.hider.vclub.entity.User;
import com.hider.vclub.service.MessageService;
import com.hider.vclub.service.UserService;
import com.hider.vclub.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/letter")

public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public String getLetterList(Model model, @RequestParam(required = false, defaultValue = "1", value = "pageNum") Integer pageNum,
                                @RequestParam(defaultValue = "5", value = "pageSize") Integer pageSize) {

        User user = hostHolder.getUser();

        // 分页信息
        PageHelper.startPage(pageNum, pageSize);

        // 会话列表
        List<Message> conversationList = messageService.findConversations(user.getId());
        PageInfo<Message> pageInfo = new PageInfo<Message>(conversationList, pageSize);

        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));

                // 如果当前用户等于消息发起者,那么目标是接收者
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);
        model.addAttribute("pageInfo", pageInfo);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";

    }

    @RequestMapping(value = "detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, @RequestParam(required = false, defaultValue = "1", value = "pageNum") Integer pageNum,
                                  @RequestParam(defaultValue = "5", value = "pageSize") Integer pageSize) {

        // 分页信息
        PageHelper.startPage(pageNum, pageSize);

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId);
        PageInfo<Message> pageInfo = new PageInfo<Message>(letterList, pageSize);
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        model.addAttribute("pageInfo", pageInfo);

        //私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        return "/site/letter-detail";

    }

    /**
     * 获取私信目标
     *
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");

        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        // 不等于当前用户的是私信用户
        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }


    }
}
