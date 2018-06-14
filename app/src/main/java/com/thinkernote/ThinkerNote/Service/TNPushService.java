package com.thinkernote.ThinkerNote.Service;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.thinkernote.ThinkerNote.Activity.TNRemindAct;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNSettings;

public class TNPushService extends Service {
	private static final String TAG = "TNPushService";
	private static final long MSG_REMIND_TIME_INTERVAL = 5*60*1000;
	
	Binder mBinder=new LocalBinder();
	private long mOriginalNoteId = 0;
	private int mOriginalRemindCount = 0;
	private int mOriginalRemaindType = 0;
	private long mOriginalProjectId = 0;
	private String mOriginalStatus = "";
	
	private Timer mTimer;
	private TimerTask mTimerTask;
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
//		TNAction.regResponder(TNActionType.GetPushInfo, this, "RespondGetPushInfo");
//		TNAction.regResponder(TNActionType.SetRemindTime, this, "RespondSetRemindTime");
		TNSettings.getInstance().serviceRuning = true;
		toGetPush();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i(TAG, "onStart");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		Log.i(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		TNSettings.getInstance().serviceRuning = false;
	}
	
	public class LocalBinder extends Binder{
		public TNPushService getService(){
			return TNPushService.this;
		}
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	private void toGetPush(){
		if(mTimer != null){
			mTimer.cancel();
			mTimer = null;
		}
		if(mTimerTask != null){
			mTimerTask.cancel();
			mTimerTask = null;
		}
		
		mTimerTask = new TimerTask() {
			
			@Override
			public void run() {
				TNSettings settings = TNSettings.getInstance();
            	Log.i(TAG, "toGetPush:" + "userName:" + settings.username 
            			+ " password:" + settings.password
            			+ "group:" + settings.remindLockGroup + " note:" + settings.remindLockNote);
            	if(settings.username.length() > 0 
            			&& settings.password.length() == 32){
            		String remindType = "remind";
            		if(settings.remindLockGroup && settings.remindLockNote){
            			remindType = "remind";
            		}else if(settings.remindLockGroup){
            			remindType = "remindProject";
            		}else if(settings.remindLockNote){
            			remindType = "remindNote";
            		}else
            			return;
            		
//            		TNAction.runAction(TNActionType.GetPushInfo, remindType, settings.username);
            	}
			}
		};
		mTimer = new Timer();
		mTimer.schedule(mTimerTask, 20 * 1000, MSG_REMIND_TIME_INTERVAL);
		
//	    AsyncTask<Object, Object, Object> taskWatcher=
//	    	new AsyncTask<Object, Object, Object>() {
//
//	        @Override
//	        protected Object doInBackground(Object... params) {
//	            while(TNSettings.getInstance().serviceRuning){
//	                try {
//	                    Thread.sleep(MSG_REMIND_TIME_INTERVAL);
//	                } catch (InterruptedException e) {
//	                    e.printStackTrace();
//	                }
//	                
//	            	TNSettings settings = TNSettings.getInstance();
//	            	Log.i(TAG, "toGetPush:" + "userName:" + settings.username 
//	            			+ " password:" + settings.password
//	            			+ "group:" + settings.remindLockGroup + " note:" + settings.remindLockNote);
//	            	if(settings.username.length() > 0 
//	            			&& settings.password.length() == 32){
//	            		String remindType = "remind";
//	            		if(settings.remindLockGroup && settings.remindLockNote){
//	            			remindType = "remind";
//	            		}else if(settings.remindLockGroup){
//	            			remindType = "remindProject";
//	            		}else if(settings.remindLockNote){
//	            			remindType = "remindNote";
//	            		}else
//	            			continue;
//	            		
//	            		TNAction.runAction(TNActionType.GetPushInfo, remindType);
//	            	}
//	            }
//				return params;
//	        }
//	    };
//	    taskWatcher.execute(null, null);
	}
	
