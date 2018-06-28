package com.thinkernote.ThinkerNote._interface.v;

/**
 *  v层 只有一个接口 通用回调
 */
public interface OnCatFragListener {
    void onGetParentFolderSuccess(Object obj);

    void onGetParentFolderFailed(String msg, Exception e);

}
