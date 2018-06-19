package com.thinkernote.ThinkerNote.OAuth2;

import android.content.Context;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNHttpUtils;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Network.TNSocketCat;
import com.thinkernote.ThinkerNote.Network.TNSocketNote;
import com.thinkernote.ThinkerNote.Network.TNSocketTag;
import com.thinkernote.ThinkerNote.Network.TNSocketUser;
import com.thinkernote.ThinkerNote.OAuth2.TNHttpHelper.RestHttpException;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *
 */
public class TNOAuth2 {
	private static final String TAG = "TNOAuth2";
	public static String TNOAUTH_ACTION_UTL_BASE = "http://s2.thinkernote.com/open/";

	private Hashtable<TNActionType, TNAction.TNRunner> handleMap;

	private static TNOAuth2 singleton = null;
	private TNHttpHelper httpHelper;

	private TNOAuth2() {
		MLog.d(TAG, "TNAttService()");
		TNAction.regRunner(TNActionType.TNOpenUrl, this, "TNOpenUrl");

		handleMap = new Hashtable<TNActionType, TNAction.TNRunner>();
		handleMap.put(TNActionType.Login, new TNRunner(TNSocketUser.class, "handle_Login", TNAction.class));
		handleMap.put(TNActionType.LoginThird, new TNRunner(TNSocketUser.class, "handle_LoginThird", TNAction.class));
		handleMap.put(TNActionType.Profile, new TNRunner(TNSocketUser.class, "handle_Profile", TNAction.class));
		
		handleMap.put(TNActionType.GetParentFolders, new TNRunner(TNSocketCat.class, "handle_Folders", TNAction.class));
		handleMap.put(TNActionType.GetFoldersByFolderId, new TNRunner(TNSocketCat.class, "handle_Folders", TNAction.class));
		
		handleMap.put(TNActionType.GetTagList, new TNRunner(TNSocketTag.class, "handle_Tags", TNAction.class));
		handleMap.put(TNActionType.GetNoteListByTrash, new TNRunner(TNSocketNote.class, "handle_TrashNotes", TNAction.class));
		handleMap.put(TNActionType.GetNoteList, new TNRunner(TNSocketNote.class, "handle_Notes", TNAction.class));
		handleMap.put(TNActionType.GetNoteListByFolderId, new TNRunner(TNSocketNote.class, "handle_Notes", TNAction.class));
		handleMap.put(TNActionType.GetNoteByNoteId, new TNRunner(TNSocketNote.class, "handle_Notes", TNAction.class));
		handleMap.put(TNActionType.GetTrashNoteByNoteId, new TNRunner(TNSocketNote.class, "handle_Notes", TNAction.class));
		handleMap.put(TNActionType.GetNoteListByTagId, new TNRunner(TNSocketNote.class, "handle_Notes", TNAction.class));
	}

	public static TNOAuth2 getInstance() {
		if (singleton == null) {
			synchronized (TNOAuth2.class) {
				if (singleton == null) {
					singleton = new TNOAuth2();
				}
			}
		}

		return singleton;
	}

	public static void useTestServer(boolean isTest) {
		if (isTest) {
			TNOAUTH_ACTION_UTL_BASE = "http://new.qingbiji.cn:8088/";
		} else {
			TNOAUTH_ACTION_UTL_BASE = "http://new.qingbiji.cn/";
//			TNOAUTH_ACTION_UTL_BASE = "https://s.qingbiji.cn/";
		}
	}

