package com.thinkernote.ThinkerNote._interface.p;

import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

/**
 * 主页--设置界面 p层interface
 */
public interface IUserInfoPresenter {
    void pLogout();
    void pUpgrade();
    void pDownload(String url, FileProgressListener progressListener);

}
