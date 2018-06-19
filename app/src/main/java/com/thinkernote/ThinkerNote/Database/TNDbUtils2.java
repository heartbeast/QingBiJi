package com.thinkernote.ThinkerNote.Database;

import java.util.Vector;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.General.TNActionType2;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;

public class TNDbUtils2 {
	private static final String TAG = "TNDbUtils";

	public static long getUserLocalId(int aUserType, Object aNameOrId){
		TNAction action = null;
		if( aUserType == 0){
			if( String.class.isInstance(aNameOrId)){
				action = TNAction.runAction(TNActionType2.Db_Execute, 
						TNSQLString2.USER_CHECK_USERNAME,
						aNameOrId,
						aNameOrId);
			}else {
				action = TNAction.runAction(TNActionType2.Db_Execute, 
						TNSQLString2.USER_CHECK_USERID,
						aNameOrId);
			}
		}else{
			action = TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString2.BINDING_CHECK_BID,
					aUserType,
					aNameOrId);
		}
		
		if(TNDb2.getSize(action) > 0){
			long userLocalId = Long.valueOf(TNDb2.getData(action, 0, 0));
			MLog.i(TAG, "getUserLocalId userLocalId = " + userLocalId);
			return userLocalId;
		}else
			return -1;
	}
	public static long getUserServerId(long userLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.USER_SELECT_BY_ID,
				userLocalId);
		return Long.valueOf(TNDb2.getData(action, 0, 6));
	}
	
	public static String getUserNickName(long userLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.USER_SELECT_BY_ID,
				userLocalId);
		return TNDb2.getData(action, 0, 4);
	}
	
	public static long getCatLocalId(Object aId){
		TNAction action =TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_CHECK_CATID,
				TNSettings.getInstance().userId,
				aId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		else
			return -1;
	}
	
	public static long getCatLocalIdForNote(Object aId){
		TNAction action =TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.NOTE_GET_CATID,
				aId);
		return Long.valueOf(TNDb2.getData(action, 0, 0));
	}
	
	public static long getCatLocalIdForAtt(Object aId){
		TNAction action =TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_GETLOCALID_BYATT,
				aId);
		return Long.valueOf(TNDb2.getData(action, 0, 0));
	}
	
	//当catId已经存在
	public static long getCatLocalIdWhenConflict(long catId, long projectLocalId, long userLocalId){
		TNAction action =TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_CHECK_CATID_CONFLICT,
				catId,
				projectLocalId,
				userLocalId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		else
			return -1;
	}
	
	public static long getCatServerId(long aId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_SELECT_BY_ID,
				aId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 5));
		else
			return -1;
	}
	
