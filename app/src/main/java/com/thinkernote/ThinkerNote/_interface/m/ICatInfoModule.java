package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnCatInfoListener;

/**
 * m层
 */
public interface ICatInfoModule {
    void mSetDefaultFolder(OnCatInfoListener listener, long catId);

    void mCatDelete(OnCatInfoListener listener, long catId);

}
