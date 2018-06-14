package com.thinkernote.ThinkerNote.General;

import java.io.File;
import java.util.Vector;

import android.app.Activity;
import android.text.TextUtils;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;

public class TNDownloadAttService {
	private static final String TAG = "TNDownloadAttService";
	private static final long ATT_MAX_DOWNLOAD_SIZE = 50*1024;
	
	private static TNDownloadAttService singleton = null;
	
	private OnDownloadStartListener startListener;
	private OnDownloadEndListener endListener;
	
	private Vector<TNNoteAtt> downloadingAtts;
	private Vector<TNNoteAtt> readyDownloadAtts;
	
	private TNNote mNote;
	private Activity act;
	
	private TNDownloadAttService(){
		readyDownloadAtts = new Vector<TNNoteAtt>();
		downloadingAtts = new Vector<TNNoteAtt>();
		
		TNAction.regResponder(TNActionType.SyncNoteAtt, this, "respondSyncNoteAtt");
	}
	
	public static TNDownloadAttService getInstance(){
		if (singleton == null){
			synchronized (TNDownloadAttService.class){
				if (singleton == null){
					singleton = new TNDownloadAttService();
				}
			}
		}		
		return singleton;
	}
	
	public TNDownloadAttService init(Activity act, TNNote note){
		this.act = act;
		mNote = note;
		this.readyDownloadAtts.clear();
		this.readyDownloadAtts.addAll(note.atts);
		
		return this;
	}
	
	public void updateNote(TNNote note){
		this.mNote = note;
		this.readyDownloadAtts.clear();
		this.readyDownloadAtts.addAll(note.atts);
	}
	
	public void start(){
		Log.d(TAG, "start");
		Vector<TNNoteAtt> tmpList = new Vector<TNNoteAtt>();
		for(TNNoteAtt att : readyDownloadAtts) {

			File file = null;
			if (!TextUtils.isEmpty(att.path)) {
				file = new File(att.path);
			}
			if(file.length() != 0 && att.syncState == 2){
				continue;
			}
			if( TNUtils.isNetWork() && att.attId != -1){
				Log.d(TAG,"downloadAtt:" + att.attId );
				if(!TNActionUtils.isDownloadingAtt(att.attId)){
					Log.d(TAG, "3 -> SyncNoteContent: " + att.attId);
					if(startListener != null)
						startListener.onStart(att);
					
					TNAction.runActionAsync(TNActionType.SyncNoteAtt,
							att, 
							mNote);
					downloadingAtts.add(att);
					tmpList.add(att);
				}
			}
		}
		readyDownloadAtts.removeAll(tmpList);
		mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
		mNote.syncState = mNote.syncState > 2 ? mNote.syncState : 2;
		if (mNote.attCounts > 0) {
			for(int i = 0; i < mNote.atts.size(); i++) {
				TNNoteAtt tempAtt = mNote.atts.get(i);
				if (i == 0 && tempAtt.type > 10000 && tempAtt.type < 20000) {
					TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, tempAtt.path, mNote.noteLocalId);
				}
				if (TextUtils.isEmpty(tempAtt.path) || "null".equals(tempAtt.path)) {
					mNote.syncState = 1;
				}
			}
		}
		TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, mNote.syncState, mNote.noteLocalId);
	}
	
	public void start(long attId){
		Log.d(TAG, "start:" + attId);
		if(act == null || act.isFinishing()){
			return;
		}
		if( TNUtilsDialog.checkNetwork(act) ){
			TNNoteAtt att = mNote.getAttDataById(attId);
			Log.d(TAG,"downloadAtt:" + att.attId );
			if(!TNActionUtils.isDownloadingAtt(att.attId)){
				Log.d(TAG, "3 -> SyncNoteContent: " + att.attId);
				if(startListener != null)
					startListener.onStart(att);
				
				TNAction.runActionAsync(TNActionType.SyncNoteAtt,
						att, 
						mNote);
				downloadingAtts.add(att);
				readyDownloadAtts.remove(att);
			}
		}
	}
	
	public void respondSyncNoteAtt(TNAction aAction){
		TNNoteAtt att = (TNNoteAtt)aAction.inputs.get(0);
		Log.d(TAG, "respond:" + att.attId);
		downloadingAtts.remove(att);
		start();
		if(endListener != null){
			if(mNote.getAttDataById(att.attId) != null)
				endListener.onEnd(aAction);
			else
				Log.i(TAG, "att:" + att.attId + " not in the note:" + mNote.noteId);
		}
	}
	
	public void setOnDownloadStartListener(OnDownloadStartListener startListener){
		this.startListener = startListener;
	}
	
	public void setOnDownloadEndListener(OnDownloadEndListener endListener){
		this.endListener = endListener;
	}
	
	public interface OnDownloadStartListener{
		public void onStart(TNNoteAtt att);
	}
	
	public interface OnDownloadEndListener{
		public void onEnd(TNAction aAction);
	}
}
