package com.thinkernote.ThinkerNote.Service;

import org.json.JSONObject;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNUtils;

public class TNTagService {
	private static final String TAG = "TNTagService";
			
	private static TNTagService singleton = null;

	private TNTagService(){
		Log.d(TAG,"TNTagService()");
		TNAction.regRunner(TNActionType.TagAdd, this, "TagAdd");
		TNAction.regRunner(TNActionType.TagDelete, this, "TagDelete");
		TNAction.regRunner(TNActionType.TagRename, this, "TagRename");
		TNAction.regRunner(TNActionType.GetTagList, this, "GetTagList");
	}
	
	public static TNTagService getInstance(){
		if (singleton == null){
			synchronized (TNTagService.class){
				if (singleton == null){
					singleton = new TNTagService();
				}
			}
		}
		
		return singleton;
	}
	
	public void TagAdd(TNAction aAction) {
		JSONObject jsonData = TNUtils.makeJSON(
				"name", aAction.inputs.get(0));
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/tags", jsonData, TNActionType.TagAdd);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void TagDelete(TNAction aAction) {
		Object tagId = aAction.inputs.get(0);
		JSONObject jsonData = TNUtils.makeJSON(
				"tag_id", aAction.inputs.get(0));
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "DELETE", "api/tags", jsonData, TNActionType.TagDelete);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			TNDb.getInstance().execSQL(TNSQLString.TAG_REAL_DELETE, tagId);
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void TagRename(TNAction aAction) {
		Object tagId = aAction.inputs.get(0);
		String name = (String) aAction.inputs.get(1);
		JSONObject jsonData = TNUtils.makeJSON(
				"tag_id", tagId,
				"name", name);
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/tags", jsonData, TNActionType.TagRename);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			TNDb.getInstance().execSQL(TNSQLString.TAG_RENAME, name, TNUtils.getPingYinIndex(name), tagId);
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
	
	public void GetTagList(TNAction aAction) {
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/tags", null, TNActionType.GetTagList);

		JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
		if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
			aAction.finished(outputs);
		} else {
			aAction.failed(outputs);
		}
	}
			
}
