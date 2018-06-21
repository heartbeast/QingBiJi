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
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Utils.TNActivityManager;
import com.thinkernote.ThinkerNote.Views.CustomDialog;
import com.thinkernote.ThinkerNote._constructer.presenter.MainPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.IMainPresener;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;

import org.json.JSONObject;

import java.util.Vector;

/**
 * 主界面
 * 说明：进入主界面：会同时执行2个异步：onCreate的更新 和 onResume下的configView的同步
 * 同步功能说明：由10多个接口串行调用，比较复杂，所以要注意调用顺序
 */
public class TNMainAct extends TNActBase implements OnClickListener, OnMainListener {
    //==================================同步常量=======================================
    //第一次登录的同步常量
    public static final int FISRTLAUCh_FOLDER_ADD = 11;//1

    //正常登录的同步常量
    public static final int HANDLER_NOTEADD = 101;//1

    //==================================变量=======================================
    private long mLastClickBackTime = 0;
    private String mDownLoadAPKPath = "";
    private TextView mTimeView;
    private TNSettings mSettings = TNSettings.getInstance();

    //
    private IMainPresener presener;
    private String[] arrayFolderName;//第一次登录，要同步的数据，（1）先调用
    private String[] arrayTagName;//第一次登录，要同步的数据，（2）后调用
    private Vector<TNNote> addOldNotes;//（2）正常同步，第一个调用数据
    Vector<TNNoteAtt> oldNotesAtts;//（1）正常同步，第一个调用数据中第一调用的数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //关闭其他界面
        TNActivityManager.getInstance().finishOtherActivity(this);

        //TODO
        TNAction.regResponder(TNActionType.Synchronize, this, "respondSynchronize");
        TNAction.regResponder(TNActionType.SynchronizeEdit, this, "respondSynchronizeEdit");
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

    public void respondSynchronize(TNAction aAction) {
        if (aAction.inputs.size() > 0 && !aAction.inputs.get(0).equals("home")) {
            return;
        }

        if (!TNActionUtils.isSynchronizing(aAction))
            findViewById(R.id.main_sync_btn).clearAnimation();

        if (aAction.result == TNActionResult.Cancelled) {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
        } else if (!TNHandleError.handleResult(this, aAction, false)) {
            TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
            if (TNActionUtils.isSynchroniz(aAction)) {
                TNSettings settings = TNSettings.getInstance();
                settings.originalSyncTime = System.currentTimeMillis();
                settings.savePref(false);
                mTimeView.setText("上次同步时间：" + TNUtilsUi.formatDate(TNMainAct.this,
                        settings.originalSyncTime / 1000L));
            }
        } else {
            TNUtilsUi.showNotification(this, R.string.alert_Synchronize_Stoped, true);
        }
    }

    public void respondSynchronizeEdit(TNAction aAction) {
        if (aAction.result == TNActionResult.Cancelled) {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
        } else if (!TNHandleError.handleResult(this, aAction, false)) {
            TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
            if (TNActionUtils.isSynchroniz(aAction)) {
                TNSettings settings = TNSettings.getInstance();
                settings.originalSyncTime = System.currentTimeMillis();
                settings.savePref(false);
                mTimeView.setText("上次同步时间：" + TNUtilsUi.formatDate(TNMainAct.this,
                        settings.originalSyncTime / 1000L));
            }
        } else {
            TNUtilsUi.showNotification(this, R.string.alert_Synchronize_Stoped, true);
        }
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


    //更新弹窗的自定义监听（确定按钮的监听）
    class CustomListener implements View.OnClickListener {
        private final AlertDialog dialog;

        public CustomListener(AlertDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new OnKeyListener() {
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

            Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);//开始下载按钮
            theButton.setText(getString(R.string.update_downloading));
            theButton.setEnabled(false);

            Button negButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);//取消下载按钮
            negButton.setEnabled(false);

            //下载接口
            downloadNewAPK(mDownLoadAPKPath, dialog);
        }
    }

    //调用图片上传，就触发更新db
    private void executeSQLUpdata(long attrId) {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().executeSQLUpData(TNSQLString.ATT_UPDATE_SYNCSTATE_ATTID, 2, attrId, -1);
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
    }

