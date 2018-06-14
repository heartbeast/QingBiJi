package com.thinkernote.ThinkerNote.Activity;

import java.util.LinkedList;
import java.util.Vector;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.Data.TNPreferenceChild;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.presenter.UserInfoPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.IUserInfoPresener;
import com.thinkernote.ThinkerNote._interface.v.OnUserinfoListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;

/**
 * 主页--设置界面
 * sjy 0614
 */
public class TNUserInfoAct extends TNActBase implements OnClickListener,
        OnItemClickListener, OnUserinfoListener {

    private ListView mListView;
    private Vector<TNPreferenceChild> mChilds;
    private TNPreferenceChild mCurrentChild;
    private String mDownLoadAPKPath = "";

    //
    private IUserInfoPresener presener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo);
        setViews();
        //TODO delete
        TNAction.regResponder(TNActionType.UpdateSoftware, this, "respondUpdateSoftware");

        //
        presener = new UserInfoPresenterImpl(this, this);

        mChilds = new Vector<TNPreferenceChild>();
        getSettings();

        mListView = (ListView) findViewById(R.id.userinfo_listview);
        mListView.setAdapter(new TNUserInfoListAdapter());
        mListView.setOnItemClickListener(this);
    }

    @Override
    protected void configView() {
        ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.userinfo_page,
                R.drawable.page_bg);
        TNUtilsSkin.setViewBackground(this, null, R.id.userinfo_toolbar_layout,
                R.drawable.toolbg);

        findViewById(R.id.userinfo_back).setOnClickListener(this);
        findViewById(R.id.userinfo_logout).setOnClickListener(this);
    }

    private void getSettings() {
        mChilds.clear();
        TNPreferenceChild child = null;

        // 软件更新
        child = new TNPreferenceChild(getString(R.string.userinfo_update), getString(R.string.userinfo_update_info), true, new TNRunner(this, "updateSoftware"));
        mChilds.add(child);

        // 用户信息
        child = new TNPreferenceChild(getString(R.string.userinfo_userinfo), getString(R.string.userinfo_userinfo_child), true, new TNRunner(this, "runActivity"));
        child.setOther("USER_INFO");
        mChilds.add(child);

        //设置
        child = new TNPreferenceChild(getString(R.string.userinfo_settings), getString(R.string.userinfo_settings_child), true, new TNRunner(this, "runActivity"));
        child.setOther("SETTING");
        mChilds.add(child);

        //关于我们
        child = new TNPreferenceChild(getString(R.string.userinfo_about), getString(R.string.userinfo_about_child), true, new TNRunner(this, "runActivity"));
        child.setOther("ABOUT");
        mChilds.add(child);

//		// 语音设置
//		child = new TNPreferenceChild(getString(R.string.userinfo_audio_settings), getString(R.string.userinfo_audio_settings_child), true, new TNRunner(this, "runActivity"));
//		child.setOther("AUDIO_SETTING");
//		mChilds.add(child);

        // 空间奖励和贡献
        child = new TNPreferenceChild(getString(R.string.userinfo_spaceinfo), getString(R.string.userinfo_spaceinfo_child), true, new TNRunner(this, "runActivity"));
        child.setOther("SPACE_INFO");
        mChilds.add(child);

//		// 友情推荐
//		{// 微团队
//			child = new TNPreferenceChild(getString(R.string.userinfo_wetuandui), null, true, new TNRunner(this, "openApp"));
//			child.setLogoId(R.drawable.ic_wetuandui);
//			child.setOther("com.thinkernote.Team");
//			mChilds.add(child);
//		}
//		{// 360浏览器
//			child = new TNPreferenceChild(getString(R.string.userinfo_360browser), null, true, new TNRunner(this, "openApp"));
//			child.setLogoId(R.drawable.ic_360);
//			child.setOther("com.qihoo.browser");
//			mChilds.add(child);
//		}
        {// 打赏
            child = new TNPreferenceChild(getString(R.string.userinfo_pay), null, true, new TNRunner(this, "runActivity"));
            child.setLogoId(R.drawable.pay_tip);
            child.setOther("PAY_TIP");
            mChilds.add(child);
        }
    }


    private class TNUserInfoListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mChilds.size();
        }

        @Override
        public Object getItem(int position) {
            return mChilds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = (LinearLayout) layoutInflater.inflate(
                        R.layout.preference_child, null);
            }
            convertView.findViewById(R.id.child_layout).setBackgroundDrawable(
                    TNUtilsSkin.getPreferenceItemStatusDrawable(TNUserInfoAct.this));

            TNPreferenceChild child = (TNPreferenceChild) getItem(position);
            if (child.getLogoId() > 0) {
                convertView.findViewById(R.id.child_logo).setVisibility(View.VISIBLE);
                TNUtilsSkin.setImageViewDrawable(TNUserInfoAct.this, convertView, R.id.child_logo, child.getLogoId());
            } else
                convertView.findViewById(R.id.child_logo).setVisibility(View.GONE);

            ((TextView) convertView.findViewById(R.id.child_name)).setText(child.getChildName());
            if (child.getInfo() == null)
                convertView.findViewById(R.id.child_info).setVisibility(View.GONE);
            else {
                convertView.findViewById(R.id.child_info).setVisibility(View.VISIBLE);
                ((TextView) convertView.findViewById(R.id.child_info)).setText(child.getInfo());
            }
            if (child.isVisibleMoreBtn())
                convertView.findViewById(R.id.child_more).setVisibility(View.VISIBLE);
            else
                convertView.findViewById(R.id.child_more).setVisibility(View.INVISIBLE);

            return convertView;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        mCurrentChild = mChilds.get(position);
        if (mCurrentChild.getTargetMethod() != null) {
            mCurrentChild.getTargetMethod().run();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userinfo_back:
                finish();
                break;

            case R.id.userinfo_logout:
                logout();

                break;
        }
    }

    // child item click methods
    // ----------------------------------------------------------------------------------

    public void runActivity() {
        String activityName = mCurrentChild.getOther();
        if (activityName != null && activityName.length() > 0) {
            if ("ABOUT".equals(activityName)) {
                startActivity(TNAboutAct.class);//关于我们
            } else if ("PAY_TIP".equals(activityName)) {
                startActivity(TNPayTipAct.class);//打赏
            } else {
                Bundle b = new Bundle();
                b.putString("Type", mCurrentChild.getOther());
                startActivity(TNSettingsAct.class, b);
            }
        }
    }


    public void openRecommend() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(mCurrentChild.getOther()));
        TNUtilsDialog.startIntent(this, intent,
                R.string.alert_About_CantOpenWeb);
    }

    public void openApp() {
        TNUtilsUi.openAppForStore(this, mCurrentChild.getOther());
    }

    public void downloadApp() {
        DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openRecommend();
            }
        };

        JSONObject jsonData = TNUtils.makeJSON(
                "CONTEXT", this,
                "TITLE", R.string.alert_Title,
                "MESSAGE", R.string.alert_UserInfo_DownloadApp,
                "POS_BTN", R.string.alert_OK,
                "POS_BTN_CLICK", pbtn_Click,
                "NEG_BTN", R.string.alert_Cancel
        );
        TNUtilsUi.alertDialogBuilder(jsonData).show();
    }


    public void respondUpdateSoftware(TNAction aAction) {
        if (aAction.result == TNActionResult.Working) {
            Dialog dialog = (Dialog) aAction.inputs.get(1);
            ProgressBar pb = (ProgressBar) dialog
                    .findViewById(R.id.update_progressbar);
            pb.setProgress((Integer) aAction.progressInfo);
            TextView percent = (TextView) dialog
                    .findViewById(R.id.update_percent);
            percent.setText(String.format("%.2fM / %.2fM (%.2f%%)",
                    pb.getProgress() / 1024f / 1024f,
                    pb.getMax() / 1024f / 1024f,
                    100f * pb.getProgress() / pb.getMax()));
        } else if (aAction.result == TNActionResult.Finished) {
            Log.d(TAG, "respondUpdateSoftware finished");
            Dialog dialog = (Dialog) aAction.inputs.get(1);
            dialog.dismiss();
            String filePath = (String) aAction.outputs.get(0);
            if (filePath != null)
                TNUtilsUi.openFile(filePath);
        }
    }

    class CustomListener implements View.OnClickListener {
        private final AlertDialog dialog;

        public CustomListener(AlertDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)// Search键
                    {
                        return true;
                    }
                    return false;
                }
            });

            Button theButton = dialog
                    .getButton(DialogInterface.BUTTON_POSITIVE);
            theButton.setText(getString(R.string.update_downloading));
            theButton.setEnabled(false);

            Button negButton = dialog
                    .getButton(DialogInterface.BUTTON_NEGATIVE);
            negButton.setEnabled(false);
            download(mDownLoadAPKPath,dialog);


        }
    }
    //-----------------------------------------p层调用--------------------------------------------

    //检查更新
    public void updateSoftware() {
        upgrade();
    }

    private void logout() {
        presener.pLogout();
    }

    private void upgrade() {
        if (TNUtilsDialog.checkNetwork(this)) {
            presener.pUpgrade();

        }
    }

    private void download(String mDownLoadAPKPath,Dialog dialog){

        //TODO 未做
        TNAction.runActionAsync(TNActionType.UpdateSoftware, mDownLoadAPKPath, dialog);
    }


    //-----------------------------------------接口回调--------------------------------------------
    @Override
    public void onLogoutSuccess(Object obj) {

        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = true;
        settings.lockPattern = new LinkedList<Integer>();
        settings.userId = -1;
        settings.username = "";
        settings.phone = "";
        settings.email = "";
        settings.password = "";
        settings.savePref(true);

        startActivity(TNLoginAct.class);
        finish();
    }

    @Override
    public void onLogoutFailed(String msg, Exception e) {
        MLog.e(msg);

        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = true;
        settings.lockPattern = new LinkedList<Integer>();
        settings.userId = -1;
        settings.username = "";
        settings.phone = "";
        settings.email = "";
        settings.password = "";
        settings.savePref(true);

        startActivity(TNLoginAct.class);
        finish();
    }

    @Override
    public void onUpgradeSuccess(Object obj) {
        MainUpgradeBean bean = (MainUpgradeBean) obj;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            MLog.d(TAG, info.versionCode + "," + info.versionName);
            String newVersionName = bean.getVersion();
            String newVersionCode = bean.getVersionCode() != null ? bean.getVersionCode() : "-1";
            int newSize = bean.getSize();
            String description = bean.getContent();
            mDownLoadAPKPath = bean.getUrl();

            MLog.d(TAG, newVersionName + "," + newSize);

            if (Integer.valueOf(newVersionCode) > info.versionCode) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout fl = (LinearLayout) layoutInflater.inflate(
                        R.layout.update, null);
                TextView hint = (TextView) fl
                        .findViewById(R.id.update_hint);
                hint.setText(String.format(getString(R.string.update_hint),
                        info.versionName, newVersionName, description));
                hint.setMovementMethod(ScrollingMovementMethod
                        .getInstance());

                ProgressBar pb = (ProgressBar) fl
                        .findViewById(R.id.update_progressbar);
                pb.setMax(newSize);
                pb.setProgress(0);
                TextView percent = (TextView) fl
                        .findViewById(R.id.update_percent);
                percent.setText(String.format("%.2fM / %.2fM (%.2f%%)",
                        pb.getProgress() / 1024f / 1024f,
                        pb.getMax() / 1024f / 1024f,
                        100f * pb.getProgress() / pb.getMax()));

                JSONObject jsonData = TNUtils.makeJSON("CONTEXT",
                        TNSettings.getInstance().topAct, "TITLE",
                        R.string.alert_Title, "VIEW", fl, "POS_BTN",
                        R.string.update_start, "NEG_BTN",
                        R.string.alert_Cancel);
                AlertDialog dialog = TNUtilsUi.alertDialogBuilder(jsonData);
                dialog.show();

                Button theButton = dialog
                        .getButton(DialogInterface.BUTTON_POSITIVE);

                theButton.setOnClickListener(new CustomListener(dialog));
            } else {
                TNUtilsUi.showToast("当前版本已是最新");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }
}
