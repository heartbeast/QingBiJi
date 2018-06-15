package com.thinkernote.ThinkerNote._interface.p;

/**
 *  p层interface
 */
public interface INoteListPresener {
    void pGetNoteListByFolderID(long mListDetail,int mPageNum,int size,String sort);
    void pGetNoteListByTagID(long mListDetail,int mPageNum,int size,String sort);

}
