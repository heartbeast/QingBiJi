package com.thinkernote.ThinkerNote._interface.p;

/**
 *  p层interface
 */
public interface ITextEditPresenter {

    void pFolderAdd(long parentID,String text);
    void pFolderRename(long parentID,String text);
    void pTagAdd(String text);
    void pTagRename(long parentID,String text);

}
