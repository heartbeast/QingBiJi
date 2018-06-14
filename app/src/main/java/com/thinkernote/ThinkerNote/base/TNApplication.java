package com.thinkernote.ThinkerNote.base;

import android.app.Application;
import android.os.AsyncTask;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDb2;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.OAuth2.TNOAuth2;
import com.thinkernote.ThinkerNote.Service.TNAttDownloadService;
import com.thinkernote.ThinkerNote.Service.TNAttLocalService;
import com.thinkernote.ThinkerNote.Service.TNAttService;
import com.thinkernote.ThinkerNote.Service.TNCacheService;
import com.thinkernote.ThinkerNote.Service.TNCatService;
import com.thinkernote.ThinkerNote.Service.TNLBSService;
import com.thinkernote.ThinkerNote.Service.TNNoteLocalService;
import com.thinkernote.ThinkerNote.Service.TNNoteService;
import com.thinkernote.ThinkerNote.Service.TNSyncService;
import com.thinkernote.ThinkerNote.Service.TNTagService;
import com.thinkernote.ThinkerNote.Service.TNUserService;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.http.HttpUtils;
import com.thinkernote.ThinkerNote.http.wechat.WeichatHttpUtils;

/**
 * sjy 0607
 */
public class TNApplication extends Application {
	private static final String TAG = "TNApplication";

	@Override
	public void onCreate() {
		// 设置是否输出log
		Log.DEBUG = true;
		// 是否使用测试服务器
		TNOAuth2.useTestServer(false);

		Log.d(TAG, "onCreate");
		super.onCreate();
		
		TNAction.regRunner(TNActionType.DbReportError, this, "DbReportError");
		
		initialize();
		//新网络框架初始化
		MLog.init(true,"SJY");

		//初始化网络
		HttpUtils.getInstance().init(this, MLog.DEBUG);

		//微信初始化网络
		WeichatHttpUtils.getInstance().init(this, MLog.DEBUG);
	}
	
	// private methods
	//-------------------------------------------------------------------------------
	private void initialize(){
		Log.d(TAG, "initialize");
		
		TNSettings settings = TNSettings.getInstance();
		settings.appContext = this;
		settings.readPref();

		// 设置此接口后，音频文件和识别结果文件保存在/sdcard/msc/record/目录下
		//com.iflytek.resource.MscSetting.setLogSaved(true);
		
		// db initialize
		TNDb.getInstance();
		TNDb2.getInstance();
		
		// service initialize
		TNSyncService.getInstance();
		TNNoteLocalService.getInstance();
		TNAttLocalService.getInstance();
		TNCacheService.getInstance();
		TNUserService.getInstance();
		TNNoteService.getInstance();
		TNAttService.getInstance();
		TNTagService.getInstance();
		TNCatService.getInstance();
		TNOAuth2.getInstance();
		TNAttDownloadService.getInstance();
		
		TNLBSService.getInstance();
		
//		//设置定制的默认皮肤， 暂指三星定制的奥运皮肤
//		TNUtilsSkin.setAssetsSkins(TNUtilsSkin.DEFAULT_SKIN);
		
		watchAppSwitch();
	}
	
	public void DbReportError(TNAction aAction){
		Log.i(TAG, "DbReportError s" + TNSettings.getInstance().topAct);
		//TNUtilsUi.showToast("DB ERROR!!");
		if( TNSettings.getInstance().topAct != null){
			TNUtilsUi.showNotification(TNSettings.getInstance().topAct,
				R.string.alert_DBError, true);
		}
		TNSettings.getInstance().hasDbError = true;
		TNSettings.getInstance().savePref(false);
		
		aAction.finished();
		Log.i(TAG, "DbReportError e");
	}
	
	private void watchAppSwitch(){
	    //一个线程，让我一直检测
	    AsyncTask<Object, Object, Object> taskWatcher=
	    	new AsyncTask<Object, Object, Object>() {

	        @Override
	        protected Object doInBackground(Object... params) {
	            
	            //把这个while当成看门狗吧。
	            while(true){
	        		TNUtilsUi.checkLockScreen(TNApplication.this);
	            
	                try {
	                    Thread.sleep(200);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            
	            }
	        }
	    };
	    
	    taskWatcher.execute(null, null);
	}
}
