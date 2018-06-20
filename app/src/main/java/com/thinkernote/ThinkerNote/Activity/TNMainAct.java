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
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
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

import org.json.JSONObject;

/**
 * 主界面
 */
public class TNMainAct extends TNActBase implements OnClickListener,OnMainListener{
	private long mLastClickBackTime = 0;
	private String mDownLoadAPKPath = "";
	private TextView mTimeView;
	TNSettings mSettings = TNSettings.getInstance();

	//
	IMainPresener presener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TNActivityManager.getInstance().finishOtherActivity(this);

		TNAction.regResponder(TNActionType.Synchronize, this, "respondSynchronize");
		TNAction.regResponder(TNActionType.SynchronizeEdit, this, "respondSynchronizeEdit");
		TNAction.regResponder(TNActionType.Upgrade, this, "respondUpgrade");
		TNAction.regResponder(TNActionType.UpdateSoftware, this, "respondUpdateSoftware");

		//
		presener = new MainPresenterImpl(this, this);
		setViews();
		
		//start help activity
		if (mSettings.firstLaunch) {
			startActivity(TNHelpAct.class);
		} 
		
		if (savedInstanceState == null) {
			if(TNUtils.isNetWork()){
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

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	protected void setViews(){
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
		if ( (createStatus == 0 && TNUtils.isAutoSync()) || mSettings.firstLaunch) {
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
		if(b != null){
			i = (Intent)b.get(Intent.EXTRA_INTENT);
		}
		if(i!= null && i.hasExtra("Type") && createStatus == 0){
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
		case R.id.main_newnote:{//综合笔记
			startActivity(TNNoteEditAct.class);
			break;}
		case R.id.main_allnote: {//我的笔记
			startActivity(TNPagerAct.class);
			break;}
		case R.id.main_cameranote: {//拍照笔记
			Bundle b = new Bundle();
			b.putString("Target", "camera");
			startActivity(TNNoteEditAct.class,b);
			break;}
		
		case R.id.main_doodlenote: {//涂鸦笔记
			Bundle b = new Bundle();
			b.putString("Target", "doodle");
			startActivity(TNNoteEditAct.class,b);;
			break;}
		case R.id.main_recordnote: {//录音笔记
			Bundle b = new Bundle();
			b.putString("Target", "record");
			startActivity(TNNoteEditAct.class,b);
			break;}
		case R.id.main_project:
			
			break;
			
		case R.id.main_exchange:{//设置
			startActivity(TNUserInfoAct.class);
			//debug:
			break;}
		case R.id.main_sync_btn:{//同步按钮
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
			break;}
		case R.id.main_serch: {//搜索
			Bundle b = new Bundle();
			b.putInt("SearchType", 1);
			startActivity(TNSearchAct.class,b);
			break;}

		case R.id.main_bootview:{//引导 说明
			findViewById(R.id.main_bootview).setVisibility(View.GONE);
			break;}
		}
	}
	
	public void confirmDialog() {
		startSyncAnimation();
	}
	
	public void cancelDialog() {
		findViewById(R.id.main_sync_btn).clearAnimation();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			View v = findViewById(R.id.main_bootview);
			if(v.getVisibility() == View.VISIBLE){
				v.setVisibility(View.GONE);
				return true;
			}
			long currTime = System.currentTimeMillis();
			if(currTime - mLastClickBackTime > 5000){
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
		
		if ( !TNActionUtils.isSynchronizing(aAction))
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

	private void startSyncAnimation() {
		RotateAnimation rAnimation = new RotateAnimation(0.0f, 360.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rAnimation.setDuration(3000);
		rAnimation.setRepeatCount(99999);
		rAnimation.setInterpolator(new LinearInterpolator());
		findViewById(R.id.main_sync_btn).startAnimation(rAnimation);
	}
	



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

			Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
			theButton.setText(getString(R.string.update_downloading));
			theButton.setEnabled(false);

			Button negButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			negButton.setEnabled(false);

			//下载包 异步
			downloadNewAPK(mDownLoadAPKPath,dialog);

		}
	}

	//-------------------------------------p层调用------------------------------------------
	private void findUpgrade() {
		presener.pUpgrade("HOME");

		// TODO 需要后台支持
//		TNAction.runActionAsync(TNActionType.Upgrade, "HOME");
	}

	// TODO
	public void respondUpgrade(TNAction aAction) {
		if (aAction.result == TNActionResult.Finished && "HOME".equals(aAction.inputs.get(0))) {
			JSONObject respond = (JSONObject) aAction.outputs.get(0);
			try {
				PackageInfo info = getPackageManager().getPackageInfo(
						getPackageName(), 0);
				MLog.d(TAG, info.versionCode + "," + info.versionName);
				String newVersionName = (String) TNUtils.getFromJSON(respond, "version");
				String newVersionCode = TNUtils.getFromJSON(respond, "versionCode") != null ? respond.getString("versionCode"): "-1";
//				这里需要加判断更新的字段,判断是否需要更新且只更新一次
				if (mSettings.version.equals(newVersionName)) {
					return;
				}
				mSettings.version = newVersionName;
				mSettings.savePref(false);

				int newSize = respond.getInt("size");
				String description = respond.getString("content");
				mDownLoadAPKPath = respond.getString("url");
				MLog.d(TAG, newVersionName + ","
						+ newSize);
				if (Integer.valueOf(newVersionCode) > info.versionCode) {
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
					AlertDialog dialog = TNUtilsUi.alertDialogBuilder(jsonData);
					dialog.show();

					Button theButton = dialog
							.getButton(DialogInterface.BUTTON_POSITIVE);
					theButton.setOnClickListener(new CustomListener(dialog));
				} else {
					TNUtilsUi.showToast("当前版本已是最新");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	private void synchronizeData() {
//		presener.pSynchronizeData();
		//TODO 大问题 待做
		TNAction.runActionAsync(TNActionType.Synchronize, "home");
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
			if(filePath != null)
				//打开文件
				TNUtilsUi.openFile(filePath);
		}
	}

	private void downloadNewAPK(String url,Dialog dialog) {
		//TODO
		TNAction.runActionAsync(TNActionType.UpdateSoftware, url, dialog);

	}


	//-------------------------------------接口返回------------------------------------------

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
		MLog.d( newVersionName ,newSize);
		String newVersionCode = null;
		if (bean.getVersionCode()==null){
			newVersionCode = "-1";
		}else{
			newVersionCode = bean.getVersionCode();
		}
		//这里需要加判断更新的字段,判断是否需要更新且只更新一次
		if (mSettings.version.equals(newVersionName)) {
			return;
		}
		mSettings.version = newVersionName;
		mSettings.savePref(false);

		//
		if (Integer.valueOf(newVersionCode) > info.versionCode) {
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

	@Override
	public void onSynchronizeSuccess(Object obj) {

	}

	@Override
	public void onSynchronizeFailed(String msg, Exception e) {

	}
}
