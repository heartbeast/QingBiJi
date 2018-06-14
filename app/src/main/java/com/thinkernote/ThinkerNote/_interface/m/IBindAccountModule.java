package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnBindAccountListener;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;

/**
 * 登录使用 绑定手机号
 */
public interface IBindAccountModule {
    void mVerifyCode(OnBindAccountListener listener, String phone, String t);
    void mLoginBind( OnBindAccountListener listener,int btype, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone, String vcode,String sign);
    void autoLoginAferBind(OnBindAccountListener listener,int btype, final String bid, final long stamp, String sign);
}
