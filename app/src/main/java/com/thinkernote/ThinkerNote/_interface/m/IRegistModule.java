package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;

/**
 * 登录 m层interface
 */
public interface IRegistModule {
    void getVerifyPic(OnRegistListener OnRegistListener);

    void phoneVerifyCode(OnRegistListener OnRegistListener, String mPhone, String name, String mAnswer, String mNonce, String mHashKey);

    void submitRegist(OnRegistListener OnRegistListener, String phone, String ps, String vcode);

    void submitForgetPs(OnRegistListener OnRegistListener, String phone, String ps, String vcode);

    void autoLogin(OnRegistListener OnRegistListener, String phone, String ps);

    void bindPhone(OnRegistListener OnRegistListener, int mUserType, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone,String vcode,String sign);

}
