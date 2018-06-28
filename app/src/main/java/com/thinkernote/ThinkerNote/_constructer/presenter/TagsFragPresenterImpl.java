package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.module.LogModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.TagsFragModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.ILogModule;
import com.thinkernote.ThinkerNote._interface.m.ITagFragModule;
import com.thinkernote.ThinkerNote._interface.p.ILogPresenter;
import com.thinkernote.ThinkerNote._interface.p.ITagFragPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;
import com.thinkernote.ThinkerNote._interface.v.OnTagsFragListener;

/**
 * 登录 p层 具体实现
 */
public class TagsFragPresenterImpl implements ITagFragPresenter, OnTagsFragListener {
    private Context context;
    private OnTagsFragListener onView;
    //p层调用M层方法
    private ITagFragModule module;

    public TagsFragPresenterImpl(Context context, OnTagsFragListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new TagsFragModuleImpl(context);
    }

    //============================p层重写，用于调用m层方法============================

    @Override
    public void pTagList() {
        module.mGetTagList(this);
    }

    //==========================结果回调==============================

    @Override
    public void onGetTagListSuccess(Object obj) {
        onView.onGetTagListSuccess(obj);
    }

    @Override
    public void onGetTagListFailed(String msg, Exception e) {
        onView.onGetTagListFailed(msg, e);
    }
    //========================================================
}
