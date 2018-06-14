package com.thinkernote.ThinkerNote.Service;

import java.io.File;
import java.util.Vector;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;

public class TNAttLocalService {
	private static final String TAG = "TNAttService";

	private static TNAttLocalService singleton = null;

	private TNAttLocalService(){
		Log.d(TAG,"TNAttService()");
		TNAction.regRunner(TNActionType.AttLocalSave, this, "AttLocalSave");
		TNAction.regRunner(TNActionType.AttLocalDelete, this, "AttLocalDelete");
	}
	
	public static TNAttLocalService getInstance(){
		if (singleton == null){
			synchronized (TNAttLocalService.class){
				if (singleton == null){
					singleton = new TNAttLocalService();
				}
			}
		}
		
		return singleton;
	}
	
	public void AttLocalSave(TNAction aAction){
		TNNote note = (TNNote)aAction.inputs.get(0);
		Vector<TNNoteAtt> newAtts = note.atts;
		Vector<TNNoteAtt> exsitAtts = TNDbUtils.getAttrsByNoteLocalId(note.noteLocalId);
		try{
			if (exsitAtts.size() != 0) {
				//循环判断是否与本地同步，新增没有就删除本地
				for (int k = 0; k < exsitAtts.size(); k++) {
					boolean exsit = false;
					TNNoteAtt tempLocalAtt = exsitAtts.get(k);
					for (int i = 0; i < newAtts.size(); i++) {
						long attLocalId = newAtts.get(i).attLocalId;
						if (tempLocalAtt.attLocalId == attLocalId) {
							exsit = true;
						}
					}
					if (!exsit) {
						NoteAttrDbHelper.deleteAttByAttLocalId(tempLocalAtt.attLocalId);
					}
				}
				//循环判断是否与新增同步，本地没有就插入数据
				exsitAtts = TNDbUtils.getAttrsByNoteLocalId(note.noteLocalId);
				for (int k = 0; k < newAtts.size(); k++) {
					TNNoteAtt att = newAtts.get(k);
					if (att.attLocalId == -1) {
						TNAction act = TNAction.runAction(
								TNActionType.Db_Execute,
								TNSQLString.ATT_INSERT,
								att.attName,
								att.type,
								att.path,
								note.noteLocalId,
								att.size,
								0,
								TNUtilsAtt.fileToMd5(att.path),
								att.attId,
								att.width,
								att.height
								);
						long attLocalId = (Long)act.outputs.get(0);

						// copy file to path
						String tPath = TNUtilsAtt.getAttPath(attLocalId, att.type);
						if( tPath == null){
							aAction.failed("存储空间不足");
							return;
						}

						//tPath = tPath + TNUtilsAtt.getAttSuffix(att.type);
						TNUtilsAtt.copyFile(att.path, tPath);
						TNUtilsAtt.recursionDeleteDir(new File(att.path));
						Log.d(TAG, att.path + " >> " + tPath + "(" + att.digest + ")");

						// update db
						TNAction.runAction(TNActionType.Db_Execute,
								TNSQLString.ATT_PATH,
								tPath,
								attLocalId
						);

						note.atts.get(k).attLocalId = attLocalId;
					}
				}
			} else {
				for(int i = 0; i < note.atts.size(); i++){
					TNNoteAtt att = note.atts.get(i);
					// insert
					TNAction act = TNAction.runAction(
						TNActionType.Db_Execute,
						TNSQLString.ATT_INSERT,
						att.attName,
						att.type,
						att.path,
						note.noteLocalId,
						att.size,
						3,
						TNUtilsAtt.fileToMd5(att.path),
						att.attId,
						att.width,
						att.height
						);
					long attLocalId = (Long)act.outputs.get(0);

					note.atts.get(i).attLocalId = attLocalId;
				}
			}
			
			//如果笔记的第一个附件是图片，则设置笔记的缩略图
			Vector<TNNoteAtt> noteAttrs = TNDbUtils.getAttrsByNoteLocalId(note.noteLocalId);
			if (noteAttrs.size() > 0) {
				TNNoteAtt temp = noteAttrs.get(0);
				if (temp.type > 10000 && temp.type < 20000) {
					TNAction.runAction(TNActionType.Db_Execute,
							TNSQLString.NOTE_UPDATE_THUMBNAIL,
							temp.path,
							note.noteLocalId
							);
				}
			}
			
			aAction.finished(note);

		}catch (Exception e) {
			e.printStackTrace();
			aAction.failed("未知错误");
		}
	}
	
	public void AttLocalDelete(TNAction aAction) {
		long attLocalId = (Long) aAction.inputs.get(0);
		TNDb.beginTransaction();
		try{
			TNAction.runAction(TNActionType.Db_Execute, 
					TNSQLString.ATT_DELETE_ATT_ID,
					attLocalId);
			
			TNDb.setTransactionSuccessful();
		} finally {
			TNDb.endTransaction();
		}
		
		aAction.finished();
	}
	
	public void AttLocalDeleteByNoteId(TNAction aAction) {
		long noteLocalId = (Long) aAction.inputs.get(0);
		TNDb.beginTransaction();
		try{
			TNAction.runAction(TNActionType.Db_Execute, 
					TNSQLString.ATT_DELETE_NOTE,
					noteLocalId);
			
			TNDb.setTransactionSuccessful();
		} finally {
			TNDb.endTransaction();
		}
		
		aAction.finished();
	}
	

}
