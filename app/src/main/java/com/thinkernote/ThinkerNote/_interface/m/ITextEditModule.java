package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnTextEditListener;
import com.thinkernote.ThinkerNote._interface.v.OnUserinfoListener;

/**
 *  m层interface
 */
public interface ITextEditModule {

    void pFolderAdd(OnTextEditListener listener, long pid,String text);

    void pFolderRename(OnTextEditListener listener, long pid,String text);

    void pTagAdd(OnTextEditListener listener, String text);

    void pTagRename(OnTextEditListener listener, long pid,String text);

}
