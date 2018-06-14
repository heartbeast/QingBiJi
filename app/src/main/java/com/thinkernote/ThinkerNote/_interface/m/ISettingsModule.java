package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnSettingsListener;

/**
 *  m层interface
 */
public interface ISettingsModule {
    void mgetProfile(OnSettingsListener listeners);
    void mVerifyEmail(OnSettingsListener listeners);
    void mSetDefaultFolder(OnSettingsListener listeners,long pid);

}
