package com.thinkernote.ThinkerNote.Activity;

import java.util.Vector;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.Adapter.TNNotesAdapter;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnRefreshListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshListView;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * 重要的类
 *
 * //1 allNote, 2 cat, 3 recycle, 4 tag, 5 serch, 7 个人公开, 8  他人公开
 * 5--搜索结果 展示界面
 *
 */
public class TNNoteListAct extends TNActBase implements OnClickListener, OnItemLongClickListener,
OnLastItemVisibleListener, OnRefreshListener, OnItemClickListener {

	/*
	 * Bundle: ListType ListDetail
	 */

	private PullToRefreshListView mPullListview;
	private ListView mListView;
	private Vector<TNNote> mNotes;
	private long mCurNoteId;
	private TNNote mCurNote;
	private float mScale;
	private ProgressDialog mProgressDialog;
	private LinearLayout mLoadingView;

	private int mListType; //1 allNote, 2 cat, 3 recycle, 4 tag, 5 serch, 7 个人公开, 8  他人公开
	private long mListDetail;
	private String mKeyWord;

	private TNTag mTag;
	private TNCat mCat;
	
	private TNSettings mSettings = TNSettings.getInstance();
	
	private TNNotesAdapter mNotesAdapter = null;
	private int mCount;
	private int mPageNum = 1;

	// Activity methods
	// -------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notelist);
		
		setViews();
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScale = metric.scaledDensity;
		
		mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);

		// register action
		TNAction.regResponder(TNActionType.GetNoteListByTagId, this, "respondGetNoteList");
		TNAction.regResponder(TNActionType.GetNoteListByFolderId, this, "respondGetNoteList");
		TNAction.regResponder(TNActionType.SynchronizeEdit, this, "respondSynchronizeEdit");
		TNAction.regResponder(TNActionType.Synchronize, this, "respondSynchronize");
		TNAction.regResponder(TNActionType.GetNoteListBySearch, this, "respondGetNoteListBySearch");
		TNAction.regResponder(TNActionType.GetAllData, this, "respondGetAllData");
		TNAction.regResponder(TNActionType.GetAllDataByNoteId, this, "respondGetAllDataByNoteId");

		// initialize
		Bundle b = getIntent().getExtras();
		mListType = b.getInt("ListType", 0);
		mCount = b.getInt("count", 0);
		if (mListType == 5) {
			mKeyWord = b.getString("ListDetail");
			findViewById(R.id.notelist_search).setVisibility(View.GONE);
		} else {
			mListDetail = b.getLong("ListDetail", -1);
		}
		
		if (mListType == 3) {
			findViewById(R.id.ll_clearrecycler).setVisibility(View.VISIBLE);
			findViewById(R.id.maincats_menu_clearrecycler).setOnClickListener(this);
		}
		
		mNotes = new Vector<TNNote>();
		mPullListview = (PullToRefreshListView) findViewById(R.id.notelist_list);
		mListView = mPullListview.getRefreshableView();
		mLoadingView = (LinearLayout) TNUtilsUi.addListHelpInfoFootView(this, mListView, TNUtilsUi.getFootViewTitle(this, mListType), TNUtilsUi.getFootViewInfo(this, mListType));
		mNotesAdapter = new TNNotesAdapter(this, mNotes, mScale);
		mListView.setAdapter(mNotesAdapter);

		mListView.setOnItemLongClickListener(this);
		mListView.setOnItemClickListener(this);
		mPullListview.setOnRefreshListener(this);
		mPullListview.setOnLastItemVisibleListener(this);
	}

	@Override
	public void onDestroy() {
		mProgressDialog.dismiss();
		super.onDestroy();
	}

	@Override
	protected void setViews() {
		TNUtilsSkin.setViewBackground(this, null, R.id.maincats_toolbar_layout, R.drawable.toolbg);
		TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.notelist_newnote, R.drawable.newnote);
		TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.notelist_search, R.drawable.search);
		TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.notelist_sort, R.drawable.sort);
		TNUtilsSkin.setViewBackground(this, null, R.id.notelist_page_bg, R.drawable.page_bg);

		findViewById(R.id.notelist_home).setOnClickListener(this);
		findViewById(R.id.notelist_folder).setOnClickListener(this);
		findViewById(R.id.notelist_newnote).setOnClickListener(this);
		findViewById(R.id.notelist_search).setOnClickListener(this);
		findViewById(R.id.notelist_sort).setOnClickListener(this);

		registerForContextMenu(findViewById(R.id.notelist_menu));
		registerForContextMenu(findViewById(R.id.notelist_recyclermenu));
		registerForContextMenu(findViewById(R.id.notelist_itemmenu));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		Bundle b = getIntent().getExtras();
		mListType = b.getInt("ListType", 0);
		mListDetail = b.getLong("ListDetail", -1);
	}

	@Override
	protected void configView() {
		if (createStatus == 0 && TNUtils.isNetWork()) {
			mPullListview.setRefreshing();
			requestData();
		} else {
			getNativeData();
		}
		
	}
	
	public void clearRecycleCB(){
		configView();
		if (TNUtils.isNetWork()) {
			if (TNActionUtils.isSynchronizing()) {
				return;
			}
			TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
			TNAction.runActionAsync(TNActionType.SynchronizeEdit);
		}
	}
	
	public void dialogCB() {
		configView();
		if (TNUtils.isNetWork()) {
			if (TNActionUtils.isSynchronizing()) {
				return;
			}
			TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
			TNAction.runActionAsync(TNActionType.SynchronizeEdit);
		}
	}

	// Implement OnClickListener
	// -------------------------------------------------------------------------------
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.maincats_menu_clearrecycler:{
			TNUtilsDialog.RunActionDialog(this, new TNRunner(this, "clearRecycleCB"), 
					TNActionType.ClearLocalRecycle, false, false,
					R.string.alert_NoteList_ClearRecycle);
			break;
		}
		case R.id.notelist_search:{
			Bundle b = new Bundle();
			b.putInt("SearchType", 1);
			startActivity(TNSearchAct.class, b);
			break;
		}
		case R.id.notelist_sort:{
			//排序
			if (mSettings.sort == TNConst.CREATETIME) {
				mSettings.sort = TNConst.UPDATETIME;
				TNUtilsUi.showShortToast("按更新时间排序");
			} else {
				mSettings.sort = TNConst.CREATETIME;
				TNUtilsUi.showShortToast("按创建时间排序");
			}
			mSettings.savePref(false);
			if (TNUtils.isNetWork()) {
				mPullListview.setRefreshing();
				mPageNum = 1;
				requestData();
			} else {
				getNativeData();
			}
			break;
		}
		
		case R.id.notelist_home:{
			Log.d(TAG, "want to go home...");
			finish();
			break;
		}

		case R.id.notelist_folder:{
			if (mListType == 2) {
				Bundle b = new Bundle();
				b.putLong("CatId", Long.valueOf(mListDetail));
				startActivity(TNCatInfoAct.class, b);
			} else if (mListType == 4) {
				Bundle b = new Bundle();
				b.putLong("TagId", Long.valueOf(mListDetail));
				startActivity(TNTagInfoAct.class, b);
			}
			break;
		}

		case R.id.notelist_newnote: {
			TNNote note = TNNote.newNote();
			if(mListType == 1){
				note.catId = mSettings.defaultCatId;
			}else if (mListType == 2) {
				note.catId = mCat.catId;
			} else if (mListType == 4) {
				note.tagStr = mTag.tagName;
			}
			Bundle b = new Bundle();
			b.putLong("NoteForEdit", note.noteLocalId);
			b.putSerializable("NOTE", note);
			startActivity(TNNoteEditAct.class, b);
			break;
		}
		
		case R.id.recycler_menu_restore: {
			mMenuBuilder.destroy();
			TNUtilsDialog.restoreNote(this, new TNRunner(this, "dialogCB"), mCurNoteId);
			break;
		}

		case R.id.recycler_menu_delete:{
			mMenuBuilder.destroy();
			TNUtilsDialog.realDeleteNote(this, new TNRunner(this, "dialogCB"), mCurNoteId);
			break;
		}

		case R.id.recycler_menu_view: {
			mMenuBuilder.destroy();
			Bundle b = new Bundle();
			b.putLong("NoteLocalId", mCurNoteId);
			startActivity(TNNoteViewAct.class, b);
			break;
		}
		
		case R.id.recycler_menu_cancel: {
			mMenuBuilder.destroy();
			break;
		}

		case R.id.notelistitem_menu_view: {
			mMenuBuilder.destroy();
			Bundle b = new Bundle();
			b.putLong("NoteLocalId", mCurNoteId);
			startActivity(TNNoteViewAct.class, b);
			break;
		}

		case R.id.notelistitem_menu_edit: {
			mMenuBuilder.destroy();
			TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurNoteId);
			if (note.trash == 1) {
				TNUtilsDialog.restoreNote(this, new TNRunner(this, "dialogCB"), note.noteLocalId);
			} else {
				if (note.syncState == 2) {
					Bundle b = new Bundle();
					b.putLong("NoteForEdit", note.noteLocalId);
					b.putLong("NoteLocalId", note.noteLocalId);
					startActivity(TNNoteEditAct.class, b);
				} else {
					TNHandleError.handleErrorCode(this,
							this.getResources().getString(R.string.alert_NoteView_NotCompleted));
				}
			}
			break;
		}

		case R.id.notelistitem_menu_changetag: {
			mMenuBuilder.destroy();
			TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurNoteId);
			if(note.syncState != 2){
				TNHandleError.handleErrorCode(this,
						this.getResources().getString(R.string.alert_NoteList_NotCompleted_ChangTag));
				break;
			}
			Bundle b = new Bundle();
			b.putString("TagStrForEdit", note.tagStr);
			b.putLong("ChangeTagForNoteList", note.noteLocalId);
			startActivity(TNTagListAct.class, b);
			break;
		}

		case R.id.notelistitem_menu_moveto:{
			mMenuBuilder.destroy();
			TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurNoteId);
			if(note.syncState != 2){
				TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Move);
				break;
			}
			Bundle b = new Bundle();
			b.putLong("OriginalCatId", note.catId);
			b.putInt("Type", 1);
			b.putLong("ChangeFolderForNoteList", note.noteLocalId);
			startActivity(TNCatListAct.class, b);
			break;
		}

		case R.id.notelistitem_menu_sync:{
			mMenuBuilder.destroy();
			TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurNoteId);
			if (note.noteId == -1) {
				break;
			}
			TNUtilsDialog.synchronize(this, null, null,
					TNActionType.GetAllDataByNoteId, note.noteId);
			break;
		}

		case R.id.notelistitem_menu_info: {
			mMenuBuilder.destroy();
			Bundle b = new Bundle();
			b.putLong("NoteLocalId", mCurNoteId);
			startActivity(TNNoteInfoAct.class, b);
			break;
		}

		case R.id.notelistitem_menu_delete:{
			mMenuBuilder.destroy();
			TNUtilsDialog.deleteNote(this, new TNRunner(this, "dialogCB"), mCurNoteId);
			break;
		}
		
		case R.id.notelist_menu_cancel:
			mMenuBuilder.destroy();
			break;
		}
	}

	private void setButtonsAndNoteList() {
		Log.i(TAG, "setButtons " + mNotes);
		String title = null;
		switch (mListType) {
		case 1:
			title = getString(R.string.notelist_allnote);
			findViewById(R.id.notelist_newnote).setVisibility(View.INVISIBLE);
			break;
		case 2:
			mCat = TNDbUtils.getCat(mListDetail);
			title = mCat.catName;
			break;
		case 3:
			title = getString(R.string.notelist_recycler);
			findViewById(R.id.notelist_newnote).setVisibility(View.INVISIBLE);
			findViewById(R.id.notelist_search).setVisibility(View.INVISIBLE);
			break;
		case 4:
			mTag = TNDbUtils.getTag(mListDetail);
			title = mTag.tagName;
			break;
		case 5:
			title = getString(R.string.notelist_search_result);
			findViewById(R.id.notelist_newnote).setVisibility(View.INVISIBLE);
			break;
		}

		Button folderBtn = (Button) findViewById(R.id.notelist_folder);
		((TextView) findViewById(R.id.notelist_home)).setText(title);
		folderBtn.setText(String.format("%s(%d)", title, mNotes.size()));
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		if (position > 0) {
			mCurNote = mNotes.get(position-1);
			mCurNoteId = mCurNote.noteLocalId;
			if (mListType == 3) {
				addRecycleMenu();
			} else {
				Bundle b = new Bundle();
				b.putLong("NoteLocalId", mCurNoteId);
				startActivity(TNNoteViewAct.class, b);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		if (position > 0) {
			mCurNote = mNotes.get(position-1);
			
			mCurNoteId = mCurNote.noteLocalId;
			if (mListType == 3) {
				addRecycleMenu();
			} else {
				addItemMenu();
			}
		}
		return true;
	}
	
	private void addItemMenu() {
		View view = addMenu(R.layout.menu_notelistitem);
		view.findViewById(R.id.notelistitem_menu_view).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_edit).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_sync).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_delete).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_moveto).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_changetag).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_info).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_cancel).setOnClickListener(this);
	}
	
	private void addRecycleMenu() {
		View view = addMenu(R.layout.menu_recycler);
		view.findViewById(R.id.recycler_menu_restore).setOnClickListener(this);
		view.findViewById(R.id.recycler_menu_delete).setOnClickListener(this);
		view.findViewById(R.id.recycler_menu_view).setOnClickListener(this);
		view.findViewById(R.id.recycler_menu_cancel).setOnClickListener(this);
	}

	@Override
	public void onRefresh() {
		if (TNActionUtils.isSynchronizing()) {
			TNUtilsUi.showNotification(this, R.string.alert_Synchronize_TooMuch, false);
			return;
		}
		mPageNum = 1;
		requestData();
	}
	
	@Override
	public void onLastItemVisible() {
		if (mPageNum != 1) {
			mLoadingView.setVisibility(View.VISIBLE);
			requestData();
		}
	}
	
	private void getNativeData() {
		if (mListType == 5) {
			TNAction.runActionAsync(TNActionType.GetNoteListBySearch, mKeyWord);
		} else {
			switch (mListType) {
			case 2:
				mNotes = TNDbUtils.getNoteListByCatId(mSettings.userId, mListDetail, mSettings.sort, TNConst.MAX_PAGE_SIZE);		
				break;
			case 3:
				mNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, mSettings.sort);
				break;
			case 4:
				mTag = TNDbUtils.getTag(mListDetail);
				mNotes = TNDbUtils.getNoteListByTagName(mSettings.userId, mTag.tagName, mSettings.sort, TNConst.MAX_PAGE_SIZE);
				break;
			}
			
			mNotesAdapter.updateNotes(mNotes);
			mNotesAdapter.notifyDataSetChanged();
		}
		setButtonsAndNoteList();
	}
	
	private void requestData() {
		switch (mListType) {
		case 2:
			TNAction.runActionAsync(TNActionType.GetNoteListByFolderId, mListDetail, mPageNum, TNConst.PAGE_SIZE, mSettings.sort);		
			break;
		case 3:
			if (mPageNum == 1)
				TNAction.runActionAsync(TNActionType.Synchronize, "Trash");
			break;
		case 4:
			TNAction.runActionAsync(TNActionType.GetNoteListByTagId, mListDetail, mPageNum, TNConst.PAGE_SIZE, mSettings.sort);
			break;
		case 5:
			TNAction.runActionAsync(TNActionType.GetNoteListBySearch, mKeyWord);
			break;
		}
	}
	
	public void dialogCallBackSyncCancell(){
		mPullListview.onRefreshComplete();
	}
	
	private void notifyData(TNAction aAction) {
		JSONObject outputs = (JSONObject) aAction.outputs.get(0);
		mPageNum = (Integer) TNUtils.getFromJSON(outputs, "pagenum");
		mCount = (Integer) TNUtils.getFromJSON(outputs, "count");
		
		if (mCount > mPageNum*TNConst.PAGE_SIZE) {
			mPageNum++;
		}
		
		switch (mListType) {
		case 2:
			mNotes = TNDbUtils.getNoteListByCatId(mSettings.userId, mListDetail, mSettings.sort, TNConst.PAGE_SIZE*mPageNum);		
			break;
		case 4:
			mTag = TNDbUtils.getTag(mListDetail);
			mNotes = TNDbUtils.getNoteListByTagName(mSettings.userId, mTag.tagName, mSettings.sort, TNConst.PAGE_SIZE*mPageNum);
			break;
		}
		
		mNotesAdapter.updateNotes(mNotes);
		mNotesAdapter.notifyDataSetChanged();
		
		setButtonsAndNoteList();
	}

	// respond Action
	// -------------------------------------------------------------------------------
	public void respondGetNoteList(TNAction aAction) {
		mLoadingView.setVisibility(View.GONE);
		mPullListview.onRefreshComplete();
		if (!TNHandleError.handleResult(this, aAction)) {
			notifyData(aAction);
		}
	}

	public void respondSynchronize(TNAction aAction) {
		mLoadingView.setVisibility(View.GONE);
		mPullListview.onRefreshComplete();
		if (!TNHandleError.handleResult(this, aAction)) {
			mNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, mSettings.sort);
			mNotesAdapter.updateNotes(mNotes);
			mNotesAdapter.notifyDataSetChanged();
			setButtonsAndNoteList();
		}
	}
	
