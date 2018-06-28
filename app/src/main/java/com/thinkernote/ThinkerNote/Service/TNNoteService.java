package com.thinkernote.ThinkerNote.Service;

import android.text.TextUtils;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

public class TNNoteService {
    private static final String TAG = "TNNoteService";

    private static TNNoteService singleton = null;

    private TNNoteService() {
        MLog.d(TAG, "TNNoteService()");
        TNAction.regRunner(TNActionType.NoteAdd, this, "NoteAdd");
        TNAction.regRunner(TNActionType.NoteEdit, this, "NoteEdit");
        TNAction.regRunner(TNActionType.NoteDelete, this, "NoteDelete");
        TNAction.regRunner(TNActionType.NoteRecovery, this, "NoteRecovery");
        TNAction.regRunner(TNActionType.NoteRealDelete, this, "NoteRealDelete");
        TNAction.regRunner(TNActionType.GetNoteList, this, "GetNoteList");
        TNAction.regRunner(TNActionType.GetNoteListByFolderId, this, "GetNoteListByFolderId");
        TNAction.regRunner(TNActionType.GetNoteByNoteId, this, "GetNoteByNoteId");
        TNAction.regRunner(TNActionType.GetTrashNoteByNoteId, this, "GetTrashNoteByNoteId");
        TNAction.regRunner(TNActionType.GetNoteListByTrash, this, "GetNoteListByTrash");
        TNAction.regRunner(TNActionType.ClearNotesByTrash, this, "ClearNotesByTrash");
        TNAction.regRunner(TNActionType.NoteMoveTo, this, "NoteMoveTo");
        TNAction.regRunner(TNActionType.NoteChangeTag, this, "NoteChangeTag");
        TNAction.regRunner(TNActionType.NoteChangeCreateTime, this, "NoteChangeCreateTime");
        TNAction.regRunner(TNActionType.GetNoteListByTagId, this, "GetNoteListByTagId");
        TNAction.regRunner(TNActionType.GetNoteListBySearch, this, "GetNoteListBySearch");
        TNAction.regRunner(TNActionType.ClearRecycle, this, "ClearRecycle");
        TNAction.regRunner(TNActionType.GetAllNoteIds, this, "GetAllNoteIds");
        TNAction.regRunner(TNActionType.GetAllTrashNoteIds, this, "GetAllTrashNoteIds");
        TNAction.regRunner(TNActionType.GetFolderNoteIds, this, "GetFolderNoteIds");

        TNAction.regRunner(TNActionType.Upload, this, "Upload");
    }

    public static TNNoteService getInstance() {
        if (singleton == null) {
            synchronized (TNUserService.class) {
                if (singleton == null) {
                    singleton = new TNNoteService();
                }
            }
        }

        return singleton;
    }


