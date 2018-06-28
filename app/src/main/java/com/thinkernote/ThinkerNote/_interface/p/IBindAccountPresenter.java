package com.thinkernote.ThinkerNote._interface.p;

/**
 * 登录使用  绑定手机号 p层interface
 */
public interface IBindAccountPresenter {
    void pPostVerifyCode(String phone, String t);

    void pBind(int btype, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone, String vcode,String bindName);
    void pAutoLogAfterBind(int btype, String bid, String name, String accessToken, String refreshToken, long currentTime,String bindName);
    void pProfile();
}
