package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import java.util.List;

/**
 * m层interface
 */
public interface ISynchronizeDataModule {
    void mProfile(OnSynchronizeDataListener listener);

    void GetFolder(OnSynchronizeDataListener onMainListener);

    void mGetTagList(OnSynchronizeDataListener onMainListener);

    void mGetFoldersByFolderId(OnSynchronizeDataListener onMainListenerl, long id, int position, List<AllFolderItemBean> beans);

    void mFirstFolderAdd(OnSynchronizeDataListener listener, int workPos, int workSize, long catID, String name, int catPos, int flag);

    void mfolderAdd(OnSynchronizeDataListener listener, int position, int arraySize, String name);

    void mTagAdd(OnSynchronizeDataListener listener, int position, int arraySize, String name);

    void mOldNoteAdd(OnSynchronizeDataListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mUploadOldNotePic(OnSynchronizeDataListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mNewNote(OnSynchronizeDataListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mNewNotePic(OnSynchronizeDataListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mRecoveryNote(OnSynchronizeDataListener listener, long noteID, int position, int arrySize);

    void mRecoveryNotePic(OnSynchronizeDataListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mRecoveryNoteAdd(OnSynchronizeDataListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mDeleteNote(OnSynchronizeDataListener listener, long noteId, int poistion);

    void mDeleteRealNotes(OnSynchronizeDataListener listener, long noteId, int poistion);

    void mGetAllNotesId(OnSynchronizeDataListener listener);

    void mEditNotePic(OnSynchronizeDataListener listener, int cloudsPos, int attrPos, TNNote note);

    void mEditNote(OnSynchronizeDataListener listener, int position, TNNote tnNote);

    void mGetNoteByNoteId(OnSynchronizeDataListener listener, int position, long id, boolean is12);

    void mGetAllTrashNoteIds(OnSynchronizeDataListener listener);

}
