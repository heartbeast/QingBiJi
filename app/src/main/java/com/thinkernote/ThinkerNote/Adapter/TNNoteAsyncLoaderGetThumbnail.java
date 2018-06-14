package com.thinkernote.ThinkerNote.Adapter;

import android.net.Uri;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNUtils;

public class TNNoteAsyncLoaderGetThumbnail extends
		AsyncTask<Object, Integer, Object> {
	private static final String TAG = "TNNoteThumbnailAsyncLoader";
	private static final int BIG_THUMBNAIL_WIDTH = 46;
	public static final int BIG_THUMBNAIL_HEIGHT = 46;

	private ImageView mView;
	private float mScale;

	@Override
	protected TNAction doInBackground(Object... params) {
		mView = (ImageView) params[0];
		mScale = (Float) params[1];
		TNNote note = (TNNote) mView.getTag();
		if (note != null) {
			try {
				Log.i(TAG, "noteid" + note.noteId);
				int width = BIG_THUMBNAIL_WIDTH;
				int height = BIG_THUMBNAIL_HEIGHT;

//				TNAction aAction = TNAction.runAction(
//						TNActionType.GetShareNoteThumbnail,
//						TNUtils.Hash17(note.noteId), note.thumbnailId, width,
//						height);
//				String thumbnailPath = (String) aAction.outputs.get(0);
//				if (thumbnailPath != null && thumbnailPath.length() > 0)
//					note.thumbnail = thumbnailPath;
//				return aAction;
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
			if (note.thumbnail != null) {
				mView.setImageURI(Uri.parse(note.thumbnail));
				note.thmDrawable = mView.getDrawable();
			} else {
				mView.setImageResource(R.drawable.notelist_thumbnail_att);
			}
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					(int) (46 * mScale), (int) (46 * mScale), Gravity.CENTER);
			mView.setLayoutParams(layoutParams);
		}
	}
}
