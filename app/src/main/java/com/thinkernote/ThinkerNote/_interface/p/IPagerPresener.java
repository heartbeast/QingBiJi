package com.thinkernote.ThinkerNote._interface.p;

/**
 * 我的笔记 p层interface
 */
public interface IPagerPresener {

    void pSetDefaultFolder(long tagID);

    void pDeleteTag(long tagID);

    void pDeleteFolder(long folderID);

}
