package com.thinkernote.ThinkerNote.Service;

import android.graphics.BitmapFactory;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;

import java.io.File;

public class TNAttService {
	private static final String TAG = "TNAttService";

	private static TNAttService singleton = null;

	private TNAttService(){
		MLog.d(TAG,"TNAttService()");
		TNAction.regRunner(TNActionType.SyncNoteAtt, this, "SyncNoteAtt");
	}
	
	public static TNAttService getInstance(){
		if (singleton == null){
			synchronized (TNAttService.class){
				if (singleton == null){
					singleton = new TNAttService();
				}
			}
		}
		
		return singleton;
	}

	// TODO
	public void SyncNoteAtt(TNAction aAction){
		TNNoteAtt att = (TNNoteAtt)aAction.inputs.get(0); 
		
		if(TNActionUtils.isDownloadingAtt(att.attId)){
			return;
		}
		// check file downloadSize
		String path = TNUtilsAtt.getAttPath(att.attId, att.type);
		if(path == null){
			aAction.failed(TNUtils.getAppContext().getResources().getString(R.string.alert_NoSDCard));
			return;
		}
		
		//http 方式从服务器下载附件
		aAction.runChildAction(TNActionType.TNHttpDownloadAtt, ("attachment/" + att.attId), att.attId, path);
		if(handleHttpDownloadOk(aAction, att, att.attId, path, att.digest)){
			aAction.finished();
			return;
		}
		
		aAction.failed(TNUtils.getAppContext().getResources().getString(R.string.alert_Net_Unreachable));
	}
	
	private boolean handleHttpDownloadOk(TNAction aAction, TNNoteAtt att, long attId, String path, String digest){
		if(aAction.childAction.result == TNActionResult.Finished){
			File file = new File(path);
			if(!file.exists())
				return false;
			
			int width = 0;
			int height = 0;
			if (att.type > 10000 && att.type < 20000) {
				BitmapFactory.Options bfo = TNUtilsAtt.getImageSize(path);
				width = bfo.outWidth;
				height = bfo.outHeight;
			}
			TNDb.getInstance().execSQL(TNSQLString.ATT_SET_DOWNLOADED, path, width, height, 2, attId);
			if(att != null){
				att.path = path;
			}
			
			aAction.finished();
			return true;
		}
		return false;
	}
	

}
