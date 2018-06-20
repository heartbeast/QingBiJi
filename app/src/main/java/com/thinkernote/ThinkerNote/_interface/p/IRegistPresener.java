package com.thinkernote.ThinkerNote._interface.p;

/**
 * 手机号注册 p层interface
 */
public interface IRegistPresener {
    void getVerifyPic();
    void phoneVerifyCode(String mPhone, String name, String mAnswer, String mNonce, String mHashKey);
    void submitRegister(String phone, String ps, String vcode);
    void submitForgotPassword(String phone, String ps, String vcode);
    void bindPhone(int mUserType,String bid,String name,String accessToken,String refreshToken,long currentTime,String phone,String Vcode );
    void autoLogin(String phoneOrEmail, String ps);
    void pProfile();

}
