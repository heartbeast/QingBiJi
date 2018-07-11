package com.thinkernote.ThinkerNote._constructer.presenter;

import android.app.Activity;
import android.text.TextUtils;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.module.NoteViewDownloadModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.INoteViewDownloadModule;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewDownloadListener;

import java.io.File;
import java.util.Vector;

/**
 * 非mvp框架内容，原框架下载文件的类
 */
public class NoteViewDownloadPresenter implements OnNoteViewDownloadListener {
    private static final String TAG = "TNDownloadAttService";
    private static final long ATT_MAX_DOWNLOAD_SIZE = 50 * 1024;

    private static NoteViewDownloadPresenter singleton = null;

    private OnDownloadStartListener startListener;
    private OnDownloadEndListener endListener;

    private Vector<TNNoteAtt> downloadingAtts;
    private Vector<TNNoteAtt> readyDownloadAtts;

    private TNNote mNote;
    private Activity act;
    private INoteViewDownloadModule module;

    private NoteViewDownloadPresenter() {
        readyDownloadAtts = new Vector<TNNoteAtt>();
        downloadingAtts = new Vector<TNNoteAtt>();
        module = new NoteViewDownloadModuleImpl(act, this);
    }

    public static NoteViewDownloadPresenter getInstance() {
        if (singleton == null) {
            synchronized (NoteViewDownloadPresenter.class) {
                if (singleton == null) {
                    singleton = new NoteViewDownloadPresenter();
                }
            }
        }
        return singleton;
    }

    public NoteViewDownloadPresenter init(Activity act, TNNote note) {
        this.act = act;
        mNote = note;
        this.readyDownloadAtts.clear();
        this.readyDownloadAtts.addAll(note.atts);

        return this;
    }

    public void updateNote(TNNote note) {
        this.mNote = note;
        this.readyDownloadAtts.clear();
        this.readyDownloadAtts.addAll(note.atts);
    }

    public void start() {
        MLog.d(TAG, "start");
        Vector<TNNoteAtt> tmpList = new Vector<TNNoteAtt>();//标记已经下载了的文件
        startPosition(tmpList, 0);
    }

    public void start(long attId) {
        MLog.d(TAG, "start:" + attId);
        if (act == null || act.isFinishing()) {
            return;
        }
        if (TNUtils.checkNetwork(act)) {
            TNNoteAtt att = mNote.getAttDataById(attId);
            MLog.d(TAG, "downloadAtt:" + att.attId);
            if (!TNActionUtils.isDownloadingAtt(att.attId)) {
                MLog.d(TAG, "3 -> SyncNoteContent: " + att.attId);
                if (startListener != null)
                    startListener.onStart(att);
                //下载
                downloadingAtts.add(att);
                singledownload(att, mNote);

            }
        }
    }

    //循环下载  替换for
    private void startPosition(Vector<TNNoteAtt> tmpList, int position) {
        if (readyDownloadAtts.size() > 0 && position < readyDownloadAtts.size()) {
            TNNoteAtt att = readyDownloadAtts.get(position);
            File file = null;
            if (!TextUtils.isEmpty(att.path)) {
                file = new File(att.path);
            }
            if (file.length() != 0 && att.syncState == 2) {
                //执行下一个循环
                startPosition(tmpList, position + 1);
                return;
            }
            if (TNUtils.isNetWork() && att.attId != -1) {
                MLog.d(TAG, "downloadAtt:" + att.attId);
                if (!TNActionUtils.isDownloadingAtt(att.attId)) {
                    MLog.d(TAG, "3 -> SyncNoteContent: " + att.attId);
                    if (startListener != null) {
                        startListener.onStart(att);
                    }
                    //添加标记
                    downloadingAtts.add(att);
                    tmpList.add(att);
                    //下载
                    listDownload(att, mNote, tmpList, position);
                }
            }

        } else {
            //结束下载,进行数据库操作
            endListDownload(tmpList);
        }
    }

    /**
     * 循环下载结束，操作数据库
     */
    private void endListDownload(Vector<TNNoteAtt> tmpList) {
        readyDownloadAtts.removeAll(tmpList);
        mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
        mNote.syncState = mNote.syncState > 2 ? mNote.syncState : 2;
        if (mNote.attCounts > 0) {
            for (int i = 0; i < mNote.atts.size(); i++) {
                TNNoteAtt tempAtt = mNote.atts.get(i);
                if (i == 0 && tempAtt.type > 10000 && tempAtt.type < 20000) {
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, tempAtt.path, mNote.noteLocalId);
                }
                if (TextUtils.isEmpty(tempAtt.path) || "null".equals(tempAtt.path)) {
                    mNote.syncState = 1;
                }
            }
        }
        TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, mNote.syncState, mNote.noteLocalId);
    }


    //===========================接口调用=================================
    //调用接口
    private void singledownload(TNNoteAtt tnNoteAtt, TNNote tnNote) {
        MLog.d("download","singledownload下载文件");
        module.singleDownload(tnNoteAtt, tnNote);
//        //TODO
//        TNAction.runActionAsync(TNActionType.SyncNoteAtt,
//                tnNoteAtt,
//                tnNote);
    }

    //调用接口
    private void listDownload(TNNoteAtt tnNoteAtt, TNNote tnNote, Vector<TNNoteAtt> tmpList, int position) {
        MLog.d("download","listDownload下载文件");
        module.listDownload(tnNoteAtt, tnNote, tmpList, position);
//        //TODO
//        TNAction.runActionAsync(TNActionType.SyncNoteAtt,
//                tnNoteAtt,
//                tnNote);
    }
    //===========================接口返回=================================

    @Override
    public void onSingleDownloadSuccess(Object obj, TNNoteAtt att) {
        readyDownloadAtts.remove(att);
        //移除标记
        downloadingAtts.remove(att);
        //回调act
        if (endListener != null) {
            if (mNote.getAttDataById(att.attId) != null)
                endListener.onEnd(att, true, null);
            else
                MLog.i(TAG, "att:" + att.attId + " not in the note:" + mNote.noteId);
        }
    }

    @Override
    public void onSingleDownloadFailed(String msg, Exception e) {

    }

    @Override
    public void onListDownloadSuccess(Object obj, TNNoteAtt att, Vector<TNNoteAtt> tmpList, int position) {
        //移除标记
        downloadingAtts.remove(att);
        //执行下一个循环
        startPosition(tmpList, position + 1);
        // TODO
//        start();

        //回调act
        if (endListener != null) {
            if (mNote.getAttDataById(att.attId) != null)
                endListener.onEnd(att, true, null);
            else
                MLog.i(TAG, "att:" + att.attId + " not in the note:" + mNote.noteId);
        }
    }

    @Override
    public void onListDownloadFailed(String msg, Exception e, TNNoteAtt att, Vector<TNNoteAtt> tmpList, int position) {
        startPosition(tmpList, position + 1);
        //回调act
        if (endListener != null) {
            if (mNote.getAttDataById(att.attId) != null)
                endListener.onEnd(att, false, msg);
            else
                MLog.i(TAG, "att:" + att.attId + " not in the note:" + mNote.noteId);
        }
    }

    //===========================回调act的自定义回调===========================
    public void setOnDownloadStartListener(OnDownloadStartListener startListener) {
        this.startListener = startListener;
    }

    public void setOnDownloadEndListener(OnDownloadEndListener endListener) {
        this.endListener = endListener;
    }


    public interface OnDownloadStartListener {
        public void onStart(TNNoteAtt att);
    }

    public interface OnDownloadEndListener {
        public void onEnd(TNNoteAtt att, boolean isSucess, String msg);
    }

}
