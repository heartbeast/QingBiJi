package com.thinkernote.ThinkerNote._interface.p;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;

import java.util.Vector;

/**
 * 我的笔记 p层interface
 */
public interface IPagerPresenter {

    void pSetDefaultFolder(long tagID);

    void pDeleteTag(long tagID);

    void pDeleteCat(long tagID);


    //syncDataByNoteId，与2-12-1共用
    void pGetDataByNoteId(long noteId,int catPos,boolean isCats);

    //syncDataByNoteId，与2-12-2共用
    void pSynceNoteAttr(int pos, TNNoteAtt attr, Vector<TNNoteAtt> atts, long noteId, int catPos,boolean isCats);

    //syncCats 同 main2-5----2-9
    //2-5
    void pNewNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pNewNote(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pRecoveryNote(long noteID, int position, int arrySize);

    void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pDeleteNote(long noteId, int position);

    //2-9
    void pDeleteRealNotes(long noteId, int position);

    //2-10
    void pGetFolderNoteIds(long catId);

    //2-10-1
    void pEditNotePic(int cloudsPos, int attrPos, TNNote note);

    //2-11-1
    void pEditNote(int position, TNNote note);

    //2-11-2
    void pGetNoteByNoteId(int position, long noteId, boolean is13);

}
