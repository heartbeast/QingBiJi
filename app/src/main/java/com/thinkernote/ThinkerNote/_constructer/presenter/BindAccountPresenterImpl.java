package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.module.BindAcccountModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.LogModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IBindAccountModule;
import com.thinkernote.ThinkerNote._interface.m.ILogModule;
import com.thinkernote.ThinkerNote._interface.p.IBindAccountPresener;
import com.thinkernote.ThinkerNote._interface.p.ILogPresener;
import com.thinkernote.ThinkerNote._interface.v.OnBindAccountListener;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;

/**
 * 登录 p层 具体实现
 */
public class BindAccountPresenterImpl implements IBindAccountPresener, OnBindAccountListener {
    private Context context;
    private OnBindAccountListener onView;
    //p层调用M层方法
    private IBindAccountModule module;

    public BindAccountPresenterImpl(Context context, OnBindAccountListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new BindAcccountModuleImpl(context);
    }

    //============================p层重写，用于调用m层方法============================

    @Override
    public void pPostVerifyCode(String phone, String t) {
        module.mVerifyCode(this, phone, t);
    }

    @Override
    public void pBind(int btype, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone, String vcode, String bindName) {
        String sign = "access_token=" + accessToken + "&bid=" + bid + "&btype=" + btype + "&name=" + name + "&phone=" + phone + "&refresh_token=" + refreshToken + "&stamp=" + currentTime + "&vcode=" + vcode + "qingbiji";
        module.mLoginBind(this, btype, bid, name, accessToken, refreshToken, currentTime, phone, vcode, TNUtils.toMd5(sign).toLowerCase());
    }

    @Override
    public void pAutoLogAfterBind(int btype, String bid, String name, String accessToken, String refreshToken, long currentTime, String bindName) {
        String sign = "bid=" + bid + "&btype=" + btype + "&stamp=" + currentTime + "qingbiji";
        module.autoLoginAferBind(this, btype, bid, currentTime, sign);
    }

    @Override
    public void pProfile() {
        module.mProfile(this);
    }

    //==========================结果回调==============================
    @Override
    public void onVerifyCodeSuccess(Object obj) {
        onView.onVerifyCodeSuccess(obj);
    }

    @Override
    public void onVerifyCodeFailed(String msg, Exception e) {
        onView.onVerifyCodeFailed(msg, e);
    }

    @Override
    public void onBindSuccess(Object obj) {
        onView.onBindSuccess(obj);
    }

    @Override
    public void onBindFailed(String msg, Exception e) {
        onView.onBindFailed(msg, e);
    }

    @Override
    public void onAutoLogSuccess(Object obj) {
        onView.onAutoLogSuccess(obj);
    }

    @Override
    public void onAutoLogFailed(String msg, Exception e) {
        onView.onAutoLogFailed(msg, e);
    }

    @Override
    public void onLogProfileSuccess(Object obj) {
        onView.onLogProfileSuccess(obj);
    }

    @Override
    public void onLogProfileFailed(String msg, Exception e) {
        onView.onLogProfileFailed(msg, e);
    }
}
