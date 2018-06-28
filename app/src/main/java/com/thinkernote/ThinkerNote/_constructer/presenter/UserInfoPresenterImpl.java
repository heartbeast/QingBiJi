package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.UserInfoModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IUserInfoModule;
import com.thinkernote.ThinkerNote._interface.p.IUserInfoPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnUserinfoListener;

/**
 * 主页--设置界面 p层 具体实现
 */
public class UserInfoPresenterImpl implements IUserInfoPresenter, OnUserinfoListener {
    private Context context;
    private OnUserinfoListener onView;
    //p层调用M层方法
    private IUserInfoModule module;

    public UserInfoPresenterImpl(Context context, OnUserinfoListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new UserInfoModuleImpl(context);
    }



    //============================p层重写，用于调用m层方法============================
    @Override
    public void pLogout() {
        module.mLogout(this);
    }

    @Override
    public void pUpgrade() {
        module.mUpgrade(this);
    }


    //==========================结果回调==============================

    @Override
    public void onLogoutSuccess(Object obj) {
        onView.onLogoutSuccess(obj);
    }

    @Override
    public void onLogoutFailed(String msg, Exception e) {
        onView.onLogoutFailed(msg,e);
    }

    @Override
    public void onUpgradeSuccess(Object obj) {
        onView.onUpgradeSuccess(obj);
    }

    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        onView.onUpgradeFailed(msg,e);
    }
}
