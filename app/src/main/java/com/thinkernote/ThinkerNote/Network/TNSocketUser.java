package com.thinkernote.ThinkerNote.Network;

import android.text.TextUtils;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.Utils.MLog;

import org.json.JSONObject;

/**
 * handle_Login
 * handle_LoginThird
 * handle_Profile
 */
public class TNSocketUser {
	private static final String TAG = "TNSocketUser";
	public static void handle_Login(TNAction aAction){
		MLog.i(TAG, "handle_LOGIN");
		JSONObject inputs = (JSONObject)aAction.inputs.get(2);
		JSONObject outputs = (JSONObject)aAction.outputs.get(0);
		int result = (Integer)TNUtils.getFromJSON(outputs, "code");
		
		if( result == 0){
			TNSettings settings = TNSettings.getInstance();
			settings.password = (String) TNUtils.getFromJSON(inputs, "password");
			settings.userId = Long.valueOf(TNUtils.getFromJSON(outputs, "user_id").toString());
			settings.username = (String) TNUtils.getFromJSON(outputs, "username");
			settings.token = (String) TNUtils.getFromJSON(outputs, "token");
			settings.expertTime = Long.valueOf(TNUtils.getFromJSON(outputs, "expire_at").toString());
			if (TextUtils.isEmpty(settings.loginname)) {
				settings.loginname = (String) TNUtils.getFromJSON(outputs, "username");
			}
			settings.savePref(false);
			
			aAction.runChildAction(TNActionType.Profile);
			
		}

	}
	
	public static void handle_LoginThird(TNAction aAction){
		MLog.i(TAG, "handle_LoginThird");
		JSONObject inputs = (JSONObject)aAction.inputs.get(2);
		JSONObject outputs = (JSONObject)aAction.outputs.get(0);
		int result = (Integer)TNUtils.getFromJSON(outputs, "code");
		
		if( result == 0){
			TNSettings settings = TNSettings.getInstance();
			settings.password = (String) TNUtils.getFromJSON(outputs, "password");
			settings.userId = Long.valueOf(TNUtils.getFromJSON(outputs, "user_id").toString());
			settings.username = (String) TNUtils.getFromJSON(outputs, "username");
			settings.token = (String) TNUtils.getFromJSON(outputs, "token");
			settings.expertTime = Long.valueOf(TNUtils.getFromJSON(outputs, "expire_at").toString());
			if (TextUtils.isEmpty(settings.loginname)) {
				settings.loginname = (String) TNUtils.getFromJSON(outputs, "username");
			}
			settings.savePref(false);
			
			aAction.runChildAction(TNActionType.Profile);
			
		}

	}
	
	public static void handle_Profile(TNAction aAction){ 
		JSONObject outputs = (JSONObject)aAction.outputs.get(0);
		TNSettings settings = TNSettings.getInstance();
		long userId = TNDbUtils.getUserId(settings.username);
		
		if ((Integer)TNUtils.getFromJSON(outputs, "code") == 0) {
			JSONObject profileObj = (JSONObject) TNUtils.getFromJSON(outputs, "profile") ;
			settings.phone = (String) TNUtils.getFromJSON(profileObj, "phone");
			settings.email = (String) TNUtils.getFromJSON(profileObj, "email");
			settings.defaultCatId = Long.valueOf(TNUtils.getFromJSON(profileObj, "default_folder").toString());
			
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
					"emailVerify", TNUtils.getFromJSON(profileObj, "emailverify"),
					"totalSpace", TNUtils.getFromJSON(profileObj, "total_space"),
					"usedSpace", TNUtils.getFromJSON(profileObj, "used_space"));

			//更新user表
			UserDbHelper.addOrUpdateUser(user);
		} else {
			aAction.failed(outputs);
		}
		
		settings.savePref(false);
	}
	
	public static void initCats(TNAction aAction) {
		aAction.runChildAction(TNActionType.FolderAdd, -1L, TNConst.FOLDER_MEMO);
		aAction.runChildAction(TNActionType.FolderAdd, -1L, TNConst.GROUP_FUN);
		aAction.runChildAction(TNActionType.GetParentFolders);
	}
	
	public static void initTags(TNAction aAction) {
		aAction.runChildAction(TNActionType.TagAdd, TNConst.TAG_IMPORTANT);
		aAction.runChildAction(TNActionType.TagAdd, TNConst.TAG_TODO);
		aAction.runChildAction(TNActionType.TagAdd, TNConst.TAG_GOODSOFT);
		
		aAction.runChildAction(TNActionType.GetTagList);
	}
	
	public static void initNotes(TNAction aAction) {
		TNNote note = TNNote.newNote();					
		TNNoteAtt pngAtt = TNNoteAtt.newAtt("thinkernote.jpg", null);
		note.atts.add(pngAtt);
		
		note.title = TNConst.FIRSTNOTE_TITLE;
		note.tagStr = TNConst.FIRSTNOTE_TAG;
		
		String contentFormat = 
				"拥有了轻笔记，您可以：<br /><br />" +
				"❤在手机与电脑之间同步查看使用资料~~ <br /><br />" +
				"❤随时随地快速录入您的新点子~~<br /><br />" + 
				"❤离线阅读网络小说~~<br /><br />" + 
				"❤写日记，记录每天好心情~~<br /><br />" + 
				"❤建立自己的群组，分享照片、音乐，交换学习笔记，管理团队~~<br /><br />" + 
				"❤轻松一键保存喜欢的网页~~<br /><br />" +
				"即将推出：<br /><br />" +
				"❤给笔记上闹钟，时时提醒,还贷提醒、日程提醒~~<br /><br />" +
				"❤更多内容请访问<a href=\"http://www.qingbiji.cn\">http://www.qingbiji.cn</a>";
		note.content = TNUtilsHtml.decodeHtml(contentFormat);
		note.shortContent = TNUtils.getBriefContent(note.content);
		
		aAction.runChildAction(TNActionType.NoteAdd, note, true);
	}
	
}
