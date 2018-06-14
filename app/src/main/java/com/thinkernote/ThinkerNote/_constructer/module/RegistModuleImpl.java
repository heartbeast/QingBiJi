package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IRegistModule;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.login.VerifyPicBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 注册 m层 具体实现
 */
public class RegistModuleImpl implements IRegistModule {

    private Context context;
    private static final String TAG = "SJY";

    public RegistModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void getVerifyPic(final OnRegistListener listener) {
        MyHttpService.NoCacheBuilder.getHttpServer()//固定样式，可自定义其他网络
                .getVerifyPic()//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<VerifyPicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "验证码--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("验证码 异常onError:" + e.toString());
                        listener.onPicFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(VerifyPicBean bean) {
                        MLog.d(TAG, "验证码-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "验证码-成功");
                            listener.onPicSuccess(bean);
                        } else {
                            listener.onPicFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    @Override
    public void phoneVerifyCode(final OnRegistListener listener, String mPhone, String name, String mAnswer, String mNonce, String mHashKey) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postVerifyCode4(mPhone, name, mAnswer, mNonce, mHashKey)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("验证码--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("验证码--异常onError:" + e.toString());
                        listener.onPhoneVCodeFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("验证码-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("验证码-成功");
                            listener.onPhoneVCodeSuccess(bean);
                        } else {
                            listener.onPhoneVCodeFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    @Override
    public void submitRegist(final OnRegistListener listener, String phone, String ps, String vcode) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .registSubmit(phone, ps, vcode)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("submit--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("submit--异常onError:" + e.toString());
                        listener.onSubmitRegistFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("submit-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("submit-成功");
                            listener.onSubmitRegistSuccess(bean);
                        } else {
                            listener.onSubmitRegistFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    @Override
    public void submitForgetPs(final OnRegistListener listener, String phone, String ps, String vcode) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .findPsSubmit(phone, ps, vcode)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("submit--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("submit--异常onError:" + e.toString());
                        listener.onSubmitFindPsFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("submit-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("submit-成功");
                            listener.onSubmitFindPsSuccess(bean);
                        } else {
                            listener.onSubmitFindPsFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    @Override
    public void autoLogin(final OnRegistListener listener, String phone, String ps) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginNormal(phone, ps)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("login--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("login--异常onError:" + e.toString());
                        listener.onAutoLoginFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("login-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("login-成功");
                            listener.onAutoLoginSuccess(bean);
                        } else {
                            listener.onAutoLoginFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    @Override
    public void bindPhone(final OnRegistListener listener, int mUserType, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone, String vcode, String sign) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginBindPhone(mUserType, bid,name,accessToken,refreshToken,currentTime,phone,vcode,sign)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("bind--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("bind--异常onError:" + e.toString());
                        listener.onBindPhoneFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("bind-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("bind-成功");
                            listener.onBindPhoneSuccess(bean);
                        } else {
                            listener.onBindPhoneFailed(bean.getMessage(), null);
                        }
                    }

                });
    }
}
