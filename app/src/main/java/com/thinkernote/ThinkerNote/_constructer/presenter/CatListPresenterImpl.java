package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.module.CatListModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.LogModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.ICatListModule;
import com.thinkernote.ThinkerNote._interface.m.ILogModule;
import com.thinkernote.ThinkerNote._interface.p.ICatInfoPresener;
import com.thinkernote.ThinkerNote._interface.p.ICatListPresener;
import com.thinkernote.ThinkerNote._interface.p.ILogPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCatListListener;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;

/**
 *
 */
public class CatListPresenterImpl implements ICatListPresener, OnCatListListener {
    private Context context;
    private OnCatListListener onView;
    //p层调用M层方法
    private ICatListModule module;

    public CatListPresenterImpl(Context context, OnCatListListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new CatListModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================
    @Override
    public void pParentFodler() {
        module.mParentFolder(this);
    }

    @Override
    public void pGetFolderByFolderId(long catId) {
        module.mGetFolderByFolderId(this, catId);
    }

    @Override
    public void pFolderMove(long catId, long selectId) {
        module.mmoveFolder(this, catId, selectId);
    }


    //==========================结果回调==============================

    @Override
    public void onParentFolderSuccess(Object obj) {
        onView.onParentFolderSuccess(obj);
    }

    @Override
    public void onParentFolderFailed(String msg, Exception e) {
        onView.onParentFolderFailed(msg, e);
    }

    @Override
    public void onGetFoldersByFolderIdSuccess(Object obj, long catId) {
        onView.onGetFoldersByFolderIdSuccess(obj,catId);
    }

    @Override
    public void onGetFoldersByFolderIdFailed(String msg, Exception e) {
        onView.onGetFoldersByFolderIdFailed(msg, e);
    }


    @Override
    public void onFolderMoveSuccess(Object obj) {
        onView.onFolderMoveSuccess(obj);
    }

    @Override
    public void onFolderMoveFailed(String msg, Exception e) {
        onView.onFolderMoveFailed(msg, e);
    }


}
