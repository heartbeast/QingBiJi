package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnPagerListener;

/**
 * 我的笔记 m层interface
 */
public interface IPagerModule {
    void mSetDefaultFolder(OnPagerListener listener, long folderID);

    void mDeleteTag(OnPagerListener listener, long tagID);

    void mDeleteFolder(OnPagerListener listener, long tagID);
}
