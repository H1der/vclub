package com.hider.vclub.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class VclubUtil {
    // 生成随机的字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // md5加密
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
