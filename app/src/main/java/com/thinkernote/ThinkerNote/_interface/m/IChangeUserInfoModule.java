package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnChangeUserInfoListener;

/**
 * m层
 */
public interface IChangeUserInfoModule {
    void mChangePs(OnChangeUserInfoListener listener, String oldPs, String newPs);

    void mChangeNameOrEmail(OnChangeUserInfoListener listener, String nameOrEmail, String type, String userPs);

    void mProfile(OnChangeUserInfoListener listener);

}
