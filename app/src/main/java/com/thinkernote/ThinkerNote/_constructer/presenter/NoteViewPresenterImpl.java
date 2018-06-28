package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.NoteViewModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.INoteViewModule;
import com.thinkernote.ThinkerNote._interface.p.INoteViewPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewListener;

/**
 * 笔记详情 p层 具体实现
 */
public class NoteViewPresenterImpl implements INoteViewPresenter, OnNoteViewListener {
    private Context context;
    private OnNoteViewListener onView;
    //p层调用M层方法
    private INoteViewModule
            module;

    public NoteViewPresenterImpl(Context context, OnNoteViewListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new NoteViewModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================

    @Override
    public void pGetNote(long noteID) {
        module.mGetNote(this, noteID);
    }
    //==========================结果回调==============================


    @Override
    public void onGetNoteSuccess(Object obj) {
        onView.onGetNoteSuccess(obj);
    }

    @Override
    public void onGetNoteFailed(String msg, Exception e) {
        onView.onGetNoteFailed(msg, e);
    }
}
