package com.thinkernote.ThinkerNote.General;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.thinkernote.ThinkerNote.Utils.MLog;

public class TNDialogPreference extends DialogPreference {
	private static final String TAG = "TNDialogPreference";

	private OnClickListener mOnClickListener = null;

	public TNDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setOnClickListener(OnClickListener aOnClickListener){
		mOnClickListener = aOnClickListener;
	}
	
	public void onClick (DialogInterface dialog, int which){
		MLog.d(TAG, dialog.toString() + ":" + which );
		if( mOnClickListener != null ){
			mOnClickListener.onClick(dialog, which);
		}
	}

}