	public void RespondGetPushInfo(TNAction aAction){
		Log.i(TAG, "RespondGetPushInfo");
		if(aAction.result != TNActionResult.Finished){
			return ;
		}
			
		/*  pushInfo
		 *  0 RESULT
		 *  1 REMIND_TYPE
		 *  2 REMIND_COUNT
		 *  3 REMIND_TIME
		 *  4 UPDATE_TIME
		 *  5 TITLE
		 *  6 NOTE_ID
		 *  7 NICKNAME
		 *  8 PROJECT_NAME
		 *  9 PROJECT_ID
		 *  10 STATUS
		 */
		@SuppressWarnings("unchecked")
		ArrayList<Object> pushInfo = (ArrayList<Object>) aAction.outputs.get(0);

		int result = Integer.valueOf(pushInfo.get(0).toString());		
		if(result == 0){
			int remindType = Integer.valueOf(pushInfo.get(1).toString());	//1、笔记 2、个人 3、管理员 
			int remindCount = Integer.valueOf(pushInfo.get(2).toString());
			long remindTime = Long.valueOf(pushInfo.get(3).toString());
//			long updateTime = Long.valueOf(pushInfo.get(4).toString());
			String noteTitle = pushInfo.get(5).toString();
			long noteId = Long.valueOf(pushInfo.get(6).toString());
			String nikeName = pushInfo.get(7).toString();
			String projectName = pushInfo.get(8).toString();
			long projectId = Long.valueOf(pushInfo.get(9).toString());
			String status = pushInfo.get(10).toString();
			String updateType = pushInfo.get(11).toString();
			
			if(remindCount == mOriginalRemindCount
					&& noteId == mOriginalNoteId
					&& remindType == mOriginalRemaindType
					&& projectId == mOriginalProjectId
					&& status.equals(mOriginalStatus)){
				Log.i(TAG, "no new remind");
				return ;
			}
			
			mOriginalRemindCount = remindCount;
			mOriginalNoteId = noteId;
			mOriginalRemaindType = remindType;
			mOriginalProjectId = projectId;
			mOriginalStatus = status;
			
			if(remindType == 1){
				if(nikeName.equals(TNSettings.getInstance().username)){//isSelf
					return;
				}
				String title = "共【" + String.valueOf(remindCount) + "】篇笔记更新";
				String msg = "【" + nikeName + "】更新了" + "【" + projectName + "】的 【" + "【" + noteTitle + "】";
				if(updateType.equals("comment")){
					msg = "【" + nikeName + "】评论了" + "【" + projectName + "】的 【" + "【" + noteTitle + "】";
				}
				
				showRemainNotification(title, msg, remindType, remindTime, status);
			}else{
				String title = "共【" + String.valueOf(remindCount) + "】篇笔记更新";
				String msg = "";
				if(remindType == 2){
					if(status.equals("active")){
						msg = "您已加入【" + projectName + "】群组";
					}else if(status.equals("deny")){
						msg = "您申请加入【" + projectName + "】被拒绝";
					}else if(status.equals("removed")){
						msg = "您已被【"+ projectName + "】的管理员移除出了该群组";
					}else
						return;
				}else{
					if(status.equals("pending")){
						msg = "【" + nikeName + "】申请加入【" + projectName + "】群组";
					}else if(status.equals("active")){
						msg = "【" + nikeName + "】已加入【" + projectName + "】群组";
					}else if(status.equals("deny")){
						msg = "【" + nikeName + "】被拒绝加入【" + projectName + "】群组";
					}else if(status.equals("removed")){
						msg = "【"+ nikeName + "】已被【" + projectName + "】群组管理员移除";
					}else
						return;
				}
				showRemainNotification(title, msg, remindType, remindTime, status);
			}
		}else{
			Log.i(TAG, "no MsgRemind");
		}
	}
	
	public void RespondSetRemindTime(TNAction aAction){
		Log.i(TAG, "RespondSetRemindTime");
		if(aAction.result == TNActionResult.Finished){
			mOriginalNoteId = 0;
			mOriginalRemindCount = 0;
			mOriginalRemaindType = 0;
			mOriginalProjectId = 0;
			mOriginalStatus = "";
		}
	}
	
	private void showRemainNotification(String title, String msg, int remindType, long remindTime, String status){
		Log.d(TAG, "showRemainNotification: title:" + title + " msg:" + msg + " remindType:" + remindType + " status:" + status);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(2);
		
        // 创建一个Notification
		Builder builder = new Notification.Builder(getApplicationContext());
        Intent intent = null;
        if(remindType == 3){
        	if(!status.equals("pending")){
        		intent = new Intent(getApplicationContext(), TNRemindAct.class);
        	}
        }
        intent.putExtra("Type", "MsgRemaind");
        intent.putExtra("RemindTime", remindTime);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
        		getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        
        builder.setContentIntent(pendingIntent);
        Notification notification = builder        
          		 .setContentText(getApplication().getString(R.string.notifcation_remaind_title))        
          		 .setSmallIcon(R.drawable.icon)
          		 .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        // 添加声音提示
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;
        // audioStreamType的值必须AudioManager中的值，代表着响铃的模式
        notification.audioStreamType = android.media.AudioManager.ADJUST_LOWER;
        
        manager.notify(2, notification);
	}

}
