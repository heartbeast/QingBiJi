package com.thinkernote.ThinkerNote._interface.p;

import java.util.List;

/**
 *  p层interface
 */
public interface IReportPresener {
    void pFeedBackPic(List<String> mFiles,String content,String email);
    void pFeedBack(String content, long pid,String email);

}
