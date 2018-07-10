package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnReportListener;

import java.io.File;
import java.util.List;

/**
 *  m层interface
 */
public interface IReportModule {

    void mFeedBackPic(OnReportListener listener, File fileList, String content, String email);

    void mFeedBack(OnReportListener listener, String content, long pid, String email);

}
