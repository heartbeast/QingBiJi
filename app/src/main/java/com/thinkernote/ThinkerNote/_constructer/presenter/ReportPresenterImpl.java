package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.ReportModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.TagListModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IReportModule;
import com.thinkernote.ThinkerNote._interface.m.ITagListModule;
import com.thinkernote.ThinkerNote._interface.p.IReportPresener;
import com.thinkernote.ThinkerNote._interface.p.ITagListPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnReportListener;

import java.util.List;

/**
 * p层 具体实现
 */
public class ReportPresenterImpl implements IReportPresener, OnReportListener {
    private Context context;
    private OnReportListener onView;
    //p层调用M层方法
    private IReportModule module;

    public ReportPresenterImpl(Context context, OnReportListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new ReportModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================

    @Override
    public void pFeedBackPic(List<String> mFiles,String content,String email) {
        module.mFeedBackPic(this, mFiles,content,email);
    }

    @Override
    public void pFeedBack(String content, long pid, String email) {
        module.mFeedBack(this, content, pid, email);
    }

    //==========================结果回调==============================


    @Override
    public void onPicSuccess(Object obj, String content, String email) {
        onView.onPicSuccess(obj, content, email);
    }

    @Override
    public void onPicFailed(String msg, Exception e) {
        onView.onPicFailed(msg, e);
    }

    @Override
    public void onSubmitSuccess(Object obj) {
        onView.onSubmitSuccess(obj);
    }

    @Override
    public void onSubmitFailed(String msg, Exception e) {
        onView.onSubmitFailed(msg, e);
    }
}
