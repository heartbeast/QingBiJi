package com.thinkernote.ThinkerNote.Activity;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNUser;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * 启动页/欢迎页
 */
public class TNSplashAct extends TNActBase {

	// Class members
	//-------------------------------------------------------------------------------
	private boolean isRunning = false;
	private Bundle extraBundle = null;
	
	// Activity methods
	//-------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//解决首次安装按home键置入后台，从桌面图标点击重新启动的问题
		if (!isTaskRoot()) {
			Intent intent = getIntent();
			String action = intent.getAction();
			if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
				finish();
				return;
			}
		}
		setContentView(R.layout.splash);
		setViews();
		
		TNAction.regResponder(TNActionType.Login, this, "respondLogin");
		
//		if(!TNSettings.getInstance().serviceRuning){
//			Intent serviceIntent = new Intent(TNSplashAct.this, TNPushService.class);
//			startService(serviceIntent);
//		}
		
		if( getIntent().hasExtra(Intent.EXTRA_INTENT)){
			extraBundle = new Bundle();
			extraBundle.putParcelable(Intent.EXTRA_INTENT, 
					(Intent)getIntent().getExtras().get(Intent.EXTRA_INTENT));
		}
	}
	
	// configView
	//-------------------------------------------------------------------------------
	protected void configView(){
		if(TNSettings.getInstance().hasDbError){
			DialogInterface.OnClickListener pbtn_Click =
					new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						TNAction.runAction(TNActionType.DBReset);
						startRun();
						TNSettings.getInstance().hasDbError = false;
						TNSettings.getInstance().savePref(false);
					}
				};

			JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", this,
				"TITLE", R.string.alert_Title,
				"MESSAGE", R.string.alert_DBErrorHint,
				"POS_BTN", R.string.alert_Uninstall,
				"POS_BTN_CLICK", pbtn_Click
				);
			AlertDialog ad = TNUtilsUi.alertDialogBuilder(jsonData);
			ad.setCanceledOnTouchOutside(false);
			ad.setCancelable(false);
			ad.show();
		}else{
			startRun();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		// 屏蔽任何按键
		return true;
	} 
	
	// Private methods
	//-------------------------------------------------------------------------------
	private void startRun(){
		if(isRunning) return;
		isRunning = true;
		
		final TNSettings settings = TNSettings.getInstance();
				
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(settings.isLogin()) {
					//如果user表被异常清空
					TNUser user = TNDbUtils.getUser(settings.userId);
					if (user == null) {
						startActivity(TNLoginAct.class, extraBundle);
					} else {
						startActivity(TNMainAct.class, extraBundle);
					}
					finish();
				} else if ((settings.expertTime != 0) && (settings.expertTime*1000-System.currentTimeMillis() < 0)) {
					TNAction.runActionAsync(TNActionType.Login, settings.loginname, settings.password);
				} else {
					startActivity(TNLoginAct.class, extraBundle);
					finish();
				}
		 	   	isRunning = false;
			}
		}, 2000);
	}
	
	public void respondLogin(TNAction aAction) {
		android.util.Log.d("SJY", "respondLogin:");
		if(!TNHandleError.handleResult(this, aAction)){
			if(!runExtraIntent()){
				TNSettings settings = TNSettings.getInstance();
				Bundle b = new Bundle();
				settings.isLogout = false;
				settings.savePref(false);
				startActivity(TNMainAct.class, b);
				finish();
			}
		} else {
			TNSettings settings = TNSettings.getInstance();
			Bundle b = new Bundle();
			settings.isLogout = true;
			settings.savePref(false);
			startActivity(TNLoginAct.class, b);
			finish();
		}
	}
	
}