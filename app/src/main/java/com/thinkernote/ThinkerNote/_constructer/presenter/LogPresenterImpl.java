package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._interface.m.ILogModule;
import com.thinkernote.ThinkerNote._interface.p.ILogPresener;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;
import com.thinkernote.ThinkerNote._constructer.module.LogModuleImpl;

/**
 * 登录 p层 具体实现
 */
public class LogPresenterImpl implements ILogPresener ,OnLogListener{
    private Context context;
    private OnLogListener onLogView;
    //p层调用M层方法
    private ILogModule logModule;

    public LogPresenterImpl(Context context, OnLogListener logListener) {
        this.context = context;
        this.onLogView = logListener;
        logModule = new LogModuleImpl(context);
    }

    //============================p层重写，用于调用m层方法============================
    @Override
    public void loginNormal(String name, String ps) {
        logModule.loginNomal(this,name,ps);
    }

    @Override
    public void loginQQ(int aArray,String unionId,long currentTime,String accessToken,String refreshToken,String name) {
        String sign = "bid=" + unionId + "&btype=" + aArray + "&stamp=" + currentTime + "qingbiji";
        logModule.loginQQ(this
                ,aArray
                ,unionId
                ,currentTime
                , TNUtils.toMd5(sign).toLowerCase()
                , accessToken
                ,refreshToken
                ,name);
    }

    @Override
    public void loginWechat(int aArray,String unionId,long currentTime,String accessToken,String refreshToken,String name) {
        String sign = "bid=" + unionId + "&btype=" + aArray + "&stamp=" + currentTime + "qingbiji";
        logModule.loginWechat(this
                ,aArray
                ,unionId
                ,currentTime
                , TNUtils.toMd5(sign).toLowerCase()
                , accessToken
                ,refreshToken
                ,name);
    }

    @Override
    public void loginSina(int aArray,String unionId,long currentTime,String accessToken,String refreshToken,String name) {
        String sign = "bid=" + unionId + "&btype=" + aArray + "&stamp=" + currentTime + "qingbiji";
        logModule.loginSina(this
                ,aArray
                ,unionId
                ,currentTime
                , TNUtils.toMd5(sign).toLowerCase()
                , accessToken
                ,refreshToken
                ,name);
    }

    //==========================结果回调==============================
    @Override
    public void onLoginNormalSuccess(Object obj) {
        onLogView.onLoginNormalSuccess(obj);
    }

    @Override
    public void onLoginNormalFailed(String msg,Exception e) {
        onLogView.onLoginNormalFailed(msg,e);
    }

    @Override
    public void onLoginQQSuccess(Object obj) {
        onLogView.onLoginQQSuccess(obj);
    }

    @Override
    public void onLoginQQFailed(String msg, Exception e, String bid,long currentTime, String accessToken, String refreshToken, String name) {
        onLogView.onLoginQQFailed(msg,e,bid,currentTime,accessToken,refreshToken,name);
    }

    @Override
    public void onLoginWechatSuccess(Object obj) {
        onLogView.onLoginWechatSuccess(obj);
    }

    @Override
    public void onLoginWechatFailed(String msg,Exception e, String bid,long currentTime,String accessToken,String refreshToken,String name) {
        onLogView.onLoginWechatFailed(msg,e,bid,currentTime,accessToken,refreshToken,name);

    }

    @Override
    public void onLoginSinaSuccess(Object obj) {
        onLogView.onLoginSinaSuccess(obj);
    }

    @Override
    public void onLoginSinaFailed(String msg,Exception e, String bid,long currentTime,String accessToken,String refreshToken,String name) {
        onLogView.onLoginSinaFailed(msg,e,bid,currentTime,accessToken,refreshToken,name);
    }
    //========================================================
}
