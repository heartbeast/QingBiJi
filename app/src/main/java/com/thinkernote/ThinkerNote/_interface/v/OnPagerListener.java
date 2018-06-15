package com.thinkernote.ThinkerNote._interface.v;

/**
 * 我的笔记 v层 只有一个接口 通用回调
 */
public interface OnPagerListener {
    void onDefaultFolderSuccess(Object obj);

    void onDefaultFolderFailed(String msg, Exception e);

    void onFolderDeleteSuccess(Object obj);

    void onFolderDeleteFailed(String msg, Exception e);

    void onTagDeleteSuccess(Object obj,long tagID);

    void onTagDeleteFailed(String msg, Exception e);

}
