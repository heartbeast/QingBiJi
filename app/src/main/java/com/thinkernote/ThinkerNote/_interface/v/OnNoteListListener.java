package com.thinkernote.ThinkerNote._interface.v;

/**
 * v层 只有一个接口 通用回调
 */
public interface OnNoteListListener {
    void onListByFolderIdSuccess(Object obj,long mListDetail, final int mPageNum, final int pageSize, String sort);

    void onListByFolderIdFailed(String msg, Exception e);

    void onNoteListByTagIdSuccess(Object obj,long mListDetail, final int mPageNum, final int pageSize, String sort);

    void onNoteListByTagIdFailed(String msg, Exception e);

}
