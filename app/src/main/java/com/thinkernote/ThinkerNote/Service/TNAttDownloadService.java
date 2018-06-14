package com.thinkernote.ThinkerNote.Service;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;

public class TNAttDownloadService {
	private static String TAG = "TNAttDownloadService";
	
	private static TNAttDownloadService singleton = null;
	
	public TNAttDownloadService(){
		Log.d(TAG, "TNAttDownloadService()");
		
		TNAction.regRunner(TNActionType.TNHttpDownloadAtt, this, "TNHttpDownloadAtt");
	}
	
	public static TNAttDownloadService getInstance(){
		if(singleton == null){
			synchronized (TNAttDownloadService.class){
				if(singleton == null){
					singleton = new TNAttDownloadService();
				}
			}
		}
		return singleton;
	}
	
	public void TNHttpDownloadAtt(TNAction aAction){
		String cmd = (String) aAction.inputs.get(0);
		String outPath = (String) aAction.inputs.get(2);
		
		aAction.runChildAction(TNActionType.TNOpenUrl, "GET", cmd, outPath, TNActionType.TNHttpDownloadAtt);
		aAction.finished();
	}
}
