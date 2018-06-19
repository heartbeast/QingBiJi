package com.thinkernote.ThinkerNote.Service;


import com.thinkernote.ThinkerNote.Utils.MLog;

public class TNCacheService {
	private static final String TAG = "TNCacheService";
	private static TNCacheService singleton = null;

	private TNCacheService(){
		MLog.d(TAG,"TNCacheService()");
	}
	
	public static TNCacheService getInstance(){
		if (singleton == null){
			synchronized (TNCacheService.class){
				if (singleton == null){
					singleton = new TNCacheService();
				}
			}
		}
		
		return singleton;
	}
	
}
