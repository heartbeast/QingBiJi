package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.ISettingsModule;
import com.thinkernote.ThinkerNote._interface.v.OnSettingsListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * m层 具体实现
 */
public class SettingsModuleImpl implements ISettingsModule {

    private Context context;
    private static final String TAG = "SJY";

    public SettingsModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mgetProfile(final OnSettingsListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getUserInfo(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upgrade--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("upgrade 异常onError:" + e.toString());
                        listener.onProfileFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "upgrade-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onProfileSuccess(bean);
                        } else {
                            listener.onProfileFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    @Override
    public void mVerifyEmail(final OnSettingsListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .verifyEmail(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upgrade--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("upgrade 异常onError:" + e.toString());
                        listener.onVerifyEmailFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "upgrade-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onVerifyEmailSuccess(bean);
                        } else {
                            listener.onVerifyEmailFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    @Override
    public void mSetDefaultFolder(final OnSettingsListener listener, final long pid) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .setDefaultFolder(pid,settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upgrade--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("upgrade 异常onError:" + e.toString());
                        listener.onDefaultFoldeFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "upgrade-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onDefaultFolderSuccess(bean,pid);
                        } else {
                            listener.onDefaultFoldeFailed(bean.getMessage(), null);
                        }
                    }
                });
    }
}
