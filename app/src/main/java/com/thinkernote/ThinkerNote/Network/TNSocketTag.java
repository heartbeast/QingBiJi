package com.thinkernote.ThinkerNote.Network;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.DBHelper.TagDbHelper;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
/**
 * handle_Tags
 */
public class TNSocketTag {
	private static final String TAG = "TNSocketTag";
	public static void handle_Tags(TNAction aAction){
		TNSettings settings = TNSettings.getInstance();
		TNActionType type = (TNActionType) aAction.inputs.get(3);
		if (type == TNActionType.GetTagList) {
			JSONObject outputs = (JSONObject)aAction.outputs.get(0);
			int result = (Integer)TNUtils.getFromJSON(outputs, "code");
			if (result == 0) {
				TagDbHelper.clearTags();
				JSONArray foldersObj = (JSONArray) TNUtils.getFromJSON(outputs, "tags");
				for (int i = 0; i < foldersObj.length(); i++) {
					JSONObject obj = (JSONObject) TNUtils.getFromJSON(foldersObj, i);
					String tagName = TNUtils.getFromJSON(obj, "name").toString();
					if (TextUtils.isEmpty(tagName)) {
						tagName = "无";
					}
					JSONObject tempObj = TNUtils.makeJSON(
							"tagName", tagName,
							"userId", settings.userId,
							"trash", 0,
							"tagId", TNUtils.getFromJSON(obj, "id"),
							"strIndex", TNUtils.getPingYinIndex(tagName),
							"count", TNUtils.getFromJSON(obj, "count")
							);
					TagDbHelper.addOrUpdateTag(tempObj);
				}
			}
		}
	}
}
