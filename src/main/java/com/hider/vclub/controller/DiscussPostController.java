package com.hider.vclub.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hider.vclub.entity.Comment;
import com.hider.vclub.entity.DiscussPost;
import com.hider.vclub.entity.User;
import com.hider.vclub.service.CommentService;
import com.hider.vclub.service.DiscussPostService;
import com.hider.vclub.service.LikeService;
import com.hider.vclub.service.UserService;
import com.hider.vclub.util.HostHolder;
import com.hider.vclub.util.VclubContant;
import com.hider.vclub.util.VclubUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements VclubContant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;


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
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, @RequestParam(required = false, defaultValue = "1", value = "pageNum") Integer pageNum,
                                 @RequestParam(defaultValue = "5", value = "pageSize") Integer pageSize) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        // 点赞状态
        long likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 评论的分页信息
        PageHelper.startPage(pageNum, pageSize);
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId());
        PageInfo<Comment> pageInfo = new PageInfo<Comment>(commentList, pageSize);

        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);
                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                for (Comment reply : replyList) {
                    Map<String, Object> replyVo = new HashMap<>();
                    // 评论
                    replyVo.put("reply", reply);
                    // 作者
                    replyVo.put("user", userService.findUserById(reply.getUserId()));

                    // 点赞数量
                    likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                    replyVo.put("likeCount", likeCount);

                    // 点赞状态
                    likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                    replyVo.put("likeStatus", likeStatus);

                    // 回复目标
                    User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                    replyVo.put("target", target);

                    replyVoList.add(replyVo);
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";

    }
}
