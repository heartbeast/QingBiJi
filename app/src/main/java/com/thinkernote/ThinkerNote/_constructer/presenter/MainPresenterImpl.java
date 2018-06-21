package com.thinkernote.ThinkerNote._constructer.presenter;

import android.app.Dialog;
import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
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

    //更新检查
    @Override
    public void pUpgrade(String home) {
        module.mUpgrade(this);
    }

    //下载
    @Override
    public void pDownload(String url, Dialog dialog) {

    }

    //=第一次登录的同步方法=
    @Override
    public void folderAdd(int position, int arraySize, String folderName) {
        module.mfolderAdd(this, position, arraySize, folderName);
    }

    @Override
    public void tagAdd(int position, int arraySize, String tagName) {
        module.mTagAdd(this, position, arraySize, tagName);
    }


    //=正常登录的同步方法=

    @Override
    public void pUploadOldNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mUploadOldNotePic(this, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    @Override
    public void pOldNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mOldNoteAdd(this, position, arraySize, tnNote, isNewDb, content);
    }

    @Override
    public void pProfile() {

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

    //=第一次登录的回调==

    @Override
    public void onSyncFolderAddSuccess(Object obj, int position, int arraySize) {
        onView.onSyncFolderAddSuccess(obj, position, arraySize);
    }

    @Override
    public void onSyncFolderAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncFolderAddFailed(msg, e, position, arraySize);
    }

    @Override
    public void onSyncTagAddSuccess(Object obj, int position, int arraySize) {
        onView.onSyncTagAddSuccess(obj, position, arraySize);
    }

    @Override
    public void onSyncTagAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncTagAddFailed(msg, e, position, arraySize);
    }


    //===正常登录的回调=
    //pic
    @Override
    public void onSyncOldNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncOldNotePicSuccess(obj, picPos, picArry, notePos, noteArry);
    }

    @Override
    public void onSyncOldNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncOldNotePicFailed(msg, e, picPos, picArry, notePos, noteArry);
    }

    //oldNoteAdd
    @Override
    public void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        onView.onSyncOldNoteAddSuccess(obj, position, arraySize, isNewDb);
    }

    @Override
    public void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncOldNoteAddFailed(msg, e, position, arraySize);
    }

    //
    @Override
    public void onSyncProfileSuccess(Object obj) {

    }

    @Override
    public void onSyncProfileAddFailed(String msg, Exception e) {

    }

}
