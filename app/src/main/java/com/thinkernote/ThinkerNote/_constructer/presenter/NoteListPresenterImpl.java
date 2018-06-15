package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.NoteListModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.SplashModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.INoteListModule;
import com.thinkernote.ThinkerNote._interface.m.ISplashModule;
import com.thinkernote.ThinkerNote._interface.p.INoteListPresener;
import com.thinkernote.ThinkerNote._interface.p.ISplashPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnNoteListListener;

/**
 * p层 具体实现
 */
public class NoteListPresenterImpl implements INoteListPresener, OnNoteListListener {
    private Context context;
    private OnNoteListListener onView;
    //p层调用M层方法
    private INoteListModule module;

    public NoteListPresenterImpl(Context context, OnNoteListListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new NoteListModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================

    @Override
    public void pGetNoteListByFolderID(long mListDetail, int mPageNum, int size, String sort) {
        module.mGetNotelistByFolderId(this, mListDetail, mPageNum, size, sort);
    }

    @Override
    public void pGetNoteListByTagID(long mListDetail, int mPageNum, int size, String sort) {
        module.mGetNotelistByTagId(this,mListDetail,mPageNum,size,sort);
    }
    //==========================结果回调==============================


    @Override
    public void onListByFolderIdSuccess(Object obj, long tagId, int mPageNum, int pageSize, String sort) {
        onView.onListByFolderIdSuccess(obj,tagId,mPageNum,pageSize,sort);
    }

    @Override
    public void onListByFolderIdFailed(String msg, Exception e) {
        onView.onListByFolderIdFailed(msg, e);
    }

    @Override
    public void onNoteListByTagIdSuccess(Object obj, long tagId, int mPageNum, int pageSize, String sort) {
        onView.onNoteListByTagIdSuccess(obj,tagId,mPageNum,pageSize,sort);
    }

    @Override
    public void onNoteListByTagIdFailed(String msg, Exception e) {
        onView.onNoteListByTagIdFailed(msg, e);
    }


}
