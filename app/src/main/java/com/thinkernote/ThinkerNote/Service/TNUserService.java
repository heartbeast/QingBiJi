package com.thinkernote.ThinkerNote.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.Utils.MLog;

import android.text.TextUtils;

public class TNUserService {
	private static final String TAG = "TNUserService";

	private static TNUserService singleton = null;

	//-------------------------------------------------------------------------------
	// Singleton
	private TNUserService(){
		Log.d(TAG,"TNUserService()");
		TNAction.regRunner(TNActionType.Register, this, "Register");
		TNAction.regRunner(TNActionType.Captcha, this, "Captcha");
		TNAction.regRunner(TNActionType.VerifyCode, this, "VerifyCode");
		TNAction.regRunner(TNActionType.Login, this, "Login");
		TNAction.regRunner(TNActionType.Logout, this, "Logout");
		TNAction.regRunner(TNActionType.Profile, this, "Profile");
		TNAction.regRunner(TNActionType.GetAllData, this, "GetAllData");
		TNAction.regRunner(TNActionType.GetAllDataByNoteId, this, "GetAllDataByNoteId");
		TNAction.regRunner(TNActionType.VerifyEmail, this, "VerifyEmail");
		TNAction.regRunner(TNActionType.ForgotPassword, this, "ForgotPassword");
		TNAction.regRunner(TNActionType.FindPswByEmail, this, "FindPswByEmail");
		TNAction.regRunner(TNActionType.LoginThird, this, "LoginThird");
		TNAction.regRunner(TNActionType.LoginBind, this, "LoginBind");

		TNAction.regRunner(TNActionType.ChangePassword, this, "ChangePassword");		
		TNAction.regRunner(TNActionType.ChangeUserNameOrEmail, this, "ChangeUserNameOrEmail");
		TNAction.regRunner(TNActionType.ChangePhone, this, "ChangePhone");
		TNAction.regRunner(TNActionType.ClearCache, this, "ClearCache");
		TNAction.regRunner(TNActionType.Upgrade, this, "Upgrade");
		TNAction.regRunner(TNActionType.UpdateSoftware, this, "UpdateSoftware");
		
		TNAction.regRunner(TNActionType.Pay, this, "Pay");
		TNAction.regRunner(TNActionType.FeedBack, this, "FeedBack");
	}
	
	public static TNUserService getInstance(){
		if (singleton == null){
			synchronized (TNUserService.class){
				if (singleton == null){
					singleton = new TNUserService();
				}
			}
		}
		
		return singleton;
	}
	
