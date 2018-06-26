package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote._interface.v.OnNoteEditListener;

/**
 *  m层interface
 */
public interface INoteEditModule {
    void mNewNote(OnNoteEditListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mNewNotePic(OnNoteEditListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mRecoveryNote(OnNoteEditListener listener, long noteID, int position, int arrySize);

    void mRecoveryNotePic(OnNoteEditListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mRecoveryNoteAdd(OnNoteEditListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mDeleteNote(OnNoteEditListener listener, long noteId, int poistion);

    void mDeleteRealNotes(OnNoteEditListener listener, long noteId, int poistion);

    void mGetAllNotesId(OnNoteEditListener listener);

    void mEditNotePic(OnNoteEditListener listener,int cloudsPos, int attrPos, TNNote note);

    void mEditNote(OnNoteEditListener listener, int position, TNNote tnNote);

    void mGetNoteByNoteId(OnNoteEditListener listener, int position, long id, boolean is12);

    void mGetAllTrashNoteIds(OnNoteEditListener listener);
}
