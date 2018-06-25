package com.thinkernote.ThinkerNote._interface.v;

/**
 *  v层 只有一个接口 通用回调
 */
public interface OnTagListListener {
    void onTagListSuccess(Object obj);

    void onTagListFailed(String msg, Exception e);

}
