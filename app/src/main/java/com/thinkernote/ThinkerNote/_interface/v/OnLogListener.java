package com.thinkernote.ThinkerNote._interface.v;

/**
 * 登录 v层
 */
public interface OnLogListener {
    void onLoginNormalSuccess(Object obj);

    void onLoginNormalFailed(String msg,Exception e);

    void onLoginQQSuccess(Object obj);

    void onLoginQQFailed(String msg,Exception e, String bid,long currentTime,String accessToken,String refreshToken,String name);

    void onLoginWechatSuccess(Object obj);

    void onLoginWechatFailed(String msg,Exception e, String bid,long currentTime,String accessToken,String refreshToken,String name);

    void onLoginSinaSuccess(Object obj);

    void onLoginSinaFailed(String msg,Exception e, String bid,long currentTime,String accessToken,String refreshToken,String name);

    void onLogProfileSuccess(Object obj);

    void onLogProfileFailed(String msg,Exception e);
}
