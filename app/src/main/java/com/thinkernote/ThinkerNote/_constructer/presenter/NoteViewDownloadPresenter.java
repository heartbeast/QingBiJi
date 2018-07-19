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
 * 非mvp框架内容，原框架下载文件的类
 * 说明：下载的文件 是TNNote.atts,是list样式，for循环每一个atts的item数据
 */
public class NoteViewDownloadPresenter implements OnNoteViewDownloadListener {
    private static final String TAG = "NoteViewDownloadPresenter";
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

    /**
     * 多图下载（自动下载）
     */
    public void start() {
        MLog.d(TAG, "start()");
        startPosition(0, null);
    }

    /**
     * 点击下载
     *
     * @param attId
     */
    public void start(long attId) {
        MLog.d(TAG, "start(attId):" + attId);
        if (act == null || act.isFinishing()) {
            return;
        }
        if (TNUtils.checkNetwork(act)) {
            TNNoteAtt att = mNote.getAttDataById(attId);
            MLog.d(TAG, "start(attId)--TNNoteAtt:" + att.toString());

            if (!TNActionUtils.isDownloadingAtt(att.attId)) {
                //开始下载回调
                if (startListener != null)
                    startListener.onStart(att);
                MLog.d(TAG, "start(attId)--singledownload 下载文件");
                //下载
                singledownload(att, mNote);
            }
        }
    }

    //循环下载  替换for

    /**
     * @param position
     * @param onAtt    结束一个position时使用，用于回调
     */
    private void startPosition(int position, TNNoteAtt onAtt) {

        if (downAtts != null && downAtts.size() > 0 && position < downAtts.size()) {
            //获取当前position的TNNoteAtt
            TNNoteAtt att = downAtts.get(position);
            File file = null;
            //如果downAtts该处position的TNNoteAtt有图片，就执行下个position
            if (!TextUtils.isEmpty(att.path)) {
                file = new File(att.path);
//                if (file.getParentFile()==null||file.getParentFile().exists()) {
////                    file.getParentFile().mkdirs();
////                }
                MLog.d("startPosition--att.syncState=" + att.syncState + "文件路径：" + att.path + "文件大小：" + att.size);
            }

            //如果att已经下载，执行下一个position
            if (file.length() != 0 && att.syncState == 2) {
                MLog.e("startPosition--文件已下载--回调主界面，执行下一个循环");
                if (downAtts.size() == 1) {
                    endOneAttCallback(att, true);
                } else {
                    if (position == downAtts.size() - 1) {
                        endOneAttCallback(att, true);
                    } else {
                        endOneAttCallback(att, true);
                        //执行下一个循环
                        startPosition(position + 1, att);
                    }
                }
                return;
            }

            if (TNUtils.isNetWork() && att.attId != -1 && !TNActionUtils.isDownloadingAtt(att.attId)) {
                //开始下载回调
                if (startListener != null) {
                    startListener.onStart(att);
                }
                //下载
                MLog.d(TAG, "startPosition--下载文件");
                listDownload(att, mNote, position);
            } else {
                MLog.d(TAG, "startPosition--网络差 无法下载");
                if (downAtts.size() == 1) {
                    endOneAttCallback(att, false);
                } else {
                    if (position == downAtts.size() - 1) {
                        endOneAttCallback(att, false);
                    } else {
                        endOneAttCallback(att, false);
                        //执行下一个循环
                        startPosition(position + 1, att);
                    }
                }
            }

        } else {
            //循环结束，回调
            endOneAttCallback(onAtt, true);
        }
    }

    /**
     * 结束一个att下载回调
     */
    private void endOneAttCallback(TNNoteAtt att, boolean isSuccess) {
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
                endListener.onEnd(att, false, null);
            }
        }

    }


    //===========================接口调用=================================
    //调用接口
    private void singledownload(TNNoteAtt tnNoteAtt, TNNote tnNote) {
        MLog.d("download", "singledownload下载文件");
        module.singleDownload(tnNoteAtt, tnNote);
    }

    //调用接口
    private void listDownload(TNNoteAtt tnNoteAtt, TNNote tnNote, int position) {
        MLog.d("download", "listDownload下载文件");
        module.listDownload(tnNoteAtt, tnNote, position);
    }
    //===========================接口返回=================================


    /**
     * @param att      已经拿到下载图片的路径
     * @param position
     */
    @Override
    public void onListDownloadSuccess(TNNote note, TNNoteAtt att, int position) {
        TNNoteAtt newAtt = att;
        File file = new File(att.path);
        if (!TextUtils.isEmpty(att.path)) {
            MLog.d("onListDownloadSuccess", "list文件下载成功" + "--att.path：" + att.path);
            MLog.d("list文件下载成功：", "原状态att.syncState=" + att.syncState + "文件路径" + file.toString() + "文件大小" + file.length());
        }
        //将图片路径保存到本地数据库
        try {
            TNDb.beginTransaction();

            if (att.type > 10000 && att.type < 20000) {
                MLog.e("List--更新数据库");
                newAtt.syncState = mNote.syncState > 2 ? mNote.syncState : 2;
                TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, att.attName, att.type, att.path, att.noteLocalId, file.length(), mNote.syncState > 2 ? mNote.syncState : 2, att.digest, att.attId, att.width, att.height, att.noteLocalId);
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, att.path, mNote.noteLocalId);
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, mNote.syncState > 2 ? mNote.syncState : 2, mNote.noteLocalId);

            }
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        //更新mNote
        mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
        //更新for循环的数据
        downAtts.clear();
        downAtts.addAll(mNote.atts);
        //下载成功的一个position的att展示
        endOneAttCallback(newAtt, true);
        //开始下一个position
        startPosition(position + 1, newAtt);
    }

    @Override
    public void onListDownloadFailed(String msg, Exception e, TNNoteAtt att, int position) {
        MLog.d(TAG, msg);
    }

    /**
     * @param att 已经拿到下载图片的路径
     */
    @Override
    public void onSingleDownloadSuccess(TNNote note, TNNoteAtt att) {
        TNNoteAtt newAtt = att;
        File file = new File(att.path);
        if (!TextUtils.isEmpty(att.path)) {
            MLog.d("single文件下载成功：", "原状态att.syncState=" + att.syncState + "文件路径" + file.toString() + "文件大小" + file.length());
        }

        //将图片路径保存到本地数据库
        try {
            TNDb.beginTransaction();
            if (att.type > 10000 && att.type < 20000) {
                MLog.e("Single-更新数据库");
                newAtt.syncState = mNote.syncState > 2 ? mNote.syncState : 2;
                TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, att.attName, att.type, att.path, att.noteLocalId, file.length(), mNote.syncState > 2 ? mNote.syncState : 2, att.digest, att.attId, att.width, att.height, att.noteLocalId);
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, att.path, mNote.noteLocalId);
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, mNote.syncState > 2 ? mNote.syncState : 2, mNote.noteLocalId);

            } else {
                MLog.e("Bug 无法更新其他position数据");
                //结束一个att下载回调
                endOneAttCallback(att, false);
            }

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
        //结束一个att下载回调
        endOneAttCallback(newAtt, true);
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

    /**
     * 点击下载 或自动下载 ，完成一个 回调一个
     */
    public interface OnDownloadStartListener {

        public void onStart(TNNoteAtt att);
    }

    /**
     * 点击下载 或自动下载 ，完成一个 回调一个
     */
    public interface OnDownloadEndListener {
        public void onEnd(TNNoteAtt att, boolean isSucess, String msg);
    }

}
