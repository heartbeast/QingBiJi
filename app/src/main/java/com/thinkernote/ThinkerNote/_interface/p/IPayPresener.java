package com.thinkernote.ThinkerNote._interface.p;

/**
 *  p层interface
 */
public interface IPayPresener {
    void pAlipay(String mAmount, String mType);

    void pWxpay(String mAmount, String mType);

}
