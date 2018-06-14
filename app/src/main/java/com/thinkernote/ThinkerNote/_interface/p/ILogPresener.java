package com.thinkernote.ThinkerNote._interface.p;

/**
 * 登录 p层interface
 */
public interface ILogPresener {
    void loginNormal(String name,String ps);
    void loginQQ(int aArray,String unionId,long currentTime,String accessToken,String refreshToken,String qqflag);
    void loginSina(int aArray,String unionId,long currentTime,String accessToken,String refreshToken,String qqflag);
    void loginWechat(int aArray,String unionId,long currentTime,String accessToken,String refreshToken,String qqflag);
}
