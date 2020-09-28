package com.hider.vclub.util;

public interface VclubContant {
    //激活成功
    int ACTIVATE_SUCCESS = 0;

    // 重复激活
    int ACTIVATE_REPEAT = 1;

    //激活失败
    int ACTIVATE_FAILURE = 2;


    // 默认状态的登录凭证超时时间

    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;


    // 记住状态的登录凭证超市时间
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
}
