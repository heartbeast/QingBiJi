package com.thinkernote.ThinkerNote.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import com.thinkernote.ThinkerNote._interface.p.ISplashPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnSplashListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.permission.PermissionHelper;
import com.thinkernote.ThinkerNote.permission.PermissionInterface;

import org.json.JSONObject;

/**
 * 启动页/欢迎页
 */
public class TNSplashAct extends TNActBase implements OnSplashListener {
    //权限申请
    private PermissionHelper mPermissionHelper;
    // Class members
    //-------------------------------------------------------------------------------

    private boolean isRunning = false;
    private Bundle extraBundle = null;
    private String passWord;

    // p
    private ISplashPresenter presener;
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
        //初始化并发起权限申请

        mPermissionHelper = new PermissionHelper(this, new PermissionInterface() {
            @Override
            public int getPermissionsRequestCode() {
                //设置权限请求requestCode，只有不跟onRequestPermissionsResult方法中的其他请求码冲突即可。
                return 10000;
            }

            @Override
            public String[] getPermissions() {
                //设置该界面所需的全部权限
                return new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE
                };
            }

            @Override
            public void requestPermissionsSuccess() {
                //权限请求用户已经全部允许
                initViews();
            }

            @Override
            public void requestPermissionsFail() {
                //权限请求不被用户允许。可以提示并退出或者提示权限的用途并重新发起权限申请。
                finish();
            }
        });
        mPermissionHelper.requestPermissions();
    }

    //权限回调处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)) {
            //权限请求结果，并已经处理了该回调
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void configView() {

    }

    private void initViews() {

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
                startActivity(TNMainAct.class, extraBundle);
                finish();
                isRunning = false;
            }
        }, 200);
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
