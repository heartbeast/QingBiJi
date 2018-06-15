package com.thinkernote.ThinkerNote.Activity;

import java.io.File;
import java.lang.reflect.Method;

import org.json.JSONObject;

import com.iflytek.speech.SpeechError;
import com.iflytek.speech.SynthesizerPlayer;
import com.iflytek.speech.SynthesizerPlayerListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNDownloadAttService;
import com.thinkernote.ThinkerNote.General.TNDownloadAttService.OnDownloadEndListener;
import com.thinkernote.ThinkerNote.General.TNDownloadAttService.OnDownloadStartListener;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Other.PoPuMenuView;
import com.thinkernote.ThinkerNote.Other.PoPuMenuView.OnPoPuMenuItemClickListener;
import com.thinkernote.ThinkerNote._constructer.presenter.NoteViewPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.INoteViewPresener;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewListener;
import com.thinkernote.ThinkerNote.base.TNActBase;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * 笔记详情
 */
public class TNNoteViewAct extends TNActBase implements OnClickListener,
        SynthesizerPlayerListener,
        OnDownloadEndListener,
        OnDownloadStartListener,
        OnPoPuMenuItemClickListener,
        OnNoteViewListener

{

    public static final long ATT_MAX_DOWNLOAD_SIZE = 50 * 1024;

    // Class members
    // -------------------------------------------------------------------------------
    private Dialog mProgressDialog = null;
    private JSInterface mJSInterface;
    private TNNoteAtt mCurAtt;
    private long mNoteLocalId;
    private TNNote mNote;
    private Tencent mTencent;
    private IUiListener mListener;

    private SynthesizerPlayer mSynthesizerPlayer;
    private String mPlainText = null;
    private int mStartPos = 0;
    private int mEndPos = 0;
    private float mScale;
    private GestureDetector mGestureDetector;

    private PoPuMenuView mPopuMenu;

    private WebView mWebView;

    //p
    private INoteViewPresener presener;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (!isFinishing())
                    openContextMenu(findViewById(R.id.noteview_openatt_menu));
            } else if (msg.what == 2) {
                TNNoteAtt att = (TNNoteAtt) msg.getData()
                        .getSerializable("att");

                String s = "<img name=\\\"loading\\\" src=\\\"file:///android_asset/download.png\\\" /><span name=\\\"abcd\\\"><br />%s(%s)</span>";
                s = String.format(s, att.attName, att.size / 1024 + "K");
                mWebView.loadUrl(String.format("javascript:wave(\"%d\", \"%s\")",
                        att.attId, s));
                Log.d(TAG, "start javascript:loading");
                mWebView.loadUrl("javascript:loading()");
            } else if (msg.what == 3) {
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                mWebView.loadDataWithBaseURL("", mNote
                        .makeHtml((int) (dm.widthPixels / dm.scaledDensity)), "text/html", "utf-8", null);
            } else if (msg.what == 4) {
                Bundle b = msg.getData();
                mWebView.loadUrl(String.format("javascript:wave(\"%d\", \"%s\")",
                        b.getLong("attLocalId"), b.getString("s")));
            }
            super.handleMessage(msg);
        }
    };

    // Activity methods
    // -------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noteview);
        mNoteLocalId = getIntent().getExtras().getLong("NoteLocalId");

        setViews();

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScale = metric.scaledDensity;

        // register action
        TNAction.regResponder(TNActionType.Synchronize, this, "respondSynchronize");
        TNAction.regResponder(TNActionType.SyncNoteAtt, this, "respondSyncNoteAtt");
        TNAction.regResponder(TNActionType.NoteLocalDelete, this, "respondNoteHandle");
        TNAction.regResponder(TNActionType.NoteLocalRealDelete, this, "respondNoteHandle");
        TNAction.regResponder(TNActionType.NoteLocalRecovery, this, "respondNoteHandle");
        TNAction.regResponder(TNActionType.GetAllDataByNoteId, this, "respondGetAllDataByNoteId");
        TNAction.regResponder(TNActionType.NoteLocalChangeTag, this, "respondNoteLocalChangeTag");

        presener = new NoteViewPresenterImpl(this, this);


        mTencent = Tencent.createInstance(TNConst.QQ_APP_ID, this.getApplicationContext());
        mListener = new IUiListener() {
            @Override
            public void onError(UiError arg0) {
                TNUtilsUi.showToast("分享失败：" + arg0.errorMessage);
            }

            @Override
            public void onComplete(JSONObject jobj) {
                TNUtilsUi.showToast("分享成功");
            }

            @Override
            public void onCancel() {
//				TNUtilsUi.showToast("分享取消");
            }
        };

        // initialize
        findViewById(R.id.noteview_home).setOnClickListener(this);
        findViewById(R.id.noteview_edit).setOnClickListener(this);
        findViewById(R.id.noteview_read_close).setOnClickListener(this);
        findViewById(R.id.noteview_read_play).setOnClickListener(this);
        findViewById(R.id.noteview_more).setOnClickListener(this);

        registerForContextMenu(findViewById(R.id.noteview_openatt_menu));
        registerForContextMenu(findViewById(R.id.noteview_read_menu));
        registerForContextMenu(findViewById(R.id.noteview_share_menu));

        TNDownloadAttService download = TNDownloadAttService.getInstance();
        download.setOnDownloadEndListener(this);
        download.setOnDownloadStartListener(this);
        mGestureDetector = new GestureDetector(this, new TNGestureListener());

        mWebView = (WebView) findViewById(R.id.noteview_web);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mJSInterface = new JSInterface();
        mWebView.addJavascriptInterface(mJSInterface, "demo");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "onPageFinished:" + url);
                super.onPageFinished(view, url);

                view.loadUrl("javascript:loading()");
                startAutoDownload();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading:" + url);
                super.shouldOverrideUrlLoading(view, url);

                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }

                // Otherwise allow the OS to handle it
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                TNUtilsDialog.startIntent(TNNoteViewAct.this, intent,
                        R.string.alert_NoteView_CantOpenMsg);

                return true;
            }

        });

        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.noteview_toolbar_layout,
                R.drawable.toolbg);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.noteview_edit, R.drawable.editnote);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.noteview_more, R.drawable.more);
        TNUtilsSkin.setViewBackground(this, null, R.id.noteview_page,
                R.drawable.page_bg);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//		configView();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onDestroy() {
        mProgressDialog.dismiss();
        if (mPopuMenu != null)
            mPopuMenu.dismiss();
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mTencent.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == R.id.noteview_binding_sina) {
            String accessToken = data.getStringExtra("AccessToken");
            String uniqueId = data.getStringExtra("UniqueId");
            if (accessToken != null) {
                Bundle b = new Bundle();
                b.putLong("NoteId", mNote.noteId);
                b.putString("AccessToken", accessToken);
                b.putString("UniqueId", uniqueId);
                // TODO 无
//				startActivity(TNWeiboSendAct.class, b);
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        outBundle.putInt("START_POS", mStartPos);
        outBundle.putInt("END_POS", mEndPos);
        outBundle.putLong("NOTE_ID", mNoteLocalId);
        super.onSaveInstanceState(outBundle);
    }


    @Override
    public void onRestoreInstanceState(Bundle outBundle) {
        super.onRestoreInstanceState(outBundle);
        mStartPos = outBundle.getInt("START_POS");
        mEndPos = outBundle.getInt("END_POS");
        mNoteLocalId = outBundle.getLong("NOTE_ID");
        configView();
    }


    protected void configView() {
        mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);
        if (createStatus == 0) {
            TNDownloadAttService.getInstance().init(this, mNote);
        } else {
            TNDownloadAttService.getInstance().updateNote(mNote);
        }
        //判断是否是回收站的笔记，如果是顶部显示还原的按钮
        if (mNote.trash == 1) {
            ((ImageButton) findViewById(R.id.noteview_more))
                    .setImageResource(R.drawable.shiftdelete);
            ((ImageButton) findViewById(R.id.noteview_edit))
                    .setImageResource(R.drawable.restorenote);
        } else {
            ((ImageButton) findViewById(R.id.noteview_more))
                    .setImageResource(R.drawable.more);
            ((ImageButton) findViewById(R.id.noteview_edit))
                    .setImageResource(R.drawable.editnote);
        }

        if (mNote.syncState == 1) {
            if (!TNUtils.isNetWork()) {
                Message msg = new Message();
                msg.what = 3;
                mHandler.sendMessage(msg);
                TNUtilsUi.alert(this, R.string.alert_NoteView_NetNotWork);
            } else {
                mWebView.loadDataWithBaseURL("", getString(R.string.getingcontent), "text/html", "utf-8", null);
                Log.i(TAG, "1 -> SyncNoteContent");
                pGetNote(mNote.noteId);

            }
        } else {
            Message msg = new Message();
            msg.what = 3;
            mHandler.sendMessage(msg);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getRepeatCount() == 0) {
                if (mSynthesizerPlayer != null) {
                    if (mSynthesizerPlayer.getState().toString().equals("PLAYING")) {
                        mSynthesizerPlayer.pause();
                        ImageButton playBtn = (ImageButton) findViewById(R.id.noteview_read_play);
                        playBtn.setImageResource(R.drawable.ic_media_play);
                        return true;
                    } else if (mSynthesizerPlayer.getState().toString()
                            .equals("PAUSED")) {
                        mSynthesizerPlayer.cancel();
                        setReadBarVisible(false);
                        return true;
                    }
                }
            }
            TNActionUtils.stopNoteSyncing();
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configView();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (null != mSynthesizerPlayer) {
            if (mSynthesizerPlayer.getState().toString().equals("PLAYING")) {
                mSynthesizerPlayer.pause();
                ImageButton playBtn = (ImageButton) findViewById(R.id.noteview_read_play);
                playBtn.setImageResource(R.drawable.ic_media_play);
            }
        }
    }


    // ContextMenu
    // -------------------------------------------------------------------------------
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.noteview_openatt_menu:
                getMenuInflater().inflate(R.menu.openatt_menu, menu);
                break;

            case R.id.noteview_read_menu:
                getMenuInflater().inflate(R.menu.read_menu, menu);
                break;

            case R.id.noteview_share_menu:
                getMenuInflater().inflate(R.menu.noteview_share, menu);
                break;

            default:
                Log.d(TAG, "onCreateContextMenu default");
                break;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.openatt_menu_view: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(mCurAtt.path)),
                        TNUtilsAtt.getMimeType(mCurAtt.type, mCurAtt.attName));
                TNUtilsDialog.startIntent(this, intent,
                        R.string.alert_NoteView_CantOpenAttMsg);
                break;
            }

            case R.id.openatt_menu_save: {
                saveAttDialog();
                break;
            }

            case R.id.openatt_menu_send: {
                try {
                    String temp = TNUtilsAtt.getTempPath(mCurAtt.path);
                    TNUtilsAtt.copyFile(mCurAtt.path, temp);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType(TNUtilsAtt.getMimeType(mCurAtt.type,
                            mCurAtt.attName));
                    intent.putExtra(Intent.EXTRA_STREAM,
                            Uri.fromFile(new File(temp)));
                    TNUtilsDialog.startIntent(this, intent,
                            R.string.alert_NoteView_CantSendAttMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }

            case R.id.read_menu_restart:
                mStartPos = 0;
                setReadBarVisible(true);
                if (mSynthesizerPlayer != null)
                    mSynthesizerPlayer.playText(getNextReadStr(), null, this);
                break;

            case R.id.read_menu_continue:
                setReadBarVisible(true);
                if (mSynthesizerPlayer != null)
                    mSynthesizerPlayer.playText(getNextReadStr(), null, this);
                break;

            case R.id.noteview_menu_shareto_WX: {
                if (mNote.syncState == 1) {
                    TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Share);
                    break;
                }
                TNUtilsUi.sendToWX(this, mNote, false);
                break;
            }

            case R.id.noteview_menu_shareto_WXCycle: {
                if (mNote.syncState == 1) {
                    TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Share);
                    break;
                }
                TNUtilsUi.sendToWX(this, mNote, true);
                break;
            }

            case R.id.noteview_menu_shareto_QQ: {
                if (mNote.syncState == 1) {
                    TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Share);
                    break;
                }
                TNUtilsUi.sendToQQ(TNNoteViewAct.this, mNote, mTencent, mListener);
                break;
            }

            case R.id.noteview_menu_shareto_SMS: {
                if (mNote.syncState == 1) {
                    TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Share);
                    break;
                }
                TNUtilsUi.sendToSMS(this, mNote);
                break;
            }

            case R.id.share_url_menu_copy: {
                ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                c.setText("http://www.qingbiji.cn/note/" + TNUtils.Hash17(mNote.noteId));
                break;
            }

            case R.id.share_url_menu_send: {
                String msg = getString(R.string.shareinfo_publicnote_url, mNote.title, TNUtils.Hash17(mNote.noteId));
                String email = String.format("mailto:?subject=%s&body=%s", mNote.title, msg);
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(email));
                TNUtilsDialog.startIntent(this, intent,
                        R.string.alert_About_CantSendEmail);
                break;
            }

            case R.id.share_url_menu_open: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.qingbiji.cn/note/" + TNUtils.Hash17(mNote.noteId)));
                TNUtilsDialog.startIntent(this, intent,
                        R.string.alert_About_CantOpenWeb);
                break;
            }

            case R.id.share_url_menu_sms: {
                String msg = getString(R.string.shareinfo_publicnote_url, mNote.title, TNUtils.Hash17(mNote.noteId));
                TNUtilsUi.sendToSMS(this, msg);
                break;
            }
            case R.id.share_url_menu_other: {
                String msg = getString(R.string.shareinfo_publicnote_url, mNote.title, TNUtils.Hash17(mNote.noteId));
                TNUtilsUi.shareContent(this, msg, "轻笔记分享");
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    // Implement OnClickListener
    // -------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.noteview_home: {
                finish();
                break;
            }

            case R.id.noteview_edit:
                editNote();
                break;

            case R.id.noteview_read_close:
                if (mSynthesizerPlayer != null)
                    mSynthesizerPlayer.cancel();
                setReadBarVisible(false);
                break;

            case R.id.noteview_read_play:
                if (mSynthesizerPlayer == null)
                    break;

                if (mSynthesizerPlayer.getState().toString().equals("PLAYING")) {
                    mSynthesizerPlayer.pause();
                    ImageButton playBtn = (ImageButton) findViewById(R.id.noteview_read_play);
                    playBtn.setImageResource(R.drawable.ic_media_play);
                } else if (mSynthesizerPlayer.getState().toString()
                        .equals("PAUSED")) {
                    ImageButton playBtn = (ImageButton) findViewById(R.id.noteview_read_play);
                    playBtn.setImageResource(R.drawable.ic_media_pause);
                    mSynthesizerPlayer.resume();
                    break;
                }
                break;

            case R.id.noteview_more: {
                if (mNote.trash == 1) {
                    TNUtilsDialog.realDeleteNote(this, new TNRunner(this, "dialogCB"), mNote.noteLocalId);
                } else {
                    if (!isFinishing()) {
                        setPopuMenu();
                        mPopuMenu.show(v);
                    }
                }
                break;
            }

        }
    }

    @Override
    public void onPoPuMenuItemClick(int id) {
        switch (id) {
            case R.id.noteview_actionitem_share:// 分享
                openContextMenu(findViewById(R.id.noteview_share_menu));
                break;

            case R.id.noteview_actionitem_tag: {// 标签
                if (mNote.syncState == 1) {
                    Toast.makeText(this,
                            R.string.alert_NoteList_NotCompleted_ChangTag,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                Bundle b = new Bundle();
                b.putString("TagStrForEdit", mNote.tagStr);
                b.putSerializable("ChangeTagForNoteList", mNote.noteLocalId);
                startActivity(TNTagListAct.class, b);
                break;
            }

            case R.id.noteview_actionitem_move: {// 移动
                if (mNote.syncState == 1) {
                    Toast.makeText(this, R.string.alert_NoteList_NotCompleted_Move,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                Bundle b = new Bundle();
                b.putLong("OriginalCatId", mNote.catId);
                b.putInt("Type", 1);
                b.putLong("ChangeFolderForNoteList", mNote.noteLocalId);
                startActivity(TNCatListAct.class, b);
                break;
            }

            case R.id.noteview_actionitem_attribute:// 属性
                Bundle b = new Bundle();
                b.putLong("NoteLocalId", mNoteLocalId);
                startActivity(TNNoteInfoAct.class, b);
                break;

            case R.id.noteview_actionitem_delete: // 删除
                deleteNote();
                break;

            case R.id.noteview_actionitem_selecttext: {// 选择文本
                try {
                    Method m = WebView.class.getMethod("emulateShiftHeld",
                            (Class[]) null);
                    m.invoke(mWebView, (Object[]) null);
                } catch (Exception e) {
                    e.printStackTrace();
                    // fallback
                    KeyEvent shiftPressEvent = new KeyEvent(0, 0,
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
                    shiftPressEvent.dispatch(mWebView);
                }
                break;
            }
        }

    }


    // onDownloadListener
    // -------------------------------------------------------------------------------
    @Override
    public void onStart(TNNoteAtt att) {
        Message msg = new Message();
        Bundle date = new Bundle();
        date.putSerializable("att", att);
        msg.what = 2;
        msg.arg1 = 2;
        msg.setData(date);
        mHandler.sendMessage(msg);
    }


    @Override
    public void onEnd(TNAction aAction) {
        respondSyncNoteAtt(aAction);
    }


    // Action respond methods
    // -------------------------------------------------------------------------------
    public void respondSynchronize(TNAction aAction) {
        if (aAction.result == TNActionResult.Cancelled) {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell,
                    true);
        } else if (!TNHandleError.handleResult(this, aAction, false)) {
            TNSettings.getInstance().originalSyncTime = System
                    .currentTimeMillis();
            TNSettings.getInstance().savePref(false);

            TNUtilsUi.showNotification(this,
                    R.string.alert_MainCats_Synchronized, true);
        } else {
            TNUtilsUi.showNotification(this, R.string.alert_Synchronize_Stoped,
                    true);
        }
    }



    public void respondGetAllDataByNoteId(TNAction aAction) {
        if (aAction.inputs.size() == 1) //消除编辑页的注册响应事件带来的影响
            return;
        if (aAction.result == TNActionResult.Cancelled) {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
        } else if (!TNHandleError.handleResult(this, aAction, false)) {
            TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
        } else {
            TNUtilsUi.showNotification(this,
                    R.string.alert_Synchronize_Stoped, true);
        }
        configView();
    }

    public void respondSyncNoteAtt(TNAction aAction) {
        Log.i(TAG, "respondSyncNoteAtt: " + aAction.type + " isInFront: "
                + isInFront);
        if (isInFront) {
            if (!TNHandleError.handleResult(this, aAction)) {
                mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);
                TNNoteAtt att = (TNNoteAtt) aAction.inputs.get(0);
                try {
                    att.makeThumbnail();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String s;
                if (att.type > 10000 && att.type < 20000)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file://%s\\\" /></a></div>",
                                    att.attLocalId, att.attLocalId, att.path);
                else if (att.type > 20000 && att.type < 30000)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/audio.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40001)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/pdf.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40002)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/txt.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40003 || att.type == 40010)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/word.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40005 || att.type == 40011)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/ppt.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40009 || att.type == 40012)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/excel.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/unknown.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("s", s);
                b.putLong("attLocalId", att.attLocalId);
                msg.what = 4;
                msg.setData(b);
                mHandler.sendMessage(msg);
            } else {
                TNNoteAtt att = (TNNoteAtt) aAction.inputs.get(0);
                String s = "";
                if (TextUtils.isEmpty(att.path) && att.syncState == 1) {
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.downloadAtt(%d)\\\"><img id=\\\"img%d\\\" src=\\\"file:///android_asset/needdownload.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attLocalId, att.attName,
                                    (att.size * 100 / 1024) / 100f + "K");
                } else {
                    s = String
                            .format("<img src=\\\"file:///android_asset/missing.png\\\" />%s<br />%s(%s)",
                                    getString(R.string.alert_NoteView_AttMissing),
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                }

                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("s", s);
                b.putLong("attLocalId", att.attLocalId);
                msg.what = 4;
                msg.setData(b);
                mHandler.sendMessage(msg);
            }
        }
    }

    public void respondNoteHandle(TNAction aAction) {
        mProgressDialog.hide();
        finish();
    }

    //更换标签后更新笔记
    public void respondNoteLocalChangeTag(TNAction aAction) {
        configView();
    }

    // Private methods
    // -------------------------------------------------------------------------------
    public void dialogCB() {
        mProgressDialog.show();
    }

    public void dialogCallBackProgress() {
        mProgressDialog.show();
    }

    private void setPopuMenu() {
        mPopuMenu = new PoPuMenuView(this);
        mPopuMenu.addItem(R.id.noteview_actionitem_tag,
                getString(R.string.noteview_actionitem_tag), mScale);
        if (Integer.valueOf(Build.VERSION.SDK) <= 7) {
            mPopuMenu.addItem(R.id.noteview_actionitem_selecttext,
                    getString(R.string.noteview_actionitem_selecttext), mScale);
        }
        mPopuMenu.addItem(R.id.noteview_actionitem_move,
                getString(R.string.noteview_actionitem_move), mScale);
        mPopuMenu.addItem(R.id.noteview_actionitem_attribute,
                getString(R.string.noteview_actionitem_attribute), mScale);
        mPopuMenu.addItem(R.id.noteview_actionitem_delete,
                getString(R.string.noteview_actionitem_delete), mScale);
        mPopuMenu.addItem(R.id.noteview_actionitem_share,
                getString(R.string.noteview_actionitem_share), mScale);

        mPopuMenu.setOnPoPuMenuItemClickListener(this);
    }

    private void deleteNote() {
        if (mNote.trash == 1) {
            TNUtilsDialog.realDeleteNote(this, new TNRunner(this, "dialogCB"), mNote.noteLocalId);
        } else {
            TNUtilsDialog.deleteNote(this, new TNRunner(this, "dialogCB"), mNote.noteLocalId);
        }
    }

    private void editNote() {
        if (mNote.trash == 1) {
            TNUtilsDialog.restoreNote(this, new TNRunner(this, "dialogCB"), mNote.noteLocalId);
        } else {
            if (mNote.syncState != 1) {
                Bundle b = new Bundle();
                b.putLong("NoteForEdit", mNote.noteLocalId);
                startActivity(TNNoteEditAct.class, b);
            } else {
                TNHandleError.handleErrorCode(this,
                        this.getResources().getString(R.string.alert_NoteView_NotCompleted));
            }
        }
    }

    private void startAutoDownload() {
        Log.d(TAG, "startAutoDownload");
        if (!TNUtils.isNetWork())
            return;

        if (mNote == null)
            return;

        TNDownloadAttService.getInstance().start();
    }

    private void showSynDialog() {
        TNUtilsUi.showToast(R.string.alert_NoteView_ReadHint);
        if (mSynthesizerPlayer == null) {
            mSynthesizerPlayer = SynthesizerPlayer.createSynthesizerPlayer(
                    this, "appid=4ea04eee");
            mPlainText = mNote.getPlainText();

            TNSettings settings = TNSettings.getInstance();
            mSynthesizerPlayer.setVoiceName(settings.voice);
            mSynthesizerPlayer.setSpeed(settings.speed);
            mSynthesizerPlayer.setVolume(settings.volume);
            mSynthesizerPlayer.setBackgroundSound("0");
        }

        if (mStartPos > 0) {
            openContextMenu(findViewById(R.id.noteview_read_menu));
        } else {
            mStartPos = 0;
            mSynthesizerPlayer.playText(getNextReadStr(), null, this);
            setReadBarVisible(true);
        }
    }

    private void setReadBarVisible(boolean visible) {
        if (visible) {
            LinearLayout readLayout = (LinearLayout) findViewById(R.id.noteview_read_layout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY);
            readLayout.setLayoutParams(layoutParams);
            ProgressBar pb = (ProgressBar) findViewById(R.id.noteview_read_progressbar);
            pb.setProgress(0);
            pb.setSecondaryProgress(0);
            ImageButton playBtn = (ImageButton) findViewById(R.id.noteview_read_play);
            playBtn.setImageResource(R.drawable.ic_media_pause);
        } else {
            LinearLayout readLayout = (LinearLayout) findViewById(R.id.noteview_read_layout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0,
                    Gravity.NO_GRAVITY);
            readLayout.setLayoutParams(layoutParams);
        }
    }

    private String getNextReadStr() {
        String result = null;
        if (mStartPos + 150 > mPlainText.length()) {
            result = mPlainText.substring(mStartPos);
            mEndPos = mPlainText.length();
        } else {
            // String subStr = plainText.substring(readedPos + 100, readedPos +
            // 150);
            int index = 150;
            for (int i = 100; i < 150; i++) {
                char posChar = mPlainText.charAt(mStartPos + i);
                if (posChar == '\r' || posChar == '\n' || posChar == '.'
                        || posChar == '!' || posChar == '?' || posChar == '。'
                        || posChar == '!' || posChar == '?' || posChar == '，'
                        || posChar == ',') {
                    // Log.i(TAG, "break: " + (int)posChar + posChar);
                    index = i;
                    break;
                }
            }
            if (index == 150) {
                for (int i = 100; i < 150; i++) {
                    char posChar = mPlainText.charAt(mStartPos + i);
                    if (posChar == '，' || posChar == ',') {
                        // Log.i(TAG, "break: " + (int)posChar + posChar);
                        index = i;
                        break;
                    }
                }
            }
            result = mPlainText.substring(mStartPos, mStartPos + index);
            mEndPos = mStartPos + index;
        }
        return result;
    }

    private void saveAttDialog() {
        if (!TNUtilsAtt.hasExternalStorage()) {
            TNUtilsUi.alert(this, R.string.alert_NoSDCard);
            return;
        }

        DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    TNUtilsAtt.copyFile(mCurAtt.path, Environment
                            .getExternalStorageDirectory().getPath()
                            + "/ThinkerNote/" + mCurAtt.attName);
                    TNUtilsUi.showToast(R.string.alert_NoteView_AttSaved);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        String hint = String.format(
                getString(R.string.alert_NoteView_SaveAttHint), "/ThinkerNote/"
                        + mCurAtt.attName);

        JSONObject jsonData = TNUtils.makeJSON("CONTEXT", this, "TITLE",
                R.string.alert_Title, "MESSAGE", hint, "POS_BTN",
                R.string.alert_Save, "POS_BTN_CLICK", pbtn_Click, "NEG_BTN",
                R.string.alert_Cancel);
        TNUtilsUi.alertDialogBuilder(jsonData).show();

    }

    // Implement SynthesizerPlayerListener
    // -------------------------------------------------------------------------------
    @Override
    public void onEnd(SpeechError error) {
        Log.i(TAG, "onEnd error:" + error);

        if (error == null) {
            mStartPos = mEndPos;
            if (mEndPos < mPlainText.length()) {
                mSynthesizerPlayer.playText(getNextReadStr(), null, this);
            } else {
                mStartPos = 0;
                setReadBarVisible(false);
            }
        } else {
            TNUtilsUi.showToast(error.toString());
            setReadBarVisible(false);
        }
    }


    @Override
    public void onBufferPercent(int percent, int beginPos, int endPos) {
        Log.i(TAG, "onBufferPercent:" + percent + "," + beginPos + "," + endPos);
        ProgressBar pb = (ProgressBar) findViewById(R.id.noteview_read_progressbar);
        pb.setSecondaryProgress(percent);

    }


    @Override
    public void onPlayBegin() {
        Log.i(TAG, "onPlayBegin:" + mSynthesizerPlayer.getState());
    }


    @Override
    public void onPlayPaused() {
        Log.i(TAG, "onPlayPaused:" + mSynthesizerPlayer.getState());
    }


    @Override
    public void onPlayPercent(int percent, int beginPos, int endPos) {
        Log.i(TAG, "onPlayPercent:" + percent + "," + beginPos + "," + endPos);
        ProgressBar pb = (ProgressBar) findViewById(R.id.noteview_read_progressbar);
        pb.setProgress(percent);
    }


    @Override
    public void onPlayResumed() {
        Log.i(TAG, "onPlayResumed:" + mSynthesizerPlayer.getState());

    }


    // Class JSInterface
    // -------------------------------------------------------------------------------
    final class JSInterface {
        /**
         * This is not called on the UI thread. Post a runnable to invoke
         * loadUrl on the UI thread.
         */
        public void downloadAtt(long id) {
            Log.d(TAG, "downloadAtt:" + id);
            TNDownloadAttService.getInstance().start(id);
        }

        public void openAtt(long id) {
            mCurAtt = mNote.getAttDataByLocalId(id);

            // Log.d(TAG, "curAtt.type" + mCurAtt.type
            // + "curAtt.uploadFlag" + mCurAtt.uploadFlag);
            Log.i(TAG, createStatus + " " + TNNoteViewAct.this.isFinishing());
            // 此处会报一个Only the original thread that created a view hierarchy can
            // touch its views
            // 原因未知，故使用handle处理
            if (mCurAtt.syncState != 1) {
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = 1;
                mHandler.sendMessage(msg);
            }
        }

        public void showSource(String html) {
            Log.e("HTML", html);
        }
    }

    public void showInnerHTML() {
        mWebView.loadUrl("javascript:window.demo.showSource('<head>'+"
                + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
    }

    private class TNGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            View toolbar = findViewById(R.id.noteview_toolbar_layout);
            if (e2.getY() - e1.getY() > 10) {
                if (toolbar.getVisibility() != View.VISIBLE)
                    findViewById(R.id.noteview_toolbar_layout).setVisibility(View.VISIBLE);
            } else if (e1.getY() - e2.getY() > 10) {
                if (toolbar.getVisibility() != View.GONE)
                    findViewById(R.id.noteview_toolbar_layout).setVisibility(View.GONE);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }

    //------------------------------p层调用------------------------------
    private void pGetNote(long mNoteLocalId) {

        presener.pGetNote(mNoteLocalId);
    }
    //-----------------------------接口结果回调-------------------------------

    @Override
    public void onGetNoteSuccess(Object obj) {

        mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);
        TNDownloadAttService.getInstance().init(this, mNote);

        startAutoDownload();

        Message msg = new Message();
        msg.what = 3;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onGetNoteFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }
}
