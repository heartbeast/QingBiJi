package com.thinkernote.ThinkerNote.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.DBHelper.CatDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.TagDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Utils.TNActivityManager;
import com.thinkernote.ThinkerNote.Views.CustomDialog;
import com.thinkernote.ThinkerNote._constructer.presenter.MainPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.IMainPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.bean.main.TagItemBean;
import com.thinkernote.ThinkerNote.bean.main.TagListBean;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主界面
 * 说明：进入主界面：会同时执行2个异步：onCreate的更新 和 onResume下的configView的同步
 * 同步功能说明：由10多个接口串行调用，比较复杂，所以要注意调用顺序
 * sjy 0525 TODO 下载未做
 */
public class TNMainAct extends TNActBase implements OnClickListener, OnMainListener {
    //==================================同步常量=======================================
    //第一次登录的同步常量
    public static final int FISRTLAUCh_FOLDER_ADD = 11;//1

    //正常登录的同步常量
    public static final int DELETE_LOCALNOTE = 101;//1
    public static final int DELETE_REALNOTE = 102;//
    public static final int DELETE_REALNOTE2 = 103;//
    public static final int UPDATA_EDITNOTES = 104;//

    //==================================变量=======================================
    private long mLastClickBackTime = 0;
    private String mDownLoadAPKPath = "";
    private TextView mTimeView;
    private TNSettings mSettings = TNSettings.getInstance();

    //
    private IMainPresenter presener;

    /**
     * 如下数据，当最后一个接口调用完成后，一定好清空数据
     */

    private String[] arrayFolderName;//第一次登录，要同步的数据，（1-1）
    private String[] arrayTagName;//第一次登录，要同步的数据，（1-2）
    //
    private Vector<TNCat> cats;//第一次登录，要同步的数据，（1-3）
    private String[] groupWorks;//（3）下第一个数组数据
    private String[] groupLife;//（3）下第2个数组数据
    private String[] groupFun;//（3）下第3个数组数据
    //
    private Vector<TNNote> addOldNotes;//（2-2）正常同步，第一个调用数据
    //    private Vector<TNNoteAtt> oldNotesAtts;//（2-3）正常同步，第一个调用数据中第一调用的数据 不可使用全局，易错
    //
    private Vector<TNNote> addNewNotes;//（2-5）正常同步，第5个调用数据
//    private Vector<TNNoteAtt> newNotesAtts;//（2-5）正常同步，第5个调用数据中第一调用的数据 不可使用全局，易错

    private Vector<TNNote> recoveryNotes;//(2-7)正常同步，第7个调用数据
//    Vector<TNNoteAtt> recoveryNotesAtts;//(2-7)正常同步，第7个调用数据中第一调用的数据 不可使用全局，易错

    Vector<TNNote> deleteNotes;//(2-8)正常同步，第8个调用数据
    Vector<TNNote> deleteRealNotes;//(2-9)正常同步，第9个调用数据
    Vector<TNNote> allNotes;//(2-10)正常同步，第10个调用数据
    Vector<TNNote> editNotes;//(2-11)正常同步，第11个调用数据
    Vector<TNNote> trashNotes;//(2-12)正常同步，第12个调用数据
    //接口返回数据
    private List<List<AllFolderItemBean>> mapList = new ArrayList<>();//递归调用使用的数据集合，size最大是5；//后台需求
    private List<AllNotesIdsBean.NoteIdItemBean> cloudIds;//2-10接口返回
    List<AllNotesIdsBean.NoteIdItemBean> trashNoteArr;//(2-12)接口返回，，第13个调用数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //关闭其他界面
        TNActivityManager.getInstance().finishOtherActivity(this);

        //TODO
        TNAction.regResponder(TNActionType.UpdateSoftware, this, "respondUpdateSoftware");

        //
        presener = new MainPresenterImpl(this, this);
        setViews();

        //第一次进入，打开帮助界面
        if (mSettings.firstLaunch) {
            startActivity(TNHelpAct.class);
        }

        //检查更新
        if (savedInstanceState == null) {
            if (TNUtils.isNetWork()) {
                // p
                findUpgrade();
            }

            mSettings.appStartCount += 1;
            mSettings.savePref(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int flag = intent.getIntExtra("FLAG", -1);
        if (flag == 1) {
            CustomDialog.Builder builder = new CustomDialog.Builder(this);
            builder.setMessage("恭喜您！绑定成功");
            builder.setTitle(R.drawable.phone_enable);
            builder.setShowNext(false);
            builder.setPositiveButton("开始使用", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }


    protected void setViews() {
        TNUtilsSkin.setImageViewDrawable(this, null, R.id.main_divide, R.drawable.main_divide);

        mTimeView = (TextView) findViewById(R.id.main_lastsync_time);

        /* set listeners */
        findViewById(R.id.main_allnote).setOnClickListener(this);
        findViewById(R.id.main_cameranote).setOnClickListener(this);
        findViewById(R.id.main_newnote).setOnClickListener(this);
        findViewById(R.id.main_project).setOnClickListener(this);
        findViewById(R.id.main_doodlenote).setOnClickListener(this);
        findViewById(R.id.main_serch).setOnClickListener(this);
        findViewById(R.id.main_sync_btn).setOnClickListener(this);
        findViewById(R.id.main_recordnote).setOnClickListener(this);
        findViewById(R.id.main_exchange).setOnClickListener(this);

        findViewById(R.id.main_projectlog_count_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.main_bootview).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        mLastClickBackTime = 0;
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void configView() {

        if (TextUtils.isEmpty(mSettings.phone) && mSettings.phoneDialogShowCount < 3 && createStatus == 0) {

            mSettings.phoneDialogShowCount += 1;
            mSettings.savePref(false);
            CustomDialog.Builder builder = new CustomDialog.Builder(this);
            builder.setMessage("检测到您的轻笔记还未绑定手机号，为了安全，请您绑定手机号");
            builder.setTitle(R.drawable.phone_disable);
            builder.setShowNext(true);
            builder.setPositiveButton("绑定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(TNBindPhoneAct.class);
                }
            });
            builder.create().show();
        }

        //第一次进来有网或者在wifi情况下自动同步
        if ((createStatus == 0 && TNUtils.isAutoSync()) || mSettings.firstLaunch) {
            if (TNActionUtils.isSynchronizing()) {
                return;
            }
            startSyncAnimation();
            TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
            //p
            synchronizeData();
        }

        Intent i = null;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            i = (Intent) b.get(Intent.EXTRA_INTENT);
        }
        if (i != null && i.hasExtra("Type") && createStatus == 0) {
            runExtraIntent();
        }

        if (TNSettings.getInstance().originalSyncTime > 0) {
            mTimeView.setText("上次同步时间：" + TNUtilsUi.formatDate(TNMainAct.this,
                    TNSettings.getInstance().originalSyncTime / 1000L));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_newnote: {//综合笔记
                startActivity(TNNoteEditAct.class);
                break;
            }
            case R.id.main_allnote: {//我的笔记
                startActivity(TNPagerAct.class);
                break;
            }
            case R.id.main_cameranote: {//拍照笔记
                Bundle b = new Bundle();
                b.putString("Target", "camera");
                startActivity(TNNoteEditAct.class, b);
                break;
            }

            case R.id.main_doodlenote: {//涂鸦笔记
                Bundle b = new Bundle();
                b.putString("Target", "doodle");
                startActivity(TNNoteEditAct.class, b);
                ;
                break;
            }
            case R.id.main_recordnote: {//录音笔记
                Bundle b = new Bundle();
                b.putString("Target", "record");
                startActivity(TNNoteEditAct.class, b);
                break;
            }
            case R.id.main_project:

                break;

            case R.id.main_exchange: {//设置
                startActivity(TNUserInfoAct.class);
                //debug:
                break;
            }
            case R.id.main_sync_btn: {//同步按钮
                if (TNUtils.isNetWork()) {
                    if (TNActionUtils.isSynchronizing()) {
                        TNUtilsUi.showNotification(this, R.string.alert_Synchronize_TooMuch, false);
                        return;
                    }
                    startSyncAnimation();
                    TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
                    //
                    synchronizeData();
                } else {
                    TNUtilsUi.showToast(R.string.alert_Net_NotWork);
                }
                break;
            }
            case R.id.main_serch: {//搜索
                Bundle b = new Bundle();
                b.putInt("SearchType", 1);
                startActivity(TNSearchAct.class, b);
                break;
            }

            case R.id.main_bootview: {//引导 说明
                findViewById(R.id.main_bootview).setVisibility(View.GONE);
                break;
            }
        }
    }

    public void cancelDialog() {
        findViewById(R.id.main_sync_btn).clearAnimation();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            View v = findViewById(R.id.main_bootview);
            if (v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.GONE);
                return true;
            }
            long currTime = System.currentTimeMillis();
            if (currTime - mLastClickBackTime > 5000) {
                TNUtilsUi.showShortToast(R.string.click_back_again_exit);
                mLastClickBackTime = currTime;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 同步按钮的动画
     */
    private void startSyncAnimation() {
        RotateAnimation rAnimation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rAnimation.setDuration(3000);
        rAnimation.setRepeatCount(99999);
        rAnimation.setInterpolator(new LinearInterpolator());
        findViewById(R.id.main_sync_btn).startAnimation(rAnimation);
    }

    /**
     * 同步结束后的操作
     *
     * @param state 0 = 成功/1=back取消同步/2-异常触发同步终止
     */
    private void endSynchronize(int state) {

        //结束动画
        findViewById(R.id.main_sync_btn).clearAnimation();

        if (state == 0) {
            //正常结束
            TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
            //
            TNSettings settings = TNSettings.getInstance();
            settings.originalSyncTime = System.currentTimeMillis();
            settings.savePref(false);
            mTimeView.setText("上次同步时间：" + TNUtilsUi.formatDate(TNMainAct.this,
                    settings.originalSyncTime / 1000L));
        } else if (state == 1) {
            TNUtilsUi.showNotification(this, R.string.alert_Synchronize_Stoped, true);
        } else {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
        }
    }

    //更新弹窗的自定义监听（确定按钮的监听）
    private AlertDialog upgradeDialog;

    class CustomListener implements View.OnClickListener {


        public CustomListener(AlertDialog dialog) {
            upgradeDialog = dialog;
        }

        @Override
        public void onClick(View v) {
            upgradeDialog.setCancelable(false);
            upgradeDialog.setCanceledOnTouchOutside(false);
            upgradeDialog.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    // Search键
                    if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
                        return true;
                    }
                    return false;
                }
            });