    //-------------------------------------handler处理同步------------------------------------------


    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case FISRTLAUCh_FOLDER_ADD://
                break;
        }
    }

    //-------------------------------------p层调用------------------------------------------
    //检查更新
    private void findUpgrade() {
        presener.pUpgrade("HOME");

    }

    //TODO
    private void downloadNewAPK(String url, Dialog dialog) {
//        presener.pDownload(url, dialog);

        TNAction.runActionAsync(TNActionType.UpdateSoftware, url, dialog);
    }

    //-------第一次登录同步的p调用-------

    /**
     * (一)同步 第一个调用的方法
     * 执行顺序：先arrayFolderName对应的所有接口，再arrayTagName对应的所有接口，
     * 接口个数=arrayFolderName.size + arrayTagName.size
     */

    private void synchronizeData() {
        if (mSettings.firstLaunch) {//如果第一次启动，执行该处方法
            //需要同步的文件数据
            arrayFolderName = new String[]{TNConst.FOLDER_DEFAULT, TNConst.FOLDER_MEMO, TNConst.GROUP_FUN, TNConst.GROUP_WORK, TNConst.GROUP_LIFE};
            arrayTagName = new String[]{TNConst.TAG_IMPORTANT, TNConst.TAG_TODO, TNConst.TAG_GOODSOFT};

            //同步第一个数据（有数组，循环调用）
            pFolderAdd(0, arrayFolderName.length, arrayFolderName[0]);
        } else {//如果正常启动，执行该处
            syncNormalLogData();
        }

        //TODO
//        TNAction.runActionAsync(TNActionType.Synchronize, "home");
    }

    /**
     * 第一次登录同步
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

    //-------正常登录同步的p调用-------

    /**
     * （二）正常登录的数据同步（非第一次登录的同步）
     * 执行顺序：同步老数据(先上传图片接口，再OldNote接口)，没有老数据就同步用户信息接口
     * 接口个数 = addOldNotes.size * oldNotesAtts.size;
     */
    private void syncNormalLogData() {
        if (!mSettings.syncOldDb) {
            //add老数据库的笔记
            addOldNotes = TNDbUtils.getOldDbNotesByUserId(TNSettings.getInstance().userId);
            if (addOldNotes.size() > 0) {
                //先 上传数组的第一个
                TNNote tnNote = addOldNotes.get(0);
                oldNotesAtts = tnNote.atts;
                if (oldNotesAtts.size() > 0) {//有图，先上传图片
                    pUploadOldNotePic(0, oldNotesAtts.size(), 0, addOldNotes.size(), oldNotesAtts.get(0));
                } else {//如果没有图片，就执行OldNote
                    pOldNote(0, addOldNotes.size(), addOldNotes.get(0), false, addOldNotes.get(0).content);
                }
            } else {
                syncProfile();
            }
        } else {
            syncProfile();
        }
    }

    /**
     * (二.1)正常同步 第一个执行的接口 上传图片OldNotePic 循环调用
     * 和（二.2组成双层for循环，该处是最内层for执行）
     */
    private void pUploadOldNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        presener.pUploadOldNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.2)正常同步 第2个执行的接口 循环调用
     * 和（二.1组成双层for循环，该处是最外层for执行）
     */

    private void pOldNote(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        presener.pOldNoteAdd(position, arraySize, tnNoteAtt, isNewDb, content);
    }

    /**
     * （二.3）正常同步
     */
    private void syncProfile() {
        presener.pProfile();
    }

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


    //-------------------------------------接口结果回调------------------------------------------

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

            Button theButton = dialog
                    .getButton(DialogInterface.BUTTON_POSITIVE);
            theButton.setOnClickListener(new CustomListener(dialog));
        } else {
            TNUtilsUi.showToast("当前版本已是最新");
        }
    }

    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }


    //---接口结果回调  第一次登录的同步---

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
        if (position < arraySize - 1) {//同步该接口的列表数据，
            //（有数组，循环调用）
            pFolderAdd(position + 1, arraySize, arrayFolderName[position + 1]);
        } else {//同步完成后，再同步其他接口列表数据
            //（有数组，循环调用）
            pTagAdd(0, arrayTagName.length, arrayTagName[0]);
        }
    }

    @Override
    public void onSyncTagAddSuccess(Object obj, int position, int arraySize) {
        if (position < arraySize - 1) {//同步该接口的列表数据，
            //（有数组，循环调用）
            pTagAdd(position + 1, arraySize, arrayTagName[position + 1]);
        } else {
            //
            mSettings.firstLaunch = false;
            mSettings.savePref(false);
            //完成后，再调用正常的同步接口
            syncNormalLogData();
        }
    }

    @Override
    public void onSyncTagAddFailed(String msg, Exception e, int position, int arraySize) {
        MLog.e(msg);
        if (position + 1 < arraySize) {//同步该接口的列表数据
            //（有数组，循环调用）
            pTagAdd(position + 1, arraySize, arrayTagName[position + 1]);
        } else {
            mSettings.firstLaunch = true;
            mSettings.savePref(false);
            //再调用正常的同步接口
            syncNormalLogData();
        }
    }

    //----接口结果回调  正常同步---

    //OldNotePic
    @Override
    public void onSyncOldNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize) {
        String content = addOldNotes.get(notePos).content;
        OldNotePicBean oldNotePicBean = (OldNotePicBean) obj;
        //更新数据库
        executeSQLUpdata(oldNotePicBean.getId());

        if (notePos < noteArrySize - 1) {
            if (picPos < picArrySize - 1) {
                //继续上传下张图
                pUploadOldNotePic(picPos + 1, picArrySize, notePos, noteArrySize, oldNotesAtts.get(picPos + 1));
            } else {//所有图片上传完成，就处理
                String digest = oldNotePicBean.getMd5();
                long attId = oldNotePicBean.getId();
                //更新 content
                String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                content = content.replaceAll(s1, s2);
                pOldNote(notePos, noteArrySize, addOldNotes.get(notePos), false, content);
            }
        } else {
            pOldNote(notePos, noteArrySize, addOldNotes.get(notePos), false, content);
        }
    }

    @Override
    public void onSyncOldNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        //TODO 考虑如何不补充
        TNUtilsUi.showToast(msg);
    }

    //OldNoteAdd
    @Override
    public void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        if (position < arraySize - 1) {
            pUploadOldNotePic(0, oldNotesAtts.size(), position + 1, arraySize, addOldNotes.get(position + 1).atts.get(0));
        } else {
            mSettings.syncOldDb = true;
            mSettings.savePref(false);
            //执行下个接口
            syncProfile();
        }
    }

    @Override
    public void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        if (position < arraySize - 1) {
            pUploadOldNotePic(0, oldNotesAtts.size(), position + 1, arraySize, addOldNotes.get(position + 1).atts.get(0));
        } else {
            mSettings.syncOldDb = false;
            mSettings.savePref(false);
            //执行下个接口
            syncProfile();
        }
    }

    @Override
    public void onSyncProfileSuccess(Object obj) {

    }

    @Override
    public void onSyncProfileAddFailed(String msg, Exception e) {

    }

}
