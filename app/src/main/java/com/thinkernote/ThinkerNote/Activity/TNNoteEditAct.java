package com.thinkernote.ThinkerNote.Activity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechConfig.RATE;
import com.iflytek.speech.SpeechError;
import com.iflytek.ui.RecognizerDialog;
import com.iflytek.ui.RecognizerDialogListener;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNRecord;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Other.PoPuMenuView;
import com.thinkernote.ThinkerNote.Other.PoPuMenuView.OnPoPuMenuItemClickListener;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Service.TNLBSService;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TODO
 * 主页--写笔记界面
 */
public class TNNoteEditAct extends TNActBase implements OnClickListener,
		OnFocusChangeListener, TextWatcher, RecognizerDialogListener,
		OnPoPuMenuItemClickListener {

	private static final int MAX_CONTENT_LEN = 4 * 100 * 1024;
	
	private TNNote mNote = null;
	private Uri mCameraUri = null;
	private boolean mIsStartOtherAct = false;
	private RecognizerDialog mIatDialog;
	private int mSelection = -1;
	private EditText mTitleView;
	private EditText mContentView;
	private PoPuMenuView mPopuMenu;
	private LinearLayout mAttsLayout;
	private ProgressDialog mProgressDialog = null;
	private TNNoteAtt mCurrentAtt;
	private float mScale;

	private TNRecord mRecord;
	private TextView mRecordTime;
	private ProgressBar mRecordAmplitudeProgress;

	private Timer mTimer;
	private TimerTask mTimerTask;


	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {//自动保存
				saveInput();
				if (mNote.isModified() && checkNote()) {
					mNote.prepareToSave();
					TNAction action = TNAction.runAction(TNActionType.NoteSave, mNote);
					if (action.result == TNActionResult.Finished) {
						TNUtilsUi.showShortToast(R.string.alert_NoteSave_SaveOK);
						mNote = (TNNote) action.outputs.get(0);
						getIntent().putExtra("NoteForEdit", mNote.noteLocalId);
						initNote();
						if (!mTitleView.hasFocus())
							mTitleView.setText(mNote.title);
					}
				}
				mHandler.removeMessages(1);
			} else if (msg.what == 3) {// 音量振幅
				mRecordAmplitudeProgress.setProgress(msg.arg1);
			} else if (msg.what == 4) {// 计时
				mRecordTime.setText(formatTime(msg.arg1, msg.arg2));
			} else if(msg.what == 5){//达到了设置的录音长度限制(20M)
				getCursorLocation();
				saveInput();
				addAtt(mRecord.getRecordTmpPath(), true);
				configView();
				showToolbar("note");
				mRecord = null;
				mRecordTime.setText(formatTime(0, 0));
				setCursorLocation();
				TNUtilsUi.alert(TNNoteEditAct.this, R.string.alert_NoteEdit_Record_Interrupt);
			} else if (msg.what == 6) {//空间不够
				TNUtilsUi.alert(TNNoteEditAct.this, R.string.alert_NoSDCard);
				MLog.e(TAG, "record faild: no space");
				showToolbar("note");
			} else if(msg.what == 7){//异步停止录音
				handleProgressDialog("hide");
				getCursorLocation();
				saveInput();
				addAtt(mRecord.getRecordTmpPath(), true);
				configView();
				showToolbar("note");
				mRecord = null;
				mRecordTime.setText(formatTime(0, 0));
				setCursorLocation();
			} else if(msg.what == 8){//保存笔记，异步停止录音
				handleProgressDialog("hide");
				addAtt(mRecord.getRecordTmpPath(), true);
				mRecord = null;
				saveNote();
			} else if (msg.what == 9){//录音出错
				if(mRecord.getRecordTmpPath() == null){
					showToolbar("note");
					mRecord = null;
					mRecordTime.setText(formatTime(0, 0));
					TNUtilsUi.showShortToast(R.string.alert_NoteEdit_Record_Error);
					return;
				}
				getCursorLocation();
				saveInput();
				addAtt(mRecord.getRecordTmpPath(), true);
				configView();
				showToolbar("note");
				mRecord = null;
				mRecordTime.setText(formatTime(0, 0));
				setCursorLocation();
				TNUtilsUi.showShortToast(R.string.alert_NoteEdit_Record_Error);
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_edit);
		initAct();
		
		if (savedInstanceState == null)
			TNLBSService.getInstance().startLocation();

		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		RunningTaskInfo task = am.getRunningTasks(1).get(0);
		String baseName = task.baseActivity.getClassName();
		MLog.e(TAG, "baseActivity = " + baseName);

		if (savedInstanceState == null) {
			initNote();
		} else {
			Serializable obj = (TNNote) savedInstanceState
					.getSerializable("NOTE");
			Uri uri = savedInstanceState.getParcelable("CAMERA_URI");
			boolean tag = savedInstanceState.getBoolean("IS_OTHER_ACT");
			if (obj != null) {
				mNote = (TNNote) obj;
				mIsStartOtherAct = tag;
			}
			if (uri != null)
				mCameraUri = uri;

			MLog.i(TAG, "onCreate" + mNote);
		}
		startTimer();

		mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
		showToolbar("note");
	}

	private void initAct(){
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScale = metric.scaledDensity;
		
		mContentView = (EditText) findViewById(R.id.noteedit_input_content);
		mTitleView = (EditText) findViewById(R.id.noteedit_input_title);
		mAttsLayout = (LinearLayout) findViewById(R.id.noteedit_atts_linearlayout);
		mRecordTime = (TextView) findViewById(R.id.record_time);
		mRecordAmplitudeProgress = (ProgressBar) findViewById(R.id.record_progressbar);
		mContentView.requestFocus();

		findViewById(R.id.noteedit_save).setOnClickListener(this);
		findViewById(R.id.noteedit_camera).setOnClickListener(this);
		findViewById(R.id.noteedit_doodle).setOnClickListener(this);
		findViewById(R.id.noteedit_other).setOnClickListener(this);
		findViewById(R.id.noteedit_record).setOnClickListener(this);
		findViewById(R.id.noteedit_speakinput).setOnClickListener(this);
		findViewById(R.id.record_start).setOnClickListener(this);
		findViewById(R.id.record_stop).setOnClickListener(this);

		mTitleView.setOnFocusChangeListener(this);
		mContentView.addTextChangedListener(this);

	}
	
	private void initNote() {
		if (getIntent().hasExtra("NoteForEdit")) {
			long id = getIntent().getLongExtra("NoteForEdit", -1);
			// edit note
			if (id < 0) {// new note
				mNote = (TNNote) getIntent().getSerializableExtra("NOTE");
			} else
				// edit note
				mNote = TNDbUtils.getNoteByNoteLocalId(id);
		} else {
			mNote = TNNote.newNote();

			Intent it = getIntent();
			if (it != null && it.getAction() != null) {
				Bundle extras = it.getExtras();
				if (extras.containsKey(Intent.EXTRA_STREAM)) {
					MLog.i(TAG, "uri++=" + extras.get(Intent.EXTRA_STREAM));
					Object extraStream = extras.get(Intent.EXTRA_STREAM);
					if (Uri.class.isInstance(extraStream)) {
						Uri uri = (Uri) extraStream;
						String path = getPath(uri);
						if (path != null) {
							File file = new File(getPath(uri));
							mNote.atts.add(TNNoteAtt.newAtt(file, this));
						}
					} else if (ArrayList.class.isInstance(extraStream)) {
						@SuppressWarnings("unchecked")
						ArrayList<Uri> uris = (ArrayList<Uri>) extraStream;
						for (Uri uri : uris) {
							File file = new File(getPath(uri));
							mNote.atts.add(TNNoteAtt.newAtt(file, this));
						}
					}
				}
				if (extras.containsKey(Intent.EXTRA_SUBJECT)) {
					Object subject = extras.get(Intent.EXTRA_SUBJECT);
					MLog.i(TAG, "subject++=" + subject);
					if (subject == null) {
						mNote.title = "";
					} else
						mNote.title = subject.toString();
				}
				if (extras.containsKey(Intent.EXTRA_TEXT)) {
					Object text = extras.get(Intent.EXTRA_TEXT);
					MLog.i(TAG, "text++=" + text);
					if (text == null)
						mNote.content = "";
					else
						mNote.content = text.toString();
				}
			}

		}
		if (mNote == null) {
			finish();
			return;
		}
		
		if (mNote.originalNote == null && mNote.noteLocalId > 0) {
			TNNote newnote = TNNote.newNote();
			newnote.originalNote = mNote;
			newnote.noteLocalId = mNote.noteLocalId;
			newnote.noteId = mNote.noteId;
			newnote.content = mNote.content;
			newnote.title = mNote.title;
			newnote.tagStr = mNote.tagStr;
			newnote.catId = mNote.catId;
			newnote.atts.addAll(mNote.atts);
			newnote.createTime = mNote.createTime;
			if (mNote.isEditable()) {
				MLog.i(TAG, mNote.content);
				newnote.content = mNote.getPlainText();
			} else {
				newnote.setMappingAndPlainText();
			}
			mNote = newnote;
		}
	}

	@Override
	protected void configView() {
		if (createStatus == 0) {
			startTargetAct(getIntent().getStringExtra("Target"));
		}

		refreshAttsView();
		initContentView();
		mTitleView.setText(mTitleView.getHint().equals(mNote.title) ? ""
				: mNote.title);
		mContentView.setText(mNote.content);
		mContentView.setSelection(mNote.content.length());
	}

	private void startTargetAct(String target) {
		if (target == null) {
			return;
		}
		if (target.equals("camera")) {
			startCamera();
		} else if(target.equals("doodle")){
			startActForResult(TNTuYaAct.class, null, R.id.noteedit_doodle);// TODO
		} else if(target.equals("record")){
			getIntent().removeExtra("Target");
			startRecord();
		}
	}

	@Override
	public void onDestroy() {
		try {
			mTimer.cancel();
			TNLBSService.getInstance().stopLocation();
		} catch (Exception e) {
		}
		handleProgressDialog("dismiss");

		if (mIatDialog != null)
			mIatDialog.dismiss();

		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outBundle) {
		saveInput();
		outBundle.putSerializable("NOTE", mNote);
		outBundle.putParcelable("CAMERA_URI", mCameraUri);
		outBundle.putBoolean("IS_OTHER_ACT", mIsStartOtherAct);

		MLog.i(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outBundle);
	}

	@Override
	public void onRestoreInstanceState(Bundle outBundle) {
		super.onRestoreInstanceState(outBundle);
		MLog.i(TAG, "onRestoreInstanceState");

		mNote = (TNNote) outBundle.getSerializable("NOTE");
		mCameraUri = outBundle.getParcelable("CAMERA_URI");
		mIsStartOtherAct = outBundle.getBoolean("IS_OTHER_ACT");

		MLog.i(TAG, "onRestoreInstanceState" + mNote);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setCursorLocation();
		((ScrollView)findViewById(R.id.noteedit_scrollview)).scrollTo(0, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getCursorLocation();
	}

	private void setCursorLocation() {
		if (mTitleView.hasFocus()) {
			setSelectTion(mTitleView);
		} else if (mContentView.hasFocus()) {
			setSelectTion(mContentView);
		} else {
			if (mTitleView.getText().length() == 0) {
				mTitleView.requestFocus();
				mSelection = 0;
				setSelectTion(mTitleView);
			} else {
				mContentView.requestFocus();
				mSelection = mContentView.getText().length();
				setSelectTion(mContentView);
			}
		}
	}

	private void getCursorLocation() {
		if (mTitleView.hasFocus()) {
			mSelection = mTitleView.getSelectionStart();
		} else if (mContentView.hasFocus()) {
			mSelection = mContentView.getSelectionStart();
		}
	}

	private void setSelectTion(EditText editText) {
		try {
			if(mSelection < 0){
				mSelection = editText.getText().length();
			}
			editText.setSelection(mSelection);
		} catch (Exception e) {
			mSelection = editText.getText().length();
			editText.setSelection(mSelection);

		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.noteedit_save: {//保存
			TNUtilsUi.hideKeyboard(this, R.id.noteedit_save);
			saveInput();
			if(mRecord != null && !mRecord.isStop()){
				handleProgressDialog("show");
				mRecord.asynStop(8);
				break;
			}
			saveNote();
			break;
		}

		case R.id.noteedit_other://其他功能
			setOtherBtnPopuMenu();
			mPopuMenu.show(v);
			break;
		case R.id.noteedit_record: {//录音
			startRecord();
			break;
		}
		case R.id.noteedit_doodle: {//涂鸦
			startActForResult(TNTuYaAct.class, null, R.id.noteedit_doodle);// TODO
			break;
		}
		case R.id.noteedit_camera://相机
			startCamera();
			break;
		case R.id.noteedit_speakinput://语音
			showIatDialog();
			break;

		case R.id.record_start://录音的开始/暂停
			if (mRecord.isRecording()) {
				mRecord.pause();
				((Button) v).setText(R.string.noteedit_record_start);
			} else {
				mRecord.start();
				((Button) v).setText(R.string.noteedit_record_pause);
			}
			break;

		case R.id.record_stop://录音的结束
//			stopRecord();
			handleProgressDialog("show");
			mRecord.asynStop(7);
			break;
		}
	}

	private void startCamera() {
		try {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			ContentValues values = new ContentValues();
			values.put(Media.TITLE, "image");
			mCameraUri = getContentResolver().insert(
					Media.EXTERNAL_CONTENT_URI, values);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
			TNUtilsDialog.startIntentForResult(this, intent,
					R.string.alert_NoteEdit_NoCamera, R.id.noteedit_camera);
		} catch (IllegalArgumentException e) {
			// 目前仅在1.6，HTC Magic发生过
			// getContentResolver().insert可能产生异常
			// java.lang.IllegalArgumentException: Unknown URL
			// content://media/external/images/media
		} catch (Exception e) {
		}
	}
	
	private void startRecord(){
		if (mRecord == null)
			mRecord = new TNRecord(mHandler);
		
		showToolbar("record");
		mRecord.start();
		((Button) findViewById(R.id.record_start)).setText(R.string.noteedit_record_pause);
	}
	
	private void showToolbar(String type) {
		if (type.equals("record")) {
			findViewById(R.id.noteedit_toolbar_layout).setVisibility(View.GONE);
			findViewById(R.id.noteedit_record_layout).setVisibility(
					View.VISIBLE);
		} else if (type.equals("play")) {

		} else {
			findViewById(R.id.noteedit_toolbar_layout).setVisibility(
					View.VISIBLE);
			findViewById(R.id.noteedit_record_layout).setVisibility(View.GONE);
		}
	}

	@Override
	public void onPoPuMenuItemClick(int id) {
		switch (id) {
		case R.id.noteedit_picture: {
			String action;
			action = Intent.ACTION_PICK;
			Intent intent = new Intent(action);
	        intent.setType("image/*");  
	        intent.putExtra("return-data", true);  
			TNUtilsDialog.startIntentForResult(this, intent,
					R.string.alert_NoteEdit_NoImage, R.id.noteedit_picture);
			break;
		}
		case R.id.noteedit_tag: {
			saveInput();
			Bundle b = new Bundle();
			b.putString("TagStrForEdit", mNote.tagStr);
			startActForResult(TNTagListAct.class, b, R.id.noteedit_tag);// TODO
			break;
		}

		case R.id.noteedit_addatt://添加附件
			saveInput();
			startActForResult(TNFileListAct.class, null, R.id.noteedit_addatt);// TODO
			break;

		case R.id.noteedit_insertcurrenttime: {
			if (mTitleView.isFocused()) {
				if (mTitleView.getText().toString().length() > 75) {
					TNUtilsUi.showToast("标题太长了，无法继续插入");
					break;
				}
				insertCurrentTime(mTitleView);
			} else if (mContentView.isFocused()) {
				insertCurrentTime(mContentView);
			} else {
				mContentView.requestFocus();
				insertCurrentTime(mContentView);
			}
			break;
		}

		case R.id.noteedit_folders: {//请选择文件夹 按钮
			saveInput();
			Bundle b = new Bundle();
			b.putLong("OriginalCatId", mNote.catId);
			b.putInt("Type", 2);
			startActForResult(TNCatListAct.class, b, R.id.noteedit_folders);// TODO
			break;
		}
		
		case R.id.noteedit_att_look:
			if(mCurrentAtt != null){
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(mCurrentAtt.path)), 
						TNUtilsAtt.getMimeType(mCurrentAtt.type, mCurrentAtt.attName));
				TNUtilsDialog.startIntent(this, intent, 
						R.string.alert_NoteView_CantOpenAttMsg);
			}
			break;
			
		case R.id.noteedit_att_delete:
			mNote.atts.remove(mCurrentAtt);
			String temp = String.format("<tn-media hash=\"%s\"></tn-media>", mCurrentAtt.digest);
			mNote.content = mNote.content.replaceAll(temp, "");
			mCurrentAtt = null;
			configView();
			break;
			
		}
	}

	private void insertCurrentTime(EditText et) {
		int index = et.getSelectionStart();
		String date = "【"
				+ TNUtilsUi.formatHighPrecisionDate(this,
						System.currentTimeMillis()) + "】";
		StringBuffer sb = new StringBuffer(et.getText().toString());
		sb.insert(index, date);
		et.setText(sb.toString());
		Selection.setSelection(et.getText(), index + date.length());
	}

	private void setOtherBtnPopuMenu() {
		if(mPopuMenu != null){
			mPopuMenu.dismiss();
		}
		mPopuMenu = new PoPuMenuView(this);
		mPopuMenu.addItem(R.id.noteedit_picture,
				getString(R.string.noteedit_popomenu_picture), mScale);
		if(!TNSettings.getInstance().isInProject()){
			mPopuMenu.addItem(R.id.noteedit_tag,
					getString(R.string.noteedit_popomenu_tag), mScale);
		}
		mPopuMenu.addItem(R.id.noteedit_addatt,
				getString(R.string.noteedit_popomenu_addatt), mScale);
		mPopuMenu.addItem(R.id.noteedit_insertcurrenttime,
				getString(R.string.noteedit_popomenu_insertcurrenttime), mScale);
		mPopuMenu.addItem(R.id.noteedit_folders,
				getString(R.string.noteedit_popomenu_folders), mScale);
		mPopuMenu.setOnPoPuMenuItemClickListener(this);
	}
	
	private void setAttBtnPopuMenu() {
		if(mPopuMenu != null){
			mPopuMenu.dismiss();
		}
		mPopuMenu = new PoPuMenuView(this);
		mPopuMenu.addItem(R.id.noteedit_att_look,
				getString(R.string.noteedit_popomenu_lookatt), mScale);
		mPopuMenu.addItem(R.id.noteedit_att_delete,
				getString(R.string.noteedit_popomenu_deleteatt), mScale);
		mPopuMenu.setOnPoPuMenuItemClickListener(this);
	}

	private void initContentView() {
		int attViewHeight = 0;
		if(mNote.atts != null && mNote.atts.size() > 0){
			attViewHeight = 85 + 38;
		}
		mContentView.setMinLines((getWindowManager().getDefaultDisplay()
				.getHeight() - TNUtils.dipToPx(this, 90) - attViewHeight) / mContentView.getLineHeight());
	}

	private void saveInput() {
		String title = mTitleView.getText().toString().trim();
		if (!title.equals(mNote.title)) {
			mNote.title = title;
		}

		String content = mContentView.getText().toString();
		if (!content.equals(mNote.content)) {
			mNote.content = content;
		}
		
		if (mNote.title.length() == 0) {
			mNote.title = this.getString(R.string.noteedit_title);
		}
		
	}

	private void refreshAttsView() {
		mAttsLayout.removeAllViews();
		boolean needRefresh = false;
		for (TNNoteAtt att : mNote.atts) {
			//此判断是为了解决特殊用户在查看附件时把附件删除引起的问题
			if (new File(att.path).length() <= 0) {
				mNote.atts.remove(att);
				String temp = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
				mNote.content = mNote.content.replaceAll(temp, "");
				needRefresh = true;
				break;
			}
			ImageView attView = new ImageView(this);
			setAttView(attView, att);
			mAttsLayout.addView(attView);
		}
		if (needRefresh) {
			configView();
		}
		mAttsLayout.setGravity(Gravity.CENTER);
	}

	private void setAttView(ImageView attView, final TNNoteAtt att) {
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				100, LayoutParams.WRAP_CONTENT);
		
		if (att.type > 10000 && att.type < 20000) {
			Bitmap thumbnail = TNUtilsAtt.makeThumbnailBitmap(att.path,
					100, 73);
			if(thumbnail != null){
				attView.setImageBitmap(thumbnail);
			}else{
				attView.setImageURI(Uri.parse(att.path));
			}
			layoutParams.setMargins((int)(2 * mScale), (int)(4 * mScale), (int)(2 * mScale), (int)(4 * mScale));
		} else if (att.type > 20000 && att.type < 30000)
			TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_audio);
		else if (att.type == 40001)
			TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_pdf);
		else if (att.type == 40002)
			TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_txt);
		else if (att.type == 40003 || att.type == 40010)
			TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_word);
		else if (att.type == 40005 || att.type == 40011)
			TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_ppt);
		else if (att.type == 40009 || att.type == 40012)
			TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_excel);
		else
			TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_unknown);
		
		attView.setLayoutParams(layoutParams);
		
		attView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setAttBtnPopuMenu();
				mPopuMenu.show(v);
				mCurrentAtt = att;
			}
		});
	}

	private String formatTime(int minute, int secend) {
		String time = "";
		if (minute < 10) {
			time += "0";
		}
		time += String.valueOf(minute);
		time += ":";
		if (secend < 10) {
			time += "0";
		}
		time += String.valueOf(secend);
		return time;
	}

	public void toFinish() {
		finish();
	}

	private void showIatDialog() {
		if (TNUtilsDialog.checkNetwork(this)) {
			if (mIatDialog == null) {
				mIatDialog = new RecognizerDialog(this, "appid=4ea04eee");
				mIatDialog.setEngine("sms", null, null);
				mIatDialog.setSampleRate(RATE.rate16k);
				mIatDialog.setListener(this);
			}
			mIatDialog.show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		MLog.i(TAG,
				"onFocusChange" + v + hasFocus
						+ ((EditText) v).getSelectionStart());
		if (v.getId() == R.id.noteedit_input_title && !hasFocus) {
			String title = ((EditText) v).getText().toString();
			String trimTitle = title.trim();
			if (!title.equals(trimTitle)) {
				if (((EditText) v).getSelectionStart() > trimTitle.length()) {
					((EditText) v).setSelection(trimTitle.length());
				}
				((EditText) v).setText(trimTitle);
			}
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		String content = mContentView.getText().toString();
		((TextView) findViewById(R.id.noteedit_wordcount)).setText(String
				.format(getString(R.string.noteedit_wordcount),
						content.length()));

		if (mNote.noteLocalId > 0 && mNote.originalNote != null
				&& !mNote.originalNote.isEditable()) {
			TNUtilsHtml.WhileTextViewChangeText(mNote, content);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		MLog.e(TAG, "str:" + s + " start:" + start + " count:" + count
				+ " after:" + after);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		MLog.e(TAG, "str:" + s + " start:" + start + " before:" + before
				+ " count:" + count);
	}

	@Override
	public void onEnd(SpeechError error) {
		MLog.i(TAG, "iat onEnd:" + error);
		if (error != null) {
			TNUtilsUi.showToast(error.toString());
		}
	}

	@Override
	public void onResults(ArrayList<RecognizerResult> results, boolean islast) {
		StringBuilder builder = new StringBuilder();
		for (RecognizerResult recognizerResult : results) {
			builder.append(recognizerResult.text);
		}
		MLog.i(TAG, "iat:" + builder.toString());

		EditText currentText = null;
		if (mTitleView.isFocused()) {
			currentText = mTitleView;
		} else if (mContentView.isFocused()) {
			currentText = mContentView;
		}

		if (currentText != null) {
			int start = currentText.getSelectionStart();
			int end = currentText.getSelectionEnd();
			currentText.getText().replace(Math.min(start, end),
					Math.max(start, end), builder);
			currentText.setSelection(Math.min(start, end) + builder.length(),
					Math.min(start, end) + builder.length());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		MLog.w(TAG, "onActivityResult:" + requestCode + "," + resultCode + ","
				+ data);

		if (resultCode != RESULT_OK
				|| (data == null && requestCode != R.id.noteedit_camera)) {
			if (mIsStartOtherAct) {
				toFinish();
			}
			if(getIntent().hasExtra("Target")){
				toFinish();
			}
			return;
		}
		getIntent().removeExtra("Target");

		mIsStartOtherAct = false;
		if (requestCode == R.id.noteedit_camera) {
			addAtt(getPath(mCameraUri), false);
		} else if (requestCode == R.id.noteedit_picture) {
			addAtt(getPath(data.getData()), false);
		} else if (requestCode == R.id.noteedit_record) {
			addAtt(getPath(data.getData()), false);
		} else if (requestCode == R.id.noteedit_folders) {
			long catId = data.getLongExtra("SelectedCatId", 0);
			mNote.catId = catId;
		} else if (requestCode == R.id.noteedit_addatt) {
			addAtt(data.getStringExtra("SelectedFile"), false);
		} else if (requestCode == R.id.noteedit_tag) {
			mNote.tagStr = data.getStringExtra("EditedTagStr");
		} else if (requestCode == R.id.noteedit_doodle) {
			addAtt(data.getStringExtra("TuYa"), false);
		}
		MLog.d(TAG, "onActivityResult end");
	}

	private void addAtt(final String path, boolean delete) {
		if (path == null) {
			MLog.e(TAG, "addAtt path is NULL");
			return;
		}
		MLog.i(TAG, "srcpath: " + path);

		if (mNote.atts.size() > 200) {
			TNUtilsUi.alert(this, R.string.alert_Att_too_Much);
			return;
		}

		File file = new File(path);
		if (file.getName().indexOf(" ") > -1) {
			TNUtilsUi.alert(this, R.string.alert_Att_Name_FormatWrong);
			return;
		}
		if (file.length() <= 0) {
			TNUtilsUi.alert(this, R.string.alert_NoteEdit_AttSizeWrong);
		} else if (file.length() > TNConst.ATT_MAX_LENTH) {
			TNUtilsUi.alert(this, R.string.alert_NoteEdit_AttTooLong);
		} else {
			mNote.atts.add(TNNoteAtt.newAtt(file, this));
			if (delete)
				file.delete();
		}
	}

	private void back() {
		saveInput();
		if (!mNote.isModified() && (mRecord == null || mRecord.isStop())) {
			toFinish();
			return;
		}
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveNote();
			}
		};
		DialogInterface.OnClickListener nbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mRecord != null && !mRecord.isStop()){
					mRecord.cancle();
				}
				toFinish();
			}
		};
		JSONObject jsonData = TNUtils.makeJSON("CONTEXT", this, "TITLE",
				R.string.alert_Title, "MESSAGE",
				R.string.alert_NoteEdit_SaveMsg, "POS_BTN",
				R.string.alert_Save, "POS_BTN_CLICK", pbtn_Click, "NEU_BTN",
				R.string.alert_NoSave, "NEU_BTN_CLICK", nbtn_Click, "NEG_BTN",
				R.string.alert_Cancel);
		TNUtilsUi.alertDialogBuilder(jsonData).show();
	}
	
	private void saveNote(){
		if (checkNote()) {
			handleProgressDialog("show");
			mNote.prepareToSave();
			TNAction action = TNAction.runAction(TNActionType.NoteSave, mNote);
			handleProgressDialog("hide");
			TNUtilsUi.showShortToast(R.string.alert_NoteSave_SaveOK);
			
			if (action.result == TNActionResult.Finished) {
				mNote = (TNNote) action.outputs.get(0);
				if (TNUtils.isNetWork()) {
					if (TNActionUtils.isSynchronizing()) {
						finish();
						return;
					}
					TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
					TNAction.runActionAsync(TNActionType.SynchronizeEdit);
				}
				finish();
			}
		}
	}

	private boolean checkNote() {
		int length = mNote.content.length();
		if (length > MAX_CONTENT_LEN) {
			TNUtilsUi.alert(this, R.string.alert_NoteEdit_ContentTooLong);
			return false;
		} else {
			return true;
		}
	}

	private String getPath(Uri uri) {
		try {
			String[] projection = { MediaStore.Images.Media.DATA };

			Cursor cursor = managedQuery(uri, projection, null, null, null);
			if (cursor != null) {
				// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
				// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE
				// MEDIA
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				return cursor.getString(column_index);
			} else {
				return uri.getPath();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void startTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
			mTimerTask = null;
		}
		mTimer = new Timer();
		mTimerTask = new TimerTask() {
			public void run() {
				Message message = new Message();
				message.what = 1;
				mHandler.sendMessage(message);
				MLog.i(TAG, "sendMessage savenote");
			}
		};
		mTimer.schedule(mTimerTask, 60 * 1000, 60 * 1000);
	}

	private void handleProgressDialog(String type){
		try {
			if(type.equals("show")){
				if(mProgressDialog == null){
					mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
				}
				mProgressDialog.show();
			}else if(type.equals("hide")){
				if(mProgressDialog != null){
					mProgressDialog.hide();
				}
			}else if(type.equals("dismiss")){
				if(mProgressDialog != null){
					mProgressDialog.dismiss();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
