package com.hider.vclub.service;

import com.hider.vclub.dao.DiscussPostMapper;
import com.hider.vclub.entity.DiscussPost;
import com.hider.vclub.util.SentistiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SentistiveFilter sentistiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId) {
        return discussPostMapper.selectDiscussPosts(userId);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

//        html 转义
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        // 敏感词过滤
        discussPost.setTitle(sentistiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sentistiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

}
