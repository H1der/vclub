package com.hider.vclub.dao;

import com.hider.vclub.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表,每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话缩包含的私信列表
    List<Message> selectLetters(String conversationId);

    //查询某个会话缩包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 增加消息
    int insertMessage(Message message);

    // 更改消息状态
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下的最新通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题下所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题缩包含的通知列表
    List<Message> selectNotices(int userId, String topic);

}
