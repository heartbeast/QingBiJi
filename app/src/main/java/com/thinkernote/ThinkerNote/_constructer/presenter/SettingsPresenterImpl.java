package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.SettingsModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.SplashModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.ISettingsModule;
import com.thinkernote.ThinkerNote._interface.m.ISplashModule;
import com.thinkernote.ThinkerNote._interface.p.ISettingsPresener;
import com.thinkernote.ThinkerNote._interface.p.ISplashPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnSettingsListener;

/**
 * p层 具体实现
 */
public class SettingsPresenterImpl implements ISettingsPresener, OnSettingsListener {
    private Context context;
    private OnSettingsListener onView;
    //p层调用M层方法
    private ISettingsModule module;

    public SettingsPresenterImpl(Context context, OnSettingsListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new SettingsModuleImpl(context);
    }

    //============================p层重写，用于调用m层方法============================
    @Override
    public void pGetProfile() {
        module.mgetProfile(this);

    }

    @Override
    public void verifyEmail() {
        module.mVerifyEmail(this);
    }

    @Override
    public void setDefaultFolder(long pid) {
        module.mSetDefaultFolder(this,pid);
    }

    //==========================结果回调==============================


    @Override
    public void onDefaultFolderSuccess(Object obj,long pid) {
        onView.onDefaultFolderSuccess(obj,pid);
    }

    @Override
    public void onDefaultFoldeFailed(String msg, Exception e) {
        onView.onDefaultFoldeFailed(msg, e);
    }

    @Override
    public void onVerifyEmailSuccess(Object obj) {
        onView.onVerifyEmailSuccess(obj);
    }

    @Override
    public void onVerifyEmailFailed(String msg, Exception e) {
        onView.onVerifyEmailFailed(msg, e);
    }

    @Override
    public void onProfileSuccess(Object obj) {
        onView.onProfileSuccess(obj);
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        onView.onProfileFailed(msg, e);
    }


}
