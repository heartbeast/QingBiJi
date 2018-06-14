package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.module.RegistModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IRegistModule;
import com.thinkernote.ThinkerNote._interface.p.IRegistPresener;
import com.thinkernote.ThinkerNote._interface.p.IUserInfoPresener;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;
import com.thinkernote.ThinkerNote._interface.v.OnUserinfoListener;

/**
 * 主页--设置界面 p层 具体实现
 */
public class UserInfoPresenterImpl implements IUserInfoPresener, OnUserinfoListener {
    private Context context;
    private OnUserinfoListener onView;
    //p层调用M层方法
    private IRegistModule module;

    public UserInfoPresenterImpl(Context context, OnUserinfoListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new RegistModuleImpl(context);
    }

    //============================p层重写，用于调用m层方法============================
    @Override
    public void getVerifyPic() {
        module.getVerifyPic(this);
    }

    @Override
    public void phoneVerifyCode(String mPhone, String name, String mAnswer, String mNonce, String mHashKey) {
        module.phoneVerifyCode(this, mPhone, name, mAnswer, mNonce, mHashKey);
    }

    @Override
    public void submitRegister(String phone, String ps, String vcode) {
        module.submitRegist(this, phone, ps, vcode);
    }

    @Override
    public void submitForgotPassword(String phone, String ps, String vcode) {
        module.submitForgetPs(this, phone, ps, vcode);
    }

    @Override
    public void autoLogin(String phoneOrEmail, String ps) {
        module.autoLogin(this, phoneOrEmail, ps);
    }

    @Override
    public void bindPhone(int mUserType, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone,String vcode) {
        String sign = "access_token=" + accessToken + "&bid=" + bid + "&btype=" + mUserType + "&name=" + name + "&phone=" + phone + "&refresh_token=" + refreshToken + "&stamp=" + currentTime + "&vcode=" + vcode + "qingbiji";

        module.bindPhone(this, mUserType, bid, name, accessToken, refreshToken, currentTime, phone, vcode,TNUtils.toMd5(sign).toLowerCase());
    }


    //==========================结果回调==============================
    @Override
    public void onPicSuccess(Object obj) {
        onView.onPicSuccess(obj);
    }

    @Override
    public void onPicFailed(String msg, Exception e) {
        onView.onPicFailed(msg, e);
    }

    @Override
    public void onPhoneVCodeSuccess(Object obj) {
        onView.onPhoneVCodeSuccess(obj);
    }

    @Override
    public void onPhoneVCodeFailed(String msg, Exception e) {
        onView.onPhoneVCodeFailed(msg, e);
    }

    @Override
    public void onSubmitRegistSuccess(Object obj) {
        onView.onSubmitRegistSuccess(obj);
    }

    @Override
    public void onSubmitRegistFailed(String msg, Exception e) {
        onView.onSubmitRegistFailed(msg, e);
    }

    @Override
    public void onSubmitFindPsSuccess(Object obj) {
        onView.onSubmitFindPsSuccess(obj);
    }

    @Override
    public void onSubmitFindPsFailed(String msg, Exception e) {
        onView.onSubmitFindPsFailed(msg, e);
    }


    @Override
    public void onAutoLoginSuccess(Object obj) {
        onView.onAutoLoginSuccess(obj);
    }

    @Override
    public void onAutoLoginFailed(String msg, Exception e) {
        onView.onAutoLoginFailed(msg, e);
    }

    @Override
    public void onBindPhoneSuccess(Object obj) {
        onView.onBindPhoneSuccess(obj);
    }

    @Override
    public void onBindPhoneFailed(String msg, Exception e) {
        onView.onBindPhoneFailed(msg, e);
    }


    //========================================================
}
