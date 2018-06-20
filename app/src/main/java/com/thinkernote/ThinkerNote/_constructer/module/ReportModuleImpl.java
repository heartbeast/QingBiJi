package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IReportModule;
import com.thinkernote.ThinkerNote._interface.m.ITextEditModule;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnReportListener;
import com.thinkernote.ThinkerNote._interface.v.OnTextEditListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *  m层 具体实现
 */
public class ReportModuleImpl implements IReportModule {

    private Context context;
    private static final String TAG = "SJY";

    public ReportModuleImpl(Context context) {
        this.context = context;
    }

    /**
     * TODO 图片上传 后台只上传一张/多张图片，没有表单
     *
     */
    @Override
    public void mFeedBackPic(final OnReportListener listener, List<String> fileList, final String content, final String email) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .upLoadPic(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "FeedBackPic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("FeedBackPic 异常onError:" + e.toString());
                        listener.onPicFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "FeedBackPic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onPicSuccess(bean,content,email);
                        } else {
                            listener.onPicFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    @Override
    public void mFeedBack(final OnReportListener listener, final String content, long pid, final String email) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .feedBack(content,pid,email,settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "FeedBackPic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("FeedBackPic 异常onError:" + e.toString());
                        listener.onSubmitFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "FeedBackPic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSubmitSuccess(bean);
                        } else {
                            listener.onSubmitFailed(bean.getMessage(), null);
                        }
                    }

                });
    }
}