    //TODO
    public void NoteAdd(TNAction aAction) {
        TNNote note = (TNNote) aAction.inputs.get(0);
        boolean isNewDb = (Boolean) aAction.inputs.get(1);//true表示新数据库，old表示老数据库
        String content = note.content;
        //上传所有图片，再执行NoteAdd
        for (TNNoteAtt att : note.atts) {
            TNAction action = uploadFile(att, 1);
            if (action.outputs.get(0) instanceof String) {
                continue;
            }

            JSONObject output = (JSONObject) action.outputs.get(0);
            if ((Integer) TNUtils.getFromJSON(output, "code") == 0) {
                att.digest = (String) TNUtils.getFromJSON(output, "md5");
                att.attId = (Long) TNUtils.getFromLongJSON(output, "id");
                String s1 = String.format("<tn-media hash=\"%s\" />", att.digest);
                String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", att.digest, att.attId);
                content = content.replaceAll(s1, s2);
            }
        }

        if (note.catId == -1) {
            note.catId = TNSettings.getInstance().defaultCatId;
        }

        JSONObject jsonData = TNUtils.makeJSON(
                "title", note.title,
                "content", content,
                "tags", note.tagStr,
                "folder_id", note.catId,
                "create_time", note.createTime,
                "update_time", note.lastUpdate,
                "longitude", note.lbsLongitude,
                "latitude", note.lbsLatitude,
                "address", note.lbsAddress,
                "radius", note.lbsRadius);

        aAction.runChildAction(TNActionType.TNOpenUrl, "POST", "api/note", jsonData, TNActionType.NoteAdd);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
                long id = Long.valueOf(TNUtils.getFromJSON(outputs, "id").toString());
                TNDb.beginTransaction();
                try {
                    TNAction.runAction(TNActionType.Db_Execute,
                            TNSQLString.NOTE_UPDATE_NOTEID_BY_NOTELOCALID,
                            id,
                            note.noteLocalId);
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
            }
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    /**
     * 上传图片，count记数，上传失败就重复上传3次
     *
     * @param att
     * @param count
     * @return
     */
    public TNAction uploadFile(TNNoteAtt att, int count) {
        TNAction action = TNAction.runAction(TNActionType.Upload, att.attName, att.path, att.attLocalId);
        if (count < 4) {
            if (action.outputs.get(0) instanceof String) {
                action = uploadFile(att, count + 1);
            }
        }
        return action;
    }

    // TODO
    public void NoteEdit(TNAction aAction) {
        TNNote note = (TNNote) aAction.inputs.get(0);
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

        JSONObject jsonData = TNUtils.makeJSON(
                "note_id", note.noteId,
                "title", note.title,
                "content", note.content,
                "tags", note.tagStr,
                "folder_id", note.catId,
                "create_time", note.createTime,
                "update_time", note.lastUpdate);

        aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/note", jsonData, TNActionType.NoteEdit);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNDb.beginTransaction();
            try {
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_SHORT_CONTENT,
                        shortContent, note.noteId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                        System.currentTimeMillis() / 1000, note.catId);
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
    public void NoteDelete(TNAction aAction) {
        long noteId = (Long) aAction.inputs.get(0);
        JSONObject jsonData = TNUtils.makeJSON(
                "note_id", noteId);

        aAction.runChildAction(TNActionType.TNOpenUrl, "DELETE", "api/note", jsonData, TNActionType.NoteDelete);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNNote note = TNDbUtils.getNoteByNoteId(noteId);
            TNDb.beginTransaction();
            try {
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_SET_TRASH,
                        2, 1, System.currentTimeMillis() / 1000, note.noteLocalId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                        System.currentTimeMillis() / 1000, note.catId);
                TNDb.setTransactionSuccessful();
            } finally {
                TNDb.endTransaction();
            }
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    // TODO
    public void NoteRecovery(TNAction aAction) {
        long noteId = (Long) aAction.inputs.get(0);
        JSONObject jsonData = TNUtils.makeJSON(
                "note_id", noteId);

        aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/note/trash", jsonData, TNActionType.NoteRecovery);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNNote note = TNDbUtils.getNoteByNoteId(noteId);
            TNDb.beginTransaction();
            try {
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_SET_TRASH,
                        0, 2, System.currentTimeMillis() / 1000, note.noteLocalId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                        System.currentTimeMillis() / 1000, note.catId);
                TNDb.setTransactionSuccessful();
            } finally {
                TNDb.endTransaction();
            }
            aAction.finished(outputs);
        }
    }

