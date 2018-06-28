package com.thinkernote.ThinkerNote._interface.p;

/**
 * p层interface
 */
public interface ICatListPresenter {
    void pParentFodler();

    void pGetFolderByFolderId(long catId);

    void pFolderMove(long catId, long selectId);

}
