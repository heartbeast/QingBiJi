package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.TagListModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.ITagListModule;
import com.thinkernote.ThinkerNote._interface.p.ITagListPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnTagListListener;

/**
 * p层 具体实现
 */
public class TagListPresenterImpl implements ITagListPresenter, OnTagListListener {
    private Context context;
    private OnTagListListener onView;
    //p层调用M层方法
    private ITagListModule module;

    public TagListPresenterImpl(Context context, OnTagListListener logListener) {
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
    public void onTagListSuccess(Object obj) {
        onView.onTagListSuccess(obj);
    }

    @Override
    public void onTagListFailed(String msg, Exception e) {
        onView.onTagListFailed(msg, e);
    }
}
