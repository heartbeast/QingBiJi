package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IPagerModule;
import com.thinkernote.ThinkerNote._interface.m.ISplashModule;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnPagerListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 我的笔记 m层 具体实现
 */
public class PagerModuleImpl implements IPagerModule {

    private Context context;
    private static final String TAG = "SJY";

    public PagerModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mSetDefaultFolder(final OnPagerListener listener, long folderID) {

        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .setDefaultFolder(folderID)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "SetDefaultFolder--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("SetDefaultFolder 异常onError:" + e.toString());
                        listener.onDefaultFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "SetDefaultFolder-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onDefaultFolderSuccess(bean);
                        } else {
                            listener.onDefaultFolderFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    @Override
    public void mDeleteTag(final OnPagerListener listener, final long tagID) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteTag(tagID)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mDeleteTag--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mDeleteTag 异常onError:" + e.toString());
                        listener.onTagDeleteFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mDeleteTag-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onTagDeleteSuccess(bean,tagID);
                        } else {
                            listener.onTagDeleteFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    @Override
    public void mDeleteFolder(OnPagerListener listener, long tagID) {

    }

}
