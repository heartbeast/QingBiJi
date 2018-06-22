package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IMainModule;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean1;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 注册 m层 具体实现
 */
public class MainModuleImpl implements IMainModule {

    private Context context;
    private static final String TAG = "SJY";

    public MainModuleImpl(Context context) {
        this.context = context;
    }


    @Override
    public void mUpgrade(final OnMainListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .upgrade(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean1<MainUpgradeBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upgrade--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("upgrade 异常onError:" + e.toString());
                        listener.onUpgradeFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean1<MainUpgradeBean> bean) {
                        MLog.d(TAG, "upgrade-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "upgrade-成功" + bean.getData().toString());
                            listener.onUpgradeSuccess(bean.getData());
                        } else {
                            listener.onUpgradeFailed(bean.getMsg(), null);
                        }
                    }

                });
    }


    //01-01第一次登录同步 folder
    @Override
    public void mfolderAdd(final OnMainListener listener, final int position, final int arraySize, String name) {
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
    public void mTagAdd(final OnMainListener listener, final int position, final int arraySize, String name) {
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
    public void GetFolder(final OnMainListener listener) {
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
    public void mGetFoldersByFolderId(final OnMainListener listener, final long id, final int position, final int size) {
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
                        listener.onSyncGetFoldersByFolderIdFailed("异常", new Exception("接口异常！"), id, position, size);
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "GetFoldersByFolderId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncGetFoldersByFolderIdSuccess(bean, id, position, size);
                        } else {
                            listener.onSyncGetFoldersByFolderIdFailed(bean.getMsg(), null, id, position, size);
                        }
                    }

                });
    }


    //01-5
    @Override
    public void mGetFoldersByFolderId2(final OnMainListener listener, final long id, final int outPos, final int outSize, final int position, final int size) {

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncGetFolderByFodlerId(id, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "GetFoldersByFolderId2--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("GetFoldersByFolderId2 异常onError:" + e.toString());
                        listener.onSyncGetFoldersByFolderId2Failed("异常", new Exception("接口异常！"), id, outPos, outSize, position, size);
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "GetFoldersByFolderId2-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncGetFoldersByFolderId2Success(bean, id, outPos, outSize, position, size);
                        } else {
                            listener.onSyncGetFoldersByFolderId2Failed(bean.getMsg(), null, id, outPos, outSize, position, size);
                        }
                    }

                });
    }

    //01-7
    @Override
    public void mFirstFolderAdd(OnMainListener listener, int workPos, int workSize, long catID, int catPos, int flag) {

    }

    //02-01 OldNotePic
    @Override
    public void mUploadOldNotePic(final OnMainListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, TNNoteAtt tnNoteAtt) {
        String filename = tnNoteAtt.attName;
        String filePath = tnNoteAtt.path;
        long fileId = tnNoteAtt.attId;

        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), filePath);
        MultipartBody.Part part = MultipartBody.Part.createFormData("fileName", filename, photoRequestBody);

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncOldNotePic(part, settings.token)//接口方法
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
                            listener.onSyncOldNotePicSuccess(bean, picPos, picArrySize, notePos, noteArrySize);
                        } else {
                            listener.onSyncOldNotePicFailed(bean.getMessage(), null, picPos, picArrySize, notePos, noteArrySize);
                        }
                    }

                });
    }


    //02-02 OldNoteAdd
    @Override
    public void mOldNoteAdd(final OnMainListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {

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


    @Override
    public void mProfile(final OnMainListener listener) {
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


}
