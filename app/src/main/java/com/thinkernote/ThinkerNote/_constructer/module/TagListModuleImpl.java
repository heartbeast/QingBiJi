package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.ITagListModule;
import com.thinkernote.ThinkerNote._interface.v.OnTagListListener;
import com.thinkernote.ThinkerNote.bean.main.TagListBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * m层 具体实现
 */
public class TagListModuleImpl implements ITagListModule {

    private Context context;
    private static final String TAG = "SJY";

    public TagListModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mTagList(final OnTagListListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getTagList(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<TagListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mTagList--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mTagList 异常onError:" + e.toString());
                        listener.onTagListFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(TagListBean bean) {
                        MLog.d(TAG, "mTagList-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onTagListSuccess(bean.getTags());
                        } else {
                            listener.onTagListFailed(bean.getMsg(), null);
                        }
                    }

                });
    }
}
