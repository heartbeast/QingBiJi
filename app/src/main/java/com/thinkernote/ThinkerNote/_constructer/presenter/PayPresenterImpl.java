package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.PayModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.SplashModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IPayModule;
import com.thinkernote.ThinkerNote._interface.m.ISplashModule;
import com.thinkernote.ThinkerNote._interface.p.IPayPresener;
import com.thinkernote.ThinkerNote._interface.p.ISplashPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnPayListener;

/**
 * p层 具体实现
 */
public class PayPresenterImpl implements IPayPresener, OnPayListener {
    private Context context;
    private OnPayListener onView;
    //p层调用M层方法
    private IPayModule module;

    public PayPresenterImpl(Context context, OnPayListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new PayModuleImpl(context);
    }

    //============================p层重写，用于调用m层方法============================


    @Override
    public void pAlipay(String mAmount, String mType) {
        module.mAlipay(this, mAmount, mType);
    }

    @Override
    public void pWxpay(String mAmount, String mType) {
        module.mWxpay(this, mAmount, mType);
    }


    //==========================结果回调==============================


    @Override
    public void onAlipaySuccess(Object obj) {
        onView.onAlipaySuccess(obj);
    }

    @Override
    public void onAlipayFailed(String msg, Exception e) {
        onView.onAlipayFailed(msg, e);
    }

    @Override
    public void onWxpaySuccess(Object obj) {
        onView.onWxpaySuccess(obj);
    }

    @Override
    public void onWxpayFailed(String msg, Exception e) {
        onView.onWxpayFailed(msg, e);
    }
}
