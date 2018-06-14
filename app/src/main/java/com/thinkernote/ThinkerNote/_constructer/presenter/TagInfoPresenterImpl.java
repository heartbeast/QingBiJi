package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.TagInfoModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.TagListModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.ITagInfoModule;
import com.thinkernote.ThinkerNote._interface.m.ITagListModule;
import com.thinkernote.ThinkerNote._interface.p.ITagInfoPresener;
import com.thinkernote.ThinkerNote._interface.p.ITagListPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnTagInfoListener;

/**
 *  p层 具体实现
 */
public class TagInfoPresenterImpl implements ITagInfoPresener, OnTagInfoListener {
    private Context context;
    private OnTagInfoListener onView;
    //p层调用M层方法
    private ITagInfoModule module;

    public TagInfoPresenterImpl(Context context, OnTagInfoListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new TagInfoModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================
    @Override
    public void pTagDelete(long pid) {
        module.mTagDelete(this,pid);
    }

    //==========================结果回调==============================

    @Override
    public void onSuccess(Object obj,long pid) {
        onView.onSuccess(obj,pid);
    }

    @Override
    public void onFailed(String msg, Exception e) {
        onView.onFailed(msg, e);
    }


}
