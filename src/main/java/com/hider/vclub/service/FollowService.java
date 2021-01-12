package com.hider.vclub.service;

import com.hider.vclub.entity.User;
import com.hider.vclub.util.RedisKeyUtil;
import com.hider.vclub.util.VclubContant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements VclubContant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;


    /**
     * 关注
     *
     * @param userId     用户id
     * @param entityType 实体类型
     * @param entityId   实体id
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followedKey = RedisKeyUtil.getFollowedKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 事务操作
                redisOperations.multi();

                redisOperations.opsForZSet().add(followedKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }


    /**
     * 取消关注
     *
     * @param userId     用户id
     * @param entityType 实体类型
     * @param entityId   实体id
     */
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followedKey = RedisKeyUtil.getFollowedKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 事务操作
                redisOperations.multi();

                redisOperations.opsForZSet().remove(followedKey, entityId);
                redisOperations.opsForZSet().remove(followerKey, userId);

                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询关注的实体数量
     *
     * @param userId     用户id
     * @param entityType 实体类型
     * @return
     */
    public long findFollowedCount(int userId, int entityType) {
        String followedKey = RedisKeyUtil.getFollowedKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followedKey);
    }

    /**
     * 查询实体的粉丝数量
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @return
     */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询当前用户是否已关注该实体
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followedKey = RedisKeyUtil.getFollowedKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followedKey, entityId) != null;
    }


    /**
     * 查询某用户关注的人
     *
     * @param userId 用户id
     * @param offset 起始
     * @param limit  数量
     * @return list
     */
    public List<Map<String, Object>> findFollows(int userId, int offset, int limit) {
        String followedKey = RedisKeyUtil.getFollowedKey(userId, ENTITY_TYPE_USER);
        // offset 包含自己要减一
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followedKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            // 查询关注时间
            Double score = redisTemplate.opsForZSet().score(followedKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;

    }

    /**
     * 查询某用户的粉丝
     *
     * @param userId 用户id
     * @param offset 起始
     * @param limit  数量
     * @return list
     */
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            // 查询关注时间
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

}
