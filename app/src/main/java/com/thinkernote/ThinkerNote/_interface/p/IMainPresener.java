package com.thinkernote.ThinkerNote._interface.p;

import android.app.Dialog;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import java.util.List;

/**
 * 手机号注册 p层interface
 */
public interface IMainPresener {
    void pUpgrade(String home);

    void folderAdd(int position, int arraySize, String folderName);

    void pGetFolder();

    void pGetFoldersByFolderId(long id, int position, List<AllFolderItemBean> beans);

    void pFirstFolderAdd(int workPos, int workSize, long catID, int catPos, int flag);

    void tagAdd(int position, int arraySize, String tagName);

    void pUploadOldNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pOldNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pDownload(String url, Dialog dialog);

    void pProfile();
}
