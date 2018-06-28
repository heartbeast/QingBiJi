package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.PagerModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IPagerModule;
import com.thinkernote.ThinkerNote._interface.p.IPagerPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnPagerListener;

/**
 * 我的笔记 p层 具体实现
 */
public class PagerPresenterImpl implements IPagerPresenter, OnPagerListener {
    private Context context;
    private OnPagerListener onView;
    //p层调用M层方法
    private IPagerModule module;

    public PagerPresenterImpl(Context context, OnPagerListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new PagerModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================

    @Override
    public void pSetDefaultFolder(long tagID) {
        module.mSetDefaultFolder(this, tagID);
    }

    @Override
    public void pDeleteTag(long tagID) {
        module.mDeleteTag(this, tagID);
    }

    @Override
    public void pDeleteFolder(long folderID) {
        module.mDeleteFolder(this, folderID );
    }

    //==========================结果回调==============================


    @Override
    public void onDefaultFolderSuccess(Object obj) {
        onView.onDefaultFolderSuccess(obj);
    }

    @Override
    public void onDefaultFolderFailed(String msg, Exception e) {
        onView.onDefaultFolderFailed(msg, e);
    }

    @Override
    public void onFolderDeleteSuccess(Object obj) {
        onView.onFolderDeleteSuccess(obj);
    }

    @Override
    public void onFolderDeleteFailed(String msg, Exception e) {
        onView.onFolderDeleteFailed(msg, e);
    }

    @Override
    public void onTagDeleteSuccess(Object obj,long tagID) {
        onView.onTagDeleteSuccess(obj,tagID);
    }

    @Override
    public void onTagDeleteFailed(String msg, Exception e) {
        onView.onTagDeleteFailed(msg, e);
    }


}
