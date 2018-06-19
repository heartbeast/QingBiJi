package com.thinkernote.ThinkerNote.Service;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Vector;

public class TNSyncService {
	private static final String TAG = "TNAttService";

	private static volatile TNSyncService singleton = null;

	private TNSyncService(){
		MLog.d(TAG,"TNAttService()");
		TNAction.regRunner(TNActionType.Synchronize, this, "Synchronize");
		TNAction.regRunner(TNActionType.SynchronizeEdit, this, "SynchronizeEdit");
		TNAction.regRunner(TNActionType.SynchronizeCat, this, "SynchronizeCat");
		TNAction.regRunner(TNActionType.SynchronizeTrash, this, "SynchronizeTrash");
	}
	
	public static TNSyncService getInstance(){
		if (singleton == null){
			synchronized (TNSyncService.class){
				if (singleton == null){
					singleton = new TNSyncService();
				}
			}
		}
		
		return singleton;
	}
	
	public void Synchronize(TNAction aAction) {
		TNSettings settings = TNSettings.getInstance();

		if (settings.firstLaunch) {
			TNAction.runAction(TNActionType.FolderAdd, -1L, TNConst.FOLDER_DEFAULT);
			TNAction.runAction(TNActionType.FolderAdd, -1L, TNConst.FOLDER_MEMO);
			TNAction.runAction(TNActionType.FolderAdd, -1L, TNConst.GROUP_FUN);
			TNAction.runAction(TNActionType.FolderAdd, -1L, TNConst.GROUP_WORK);
			TNAction.runAction(TNActionType.FolderAdd, -1L, TNConst.GROUP_LIFE);
			TNAction.runAction(TNActionType.TagAdd, TNConst.TAG_IMPORTANT);
			TNAction.runAction(TNActionType.TagAdd, TNConst.TAG_TODO);
			TNAction.runAction(TNActionType.TagAdd, TNConst.TAG_GOODSOFT);
			settings.firstLaunch = false;
			settings.savePref(false);
		}

		if (!settings.syncOldDb) {
			//add老数据库的笔记
			Vector<TNNote> addOldNotes = TNDbUtils.getOldDbNotesByUserId(TNSettings.getInstance().userId);
			for (int i = 0; i < addOldNotes.size(); i++) {
				TNAction.runAction(TNActionType.NoteAdd, addOldNotes.get(i), false);
			}
			settings.syncOldDb = true;
			settings.savePref(false);
		}

		TNAction.runAction(TNActionType.Profile);
		Vector<TNCat> cats = TNDbUtils.getAllCatList(settings.userId);
		if (aAction.inputs.get(0) == "home" || aAction.inputs.get(0) == "Folder"  || cats.size() == 0) {
			TNAction.runAction(TNActionType.GetAllFolders);
		}

		Vector<TNTag> tags = TNDbUtils.getTagList(settings.userId);
		if (aAction.inputs.get(0) == "home" || aAction.inputs.get(0) == "Tags" || tags.size() == 0) {
			TNAction.runAction(TNActionType.GetTagList);
		}

		//add
		Vector<TNNote> addNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 3);
		for (int i = 0; i < addNotes.size(); i++) {
			TNAction.runAction(TNActionType.NoteAdd, addNotes.get(i), true);
		}

		//recovery
		Vector<TNNote> recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
		for (int i = 0; i < recoveryNotes.size(); i++) {
			if (recoveryNotes.get(i).noteId != -1) {
				TNAction.runAction(TNActionType.NoteRecovery, recoveryNotes.get(i).noteId);
			} else {
				TNAction.runAction(TNActionType.NoteAdd, recoveryNotes.get(i), true);
			}
		}

		//delete
		Vector<TNNote> deleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
		for (int i = 0; i < deleteNotes.size(); i++) {
			if (deleteNotes.get(i).noteId != -1) {
				TNAction.runAction(TNActionType.NoteDelete, deleteNotes.get(i).noteId);
			} else {
				TNAction.runAction(TNActionType.NoteLocalDelete, deleteNotes.get(i).noteLocalId);
			}
		}

