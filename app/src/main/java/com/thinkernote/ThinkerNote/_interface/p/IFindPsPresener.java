package com.thinkernote.ThinkerNote._interface.p;

/**
 * 登录 p层interface
 */
public interface IFindPsPresener {
    void getVerifyPic();
    void phoneVerifyCode(String mPhone,String name,String mAnswer,String mNonce,String mHashKey);
    void mailVerifyCode(String mEmail,String name);
    void submit(String phone,String ps,String vcode);
    void autoLogin(String phoneOrEmail,String ps);
    void pProfile();
}
