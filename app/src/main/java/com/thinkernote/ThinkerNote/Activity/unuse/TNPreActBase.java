package com.thinkernote.ThinkerNote.Activity.unuse;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.baidu.mobstat.StatService;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
/**
 * TODO 未使用
 */
public class TNPreActBase extends PreferenceActivity {
	private static final String kThinkerNotePackage = TNSettings.kThinkerNotePackage;
	private static final String kActivityPackage = TNSettings.kActivityPackage;
	
	protected final String TAG = getClass().getSimpleName();
	protected boolean isInFront;
	protected int createStatus; // 0 firstCreate, 1 resume, 2 reCreate

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate:" + savedInstanceState);
		super.onCreate(savedInstanceState);		
		System.gc();
		
		//响应换肤事件
//		TNAction.regResponder(TNActionType.ChangeSkin, this, "RespondChangeSkin");
		
		createStatus = (savedInstanceState==null) ? 0 : 2;
		
		overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);
	}
	
	protected void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		
		TNSettings settings = TNSettings.getInstance();
		if(settings.isLogout /*|| settings.isGoHome*/){
			finish();
			return;
		}
		
	}
	
	@Override 
	public void finish () {
		Log.d(TAG, "finish");
		TNAction.unregister(this);
		
		super.finish();
	}

	protected void onResume(){
		Log.d(TAG, "onResume");
		super.onResume();

		//百度
		StatService.onResume(this);

		TNSettings settings = TNSettings.getInstance();

		isInFront = true;
		TNSettings.getInstance().topAct = this;
		
		// 直接安装包启动，再home退出，再进入，将分别产生2个task。一个task退出将导致另一个task出错。
		if( !TAG.equals("TNSplashAct") && !TAG.equals("TNLoginAct") && !TAG.equals("TNAboutAct")
				&& settings.userId <= 0 && !isFinishing()){
			finish();
			return;
		}
		
		configView();
		createStatus = 1;
		
		TNUtilsUi.checkLockScreen(this);
		if( settings.needShowLock){
			if( !TAG.equals("TNLockAct") && !TAG.equals("TNSplashAct")
					&& settings.lockPattern.size() > 0){
				Log.i(TAG, "show lock");
				Bundle b = new Bundle();
				b.putInt("Type", 2);
				b.putString("OriginalPath", settings.lockPattern.toString());
				runActivity("TNLockAct", b);
			}
		}
		
	}
	
	protected void configView(){
	}
	
	protected void setViews(){}

	protected void onPause(){
		Log.d(TAG, "onPause");
		overridePendingTransition(R.anim.hold, R.anim.push_out_to_right);
		super.onPause();
		
		//百度
		StatService.onPause(this);
		
		isInFront = false;
		
		TNSettings settings = TNSettings.getInstance();
		if( settings.topAct == this )
			settings.topAct = null;
	}

	@Override
	public void onDestroy(){
		Log.d(TAG, "onDestroy");
		TNAction.unregister(this);
		super.onDestroy();
	}
	
	//-------------------------------------------------------------------------------
	public void runActivity(String aActName){
		runActivity(aActName, null);
	}

	public void runActivity(String aActName, Bundle aBundle){
		Intent i = new Intent();
		i.setClassName(kThinkerNotePackage, kActivityPackage + "." + aActName);
		if(aBundle != null)
			i.putExtras(aBundle);

		startActivity(i);
	}

	public void runActivityForResult(String aActName, Bundle aBundle, int requestCode){
		Intent i = new Intent();
		i.setClassName(kThinkerNotePackage, kActivityPackage + "." + aActName);
		if(aBundle != null)
			i.putExtras(aBundle);
		startActivityForResult(i, requestCode);
	}
	
	public boolean runExtraIntent(){
		Intent it = getIntent();
		if( it.hasExtra(Intent.EXTRA_INTENT) ){
			startActivity((Intent) it.getExtras().get(Intent.EXTRA_INTENT));
			return true;
		}
		return false;
	}
	
	public void RespondChangeSkin(TNAction aAction){
		Log.i(TAG, "RespondChangeSkin");
		setViews();
	}
}
