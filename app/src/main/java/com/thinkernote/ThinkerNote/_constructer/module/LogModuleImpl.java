package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.ILogModule;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 登录 m层 具体实现
 */
public class LogModuleImpl implements ILogModule {

    private Context context;
    private static final String TAG = "SJY";
    public LogModuleImpl(Context context) {
        this.context = context;
    }


    @Override
    public void loginNomal(final OnLogListener listener, String name, String ps) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginNormal(name, ps)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<LoginBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onLoginNormalFailed("异常"  ,new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(LoginBean bean) {
                        MLog.d(TAG, "登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "登录-成功");
                            listener.onLoginNormalSuccess(bean);
                        } else{
                            listener.onLoginNormalFailed(bean.getMessage(),null);
                        }
                    }

                });
    }

    @Override
    public void loginQQ(final OnLogListener listener, int btype, final String bid, final long stamp, String sign, final String accessToken, final String refreshToken, final String name) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginQQ(btype, bid,stamp,sign)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onLoginQQFailed("异常"  ,new Exception("接口异常！"),bid,stamp,accessToken,refreshToken,name);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "登录-成功");
                            listener.onLoginQQSuccess(bean);
                        } else{
                            listener.onLoginQQFailed(bean.getMessage(),null,bid,stamp,accessToken,refreshToken,name);
                        }
                    }

                });
    }

    @Override
    public void loginSina(final OnLogListener listener, int btype, final String bid, final long stamp, String sign, final String accessToken, final String refreshToken, final String name) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginSina(btype, bid,stamp,sign)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onLoginSinaFailed("异常"  ,new Exception("接口异常！"),bid,stamp,accessToken,refreshToken,name);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "登录-成功");
                            listener.onLoginSinaSuccess(bean);
                        } else{
                            listener.onLoginSinaFailed(bean.getMessage(),null,bid,stamp,accessToken,refreshToken,name);
                        }
                    }

                });
    }


    @Override
    public void loginWechat(final OnLogListener listener, int btype, final String bid, final long stamp, String sign, final String accessToken, final String refreshToken, final String name) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginWechat(btype, bid,stamp,sign)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onLoginWechatFailed("异常"  ,new Exception("接口异常！"),bid,stamp,accessToken,refreshToken,name);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "登录-成功");
                            listener.onLoginWechatSuccess(bean);
                        } else{
                            listener.onLoginWechatFailed(bean.getMessage(),null,bid,stamp,accessToken,refreshToken,name);
                        }
                    }

                });
    }

}
