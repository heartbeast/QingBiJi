package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.module.FindPsModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.LogModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IFindPsModule;
import com.thinkernote.ThinkerNote._interface.m.ILogModule;
import com.thinkernote.ThinkerNote._interface.p.IFindPsPresener;
import com.thinkernote.ThinkerNote._interface.p.ILogPresener;
import com.thinkernote.ThinkerNote._interface.v.OnFindPsListener;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;

/**
 * 登录 p层 具体实现
 */
public class FindPsPresenterImpl implements IFindPsPresener,OnFindPsListener {
    private Context context;
    private OnFindPsListener onView;
    //p层调用M层方法
    private IFindPsModule module;

    public FindPsPresenterImpl(Context context, OnFindPsListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new FindPsModuleImpl(context);
    }

    //============================p层重写，用于调用m层方法============================
    @Override
    public void getVerifyPic() {
        module.getVerifyPic(this);
    }

    @Override
    public void phoneVerifyCode(String mPhone,String name,String mAnswer,String mNonce,String mHashKey) {
        module.phoneVerifyCode(this,mPhone,name,mAnswer,mNonce,mHashKey);
    }

    @Override
    public void mailVerifyCode(String mEmail,String name) {
        module.mailVerifyCode(this,mEmail,name);
    }

    @Override
    public void submit(String phone,String ps,String vcode) {
        module.submit(this,phone,ps,vcode);
    }

    @Override
    public void autoLogin(String phoneOrEmail, String ps) {
        module.autoLogin(this,phoneOrEmail,ps);
    }

    //==========================结果回调==============================
    @Override
    public void onPicSuccess(Object obj) {
        onView.onPicSuccess(obj);
    }

    @Override
    public void onPicFailed(String msg, Exception e) {
        onView.onPicFailed(msg,e);
    }

    @Override
    public void onPhoneVCodeSuccess(Object obj) {
        onView.onPhoneVCodeSuccess(obj);
    }

    @Override
    public void onPhoneVCodeFailed(String msg, Exception e) {
        onView.onPhoneVCodeFailed(msg,e);
    }

    @Override
    public void onMailVCodeSuccess(Object obj) {
        onView.onMailVCodeSuccess(obj);
    }

    @Override
    public void onMailVCodetFailed(String msg, Exception e) {
        onView.onMailVCodetFailed(msg,e);
    }

    @Override
    public void onSubmitSuccess(Object obj) {
        onView.onSubmitSuccess(obj);
    }

    @Override
    public void onSubmitFailed(String msg, Exception e) {
        onView.onSubmitFailed(msg,e);
    }

    @Override
    public void onAutoLoginSuccess(Object obj) {
        onView.onAutoLoginSuccess(obj);
    }

    @Override
    public void onAutoLoginFailed(String msg, Exception e) {
        onView.onAutoLoginFailed(msg,e);
    }


    //========================================================
}
