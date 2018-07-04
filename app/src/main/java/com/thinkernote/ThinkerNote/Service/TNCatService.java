package com.thinkernote.ThinkerNote.Service;


import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;

import org.json.JSONArray;
import org.json.JSONObject;

public class TNCatService {
    private static final String TAG = "TNCatService";

    private static TNCatService singleton = null;

    private TNCatService() {
        MLog.d(TAG, "TNCatService()");
        TNAction.regRunner(TNActionType.GetAllFolders, this, "GetAllFolders");
        TNAction.regRunner(TNActionType.FolderAdd, this, "FolderAdd");
        TNAction.regRunner(TNActionType.FolderEdit, this, "FolderEdit");
        TNAction.regRunner(TNActionType.FolderDelete, this, "FolderDelete");
        TNAction.regRunner(TNActionType.GetParentFolders, this, "GetParentFolders");
        TNAction.regRunner(TNActionType.GetFoldersByFolderId, this, "GetFoldersByFolderId");
        TNAction.regRunner(TNActionType.SetDefaultFolderId, this, "SetDefaultFolderId");
        TNAction.regRunner(TNActionType.FolderMoveTo, this, "FolderMoveTo");
    }

    public static TNCatService getInstance() {
        if (singleton == null) {
            synchronized (TNCatService.class) {
                if (singleton == null) {
                    singleton = new TNCatService();
                }
            }
        }

        return singleton;
    }

    //TODO
    public void GetAllFolders(TNAction aAction) {

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/folders", null, TNActionType.GetParentFolders);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            //区别GetParentFolders（TNAction aAction）
            DGGetFolders(outputs);

            aAction.finished();
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void GetParentFolders(TNAction aAction) {

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/folders", null, TNActionType.GetParentFolders);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }


    //TODO 递归调用
    private void DGGetFolders(JSONObject outputs) {
        JSONArray foldersObj = (JSONArray) TNUtils.getFromJSON(outputs, "folders");

        for (int i = 0; i < foldersObj.length(); i++) {
            JSONObject obj = (JSONObject) TNUtils.getFromJSON(foldersObj, i);
            int folderCount = Integer.valueOf(TNUtils.getFromJSON(obj, "folder_count").toString());
            if (folderCount == 0) {
                continue;
            }

            TNAction action = TNAction.runAction(TNActionType.GetFoldersByFolderId, TNUtils.getFromJSON(obj, "id"));
            //新接口返回
            if (action.outputs.get(0) instanceof String) {
                continue;
            }

            JSONObject output = (JSONObject) action.outputs.get(0);
            if ((Integer) TNUtils.getFromJSON(output, "code") == 0) {
                DGGetFolders(output);
            }
        }
    }

    //TODO
    public void FolderAdd(TNAction aAction) {
        long pid = (Long) aAction.inputs.get(0);
        String name = (String) aAction.inputs.get(1);
        JSONObject jsonData = null;
        if (pid == -1) {
            jsonData = TNUtils.makeJSON(
                    "name", name);
        } else {
            jsonData = TNUtils.makeJSON(
                    "pid", pid,
                    "name", name);
        }

        aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/folders", jsonData, TNActionType.FolderAdd);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void FolderDelete(TNAction aAction) {
        long folder_id = (Long) aAction.inputs.get(0);
        JSONObject jsonData = TNUtils.makeJSON(
                "folder_id", folder_id);

        aAction.runChildAction(TNActionType.TNOpenUrl, "DELETE", "api/folders", jsonData, TNActionType.FolderDelete);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNDb.beginTransaction();
            try {
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.CAT_DELETE_CAT,
                        folder_id);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_TRASH_CATID,
                        2,
                        System.currentTimeMillis() / 1000,
                        TNSettings.getInstance().defaultCatId,
                        folder_id);

                TNDb.setTransactionSuccessful();
            } finally {
                TNDb.endTransaction();
            }
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void FolderEdit(TNAction aAction) {
        long folder_id = (Long) aAction.inputs.get(0);
        String name = (String) aAction.inputs.get(1);
        JSONObject jsonData = TNUtils.makeJSON(
                "folder_id", folder_id,
                "name", name);

        aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/folders", jsonData, TNActionType.FolderEdit);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNDb.beginTransaction();
            try {
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.CAT_RENAME,
                        name,
                        folder_id);

                TNDb.setTransactionSuccessful();
            } finally {
                TNDb.endTransaction();
            }
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void GetFoldersByFolderId(TNAction aAction) {
        JSONObject jsonData = TNUtils.makeJSON(
                "folder_id", aAction.inputs.get(0));
        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/folders", jsonData, TNActionType.GetFoldersByFolderId);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }


    //TODO
    public void SetDefaultFolderId(TNAction aAction) {
        long folderId = Long.valueOf(aAction.inputs.get(0).toString());
        JSONObject jsonData = TNUtils.makeJSON(
                "folder_id", aAction.inputs.get(0));
        aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/folders/default", jsonData, TNActionType.SetDefaultFolderId);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNSettings settings = TNSettings.getInstance();
            settings.defaultCatId = folderId;
            settings.savePref(false);
            aAction.finished(folderId);
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void FolderMoveTo(TNAction aAction) {
        JSONObject jsonData = TNUtils.makeJSON(
                "folder_id", aAction.inputs.get(0),
                "parent_id", aAction.inputs.get(1));
        aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/folders/move", jsonData, TNActionType.FolderMoveTo);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            aAction.finished();
        } else {
            aAction.failed(outputs);
        }
    }

}
