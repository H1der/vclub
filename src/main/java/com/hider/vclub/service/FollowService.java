package com.hider.vclub.service;

import com.hider.vclub.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;


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

    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followedKey = RedisKeyUtil.getFollowedKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followedKey, entityId) != null;
    }

}
