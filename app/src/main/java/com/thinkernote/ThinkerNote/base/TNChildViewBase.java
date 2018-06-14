package com.thinkernote.ThinkerNote.base;

import android.os.Bundle;
import android.view.View;

import com.thinkernote.ThinkerNote.Activity.TNPagerAct;

// fragment基类
public class TNChildViewBase {
	public int pageId;
	public TNPagerAct mActivity;
	public View mChildView;
	public Bundle mBundle;
	
	public TNChildViewBase() {
		mBundle = new Bundle();
	}
	
	public void configView(int createStatus){
		
	}
}
