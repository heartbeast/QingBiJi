package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import java.io.File;
import java.util.List;

/**
 * main m层interface
 */
public interface IMainModule {
    void mUpgrade(OnMainListener onMainListener);

    void mDownload(OnMainListener onMainListener,String url, FileProgressListener listener);

    void mProfile(OnMainListener listener);

    void GetFolder(OnMainListener onMainListener);

    void mGetTagList(OnMainListener onMainListener);

    void mGetFoldersByFolderId(OnMainListener onMainListenerl, long id, int position, List<AllFolderItemBean> beans);

    void mFirstFolderAdd(OnMainListener listener, int workPos, int workSize, long catID,String name, int catPos, int flag);

    void mfolderAdd(OnMainListener listener, int position, int arraySize, String name);

    void mTagAdd(OnMainListener listener, int position, int arraySize, String name);

    void mOldNoteAdd(OnMainListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mUploadOldNotePic(OnMainListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    //2-5
    void mNewNotePic(OnMainListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    //2-6
    void mNewNote(OnMainListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    //2-7-1
    void mRecoveryNote(OnMainListener listener, long noteID, int position, int arrySize);

    //2-7-2
    void mRecoveryNotePic(OnMainListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    //2-7-3
    void mRecoveryNoteAdd(OnMainListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    //2-8
    void mDeleteNote(OnMainListener listener, long noteId, int poistion);

    //2-9
    void mDeleteRealNotes(OnMainListener listener, long noteId, int poistion);

    void mGetAllNotesId(OnMainListener listener);

    //2-10-2
    void mEditNotePic(OnMainListener listener,int cloudsPos, int attrPos, TNNote note);

    //2-11-1
    void mEditNote(OnMainListener listener, int position, TNNote tnNote);

    //2-11-2
    void mGetNoteByNoteId(OnMainListener listener, int position, long id, boolean is12);

    void mGetAllTrashNoteIds(OnMainListener listener);

}
