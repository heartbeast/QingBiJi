package com.thinkernote.ThinkerNote.Adapter;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Data.TNPreferenceChild;
import com.thinkernote.ThinkerNote.Data.TNPreferenceGroup;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;

public class TNPreferenceAdapter extends BaseExpandableListAdapter {
	private Vector<TNPreferenceGroup> mGroups;
	private Context context;
	private LayoutInflater layoutInflater;
	
	public TNPreferenceAdapter(Context context, Vector<TNPreferenceGroup> groups){
		this.context = context;
		mGroups = groups;
		layoutInflater = LayoutInflater.from(context);
	}
	
	public int getGroupCount() {
		return mGroups.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mGroups.get(groupPosition).getChilds().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mGroups.get(groupPosition).getChilds().get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = (LinearLayout) layoutInflater.inflate(
						R.layout.preference_group, null);
		}
		((TextView)convertView.findViewById(R.id.group_name)).setText(((TNPreferenceGroup)getGroup(groupPosition)).getGroupName());
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = (LinearLayout) layoutInflater.inflate(
						R.layout.preference_child, null);
		}
		convertView.findViewById(R.id.child_layout).setBackgroundDrawable(
				TNUtilsSkin.getPreferenceItemStatusDrawable((Activity)context));
		
		TNPreferenceChild child = mGroups.get(groupPosition).getChilds().get(childPosition);
		if(child.getLogoId() > 0){
			convertView.findViewById(R.id.child_logo).setVisibility(View.VISIBLE);
			TNUtilsSkin.setImageViewDrawable((Activity)context, convertView, R.id.child_logo, child.getLogoId());
		}else
			convertView.findViewById(R.id.child_logo).setVisibility(View.GONE);
		
		((TextView)convertView.findViewById(R.id.child_name)).setText(child.getChildName());
		if(child.getInfo() == null)
			convertView.findViewById(R.id.child_info).setVisibility(View.GONE);
		else{
			convertView.findViewById(R.id.child_info).setVisibility(View.VISIBLE);
			((TextView)convertView.findViewById(R.id.child_info)).setText(child.getInfo());
		}
		if(child.isVisibleMoreBtn())
			convertView.findViewById(R.id.child_more).setVisibility(View.VISIBLE);
		else
			convertView.findViewById(R.id.child_more).setVisibility(View.INVISIBLE);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
