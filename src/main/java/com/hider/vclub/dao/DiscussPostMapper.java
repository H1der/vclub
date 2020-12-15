package com.hider.vclub.dao;

import com.hider.vclub.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    // 查询帖子列表
    List<DiscussPost> selectDiscussPosts(int userId);

    // 查询帖子函数
    int selectDiscussPostRows(@Param("userId") int userId);

    // 插入帖子
    int insertDiscussPost(DiscussPost discussPost);

    // 根据 id 查询帖子详情
    DiscussPost selectDiscussPostById(int id);
}
