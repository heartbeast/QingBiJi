package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnCatListListener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;

/**
 * m层
 */
public interface ICatListModule {
    void mParentFolder(OnCatListListener listener);

    void mGetFolderByFolderId(OnCatListListener listener,long catId);

    void mmoveFolder(OnCatListListener listener, long catId, long selectId);

}
