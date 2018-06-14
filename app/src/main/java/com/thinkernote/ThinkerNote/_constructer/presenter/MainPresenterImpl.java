package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.module.MainModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.RegistModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IMainModule;
import com.thinkernote.ThinkerNote._interface.m.IRegistModule;
import com.thinkernote.ThinkerNote._interface.p.IMainPresener;
import com.thinkernote.ThinkerNote._interface.p.IRegistPresener;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;

/**
 * 注册 p层 具体实现
 */
public class MainPresenterImpl implements IMainPresener, OnMainListener {
    private Context context;
    private OnMainListener onView;

    //p层调用M层方法
    private IMainModule module;

    public MainPresenterImpl(Context context, OnMainListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new MainModuleImpl(context);
    }

    //============================p层重写，用于调用m层方法============================
    @Override
    public void pUpgrade(String home) {
        module.mUpgrade(this);
    }

    @Override
    public void pSynchronizeData() {
        module.mSynchronizeData(this);
    }


    //==========================结果回调==============================

    @Override
    public void onUpgradeSuccess(Object obj) {
        onView.onUpgradeSuccess(obj);
    }

    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        onView.onUpgradeFailed(msg, e);
    }

    @Override
    public void onSynchronizeSuccess(Object obj) {
        onView.onSynchronizeSuccess(obj);
    }

    @Override
    public void onSynchronizeFailed(String msg, Exception e) {
        onView.onSynchronizeFailed(msg, e);
    }
}
