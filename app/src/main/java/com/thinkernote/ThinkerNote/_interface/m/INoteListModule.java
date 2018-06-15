package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnNoteListListener;

/**
 *  m层interface
 */
public interface INoteListModule {
    void mGetNotelistByFolderId(OnNoteListListener listener, long mListDetail, int mPageNum, int size, String sort);
    void mGetNotelistByTagId(OnNoteListListener listener, long mListDetail, int mPageNum, int size, String sort);
}
