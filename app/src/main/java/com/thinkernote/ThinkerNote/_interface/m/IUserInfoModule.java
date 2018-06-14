package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;
import com.thinkernote.ThinkerNote._interface.v.OnUserinfoListener;

/**
 * 主页--设置界面 m层interface
 */
public interface IUserInfoModule {
    void mLogout(OnUserinfoListener listener);

    void mUpgrade(OnUserinfoListener listener);


}