		//realDelete
		Vector<TNNote> deleteRealNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
		for (int i = 0; i < deleteRealNotes.size(); i++) {
			if (deleteRealNotes.get(i).noteId == -1) {
				TNAction.runAction(TNActionType.Db_Execute,
						TNSQLString.NOTE_DELETE_BY_NOTELOCALID,
						deleteRealNotes.get(i).noteLocalId);
			} else {
				TNAction.runAction(TNActionType.NoteDelete, deleteRealNotes.get(i).noteId);
				TNAction.runAction(TNActionType.NoteRealDelete, deleteRealNotes.get(i).noteId);
			}
		}

		TNAction actionPage = TNAction.runAction(TNActionType.GetAllNoteIds);
		JSONArray cloudIds = new JSONArray();
		if (!(actionPage.outputs.get(0) instanceof String)) {
			JSONObject outputPage = (JSONObject) actionPage.outputs.get(0);
			if ((Integer) TNUtils.getFromJSON(outputPage, "code") == 0) {
				cloudIds = (JSONArray) TNUtils.getFromJSON(outputPage, "note_ids");
			}

			//与云端同步数据
			Vector<TNNote> allNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
			try {
				for (int i = 0; i < allNotes.size(); i++) {
					boolean isExit = false;
					TNNote note = allNotes.get(i);
					for (int j = 0; j < cloudIds.length(); j++) {
						if (note.noteId == Long.valueOf( TNUtils.getFromJSON((JSONObject)cloudIds.get(j), "id").toString())) {
							isExit = true;
							break;
						}
					}
					if (!isExit && note.syncState != 7) {
						TNAction.runAction(TNActionType.Db_Execute,
								TNSQLString.NOTE_DELETE_BY_NOTEID,
								note.noteId);
					}
				}

				//edit  通过最后更新时间来与云端比较是否该上传本地编辑的笔记
				Vector<TNNote> editNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 4);
				for (int i = 0; i < cloudIds.length(); i++) {
					long id = Long.valueOf( TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "id").toString());
					int lastUpdate =  Integer.valueOf(TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "update_at").toString());
					for (int j = 0; j < editNotes.size(); j++) {
						if (id == editNotes.get(j).noteId) {
							if (editNotes.get(j).lastUpdate > lastUpdate) {
								TNAction.runAction(TNActionType.NoteEdit, editNotes.get(j));
							} else {
								TNAction.runAction(TNActionType.Db_Execute,
										TNSQLString.NOTE_UPDATE_SYNCSTATE,
										1,
										editNotes.get(j).noteLocalId);
							}
						}
					}
				}

				//更新云端的笔记
				for (int i = 0; i < cloudIds.length(); i++) {
					boolean isExit = false;
					long id = Long.valueOf( TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "id").toString());
					int lastUpdate =  Integer.valueOf(TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "update_at").toString());
					for (int j = 0; j < allNotes.size(); j++) {
						TNNote note = allNotes.get(j);
						if (id == note.noteId) {
							isExit = true;
							if (lastUpdate > note.lastUpdate) {
								TNAction.runAction(TNActionType.GetNoteByNoteId, id);
							}
							break;
						}
					}
					if (!isExit) {
						TNAction.runAction(TNActionType.GetNoteByNoteId, id);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				MLog.e("Synchronize", "==================================================Synchronize");
			}

			//同步回收站的笔记
			Vector<TNNote> trashNotes = TNDbUtils.getNoteListByTrash(settings.userId, TNConst.CREATETIME);
			TNAction actionTrash = TNAction.runAction(TNActionType.GetAllTrashNoteIds);
			JSONArray trashNoteArr = new JSONArray();
			if (!(actionTrash.outputs.get(0) instanceof String)) {
				JSONObject outputTrash = (JSONObject) actionTrash.outputs.get(0);
				if ((Integer) TNUtils.getFromJSON(outputTrash, "code") == 0) {
					trashNoteArr = (JSONArray) TNUtils.getFromJSON(outputTrash, "note_ids");
					for (TNNote trashNote: trashNotes) {
						boolean trashNoteExit = false;
						for (int i = 0 ; i < trashNoteArr.length(); i++) {
							JSONObject obj = (JSONObject) TNUtils.getFromJSON(trashNoteArr, i);
							long noteId = Long.valueOf(TNUtils.getFromJSON(obj, "id").toString());
							if (trashNote.noteId == noteId) {
								trashNoteExit = true;
								break;
							}
						}
						if (!trashNoteExit) {
							TNAction.runAction(TNActionType.Db_Execute,
									TNSQLString.NOTE_DELETE_BY_NOTEID,
									trashNote.noteId);
						}
					}
					for (int i = 0 ; i < trashNoteArr.length(); i++) {
						JSONObject obj = (JSONObject) TNUtils.getFromJSON(trashNoteArr, i);
						long noteId = Long.valueOf(TNUtils.getFromJSON(obj, "id").toString());
						boolean trashNoteExit = false;
						for (TNNote trashNote: trashNotes) {
							if (trashNote.noteId == noteId) {
								trashNoteExit = true;
								break;
							}
						}
						if (!trashNoteExit) {
							TNAction.runAction(TNActionType.GetNoteByNoteId, noteId);
						}
					}
				}
			}

			aAction.finished();
		} else {
			aAction.finished();
		}

	}
	
	public void SynchronizeEdit(TNAction aAction) {
		//add
		Vector<TNNote> addNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 3);
		for (int i = 0; i < addNotes.size(); i++) {
			TNAction.runAction(TNActionType.NoteAdd, addNotes.get(i), true);
		}

		//recovery
		Vector<TNNote> recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
		for (int i = 0; i < recoveryNotes.size(); i++) {
			if (recoveryNotes.get(i).noteId != -1) {
				TNAction.runAction(TNActionType.NoteRecovery, recoveryNotes.get(i).noteId);
			} else {
				TNAction.runAction(TNActionType.NoteAdd, recoveryNotes.get(i), true);
			}
		}

		//delete
		Vector<TNNote> deleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
		for (int i = 0; i < deleteNotes.size(); i++) {
			if (deleteNotes.get(i).noteId != -1) {
				TNAction.runAction(TNActionType.NoteDelete, deleteNotes.get(i).noteId);
			} else {
				TNAction.runAction(TNActionType.NoteLocalDelete, deleteNotes.get(i).noteLocalId);
			}
		}

		//realDelete
		Vector<TNNote> deleteRealNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
		for (int i = 0; i < deleteRealNotes.size(); i++) {
			if (deleteRealNotes.get(i).noteId == -1) {
				TNAction.runAction(TNActionType.Db_Execute,
						TNSQLString.NOTE_DELETE_BY_NOTELOCALID,
						deleteRealNotes.get(i).noteLocalId);
			} else {
				TNAction.runAction(TNActionType.NoteDelete, deleteRealNotes.get(i).noteId);
				TNAction.runAction(TNActionType.NoteRealDelete, deleteRealNotes.get(i).noteId);
			}
		}

		TNAction actionPage = TNAction.runAction(TNActionType.GetAllNoteIds);
		JSONArray cloudIds = new JSONArray();
		if (!(actionPage.outputs.get(0) instanceof String)) {
			JSONObject outputPage = (JSONObject) actionPage.outputs.get(0);
			if ((Integer) TNUtils.getFromJSON(outputPage, "code") == 0) {
				cloudIds = (JSONArray) TNUtils.getFromJSON(outputPage, "note_ids");
			}

			try {
				//与云端同步数据
				Vector<TNNote> allNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
				for (int i = 0; i < allNotes.size(); i++) {
					boolean isExit = false;
					TNNote note = allNotes.get(i);
					for (int j = 0; j < cloudIds.length(); j++) {
						if (note.noteId == Long.valueOf( TNUtils.getFromJSON((JSONObject)cloudIds.get(j), "id").toString())) {
							isExit = true;
							break;
						}
					}
					if (!isExit && note.syncState != 7) {
						TNAction.runAction(TNActionType.Db_Execute,
								TNSQLString.NOTE_DELETE_BY_NOTEID,
								note.noteId);
					}
				}

				//edit  通过最后更新时间来与云端比较是否该上传本地编辑的笔记
				for (int i = 0; i < cloudIds.length(); i++) {
					long id = Long.valueOf( TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "id").toString());
					int lastUpdate =  Integer.valueOf(TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "update_at").toString());
					Vector<TNNote> editNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 4);
					for (int j = 0; j < editNotes.size(); j++) {
						if (id == editNotes.get(j).noteId) {
							if (editNotes.get(j).lastUpdate > lastUpdate) {
								TNAction.runAction(TNActionType.NoteEdit, editNotes.get(j));
							} else {
								TNAction.runAction(TNActionType.Db_Execute,
										TNSQLString.NOTE_UPDATE_SYNCSTATE,
										1,
										editNotes.get(j).noteLocalId);
							}
						}
					}
				}

				//更新云端的笔记
				for (int i = 0; i < cloudIds.length(); i++) {
					boolean isExit = false;
					long id = Long.valueOf( TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "id").toString());
					int lastUpdate =  Integer.valueOf(TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "update_at").toString());
					for (int j = 0; j < allNotes.size(); j++) {
						TNNote note = allNotes.get(j);
						if (id == note.noteId) {
							isExit = true;
							if (lastUpdate > note.lastUpdate) {
								TNAction.runAction(TNActionType.GetNoteByNoteId, id);
							}
							break;
						}
					}
					if (!isExit) {
						TNAction.runAction(TNActionType.GetNoteByNoteId, id);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				MLog.e("SynchronizeEdit", "==================================================SynchronizeEdit");
			}

			aAction.finished();
		} else {
			aAction.finished();
		}
	}
	
	public void SynchronizeCat(TNAction aAction) {
		long catId = (Long) aAction.inputs.get(0);
		//add
		Vector<TNNote> addNotes = TNDbUtils.getNoteListBySyncStateByCatId(TNSettings.getInstance().userId, 3, catId);
		for (int i = 0; i < addNotes.size(); i++) {
			TNAction.runAction(TNActionType.NoteAdd, addNotes.get(i), true);
		}

		//recovery
		Vector<TNNote> recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
		for (int i = 0; i < recoveryNotes.size(); i++) {
			if (recoveryNotes.get(i).noteId != -1) {
				TNAction.runAction(TNActionType.NoteRecovery, recoveryNotes.get(i).noteId);
			} else {
				TNAction.runAction(TNActionType.NoteAdd, recoveryNotes.get(i), true);
			}
		}

		//delete
		Vector<TNNote> deleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
		for (int i = 0; i < deleteNotes.size(); i++) {
			if (deleteNotes.get(i).noteId != -1) {
				TNAction.runAction(TNActionType.NoteDelete, deleteNotes.get(i).noteId);
			} else {
				TNAction.runAction(TNActionType.NoteLocalDelete, deleteNotes.get(i).noteLocalId);
			}
		}

		//realDelete
		Vector<TNNote> deleteRealNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
		for (int i = 0; i < deleteRealNotes.size(); i++) {
			if (deleteRealNotes.get(i).noteId == -1) {
				TNAction.runAction(TNActionType.Db_Execute,
						TNSQLString.NOTE_DELETE_BY_NOTELOCALID,
						deleteRealNotes.get(i).noteLocalId);
			} else {
				TNAction.runAction(TNActionType.NoteDelete, deleteRealNotes.get(i).noteId);
				TNAction.runAction(TNActionType.NoteRealDelete, deleteRealNotes.get(i).noteId);
			}
		}

		TNAction actionPage = TNAction.runAction(TNActionType.GetFolderNoteIds, catId);//這塊需要獲取某個文件夾下的所有筆記id
		JSONArray cloudIds = new JSONArray();
		if (!(actionPage.outputs.get(0) instanceof String)) {
			JSONObject outputPage = (JSONObject) actionPage.outputs.get(0);
			if ((Integer) TNUtils.getFromJSON(outputPage, "code") == 0) {
				cloudIds = (JSONArray) TNUtils.getFromJSON(outputPage, "note_ids");
			}

			try {
				//与云端同步数据
				Vector<TNNote> allNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
				for (int i = 0; i < allNotes.size(); i++) {
					boolean isExit = false;
					TNNote note = allNotes.get(i);
					for (int j = 0; j < cloudIds.length(); j++) {
						if (note.noteId == Long.valueOf( TNUtils.getFromJSON((JSONObject)cloudIds.get(j), "id").toString())) {
							isExit = true;
							break;
						}
					}
					if (!isExit && note.syncState != 7) {
						TNAction.runAction(TNActionType.Db_Execute,
								TNSQLString.NOTE_DELETE_BY_NOTEID,
								note.noteId);
					}
				}

				//edit  通过最后更新时间来与云端比较是否该上传本地编辑的笔记
				for (int i = 0; i < cloudIds.length(); i++) {
					long id = Long.valueOf( TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "id").toString());
					int lastUpdate =  Integer.valueOf(TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "update_at").toString());
					Vector<TNNote> editNotes = TNDbUtils.getNoteListBySyncStateByCatId(TNSettings.getInstance().userId, 4, catId);
					for (int j = 0; j < editNotes.size(); j++) {
						if (id == editNotes.get(j).noteId) {
							if (editNotes.get(j).lastUpdate > lastUpdate) {
								TNAction.runAction(TNActionType.NoteEdit, editNotes.get(j));
							} else {
								TNAction.runAction(TNActionType.Db_Execute,
										TNSQLString.NOTE_UPDATE_SYNCSTATE,
										1,
										editNotes.get(j).noteLocalId);
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				MLog.e("SynchronizeCat", "==================================================SynchronizeCat");
			}

			Vector<TNNote> allLocalNotes = TNDbUtils.getNoteListByCatId(TNSettings.getInstance().userId, catId, TNSettings.getInstance().sort, TNConst.MAX_PAGE_SIZE);
			try {
				//这块不严谨
				for (int i = 0; i < cloudIds.length(); i++) {
					long id = Long.valueOf( TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "id").toString());
					int lastUpdate =  Integer.valueOf(TNUtils.getFromJSON((JSONObject)cloudIds.get(i), "update_at").toString());
					boolean isExit = false;
					for (int j = 0; j < allLocalNotes.size(); j++) {
						if (id == allLocalNotes.get(j).noteId) {
							if (allLocalNotes.get(j).lastUpdate < lastUpdate) {
								TNAction action = TNAction.runAction(TNActionType.GetNoteByNoteId, id);
								if (action.outputs.get(0) instanceof String) {
									continue;
								}
							}
							isExit = true;
							break;
						}
					}
					if (!isExit) {
						TNAction action = TNAction.runAction(TNActionType.GetNoteByNoteId, id);
						if (action.outputs.get(0) instanceof String) {
							continue;
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Vector<TNNote> catNotes = TNDbUtils.getNoteListByCatId(TNSettings.getInstance().userId, catId, TNSettings.getInstance().sort, TNConst.MAX_PAGE_SIZE);
			for (TNNote catNote: catNotes) {
				if (catNote.syncState == 1) {
					TNAction action = TNAction.runAction(TNActionType.GetAllDataByNoteId, catNote.noteId);
					if (action.outputs.size() >0 && action.outputs.get(0) instanceof String) {
						continue;
					}
				}
			}

			aAction.finished();
		} else {
			aAction.finished();
		}

	}

	public void SynchronizeTrash(TNAction aAction) {
		//同步回收站的笔记
		Vector<TNNote> trashNotes = TNDbUtils.getNoteListByTrash(TNSettings.getInstance().userId, TNConst.CREATETIME);
		TNAction actionTrash = TNAction.runAction(TNActionType.GetAllTrashNoteIds);
		JSONArray trashNoteArr = new JSONArray();
		if (!(actionTrash.outputs.get(0) instanceof String)) {
			JSONObject outputTrash = (JSONObject) actionTrash.outputs.get(0);
			if ((Integer) TNUtils.getFromJSON(outputTrash, "code") == 0) {
				trashNoteArr = (JSONArray) TNUtils.getFromJSON(outputTrash, "note_ids");
				for (TNNote trashNote: trashNotes) {
					boolean trashNoteExit = false;
					for (int i = 0 ; i < trashNoteArr.length(); i++) {
						JSONObject obj = (JSONObject) TNUtils.getFromJSON(trashNoteArr, i);
						long noteId = Long.valueOf(TNUtils.getFromJSON(obj, "id").toString());
						if (trashNote.noteId == noteId) {
							trashNoteExit = true;
							break;
						}
					}
					if (!trashNoteExit) {
						TNAction.runAction(TNActionType.Db_Execute,
								TNSQLString.NOTE_DELETE_BY_NOTEID,
								trashNote.noteId);
					}
				}
				for (int i = 0 ; i < trashNoteArr.length(); i++) {
					JSONObject obj = (JSONObject) TNUtils.getFromJSON(trashNoteArr, i);
					long noteId = Long.valueOf(TNUtils.getFromJSON(obj, "id").toString());
					boolean trashNoteExit = false;
					for (TNNote trashNote: trashNotes) {
						if (trashNote.noteId == noteId) {
							trashNoteExit = true;
							break;
						}
					}
					if (!trashNoteExit) {
						TNAction.runAction(TNActionType.GetNoteByNoteId, noteId);
					}
				}
			}
		}

		aAction.finished();
	}

}
