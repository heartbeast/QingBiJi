package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnLogListener;

/**
 * 登录
 */
public interface ILogModule {
    void loginNomal(OnLogListener listener, String name, String ps);

    void loginThird(OnLogListener listener, int btype, String bid, long stamp, String sign, String accessToken, String refreshToken, String name);

    void mProfile(OnLogListener listener);
}
