package com.thinkernote.ThinkerNote._interface.p;

/**
 * 登录 p层interface
 */
public interface ILogPresenter {
    void loginNormal(String name, String ps);

    void loginThird(int aArray, String unionId, long currentTime, String accessToken, String refreshToken, String qqflag);

    void pUpdataProfile();
}
