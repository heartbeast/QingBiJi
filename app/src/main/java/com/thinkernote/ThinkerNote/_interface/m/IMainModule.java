package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;

/**
 * 登录 m层interface
 */
public interface IMainModule {
    void mUpgrade(OnMainListener onMainListener);

    void mfolderAdd(OnMainListener listener, int position, int arraySize, String name);

    void mTagAdd(OnMainListener listener, int position, int arraySize, String name);

    void mOldNoteAdd(OnMainListener listener, int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void mUploadOldNotePic(OnMainListener listener, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void mProfile(OnMainListener listener);
}
