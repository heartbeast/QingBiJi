package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnBindPhoneListener;

/**
 * 主界面使用  绑定新手机号 p层interface
 */
public interface IBindPhoneModule {
    void mVcode(OnBindPhoneListener listener, String phone, String name,String answer,String mNonce,String mHashKey);

    void mVerifyPic(OnBindPhoneListener listener);

    void mBindNewPhone(OnBindPhoneListener listener, String phone, String vcode, String ps);

    void mGetUserInfo(OnBindPhoneListener listener);
}
