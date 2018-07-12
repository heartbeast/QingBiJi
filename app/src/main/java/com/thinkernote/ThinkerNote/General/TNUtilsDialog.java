package com.thinkernote.ThinkerNote.General;

import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.Activity.TNCatInfoAct;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNSQLString;

// TODO delete
public class TNUtilsDialog {
    static Handler mHandler;
    static TimerTask mTimerTask;


    public static void startIntent(final Activity act, final Intent intent,
                                   int msgId) {
        PackageManager packageManager = act.getPackageManager();
        if (packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            act.startActivity(intent);
        } else {
            TNUtilsUi.alert(act, msgId);
        }
    }

    public static void startIntentForResult(final Activity act, final Intent intent,
                                            int msgId, int requestCode) {
        PackageManager packageManager = act.getPackageManager();
        if (packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            act.startActivityForResult(intent, requestCode);
        } else {
            TNUtilsUi.alert(act, msgId);
        }
    }
}
