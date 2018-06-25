package com.thinkernote.ThinkerNote._constructer.presenter;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.module.MainModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.RegistModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IMainModule;
import com.thinkernote.ThinkerNote._interface.m.IRegistModule;
import com.thinkernote.ThinkerNote._interface.p.IMainPresener;
import com.thinkernote.ThinkerNote._interface.p.IRegistPresener;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册 p层 具体实现
 */
public class MainPresenterImpl implements IMainPresener, OnMainListener {
    private Context context;
    private OnMainListener onView;

    //p层调用M层方法
    private IMainModule module;

    public MainPresenterImpl(Context context, OnMainListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new MainModuleImpl(context);
    }

    //============================p层重写，用于调用m层方法============================

    //更新检查
    @Override
    public void pUpgrade(String home) {
        module.mUpgrade(this);
    }

    //下载
    @Override
    public void pDownload(String url, Dialog dialog) {

    }

    //=第一次登录的同步方法=
    //1
    @Override
    public void folderAdd(int position, int arraySize, String folderName) {
        module.mfolderAdd(this, position, arraySize, folderName);
    }

    //2
    @Override
    public void tagAdd(int position, int arraySize, String tagName) {
        module.mTagAdd(this, position, arraySize, tagName);
    }

    //3
    @Override
    public void pGetFolder() {
        module.GetFolder(this);
    }


    //4
    @Override
    public void pGetFoldersByFolderId(long id, int position, List<AllFolderItemBean> beans) {
        module.mGetFoldersByFolderId(this, id, position, beans);
    }

    //5
    @Override
    public void pFirstFolderAdd(int workPos, int workSize, long catID, int catPos, int flag) {
        module.mFirstFolderAdd(this, workPos, workSize, catID, catPos, flag);
    }


    //=正常登录的同步方法=

    //2-1
    @Override
    public void pProfile() {
        module.mProfile(this);
    }


    //2-2
    @Override
    public void pUploadOldNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mUploadOldNotePic(this, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }


