package com.thinkernote.ThinkerNote._interface.v;

public interface OnMainListener {
    void onUpgradeSuccess(Object obj);

    void onUpgradeFailed(String msg, Exception e);

    //第一次登录同步1
    void onSyncFolderAddSuccess(Object obj, int position, int arraySize);

    void onSyncFolderAddFailed(String msg, Exception e, int position, int arraySize);

    void onSyncTagAddSuccess(Object obj, int position, int arraySize);

    void onSyncTagAddFailed(String msg, Exception e, int position, int arraySize);

    //正常同步2
    void onSyncOldNotePicSuccess(Object obj, int picPos, int picArry,int notePos,int noteArry);

    void onSyncOldNotePicFailed(String msg, Exception e, int picPos, int picArry,int notePos,int noteArry);

    void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb);

    void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize);

    void onSyncProfileSuccess(Object obj);

    void onSyncProfileAddFailed(String msg, Exception e);

}
