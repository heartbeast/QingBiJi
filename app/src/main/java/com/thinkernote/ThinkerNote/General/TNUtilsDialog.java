package com.thinkernote.ThinkerNote.General;

import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.Activity.TNCatInfoAct;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNSQLString;

public class TNUtilsDialog {
	static Handler mHandler;
	static TimerTask mTimerTask;
	
	public static void logout(final Activity act){
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				TNSettings settings = TNSettings.getInstance();
//				settings.username = "";
//				settings.tnAccessToken = "";
				settings.password = "";
				settings.userType = 0;
				settings.refreshToken = null;
				settings.accessToken = null;
				settings.sinaUid = "";
				settings.savePref(false);
					
				settings.isLogout = true;
				act.finish();
				NotificationManager nm = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancelAll();
			}
		};
		
		JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", act,
				"TITLE", R.string.alert_Title,
				"MESSAGE", R.string.alert_UserInfo_LogoutMsg,
				"POS_BTN", R.string.alert_OK,
				"POS_BTN_CLICK", pbtn_Click,
				"NEG_BTN", R.string.alert_Cancel
				);
		TNUtilsUi.alertDialogBuilder(jsonData).show();
	}
	
	public static void deleteComment(final Activity act, 
			final TNRunner callback,
			final long comLocalId){
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
//				TNAction.runAction(TNActionType.CommentDelete, comLocalId); 	
				if( callback != null)
					callback.run();
			}
		};
		JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", act,
				"TITLE", R.string.alert_Title,
				"MESSAGE", R.string.alert_NoteView_DeleteNoteMsg,
				"POS_BTN", R.string.alert_OK,
				"POS_BTN_CLICK", pbtn_Click,
				"NEG_BTN", R.string.alert_Cancel
				);
		TNUtilsUi.alertDialogBuilder(jsonData).show();
	}
	
	public static void deleteNote(final Activity act, 
			final TNRunner callback,
			final long noteLocalId){
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				TNAction.runAction(TNActionType.NoteLocalDelete, noteLocalId);
				if( callback != null)
					callback.run();
			}
		};
		int msg = R.string.alert_NoteView_DeleteNoteMsg;
		if(TNSettings.getInstance().isInProject()){
			msg = R.string.alert_NoteView_DeleteNoteMsg_InGroup;
		}
		
		JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", act,
				"TITLE", R.string.alert_Title,
				"MESSAGE", msg,
				"POS_BTN", R.string.alert_OK,
				"POS_BTN_CLICK", pbtn_Click,
				"NEG_BTN", R.string.alert_Cancel
				);
		TNUtilsUi.alertDialogBuilder(jsonData).show();
	}

	public static void realDeleteNote(final Activity act, 
			final TNRunner callback,
			final long noteLocalId){
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				TNAction.runAction(TNActionType.NoteLocalRealDelete, noteLocalId); 
				if( callback != null)
					callback.run();
			}
		};
		
		JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", act,
				"TITLE", R.string.alert_Title,
				"MESSAGE", R.string.alert_NoteView_RealDeleteNoteMsg,
				"POS_BTN", R.string.alert_OK,
				"POS_BTN_CLICK", pbtn_Click,
				"NEG_BTN", R.string.alert_Cancel
				);
		TNUtilsUi.alertDialogBuilder(jsonData).show();

	}
	
	public static void synchronize(final Activity act, 
			final TNRunner callback,
			final TNRunner cancell_callback,
			final TNActionType actionType,
			final Object ... extraInfos){
		
		if( !checkNetwork(act) ){
			if(cancell_callback != null)
				cancell_callback.run();
			return;
		}
		if( !TNActionUtils.isSynchronizing()){
			if( actionType == TNActionType.GetAllDataByNoteId || actionType == TNActionType.GetAllData){
				DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if( !TNActionUtils.isSynchronizing()){
							TNUtilsUi.showNotification(act, 
									R.string.alert_NoteView_Synchronizing, false);
							TNAction.runActionAsync(actionType, extraInfos);
							if( callback != null)
								callback.run();
						}
					}
				};
				
				DialogInterface.OnClickListener nbtn_Click = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(cancell_callback != null)
							cancell_callback.run();
					}
				};
				
				int msg = R.string.alert_MainCats_SynchronizeAll; 
				if(actionType == TNActionType.GetAllData){
					msg = R.string.alert_project_MainCats_SynchronizeAll;
				}else if(actionType == TNActionType.GetAllDataByNoteId){
					msg = R.string.alert_MainCats_SynchronizeNoteAll;
				}
				int posBtnText = R.string.maincats_menu_syncall;
				
				LayoutInflater lf = LayoutInflater.from(act);
				View title = lf.inflate(R.layout.dialog, null);
			
				JSONObject jsonData = null;
				jsonData = TNUtils.makeJSON(
						"CONTEXT", act,
						"TITLE", title,
						"MESSAGE", msg,
						"POS_BTN", posBtnText,
						"POS_BTN_CLICK", pbtn_Click,
						"NEG_BTN", R.string.alert_Cancel,
						"NEG_BTN_CLICK", nbtn_Click
						);
				TNUtilsUi.alertDialogBuilder(jsonData).show();
			}else{
				TNUtilsUi.showNotification(act, 
						R.string.alert_NoteView_Synchronizing, false);
				TNAction.runActionAsync(actionType, extraInfos);
				if( callback != null)
					callback.run();
			}
				
		}else{
			DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if( TNActionUtils.isSynchronizing()){
						TNActionUtils.stopSynchronizing();
						if( callback != null)
							callback.run();
					}
				}
			};
			
			DialogInterface.OnClickListener nbtn_Click = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(cancell_callback != null)
						cancell_callback.run();
				}
			};

			LayoutInflater lf = LayoutInflater.from(act);
			View title = lf.inflate(R.layout.dialog, null);
		
			JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", act,
