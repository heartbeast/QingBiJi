package com.thinkernote.ThinkerNote.Adapter;

import android.net.Uri;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;

public class TNNoteAsyncloaderMarkThumbnail extends
		AsyncTask<Object, Integer, Object> {
	private ImageView mView;
	private float mScale;

	@Override
	protected TNAction doInBackground(Object... params) {
		mView = (ImageView) params[0];
		mScale = (Float) params[1];
		TNNote note = (TNNote) mView.getTag();
		 if (note != null) {
		 try {
		 String thmPath = TNUtilsAtt.makeThumbnailForImage(note.thumbnail);
		 if (thmPath != null) {
		 note.thumbnail = thmPath;
		 }
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		 }
		return null;
	}

	@Override
	protected void onPostExecute(Object result) {
		TNNote note = (TNNote) mView.getTag();
		if (note != null) {
			mView.setImageURI(Uri.parse(note.thumbnail));
			note.thmDrawable = mView.getDrawable();
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					(int) (46 * mScale), (int) (46 * mScale), Gravity.CENTER);
			mView.setLayoutParams(layoutParams);
		}
	}
}
