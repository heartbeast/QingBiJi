package com.thinkernote.ThinkerNote._interface.v;

import com.thinkernote.ThinkerNote.Data.TNNoteAtt;

import java.util.Vector;

/**
 * 笔记详情 v层
 */
public interface OnNoteViewDownloadListener {
    void onSingleDownloadSuccess(Object obj,TNNoteAtt att);

    void onSingleDownloadFailed(String msg, Exception e);

    void onListDownloadSuccess(Object obj,TNNoteAtt att, Vector<TNNoteAtt> tmpList,int position);

    void onListDownloadFailed(String msg, Exception e,TNNoteAtt att, Vector<TNNoteAtt> tmpList, int position);

}