//	public static int getCatIsNew(long catLocalId){
//		TNAction action = TNActionCenter.runAction(TNActionType2.Db_Execute, 
//				TNSQLString.CAT_GET_ISNEW,
//				catLocalId);
//		return Integer.valueOf(TNDb.getData(action, 0, 0));
//	}
	
	public static long getNoteLocalId(Object aId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString.NOTE_CHECK_ID,
				TNSettings.getInstance().userId,
				aId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		else{
			action = TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString.NOTE_CHECK_ID,
					-1, aId);
			if(TNDb2.getSize(action) > 0)
				return Long.valueOf(TNDb2.getData(action, 0, 0));
			else
				return -1;
		}
	}
	
	public static long getNoteLocalId1(Object aId){
		String sql = TNSQLString.NOTE_CHECK_ID;
		sql = sql.replace("`userLocalId` = ? AND ", "");
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				sql,
				aId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		else
			return -1;
	}
	
	public static long getNoteComSyncRevision(long noteLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.NOTE_GET_COMSYNCREVISION,
				noteLocalId);
		return Long.valueOf(TNDb2.getData(action, 0, 0));
	}

	public static long getTagLocalId(Object aId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.TAG_CHECK_ID,
				aId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		else
			return -1;
	}
	
	public static long getAttLocalId(Object aId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.ATT_CHECK_ID,
				aId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		else
			return -1;
	}
	
	public static long getAttLocalIdForUser(Object aId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute,
				TNSQLString2.ATT_CHECK_ID_FORUSER, 
				aId);
		if(TNDb2.getSize(action) > 0){
			long userLocalId = Long.valueOf(TNDb2.getData(action, 0, 0));
			if(userLocalId == TNSettings.getInstance().userId)
				return Long.valueOf(TNDb2.getData(action, 0, 1));
		}
		return -1;
	}
	
	public static long getAttLocalIdForUser(Object aId, long currentUserLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute,
				TNSQLString2.ATT_CHECK_ID_FORUSER, 
				aId);
		if(TNDb2.getSize(action) > 0){
			long userLocalId1 = Long.valueOf(TNDb2.getData(action, 0, 0));
			if(userLocalId1 == currentUserLocalId)
				return Long.valueOf(TNDb2.getData(action, 0, 1));
		}
		return -1;
	}
	
	public static long getProjectLocalId(Object aId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.PROJECT_CHECK_ID,
				aId,
				TNSettings.getInstance().userId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		else
			return 0;
	}
	
	public static long getProjectLocalIdForNote(Object aId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.PROJECT_SELECT_FOR_NOTEID,
				aId,
				TNSettings.getInstance().userId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		else
			return 0;
	}

	public static long getProjectServerId(long aId){
		if( aId <= 0)
			return 0;
		
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.PROJECT_GET_BYID,
				aId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 1));
		else
			return -1;
	}
	
	public static int getProjectCount(long userLocalId){
		if(userLocalId <= 0){
			return -1;
		}
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.PROJECT_COUNT,
				userLocalId);
		return Integer.valueOf(TNDb2.getData(action, 0, 0).toString());
	}
	
	public static String withSyncState(String aSql){
		int index = aSql.indexOf("SET");
		if( index > 0){
			return aSql.substring(0, index + 3) + " `syncState` = 1," 
					+ aSql.substring(index + 3);
		}else{
			return aSql;
		}
	}

	public static String withoutSyncState(String aSql){
		int index = aSql.indexOf("SET");
		if( index > 0){
			return aSql.substring(0, index + 3) + " `syncState` = 0," 
					+ aSql.substring(index + 3);
		}else{
			return aSql;
		}
	}

	public static String addNotInClause(String aSql, String aField, 
			Vector<Long> aIds){
		int index = aSql.indexOf("WHERE");
		if( index > 0 && aIds.size() > 0){
			String ids = aIds.toString();
			return aSql.substring(0, index + 6) 
				+ aField + " NOT IN (" + ids.substring(1, ids.length()-1) + ") AND " 
				+ aSql.substring(index + 6);
		}else{
			return aSql;
		}
	}
	
