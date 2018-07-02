package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;
import com.thinkernote.ThinkerNote._interface.v.OnUserinfoListener;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

/**
 * 主页--设置界面 m层interface
 */
public interface IUserInfoModule {
    void mLogout(OnUserinfoListener listener);

    void mUpgrade(OnUserinfoListener listener);

    void mDownload(OnUserinfoListener onMainListener, String url, FileProgressListener listener);

}
