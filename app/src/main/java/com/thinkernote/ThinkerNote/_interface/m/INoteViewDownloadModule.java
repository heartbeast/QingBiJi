package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;

import java.util.Vector;

/**
 * 登录 p层interface
 */
public interface INoteViewDownloadModule {
    void listDownload(TNNoteAtt tnNoteAtt, TNNote tnNote,int position);
    void singleDownload(TNNoteAtt tnNoteAtt, TNNote tnNote);
}
