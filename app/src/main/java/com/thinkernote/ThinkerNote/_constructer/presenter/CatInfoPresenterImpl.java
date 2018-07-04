package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.CatInfoModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.ICatInfoModule;
import com.thinkernote.ThinkerNote._interface.p.ICatInfoPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnCatInfoListener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;

/**
 * 登录 p层 具体实现
 */
public class CatInfoPresenterImpl implements ICatInfoPresenter, OnCatInfoListener {
    private Context context;
    private OnCatInfoListener onView;
    //p层调用M层方法
    private ICatInfoModule module;

    public CatInfoPresenterImpl(Context context, OnCatInfoListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new CatInfoModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================
    @Override
    public void pSetDefaultFolder(long catId) {
        module.mSetDefaultFolder(this, catId);
    }

    @Override
    public void pDeleteCat(long catId) {
        module.mCatDelete(this, catId);
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

    @Override
    public void onDeleteFolderSuccess(Object obj, long catId) {
        onView.onDeleteFolderSuccess(obj, catId);
    }

    @Override
    public void onDeleteFolderFailed(String msg, Exception e) {
        onView.onDeleteFolderFailed(msg, e);
    }
}
