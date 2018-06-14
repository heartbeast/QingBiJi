package com.thinkernote.ThinkerNote.Activity;

import java.util.Vector;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.Toast;

import com.thinkernote.ThinkerNote.Activity.fragment.TNPageCats;
import com.thinkernote.ThinkerNote.Activity.fragment.TNPageNotes;
import com.thinkernote.ThinkerNote.Activity.fragment.TNPageTags;
import com.thinkernote.ThinkerNote.base.TNChildViewBase;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
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
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Other.HorizontalPager;
import com.thinkernote.ThinkerNote.Other.HorizontalPager.OnScreenSwitchListener;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * 主页--我的笔记
 */

public class TNPagerAct extends TNActBase implements OnScreenSwitchListener,
		OnClickListener {
	private HorizontalPager mPager;
	private Vector<TNChildViewBase> mChildPages;
	private TNChildViewBase mCurrChild;
	private ProgressDialog mProgressDialog;
	private TNSettings mSettings = TNSettings.getInstance();
	private TNNote mCurrNote;
	private TNCat mCurrCat;
	private TNTag mCurTag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_layout);
		TNAction.regResponder(TNActionType.SetDefaultFolderId, this, "respondSetDefaultFolderId");
		TNAction.regResponder(TNActionType.FolderDelete, this, "respondFolderDelete");
		TNAction.regResponder(TNActionType.TagDelete, this, "respondTagDelete");
		TNAction.regResponder(TNActionType.GetAllDataByNoteId, this, "respondGetAllDataByNoteId");
		TNAction.regResponder(TNActionType.SynchronizeCat, this, "respondSynchronizeCat");
		TNAction.regResponder(TNActionType.ClearRecycle, this, "respondNoteHandle");

		mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
		initAct();
	}

	private void initAct() {
		findViewById(R.id.table_home).setOnClickListener(this);
		findViewById(R.id.table_notes_newnote).setOnClickListener(this);
		findViewById(R.id.table_notes_search).setOnClickListener(this);
		findViewById(R.id.table_notes_sort).setOnClickListener(this);
		findViewById(R.id.table_cats_newfolder).setOnClickListener(this);
		findViewById(R.id.table_cats_newnote).setOnClickListener(this);
		findViewById(R.id.table_cats_serch).setOnClickListener(this);
		findViewById(R.id.table_cats_sort).setOnClickListener(this);
		findViewById(R.id.table_tags_newtag).setOnClickListener(this);
		findViewById(R.id.tablelayout_btn_page1).setOnClickListener(this);
		findViewById(R.id.tablelayout_btn_page2).setOnClickListener(this);
		findViewById(R.id.tablelayout_btn_page3).setOnClickListener(this);
		((RadioButton)findViewById(R.id.tablelayout_btn_page1)).setText(R.string.table_notes);
		((RadioButton)findViewById(R.id.tablelayout_btn_page2)).setText(R.string.table_cats);
		((RadioButton)findViewById(R.id.tablelayout_btn_page3)).setText(R.string.table_tags);
		
		mPager = (HorizontalPager) findViewById(R.id.tablelayout_horizontalPager);
		mPager.setOnScreenSwitchListener(this);

		mChildPages = new Vector<TNChildViewBase>();
		//
		TNPageNotes notesView = new TNPageNotes(this);
		mChildPages.add(notesView);
		//
		TNPageCats catsView = new TNPageCats(this);
		mChildPages.add(catsView);
		//
		TNPageTags tagsView = new TNPageTags(this);
		mChildPages.add(tagsView);

		for (int i = 0; i < mChildPages.size(); i++) {
			mPager.addView(mChildPages.get(i).mChildView);
		}
		int screen = 0;
		mPager.setCurrentScreen(screen, false);
		mCurrChild = mChildPages.get(screen);
		changeViewForScreen(0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.table_notes_newnote: {
			TNNote note = TNNote.newNote();
			Bundle b = new Bundle();
			b.putLong("NoteForEdit", note.noteLocalId);
			b.putSerializable("NOTE", note);
			startActivity(TNNoteEditAct.class, b);
			break;
		}
		case R.id.table_notes_search: {
			Bundle b = new Bundle();
			b.putInt("SearchType", 1);
			startActivity(TNSearchAct.class, b);
			break;
		}
		case R.id.table_notes_sort: {
			//笔记排序
			if (mSettings.sort == TNConst.CREATETIME) {
				mSettings.sort = TNConst.UPDATETIME;
				TNUtilsUi.showShortToast("按更新时间排序");
			} else {
				mSettings.sort = TNConst.CREATETIME;
				TNUtilsUi.showShortToast("按创建时间排序");
			}
			mSettings.savePref(false);
			((TNPageNotes) mCurrChild).isNewSortord = true;
			configView();
			break;
		}
		case R.id.table_cats_newfolder: {
			((TNPageCats) mCurrChild).newFolder();
			break;
		}
		case R.id.table_cats_newnote:
			((TNPageCats) mCurrChild).newNote();
			break;
		case R.id.table_cats_serch: {
			Bundle b = new Bundle();
			b.putInt("SearchType", 1);
			startActivity(TNSearchAct.class, b);
			break;
		}
		case R.id.table_cats_sort: {
			//文件夹排序
			if (mSettings.sort == TNConst.CREATETIME) {
				mSettings.sort = TNConst.UPDATETIME;
				TNUtilsUi.showShortToast("按更新时间排序");
			} else {
				mSettings.sort = TNConst.CREATETIME;
				TNUtilsUi.showShortToast("按创建时间排序");
			}
			mSettings.savePref(false);
			configView();
			break;
		}
		case R.id.table_tags_newtag: {
			Bundle b = new Bundle();
			b.putString("TextType", "tag_add");
			b.putString("TextHint", getString(R.string.textedit_tag));
			b.putString("OriginalText", "");
			startActivity(TNTextEditAct.class, b);
			break;
		}
		case R.id.table_home:
			finish();
			break;
		case R.id.tablelayout_btn_page1:
			if (mPager.getCurrentScreen() != 0) {
				mPager.setCurrentScreen(0, true);
			}
			break;
		case R.id.tablelayout_btn_page2:
			if (mPager.getCurrentScreen() != 1) {
				mPager.setCurrentScreen(1, true);
			}
			break;
		case R.id.tablelayout_btn_page3:
			if (mPager.getCurrentScreen() != 2) {
				mPager.setCurrentScreen(2, true);
			}
			break;
		//笔记相关的点击事件
		case R.id.notelistitem_menu_view: {
			mMenuBuilder.destroy();
			if(mCurrNote == null)
				break;
			Bundle b = new Bundle();
			int type = 0;
			if(mCurrChild.pageId == R.id.page_sharenotes){
				type = 1;
			}
			b.putInt("Type", type);
			b.putLong("NoteLocalId", mCurrNote.noteLocalId);
			startActivity(TNNoteViewAct.class, b);
			break;
		}
		case R.id.notelistitem_menu_changetag: {
			mMenuBuilder.destroy();
			if(mCurrNote == null)
				break;
			TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurrNote.noteLocalId);
			if (note.syncState != 2) {
				Toast.makeText(this,
						R.string.alert_NoteList_NotCompleted_ChangTag,
						Toast.LENGTH_SHORT).show();
				break;
			}
			Bundle b = new Bundle();
			b.putString("TagStrForEdit", note.tagStr);
			b.putLong("ChangeTagForNoteList", note.noteLocalId);
			startActivity(TNTagListAct.class, b);
			break;
		}
		
		case R.id.notelistitem_menu_edit: {
			mMenuBuilder.destroy();
			if(mCurrNote == null)
				break;
			if (mCurrNote.syncState == 2) {
				Bundle b = new Bundle();
				b.putLong("NoteForEdit", mCurrNote.noteLocalId);
				b.putLong("NoteLocalId", mCurrNote.noteLocalId);
				startActivity(TNNoteEditAct.class, b);
			} else {
				TNHandleError.handleErrorCode(this,
						this.getResources().getString(R.string.alert_NoteView_NotCompleted));
			}
			break;
		}
	
		case R.id.notelistitem_menu_moveto: {
			mMenuBuilder.destroy();
			if(mCurrNote == null)
				break;
			TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurrNote.noteLocalId);
			if (note.syncState == 1) {
				Toast.makeText(this, R.string.alert_NoteList_NotCompleted_Move,
						Toast.LENGTH_SHORT).show();
				break;
			}
			Bundle b = new Bundle();
			b.putLong("OriginalCatId", note.catId);
			b.putInt("Type", 1);
			b.putLong("ChangeFolderForNoteList", note.noteLocalId);
			startActivity(TNCatListAct.class, b);
			break;
		}
	
		case R.id.notelistitem_menu_sync: {
			mMenuBuilder.destroy();
			if(mCurrNote == null || mCurrNote.noteId == -1)
				break;
			TNUtilsDialog.synchronize(this, null, null,
					TNActionType.GetAllDataByNoteId, mCurrNote.noteId, "noteItem");//增加noteItem是为了防止完全同步文件夹时也会走到这个的响应函数导致的报错
			break;
		}
	
		case R.id.notelistitem_menu_info: {
			mMenuBuilder.destroy();
			if(mCurrNote == null)
				break;
			Bundle b = new Bundle();
			b.putLong("NoteLocalId", mCurrNote.noteLocalId);
			startActivity(TNNoteInfoAct.class, b);
			break;
		}
	
		case R.id.notelistitem_menu_delete: {
			mMenuBuilder.destroy();
			if(mCurrNote == null)
				break;
			TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurrNote.noteLocalId);
			TNUtilsDialog.deleteNote(this, new TNRunner(this, "deleteNoteCallBack"), note.noteLocalId);
			break;
		}
		
		case R.id.notelistitem_menu_cancel: {
			mMenuBuilder.destroy();
			break;
		}
		
		//文件夹相关的点击事件
		case R.id.folder_menu_sync: {
			mMenuBuilder.destroy();
			if(mCurrCat == null)
				break;
			TNUtilsDialog.synchronize(this, null, null,
					TNActionType.SynchronizeCat, mCurrCat.catId);
			break;
		}

		case R.id.folder_menu_rename: {
			mMenuBuilder.destroy();
			if(mCurrCat == null)
				break;
			Bundle b = new Bundle();
			b.putString("TextType", "cat_rename");
			b.putString("TextHint", getString(R.string.textedit_folder));
			b.putString("OriginalText", mCurrCat.catName);
			b.putLong("ParentId", mCurrCat.catId);
			startActivity(TNTextEditAct.class, b);
			break;
		}

		case R.id.folder_menu_moveto: {
			mMenuBuilder.destroy();
			if(mCurrCat == null)
				break;
			Bundle b = new Bundle();
			b.putLong("OriginalCatId", mCurrCat.pCatId);
			b.putInt("Type", 0);
			b.putLong("ChangeFolderForFolderList", mCurrCat.catId);
			startActivity(TNCatListAct.class, b);
			break;
		}

		case R.id.folder_menu_delete: {
			mMenuBuilder.destroy();
			if(mCurrCat == null)
				break;
			mProgressDialog.show();
			TNUtilsDialog.deleteCatDialog(this, new TNRunner(this,
					"dialogCB"), mCurrCat);
			break;
		}

		case R.id.folder_menu_info: {
			mMenuBuilder.destroy();
			if(mCurrCat == null)
				break;
			Bundle b = new Bundle();
			b.putLong("CatId", mCurrCat.catId);
			startActivity(TNCatInfoAct.class, b);
			break;
		}

		case R.id.folder_menu_setdefault: {
			mMenuBuilder.destroy();
			if(mCurrCat == null)
				break;
			setDefaultCatDialog(mCurrCat);
			break;
		}
		
		case R.id.folder_menu_recycle: {
			mMenuBuilder.destroy();
			TNUtilsDialog.RunActionDialog(this, new TNRunner(this, "clearRecycleCB"), 
					TNActionType.ClearLocalRecycle, false, false,
					R.string.alert_NoteList_ClearRecycle);
			break;
		}
		
		case R.id.folder_menu_cancel: {
			mMenuBuilder.destroy();
			break;
		}

		//标签相关的点击事件
		case R.id.tag_menu_display: {
			mMenuBuilder.destroy();
			if(mCurTag == null)
				break;
			Bundle b = new Bundle();
			b.putLong("UserId", mSettings.userId);
			b.putInt("ListType", 4);
			b.putLong("ListDetail", mCurTag.tagId);
			b.putInt("count", mCurTag.noteCounts);
			startActivity(TNNoteListAct.class, b);
			break;
		}

		case R.id.tag_menu_info: {
			mMenuBuilder.destroy();
			if(mCurTag == null)
				break;
			Bundle b = new Bundle();
			b.putLong("TagId", mCurTag.tagId);
			startActivity(TNTagInfoAct.class, b);
			break;
		}
		
		case R.id.tag_menu_delete: {
			mMenuBuilder.destroy();
			if(mCurTag == null)
				break;
			deleteTag(mCurTag);
			break;
		}
		
		case R.id.tag_menu_cancel: {
			mMenuBuilder.destroy();
			break;
		}
		}
	}

	@Override
	public void onScreenSwitched(int screen) {
		mCurrChild = mChildPages.get(screen);
		changeViewForScreen(screen);
	}

	private void changeViewForScreen(int screen) {
		switch (mCurrChild.pageId) {
		case R.id.page_notes: {
			findViewById(R.id.table_toolbar_layout_notes).setVisibility(
					View.VISIBLE);
			findViewById(R.id.table_toolbar_layout_cats).setVisibility(
					View.GONE);
			findViewById(R.id.table_toolbar_layout_tags).setVisibility(
					View.GONE);

			RadioButton rb = (RadioButton) findViewById(R.id.tablelayout_btn_page1);
			rb.setChecked(true);
			break;
		}

		case R.id.page_cats: {
			findViewById(R.id.table_toolbar_layout_notes).setVisibility(
					View.GONE);
			findViewById(R.id.table_toolbar_layout_cats).setVisibility(
					View.VISIBLE);
			findViewById(R.id.table_toolbar_layout_tags).setVisibility(
					View.GONE);
			RadioButton rb = (RadioButton) findViewById(R.id.tablelayout_btn_page2);
			rb.setChecked(true);
			configView();
			break;
		}

		case R.id.page_tags: {
			findViewById(R.id.table_toolbar_layout_notes).setVisibility(
					View.GONE);
			findViewById(R.id.table_toolbar_layout_cats).setVisibility(
					View.GONE);
			findViewById(R.id.table_toolbar_layout_tags).setVisibility(
					View.VISIBLE);
			RadioButton rb = (RadioButton) findViewById(R.id.tablelayout_btn_page3);
			rb.setChecked(true);
			configView();
			break;
		}
		}
	}
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.i(TAG, "onRestoreInstanceState");
		int screen = savedInstanceState.getInt("CurrentScreen");
		mPager.setCurrentScreen(screen, false);
	}	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i(TAG, "onSaveInstanceState");
		outState.putInt("CurrentScreen", mPager.getCurrentScreen());
		super.onSaveInstanceState(outState);
	}	

	public void addNoteMenu(int resource) {
		mCurrNote = (TNNote) mCurrChild.mBundle.get("currentNote");
		View view = addMenu(resource);
		view.findViewById(R.id.notelistitem_menu_view).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_edit).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_sync).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_delete).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_moveto).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_changetag).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_info).setOnClickListener(this);
		view.findViewById(R.id.notelistitem_menu_cancel).setOnClickListener(this);
	}
	
	public void addCatMenu(int resource) {
		mCurrCat = (TNCat) mCurrChild.mBundle.get("currentCat");
		View view = addMenu(resource);
		if (mCurrCat.catId == -1002) {
			view.findViewById(R.id.folder_menu_recycle).setOnClickListener(this);
		} else {
			view.findViewById(R.id.folder_menu_sync).setOnClickListener(this);
			view.findViewById(R.id.folder_menu_rename).setOnClickListener(this);
			if (mCurrCat.catId != mSettings.defaultCatId) {
				view.findViewById(R.id.folder_menu_moveto).setOnClickListener(this);
				view.findViewById(R.id.folder_menu_delete).setOnClickListener(this);
				view.findViewById(R.id.folder_menu_setdefault).setOnClickListener(this);
			}
			view.findViewById(R.id.folder_menu_info).setOnClickListener(this);
		}
		view.findViewById(R.id.folder_menu_cancel).setOnClickListener(this);
	}
	
	public void addTagMenu(int resource) {
		mCurTag = (TNTag) mCurrChild.mBundle.get("currentTag");
		View view = addMenu(resource);
		view.findViewById(R.id.tag_menu_display).setOnClickListener(this);
		view.findViewById(R.id.tag_menu_info).setOnClickListener(this);
		view.findViewById(R.id.tag_menu_delete).setOnClickListener(this);
		view.findViewById(R.id.tag_menu_cancel).setOnClickListener(this);
	}
	
	private void deleteTag(final TNTag tag){
		DialogInterface.OnClickListener pbtn_Click = 
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mProgressDialog.show();
					TNAction.runActionAsync(TNActionType.TagDelete, tag.tagId);
				}
			};
			
			JSONObject jsonData = TNUtils.makeJSON(
					"CONTEXT", this,
					"TITLE", R.string.alert_Title,
					"MESSAGE", R.string.alert_TagInfo_DeleteMsg,
					"POS_BTN", R.string.alert_OK,
					"POS_BTN_CLICK", pbtn_Click,
					"NEG_BTN", R.string.alert_Cancel
					);
			TNUtilsUi.alertDialogBuilder(jsonData).show();
	}
	
	private void setDefaultCatDialog(final TNCat cat) {
		DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				TNAction.runActionAsync(TNActionType.SetDefaultFolderId,
						 cat.catId);
				configView();
			}
		};

		JSONObject jsonData = TNUtils.makeJSON("CONTEXT", this, "TITLE",
				R.string.alert_Title, "MESSAGE",
				R.string.alert_CatInfo_SetDefaultMsg, "POS_BTN",
				R.string.alert_OK, "POS_BTN_CLICK", pbtn_Click, "NEG_BTN",
				R.string.alert_Cancel);
		TNUtilsUi.alertDialogBuilder(jsonData).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mCurrChild.pageId == R.id.page_cats) {
				if (((TNPageCats) mCurrChild).onKeyDown()) {
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void clearRecycleCB(){
		configView();
	}

	public void dialogCB() {
//		mProgressDialog.show();
		configView();
	}
	
	public void deleteNoteCallBack() {
		if (isInFront)
			configView();
	}

	@Override
	protected void configView() {
		for (TNChildViewBase child : mChildPages) {
//			if (child.pageId == mCurrChild.pageId)
				child.configView(createStatus);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		finish();
	}

	public void respondSetDefaultFolderId(TNAction aAction) {
		mProgressDialog.hide();
		TNHandleError.handleResult(this, aAction);
		if (isInFront) {
			configView();
		}
	}
	
	public void respondTagDelete(TNAction aAction) {
		mProgressDialog.hide();
		TNHandleError.handleResult(this, aAction);
		if (isInFront) {
			configView();
		}
	}
	
	public void respondFolderDelete(TNAction aAction) {
		mProgressDialog.hide();
		TNHandleError.handleResult(this, aAction);
		if (isInFront) {
			configView();
		}
	}
	
	public void respondGetAllDataByNoteId(TNAction aAction) {
		if (aAction.inputs.size() < 2) //完全同步文件夹时，只会传一个参数进来
			return;
		if (aAction.result == TNActionResult.Cancelled) {
			TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
		} else if (!TNHandleError.handleResult(this, aAction, false)) {
			TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
		} else {
			TNUtilsUi.showNotification(this, R.string.alert_Synchronize_Stoped, true);
		}
		configView();
	}
	
	public void respondSynchronizeCat(TNAction aAction) {
		if (aAction.result == TNActionResult.Cancelled) {
			TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
		} else if (!TNHandleError.handleResult(this, aAction, false)) {
			TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
			if (TNActionUtils.isSynchroniz(aAction)) {
				mSettings.originalSyncTime = System.currentTimeMillis();
				mSettings.savePref(false);
			}
		} else {
			TNUtilsUi.showNotification(this, 
					R.string.alert_Synchronize_Stoped, true);
		}
		configView();
	}
	
	public void respondNoteHandle(TNAction aAction){
		mProgressDialog.hide();
		if (!TNHandleError.handleResult(this, aAction)) {
			TNUtilsUi.showToast("回收站已清空");
			configView();
		}
	}

}
