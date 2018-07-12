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
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.module.NoteViewDownloadModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.INoteViewDownloadModule;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewDownloadListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * TODO 该处逻辑有问题 需要重写
 * 非mvp框架内容，原框架下载文件的类
 */
public class NoteViewDownloadPresenter implements OnNoteViewDownloadListener {
    private static final String TAG = "TNDownloadAttService";
    private static final long ATT_MAX_DOWNLOAD_SIZE = 50 * 1024;

    private static NoteViewDownloadPresenter singleton = null;

    private OnDownloadStartListener startListener;
    private OnDownloadEndListener endListener;

    private List<TNNoteAtt> downAtts;

    private TNNote mNote;
    private Activity act;
    private INoteViewDownloadModule module;

    private NoteViewDownloadPresenter() {
        downAtts = new ArrayList<>();
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
        this.mNote = note;
        this.downAtts.clear();
        this.downAtts.addAll(note.atts);

        return this;
    }

    public void updateNote(TNNote note) {
        this.mNote = note;
        this.downAtts.addAll(note.atts);
    }

    public void start() {
        MLog.d(TAG, "start");
        Vector<TNNoteAtt> tmpList = new Vector<TNNoteAtt>();//标记已经下载了的文件
        //不会readyDownloadAtts.size = 0
        startPosition(0, null);
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
                singledownload(att, mNote);

            }
        }
    }

    //循环下载  替换for

    /**
     * TODO 该处依然有问题
     *
     * @param position
     * @param endAttr  最后结束循环使用
     */
    private void startPosition(int position, TNNoteAtt endAttr) {

        if (downAtts != null && downAtts.size() > 0 && position < downAtts.size()) {
            MLog.d("downAtts.size=" + downAtts.size());
            TNNoteAtt att = downAtts.get(position);
            File file = null;
            if (!TextUtils.isEmpty(att.path)) {
                file = new File(att.path);
                MLog.d("att.syncState=" + att.syncState + "文件路径：" + att.path + "文件大小：" + att.size);

            }

            if (file.length() != 0 && att.syncState == 2) {
                MLog.d("执行下一个循环");
                //执行下一个循环
                startPosition(position + 1, att);
                return;
            }

            if (TNUtils.isNetWork() && att.attId != -1 && !TNActionUtils.isDownloadingAtt(att.attId)) {
                if (startListener != null) {
                    startListener.onStart(att);
                }

                //下载
                MLog.d(TAG, "下载文件");
                listDownload(att, mNote, position);

            } else {
                MLog.d(TAG, "网络差 无法下载");
                endListDownload(endAttr, false);
            }

        } else {
            //进行数据库操作
            DbListDownload();
            //结束循环,调用endListener
            endListDownload(endAttr, true);
        }
    }

    /**
     * 操作数据库
     */
    private void DbListDownload() {
        MLog.d(TAG, "start()---for循环---数据库操作");
        //更新mNote
        mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
        mNote.syncState = mNote.syncState > 2 ? mNote.syncState : 2;
        //更新for循环的数据
        downAtts.clear();
        downAtts.addAll(mNote.atts);

        try {
            TNDb.beginTransaction();
            if (mNote.attCounts > 0) {
                for (int i = 0; i < mNote.atts.size(); i++) {
                    TNNoteAtt tempAtt = mNote.atts.get(i);
                    if (tempAtt.type > 10000 && tempAtt.type < 20000) {
                        TNDb.getInstance().updataSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, new Object[]{tempAtt.path, mNote.noteLocalId});
                    }
                    if (TextUtils.isEmpty(tempAtt.path) || "null".equals(tempAtt.path)) {
                        mNote.syncState = 1;
                    }
                    MLog.d("数据库操作：" + "mNote.syncState=" + mNote.syncState + "--mNote.atts.size()=" + mNote.atts.size() + "--TNNoteAtt-->position=" + i + "--内容=" + tempAtt.toString());
                }
            }
            TNDb.getInstance().updataSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, new Object[]{mNote.syncState, mNote.noteLocalId});
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
        MLog.d(TAG, "start()---for循环---数据库操作--结束");
    }

    /**
     * 循环下载结束
     */
    private void endListDownload(TNNoteAtt att, boolean isSuccess) {
        if (att != null) {
            MLog.d(TAG, "回调act endListener attName=" + att.attName);
            //回调act
            if (endListener != null) {
                if (mNote.getAttDataById(att.attId) != null) {
                    MLog.d(TAG, "回调act endListener attName=" + att.attName);
                    if (isSuccess) {
                        endListener.onEnd(att, true, null);
                    } else {
                        endListener.onEnd(null, false, null);

                    }
                } else {
                    MLog.i(TAG, "att:" + att.attId + " not in the note:" + mNote.noteId);
                }
            }
        } else {
            if (!isSuccess) {
                endListener.onEnd(null, false, null);
            }
        }

    }


    //===========================接口调用=================================
    //调用接口
    private void singledownload(TNNoteAtt tnNoteAtt, TNNote tnNote) {
        MLog.d("download", "singledownload下载文件");
        module.singleDownload(tnNoteAtt, tnNote);
//        //TODO
//        TNAction.runActionAsync(TNActionType.SyncNoteAtt,
//                tnNoteAtt,
//                tnNote);
    }

    //调用接口
    private void listDownload(TNNoteAtt tnNoteAtt, TNNote tnNote, int position) {
        MLog.d("download", "listDownload下载文件");
        module.listDownload(tnNoteAtt, tnNote, position);
//        //TODO
//        TNAction.runActionAsync(TNActionType.SyncNoteAtt,
//                tnNoteAtt,
//                tnNote);
    }
    //===========================接口返回=================================


    /**
     * TODO 说明：
     *
     * @param att      已经拿到下载图片的路径
     * @param position
     */
    @Override
    public void onListDownloadSuccess(TNNote note, TNNoteAtt att, int position) {
        File file = new File(att.path);
        if (!TextUtils.isEmpty(att.path)) {
            MLog.d("文件下载成功：", "原状态att.syncState=" + att.syncState + "文件路径" + file.toString() + "文件大小" + file.length());
        }
        //将图片路径保存到本地数据库
        try {
            TNDb.beginTransaction();

            if (att.type > 10000 && att.type < 20000) {
                MLog.e("更新数据库");
                TNDb.getInstance().updataSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, new Object[]{att.attName, att.type, att.path, att.noteLocalId, file.length(), 2, att.digest, att.attId, att.width, att.height, mNote.noteLocalId});
                TNDb.getInstance().updataSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, new Object[]{att.path, mNote.noteLocalId});
            }
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        //执行数据库操作
        DbListDownload();
        //等价于 start（）,但是有 att参数
        startPosition(0, att);
    }

    @Override
    public void onListDownloadFailed(String msg, Exception e, TNNoteAtt att, int position) {
        MLog.d(TAG, msg);
    }

    @Override
    public void onSingleDownloadSuccess(TNNote mNote, TNNoteAtt att) {
        File file = new File(att.path);
        if (!TextUtils.isEmpty(att.path)) {
            MLog.d("文件下载成功：", "原状态att.syncState=" + att.syncState + "文件路径" + file.toString() + "文件大小" + file.length());
        }
        //将图片路径保存到本地数据库
        try {
            TNDb.beginTransaction();

            if (att.type > 10000 && att.type < 20000) {
                TNDb.getInstance().updataSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, new Object[]{att.attName, att.type, att.path, att.noteLocalId, file.length(), 2, att.digest, att.attId, att.width, att.height, mNote.noteLocalId});
                TNDb.getInstance().updataSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, new Object[]{att.path, mNote.noteLocalId});
            } else {
                MLog.e("Bug 无法更新其他position数据");
                MLog.d("att.type=" + att.type);
                TNDb.getInstance().updataSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, new Object[]{att.attName, att.type, att.path, att.noteLocalId, file.length(), 2, att.digest, att.attId, att.width, att.height, mNote.noteLocalId});
                TNDb.getInstance().updataSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, new Object[]{att.path, mNote.noteLocalId});
            }
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        //执行数据库操作
        DbListDownload();
        //等价于 start（）,但是有 att参数
        startPosition(0, att);
    }

    @Override
    public void onSingleDownloadFailed(String msg, Exception e) {
        MLog.d(TAG, msg);
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
