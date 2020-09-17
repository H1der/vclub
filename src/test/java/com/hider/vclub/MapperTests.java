package com.hider.vclub;

import com.hider.vclub.dao.DiscussPostMapper;
import com.hider.vclub.dao.UserMapper;
import com.hider.vclub.entity.DiscussPost;
import com.hider.vclub.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VclubApplication.class)
class MapperTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    void contextLoads() {
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for (DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);

    }

}
