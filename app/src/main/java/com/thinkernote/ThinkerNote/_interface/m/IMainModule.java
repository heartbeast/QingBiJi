package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;

/**
 * 登录 m层interface
 */
public interface IMainModule {
    void mUpgrade(OnMainListener onMainListener);
    void mSynchronizeData(OnMainListener onMainListener);
}
