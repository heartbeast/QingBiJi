package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnSplashListener;

/**
 *  m层interface
 */
public interface ISplashModule {
    void mLogin(OnSplashListener listener, String name, String ps);

    void mProFile(OnSplashListener listener);
}
