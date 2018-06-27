package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.INoteListModule;
import com.thinkernote.ThinkerNote._interface.m.ISplashModule;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnNoteListListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeEditListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * m层 具体实现
 */
public class NoteListModuleImpl implements INoteListModule {

    private Context context;
    private static final String TAG = "SJY";

    public NoteListModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mGetNotelistByFolderId(final OnNoteListListener listener, final long mListDetail, final int mPageNum, final int pageSize, final String sort) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getNoteListByFolderId(mListDetail, mPageNum, TNConst.PAGE_SIZE, sort, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<NoteListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upgrmGetNotelistByFolderIdade--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNotelistByFolderId 异常onError:" + e.toString());
                        listener.onListByFolderIdFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(NoteListBean bean) {
                        MLog.d(TAG, "mGetNotelistByFolderId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onListByFolderIdSuccess(bean, mListDetail, mPageNum, pageSize, sort);
                        } else {
                            listener.onListByFolderIdFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    //1-2
    @Override
    public void mGetNotelistByTagId(final OnNoteListListener listener, final long mListDetail, final int mPageNum, final int pageSize, final String sort) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getNoteListByTagId(mListDetail, mPageNum, TNConst.PAGE_SIZE, sort, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<NoteListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetNotelistByTagId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNotelistByTagId 异常onError:" + e.toString());
                        listener.onNoteListByTagIdFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(NoteListBean bean) {
                        MLog.d(TAG, "mGetNotelistByTagId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onListByFolderIdSuccess(bean, mListDetail, mPageNum, pageSize, sort);
                        } else {
                            listener.onNoteListByTagIdFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    //=========================================syncData===========================================

    @Override
    public void mProfile(OnSynchronizeDataListener listener) {

    }

    @Override
    public void GetFolder(OnSynchronizeDataListener OnSynchronizeDataListener) {

    }

    //
    @Override
    public void mGetTagList(OnSynchronizeDataListener OnSynchronizeDataListener) {

    }

    @Override
    public void mGetFoldersByFolderId(OnSynchronizeDataListener OnSynchronizeDataListenerl, long id, int position, List<AllFolderItemBean> beans) {

    }

    //
    @Override
    public void mFirstFolderAdd(OnSynchronizeDataListener listener, int workPos, int workSize, long catID, int catPos, int flag) {

    }

    @Override
    public void mfolderAdd(OnSynchronizeDataListener listener, int position, int arraySize, String name) {

    }

    //
    @Override
    public void mTagAdd(OnSynchronizeDataListener listener, int position, int arraySize, String name) {

    }

    @Override
    public void mOldNoteAdd(OnSynchronizeDataListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {

    }

    //
    @Override
    public void mUploadOldNotePic(OnSynchronizeDataListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {

    }


    //2-5
    @Override
    public void mNewNote(OnSynchronizeDataListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {

    }

    //2-6
    @Override
    public void mNewNotePic(OnSynchronizeDataListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {

    }


    @Override
    public void mRecoveryNote(OnSynchronizeDataListener listener, long noteID, int position, int arrySize) {

    }

    //
    @Override
    public void mRecoveryNotePic(OnSynchronizeDataListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {

    }

    //
    @Override
    public void mRecoveryNoteAdd(OnSynchronizeDataListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {

    }

    @Override
    public void mDeleteNote(OnSynchronizeDataListener listener, long noteId, int poistion) {

    }

    @Override
    public void mDeleteRealNotes(OnSynchronizeDataListener listener, long noteId, int poistion) {

    }

    @Override
    public void mGetAllNotesId(OnSynchronizeDataListener listener) {

    }

    @Override
    public void mEditNotePic(OnSynchronizeDataListener listener, int cloudsPos, int attrPos, TNNote note) {

    }

    @Override
    public void mEditNote(OnSynchronizeDataListener listener, int position, TNNote tnNote) {

    }

    @Override
    public void mGetNoteByNoteId(OnSynchronizeDataListener listener, int position, long id, boolean is12) {

    }

    @Override
    public void mGetAllTrashNoteIds(OnSynchronizeDataListener listener) {

    }

    //=========================================syncEdit===========================================

    @Override
    public void mNewNote(OnSynchronizeEditListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {

    }

    @Override
    public void mNewNotePic(OnSynchronizeEditListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {

    }

    @Override
    public void mRecoveryNote(OnSynchronizeEditListener listener, long noteID, int position, int arrySize) {

    }

    @Override
    public void mRecoveryNotePic(OnSynchronizeEditListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {

    }

    @Override
    public void mRecoveryNoteAdd(OnSynchronizeEditListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {

    }

    @Override
    public void mDeleteNote(OnSynchronizeEditListener listener, long noteId, int poistion) {

    }

    @Override
    public void mDeleteRealNotes(OnSynchronizeEditListener listener, long noteId, int poistion) {

    }

    @Override
    public void mGetAllNotesId(OnSynchronizeEditListener listener) {

    }

    @Override
    public void mEditNotePic(OnSynchronizeEditListener listener, int cloudsPos, int attrPos, TNNote note) {

    }

    @Override
    public void mEditNote(OnSynchronizeEditListener listener, int position, TNNote tnNote) {

    }

    @Override
    public void mGetNoteByNoteId(OnSynchronizeEditListener listener, int position, long id, boolean is12) {

    }

    @Override
    public void mGetAllTrashNoteIds(OnSynchronizeEditListener listener) {

    }
}
