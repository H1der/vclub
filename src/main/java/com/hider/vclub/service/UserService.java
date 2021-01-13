package com.hider.vclub.service;

import com.hider.vclub.dao.UserMapper;
import com.hider.vclub.entity.LoginTicket;
import com.hider.vclub.entity.User;
import com.hider.vclub.util.MailClient;
import com.hider.vclub.util.RedisKeyUtil;
import com.hider.vclub.util.VclubContant;
import com.hider.vclub.util.VclubUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements VclubContant {
    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${vclub.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }


    public Map<String, Object> register(User user) {
        HashMap<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }

        // 注册用户
        user.setSalt(VclubUtil.generateUUID().substring(0, 5));
        user.setPassword(VclubUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(VclubUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activate/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);

        String content = templateEngine.process("/mail/activate", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;

    }

    public int activate(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATE_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATE_SUCCESS;
        } else {
            return ACTIVATE_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        HashMap<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "账号不存在!");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活!");
            return map;
        }

        // 验证密码
        password = VclubUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(VclubUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    // 销毁凭证,退出登录
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        // 先吧凭证取出来,再进行销毁,最后写入redis
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);

    }

    // 查找凭证
    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }


    // 更新头像
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
//        return userMapper.updateHeader(userId, headerUrl);
    }

    // 更新密码
    public Map<String, Object> changePassword(int userId, String originalPassword, String password, String confirmPassword) {
        HashMap<String, Object> map = new HashMap<>();
        if (userId == 0) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(originalPassword)) {
            map.put("passwordMsg", "原密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(confirmPassword)) {
            map.put("passwordMsg", "确认密码不能为空！");
            return map;
        }
        if (!password.equals(confirmPassword)) {
            map.put("passwordMsg", "两次输入密码不正确！");
            return map;
        }
        // 验证账号
        User user = userMapper.selectById(userId);
        if (user == null) {
            map.put("usernameMsg", "账号不存在!");
            return map;
        }
//        System.out.println(user);

//         验证密码
        originalPassword = VclubUtil.md5(originalPassword + user.getSalt());
        if (!user.getPassword().equals(originalPassword)) {
            map.put("passwordMsg", "原密码不正确!");
            return map;
        }


        // 更新密码
        userMapper.updatePassword(userId, VclubUtil.md5(password + user.getSalt()));
        clearCache(userId);


        return map;
    }

    // 通过用户名查找用户
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }


    //1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    //3.数据变更时清楚缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}
