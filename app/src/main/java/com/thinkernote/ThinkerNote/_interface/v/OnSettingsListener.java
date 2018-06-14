package com.thinkernote.ThinkerNote._interface.v;

/**
 * v层
 */
public interface OnSettingsListener {
    void onDefaultFolderSuccess(Object obj,long pid);

    void onDefaultFoldeFailed(String msg, Exception e);

    void onVerifyEmailSuccess(Object obj);

    void onVerifyEmailFailed(String msg, Exception e);

    void onProfileSuccess(Object obj);

    void onProfileFailed(String msg, Exception e);

}
