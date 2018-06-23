package com.thinkernote.ThinkerNote._interface.v;

import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import java.util.List;

public interface OnMainListener {
    void onUpgradeSuccess(Object obj);

    void onUpgradeFailed(String msg, Exception e);

    //第一次登录同步1
    //1
    void onSyncFolderAddSuccess(Object obj, int position, int arraySize);

    void onSyncFolderAddFailed(String msg, Exception e, int position, int arraySize);

    //2
    void onSyncTagAddSuccess(Object obj, int position, int arraySize);

    void onSyncTagAddFailed(String msg, Exception e, int position, int arraySize);

    //3
    void onSyncGetFolderSuccess(Object obj);

    void onSyncGetFolderFailed(String msg, Exception e);

    //4
    void onSyncGetFoldersByFolderIdSuccess(Object obj, long catID, int startPos, List<AllFolderItemBean> beans);

    void onSyncGetFoldersByFolderIdFailed(String msg, Exception e, long catID, int posstartPosition, List<AllFolderItemBean> beans);

    //5
    void onSyncFirstFolderAddSuccess(Object obj, int workPos, int workSize, long catID, int catPos, int flag);

    void onSyncFirstFolderAddFailed(String msg, Exception e, int workPos, int workSize, long catID, int catPos, int flag);


    //正常同步2


    //1
    void onSyncProfileSuccess(Object obj);

    void onSyncProfileAddFailed(String msg, Exception e);

    //2
    void onSyncOldNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry);

    void onSyncOldNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry);

    //3
    void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb);

    void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize);


    //4
    void onSyncTagListSuccess(Object obj);

    void onSyncTagListAddFailed(String msg, Exception e);

    //5
    void onSyncNewNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry);

    void onSyncNewNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry);

    //6
    void onSyncNewNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb);

    void onSyncNewNoteAddFailed(String msg, Exception e, int position, int arraySize);


}
