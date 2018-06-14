package com.thinkernote.ThinkerNote.Data;

import java.util.Vector;

public class TNPreferenceGroup {
	String groupName;
	Vector<TNPreferenceChild> childs;
	
	public TNPreferenceGroup(){
		groupName = "";
		childs = new Vector<TNPreferenceChild>();
	}
	
	public TNPreferenceGroup(String groupName){
		this.groupName = groupName;
		childs = new Vector<TNPreferenceChild>();
	}
	
	public void addChild(TNPreferenceChild child){
		childs.add(child);
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Vector<TNPreferenceChild> getChilds() {
		return childs;
	}

	public void setChilds(Vector<TNPreferenceChild> childs) {
		this.childs = childs;
	}
	
}
