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
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeEditListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote._interface.v.OnNoteListListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeEditListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.CommonBean3;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.bean.main.TagListBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.MyRxUtils;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    //01-01第一次登录同步 folder
    @Override
    public void mfolderAdd(final OnSynchronizeDataListener listener, final int position, final int arraySize, String name) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncFolderAdd(name, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mfolderAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mfolderAdd 异常onError:" + e.toString());
                        listener.onSyncFolderAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mfolderAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncFolderAddSuccess(bean, position, arraySize);
                        } else {
                            listener.onSyncFolderAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //01-02第一次登录同步 tag
    @Override
    public void mTagAdd(final OnSynchronizeDataListener listener, final int position, final int arraySize, String name) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncTagAdd(name, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mTagAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mTagAdd 异常onError:" + e.toString());
                        listener.onSyncTagAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mTagAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncTagAddSuccess(bean, position, arraySize);
                        } else {
                            listener.onSyncTagAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    // 01-3
    @Override
    public void GetFolder(final OnSynchronizeDataListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncGetFolder(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "GetFolder--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("GetFolder 异常onError:" + e.toString());
                        listener.onSyncGetFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "GetFolder-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncGetFolderSuccess(bean);
                        } else {
                            listener.onSyncGetFolderFailed(bean.getMsg(), null);
                        }
                    }
                });
    }


    //01-4
    @Override
    public void mGetFoldersByFolderId(final OnSynchronizeDataListener listener, final long id, final int position, final List<AllFolderItemBean> beans) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncGetFolderByFodlerId(id, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "GetFoldersByFolderId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("GetFoldersByFolderId 异常onError:" + e.toString());
                        listener.onSyncGetFoldersByFolderIdFailed("异常", new Exception("接口异常！"), id, position, beans);
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "GetFoldersByFolderId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncGetFoldersByFolderIdSuccess(bean, id, position, beans);
                        } else {
                            listener.onSyncGetFoldersByFolderIdFailed(bean.getMsg(), null, id, position, beans);
                        }
                    }

                });
    }


    //1-5
    @Override
    public void mFirstFolderAdd(OnSynchronizeDataListener listener, int workPos, int workSize, long catID, int catPos, int flag) {

    }

    //2-1
    @Override
    public void mProfile(final OnSynchronizeDataListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .LogNormalProfile(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean2<ProfileBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mProFile--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mProFile 异常onError:" + e.toString());
                        listener.onSyncProfileAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean2<ProfileBean> bean) {
                        MLog.d(TAG, "mProFile-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncProfileSuccess(bean.getProfile());
                        } else {
                            listener.onSyncProfileAddFailed(bean.getMsg(), null);
                        }
                    }
                });
    }


    //02-02 OldNotePic
    @Override
    public void mUploadOldNotePic(final OnSynchronizeDataListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
        String filename = tnNoteAtt.attName;
        String filePath = tnNoteAtt.path;
        long fileId = tnNoteAtt.attId;

        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), filePath);//TODO image/jpg
        MultipartBody.Part part = MultipartBody.Part.createFormData("fileName", filename, photoRequestBody);

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncOldNotePic(part, MyRxUtils.toRequestBody(settings.token))//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "OldNotePic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("OldNotePic 异常onError:" + e.toString());
                        listener.onSyncOldNotePicFailed("异常", new Exception("接口异常！"), picPos, picArrySize, notePos, noteArrySize);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "OldNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncOldNotePicSuccess(bean, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
                        } else {
                            listener.onSyncOldNotePicFailed(bean.getMessage(), null, picPos, picArrySize, notePos, noteArrySize);
                        }
                    }

                });
    }


    //02-03 OldNoteAdd
    @Override
    public void mOldNoteAdd(final OnSynchronizeDataListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncOldNoteAdd(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNoteAddBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mfolderAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mfolderAdd 异常onError:" + e.toString());
                        listener.onSyncOldNoteAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(OldNoteAddBean bean) {
                        MLog.d(TAG, "mfolderAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncOldNoteAddSuccess(bean, position, arraySize, isNewDb);
                        } else {
                            listener.onSyncOldNoteAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //2-4
    @Override
    public void mGetTagList(final OnSynchronizeDataListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncTagList(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<TagListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetTagList--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetTagList 异常onError:" + e.toString());
                        listener.onSyncTagListAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(TagListBean bean) {
                        MLog.d(TAG, "mGetTagList-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncTagListSuccess(bean);
                        } else {
                            listener.onSyncTagListAddFailed(bean.getMsg(), null);
                        }
                    }

                });
    }


    //2-5
    @Override
    public void mNewNotePic(final OnSynchronizeDataListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
        String filename = tnNoteAtt.attName;
        String filePath = tnNoteAtt.path;
        long fileId = tnNoteAtt.attId;

        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), filePath);//TODO image/jpg
        MultipartBody.Part part = MultipartBody.Part.createFormData("fileName", filename, photoRequestBody);

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncNewNotePic(part, MyRxUtils.toRequestBody(settings.token))//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mNewNotePic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mNewNotePic 异常onError:" + e.toString());
                        listener.onSyncNewNotePicFailed("异常", new Exception("接口异常！"), picPos, picArrySize, notePos, noteArrySize);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mNewNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncNewNotePicSuccess(bean, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
                        } else {
                            listener.onSyncNewNotePicFailed(bean.getMessage(), null, picPos, picArrySize, notePos, noteArrySize);
                        }
                    }

                });
    }

    //2-6
    @Override
    public void mNewNote(final OnSynchronizeDataListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncNewNoteAdd(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNoteAddBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mNewNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mNewNote 异常onError:" + e.toString());
                        listener.onSyncNewNoteAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(OldNoteAddBean bean) {
                        MLog.d(TAG, "mNewNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncNewNoteAddSuccess(bean, position, arraySize, isNewDb);
                        } else {
                            listener.onSyncNewNoteAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //2-7-1
    @Override
    public void mRecoveryNote(final OnSynchronizeDataListener listener, final long noteID, final int position, int arrySize) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncRecoveryNote(noteID, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNote 异常onError:" + e.toString());
                        listener.onSyncRecoveryFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncRecoverySuccess(bean, noteID, position);
                        } else {
                            listener.onSyncRecoveryFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-7-2
    @Override
    public void mRecoveryNotePic(final OnSynchronizeDataListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
        TNSettings settings = TNSettings.getInstance();
        //token
        // 需要加入到MultipartBody中，而不是作为参数传递
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)//表单类型
                .addFormDataPart("token", settings.token);
        String filename = tnNoteAtt.attName;
        String filePath = tnNoteAtt.path;
        long fileId = tnNoteAtt.attId;
        //file
        File file = new File(filename);
        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);//TODO image/jpg
        builder.addFormDataPart("fileName", file.getName(), photoRequestBody);

        List<MultipartBody.Part> parts = builder.build().parts();


        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncRecoveryNotePic(parts)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNotePic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNotePic 异常onError:" + e.toString());
                        listener.onSyncRecoveryNotePicFailed("异常", new Exception("接口异常！"), picPos, picArrySize, notePos, noteArrySize);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mRecoveryNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncRecoveryNotePicSuccess(bean, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
                        } else {
                            listener.onSyncRecoveryNotePicFailed(bean.getMessage(), null, picPos, picArrySize, notePos, noteArrySize);
                        }
                    }

                });
    }

    //2-7-3
    @Override
    public void mRecoveryNoteAdd(final OnSynchronizeDataListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncRecoveryNoteAdd(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNoteAddBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncRecoveryNoteAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(OldNoteAddBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncRecoveryNoteAddSuccess(bean, position, arraySize, isNewDb);
                        } else {
                            listener.onSyncRecoveryNoteAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //2-8
    @Override
    public void mDeleteNote(final OnSynchronizeDataListener listener, final long noteId, final int poistion) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncDeleteNote(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncDeleteNoteFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncDeleteNoteSuccess(bean, noteId, poistion);
                        } else {
                            listener.onSyncDeleteNoteFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-9
    @Override
    public void mDeleteRealNotes(final OnSynchronizeDataListener listener, final long noteId, final int poistion) {
        TNSettings settings = TNSettings.getInstance();
        //2-9-1
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncDeleteRealNote1(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncDeleteRealNotes1Failed("异常", new Exception("接口异常！"), poistion);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncpDeleteRealNotes1Success(bean, noteId, poistion);
                        } else {
                            listener.onSyncDeleteRealNotes1Failed(bean.getMessage(), null, poistion);
                        }
                    }

                });

        //2-9-2
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncDeleteRealNote2(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncDeleteRealNotes2Failed("异常", new Exception("接口异常！"), poistion);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncDeleteRealNotes2Success(bean, noteId, poistion);
                        } else {
                            listener.onSyncDeleteRealNotes2Failed(bean.getMessage(), null, poistion);
                        }
                    }

                });
    }

    //2-10
    @Override
    public void mGetAllNotesId(final OnSynchronizeDataListener listener) {
        TNSettings settings = TNSettings.getInstance();
        //2-9-1
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncAllNotsId(settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncAllNotesIdAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncAllNotesIdSuccess(bean.getNote_ids());
                        } else {
                            listener.onSyncAllNotesIdAddFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-10-1
    @Override
    public void mEditNotePic(final OnSynchronizeDataListener listener, final int cloudsPos, final int attrPos, final TNNote note) {
        String filename = note.atts.get(attrPos).attName;
        String filePath = note.atts.get(attrPos).path;
        long fileId = note.atts.get(attrPos).attId;

        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), filePath);//TODO image/jpg
        MultipartBody.Part part = MultipartBody.Part.createFormData("fileName", filename, photoRequestBody);

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncEditNotePic(part, MyRxUtils.toRequestBody(settings.token))//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mNewNotePic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mNewNotePic 异常onError:" + e.toString());
                        listener.onSyncEditNotePicFailed("异常", new Exception("接口异常！"), cloudsPos, attrPos, note);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mNewNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncEditNotePicSuccess(bean, cloudsPos, attrPos, note);
                        } else {
                            listener.onSyncEditNotePicFailed(bean.getMessage(), null, cloudsPos, attrPos, note);
                        }
                    }

                });
    }

    //2-11-1
    @Override
    public void mEditNote(final OnSynchronizeDataListener listener, final int position, final TNNote note) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncEditNote(note.noteId, note.title, note.content, note.tagStr, note.catId, note.createTime, note.lastUpdate, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mEditNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mEditNote 异常onError:" + e.toString());
                        listener.onSyncEditNoteAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mEditNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncEditNoteSuccess(bean, position, note);
                        } else {
                            listener.onSyncEditNoteAddFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-11-2
    @Override
    public void mGetNoteByNoteId(final OnSynchronizeDataListener listener, final int position, long id, final boolean is12) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetNoteByNoteId(id, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean3<GetNoteByNoteIdBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetNoteByNoteId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNoteByNoteId 异常onError:" + e.toString());
                        listener.onSyncpGetNoteByNoteIdFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean3<GetNoteByNoteIdBean> bean) {
                        MLog.d(TAG, "mGetNoteByNoteId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncpGetNoteByNoteIdSuccess(bean.getNote(), position, is12);
                        } else {
                            listener.onSyncpGetNoteByNoteIdFailed(bean.getMsg(), null);
                        }
                    }


                });
    }

    //2-12
    @Override
    public void mGetAllTrashNoteIds(final OnSynchronizeDataListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetTrashNoteIds(settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetAllTrashNoteIds--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetAllTrashNoteIds 异常onError:" + e.toString());
                        listener.onSyncpGetAllTrashNoteIdsFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        MLog.d(TAG, "mGetAllTrashNoteIds-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncpGetAllTrashNoteIdsSuccess(bean.getNote_ids());
                        } else {
                            listener.onSyncpGetAllTrashNoteIdsFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //=========================================syncEdit===========================================


    //2-5
    @Override
    public void mNewNotePic(final OnSynchronizeEditListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
        String filename = tnNoteAtt.attName;
        String filePath = tnNoteAtt.path;
        long fileId = tnNoteAtt.attId;

        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), filePath);//TODO image/jpg
        MultipartBody.Part part = MultipartBody.Part.createFormData("fileName", filename, photoRequestBody);

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncNewNotePic(part, MyRxUtils.toRequestBody(settings.token))//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mNewNotePic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mNewNotePic 异常onError:" + e.toString());
                        listener.onSyncNewNotePicFailed2("异常", new Exception("接口异常！"), picPos, picArrySize, notePos, noteArrySize);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mNewNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncNewNotePicSuccess2(bean, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
                        } else {
                            listener.onSyncNewNotePicFailed2(bean.getMessage(), null, picPos, picArrySize, notePos, noteArrySize);
                        }
                    }

                });
    }

    //2-6
    @Override
    public void mNewNote(final OnSynchronizeEditListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncNewNoteAdd(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNoteAddBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mNewNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mNewNote 异常onError:" + e.toString());
                        listener.onSyncNewNoteAddFailed2("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(OldNoteAddBean bean) {
                        MLog.d(TAG, "mNewNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncNewNoteAddSuccess2(bean, position, arraySize, isNewDb);
                        } else {
                            listener.onSyncNewNoteAddFailed2(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //2-7-1
    @Override
    public void mRecoveryNote(final OnSynchronizeEditListener listener, final long noteID, final int position, int arrySize) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncRecoveryNote(noteID, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNote 异常onError:" + e.toString());
                        listener.onSyncRecoveryFailed2("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncRecoverySuccess2(bean, noteID, position);
                        } else {
                            listener.onSyncRecoveryFailed2(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-7-2
    @Override
    public void mRecoveryNotePic(final OnSynchronizeEditListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
        TNSettings settings = TNSettings.getInstance();
        //token
        // 需要加入到MultipartBody中，而不是作为参数传递
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)//表单类型
                .addFormDataPart("token", settings.token);
        String filename = tnNoteAtt.attName;
        String filePath = tnNoteAtt.path;
        long fileId = tnNoteAtt.attId;
        //file
        File file = new File(filename);
        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);//TODO image/jpg
        builder.addFormDataPart("fileName", file.getName(), photoRequestBody);

        List<MultipartBody.Part> parts = builder.build().parts();


        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncRecoveryNotePic(parts)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNotePic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNotePic 异常onError:" + e.toString());
                        listener.onSyncRecoveryNotePicFailed2("异常", new Exception("接口异常！"), picPos, picArrySize, notePos, noteArrySize);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mRecoveryNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncRecoveryNotePicSuccess2(bean, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
                        } else {
                            listener.onSyncRecoveryNotePicFailed2(bean.getMessage(), null, picPos, picArrySize, notePos, noteArrySize);
                        }
                    }

                });
    }

    //2-7-3
    @Override
    public void mRecoveryNoteAdd(final OnSynchronizeEditListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncRecoveryNoteAdd(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNoteAddBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncRecoveryNoteAddFailed2("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(OldNoteAddBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncRecoveryNoteAddSuccess2(bean, position, arraySize, isNewDb);
                        } else {
                            listener.onSyncRecoveryNoteAddFailed2(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //2-8
    @Override
    public void mDeleteNote(final OnSynchronizeEditListener listener, final long noteId, final int poistion) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncDeleteNote(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncDeleteNoteFailed2("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncDeleteNoteSuccess2(bean, noteId, poistion);
                        } else {
                            listener.onSyncDeleteNoteFailed2(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-9
    @Override
    public void mDeleteRealNotes(final OnSynchronizeEditListener listener, final long noteId, final int poistion) {
        TNSettings settings = TNSettings.getInstance();
        //2-9-1
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncDeleteRealNote1(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncDeleteRealNotes1Failed2("异常", new Exception("接口异常！"), poistion);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncpDeleteRealNotes1Success2(bean, noteId, poistion);
                        } else {
                            listener.onSyncDeleteRealNotes1Failed2(bean.getMessage(), null, poistion);
                        }
                    }

                });

        //2-9-2
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncDeleteRealNote2(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncDeleteRealNotes2Failed2("异常", new Exception("接口异常！"), poistion);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncDeleteRealNotes2Success2(bean, noteId, poistion);
                        } else {
                            listener.onSyncDeleteRealNotes2Failed2(bean.getMessage(), null, poistion);
                        }
                    }

                });
    }

    //2-10
    @Override
    public void mGetAllNotesId(final OnSynchronizeEditListener listener) {
        TNSettings settings = TNSettings.getInstance();
        //2-9-1
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncAllNotsId(settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncAllNotesIdAddFailed2("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncAllNotesIdSuccess2(bean.getNote_ids());
                        } else {
                            listener.onSyncAllNotesIdAddFailed2(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-10-1
    @Override
    public void mEditNotePic(final OnSynchronizeEditListener listener, final int cloudsPos, final int attrPos, final TNNote note) {
        String filename = note.atts.get(attrPos).attName;
        String filePath = note.atts.get(attrPos).path;
        long fileId = note.atts.get(attrPos).attId;

        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), filePath);//TODO image/jpg
        MultipartBody.Part part = MultipartBody.Part.createFormData("fileName", filename, photoRequestBody);

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncEditNotePic(part, MyRxUtils.toRequestBody(settings.token))//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mNewNotePic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mNewNotePic 异常onError:" + e.toString());
                        listener.onSyncEditNotePicFailed2("异常", new Exception("接口异常！"), cloudsPos, attrPos, note);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mNewNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncEditNotePicSuccess2(bean, cloudsPos, attrPos, note);
                        } else {
                            listener.onSyncEditNotePicFailed2(bean.getMessage(), null, cloudsPos, attrPos, note);
                        }
                    }

                });
    }

    //2-11-1
    @Override
    public void mEditNote(final OnSynchronizeEditListener listener, final int position, final TNNote note) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncEditNote(note.noteId, note.title, note.content, note.tagStr, note.catId, note.createTime, note.lastUpdate, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mEditNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mEditNote 异常onError:" + e.toString());
                        listener.onSyncEditNoteAddFailed2("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mEditNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncEditNoteSuccess2(bean, position, note);
                        } else {
                            listener.onSyncEditNoteAddFailed2(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-11-2
    @Override
    public void mGetNoteByNoteId(final OnSynchronizeEditListener listener, final int position, long id, final boolean is12) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetNoteByNoteId(id, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean3<GetNoteByNoteIdBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetNoteByNoteId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNoteByNoteId 异常onError:" + e.toString());
                        listener.onSyncpGetNoteByNoteIdFailed2("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean3<GetNoteByNoteIdBean> bean) {
                        MLog.d(TAG, "mGetNoteByNoteId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncpGetNoteByNoteIdSuccess2(bean.getNote(), position, is12);
                        } else {
                            listener.onSyncpGetNoteByNoteIdFailed2(bean.getMsg(), null);
                        }
                    }


                });
    }

    //2-12
    @Override
    public void mGetAllTrashNoteIds(final OnSynchronizeEditListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetTrashNoteIds(settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetAllTrashNoteIds--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetAllTrashNoteIds 异常onError:" + e.toString());
                        listener.onSyncpGetAllTrashNoteIdsFailed2("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        MLog.d(TAG, "mGetAllTrashNoteIds-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncpGetAllTrashNoteIdsSuccess2(bean.getNote_ids());
                        } else {
                            listener.onSyncpGetAllTrashNoteIdsFailed2(bean.getMessage(), null);
                        }
                    }

                });
    }
}