//	public static long getCatMinRevision(){
//		TNAction action = TNActionCenter.runAction(TNActionType2.Db_Execute, 
//				TNSQLString2.CAT_MINREVISION,
//				TNSettings.getInstance().userId);
//		return Long.valueOf(TNDb2.getData(action, 0, 0));
//	}
//	
//	public static long getTagMinRevision(){
//		TNAction action = TNActionCenter.runAction(TNActionType2.Db_Execute, 
//				TNSQLString2.TAG_MINREVISION,
//				TNSettings.getInstance().userId);
//		return Long.valueOf(TNDb2.getData(action, 0, 0));
//	}
	
	public static long getNoteMinRevision(long noteLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.NOTE_MINREVISION,
				noteLocalId,
				noteLocalId,
				noteLocalId);
		return Long.valueOf(TNDb2.getData(action, 0, 0));
	}
	
	public static boolean checkNoteTagIsModified(long noteLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.NOTETAG_SYNCSTATE, 
				noteLocalId);
		if(TNDb2.getSize(action) > 0){
			if(Integer.valueOf(TNDb2.getData(action, 0, 0)) > 0){
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkNoteAttIsModified(long noteLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.ATT_SYNCSTATE,
				noteLocalId);
		if(TNDb2.getSize(action) > 0){
			if(Integer.valueOf(TNDb2.getData(action, 0, 0)) > 0){
				return true;
			}
		}
		return false;
	}
	
//	public static boolean checkNoteIsPublic(long noteLocalId){
//		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
//				TNSQLString2.NOTE_se,
//				noteLocalId);
//	}
	
//	public static long getAllNoteMinRevision(){
//		TNAction action = TNActionCenter.runAction(TNActionType2.Db_Execute, 
//				TNSQLString2.NOTE_ALLMINREVISION,
//				TNSettings.getInstance().userLocalId,
//				TNSettings.getInstance().userLocalId,
//				TNSettings.getInstance().userLocalId);
//		return Long.valueOf(TNDb2.getData(action, 0, 0));
//	}
	
	public static long getDefaultCatId(){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.USER_SELECT_BY_ID,
				TNSettings.getInstance().userId);
		return Long.valueOf(TNDb2.getData(action, 0, 5));
	}

	public static long getNoteSyncRevision(long projectLocalId){
		if( projectLocalId <= 0 ){
			TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString2.USER_SELECT_SYNC_REVISION,
					TNSettings.getInstance().userId);
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		}else{
			TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString2.PROJECT_SELECT_SYNC_REVISION,
					projectLocalId);
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		}
	}
	
	public static long getCatSyncRevision(long projectLocalId){
		if( projectLocalId <= 0 ){
			TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString2.USER_SELECT_SYNC_REVISION,
					TNSettings.getInstance().userId);
			return Long.valueOf(TNDb2.getData(action, 0, 1));
		}else{
			TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString2.PROJECT_SELECT_SYNC_REVISION,
					projectLocalId);
			return Long.valueOf(TNDb2.getData(action, 0, 1));
		}
	}
	
	public static long getTagSyncRevision(){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.USER_SELECT_SYNC_REVISION,
				TNSettings.getInstance().userId);
		return Long.valueOf(TNDb2.getData(action, 0, 2));
	}
	
	public static long getProjectRevision(){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.USER_SELECT_SYNC_REVISION,
				TNSettings.getInstance().userId);
		MLog.i(TAG, "userLocalId: " + TNSettings.getInstance().userId);
		return Long.valueOf(TNDb2.getData(action, 0, 3));
	}
	
	public static void setCatSyncRevisionToZero(long userLocalId, long projectLocalId){
		if(projectLocalId > 0){
			TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString2.PROJECT_SET_CATSYNCREVISION,
					0,
					projectLocalId);
		}else{
			TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString2.USER_SET_CATSYNCREVISION,
					0,
					userLocalId);
		}
	}
	
	public static void setInviteUser(long userLocalId){
		TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.USER_UPDATE_INVITENAME,
				"default@invitename",
				userLocalId);
	}
	
	public static boolean isCatGroup(long aId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_SELECT_BY_ID,
				aId);
		if(TNDb2.getSize(action) > 0)
			return Integer.valueOf(TNDb2.getData(action, 0, 9))==0;
		else
			return false;
	}
	
	public static String getCatName(long aId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_SELECT_BY_ID,
				aId);
		if(TNDb2.getSize(action) > 0)
			return TNDb2.getData(action, 0, 1);
		else
			return "";
	}

	public static long getProjectLocalIdByCat(long aCatLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_SELECT_BY_ID,
				aCatLocalId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 10));
		else
			return 0;
	}
	
	public static long getProjectLocalIdByNote(long aNoteLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.NOTE_SIMPLE_INFO,
				aNoteLocalId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 8));
		else
			return 0;
	}
	
	public static long getCommentLocalId(long comId, long noteLocalId){
		TNAction action = TNAction.runAction(TNActionType2.Db_Execute,
				TNSQLString2.COMMENT_CHECK_ID,
				comId,
				noteLocalId);
		if(TNDb2.getSize(action) > 0)
			return Long.valueOf(TNDb2.getData(action, 0, 0));
		else
			return 0;
	}
	
	//恢复cat本身及其系列父文件夹
	public static void recoverParentCats(long catLocalId){
		MLog.i(TAG, "recoverParentCats");
		TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_UPDATE_TRASH,
				0, catLocalId);
			
		TNAction act1 = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_SELECT,
				catLocalId);
		long pCatLocalId = Integer.valueOf(TNDb2.getData(act1, 0, 4));
		if(pCatLocalId <= 0)
			return;
		TNAction act = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.CAT_SELECT,
				TNDb2.getData(act1, 0, 4));
			
		while(Integer.valueOf(TNDb2.getData(act, 0, 1)) == 1){
			TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString2.CAT_UPDATE_TRASH,
					0, TNDb2.getData(act, 0, 0));
				
			pCatLocalId = Integer.valueOf(TNDb2.getData(act, 0, 4));
			if(pCatLocalId <= 0){
				break;
			}
			act = TNAction.runAction(TNActionType2.Db_Execute, 
					TNSQLString2.CAT_SELECT,
					pCatLocalId);
		}
	}

	public static long getSharNoteLocalId(long oId, long userId){
		TNAction aAction = TNAction.runAction(TNActionType2.Db_Execute, 
				TNSQLString2.NOTESHARE_GET_BY_NOTEID, oId, userId
				);
		if(TNDb2.getSize(aAction) > 0){
			return Long.valueOf(TNDb2.getData(aAction, 0, 0));
		}else
			return -1;
	}
}