	public void TNOpenUrl(TNAction aAction) {
		Context context = TNSettings.getInstance().appContext;
		String method = (String) aAction.inputs.get(0);
		String cmd = (String) aAction.inputs.get(1);
		String url = TNOAUTH_ACTION_UTL_BASE + cmd;
		JSONObject json = null;
		String outPath = null;
		if (aAction.inputs.size() > 2) {
			if(cmd.startsWith("attachment/"))
				outPath = (String) aAction.inputs.get(2);
			else
				json = (JSONObject) aAction.inputs.get(2);
		}
		TNActionType type = null;
		if (aAction.inputs.size() > 3) {
			type = (TNActionType) aAction.inputs.get(3);
		}

		String errorCode = null;
		int count = getLoopCount(cmd);
		while (count > 0) {
			count--;

			json = addUserParams(cmd, json);
			TNHttpEntity tnEntity = null;
			try {
				tnEntity = tryOpenUrl(context, method, url, json);
			} catch (Exception e) {
				e.printStackTrace();
				errorCode = context.getResources().getString(R.string.alert_Net_Unreachable);
				continue;
			}
			
			Object result = null;
			if(cmd.startsWith("attachment/")){
				try {
					result = TNHttpUtils.getEntityToFile(tnEntity, outPath);
				} catch (RestHttpException e1) {
					JSONObject data = null;
					try {
						data = new JSONObject(e1.responseBody);
					} catch (JSONException e) {
						errorCode = context.getResources().getString(R.string.alert_Net_Unreachable);
						break;
					}
					errorCode = handleError(context, data);
				} catch (Exception e1) {
					e1.printStackTrace();
					errorCode = context.getResources().getString(R.string.alert_Net_Unreachable);
					break;
				}
			}else
				result = TNHttpUtils.getEntityContent(tnEntity);

			MLog.d(TAG, "<<<rcv:" + result);

			if (result != null) {
				if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE") || method.equals("UPLOAD") || (method.equals("GET")&&!cmd.startsWith("attachment/"))) {
					try {
						JSONObject data = new JSONObject((String) result);
						errorCode = handleError(context, data);
						if (errorCode != null) {
							continue;
						}
						aAction.finished(data);
					} catch (JSONException e) {
						e.printStackTrace();
						errorCode = context.getResources().getString(R.string.alert_Net_Unreachable);
						continue;
					}
				} else if(method.equals("GET") && cmd.startsWith("attachment/")) {
					aAction.finished(result, tnEntity.getHeadValue());
				} else {
					aAction.finished(result);
				}
				TNRunner runner = handleMap.get(type);
				if (runner != null) {
					runner.run(aAction);
				}
				return;
			} else {
				errorCode = context.getResources().getString(R.string.alert_Net_Unreachable);
				continue;
			}
		}
		aAction.failed(errorCode);
	}
	
	private int getLoopCount(String cmd){
		if(cmd.equals("reportInvalidUrl")){
			return 3;
		}
		return 1;
	}

	private JSONObject addUserParams(String cmd, JSONObject params) {
		if (cmd.equals("api/login") || cmd.equals("api/register")) {
			return params;
		} else {
			if(params == null){
				params = new JSONObject();
			}
			
			TNSettings settings = TNSettings.getInstance();
			try {
				params.put("session_token", settings.token);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return params;
		}
	}

	private String handleError(Context context, JSONObject data) {
		try {
			int resultCode = Integer.valueOf(data.get("code").toString());
			if (resultCode != 0) {
				String errorCode = data.get("msg").toString();
				return errorCode;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return context.getResources().getString(R.string.alert_Net_UnknownError);
		} catch (Exception e) {
			e.printStackTrace();
			return context.getResources().getString(R.string.alert_Net_UnknownError);
		}
		return null;
	}

	public TNHttpEntity tryOpenUrl(Context context, String method, String host,
			JSONObject jsonData) throws Exception {
		MLog.d(TAG, ">>>send:" + jsonData);
		TNHttpEntity entity = null;
		httpHelper = new TNHttpHelper(TNSettings.getInstance().appContext);
		if (method.equals("GET")) {
			String url = host;
			Vector<Header> headers = null;
			if(jsonData != null){
				if(url.equals("http://api.cloud.189.cn/getUserInfo.action")){
					headers = new Vector<Header>();
					Iterator<?> it = jsonData.keys();
					while (it.hasNext()) {
						String key = (String) it.next().toString();
						String value = "";
						try {
							value = jsonData.getString(key);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						MLog.e(TAG, "add header: key" + key + " value=" + value);
						headers.add(new BasicHeader(key, value));
					}
				}else{
					url = TNHttpUtils.makeUrl(host, jsonData);
				}
			}
			
			entity = httpHelper.doGet(url, headers);
		} else if (method.equals("POST")) {
			List<NameValuePair> pairs = TNHttpUtils
					.convertToNameValuePair(jsonData);
			entity = httpHelper.doPost(host, pairs);
		} else if (method.equals("MULTIPART")) {
			Object imageData = jsonData.get("imageData");
			String key = jsonData.getString("imageKey");
			jsonData.remove("imageData");
			jsonData.remove("imageKey");

			List<NameValuePair> pairs = TNHttpUtils
					.convertToNameValuePair(jsonData);
			entity = httpHelper.doPostMultipart(host, pairs, imageData, key);
		} else if (method.equals("PUT")) {
			List<NameValuePair> pairs = TNHttpUtils
					.convertToNameValuePair(jsonData);
			entity = httpHelper.doPut(host, pairs);
		} else if (method.equals("DELETE")) {
			List<NameValuePair> pairs = TNHttpUtils
					.convertToNameValuePair(jsonData);
			entity = httpHelper.doDelete(host, pairs);
		}  else if(method.equals("UPLOAD")){
			String path = (String) TNUtils.getFromJSON(jsonData, "path");
			jsonData.remove("path");
			host = TNHttpUtils.makeUrl(host, jsonData);
			TNUtils.putToJson(jsonData, "path", path);
			List<NameValuePair> pairs = TNHttpUtils
					.convertToNameValuePair(jsonData);
			entity = httpHelper.doUploadFile(host, path, pairs);
		} 

		return entity;
	}

}
