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
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
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
    private String[] arrayFolderName;//第一次登录，要同步的数据，（1）
    private String[] arrayTagName;//第一次登录，要同步的数据，（2）
    private Vector<TNCat> cats;//第一次登录，要同步的数据，（3）
    private String[] groupWorks;//（3）下第一个数组数据
    private String[] groupLife;//（3）下第2个数组数据
    private String[] groupFun;//（3）下第3个数组数据
    private Vector<TNNote> addOldNotes;//（2）正常同步，第一个调用数据
    Vector<TNNoteAtt> oldNotesAtts;//（1）正常同步，第一个调用数据中第一调用的数据
    //接口返回数据

    private List<List<AllFolderItemBean>> mapList;//递归调用使用的数据集合，size最大是5；//后台需求


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

    /**
     * 调用图片上传，就触发更新db
     *
     * @param attrId
     */
    private void upDataAttIdSQL(long attrId) {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().upDataAttIdSQL(TNSQLString.ATT_UPDATE_SYNCSTATE_ATTID, 2, attrId, -1);
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
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

    //-------------------------------------handler处理同步------------------------------------------


    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case FISRTLAUCh_FOLDER_ADD://
                break;
        }
    }

    //=============================================p层调用======================================================

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
                        //执行上一层的循环
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
            syncProfile();
        }
    }

    /**
     * 更新 postion的TNCat数据
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
                    pFirstFolderAdd(0, groupWorks.length, tempCat.catId, postion, 1);//执行第一个
                } else {
                    if (groupLife != null) {
                        //执行顺序:groupWorks-->groupLife-->groupFun
                        pFirstFolderAdd(0, groupLife.length, tempCat.catId, postion, 2);//执行第2个
                    } else {
                        //保险一点，我对这个数据不甚了解 sjy 0622
                        if (groupFun != null) {
                            //执行顺序:groupWorks-->groupLife-->groupFun
                            pFirstFolderAdd(0, groupFun.length, tempCat.catId, postion, 2);//执行第3个
                        } else {
                            //postion下没有数据，执行下个position
                            syncTNCat(postion + 1, catsSize);
                        }

                    }
                }
            }
        } else {
            syncProfile();
        }
    }

    /**
     * 具体执行TNCat的步骤 p层调用
     *
     * @param workPos
     * @param workSize
     * @param catID
     * @param catPos
     * @param flag     TNCat下有三条数据数组，flag决定执行哪一条数据的标记
     */
    private void pFirstFolderAdd(int workPos, int workSize, long catID, int catPos, int flag) {
        presener.pFirstFolderAdd(workPos, workSize, catID, catPos, flag);
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
                syncGetFolder();
            }
        } else {
            syncGetFolder();
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
     * （二.3）正常同步 （如果没有老数据，不执行(二.1)和(二.2)的接口）
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


    //=============================================接口结果回调======================================================

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

    //1
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
        //TODO
//        if (position < arraySize - 1) {//同步该接口的列表数据，
//            //（有数组，循环调用）
//            pFolderAdd(position + 1, arraySize, arrayFolderName[position + 1]);
//        } else {//同步完成后，再同步其他接口列表数据
//            //（有数组，循环调用）
//            pTagAdd(0, arrayTagName.length, arrayTagName[0]);
//        }
    }

    //2
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

        //TODO
//        if (position + 1 < arraySize) {//同步该接口的列表数据
//            //（有数组，循环调用）
//            pTagAdd(position + 1, arraySize, arrayTagName[position + 1]);
//        } else {
//            mSettings.firstLaunch = true;
//            mSettings.savePref(false);
//            //再调用正常的同步接口
//            syncNormalLogData();
//        }
    }

    //3
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

    //4
    @Override
    public void onSyncGetFoldersByFolderIdSuccess(Object obj, long catID, int startPos, List<AllFolderItemBean> beans) {

        AllFolderBean allFolderBean = (AllFolderBean) obj;
        List<AllFolderItemBean> allFolderItemBeans = allFolderBean.getFolders();
        mapList.add(allFolderItemBeans);

        insertDBCatsSQL(allFolderBean, catID);

        //执行下个position循环
        syncGetFoldersByFolderId(startPos, false);
    }

    @Override
    public void onSyncGetFoldersByFolderIdFailed(String msg, Exception e, long catID, int position, List<AllFolderItemBean> beans) {

    }

    //6
    @Override
    public void onSyncFirstFolderAddSuccess(Object obj, int workPos, int workSize, long catID, int catPos, int flag) {
        if (catPos < cats.size() - 1) {
            if (flag == 1) {//groupWorks
                if (workPos < workSize - 1) {
                    pFirstFolderAdd(workPos + 1, groupWorks.length, catID, catPos, 1);//继续执行第1个
                } else {//groupWorks执行完，执行groupLife
                    pFirstFolderAdd(0, groupLife.length, catID, catPos, 2);//执行第2个
                }
            } else if (flag == 2) {//groupLife
                if (workPos < workSize - 1) {
                    pFirstFolderAdd(workPos + 1, groupLife.length, catID, catPos, 2);//继续执行第2个
                } else {//groupLife执行完，执行groupFun
                    pFirstFolderAdd(0, groupFun.length, catID, catPos, 3);//执行第3个
                }
            } else if (flag == 3) {//groupFun
                if (workPos < workSize - 1) {
                    pFirstFolderAdd(workPos + 1, groupFun.length, catID, catPos, 3);//继续执行第3个
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

    //7
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
        settings.firstLaunch = false;
        settings.savePref(false);
        //执行下个接口 TODO

    }

    @Override
    public void onSyncProfileAddFailed(String msg, Exception e) {
        //
        MLog.e(msg);
    }
    //----接口结果回调  正常同步---

    //OldNotePic
    @Override
    public void onSyncOldNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize) {
        String content = addOldNotes.get(notePos).content;
        OldNotePicBean oldNotePicBean = (OldNotePicBean) obj;
        //更新图片 数据库
        upDataAttIdSQL(oldNotePicBean.getId());

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

    //OldNoteAdd
    @Override
    public void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        OldNoteAddBean oldNoteAddBean = (OldNoteAddBean) obj;

        if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
            upDataNoteLocalIdSQL(oldNoteAddBean, addOldNotes.get(position));
        }

        if (position < arraySize - 1) {
            pUploadOldNotePic(0, oldNotesAtts.size(), position + 1, arraySize, addOldNotes.get(position + 1).atts.get(0));
        } else {
            mSettings.syncOldDb = true;
            mSettings.savePref(false);
            //执行下个接口
            syncGetFolder();
        }
    }

    @Override
    public void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        MLog.e(msg);

        //TODO
//        if (position < arraySize - 1) {
//            pUploadOldNotePic(0, oldNotesAtts.size(), position + 1, arraySize, addOldNotes.get(position + 1).atts.get(0));
//        } else {
//            mSettings.syncOldDb = false;
//            mSettings.savePref(false);
//            //执行下个接口
//            syncProfile();
//        }
    }


}