//				"TITLE", R.string.alert_Title,
				"TITLE", title,
				"MESSAGE", R.string.alert_Synchronize_TooMuch,
				"POS_BTN", R.string.alert_StopSync,
				"POS_BTN_CLICK", pbtn_Click,
				"NEG_BTN", R.string.alert_Cancel,
				"NEG_BTN_CLICK", nbtn_Click
				);
			TNUtilsUi.alertDialogBuilder(jsonData).show();
		}
	}
	
	public static void synchronizeAlert(Activity act,
			final TNActionType actionType,
			final Object ... extraInfos){
		if(!TNUtils.isNetWork()){
			TNUtilsUi.showToast(act.getString(R.string.alert_NoNetWork_NoteSave));
			return;
		}
		if(TNActionUtils.isSynchronizing()){
			TNActionUtils.stopSynchronizing();
		}
		
		TNUtilsUi.showNotification(act, 
				R.string.alert_NoteView_Synchronizing, false);
		TNAction.runActionAsync(actionType, extraInfos);
		
	}
	
	public static boolean checkNetwork(Activity act){
		
		boolean network = TNUtils.isNetWork();
		if( !network){
			TNUtilsUi.alert(act, R.string.alert_Net_NotWork);
		}
		return network;
	}
	
	public static void RunActionDialog(final Activity act, final TNRunner callback, 
			final TNActionType type, final boolean isAsync, final boolean actFinish,
			int msg, final Object... objects ){
		RunActionDialog(act, callback, type, isAsync, actFinish, msg, R.string.alert_OK, R.string.alert_Cancel, objects);
	}
	
	public static void RunActionDialog(final Activity act, final TNRunner callback, 
			final TNActionType type, final boolean isAsync, final boolean actFinish,
			Object msg, int posbtnName, int negbtnName, final Object... objects ){
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(isAsync)
					TNAction.runActionAsync(type, objects);
				else
					TNAction.runAction(type, objects);
				if( callback != null)
					callback.run();
				if(actFinish){
					act.finish();
				}
			}
		};
		
		JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", act,
				"TITLE", R.string.alert_Title,
				"MESSAGE", msg,
				"POS_BTN", R.string.alert_OK,
				"POS_BTN_CLICK", pbtn_Click,
				"NEG_BTN", R.string.alert_Cancel
				);
		TNUtilsUi.alertDialogBuilder(jsonData).show();
	}
	
	public static void restoreNote(final Activity act, 
			final TNRunner callback,
			final long noteLocalId){
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// restore
				TNAction.runAction(TNActionType.NoteLocalRecovery, noteLocalId);
				if( callback != null)
					callback.run();
			}
		};

		JSONObject jsonData = TNUtils.makeJSON(
			"CONTEXT", act,
			"TITLE", R.string.alert_Title,
			"MESSAGE", R.string.alert_NoteView_RestoreHint,
			"POS_BTN", R.string.alert_OK,
			"POS_BTN_CLICK", pbtn_Click,
			"NEG_BTN", R.string.alert_Cancel
			);
		TNUtilsUi.alertDialogBuilder(jsonData).show();
	}
	
	
	public static void showAppCommentDialog(final Activity act){
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		    	Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id="+ act.getPackageName()));
				TNUtilsDialog.startIntent(act, intent, 
						R.string.alert_About_CantOpenComment);
			}
		};
		
		JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", act,
				"TITLE", R.string.alert_Title,
				"MESSAGE", R.string.alert_AppComment_Msg,
				"POS_BTN", R.string.alert_AppComment_Go,
				"POS_BTN_CLICK", pbtn_Click,
				"NEG_BTN", R.string.alert_Cancel
				);
			TNUtilsUi.alertDialogBuilder(jsonData).show();
	}

	
	public static void startIntent(final Activity act, final Intent intent,
			int msgId){
		PackageManager packageManager = act.getPackageManager();
		if (packageManager.queryIntentActivities(intent, 
				PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
			act.startActivity(intent);
		}else{
			TNUtilsUi.alert(act, msgId);
		}
	}
	
	public static void startIntentForResult(final Activity act, final Intent intent,
			int msgId, int requestCode){
		PackageManager packageManager = act.getPackageManager();
		if (packageManager.queryIntentActivities(intent, 
				PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
			act.startActivityForResult(intent, requestCode);
		}else{
			TNUtilsUi.alert(act, msgId);
		}
	}
	
	public static void deleteCatDialog(final Activity act, final TNRunner callback, final TNCat cat){
		int dialogMsg = R.string.alert_CatInfo_Delete_HasChild;
		
		DialogInterface.OnClickListener pbtn_Click = 
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					TNAction.runActionAsync(TNActionType.FolderDelete, cat.catId);
					if(TNCatInfoAct.class.isInstance(act)){
						act.finish();
					}
					if(callback != null){
						callback.run();
					}
				}
			};
			
			JSONObject jsonData = TNUtils.makeJSON(
					"CONTEXT", act,
					"TITLE", R.string.alert_Title,
					"MESSAGE", dialogMsg,
					"POS_BTN", R.string.alert_OK,
					"POS_BTN_CLICK", pbtn_Click,
					"NEG_BTN", R.string.alert_Cancel
					);
			TNUtilsUi.alertDialogBuilder(jsonData).show();
		
	}
	
	public static void showUserInfoNotCompleted(final Activity act, Object msg){
		DialogInterface.OnClickListener pbtn_Click = 
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent();
					i.putExtra("Type", "USER_INFO");
					startActivity(act, i, "TNSettingsAct");
				}
			};
			
			JSONObject jsonData = TNUtils.makeJSON(
					"CONTEXT", act,
					"TITLE", R.string.alert_Title,
					"MESSAGE", msg,
					"POS_BTN", R.string.alert_Welcome_Change,
					"POS_BTN_CLICK", pbtn_Click,
					"NEG_BTN", R.string.alert_Welcome_Cancel
					);
			TNUtilsUi.alertDialogBuilder(jsonData).show();
	}
	
	
	public static void showBindingDialog(final Activity act, final int type, final TNRunner callback, Object msg){
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent();
				Bundle b = new Bundle();
				b.putInt("UserType", type);
				b.putString("TaskType", "binding");
				i.putExtras(b);
				startActivity(act, i, "TNAuthAct");				
				if(callback != null){
					callback.run();
				}
			}
		};
		
		//不再提醒
		DialogInterface.OnClickListener ubtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(type == 1)
					TNSettings.getInstance().showDialogType |= 0x01;
				else if(type == 8)
					TNSettings.getInstance().showDialogType |= 0x04;
				
				TNSettings.getInstance().savePref(true);
				if(callback != null){
					callback.run();
				}
			}
		};
		//取消
		DialogInterface.OnClickListener nbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(callback != null){
					callback.run();
				}
			}
		};
		
		JSONObject jsonData = TNUtils.makeJSON(
				"CONTEXT", act,
				"TITLE", R.string.alert_Title,
				"MESSAGE", msg,
				"POS_BTN", R.string.alert_OK,
				"POS_BTN_CLICK", pbtn_Click,
				"NEU_BTN", R.string.alert_NoRemind,
				"NEU_BTN_CLICK", ubtn_Click,
				"NEG_BTN", R.string.alert_Cancel,
				"NEG_BTN_CLICK",nbtn_Click
				);
		AlertDialog ad = TNUtilsUi.alertDialogBuilder(jsonData);
		ad.show();
		ad.setCancelable(false);
		ad.setCanceledOnTouchOutside(false);
	}
	
	public static void showNoReadPrivDialog(final Activity act, Object msg){
		DialogInterface.OnClickListener pbtn_Click = 
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(act, null, "TNMainAct");
					act.finish();
				}
			};
			JSONObject jsonData = TNUtils.makeJSON(
					"CONTEXT", act,
					"TITLE", R.string.alert_Title,
					"MESSAGE", msg,
					"POS_BTN", R.string.alert_OK,
					"POS_BTN_CLICK", pbtn_Click
					);
		TNUtilsUi.alertDialogBuilder(jsonData).show();
	}
	
	public static void startActivity(Activity act, Intent i, String actName){
		if(i == null)
			i = new Intent();
		
		i.setClassName(TNSettings.kThinkerNotePackage, TNSettings.kActivityPackage + "." + actName);
		act.startActivity(i);
	}
}