	//-------------------------------------------------------------------------------
	// TODO
	public void ForgotPassword(TNAction aAction) {
		JSONObject jsonData = TNUtils.makeJSON(
				"phone", aAction.inputs.get(0), 
				"password", aAction.inputs.get(1),
				"vcode", aAction.inputs.get(2));
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/user/password/reset", jsonData, TNActionType.ForgotPassword);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}

	//TODO
	public void FindPswByEmail(TNAction aAction) {
		JSONObject jsonData = TNUtils.makeJSON(
				"email", aAction.inputs.get(0), 
				"t", aAction.inputs.get(1));
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/verifycode/email", jsonData, TNActionType.FindPswByEmail);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}

	// TODO
	public void Register(TNAction aAction) {
		JSONObject jsonData = TNUtils.makeJSON(
				"phone", aAction.inputs.get(0), 
				"password", aAction.inputs.get(1),
				"vcode", aAction.inputs.get(2));
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/register", jsonData, TNActionType.Register);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			TNSettings settings = TNSettings.getInstance();
			settings.firstLaunch = true;
			settings.savePref(false);
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}

	//TODO
	public void VerifyCode(TNAction aAction) {
		JSONObject jsonData = null;
		if (aAction.inputs.size() > 2) {
			jsonData = TNUtils.makeJSON(
					"phone", aAction.inputs.get(0),
					"t", aAction.inputs.get(1),
					"answer", aAction.inputs.get(2),
					"nonce", aAction.inputs.get(3),
					"hashkey", aAction.inputs.get(4));
		} else {
			jsonData = TNUtils.makeJSON(
					"phone", aAction.inputs.get(0),
					"t", aAction.inputs.get(1));
		}

		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/verifycode", jsonData, TNActionType.VerifyCode);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}

	// todo
	public void Captcha(TNAction aAction) {
		aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/captcha", null, TNActionType.Captcha);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	//TODO
	public void Login(TNAction aAction) {		
		String username = aAction.inputs.get(0).toString();
		String password = aAction.inputs.get(1).toString();
		
		JSONObject jsonData = TNUtils.makeJSON(
				"username", username, 
				"password", password);
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/login", jsonData, TNActionType.Login);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	//TODO
	public void LoginThird(TNAction aAction) {		
		int btype  = (Integer) aAction.inputs.get(0);
		String bid = (String) aAction.inputs.get(1);
		long stamp = (Long) aAction.inputs.get(2);
		String sign = "bid=" + bid + "&btype=" + btype + "&stamp=" + stamp + "qingbiji";
		
		JSONObject jsonData = TNUtils.makeJSON(
				"btype", btype, 
				"bid", bid, 
				"stamp", stamp, 
				"sign", TNUtils.toMd5(sign).toLowerCase());
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/login/third", jsonData, TNActionType.LoginThird);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}

	//TODO
	public void LoginBind(TNAction aAction) {		
		int btype  = (Integer) aAction.inputs.get(0);
		String bid = (String) aAction.inputs.get(1);
		String name = (String) aAction.inputs.get(2);
		String access_token = (String) aAction.inputs.get(3);
		String refresh_token = (String) aAction.inputs.get(4);
		long stamp = (Long) aAction.inputs.get(5);
		String phone = (String) aAction.inputs.get(6);
		String vcode = (String) aAction.inputs.get(7);
		String sign = "access_token=" + access_token + "&bid=" + bid + "&btype=" + btype + "&name=" + name + "&phone=" + phone + "&refresh_token=" + refresh_token + "&stamp=" + stamp + "&vcode=" + vcode + "qingbiji";
		
		JSONObject jsonData = TNUtils.makeJSON(
				"btype", btype, 
				"bid", bid,
				"name", name,
				"access_token", access_token,
				"refresh_token", refresh_token,
				"stamp", stamp, 
				"phone", phone,
				"vcode", vcode,
				"sign", TNUtils.toMd5(sign).toLowerCase());
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/login/bind", jsonData, TNActionType.LoginBind);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			TNSettings settings = TNSettings.getInstance();
			settings.firstLaunch = true;
			settings.savePref(false);
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void Logout(TNAction aAction) {
		aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/logout", null, TNActionType.Logout);
		
		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		TNSettings settings = TNSettings.getInstance();
		settings.isLogout = true;
		settings.lockPattern = new LinkedList<Integer>();
		settings.userId = -1;
		settings.username = "";
		settings.phone = "";
		settings.email = "";
		settings.password = "";
		settings.savePref(true);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}

	//TODO
	public void Profile(TNAction aAction) {	
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/user/profile", null, TNActionType.Profile);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void GetAllData(TNAction aAction) {	
		aAction.runChildAction(TNActionType.GetTagList);
		aAction.runChildAction(TNActionType.GetAllFolders);
		aAction.runChildAction(TNActionType.GetNoteListByTrash, 1, TNConst.MAX_PAGE_SIZE, TNSettings.getInstance().sort);
		aAction.runChildAction(TNActionType.GetNoteList, 1, TNConst.MAX_PAGE_SIZE, TNSettings.getInstance().sort);
		aAction.finished();
	}
	
	public void GetAllDataByNoteId(TNAction aAction) {	
		long noteId = (Long) aAction.inputs.get(0);
		aAction.runChildAction(TNActionType.GetNoteByNoteId, noteId);
		TNNote note = TNDbUtils.getNoteByNoteId(noteId);
		for (TNNoteAtt att: note.atts) {
			aAction.runChildAction(TNActionType.SyncNoteAtt, att);
		}
		note = TNDbUtils.getNoteByNoteId(noteId);
		note.syncState = 2;
		if (note.attCounts > 0) {
			for(int i = 0; i < note.atts.size(); i++) {
				TNNoteAtt tempAtt = note.atts.get(i);
				if (i == 0 && tempAtt.type > 10000 && tempAtt.type < 20000) {
					TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, tempAtt.path, note.noteLocalId);
				}
				if (TextUtils.isEmpty(tempAtt.path) || "null".equals(tempAtt.path)) {
					note.syncState = 1;
				}
			}
		}
		TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, note.syncState, note.noteLocalId);
		aAction.finished();
	}

	//
	public void VerifyEmail(TNAction aAction) {	
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/user/verifyemail", null, TNActionType.VerifyEmail);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished();
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void ChangePassword(TNAction aAction) {	
		String newPassword = aAction.inputs.get(1).toString();
		JSONObject jsonData = TNUtils.makeJSON(
				"oldpassword", aAction.inputs.get(0), 
				"newpassword", newPassword);
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/user/password", jsonData, TNActionType.ChangePassword);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			TNSettings settings = TNSettings.getInstance();
			settings.password = newPassword;
			settings.savePref(false);
			TNDb.getInstance().execSQL(TNSQLString.USER_UPDATE_PWD, newPassword, settings.userId);
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}

	//TODO
	public void ChangePhone(TNAction aAction) {
		String phone = aAction.inputs.get(0).toString();
		JSONObject jsonData = TNUtils.makeJSON(
					"phone", phone,
					"vcode", aAction.inputs.get(1),
					"password", aAction.inputs.get(2));
			
		aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/user/profile", jsonData, TNActionType.ChangePhone);
	
		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			TNSettings settings = TNSettings.getInstance();
			settings.phone = phone;
			settings.savePref(false);
			TNDb.getInstance().execSQL(TNSQLString.USER_UPDATE_PHONE, phone, settings.userId);
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void ChangeUserNameOrEmail(TNAction aAction) {
		String value = aAction.inputs.get(0).toString();
		String type = aAction.inputs.get(1).toString();
		JSONObject jsonData = null;
		if ("userName".equals(type)) {
			jsonData = TNUtils.makeJSON(
					"username", value,
					"password", aAction.inputs.get(2));
		} else {
			jsonData = TNUtils.makeJSON(
					"email", value,
					"password", aAction.inputs.get(2));
		}
			
		aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/user/profile", jsonData, TNActionType.ChangeUserNameOrEmail);
	
		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			TNSettings settings = TNSettings.getInstance();
			if ("userName".equals(type)) {
				settings.username = value;
				TNDb.getInstance().execSQL(TNSQLString.USER_UPDATE_NAME, value, settings.userId);
			} else {
				settings.email = value;
				TNDb.getInstance().execSQL(TNSQLString.USER_UPDATE_EMAIL, value, settings.userId);
			}
			settings.savePref(false);
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void ClearCache(TNAction aAction) {
		TNUtilsAtt.deleteTempFiles();
		TNDbUtils.clearCache();
	}

	// TODO
	public void Upgrade(TNAction aAction) {	
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/app/upgrade", null, TNActionType.Upgrade);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		MLog.d("更新返回="+outputs.toString());
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			JSONObject data = (JSONObject) TNUtils.getFromJSON(outputs, "data");
			aAction.finished(data);
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void UpdateSoftware(TNAction aAction){
		String uriPath = (String) aAction.inputs.get(0);
		InputStream in = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(uriPath);
			HttpResponse response = client.execute(request);
			in = response.getEntity().getContent();
			File myTempFile = new File(TNUtilsAtt.getTempPath("ThinkerNote-Setup.apk"));
			if( !myTempFile.exists() ){
				myTempFile.getParentFile().mkdirs();
				myTempFile.createNewFile();
			}
			//Log.i(TAG, "myTempFile=" + myTempFile.getAbsolutePath());
			
			FileOutputStream fos = new FileOutputStream(myTempFile);
			byte buf[] = new byte[1024];
			int sum = 0;
			int numread;
			while( (numread = in.read(buf)) > 0){
				fos.write(buf, 0, numread);
				sum += numread;
				aAction.progressUpdate(sum);
			}
			fos.close();
			in.close();
			Log.i(TAG, "myTempFile=" + myTempFile.getAbsolutePath());
			aAction.finished(myTempFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			aAction.failed();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void Pay(TNAction aAction) {
		JSONObject jsonData = TNUtils.makeJSON(
				"amount", aAction.inputs.get(0), 
				"channel", aAction.inputs.get(1));
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/margin/deposit", jsonData, TNActionType.Pay);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void FeedBack(TNAction aAction) {
		String content = (String) aAction.inputs.get(0);
		String email = (String) aAction.inputs.get(2);
		@SuppressWarnings("unchecked")
		ArrayList<String> paths = (ArrayList<String>) aAction.inputs.get(1);
		
		long picId = -1L;
		if (paths.size() > 0) {
			File f = new File(paths.get(0));
			TNAction action = TNAction.runAction(TNActionType.Upload, f.getName(), paths.get(0));
			if (action.outputs.get(0) instanceof String) {
				return;
			}
			JSONObject output = (JSONObject) action.outputs.get(0);
			if ((Integer) TNUtils.getFromJSON(output, "code") == 0) {
				picId = (Long) TNUtils.getFromLongJSON(output, "id");
			}
		}
		
		JSONObject jsonData = TNUtils.makeJSON(
				"content", content, 
				"pic_id", picId,
				"email", email);
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/feedback", jsonData, TNActionType.FeedBack);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	
}
