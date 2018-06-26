package com.thinkernote.ThinkerNote.Network;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.DBHelper.CatDbHelper;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;

/**
 * handle_Folders
 *
 */
public class TNSocketCat {
	private static final String TAG = "TNSocketCat";

	//TODO
	public static void handle_Folders(TNAction aAction){
		TNSettings settings = TNSettings.getInstance();
		TNActionType type = (TNActionType)aAction.inputs.get(3);
		
			JSONObject inputs = (JSONObject)aAction.inputs.get(2);
			JSONObject outputs = (JSONObject)aAction.outputs.get(0);
			int result = (Integer)TNUtils.getFromJSON(outputs, "code");
			if (type == TNActionType.GetParentFolders) {//GetParentFolders
				if (result == 0) {
					insertDBCats(outputs, -1);
				}

				//该处不会执行
				if (settings.firstLaunch) {
					Vector<TNCat> cats = TNDbUtils.getAllCatList(settings.userId);
					TNCat tempCat = null;
					for (int i = 0; i < cats.size(); i++) {
						tempCat = cats.get(i);
						if (TNConst.GROUP_WORK.equals(tempCat.catName)) {
							aAction.runChildAction(TNActionType.FolderAdd, tempCat.catId, TNConst.FOLDER_WORK_NOTE);
							aAction.runChildAction(TNActionType.FolderAdd, tempCat.catId, TNConst.FOLDER_WORK_UNFINISHED);
							aAction.runChildAction(TNActionType.FolderAdd, tempCat.catId, TNConst.FOLDER_WORK_FINISHED);
						}
						if (TNConst.GROUP_LIFE.equals(tempCat.catName)) {
							aAction.runChildAction(TNActionType.FolderAdd, tempCat.catId, TNConst.FOLDER_LIFE_DIARY);
							aAction.runChildAction(TNActionType.FolderAdd, tempCat.catId, TNConst.FOLDER_LIFE_KNOWLEDGE);
							aAction.runChildAction(TNActionType.FolderAdd, tempCat.catId, TNConst.FOLDER_LIFE_PHOTO);
						}
						if (TNConst.GROUP_FUN.equals(tempCat.catName)) {
							aAction.runChildAction(TNActionType.FolderAdd, tempCat.catId, TNConst.FOLDER_FUN_TRAVEL);
							aAction.runChildAction(TNActionType.FolderAdd, tempCat.catId, TNConst.FOLDER_FUN_MOVIE);
							aAction.runChildAction(TNActionType.FolderAdd, tempCat.catId, TNConst.FOLDER_FUN_GAME);
						}
						
					}
				}
			} else if (type == TNActionType.GetFoldersByFolderId) {
				//获取文件夹下的文件夹列表
				long pCatId = Long.valueOf(TNUtils.getFromJSON(inputs, "folder_id").toString());
				if (result == 0) {
					insertDBCats(outputs, pCatId);
				}
				
			}
	}
	
	public static void insertDBCats (JSONObject outputs, long pCatId) {
		TNSettings settings = TNSettings.getInstance();
		CatDbHelper.clearCatsByParentId(pCatId);
		JSONArray foldersObj = (JSONArray) TNUtils.getFromJSON(outputs, "folders");
		for (int i = 0; i < foldersObj.length(); i++) {
			JSONObject obj = (JSONObject) TNUtils.getFromJSON(foldersObj, i);
			JSONObject tempObj = TNUtils.makeJSON(
					"catName", TNUtils.getFromJSON(obj, "name"),
					"userId", settings.userId,
					"trash", 0,
					"catId", TNUtils.getFromJSON(obj, "id"),
					"noteCounts", TNUtils.getFromJSON(obj, "count"),
					"catCounts", TNUtils.getFromJSON(obj, "folder_count"),
					"deep", Integer.valueOf(TNUtils.getFromJSON(obj, "folder_count").toString()) > 0 ? 1: 0,
					"pCatId", pCatId,
					"isNew", -1,
					"createTime", TNUtils.formatStringToTime(TNUtils.getFromJSON(obj, "create_at").toString()),
					"lastUpdateTime", TNUtils.formatStringToTime(TNUtils.getFromJSON(obj, "update_at").toString()),
					"strIndex", TNUtils.getPingYinIndex(TNUtils.getFromJSON(obj, "name").toString())
					);
			CatDbHelper.addOrUpdateCat(tempObj);
		}
	}
}
