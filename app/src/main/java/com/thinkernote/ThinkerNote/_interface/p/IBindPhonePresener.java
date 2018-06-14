package com.thinkernote.ThinkerNote._interface.p;

/**
 * 主界面使用  绑定新手机号 p层interface
 */
public interface IBindPhonePresener {
    void pVcode(String phone, String name,String answer,String mNonce,String mHashKey);

    void pVerifyPic();
    void pGetUserInfo();

    void pSubmit(String phone,String vcode,String ps);
}