    //TODO
    public void NoteRealDelete(TNAction aAction) {
        long noteId = (Long) aAction.inputs.get(0);
        JSONObject jsonData = TNUtils.makeJSON(
                "note_id", noteId);
        aAction.runChildAction(TNActionType.TNOpenUrl, "DELETE", "api/note/trash", jsonData, TNActionType.NoteRealDelete);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNDb.beginTransaction();
            try {
                TNNote note = TNDbUtils.getNoteByNoteId(noteId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_DELETE_BY_NOTEID,
                        noteId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                        System.currentTimeMillis() / 1000, note.catId);
                TNDb.setTransactionSuccessful();
            } finally {
                TNDb.endTransaction();
            }
            aAction.finished(outputs);
        }
    }

    public void GetNoteList(TNAction aAction) {
        int pageNum = (Integer) aAction.inputs.get(0);
        int pageSize = (Integer) aAction.inputs.get(1);
        String sortord = (String) aAction.inputs.get(2);
        JSONObject jsonData = TNUtils.makeJSON(
                "pagenum", pageNum,
                "pagesize", TNConst.PAGE_SIZE_BIG,
                "sortord", sortord);

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/note", jsonData, TNActionType.GetNoteList);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            int count = (Integer) TNUtils.getFromJSON(outputs, "count");
            TNSettings.getInstance().totalCount = count;
            TNSettings.getInstance().savePref(false);
            if (pageSize == TNConst.MAX_PAGE_SIZE) {
                int currentCount = pageNum * TNConst.PAGE_SIZE_BIG;
                if (count > currentCount) {
                    pageNum++;
                    aAction.runChildAction(TNActionType.GetNoteList, pageNum, TNConst.MAX_PAGE_SIZE, sortord);
                }
                aAction.finished(outputs);
            } else {
                int currentCount = pageNum * TNConst.PAGE_SIZE_BIG;
                if (pageSize > currentCount) {
                    pageNum++;
                    aAction.runChildAction(TNActionType.GetNoteList, pageNum, pageSize, sortord);
                }
                aAction.finished(outputs);
            }
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void GetNoteListByFolderId(TNAction aAction) {
        long folder_id = (Long) aAction.inputs.get(0);
        int pageNum = (Integer) aAction.inputs.get(1);
        int pageSize = (Integer) aAction.inputs.get(2);
        String sortord = (String) aAction.inputs.get(3);
        JSONObject jsonData = TNUtils.makeJSON(
                "folder_id", folder_id,
                "pagenum", pageNum,
                "pagesize", TNConst.PAGE_SIZE,
                "sortord", sortord);

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/folders/note", jsonData, TNActionType.GetNoteListByFolderId);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            if (pageSize == TNConst.MAX_PAGE_SIZE) {
                int currentCount = pageNum * TNConst.PAGE_SIZE;
                int count = (Integer) TNUtils.getFromJSON(outputs, "count");
                if (count > currentCount) {
                    pageNum++;
                    aAction.runChildAction(TNActionType.GetNoteListByFolderId, folder_id, pageNum, TNConst.MAX_PAGE_SIZE, sortord);
                }
                aAction.finished(outputs);
            } else {
                aAction.finished(outputs);
            }
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void GetNoteByNoteId(TNAction aAction) {
        JSONObject jsonData = TNUtils.makeJSON(
                "note_id", aAction.inputs.get(0));

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/note", jsonData, TNActionType.GetNoteByNoteId);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void GetTrashNoteByNoteId(TNAction aAction) {
        JSONObject jsonData = TNUtils.makeJSON(
                "note_id", aAction.inputs.get(0));

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/note", jsonData, TNActionType.GetTrashNoteByNoteId);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void GetNoteListByTrash(TNAction aAction) {
        int pageNum = (Integer) aAction.inputs.get(0);
        int pageSize = (Integer) aAction.inputs.get(1);
        String sortord = (String) aAction.inputs.get(2);
        JSONObject jsonData = TNUtils.makeJSON(
                "pagesize", pageSize == TNConst.MAX_PAGE_SIZE ? TNConst.PAGE_SIZE : pageSize,
                "pagenum", pageNum,
                "sortord", sortord);

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/note/trash", jsonData, TNActionType.GetNoteListByTrash);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {

            if (pageSize == TNConst.MAX_PAGE_SIZE) {
                int currentCount = pageNum * TNConst.PAGE_SIZE;
                int count = (Integer) TNUtils.getFromJSON(outputs, "count");
                if (count > currentCount) {
                    pageNum++;
                    //循环调用
                    aAction.runChildAction(TNActionType.GetNoteListByTrash, pageNum, TNConst.MAX_PAGE_SIZE, sortord);
                }
                aAction.finished(outputs);
            } else {
                aAction.finished(outputs);
            }
        } else {
            aAction.failed(outputs);
        }
    }

    public void ClearNotesByTrash(TNAction aAction) {
        @SuppressWarnings("unchecked")
        Vector<TNNote> notes = (Vector<TNNote>) aAction.outputs.get(0);
        for (int i = 0; i < notes.size(); i++) {
            aAction.runChildAction(TNActionType.NoteRealDelete, notes.get(i).noteId);
        }
    }

    public void NoteMoveTo(TNAction aAction) {
        long noteId = (Long) aAction.inputs.get(0);
        long catId = (Long) aAction.inputs.get(1);
        JSONObject jsonData = TNUtils.makeJSON(
                "note_id", noteId,
                "folder_id", catId);
        aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/note", jsonData, TNActionType.NoteMoveTo);
        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNDb.beginTransaction();
            try {
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_MOVE_CAT,
                        catId, noteId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                        System.currentTimeMillis() / 1000, catId);
                TNDb.setTransactionSuccessful();
            } finally {
                TNDb.endTransaction();
            }
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    public void NoteChangeTag(TNAction aAction) {
        long noteId = (Long) aAction.inputs.get(0);
        String tagStr = (String) aAction.inputs.get(1);
        JSONObject jsonData = TNUtils.makeJSON(
                "note_id", noteId,
                "tags", tagStr);
        aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/note", jsonData, TNActionType.NoteChangeTag);
        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNDb.beginTransaction();
            try {
                TNNote note = TNDbUtils.getNoteByNoteId(noteId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_CHANGE_TAG,
                        tagStr, noteId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                        System.currentTimeMillis() / 1000, note.catId);
                TNDb.setTransactionSuccessful();
            } finally {
                TNDb.endTransaction();
            }
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    public void NoteChangeCreateTime(TNAction aAction) {
        long noteId = (Long) aAction.inputs.get(0);
        int createTime = (Integer) aAction.inputs.get(1);
        JSONObject jsonData = TNUtils.makeJSON(
                "note_id", noteId,
                "create_time", createTime);
        aAction.runChildAction(TNActionType.TNOpenUrl, "PUT", "api/note", jsonData, TNActionType.NoteChangeCreateTime);
        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            TNDb.beginTransaction();
            try {
                TNNote note = TNDbUtils.getNoteByNoteId(noteId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_CHANGE_CREATETIME,
                        createTime, noteId);
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                        System.currentTimeMillis() / 1000, note.catId);
                TNDb.setTransactionSuccessful();
            } finally {
                TNDb.endTransaction();
            }
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    // TODO
    public void GetAllNoteIds(TNAction aAction) {

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/note/ids", null, TNActionType.GetAllNoteIds);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void GetAllTrashNoteIds(TNAction aAction) {

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/note/trash/ids", null, TNActionType.GetAllTrashNoteIds);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    public void GetFolderNoteIds(TNAction aAction) {
        long catId = (Long) aAction.inputs.get(0);
        JSONObject jsonData = TNUtils.makeJSON(
                "folder_id", catId);

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/folders/note/ids", jsonData, TNActionType.GetFolderNoteIds);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO
    public void GetNoteListBySearch(TNAction aAction) {
        //暂时只做了本地搜索，以后会增加网络搜索
        String keyWord = (String) aAction.inputs.get(0);
        TNSettings settings = TNSettings.getInstance();
        Vector<TNNote> notes = TNDbUtils.getNoteListBySearch(settings.userId, keyWord, settings.sort);
        aAction.finished(notes);
    }

    //TODO
    public void GetNoteListByTagId(TNAction aAction) {
        long tagId = (Long) aAction.inputs.get(0);
        int pageNum = (Integer) aAction.inputs.get(1);
        int pageSize = (Integer) aAction.inputs.get(2);
        String sortord = (String) aAction.inputs.get(3);

        JSONObject jsonData = TNUtils.makeJSON(
                "tag_id", tagId,
                "pagenum", pageNum,
                "pagesize", TNConst.PAGE_SIZE,
                "sortord", sortord);

        aAction.runChildAction(TNActionType.TNOpenUrl, "GET", "api/tags", jsonData, TNActionType.GetNoteListByTagId);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            if (pageSize == TNConst.MAX_PAGE_SIZE) {
                int currentCount = pageNum * TNConst.PAGE_SIZE;
                int count = (Integer) TNUtils.getFromJSON(outputs, "count");
                if (count > currentCount) {
                    pageNum++;
                    aAction.runChildAction(TNActionType.GetNoteListByTagId, tagId, pageNum, TNConst.MAX_PAGE_SIZE, sortord);
                }
                aAction.finished(outputs);
            } else {
                aAction.finished(outputs);
            }
        } else {
            aAction.failed(outputs);
        }
    }

    public void ClearRecycle(TNAction aAction) {
        JSONObject jsonData = TNUtils.makeJSON(
                "t", "all");

        aAction.runChildAction(TNActionType.TNOpenUrl, "DELETE", "api/note/trash", jsonData, TNActionType.ClearRecycle);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {
            Vector<TNNote> notes = TNDbUtils.getNoteListByTrash(TNSettings.getInstance().userId, TNConst.UPDATETIME);
            TNDb.beginTransaction();
            try {
                for (TNNote note : notes) {
                    TNAction.runAction(TNActionType.Db_Execute,
                            TNSQLString.NOTE_DELETE_BY_NOTEID,
                            note.noteId);
                }
                TNDb.setTransactionSuccessful();
            } finally {
                TNDb.endTransaction();
            }
            aAction.finished();
        } else {
            aAction.failed(outputs);
        }
    }

    //TODO 重要的类
    public void Upload(TNAction aAction) {
        long attLocalId = -1;
        if (aAction.inputs.size() > 2) {//加这个判断是为了区分意见反馈过来的是没有attLocalId这个参数的
            attLocalId = (Long) aAction.inputs.get(2);
        }
        JSONObject jsonData = TNUtils.makeJSON(
                "filename", aAction.inputs.get(0),
                "path", aAction.inputs.get(1));
        aAction.runChildAction(TNActionType.TNOpenUrl, "UPLOAD", "api/attachment", jsonData, TNActionType.Upload);

        JSONObject outputs = (JSONObject) aAction.childAction.outputs.get(0);
        if ((Integer) TNUtils.getFromJSON(outputs, "code") == 0) {

            if (aAction.inputs.size() > 2) {//意见反馈不走这个逻辑
                long attId = (Long) TNUtils.getFromLongJSON(outputs, "id");
                TNDb.beginTransaction();
                try {
                    TNAction.runAction(TNActionType.Db_Execute,
                            TNSQLString.ATT_UPDATE_SYNCSTATE_ATTID,
                            2,
                            attId,
                            attLocalId
                    );
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
            }
            aAction.finished(outputs);
        } else {
            aAction.failed(outputs);
        }
    }

}
