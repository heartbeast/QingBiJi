package com.thinkernote.ThinkerNote.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.Service.TNPushService;

public class TNSystemStartedReceiver extends BroadcastReceiver {
	String TAG = "TNSystemStartedReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "System started");
		Intent serviceIntent = new Intent(context, TNPushService.class);
		context.startService(serviceIntent);
	}

}
