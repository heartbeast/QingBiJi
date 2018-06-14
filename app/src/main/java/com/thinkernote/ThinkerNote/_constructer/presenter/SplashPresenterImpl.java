package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.SplashModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.TagInfoModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.ISplashModule;
import com.thinkernote.ThinkerNote._interface.m.ITagInfoModule;
import com.thinkernote.ThinkerNote._interface.p.ISplashPresener;
import com.thinkernote.ThinkerNote._interface.p.ITagInfoPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnTagInfoListener;

/**
 *  p层 具体实现
 */
public class SplashPresenterImpl implements ISplashPresener, OnCommonListener {
    private Context context;
    private OnCommonListener onView;
    //p层调用M层方法
    private ISplashModule module;

    public SplashPresenterImpl(Context context, OnCommonListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new SplashModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================

    @Override
    public void plogin(String name, String ps) {
        module.mLogin(this,name,ps);

    }
    //==========================结果回调==============================

    @Override
    public void onSuccess(Object obj) {
        onView.onSuccess(obj);
    }

    @Override
    public void onFailed(String msg, Exception e) {
        onView.onFailed(msg, e);
    }



}