            Button theButton = upgradeDialog.getButton(DialogInterface.BUTTON_POSITIVE);//开始下载按钮
            theButton.setText(getString(R.string.update_downloading));
            theButton.setEnabled(false);

            Button negButton = upgradeDialog.getButton(DialogInterface.BUTTON_NEGATIVE);//取消下载按钮
            negButton.setEnabled(false);

            //下载接口
            downloadNewAPK(mDownLoadAPKPath);
        }
    }

    //-------------------------------------数据库操作------------------------------------------

    /**
     * 调用图片上传，就触发更新db
     *
     * @param attrId
     */
    private void upDataAttIdSQL(final long attrId, final TNNoteAtt tnNoteAtt) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    TNDb.getInstance().upDataAttIdSQL(TNSQLString.ATT_UPDATE_SYNCSTATE_ATTID, 2, attrId, (int) tnNoteAtt.noteLocalId);
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
            }
        });
    }

    /**
     * 调用OldNoteAdd接口，就触发更新db
     */
    private void upDataNoteLocalIdSQL(OldNoteAddBean oldNoteAddBean, TNNote note) {
        long id = oldNoteAddBean.getId();
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().upDataNoteLocalIdSQL(TNSQLString.NOTE_UPDATE_NOTEID_BY_NOTELOCALID, id, note.noteLocalId);
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * 调用GetFoldersByFolderId接口，就触发插入db
     */
    private void insertDBCatsSQL(AllFolderBean allFolderBean, long pCatId) {
        TNSettings settings = TNSettings.getInstance();
        CatDbHelper.clearCatsByParentId(pCatId);
        List<AllFolderItemBean> beans = allFolderBean.getFolders();
        for (int i = 0; i < beans.size(); i++) {
            AllFolderItemBean bean = beans.get(i);

            JSONObject tempObj = TNUtils.makeJSON(
                    "catName", bean.getName(),
                    "userId", settings.userId,
                    "trash", 0,
                    "catId", bean.getId(),
                    "noteCounts", bean.getCount(),
                    "catCounts", bean.getFolder_count(),
                    "deep", bean.getFolder_count() > 0 ? 1 : 0,
                    "pCatId", pCatId,
                    "isNew", -1,
                    "createTime", TNUtils.formatStringToTime(bean.getCreate_at()),
                    "lastUpdateTime", TNUtils.formatStringToTime(bean.getUpdate_at()),
                    "strIndex", TNUtils.getPingYinIndex(bean.getName())
            );
            CatDbHelper.addOrUpdateCat(tempObj);
        }
    }

    /**
     * 调用recovery接口(2-7-1)，就触发更新db
     */
    private void recoveryNoteSQL(long noteId) {

        TNNote note = TNDbUtils.getNoteByNoteId(noteId);
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().upDataRecoverySQL(TNSQLString.NOTE_SET_TRASH, 0, 2, System.currentTimeMillis() / 1000, note.noteLocalId);
            TNDb.getInstance().upDataRecoveryLastTimeSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
    }

    private void updataDeleteNoteSQL(long noteId) {

        TNNote note = TNDbUtils.getNoteByNoteId(noteId);
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().upDataDeleteNoteSQL(TNSQLString.NOTE_SET_TRASH, 2, 1, System.currentTimeMillis() / 1000, note.noteLocalId);
            TNDb.getInstance().upDatadeleteLastTimeSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * 2-9-2接口
     */
    private void deleteRealSQL(final long nonteLocalID, final int position) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    TNNote note = TNDbUtils.getNoteByNoteId(nonteLocalID);
                    //
                    TNDb.getInstance().deleteReadNotesSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, nonteLocalID);
                    TNDb.getInstance().updataReadNotesLastTimeSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }

                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = DELETE_REALNOTE2;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 2-11-1 更新日记时间
     *
     * @param noteId
     */
    private void updataEditNotesLastTime(final int position, final long noteId) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    //
                    String[] args = new String[]{noteId + ""};
                    TNDb.getInstance().updataSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, args);
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = UPDATA_EDITNOTES;
                handler.sendMessage(msg);
            }
        });

    }

    /**
     * 2-11-1 更新日记时间 （接口返回处理）
     */
    private void updataEditNotes(final int position, final TNNote note) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String shortContent = TNUtils.getBriefContent(note.content);
                TNDb.beginTransaction();
                try {
                    //
                    String[] args = new String[]{shortContent, note.noteId + ""};
                    TNDb.getInstance().updataSQL(TNSQLString.NOTE_SHORT_CONTENT, args);

                    //
                    String[] args2 = new String[]{System.currentTimeMillis() / 1000 + "", note.catId + ""};
                    TNDb.getInstance().updataSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, args2);

                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
                //下一个position
                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = UPDATA_EDITNOTES;
                handler.sendMessage(msg);
            }
        });

    }

    //2-11-2
    public static void updateNote(GetNoteByNoteIdBean bean) {

        long noteId = bean.getId();
        String contentDigest = bean.getContent_digest();
        TNNote note = TNDbUtils.getNoteByNoteId(noteId);//在全部笔记页同步，会走这里，没在首页同步过的返回为null

        int syncState = note == null ? 1 : note.syncState;
        List<GetNoteByNoteIdBean.TagBean> tags = bean.getTags();

        String tagStr = "";
        for (int k = 0; k < tags.size(); k++) {
            GetNoteByNoteIdBean.TagBean tempTag = tags.get(k);
            String tag = tempTag.getName();
            if ("".equals(tag)) {
                continue;
            }
            if (tags.size() == 1) {
                tagStr = tag;
            } else {
                if (k == (tags.size() - 1)) {
                    tagStr = tagStr + tag;
                } else {
                    tagStr = tagStr + tag + ",";
                }
            }
        }

        String thumbnail = "";
        if (note != null) {
            thumbnail = note.thumbnail;
            Vector<TNNoteAtt> localAtts = TNDbUtils.getAttrsByNoteLocalId(note.noteLocalId);
            List<GetNoteByNoteIdBean.Attachments> atts = bean.getAttachments();
            if (localAtts.size() != 0) {
                //循环判断是否与线上同步，线上没有就删除本地
                for (int k = 0; k < localAtts.size(); k++) {
                    boolean exit = false;
                    TNNoteAtt tempLocalAtt = localAtts.get(k);
                    for (int i = 0; i < atts.size(); i++) {
                        GetNoteByNoteIdBean.Attachments tempAtt = atts.get(i);
                        long attId = tempAtt.getId();
                        if (tempLocalAtt.attId == attId) {
                            exit = true;
                        }
                    }
                    if (!exit) {
                        if (thumbnail.indexOf(String.valueOf(tempLocalAtt.attId)) != 0) {
                            thumbnail = "";
                        }
                        NoteAttrDbHelper.deleteAttById(tempLocalAtt.attId);
                    }
                }
                //循环判断是否与线上同步，本地没有就插入数据
                for (int k = 0; k < atts.size(); k++) {
                    GetNoteByNoteIdBean.Attachments tempAtt = atts.get(k);
                    long attId = tempAtt.getId();
                    boolean exit = false;
                    for (int i = 0; i < localAtts.size(); i++) {
                        TNNoteAtt tempLocalAtt = localAtts.get(i);
                        if (tempLocalAtt.attId == attId) {
                            exit = true;
                        }
                    }
                    if (!exit) {
                        syncState = 1;
                        insertAttr(tempAtt, note.noteLocalId);
                    }
                }
            } else {
                for (int i = 0; i < atts.size(); i++) {
                    GetNoteByNoteIdBean.Attachments tempAtt = atts.get(i);
                    syncState = 1;
                    insertAttr(tempAtt, note.noteLocalId);
                }
            }

            //如果本地的更新时间晚就以本地的为准
            if (note.lastUpdate > (bean.getUpdate_at() / 1000)) {
                return;
            }

            if (atts.size() == 0) {
                syncState = 2;
            }
        }

        int catId = -1;
        //TODO getFolder_id可以为负值么
        if (bean.getFolder_id() > 0) {
            catId = bean.getFolder_id();
        }

        JSONObject tempObj = TNUtils.makeJSON(
                "title", bean.getTitle(),
                "userId", TNSettings.getInstance().userId,
                "trash", bean.getTrash(),
                "source", "android",
                "catId", catId,
                "content", TNUtilsHtml.codeHtmlContent(bean.getContent(), true),
                "createTime", bean.getCreate_at() / 1000,
                "lastUpdate", bean.getUpdate_at() / 1000,
                "syncState", syncState,
                "noteId", noteId,
                "shortContent", TNUtils.getBriefContent(bean.getContent()),
                "tagStr", tagStr,
                "lbsLongitude", bean.getLongitude() <= 0 ? 0 : bean.getLongitude(),
                "lbsLatitude", bean.getLatitude() <= 0 ? 0 : bean.getLatitude(),
                "lbsRadius", bean.getRadius() <= 0 ? 0 : bean.getRadius(),
                "lbsAddress", bean.getAddress(),
                "nickName", TNSettings.getInstance().username,
                "thumbnail", thumbnail,
                "contentDigest", contentDigest
        );
        if (note == null)
            NoteDbHelper.addOrUpdateNote(tempObj);
        else
            NoteDbHelper.updateNote(tempObj);
    }

    public static void insertAttr(GetNoteByNoteIdBean.Attachments tempAtt, long noteLocalId) {
        long attId = tempAtt.getId();
        String digest = tempAtt.getDigest();
        //
        TNNoteAtt noteAtt = TNDbUtils.getAttrById(attId);
        noteAtt.attName = tempAtt.getName();
        noteAtt.type = tempAtt.getType();
        noteAtt.size = tempAtt.getSize();
        noteAtt.syncState = 1;

        JSONObject tempObj = TNUtils.makeJSON(
                "attName", noteAtt.attName,
                "type", noteAtt.type,
                "path", noteAtt.path,
                "noteLocalId", noteLocalId,
                "size", noteAtt.size,
                "syncState", noteAtt.syncState,
                "digest", digest,
                "attId", attId,
                "width", noteAtt.width,
                "height", noteAtt.height
        );
        NoteAttrDbHelper.addOrUpdateAttr(tempObj);
    }
    //-------------------------------------handler处理同步------------------------------------------


    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case DELETE_LOCALNOTE://2-8-2的调用
                //执行下一个position/执行下一个接口
                pDelete(((int) msg.obj + 1));
                break;

            case DELETE_REALNOTE://2-9 deleteRealNotes
                //执行下一个position/执行下一个接口
                pRealDelete(((int) msg.obj + 1));
                break;
            case DELETE_REALNOTE2://2-9 deleteRealNotes
                //执行下一个position/执行下一个接口

                if (isRealDelete1 && isRealDelete2) {
                    //执行下一个
                    pRealDelete((int) msg.obj + 1);

                    //复原 false,供下次循环使用
                    isRealDelete1 = false;
                    isRealDelete2 = false;
                }

                break;
            case UPDATA_EDITNOTES://2-11 更新日记时间返回
                //执行下一个position/执行下一个接口
                pEditNotePic((int) msg.obj + 1);
                break;
        }
    }


    //=============================================p层调用======================================================

    //检查更新
    private void findUpgrade() {
        presener.pUpgrade("HOME");

    }

    //TODO
    private void downloadNewAPK(String url) {
        presener.pDownload(url, progressListener);

//        //TODO
//        TNAction.runActionAsync(TNActionType.UpdateSoftware, url, upgradeDialog);
    }

    //监听下载文件进度
    FileProgressListener progressListener = new FileProgressListener() {

        @Override
        public void onFileProgressing(int progress) {
            MLog.e("实时progress=" + progress);
            ProgressBar pb = (ProgressBar) upgradeDialog.findViewById(R.id.update_progressbar);
            TextView percent = (TextView) upgradeDialog.findViewById(R.id.update_percent);

            pb.setProgress(progress);
            percent.setText(progress + "%");
        }

    };

    //下载
    public void respondUpdateSoftware(TNAction aAction) {

        if (aAction.result == TNActionResult.Working) {
            Dialog dialog = (Dialog) aAction.inputs.get(1);
            ProgressBar pb = (ProgressBar) dialog.findViewById(R.id.update_progressbar);
            pb.setProgress((Integer) aAction.progressInfo);
            TextView percent = (TextView) dialog.findViewById(R.id.update_percent);
            percent.setText(String.format("%.2fM / %.2fM (%.2f%%)",
                    pb.getProgress() / 1024f / 1024f,
                    pb.getMax() / 1024f / 1024f,
                    100f * pb.getProgress() / pb.getMax()));
        } else if (aAction.result == TNActionResult.Finished) {
            MLog.d(TAG, "respondUpdateSoftware finished");
            Dialog dialog = (Dialog) aAction.inputs.get(1);
            dialog.dismiss();
            String filePath = (String) aAction.outputs.get(0);
            if (filePath != null)
                //打开文件
                TNUtilsUi.openFile(filePath);
        }
    }


    //-------第一次登录同步的p调用-------

    /**
     * (一)同步 第一个调用的方法
     * 执行顺序：先arrayFolderName对应的所有接口，再arrayTagName对应的所有接口，
     * 接口个数=arrayFolderName.size + arrayTagName.size
     */

    private void synchronizeData() {
        if (mSettings.firstLaunch) {//如果第一次登录app，执行该处方法
            //需要同步的文件数据
            arrayFolderName = new String[]{TNConst.FOLDER_DEFAULT, TNConst.FOLDER_MEMO, TNConst.GROUP_FUN, TNConst.GROUP_WORK, TNConst.GROUP_LIFE};
            arrayTagName = new String[]{TNConst.TAG_IMPORTANT, TNConst.TAG_TODO, TNConst.TAG_GOODSOFT};

            //同步第一个数据（有数组，循环调用）
            pFolderAdd(0, arrayFolderName.length, arrayFolderName[0]);
        } else {//如果正常启动，执行该处
            syncProfile();
        }
    }

    /**
     * 第一次登录同步(按如下执行顺序调用接口)
     * <p>
     * （一.1）更新 文件
     */
    private void pFolderAdd(int position, int arraySize, String name) {
        presener.folderAdd(position, arraySize, name);
    }

    /**
     * 第一次登录同步
     * <p>
     * （一.2）更新 tag
     */
    private void pTagAdd(int position, int arraySize, String name) {
        presener.tagAdd(position, arraySize, name);
    }

    /**
     * 第一次登录同步
     * <p>
     * （一.3）更新 GetFolder
     */
    private void syncGetFolder() {
        presener.pGetFolder();
    }

    /**
     * 第一次登录同步 每一次层的调用
     * <p>
     * （一.4） GetFoldersByFolderId
     * list中都要调用接口，串行调用
     * 接口个数= allFolderItemBeans.size的n个连乘（n最大5）
     *
     * @param isAdd 如果mapList.add之后立即执行该方法，为true
     */
    private void syncGetFoldersByFolderId(int startPos, boolean isAdd) {
        if (mapList.size() > 0 && mapList.size() <= 5) {
            //有1---5，for循环层层内嵌,从最内层（size最大处）开始执行
            List<AllFolderItemBean> allFolderItemBeans = mapList.get(mapList.size() - 1);

            if (allFolderItemBeans.size() > 0) {
                if (startPos < allFolderItemBeans.size() - 1) {
                    //从1层从第一个数据开始
                    if (isAdd) {
                        syncGetFoldersByFolderId(0, allFolderItemBeans);
                    } else {
                        syncGetFoldersByFolderId(startPos, allFolderItemBeans);
                    }

                } else {
                    if (mapList.size() == 1) {
                        //执行下一个接口
                        syncTNCat();
                    } else {
                        //执行上一层的新循环
                        mapList.remove(mapList.size() - 1);
                        syncGetFoldersByFolderId(0, false);
                    }
                }
            } else {
                //执行上一层的循环
                if (mapList.size() == 1) {
                    //执行下一个接口
                    syncTNCat();
                } else {
                    mapList.remove(mapList.size() - 1);
                    syncGetFoldersByFolderId(0, false);
                }
            }

        } else {
            TNUtilsUi.showToast("递归调用数据出错了！");
        }
    }

    /**
     * 具体执行GetFoldersByFolderId的步骤 p层调用
     */

    private void syncGetFoldersByFolderId(int startPos, List<AllFolderItemBean> beans) {
        if (beans.get(startPos).getFolder_count() == 0) {//没有数据就跳过
            syncGetFoldersByFolderId(startPos + 1, false);
        } else {
            presener.pGetFoldersByFolderId(beans.get(startPos).getId(), startPos, beans);
        }
    }

    /**
     * （一.5）更新TNCat
     * 双层for循环的样式,串行执行接口
     * 接口个数 = 3*cats.size*groupXXX.size;
     */
    private void syncTNCat() {
        //同步TNCat
        cats = TNDbUtils.getAllCatList(mSettings.userId);
        if (cats.size() > 0) {
            //先执行最外层的数据
            syncTNCat(0, cats.size());
        } else {
            //执行下一个接口
            syncProfile();
        }
    }

    /**
     * （一.5）更新 postion的TNCat数据
     *
     * @param postion
     */
    private void syncTNCat(int postion, int catsSize) {
        if (postion < catsSize - 1) {
            //获取postion条数据
            TNCat tempCat = cats.get(postion);

            if (TNConst.GROUP_WORK.equals(tempCat.catName)) {
                groupWorks = new String[]{TNConst.FOLDER_WORK_NOTE, TNConst.FOLDER_WORK_UNFINISHED, TNConst.FOLDER_WORK_FINISHED};
            }
            if (TNConst.GROUP_LIFE.equals(tempCat.catName)) {
                groupLife = new String[]{TNConst.FOLDER_LIFE_DIARY, TNConst.FOLDER_LIFE_KNOWLEDGE, TNConst.FOLDER_LIFE_PHOTO};

            }
            if (TNConst.GROUP_FUN.equals(tempCat.catName)) {
                groupFun = new String[]{TNConst.FOLDER_FUN_TRAVEL, TNConst.FOLDER_FUN_MOVIE, TNConst.FOLDER_FUN_GAME};
            }

            //执行顺序:groupWorks-->groupLife-->groupFun
            if (groupWorks == null && groupLife == null && groupFun == null) {
                //postion下没有数据，执行下个position
                syncTNCat(postion + 1, catsSize);
            } else {
                if (groupWorks != null) {
                    //执行顺序:groupWorks-->groupLife-->groupFun
                    pFirstFolderAdd(0, groupWorks.length, tempCat.catId, tempCat.catName, postion, 1);//执行第一个
                } else {
                    if (groupLife != null) {
                        //执行顺序:groupWorks-->groupLife-->groupFun
                        pFirstFolderAdd(0, groupLife.length, tempCat.catId, tempCat.catName, postion, 2);//执行第2个
                    } else {
                        //保险一点，我对这个数据不甚了解 sjy 0622
                        if (groupFun != null) {
                            //执行顺序:groupWorks-->groupLife-->groupFun
                            pFirstFolderAdd(0, groupFun.length, tempCat.catId, tempCat.catName, postion, 2);//执行第3个
                        } else {
                            //postion下没有数据，执行下个position
                            syncTNCat(postion + 1, catsSize);
                        }

                    }
                }
            }
        } else {
            //执行下一个接口
            syncProfile();
        }
    }

    /**
     * （一.5）具体执行TNCat的步骤 p层调用
     *
     * @param workPos
     * @param workSize
     * @param catID
     * @param catPos
     * @param flag     TNCat下有三条数据数组，flag决定执行哪一条数据的标记
     */
    private void pFirstFolderAdd(int workPos, int workSize, long catID, String name, int catPos, int flag) {
        presener.pFirstFolderAdd(workPos, workSize, catID, name, catPos, flag);
    }


    //-------正常登录同步的p调用-------

    /**
     * （二.1）正常同步 第一个接口
     */
    private void syncProfile() {
        presener.pProfile();
    }

    /**
     * （二。2+二。3）正常登录的数据同步（非第一次登录的同步）
     * 执行顺序：同步老数据(先上传图片接口，再OldNote接口)，没有老数据就同步用户信息接口
     * 接口个数 = addOldNotes.size * oldNotesAtts.size;
     */
    private void syncOldNote() {
        if (!mSettings.syncOldDb) {
            //add老数据库的笔记
            addOldNotes = TNDbUtils.getOldDbNotesByUserId(TNSettings.getInstance().userId);
            if (addOldNotes.size() > 0) {
                //先 上传数组的第一个
                TNNote tnNote = addOldNotes.get(0);
                Vector<TNNoteAtt> oldNotesAtts = tnNote.atts;
                if (oldNotesAtts.size() > 0) {//有图，先上传图片
                    pUploadOldNotePic(0, oldNotesAtts.size(), 0, addOldNotes.size(), oldNotesAtts.get(0));
                } else {//如果没有图片，就执行OldNote
                    pOldNote(0, addOldNotes.size(), addOldNotes.get(0), false, addOldNotes.get(0).content);
                }
            } else {
                //下个执行接口
                pGetTagList();
            }
        } else {
            //下个执行接口
            pGetTagList();
        }
    }

    /**
     * (二.2)正常同步 第一个执行的接口 上传图片OldNotePic 循环调用
     * 说明：先处理notepos的图片，处理完就上传notepos的文本，然后再处理notepos+1的图片...,如此循环
     * 和（二.3组成双层for循环，该处是最内层for执行）
     */
    private void pUploadOldNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        presener.pUploadOldNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.3)正常同步 第2个执行的接口 循环调用
     * 和（二.2组成双层for循环，该处是最外层for执行）
     */

    private void pOldNote(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        presener.pOldNoteAdd(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.4)正常同步 pGetTagList
     */

    private void pGetTagList() {
        presener.pGetTagList();
    }


    /**
     * (二.5+二.6)正常同步 pAddNewNote
     * 说明：同(二.2+二.3)的执行顺序，先处理notepos的图片，处理完就上传notepos的文本，然后再处理notepos+1的图片，如此循环
     * 接口个数：addNewNotes.size * addNewNotes.size
     */

    private void pAddNewNote() {
        addNewNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 3);

        if (addNewNotes.size() > 0) {
            //先 上传数组的第一个
            TNNote tnNote = addNewNotes.get(0);
            Vector<TNNoteAtt> newNotesAtts = tnNote.atts;
            if (newNotesAtts.size() > 0) {//有图，先上传图片
                pNewNotePic(0, newNotesAtts.size(), 0, addNewNotes.size(), newNotesAtts.get(0));
            } else {//如果没有图片，就执行OldNote
                pNewNote(0, addNewNotes.size(), addNewNotes.get(0), false, addNewNotes.get(0).content);
            }
        } else {
            //下个执行接口
            recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
            recoveryNote(0);
        }
    }

    /**
     * (二.5)正常同步 第一个执行的接口 上传图片OldNotePic 循环调用
     * 和（二.6组成双层for循环，该处是最内层for执行）
     */
    private void pNewNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        presener.pNewNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.6)正常同步 第2个执行的接口 循环调用
     * 和（二.5组成双层for循环，该处是最外层for执行）
     */

    private void pNewNote(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {

        presener.pNewNote(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.7)正常同步 recoveryNote
     * 从0开始执行
     * 接口个数：如果走NoteRecovery:recoveryNotes.size /如果走NoteAdd：recoveryNotes.size * recoveryNotesattrs.size
     * 说明：同(二.7-2+二.7-3)的执行顺序，先处理recoveryNotes的图片，处理完就上传recoveryNotes的文本，然后再处理position+1的图片，如此循环
     *
     * @param position 标记，表示recoveryNotes的开始位置，非recoveryNotesAtts位置
     */
    private void recoveryNote(int position) {
        if (position < recoveryNotes.size() && position >= 0) {
            if (recoveryNotes.get(position).noteId != -1) {
                //循环执行
                pRecoveryNote(recoveryNotes.get(position).noteId, position, recoveryNotes.size());
            } else {
                Vector<TNNoteAtt> recoveryNotesAtts = recoveryNotes.get(position).atts;
                if (recoveryNotesAtts.size() > 0) {//有图，先上传图片
                    pRecoveryNotePic(0, recoveryNotesAtts.size(), position, recoveryNotes.size(), recoveryNotesAtts.get(0));
                } else {//如果没有图片，就执行RecoveryNoteAdd
                    pRecoveryNoteAdd(0, recoveryNotes.size(), recoveryNotes.get(position), true, recoveryNotes.get(position).content);
                }
            }
        } else {

            //执行下一个接口
            deleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
            pDelete(0);
        }

    }

    /**
     * (二.7)01
     */
    private void pRecoveryNote(long noteID, int position, int arrySize) {
        presener.pRecoveryNote(noteID, position, arrySize);
    }

    /**
     * (二.7)02
     */
    private void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        presener.pRecoveryNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.7)03
     */
    private void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        presener.pRecoveryNoteAdd(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.8)
     *
     * @param position
     */
    private void pDelete(int position) {

        if (deleteNotes.size() > 0 && position < (deleteNotes.size() - 1)) {
            if (deleteNotes.get(position).noteId != -1) {
                pNoteDelete(deleteNotes.get(position).noteId, position);
            } else {
                //不调接口
                pNoteLocalDelete(position, deleteNotes.get(position).noteLocalId);
            }
        } else {
            //下一个接口
            deleteRealNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
            pRealDelete(0);
        }
    }

    /**
     * (二.8)
     */
    private void pNoteDelete(long noteId, int postion) {
        presener.pDeleteNote(noteId, postion);
    }

    /**
     * (二.8)删除本地数据 （不调接口）
     */
    private void pNoteLocalDelete(final int position, final long noteLocalId) {

        //使用异步操作，完成后，执行下一个 position或接口
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    //
                    TNDb.getInstance().upDataDeleteLocalNoteSQL(TNSQLString.NOTE_SET_TRASH, 2, 6, System.currentTimeMillis() / 1000, noteLocalId);
                    //
                    TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
                    TNDb.getInstance().upDatadeleteLocalLastTimeSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }

                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = DELETE_LOCALNOTE;
                handler.sendMessage(msg);
            }
        });

    }

    /**
     * (二.9)deleteRealNotes
     *
     * @param position deleteRealNotes执行位置
     */
    //添加标记，两个线程异步执行，都执行完，isRealDelete都设置为true,再执行下一poistion，
    private boolean isRealDelete1 = false;
    private boolean isRealDelete2 = false;

    private void pRealDelete(int position) {

        if (deleteRealNotes.size() > 0 && position < (deleteRealNotes.size() - 1)) {
            if (deleteRealNotes.get(position).noteId == -1) {
                //
                pDeleteReadNotesSql(deleteRealNotes.get(position).noteLocalId, position);
            } else {
                //2个接口
                pDeleteRealNotes(deleteRealNotes.get(position).noteId, position);
            }
        } else {
            //下一个接口
            pGetAllNoteIds();
        }
    }


    /**
     * (二.9) deleteReadNotes
     * 数据库
     * 接口个数：2
     *
     * @param nonteLocalID
     */
    private void pDeleteReadNotesSql(final long nonteLocalID, final int position) {
        //使用异步操作，完成后，执行下一个 position或接口
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    //
                    TNDb.getInstance().deleteReadNotesSQL(TNSQLString.NOTE_DELETE_BY_NOTELOCALID, nonteLocalID);
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }

                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = DELETE_REALNOTE;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * (二.9)
     */
    private void pDeleteRealNotes(long noteId, int postion) {
        //
        presener.pDeleteRealNotes(noteId, postion);

    }

    /**
     * (二.10)
     */
    private void pGetAllNoteIds() {
        //
        presener.pGetAllNotesId();
    }

    /**
     * (二.10)-1 editNote上传图片
     * 说明：
     * 2-10-1和2-11-1图片上传和2-5/2-6相同
     *
     * @param position cloudIds数据的其实操作位置
     */
    private void pEditNotePic(int position) {
        if (cloudIds.size() > 0 && position < (cloudIds.size() - 1)) {
            long id = cloudIds.get(position).getId();
            int lastUpdate = cloudIds.get(position).getUpdate_at();

            //找出该日记，比较时间
            for (int j = 0; j < editNotes.size(); j++) {
                if (id == editNotes.get(j).noteId) {
                    if (editNotes.get(j).lastUpdate > lastUpdate) {
                        //上传图片，之后上传文本
                        pEditNotePic(position, 0, editNotes.get(j));
                    } else {
                        updataEditNotesLastTime(position, editNotes.get(j).noteLocalId);
                    }
                }
            }

        } else {
            pUpdataNote(0, false);
        }
    }

    /**
     * (二.10)-1
     * 图片上传
     *
     * @param cloudsPos cloudIds数据的其实操作位置
     * @param tnNote
     */
    private void pEditNotePic(int cloudsPos, int attsPos, TNNote tnNote) {
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size() - 1)) {
            TNNote note = tnNote;
            String shortContent = TNUtils.getBriefContent(note.content);
            String content = note.content;
            ArrayList list = new ArrayList();
            int index1 = content.indexOf("<tn-media");
            int index2 = content.indexOf("</tn-media>");
            while (index1 >= 0 && index2 > 0) {
                String temp = content.substring(index1, index2 + 11);
                list.add(temp);
                content = content.replaceAll(temp, "");
                index1 = content.indexOf("<tn-media");
                index2 = content.indexOf("</tn-media>");
            }
            for (int i = 0; i < list.size(); i++) {
                String temp = (String) list.get(i);
                boolean isExit = false;
                for (TNNoteAtt att : note.atts) {
                    String temp2 = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
                    if (temp.equals(temp2)) {
                        isExit = true;
                    }
                }
                if (!isExit) {
                    note.content = note.content.replaceAll(temp, "");
                }
            }

            if (note.atts.size() > 0 && attsPos < (note.atts.size() - 1)) {
                //上传attsPos的图片
                TNNoteAtt att = note.atts.get(attsPos);
                if (!TextUtils.isEmpty(att.path) && att.attId != -1) {
                    String s1 = String.format("<tn-media hash=\"%s\" />", att.digest);
                    String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", att.digest, att.attId);
                    note.content = note.content.replaceAll(s1, s2);
                    String s3 = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
                    String s4 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", att.digest, att.attId);
                    note.content = note.content.replaceAll(s3, s4);

                    //执行下一个attsPos位置的数据
                    pEditNotePic(cloudsPos, attsPos + 1, note);

                } else {
                    //接口，上传图片
                    presener.pEditNotePic(cloudsPos, attsPos, note);
                }
            } else {
                //图片上传完，再上传文本
                pEditNotes(cloudsPos, note);
            }
        } else {
            //执行下一个接口
            pUpdataNote(0, false);

        }

    }


    /**
     * (二.11)-1 edit  通过最后更新时间来与云端比较，是否该上传本地编辑的笔记
     * 上传文本
     *
     * @param cloudsPos cloudIds数据的其实操作位置
     */
    private void pEditNotes(int cloudsPos, TNNote note) {
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size() - 1)) {
            presener.pEditNote(cloudsPos, note);
        } else {
            //执行下一个接口
            pUpdataNote(0, false);
        }
    }

    /**
     * (二.11)-2 更新云端的笔记
     *
     * @param position 执行的位置
     * @param is13     (二.11)-2和(二.13)调用同一个接口，用于区分
     */
    private void pUpdataNote(int position, boolean is13) {
        if (cloudIds.size() > 0 && position < (cloudIds.size() - 1)) {
            boolean isExit = false;
            long id = cloudIds.get(position).getId();
            int lastUpdate = cloudIds.get(position).getUpdate_at();

            //本地更新
            for (int j = 0; j < allNotes.size(); j++) {
                TNNote note = allNotes.get(j);
                if (id == note.noteId && lastUpdate > note.lastUpdate) {
                    isExit = true;
                    pUpdataNote(position, id, is13);
                    break;
                }
            }
            if (!isExit) {
                pUpdataNote(position, id, is13);
            }
        } else {
            //下一个接口
            //同步回收站的笔记
            trashNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, TNConst.CREATETIME);
            pTrashNotes();

        }
    }

    /**
     * (二.11)-2/(二.13) 更新云端的笔记
     * <p>
     * p层
     */
    private void pUpdataNote(int position, long noteId, boolean is13) {
        presener.pGetNoteByNoteId(position, noteId, is13);
    }

    /**
     * (二.12) 同步回收站的笔记
     */
    private void pTrashNotes() {
        presener.pGetAllTrashNoteIds();
    }

    /**
     * (二.13) 更新云端的笔记
     * <p>
     * 该接口同(二.11)-2
     *
     * @param position
     * @param is13
     */
    private void pUpdataNote13(int position, boolean is13) {
        if (trashNoteArr.size() > 0 && (position < trashNoteArr.size() - 1) && position >= 0) {
            AllNotesIdsBean.NoteIdItemBean bean = trashNoteArr.get(position);
            long noteId = bean.getId();
            boolean trashNoteExit = false;
            for (TNNote trashNote : trashNotes) {
                if (trashNote.noteId == noteId) {
                    trashNoteExit = true;
                    break;
                }
            }
            if (!trashNoteExit) {
                pUpdataNote(position, noteId, is13);
            }
        } else {
            //同步所有接口完成，结束同步
            endSynchronize(0);
        }
    }


    //=============================================接口结果回调(成对的success+failed)======================================================


    //检查更新
    @Override
    public void onUpgradeSuccess(Object obj) {
        MainUpgradeBean bean = (MainUpgradeBean) obj;

        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
            MLog.d(TAG, info.versionCode + "," + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int newSize = bean.getSize();
        String newVersionName = bean.getVersion();
        String description = bean.getContent();
        mDownLoadAPKPath = bean.getUrl();
        MLog.d(newVersionName, newSize);
        int newVersionCode = 0;
        if (bean.getVersionCode() == 0) {
            newVersionCode = -1;
        } else {
            newVersionCode = bean.getVersionCode();
        }
        //这里需要加判断更新的字段,判断是否需要更新且只更新一次
        if (mSettings.version.equals(newVersionName)) {
            return;
        }
        mSettings.version = newVersionName;
        mSettings.savePref(false);

        //
        if (newVersionCode > info.versionCode) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout fl = (LinearLayout) layoutInflater.inflate(
                    R.layout.update, null);
            TextView hint = (TextView) fl
                    .findViewById(R.id.update_hint);
            hint.setText(String.format(getString(R.string.update_hint),
                    info.versionName, newVersionName, description));
            hint.setMovementMethod(ScrollingMovementMethod
                    .getInstance());

            ProgressBar pb = (ProgressBar) fl
                    .findViewById(R.id.update_progressbar);
            pb.setMax(newSize);
            pb.setProgress(0);
            TextView percent = (TextView) fl
                    .findViewById(R.id.update_percent);
            percent.setText(String.format("%.2fM / %.2fM (%.2f%%)",
                    pb.getProgress() / 1024f / 1024f,
                    pb.getMax() / 1024f / 1024f,
                    100f * pb.getProgress() / pb.getMax()));

            JSONObject jsonData = TNUtils.makeJSON("CONTEXT",
                    TNSettings.getInstance().topAct, "TITLE",
                    R.string.alert_Title, "VIEW", fl, "POS_BTN",
                    R.string.update_start, "NEG_BTN",
                    R.string.alert_Cancel);

            //更新弹窗
            AlertDialog dialog = TNUtilsUi.alertDialogBuilder(jsonData);
            dialog.show();

            Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            theButton.setOnClickListener(new CustomListener(dialog));
        } else {
            TNUtilsUi.showToast("当前版本已是最新");
        }
    }

    //下载完成
    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onDownloadSuccess(File filePath) {
        upgradeDialog.dismiss();
        if (filePath != null) {
            //打开文件
            TNUtilsUi.openFile(filePath.toString());
        }
    }

    @Override
    public void onDownloadFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }


    //---接口结果回调  第一次登录的同步---

    //1-1
    @Override
    public void onSyncFolderAddSuccess(Object obj, int position, int arraySize) {
        if (position < arraySize - 1) {//同步该接口的列表数据，
            //（有数组，循环调用）
            pFolderAdd(position + 1, arraySize, arrayFolderName[position + 1]);
        } else {//同步完成后，再同步其他接口列表数据
            //（有数组，循环调用）
            pTagAdd(0, arrayTagName.length, arrayTagName[0]);
        }
    }

    @Override
    public void onSyncFolderAddFailed(String msg, Exception e, int position, int arraySize) {
        MLog.e(msg);
        //
//        if (position < arraySize - 1) {//同步该接口的列表数据，
//            //（有数组，循环调用）
//            pFolderAdd(position + 1, arraySize, arrayFolderName[position + 1]);
//        } else {//同步完成后，再同步其他接口列表数据
//            //（有数组，循环调用）
//            pTagAdd(0, arrayTagName.length, arrayTagName[0]);
//        }
    }

    //1-2
    @Override
    public void onSyncTagAddSuccess(Object obj, int position, int arraySize) {
        if (position < arraySize - 1) {//同步该接口的列表数据，
            //（有数组，循环调用）
            pTagAdd(position + 1, arraySize, arrayTagName[position + 1]);
        } else {
            //
            mSettings.savePref(false);
            //执行下个接口
            syncGetFolder();
        }
    }

    @Override
    public void onSyncTagAddFailed(String msg, Exception e, int position, int arraySize) {
        MLog.e(msg);

        //
//        if (position + 1 < arraySize) {//同步该接口的列表数据
//            //（有数组，循环调用）
//            pTagAdd(position + 1, arraySize, arrayTagName[position + 1]);
//        } else {
//            mSettings.savePref(false);
//            //再调用正常的同步接口
//            syncNormalLogData();
//        }
    }

    //1-3
    @Override
    public void onSyncGetFolderSuccess(Object obj) {

        AllFolderBean allFolderBean = (AllFolderBean) obj;
        List<AllFolderItemBean> allFolderItemBeans = allFolderBean.getFolders();
        mapList.add(allFolderItemBeans);
        //更新数据库
        insertDBCatsSQL(allFolderBean, -1);

        //执行下个接口 处理递归
        syncGetFoldersByFolderId(0, true);
    }

    @Override
    public void onSyncGetFolderFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //1-4
    @Override
    public void onSyncGetFoldersByFolderIdSuccess(Object obj, long catID, int startPos, List<AllFolderItemBean> beans) {

        AllFolderBean allFolderBean = (AllFolderBean) obj;
        List<AllFolderItemBean> allFolderItemBeans = allFolderBean.getFolders();
        //判断是否有返回值
        if (allFolderBean == null || allFolderItemBeans == null || allFolderItemBeans.size() <= 0) {
            //执行下个position循环
            syncGetFoldersByFolderId(startPos + 1, false);
        } else {
            mapList.add(allFolderItemBeans);
            insertDBCatsSQL(allFolderBean, catID);
            //执行新循环
            syncGetFoldersByFolderId(0, true);
        }
    }

    @Override
    public void onSyncGetFoldersByFolderIdFailed(String msg, Exception e, long catID, int startPos, List<AllFolderItemBean> beans) {
        MLog.e(msg);
        //执行下个position循环
        syncGetFoldersByFolderId(startPos + 1, false);
    }

    //1-5
    @Override
    public void onSyncFirstFolderAddSuccess(Object obj, int workPos, int workSize, long catID, String name, int catPos, int flag) {
        if (catPos < cats.size() - 1) {
            if (flag == 1) {//groupWorks
                if (workPos < workSize - 1) {
                    pFirstFolderAdd(workPos + 1, groupWorks.length, catID, name, catPos, 1);//继续执行第1个
                } else {//groupWorks执行完，执行groupLife
                    pFirstFolderAdd(0, groupLife.length, catID, name, catPos, 2);//执行第2个
                }
            } else if (flag == 2) {//groupLife
                if (workPos < workSize - 1) {
                    pFirstFolderAdd(workPos + 1, groupLife.length, catID, name, catPos, 2);//继续执行第2个
                } else {//groupLife执行完，执行groupFun
                    pFirstFolderAdd(0, groupFun.length, catID, name, catPos, 3);//执行第3个
                }
            } else if (flag == 3) {//groupFun
                if (workPos < workSize - 1) {
                    pFirstFolderAdd(workPos + 1, groupFun.length, catID, name, catPos, 3);//继续执行第3个
                } else {//groupFun执行完，执行下个TNCat
                    syncTNCat(catPos + 1, cats.size());//执行for的外层TNCat的下一个
                }
            }
        } else {
            //执行下一个接口
            syncProfile();
        }
    }

    @Override
    public void onSyncFirstFolderAddFailed(String msg, Exception e, int workPos, int workSize, long catID, int catPos, int flag) {
        MLog.e(msg);
    }


    //----接口结果回调  正常同步---
    //2-1
    @Override
    public void onSyncProfileSuccess(Object obj) {
        ProfileBean profileBean = (ProfileBean) obj;
        //
        TNSettings settings = TNSettings.getInstance();
        long userId = TNDbUtils.getUserId(settings.username);

        settings.phone = profileBean.getPhone();
        settings.email = profileBean.getEmail();
        settings.defaultCatId = profileBean.getDefault_folder();

        if (userId != settings.userId) {
            //清空user表
            UserDbHelper.clearUsers();
        }

        JSONObject user = TNUtils.makeJSON(
                "username", settings.username,
                "password", settings.password,
                "userEmail", settings.email,
                "phone", settings.phone,
                "userId", settings.userId,
                "emailVerify", profileBean.getEmailverify(),
                "totalSpace", profileBean.getTotal_space(),
                "usedSpace", profileBean.getUsed_space());

        //更新user表
        UserDbHelper.addOrUpdateUser(user);

        //
        settings.isLogout = false;
        settings.firstLaunch = false;//在此处设置 false
        settings.savePref(false);
        //执行下个接口（该处是 第一次登录的最后一个同步接口，下一个正常登录的同步接口）
        syncOldNote();
    }

    @Override
    public void onSyncProfileAddFailed(String msg, Exception e) {
        //
        MLog.e(msg);
    }

    //2-2 OldNotePic
    @Override
    public void onSyncOldNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        String content = addOldNotes.get(notePos).content;
        OldNotePicBean oldNotePicBean = (OldNotePicBean) obj;
        //更新图片 数据库
        upDataAttIdSQL(oldNotePicBean.getId(), tnNoteAtt);

        if (notePos < noteArrySize - 1) {
            if (picPos < picArrySize - 1) {
                //继续上传下张图
                Vector<TNNoteAtt> oldNotesAtts = addOldNotes.get(notePos).atts;
                pUploadOldNotePic(picPos + 1, picArrySize, notePos, noteArrySize, oldNotesAtts.get(picPos + 1));
            } else {//所有图片上传完成，就处理
                String digest = oldNotePicBean.getMd5();
                long attId = oldNotePicBean.getId();
                //更新 content
                String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                content = content.replaceAll(s1, s2);

                //
                TNNote note = addOldNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pOldNote(notePos, noteArrySize, note, false, content);
            }
        } else {

            //
            TNNote note = addOldNotes.get(notePos);
            if (note.catId == -1) {
                note.catId = TNSettings.getInstance().defaultCatId;
            }
            pOldNote(notePos, noteArrySize, note, false, content);
        }
    }

    @Override
    public void onSyncOldNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        MLog.e(msg);
    }

    //2-3OldNoteAdd
    @Override
    public void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        OldNoteAddBean oldNoteAddBean = (OldNoteAddBean) obj;

        if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
            upDataNoteLocalIdSQL(oldNoteAddBean, addOldNotes.get(position));
        }

        if (position < arraySize - 1) {
            //执行下一个 图片
            Vector<TNNoteAtt> oldNotesAtts = addOldNotes.get(position + 1).atts;
            pUploadOldNotePic(0, oldNotesAtts.size(), position + 1, arraySize, addOldNotes.get(position + 1).atts.get(0));
        } else {
            //执行下个接口
            pGetTagList();
        }
    }

    @Override
    public void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        MLog.e(msg);

        //
