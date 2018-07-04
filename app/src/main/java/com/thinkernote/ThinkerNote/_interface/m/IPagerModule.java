package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote._interface.v.OnPagerListener;
import com.thinkernote.ThinkerNote._interface.v.OnPagerListener;
import com.thinkernote.ThinkerNote._interface.v.OnPagerListener;

import java.util.Vector;

/**
 * 我的笔记 m层interface
 */
public interface IPagerModule {
    void mSetDefaultFolder(OnPagerListener listener, long folderID);

    void mDeleteTag(OnPagerListener listener, long tagID);

    void mDeleteFolder(OnPagerListener listener, long catID);

    //syncDataByNoteId/与2-12共用

    void mGetDataByNoteId(OnPagerListener listListener, long noteId, int catPos,boolean isCats);

    void mSynceNoteAttr(OnPagerListener listListener, int pos, TNNoteAtt tnNoteAtt, Vector<TNNoteAtt> attrs, long noteId,int catPos, boolean isCats);

    //syncCats
    //2-5
    void mNewNotePic(OnPagerListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    //2-6
    void mNewNote(OnPagerListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    //2-7-1
    void mRecoveryNote(OnPagerListener listener, long noteID, int position, int arrySize);

    //2-7-2
    void mRecoveryNotePic(OnPagerListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    //2-7-3
    void mRecoveryNoteAdd(OnPagerListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    //2-8
    void mDeleteNote(OnPagerListener listener, long noteId, int poistion);

    //2-9
    void mDeleteRealNotes(OnPagerListener listener, long noteId, int poistion);

    //2-10 与main不同
    void mGetFolderNoteIds(OnPagerListener listener, long catId);


    //2-10-2
    void mEditNotePic(OnPagerListener listener, int cloudsPos, int attrPos, TNNote note);

    //2-11-1
    void mEditNote(OnPagerListener listener, int position, TNNote tnNote);

    //2-11-2
    void mGetNoteByNoteId(OnPagerListener listener, int position, long id, boolean is12);

}
