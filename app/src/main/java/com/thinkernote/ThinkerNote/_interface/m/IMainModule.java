package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import java.util.List;

/**
 * 登录 m层interface
 */
public interface IMainModule {
    void mUpgrade(OnMainListener onMainListener);

    void mProfile(OnMainListener listener);

    void GetFolder(OnMainListener onMainListener);

    void mGetTagList(OnMainListener onMainListener);

    void mGetFoldersByFolderId(OnMainListener onMainListenerl, long id, int position, List<AllFolderItemBean> beans);

    void mFirstFolderAdd(OnMainListener listener, int workPos, int workSize, long catID, int catPos, int flag);

    void mfolderAdd(OnMainListener listener, int position, int arraySize, String name);

    void mTagAdd(OnMainListener listener, int position, int arraySize, String name);

    void mOldNoteAdd(OnMainListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mUploadOldNotePic(OnMainListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mNewNote(OnMainListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mNewNotePic(OnMainListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mRecoveryNote(OnMainListener listener, long noteID, int position, int arrySize);

    void mRecoveryNotePic(OnMainListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mRecoveryNoteAdd(OnMainListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mDeleteNote(OnMainListener listener, long noteId, int poistion);

    void mDeleteRealNotes(OnMainListener listener, long noteId, int poistion);

    void mGetAllNotesId(OnMainListener listener);

    void mEditNotePic(OnMainListener listener,int cloudsPos, int attrPos, TNNote note);

    void mEditNote(OnMainListener listener, int position, TNNote tnNote);

    void mGetNoteByNoteId(OnMainListener listener, int position, long id, boolean is12);

    void mGetAllTrashNoteIds(OnMainListener listener);

}
