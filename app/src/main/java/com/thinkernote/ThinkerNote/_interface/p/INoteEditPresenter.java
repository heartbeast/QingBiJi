package com.thinkernote.ThinkerNote._interface.p;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;

/**
 * 登录 p层interface
 */
public interface INoteEditPresenter {

    void pNewNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pNewNote(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pRecoveryNote(long noteID, int position, int arrySize);

    void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pDeleteNote(long noteId, int position);

    void pDeleteRealNotes(long noteId, int position);

    void pGetAllNotesId();

    void pEditNotePic(int cloudsPos, int attrPos, TNNote note);

    void pEditNote(int position, TNNote note);

    void pGetNoteByNoteId(int position, long noteId, boolean is12);

    void pGetAllTrashNoteIds();

}
