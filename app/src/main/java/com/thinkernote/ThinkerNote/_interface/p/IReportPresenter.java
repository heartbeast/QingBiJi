package com.thinkernote.ThinkerNote._interface.p;

import java.io.File;
import java.util.List;

/**
 *  p层interface
 */
public interface IReportPresenter {
    void pFeedBackPic(File mFiles, String content, String email);
    void pFeedBack(String content, long pid,String email);

}
