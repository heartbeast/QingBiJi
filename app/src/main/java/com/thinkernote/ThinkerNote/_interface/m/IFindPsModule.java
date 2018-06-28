package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnFindPsListener;

/**
 *  m层interface
 */
public interface IFindPsModule {
    void getVerifyPic(OnFindPsListener onFindPsListener);

    void phoneVerifyCode(OnFindPsListener onFindPsListener, String mPhone, String name, String mAnswer, String mNonce, String mHashKey);

    void mailVerifyCode(OnFindPsListener onFindPsListener, String mEmail, String name);

    void submit(OnFindPsListener onFindPsListener, String phone, String ps, String vcode);

    void autoLogin(OnFindPsListener onFindPsListener, String phone, String ps);

    void mProfile(OnFindPsListener onFindPsListeners);
}