    //2-3
    @Override
    public void pOldNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mOldNoteAdd(this, position, arraySize, tnNote, isNewDb, content);
    }


    //2-4
    @Override
    public void pGetTagList() {
        module.mGetTagList(this);
    }

    //2-5
    @Override
    public void pNewNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mNewNotePic(this, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    //2-6
    @Override
    public void pNewNote(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mNewNote(this, position, arraySize, tnNote, isNewDb, content);
    }

    //2-7-1
    @Override
    public void pRecoveryNote(long noteID, int position, int arrySize) {
        module.mRecoveryNote(this, noteID, position, arrySize);
    }

    //2-7-2
    @Override
    public void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mRecoveryNotePic(this, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    //2-7-3
    @Override
    public void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mRecoveryNoteAdd(this, position, arraySize, tnNote, isNewDb, content);
    }

    //2-8
    @Override
    public void pDeleteNote(long noteId, int position) {
        module.mDeleteNote(this, noteId, position);
    }

    //2-9
    @Override
    public void pDeleteRealNotes(long noteId, int position) {
        module.mDeleteRealNotes(this, noteId, position);
    }

    //2-10
    @Override
    public void pGetAllNotesId() {
        module.mGetAllNotesId(this);
    }

    //2-11-1
    @Override
    public void pEditNote(int position, TNNote tnNote) {
        TNNote note = tnNote;
        String shortContent = TNUtils.getBriefContent(note.content);
        String content = note.content;
        ArrayList list = new ArrayList();
        int index1 = content.indexOf("<tn-media");
        int index2 = content.indexOf("</tn-media>");
        while (index1 >= 0 && index2 > 0) {
            String temp = content.substring(index1, index2 + 11);
            list.add(temp);
            content = content.replaceAll(temp, "");
            index1 = content.indexOf("<tn-media");
            index2 = content.indexOf("</tn-media>");
        }
        for (int i = 0; i < list.size(); i++) {
            String temp = (String) list.get(i);
            boolean isExit = false;
            for (TNNoteAtt att : note.atts) {
                String temp2 = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
                if (temp.equals(temp2)) {
                    isExit = true;
                }
            }
            if (!isExit) {
                note.content = note.content.replaceAll(temp, "");
            }
        }
        for (TNNoteAtt att : note.atts) {
            if (!TextUtils.isEmpty(att.path) && att.attId != -1) {
                String s1 = String.format("<tn-media hash=\"%s\" />", att.digest);
                String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", att.digest, att.attId);
                note.content = note.content.replaceAll(s1, s2);
                String s3 = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
                String s4 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", att.digest, att.attId);
                note.content = note.content.replaceAll(s3, s4);
            } else {
                TNAction action = TNAction.runAction(TNActionType.Upload, att.attName, att.path, att.attLocalId);
                if (action.outputs.get(0) instanceof String) {
                    continue;
                }
                JSONObject output = (JSONObject) action.outputs.get(0);
                if ((Integer) TNUtils.getFromJSON(output, "code") == 0) {
                    att.digest = (String) TNUtils.getFromJSON(output, "md5");
                    att.attId = (Long) TNUtils.getFromLongJSON(output, "id");
                    String s1 = String.format("<tn-media hash=\"%s\" />", att.digest);
                    String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", att.digest, att.attId);
                    note.content = note.content.replaceAll(s1, s2);
                } else {
                    continue;
                }
            }
        }

        if (note.catId == -1) {
            note.catId = TNSettings.getInstance().defaultCatId;
        }

        //m层
        module.mEditNote(this, position, note);

    }

    //2-11-2
    @Override
    public void pGetNoteByNoteId( int position,long noteId,boolean is12) {
        module.mGetNoteByNoteId(this, position,noteId,is12);
    }

    @Override
    public void pGetAllTrashNoteIds() {
        module.mGetAllTrashNoteIds(this);
    }

    //==========================接口结果回调==============================

    @Override
    public void onUpgradeSuccess(Object obj) {
        onView.onUpgradeSuccess(obj);
    }

    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        onView.onUpgradeFailed(msg, e);
    }

    //=第一次登录的回调==

    //1 创建folder
    @Override
    public void onSyncFolderAddSuccess(Object obj, int position, int arraySize) {
        onView.onSyncFolderAddSuccess(obj, position, arraySize);
    }

    @Override
    public void onSyncFolderAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncFolderAddFailed(msg, e, position, arraySize);
    }

    //2 创建tag
    @Override
    public void onSyncTagAddSuccess(Object obj, int position, int arraySize) {
        onView.onSyncTagAddSuccess(obj, position, arraySize);
    }

    @Override
    public void onSyncTagAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncTagAddFailed(msg, e, position, arraySize);
    }

    //3 getAllFolder
    @Override
    public void onSyncGetFolderSuccess(Object obj) {
        onView.onSyncGetFolderSuccess(obj);
    }

    @Override
    public void onSyncGetFolderFailed(String msg, Exception e) {
        onView.onSyncGetFolderFailed(msg, e);
    }

    //4
    @Override
    public void onSyncGetFoldersByFolderIdSuccess(Object obj, long id, int position, List<AllFolderItemBean> beans) {
        onView.onSyncGetFoldersByFolderIdSuccess(obj, id, position, beans);
    }

    @Override
    public void onSyncGetFoldersByFolderIdFailed(String msg, Exception e, long id, int position, List<AllFolderItemBean> beans) {
        onView.onSyncGetFoldersByFolderIdFailed(msg, e, id, position, beans);
    }

    //5 TNCat
    @Override
    public void onSyncFirstFolderAddSuccess(Object obj, int workPos, int workSize, long catID, int catPos, int flag) {
        onView.onSyncFirstFolderAddSuccess(obj, workPos, workSize, catID, catPos, flag);
    }

    @Override
    public void onSyncFirstFolderAddFailed(String msg, Exception e, int workPos, int workSize, long catID, int catPos, int flag) {
        onView.onSyncFirstFolderAddFailed(msg, e, workPos, workSize, catID, catPos, flag);
    }


    //===正常登录的回调=
    //2-1
    @Override
    public void onSyncProfileSuccess(Object obj) {
        onView.onSyncProfileSuccess(obj);
    }

    @Override
    public void onSyncProfileAddFailed(String msg, Exception e) {
        onView.onSyncProfileAddFailed(msg, e);
    }

    //2-2 pic
    @Override
    public void onSyncOldNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncOldNotePicSuccess(obj, picPos, picArry, notePos, noteArry);
    }

    @Override
    public void onSyncOldNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncOldNotePicFailed(msg, e, picPos, picArry, notePos, noteArry);
    }

    //2-3 oldNoteAdd
    @Override
    public void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        onView.onSyncOldNoteAddSuccess(obj, position, arraySize, isNewDb);
    }

    @Override
    public void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncOldNoteAddFailed(msg, e, position, arraySize);
    }

    //2-4
    @Override
    public void onSyncTagListSuccess(Object obj) {
        onView.onSyncTagListSuccess(obj);
    }

    @Override
    public void onSyncTagListAddFailed(String msg, Exception e) {
        onView.onSyncTagListAddFailed(msg, e);
    }

    //2-5
    @Override
    public void onSyncNewNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncNewNotePicSuccess(obj, picPos, picArry, notePos, noteArry);
    }

    @Override
    public void onSyncNewNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncNewNotePicFailed(msg, e, picPos, picArry, notePos, noteArry);


    }

    //2-6
    @Override
    public void onSyncNewNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        onView.onSyncNewNoteAddSuccess(obj, position, arraySize, isNewDb);
    }

    @Override
    public void onSyncNewNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncNewNoteAddFailed(msg, e, position, arraySize);
    }

    //2-7-1
    @Override
    public void onSyncRecoverySuccess(Object obj, long noteId, int position) {
        onView.onSyncRecoverySuccess(obj, noteId, position);
    }

    @Override
    public void onSyncRecoveryFailed(String msg, Exception e) {
        onView.onSyncRecoveryFailed(msg, e);
    }

    //2-7-2
    @Override
    public void onSyncRecoveryNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncRecoveryNotePicSuccess(obj, picPos, picArry, notePos, noteArry);
    }

    @Override
    public void onSyncRecoveryNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncRecoveryNotePicFailed(msg, e, picPos, picArry, notePos, noteArry);

    }

    //2-7-3
    @Override
    public void onSyncRecoveryNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        onView.onSyncRecoveryNoteAddSuccess(obj, position, arraySize, isNewDb);
    }

    @Override
    public void onSyncRecoveryNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncRecoveryNoteAddFailed(msg, e, position, arraySize);
    }

    //2-8
    @Override
    public void onSyncDeleteNoteSuccess(Object obj, long noteId, int position) {
        onView.onSyncDeleteNoteSuccess(obj, noteId, position);
    }

    @Override
    public void onSyncDeleteNoteFailed(String msg, Exception e) {
        onView.onSyncDeleteNoteFailed(msg, e);
    }

    //2-9-1
    @Override
    public void onSyncpDeleteRealNotes1Success(Object obj, long noteId, int position) {
        onView.onSyncpDeleteRealNotes1Success(obj, noteId, position);
    }

    @Override
    public void onSyncDeleteRealNotes1Failed(String msg, Exception e, int position) {
        onView.onSyncDeleteRealNotes1Failed(msg, e, position);
    }

    //2-9-2
    @Override
    public void onSyncDeleteRealNotes2Success(Object obj, long noteId, int position) {
        onView.onSyncDeleteRealNotes2Success(obj, noteId, position);
    }

    @Override
    public void onSyncDeleteRealNotes2Failed(String msg, Exception e, int position) {
        onView.onSyncDeleteRealNotes2Failed(msg, e, position);
    }

    //2-10
    @Override
    public void onSyncAllNotesIdSuccess(Object obj) {
        onView.onSyncAllNotesIdSuccess(obj);
    }

    @Override
    public void onSyncAllNotesIdAddFailed(String msg, Exception e) {
        onView.onSyncAllNotesIdAddFailed(msg, e);
    }

    //2-11-1
    @Override
    public void onSyncEditNoteSuccess(Object obj, int position, TNNote note) {
        onView.onSyncEditNoteSuccess(obj, position, note);
    }

    @Override
    public void onSyncEditNoteAddFailed(String msg, Exception e) {
        onView.onSyncEditNoteAddFailed(msg, e);
    }

    //2-11-2
    @Override
    public void onSyncpGetNoteByNoteIdSuccess(Object obj, int position,boolean is12) {
        onView.onSyncpGetNoteByNoteIdSuccess(obj, position,is12);
    }

    @Override
    public void onSyncpGetNoteByNoteIdFailed(String msg, Exception e) {
        onView.onSyncpGetNoteByNoteIdFailed(msg, e);
    }

    //2-12
    @Override
    public void onSyncpGetAllTrashNoteIdsSuccess(Object obj) {
        onView.onSyncpGetAllTrashNoteIdsSuccess(obj);
    }

    @Override
    public void onSyncpGetAllTrashNoteIdsFailed(String msg, Exception e) {
        onView.onSyncpGetAllTrashNoteIdsFailed(msg, e);
    }


}
