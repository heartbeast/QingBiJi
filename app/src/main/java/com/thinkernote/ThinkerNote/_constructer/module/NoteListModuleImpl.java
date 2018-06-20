package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.INoteListModule;
import com.thinkernote.ThinkerNote._interface.m.ISplashModule;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnNoteListListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

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
        MyHttpService.GETBuilder.getHttpServer()//固定样式，可自定义其他网络
                .getNoteListByFolderId(mListDetail,mPageNum,TNConst.PAGE_SIZE,sort)//接口方法
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
                            listener.onListByFolderIdSuccess(bean,mListDetail,mPageNum,pageSize,sort);
                        } else {
                            listener.onListByFolderIdFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    @Override
    public void mGetNotelistByTagId(final OnNoteListListener listener, final long mListDetail, final int mPageNum, final int pageSize, final String sort) {

        MyHttpService.GETBuilder.getHttpServer()//固定样式，可自定义其他网络
                .getNoteListByTagId(mListDetail,mPageNum,TNConst.PAGE_SIZE,sort)//接口方法
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
                            listener.onListByFolderIdSuccess(bean,mListDetail,mPageNum,pageSize,sort);
                        } else {
                            listener.onNoteListByTagIdFailed(bean.getMessage(), null);
                        }
                    }
                });
    }
}
