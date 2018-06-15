package com.thinkernote.ThinkerNote._interface.m;

import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewListener;

/**
 *  m层interface
 */
public interface INoteViewModule {
    void mGetNote(OnNoteViewListener listener, long noteId);
}
