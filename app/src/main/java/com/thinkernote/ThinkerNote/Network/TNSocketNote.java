package com.thinkernote.ThinkerNote.Network;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;

import android.text.TextUtils;
import android.util.Log;

public class TNSocketNote {
	private static final String TAG = "TNSocketNote";
	public static void handle_Notes(TNAction aAction){
		TNActionType type = (TNActionType)aAction.inputs.get(3);
		JSONObject outputs = (JSONObject)aAction.outputs.get(0);
		int result = (Integer)TNUtils.getFromJSON(outputs, "code");
		if (result == 0) {
			if (type == TNActionType.GetNoteList || type == TNActionType.GetNoteListByFolderId || type == TNActionType.GetNoteListByTagId) {
				insertDbNotes(outputs, false);
			} else if (type == TNActionType.GetNoteByNoteId) {
				JSONObject obj = (JSONObject) TNUtils.getFromJSON(outputs, "note");
				updateNote(obj);
			}
		}
		
	}
	
	public static void handle_TrashNotes(TNAction aAction) {
		JSONObject outputs = (JSONObject)aAction.outputs.get(0);
		int result = (Integer)TNUtils.getFromJSON(outputs, "code");
		if (result == 0) {
			insertDbNotes(outputs, true);
		}
	}
	
	public static void insertDbNotes(JSONObject outputs, boolean isTrash) {
		JSONArray notesObj = (JSONArray) TNUtils.getFromJSON(outputs, "notes");
		int trash = isTrash ? 2 : 0;
		for (int i = 0; i < notesObj.length(); i++) {
			JSONObject obj = (JSONObject) TNUtils.getFromJSON(notesObj, i);
			long noteId = Long.valueOf(TNUtils.getFromJSON(obj, "id").toString());
			long lastUpdate = TNUtils.formatStringToTime(TNUtils.getFromJSON(obj, "update_at").toString()) / 1000;

			JSONArray tags = (JSONArray) TNUtils.getFromJSON(obj, "tags");
			String tagStr = "";
			for (int k = 0; k < tags.length(); k++) {
				JSONObject tempTag = (JSONObject) TNUtils.getFromJSON(tags, k);
				String tag = TNUtils.getFromJSON(tempTag, "name").toString();
				if ("".equals(tag)) {
					continue;
				}
				if (tags.length() == 1) {
					tagStr = tag;
				} else {
					if (k == (tags.length() - 1)) {
						tagStr = tagStr + tag;
					} else {
						tagStr = tagStr + tag + ",";
					}
				}
			}

			int catId = -1;
			if (!obj.isNull("folder_id")) {
				catId = (Integer) TNUtils.getFromJSON(obj, "folder_id");
			}
			int syncState = 1;
			TNNote note = TNDbUtils.getNoteByNoteId(noteId);
			if (note != null) {
				if (note.lastUpdate > lastUpdate) {
					continue;
				} else {
					syncState = note.syncState;
				}
			}
			JSONObject tempObj = TNUtils.makeJSON(
					"title", TNUtils.getFromJSON(obj, "title"),
					"userId", TNSettings.getInstance().userId,
					"trash", trash,
					"source", "android",
					"catId", catId,
					"content", TNUtils.getFromJSON(obj, "summary").toString(),
					"createTime", TNUtils.formatStringToTime(TNUtils.getFromJSON(obj, "create_at").toString()) / 1000,
					"lastUpdate", lastUpdate,
					"syncState", syncState,
					"noteId", noteId,
					"shortContent", TNUtils.getFromJSON(obj, "summary").toString(),
					"tagStr", tagStr,
					"lbsLongitude", 0,
					"lbsLatitude", 0,
					"lbsRadius", 0,
					"lbsAddress", "",
					"nickName", TNSettings.getInstance().username,
					"thumbnail", "",
					"contentDigest", TNUtils.getFromJSON(obj, "content_digest").toString()
			);
			NoteDbHelper.addOrUpdateNote(tempObj);
		}
	}
	
