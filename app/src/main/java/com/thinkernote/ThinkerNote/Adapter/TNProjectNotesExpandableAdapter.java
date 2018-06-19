package com.thinkernote.ThinkerNote.Adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class TNProjectNotesExpandableAdapter extends BaseExpandableListAdapter{
	private static final String TAG = "TNProjectNotesExpandableAdapter";
	private Activity mActivity;
	private LayoutInflater layoutInflater = null;
	private Vector<TNNote> mNotes;
	private Vector<TNNoteGroup> mGroups;
	private float mScale;
	
	public TNProjectNotesExpandableAdapter(Activity activity, Vector<TNNote> notes, Vector<TNNoteGroup> groups, float scale){
		this.mActivity = activity;
		this.layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mNotes = notes;
		this.mGroups = groups;
		this.mScale = scale;
	}
	
	public void update(Vector<TNNote> notes, Vector<TNNoteGroup> groups){
		this.mNotes = notes;
		this.mGroups = groups;
	}
	
	public Object getChild(int groupPosition, int childPosition) {
		return mNotes.get(mGroups.get(groupPosition).startIndex
				+ childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		TNNote note = (TNNote) getChild(groupPosition,
				childPosition);
		return note.noteId;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		TNNoteViewHolder holder = null;
		if (convertView == null) {
			holder = new TNNoteViewHolder();
			convertView = (LinearLayout) layoutInflater.inflate(
					R.layout.notelistitem_big, null);
			
			holder.noteTitle = (TextView)convertView.findViewById(R.id.notelistitem_big_title);
			holder.date = (TextView)convertView.findViewById(R.id.notelistitem_big_date);
			holder.shortContent = (TextView)convertView.findViewById(R.id.notelistitem_big_shortcontent);
			holder.thumbnail = (ImageView)convertView.findViewById(R.id.notelistitem_big_thumbnail);
			holder.thumbnailBg = (ImageView)convertView.findViewById(R.id.notelistitem_big_thumbnail_bg);
			holder.source = (TextView)convertView.findViewById(R.id.notelistitem_big_source);
			
			convertView.setTag(holder);
		}else{
			holder = (TNNoteViewHolder)convertView.getTag();
		}
		setChildView(holder, groupPosition, childPosition);

		return convertView;
	}

	private void setChildView(TNNoteViewHolder holder, int groupPosition,
			int childPosition) {
		TNNote note = (TNNote) getChild(groupPosition,
				childPosition);
		holder.noteTitle.setText(note.title);
		holder.shortContent.setText(TNUtilsHtml.decodeHtml(note.shortContent.trim()));
		if (TNSettings.getInstance().sort == TNConst.UPDATETIME) {
			holder.date.setText(note.lastUpdate);
		} else {
			holder.date.setText(note.createTime);
		}
		
	
		ImageView thumbnailView = holder.thumbnail;
		thumbnailView.setTag(null);
		if (note.attCounts > 0) {
			if (!TextUtils.isEmpty(note.thumbnail) && !("null").equals(note.thumbnail) && note.syncState != 1) {
				holder.thumbnailBg.setVisibility(View.VISIBLE);
				thumbnailView.setImageBitmap(TNUtilsAtt.getImage(note.thumbnail, 90));
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
						(int) (46 * mScale), (int) (46 * mScale), Gravity.CENTER);
				holder.thumbnailBg.setLayoutParams(layoutParams);
				thumbnailView.setLayoutParams(layoutParams);
			} else {
				holder.thumbnailBg.setVisibility(View.INVISIBLE);
				thumbnailView.setImageResource(R.drawable.notelist_thumbnail_att);
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
						(int) (46 * mScale), (int) (46 * mScale), Gravity.CENTER);
				thumbnailView.setLayoutParams(layoutParams);
			}
		} else {
			thumbnailView.setImageResource(R.drawable.notelist_thumbnail_note);
			holder.thumbnailBg.setVisibility(View.INVISIBLE);
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					(int) (46 * mScale), (int) (46 * mScale), Gravity.CENTER);
			thumbnailView.setLayoutParams(layoutParams);
		}

		holder.source.setText(mActivity.getString(R.string.sharenotes_source, note.creatorNick));
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mGroups.get(groupPosition).count;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return mGroups.get(groupPosition).groupTime;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = (LinearLayout) layoutInflater.inflate(
					R.layout.notelistgroup, null);
		}
		setGroupView(convertView, groupPosition);

		return convertView;
	}

	private void setGroupView(View layout, int groupPosition) {
		TNNoteGroup group = (TNNoteGroup) getGroup(groupPosition);
		if (TNSettings.getInstance().noteListOrder == 2) {
			((TextView) layout.findViewById(R.id.notelistgroup_title))
					.setText(group.strIndex);
		} else {
			long tzOffset = Calendar.getInstance().getTimeZone()
					.getRawOffset() / 1000;
			
			Date date = new Date(group.groupTime * 1000L -tzOffset);
			String formated = String
					.format(mActivity
							.getString(R.string.notelist_mmformat), date
							.getYear() + 1900, date.getMonth() + 1);
			((TextView) layout.findViewById(R.id.notelistgroup_title))
					.setText(formated);
		}
		((TextView) layout.findViewById(R.id.notelistgroup_count))
				.setText(group.count + "");
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
