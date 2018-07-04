package com.thinkernote.ThinkerNote._interface.p;

import android.app.Dialog;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import java.util.List;

/**
 * main p层interface
 */
public interface IMainPresenter {
    void pUpgrade(String home);

    void pDownload(String url, FileProgressListener progressListener);

    void folderAdd(int position, int arraySize, String folderName);

    void pGetFolder();

    void pProfile();

    void pGetFoldersByFolderId(long id, int position, List<AllFolderItemBean> beans);

    void pFirstFolderAdd(int workPos, int workSize, long catID,String name, int catPos, int flag);

    void tagAdd(int position, int arraySize, String tagName);

    void pUploadOldNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pOldNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pGetTagList();

    //2-6
    void pNewNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    //2-5
    void pNewNote(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pRecoveryNote(long noteID, int position, int arrySize);

    void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pDeleteNote(long noteId, int position);

    //2-9-2
    void pDeleteRealNotes(long noteId, int position);

    //2-10
    void pGetAllNotesId();

    //2-10-1
    void pEditNotePic(int cloudsPos, int attrPos, TNNote note);

    //2-11-1
    void pEditNote(int position, TNNote note);

    //2-11-2
    void pGetNoteByNoteId(int position, long noteId, boolean is13);

    void pGetAllTrashNoteIds();
}