//	public void respondNoteHandle(TNAction aAction){
//		mProgressDialog.hide();
//		if (!TNHandleError.handleResult(this, aAction)) {
//			configView();
//		}
//	}
	
	@SuppressWarnings("unchecked")
	public void respondGetNoteListBySearch(TNAction aAction) {
		mPullListview.onRefreshComplete();
		mLoadingView.setVisibility(View.GONE);
		mNotes = (Vector<TNNote>) aAction.outputs.get(0);
		mNotesAdapter.updateNotes(mNotes);
		mNotesAdapter.notifyDataSetChanged();
		setButtonsAndNoteList();
	}
	
	public void respondGetAllData(TNAction aAction) {
		if (aAction.result == TNActionResult.Cancelled) {
			TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
		} else if (!TNHandleError.handleResult(this, aAction, false)) {
			TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
			if (TNActionUtils.isSynchroniz(aAction)) {
				TNSettings settings = TNSettings.getInstance();
				settings.originalSyncTime = System.currentTimeMillis();
				settings.savePref(false);
			}
		} else {
			TNUtilsUi.showNotification(this, 
					R.string.alert_Synchronize_Stoped, true);
		}
	}
	
	public void respondGetAllDataByNoteId(TNAction aAction) {
		if (aAction.result == TNActionResult.Cancelled) {
			TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
		} else if (!TNHandleError.handleResult(this, aAction, false)) {
			TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
		} else {
			TNUtilsUi.showNotification(this, 
					R.string.alert_Synchronize_Stoped, true);
		}
	}

	public void respondSynchronizeEdit(TNAction aAction) {
		if (aAction.result == TNActionResult.Cancelled) {
			TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
		} else if (!TNHandleError.handleResult(this, aAction, false)) {
			TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
		} else {
			TNUtilsUi.showNotification(this,
					R.string.alert_Synchronize_Stoped, true);
		}
	}

}
