package com.thinkernote.ThinkerNote.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Data.TNUser;
import com.thinkernote.ThinkerNote.Database.TNDb2;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.presenter.SplashPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.ISplashPresener;
import com.thinkernote.ThinkerNote._interface.v.OnSplashListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;

import org.json.JSONObject;

/**
 * 启动页/欢迎页
 */
public class TNSplashAct extends TNActBase implements OnSplashListener {

    // Class members
    //-------------------------------------------------------------------------------
    private boolean isRunning = false;
    private Bundle extraBundle = null;
    private String passWord;

    // p
    private ISplashPresener presener;
    private LoginBean loginBean;
    private ProfileBean profileBean;


    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //解决首次安装按home键置入后台，从桌面图标点击重新启动的问题
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        setContentView(R.layout.splash);
        setViews();
        //
        presener = new SplashPresenterImpl(this, this);
//		if(!TNSettings.getInstance().serviceRuning){
//			Intent serviceIntent = new Intent(TNSplashAct.this, TNPushService.class);
//			startService(serviceIntent);
//		}

        if (getIntent().hasExtra(Intent.EXTRA_INTENT)) {
            extraBundle = new Bundle();
            extraBundle.putParcelable(Intent.EXTRA_INTENT,
                    (Intent) getIntent().getExtras().get(Intent.EXTRA_INTENT));
        }
    }

    protected void configView() {
        if (TNSettings.getInstance().hasDbError) {
            //监听
            DialogInterface.OnClickListener onClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //重置 数据库

                            resetDb();
                            startRun();
                            TNSettings.getInstance().hasDbError = false;
                            TNSettings.getInstance().savePref(false);
                        }
                    };

            JSONObject jsonData = TNUtils.makeJSON(
                    "CONTEXT", this,
                    "TITLE", R.string.alert_Title,
                    "MESSAGE", R.string.alert_DBErrorHint,
                    "POS_BTN", R.string.alert_Uninstall,
                    "POS_BTN_CLICK", onClickListener);
            AlertDialog ad = TNUtilsUi.alertDialogBuilder(jsonData);
            ad.setCanceledOnTouchOutside(false);
            ad.setCancelable(false);
            ad.show();
        } else {
            startRun();
        }
    }

    /**
     * 重置数据库
     */
    private void resetDb() {
        TNDb2.getInstance().DBReset();
    }


    private void startRun() {
        if (isRunning) return;
        isRunning = true;

        final TNSettings settings = TNSettings.getInstance();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (settings.isLogin()) {
                    //如果user表被异常清空
                    TNUser user = TNDbUtils.getUser(settings.userId);
                    if (user == null) {
                        startActivity(TNLoginAct.class, extraBundle);
                    } else {
                        startActivity(TNMainAct.class, extraBundle);
                    }
                    finish();
                } else if ((settings.expertTime != 0) && (settings.expertTime * 1000 - System.currentTimeMillis() < 0)) {
                    passWord = settings.password;//回调中需要使用
                    login(settings.loginname, passWord);

                } else {
                    startActivity(TNLoginAct.class, extraBundle);
                    finish();
                }
                isRunning = false;
            }
        }, 2000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 屏蔽任何按键
        return true;
    }

    //-----------------------------------p层调用-------------------------------------
    private void login(String name, String ps) {
        presener.plogin(name, ps);
    }

    private void updateProfile() {
        presener.pUpdataProfile();
    }

    //-----------------------------------接口返回回调-------------------------------------
    @Override
    public void onSuccess(Object obj) {
        loginBean = (LoginBean) obj;

        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = false;

        //
        settings.password = passWord;
        settings.userId = loginBean.getUser_id();
        settings.username = loginBean.getUsername();
        settings.token = loginBean.getToken();
        settings.expertTime = loginBean.getExpire_at();
        if (TextUtils.isEmpty(settings.loginname)) {
            settings.loginname = loginBean.getUsername();
        }
        settings.savePref(false);
        //更新
        updateProfile();


    }

    @Override
    public void onFailed(String msg, Exception e) {
        MLog.e(msg);
        TNSettings settings = TNSettings.getInstance();
        Bundle b = new Bundle();
        settings.isLogout = true;
        settings.savePref(false);
        startActivity(TNLoginAct.class, b);
        finish();
    }


    @Override
    public void onProfileSuccess(Object obj) {
        profileBean = (ProfileBean) obj;
        //
        TNSettings settings = TNSettings.getInstance();
        long userId = TNDbUtils.getUserId(settings.username);

        settings.phone = profileBean.getPhone();
        settings.email = profileBean.getEmail();
        settings.defaultCatId = profileBean.getDefault_folder();

        if (userId != settings.userId) {
            //清空user表
            UserDbHelper.clearUsers();
        }

        JSONObject user = TNUtils.makeJSON(
                "username", settings.username,
                "password", settings.password,
                "userEmail", settings.email,
                "phone", settings.phone,
                "userId", settings.userId,
                "emailVerify", profileBean.getEmailverify(),
                "totalSpace", profileBean.getTotal_space(),
                "usedSpace", profileBean.getUsed_space());

        //更新user表
        UserDbHelper.addOrUpdateUser(user);

        //
        settings.isLogout = false;
        settings.savePref(false);

        //onProfileSuccess中调用
        if (!runExtraIntent()) {
            Bundle b = new Bundle();
            settings.isLogout = false;
            settings.savePref(false);
            startActivity(TNMainAct.class, b);
            finish();
        }
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        MLog.e(msg);
        TNSettings settings = TNSettings.getInstance();
        Bundle b = new Bundle();
        settings.isLogout = true;
        settings.savePref(false);
        startActivity(TNLoginAct.class, b);
        finish();
    }
}