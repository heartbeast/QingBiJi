package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.module.BindAcccountModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.BindPhoneModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IBindAccountModule;
import com.thinkernote.ThinkerNote._interface.m.IBindPhoneModule;
import com.thinkernote.ThinkerNote._interface.p.IBindAccountPresener;
import com.thinkernote.ThinkerNote._interface.p.IBindPhonePresener;
import com.thinkernote.ThinkerNote._interface.v.OnBindAccountListener;
import com.thinkernote.ThinkerNote._interface.v.OnBindPhoneListener;

/**
 * 绑定新手机 p层 具体实现
 */
public class BindPhonePresenterImpl implements IBindPhonePresener, OnBindPhoneListener {
    private Context context;
    private OnBindPhoneListener onView;
    //p层调用M层方法
    private IBindPhoneModule module;

    public BindPhonePresenterImpl(Context context, OnBindPhoneListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new BindPhoneModuleImpl(context);
    }
    //============================p层重写，用于调用m层方法============================

    @Override
    public void pVcode(String phone, String name,String answer,String mNonce,String mHashKey) {
        module.mVcode(this,phone,name,answer,mNonce,mHashKey);
    }

    @Override
    public void pVerifyPic() {
        module. mVerifyPic(this);
    }

    @Override
    public void pRrofile() {
        module.mGetUserInfo(this);
    }

    @Override
    public void pSubmit(String phone, String vcode, String ps) {
        module.mBindNewPhone(this, phone, vcode, ps);
    }


    //==========================结果回调==============================


    @Override
    public void onVerifyPicSuccess(Object obj) {
        onView.onVerifyPicSuccess(obj);
    }

    @Override
    public void onVerifyPicFailed(String msg, Exception e) {
        onView.onVerifyPicFailed(msg, e);
    }

    @Override
    public void onVcodeSuccess(Object obj) {
        onView.onVcodeSuccess(obj);
    }

    @Override
    public void onVcodeFailed(String msg, Exception e) {
        onView.onVcodeFailed(msg,e);
    }

    @Override
    public void onBindSuccess(Object obj,String phone) {
        onView.onBindSuccess(obj,phone);
    }

    @Override
    public void onBindFailed(String msg, Exception e) {
        onView.onBindFailed(msg, e);
    }

    @Override
    public void onProfileSuccess(Object obj) {
        onView.onProfileSuccess(obj);
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        onView.onProfileFailed(msg,e);
    }


}
