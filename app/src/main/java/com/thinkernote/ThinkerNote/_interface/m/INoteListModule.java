package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeEditListener;
import com.thinkernote.ThinkerNote._interface.v.OnNoteListListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import java.util.List;
import java.util.Vector;

/**
 * m层interface
 */
public interface INoteListModule {
    void mGetNotelistByFolderId(OnNoteListListener listener, long mListDetail, int mPageNum, int size, String sort);

    void mGetNotelistByTagId(OnNoteListListener listener, long mListDetail, int mPageNum, int size, String sort);

    //syncDataByNoteId

    void mGetDataByNoteId(OnNoteListListener listListener, long noteId);
    void mSynceNoteAttr(OnNoteListListener listListener, int pos, TNNoteAtt tnNoteAtt, Vector<TNNoteAtt> attrs,long noteId);

    //syncData
    void mProfile(OnSynchronizeDataListener listener);

    void GetFolder(OnSynchronizeDataListener OnSynchronizeDataListener);

    void mGetTagList(OnSynchronizeDataListener OnSynchronizeDataListener);

    void mGetFoldersByFolderId(OnSynchronizeDataListener OnSynchronizeDataListenerl, long id, int position, List<AllFolderItemBean> beans);

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

    //syncEdit
    void mNewNote(OnSynchronizeEditListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mNewNotePic(OnSynchronizeEditListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mRecoveryNote(OnSynchronizeEditListener listener, long noteID, int position, int arrySize);

    void mRecoveryNotePic(OnSynchronizeEditListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mRecoveryNoteAdd(OnSynchronizeEditListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mDeleteNote(OnSynchronizeEditListener listener, long noteId, int poistion);

    void mDeleteRealNotes(OnSynchronizeEditListener listener, long noteId, int poistion);

    void mGetAllNotesId(OnSynchronizeEditListener listener);

    void mEditNotePic(OnSynchronizeEditListener listener, int cloudsPos, int attrPos, TNNote note);

    void mEditNote(OnSynchronizeEditListener listener, int position, TNNote tnNote);

    void mGetNoteByNoteId(OnSynchronizeEditListener listener, int position, long id, boolean is12);

    void mGetAllTrashNoteIds(OnSynchronizeEditListener listener);
}
