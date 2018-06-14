package com.thinkernote.ThinkerNote._interface.v;

/**
 *  v层
 */
public interface OnTextEditListener {
    void onFolderAddSuccess(Object obj);

    void onFolderAddFailed(String msg, Exception e);

    void onFolderRenameSuccess(Object obj);

    void onFolderRenameFailed(String msg, Exception e);

    void onTagAddSuccess(Object obj);

    void onTagAddFailed(String msg, Exception e);

    void onTagRenameSuccess(Object obj);

    void onTagRenameFailed(String msg, Exception e);
}
