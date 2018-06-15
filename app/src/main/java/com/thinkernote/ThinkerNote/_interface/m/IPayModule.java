package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnPayListener;

/**
 *  m层interface
 */
public interface IPayModule {
    void mAlipay(OnPayListener listener, String mAmount, String mType);
    void mWxpay(OnPayListener listener, String mAmount, String mType);
}