	public static void updateNote(JSONObject obj) {
		long noteId = Long.valueOf(TNUtils.getFromJSON(obj, "id").toString());
		String contentDigest = TNUtils.getFromJSON(obj, "content_digest").toString();
		TNNote note = TNDbUtils.getNoteByNoteId(noteId);//在全部笔记页同步，会走这里，没在首页同步过的返回为null

		int syncState = note == null ? 1 : note.syncState;
		
		JSONArray tags = (JSONArray) TNUtils.getFromJSON(obj, "tags");
		String tagStr = "";
		for (int k = 0; k < tags.length(); k++) {
			JSONObject tempTag = (JSONObject) TNUtils.getFromJSON(tags, k);
			String tag = TNUtils.getFromJSON(tempTag, "name").toString();
			if ("".equals(tag)) {
				continue;
			}
			if (tags.length() == 1) {
				tagStr = tag;					
			} else {
				if (k == (tags.length()-1)) {
					tagStr = tagStr + tag;
				} else {
					tagStr = tagStr + tag + ",";
				}
			}
		}

		String thumbnail = "";
		if (note != null) {
			thumbnail = note.thumbnail;
			Vector<TNNoteAtt> localAtts = TNDbUtils.getAttrsByNoteLocalId(note.noteLocalId);
			JSONArray atts = (JSONArray) TNUtils.getFromJSON(obj, "attachments");
			if (localAtts.size() != 0) {
				//循环判断是否与线上同步，线上没有就删除本地
				for (int k = 0; k < localAtts.size(); k++) {
					boolean exit = false;
					TNNoteAtt tempLocalAtt = localAtts.get(k);
					for (int i = 0; i < atts.length(); i++) {
						JSONObject tempAtt = (JSONObject) TNUtils.getFromJSON(atts, i);
						long attId = Long.valueOf(TNUtils.getFromJSON(tempAtt, "id").toString());
						if (tempLocalAtt.attId == attId) {
							exit = true;
						}
					}
					if (!exit) {
						if (thumbnail.indexOf(String.valueOf(tempLocalAtt.attId)) != 0) {
							thumbnail = "";
						}
						NoteAttrDbHelper.deleteAttById(tempLocalAtt.attId);
					}
				}
				//循环判断是否与线上同步，本地没有就插入数据
				for (int k = 0; k < atts.length(); k++) {
					JSONObject tempAtt = (JSONObject) TNUtils.getFromJSON(atts, k);
					long attId = Long.valueOf(TNUtils.getFromJSON(tempAtt, "id").toString());
					boolean exit = false;
					for (int i = 0; i < localAtts.size(); i++) {
						TNNoteAtt tempLocalAtt = localAtts.get(i);
						if (tempLocalAtt.attId == attId) {
							exit = true;
						}
					}
					if (!exit) {
						syncState = 1;
						insertAttr(tempAtt, note.noteLocalId);
					}
				}
			} else {
				for (int i = 0; i < atts.length(); i++) {
					JSONObject tempAtt = (JSONObject) TNUtils.getFromJSON(atts, i);
					syncState = 1;
					insertAttr(tempAtt, note.noteLocalId);
				}
			}

			//如果本地的更新时间晚就以本地的为准
			if (note.lastUpdate > TNUtils.formatStringToTime(TNUtils.getFromJSON(obj, "update_at").toString()) / 1000) {
				return;
			}

			if (atts.length() == 0) {
				syncState = 2;
			}
		}
		
		int catId = -1;
		if (!obj.isNull("folder_id")) {
			catId = (Integer) TNUtils.getFromJSON(obj, "folder_id");
		}
		
		JSONObject tempObj = TNUtils.makeJSON(
				"title", TNUtils.getFromJSON(obj, "title"),
				"userId", TNSettings.getInstance().userId,
				"trash", TNUtils.getFromJSON(obj, "trash"),
				"source", "android",
				"catId", catId,
				"content", TNUtilsHtml.codeHtmlContent(TNUtils.getFromJSON(obj, "content").toString(), true),
				"createTime", TNUtils.formatStringToTime(TNUtils.getFromJSON(obj, "create_at").toString())/1000,
				"lastUpdate",  TNUtils.formatStringToTime(TNUtils.getFromJSON(obj, "update_at").toString())/1000,
				"syncState", syncState,
				"noteId", noteId,
				"shortContent", TNUtils.getBriefContent(TNUtils.getFromJSON(obj, "content").toString()),
				"tagStr", tagStr,
				"lbsLongitude", obj.isNull("longitude") ? 0 : TNUtils.getFromJSON(obj, "longitude"),
				"lbsLatitude", obj.isNull("latitude") ? 0 : TNUtils.getFromJSON(obj, "latitude"),
				"lbsRadius", obj.isNull("radius") ? 0 : TNUtils.getFromJSON(obj, "radius"),
				"lbsAddress", TNUtils.getFromJSON(obj, "address"),
				"nickName", TNSettings.getInstance().username,
				"thumbnail", thumbnail,
				"contentDigest", contentDigest
				);
		if (note == null)
			NoteDbHelper.addOrUpdateNote(tempObj);
		else
			NoteDbHelper.updateNote(tempObj);
	}
	
	public static void insertAttr(JSONObject obj, long noteLocalId) {
		long attId = Long.valueOf(TNUtils.getFromJSON(obj, "id").toString());
		String digest = (String) TNUtils.getFromJSON(obj, "digest");
		TNNoteAtt noteAtt = TNDbUtils.getAttrById(attId);
		noteAtt.attName = (String) TNUtils.getFromJSON(obj, "name");
		noteAtt.type = (Integer) TNUtils.getFromJSON(obj, "type");
		noteAtt.size = (Integer) TNUtils.getFromJSON(obj, "size");
		noteAtt.syncState = 1;

		JSONObject tempObj = TNUtils.makeJSON(
				"attName", noteAtt.attName,
				"type", noteAtt.type,
				"path", noteAtt.path,
				"noteLocalId", noteLocalId,
				"size", noteAtt.size,
				"syncState", noteAtt.syncState,
				"digest", digest,
				"attId", attId,
				"width", noteAtt.width,
				"height", noteAtt.height
				);
		NoteAttrDbHelper.addOrUpdateAttr(tempObj);
	}
}
