package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.TagListModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.UserInfoModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.ITagListModule;
import com.thinkernote.ThinkerNote._interface.m.IUserInfoModule;
import com.thinkernote.ThinkerNote._interface.p.ITagListPresener;
import com.thinkernote.ThinkerNote._interface.p.IUserInfoPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnUserinfoListener;

/**
 * 主页--设置界面 p层 具体实现
 */
public class TagListPresenterImpl implements ITagListPresener, OnCommonListener {
    private Context context;
    private OnCommonListener onView;
    //p层调用M层方法
    private ITagListModule module;

    public TagListPresenterImpl(Context context, OnCommonListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new TagListModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================

    @Override
    public void pTagList() {
        module.mTagList(this);
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