//        if (position < arraySize - 1) {
//            pUploadOldNotePic(0, oldNotesAtts.size(), position + 1, arraySize, addOldNotes.get(position + 1).atts.get(0));
//        } else {
//            mSettings.syncOldDb = false;
//            mSettings.savePref(false);
//            //执行下个接口
//        }
    }

    //2-4
    @Override
    public void onSyncTagListSuccess(Object obj) {

        TagListBean tagListBean = (TagListBean) obj;
        List<TagItemBean> beans = tagListBean.getTags();
        //
        TNSettings settings = TNSettings.getInstance();
        TagDbHelper.clearTags();

        for (int i = 0; i < beans.size(); i++) {
            TagItemBean itemBean = beans.get(i);

            String tagName = itemBean.getName();
            if (TextUtils.isEmpty(tagName)) {
                tagName = "无";
            }
            JSONObject tempObj = TNUtils.makeJSON(
                    "tagName", tagName,
                    "userId", settings.userId,
                    "trash", 0,
                    "tagId", itemBean.getId(),
                    "strIndex", TNUtils.getPingYinIndex(tagName),
                    "count", itemBean.getCount()
            );
            TagDbHelper.addOrUpdateTag(tempObj);
        }

        //执行下个接口
        pAddNewNote();
    }

    @Override
    public void onSyncTagListAddFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-5
    @Override
    public void onSyncNewNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {

        String content = addNewNotes.get(notePos).content;
        OldNotePicBean newPicbean = (OldNotePicBean) obj;
        //更新图片 数据库
        upDataAttIdSQL(newPicbean.getId(), tnNoteAtt);

        if (notePos < noteArrySize - 1) {
            if (picPos < picArrySize - 1) {
                //继续上传下张图
                Vector<TNNoteAtt> newNotesAtts = addNewNotes.get(notePos).atts;
                pNewNotePic(picPos + 1, picArrySize, notePos, noteArrySize, newNotesAtts.get(picPos + 1));
            } else {//所有图片上传完成，就开始上传文本
                String digest = newPicbean.getMd5();
                long attId = newPicbean.getId();
                //更新 content
                String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                content = content.replaceAll(s1, s2);

                //
                TNNote note = addNewNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pNewNote(notePos, noteArrySize, note, false, content);
            }
        } else {

            //所有图片上传完成，就开始上传newPos的文本
            TNNote note = addNewNotes.get(notePos);
            if (note.catId == -1) {
                note.catId = TNSettings.getInstance().defaultCatId;
            }
            pNewNote(notePos, noteArrySize, note, false, content);
        }
    }

    @Override
    public void onSyncNewNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        MLog.e(msg);
    }

    //2-6
    @Override
    public void onSyncNewNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {

        OldNoteAddBean newNoteBean = (OldNoteAddBean) obj;
        //更新数据库
        if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
            upDataNoteLocalIdSQL(newNoteBean, addNewNotes.get(position));
        }


        if (position < arraySize - 1) {
            //处理position + 1下的图片上传
            Vector<TNNoteAtt> newNotesAtts = addNewNotes.get(position + 1).atts;
            pNewNotePic(0, newNotesAtts.size(), position + 1, arraySize, addNewNotes.get(position + 1).atts.get(0));
        } else {

            //执行下个接口
            recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
            recoveryNote(0);
        }
    }

    @Override
    public void onSyncNewNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        MLog.e(msg);
    }

    //2-7-1
    @Override
    public void onSyncRecoverySuccess(Object obj, long noteId, int position) {
        //更新数据库
        recoveryNoteSQL(noteId);

        //执行循环的下一个position+1数据/下一个接口
        recoveryNote(position + 1);
    }

    @Override
    public void onSyncRecoveryFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-7-2
    @Override
    public void onSyncRecoveryNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        String content = recoveryNotes.get(notePos).content;
        OldNotePicBean recoveryPicbean = (OldNotePicBean) obj;

        //更新图片 数据库
        upDataAttIdSQL(recoveryPicbean.getId(), tnNoteAtt);

        if (notePos < noteArrySize - 1) {
            if (picPos < picArrySize - 1) {
                //继续上传下张图
                Vector<TNNoteAtt> newNotesAtts = recoveryNotes.get(notePos).atts;
                pRecoveryNotePic(picPos + 1, picArrySize, notePos, noteArrySize, newNotesAtts.get(picPos + 1));
            } else {//所有图片上传完成，就开始上传文本
                String digest = recoveryPicbean.getMd5();
                long attId = recoveryPicbean.getId();
                //更新 content
                String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                content = content.replaceAll(s1, s2);

                //所有图片上传完成，就开始上传newPos的文本
                TNNote note = recoveryNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pRecoveryNoteAdd(notePos, noteArrySize, note, true, content);
            }
        } else {

            //所有图片上传完成，就开始上传newPos的文本
            TNNote note = recoveryNotes.get(notePos);
            if (note.catId == -1) {
                note.catId = TNSettings.getInstance().defaultCatId;
            }
            pRecoveryNoteAdd(notePos, noteArrySize, note, true, content);
        }
    }

    @Override
    public void onSyncRecoveryNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        MLog.e(msg);
    }

    //2-7-3
    @Override
    public void onSyncRecoveryNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {

        OldNoteAddBean recoveryNoteBean = (OldNoteAddBean) obj;
        //更新数据库
        if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
            upDataNoteLocalIdSQL(recoveryNoteBean, recoveryNotes.get(position));
        }

        //处理position + 1下的TNNote/下一个接口
        recoveryNote(position + 1);

    }

    @Override
    public void onSyncRecoveryNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        MLog.e(msg);
    }

    //2-8
    @Override
    public void onSyncDeleteNoteSuccess(Object obj, long noteId, int position) {

        //更新数据
        updataDeleteNoteSQL(noteId);

        //执行下一个
        pDelete(position + 1);
    }

    @Override
    public void onSyncDeleteNoteFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-9-1
    @Override
    public void onSyncpDeleteRealNotes1Success(Object obj, long noteId, int position) {
        isRealDelete1 = true;
        //更新数据
        updataDeleteNoteSQL(noteId);

        if (isRealDelete1 && isRealDelete2) {
            //执行下一个
            pRealDelete(position + 1);

            //复原 false,供下次循环使用
            isRealDelete1 = false;
            isRealDelete2 = false;
        }

    }

    @Override
    public void onSyncDeleteRealNotes1Failed(String msg, Exception e, int position) {
        isRealDelete1 = true;
        MLog.e(msg);
    }

    //2-9-2
    @Override
    public void onSyncDeleteRealNotes2Success(Object obj, long noteId, int position) {
        isRealDelete2 = true;
        //更新数据库
        deleteRealSQL(noteId, position);
    }

    @Override
    public void onSyncDeleteRealNotes2Failed(String msg, Exception e, int position) {
        isRealDelete2 = true;
        MLog.e(msg);
    }

    //2-10
    @Override
    public void onSyncAllNotesIdSuccess(Object obj) {
        cloudIds = (List<AllNotesIdsBean.NoteIdItemBean>) obj;

        //与云端同步数据 sjy-0623
        allNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
        for (int i = 0; i < allNotes.size(); i++) {
            boolean isExit = false;
            final TNNote note = allNotes.get(i);
            //查询本地是否存在
            for (int j = 0; j < cloudIds.size(); j++) {
                if (note.noteId == cloudIds.get(j).getId()) {
                    isExit = true;
                    break;
                }
            }

            //不存在就删除  /使用异步
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            if (!isExit && note.syncState != 7) {

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        TNDb.beginTransaction();
                        try {
                            //
                            TNDb.getInstance().deleteNotesByNoteIdSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, note.noteId);

                            TNDb.setTransactionSuccessful();
                        } finally {
                            TNDb.endTransaction();
                        }
                    }
                });
            }
        }

        //edit  通过最后更新时间来与云端比较是否该上传本地编辑的笔记
        editNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 4);
        //执行下一个接口
        pEditNotePic(0);
    }

    @Override
    public void onSyncAllNotesIdAddFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-10-1
    @Override
    public void onSyncEditNotePicSuccess(Object obj, int cloudsPos, int attsPos, TNNote tnNote) {
        TNNote note = tnNote;
        OldNotePicBean editPicbean = (OldNotePicBean) obj;
        note.atts.get(attsPos).digest = editPicbean.getMd5();
        note.atts.get(attsPos).attId = editPicbean.getId();
        String s1 = String.format("<tn-media hash=\"%s\" />", note.atts.get(attsPos).digest);
        String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", note.atts.get(attsPos).digest, note.atts.get(attsPos).attId);
        note.content = note.content.replaceAll(s1, s2);
        //更新图片 数据库
        upDataAttIdSQL(editPicbean.getId(), note.atts.get(attsPos));
        //执行下一个attsPos的图片上传
        pEditNotePic(cloudsPos, attsPos + 1, note);
    }

    @Override
    public void onSyncEditNotePicFailed(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote) {
        MLog.e(msg);
    }


    //2-11-1
    @Override
    public void onSyncEditNoteSuccess(Object obj, int cloudsPos, TNNote note) {

        //更新下一个cloudsPos位置的数据
        updataEditNotes(cloudsPos, note);
    }

    @Override
    public void onSyncEditNoteAddFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-11-2
    @Override
    public void onSyncpGetNoteByNoteIdSuccess(Object obj, int position, boolean is13) {
        updateNote((GetNoteByNoteIdBean) obj);
        if (is13) {
            pUpdataNote13(position + 1, is13);
        } else {
            //执行一个position或下一个接口
            pUpdataNote(position + 1, false);
        }

    }

    @Override
    public void onSyncpGetNoteByNoteIdFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-12
    @Override
    public void onSyncpGetAllTrashNoteIdsSuccess(Object obj) {
        trashNoteArr = (List<AllNotesIdsBean.NoteIdItemBean>) obj;
        ExecutorService executorService = Executors.newCachedThreadPool();//开启线程池
        for (final TNNote trashNote : trashNotes) {
            boolean trashNoteExit = false;
            for (int i = 0; i < trashNoteArr.size(); i++) {
                AllNotesIdsBean.NoteIdItemBean bean = trashNoteArr.get(i);
                long noteId = bean.getId();
                if (trashNote.noteId == noteId) {
                    trashNoteExit = true;
                    break;
                }
            }
            if (!trashNoteExit) {

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        TNDb.beginTransaction();
                        try {
                            //
                            TNDb.getInstance().deleteSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, new String[]{trashNote.noteId + ""});
                            TNDb.setTransactionSuccessful();
                        } finally {
                            TNDb.endTransaction();
                        }
                    }
                });
            }
        }
        //执行下一个接口
        pUpdataNote13(0, true);

    }

    @Override
    public void onSyncpGetAllTrashNoteIdsFailed(String msg, Exception e) {

    }


}
