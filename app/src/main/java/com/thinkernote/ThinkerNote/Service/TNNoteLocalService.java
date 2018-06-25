package com.thinkernote.ThinkerNote.Service;

import java.util.Vector;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;

public class TNNoteLocalService {

    private static TNNoteLocalService singleton = null;

    private TNNoteLocalService() {
        TNAction.regRunner(TNActionType.NoteSave, this, "NoteSave");
        TNAction.regRunner(TNActionType.NoteLocalDelete, this, "NoteLocalDelete");
        TNAction.regRunner(TNActionType.NoteLocalRecovery, this, "NoteLocalRecovery");
        TNAction.regRunner(TNActionType.NoteLocalRealDelete, this, "NoteLocalRealDelete");
        TNAction.regRunner(TNActionType.NoteLocalMoveTo, this, "NoteLocalMoveTo");
        TNAction.regRunner(TNActionType.NoteLocalChangeTag, this, "NoteLocalChangeTag");
        TNAction.regRunner(TNActionType.NoteLocalChangeCreateTime, this, "NoteLocalChangeCreateTime");
        TNAction.regRunner(TNActionType.ClearLocalRecycle, this, "ClearLocalRecycle");
    }

    public static TNNoteLocalService getInstance() {
        if (singleton == null) {
            synchronized (TNNoteLocalService.class) {
                if (singleton == null) {
                    singleton = new TNNoteLocalService();
                }
            }
        }

        return singleton;
    }

    public void NoteSave(TNAction aAction) {

        TNNote note = (TNNote) aAction.inputs.get(0);

        if (note.title.length() <= 0) {
            note.resetTitle();
        }

        if (note.catId == -1) {
            note.catId = TNSettings.getInstance().defaultCatId;
        }

        note.lastUpdate = (int) (System.currentTimeMillis() / 1000);

        TNDb.beginTransaction();
        try {
            if (note.noteLocalId < 0) {
                // insert
                note.createTime = (int) (System.currentTimeMillis() / 1000);
                TNAction act = TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_INSERT,
                        note.title,
                        TNSettings.getInstance().userId,
                        note.catId,
                        note.trash,
                        note.content,
                        note.source,
                        note.createTime,
                        note.lastUpdate,
                        3,
                        -1,
                        note.shortContent,
                        note.tagStr,
                        note.lbsLongitude,
                        note.lbsLatitude,
                        note.lbsRadius,
                        note.lbsAddress,
                        TNSettings.getInstance().username,
                        note.thumbnail,
                        note.contentDigest);
                note.noteLocalId = (Long) act.outputs.get(0);
            } else {
                // update
                note.syncState = note.noteId != -1 ? 4 : 3;
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_LOCAL_UPDATE,
                        note.title,
                        note.catId,
                        note.content,
                        note.createTime,
                        note.lastUpdate,
                        note.shortContent,
                        note.tagStr,
                        note.contentDigest,
                        note.syncState,
                        note.noteLocalId);

            }

            // save att
            TNAction attAction = aAction.runChildAction(TNActionType.AttLocalSave, note);
            note = (TNNote) attAction.outputs.get(0);

            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                    System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        aAction.finished(note);
    }

    public void NoteLocalRecovery(TNAction aAction) {
        long noteLocalId = (Long) aAction.inputs.get(0);
        TNDb.beginTransaction();
        try {
            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.NOTE_SET_TRASH,
                    0,
                    7,
                    System.currentTimeMillis() / 1000,
                    noteLocalId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        aAction.finished();
    }

    //TODO
    public void NoteLocalDelete(TNAction aAction) {
        long noteLocalId = (Long) aAction.inputs.get(0);
        TNDb.beginTransaction();
        try {
            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.NOTE_SET_TRASH,
                    2,
                    6,
                    System.currentTimeMillis() / 1000,
                    noteLocalId);

            TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                    System.currentTimeMillis() / 1000, note.catId);
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        aAction.finished();
    }

    public void NoteLocalRealDelete(TNAction aAction) {
        long noteLocalId = (Long) aAction.inputs.get(0);
        TNDb.beginTransaction();
        try {
            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.NOTE_UPDATE_SYNCSTATE,
                    5,
                    noteLocalId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        aAction.finished();
    }

    public void NoteLocalMoveTo(TNAction aAction) {
        long noteLocalId = (Long) aAction.inputs.get(0);
        long catId = (Long) aAction.inputs.get(1);
        int lastUpdate = (int) (System.currentTimeMillis() / 1000);
        TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
        int syncState = note.noteId == -1 ? 3 : 4;
        TNDb.beginTransaction();
        try {
            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.NOTE_MOVE_CAT,
                    catId,
                    syncState,
                    lastUpdate,
                    noteLocalId);

            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                    System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        aAction.finished();
    }

    //TODO
    public void NoteLocalChangeTag(TNAction aAction) {
        long noteLocalId = (Long) aAction.inputs.get(0);
        String tags = (String) aAction.inputs.get(1);
        int lastUpdate = (int) (System.currentTimeMillis() / 1000);
        TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
        int syncState = note.noteId == -1 ? 3 : 4;
        TNDb.beginTransaction();
        try {
            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.NOTE_CHANGE_TAG,
                    tags,
                    syncState,
                    lastUpdate,
                    noteLocalId);

            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                    System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        aAction.finished();
    }

    public void NoteLocalChangeCreateTime(TNAction aAction) {
        long noteLocalId = (Long) aAction.inputs.get(0);
        int createTime = (Integer) aAction.inputs.get(1);
        int lastUpdate = (int) (System.currentTimeMillis() / 1000);
        TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
        int syncState = note.noteId == -1 ? 3 : 4;
        TNDb.beginTransaction();
        try {
            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.NOTE_CHANGE_CREATETIME,
                    createTime,
                    syncState,
                    lastUpdate,
                    noteLocalId);

            TNAction.runAction(TNActionType.Db_Execute,
                    TNSQLString.CAT_UPDATE_LASTUPDATETIME,
                    System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        aAction.finished();
    }

    public void ClearLocalRecycle(TNAction aAction) {
        Vector<TNNote> notes = TNDbUtils.getNoteListByTrash(TNSettings.getInstance().userId, TNConst.CREATETIME);
        TNDb.beginTransaction();
        try {
            for (int i = 0; i < notes.size(); i++) {
                TNAction.runAction(TNActionType.Db_Execute,
                        TNSQLString.NOTE_UPDATE_SYNCSTATE,
                        5,
                        notes.get(i).noteLocalId);
            }
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        aAction.finished();
    }


}
