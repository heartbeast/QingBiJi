package com.thinkernote.ThinkerNote.Activity;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.base.TNActBase;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
/**
 * 主页--设置--用户信息--具体修改界面（密码 用户名 邮箱等）
 * TODO
 */
public class TNChangeUserInfoAct extends TNActBase implements OnClickListener {
	
	private TextView mTitleView;
	private String mType;
	private EditText mUserNameView, mUserNamePswView, mNewPswView, mNewPswAgainView, mOldPswView, mEmailView, mEmailPswView;
	private Dialog mProgressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_change_userinfo);
		
		TNAction.regResponder(TNActionType.ChangePassword, this, "respondChangePassword");
		TNAction.regResponder(TNActionType.ChangePhone, this, "respondChangePhone");
		TNAction.regResponder(TNActionType.ChangeUserNameOrEmail, this, "respondChangeUserNameOrEmail");
		TNAction.regResponder(TNActionType.Profile, this, "respondProfile");
		
		initView();
	}
	
	private void initView() {
		mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
		
		mTitleView = (TextView) findViewById(R.id.change_userinfo_back);
		mUserNameView = (EditText) findViewById(R.id.change_username);
		mUserNamePswView = (EditText) findViewById(R.id.old_password_username);
		mNewPswView = (EditText) findViewById(R.id.new_password);
		mNewPswAgainView = (EditText) findViewById(R.id.new_password_again);
		mOldPswView = (EditText) findViewById(R.id.old_password);
		mEmailView = (EditText) findViewById(R.id.change_email);
		mEmailPswView = (EditText) findViewById(R.id.old_password_email);
		
		mTitleView.setOnClickListener(this);
		findViewById(R.id.change_confirm).setOnClickListener(this);
		
		mType = getIntent().getStringExtra("type");
		if ("username".equals(mType)) {
			mTitleView.setText("修改用户名");
			findViewById(R.id.ll_username).setVisibility(View.VISIBLE);
		} else if ("password".equals(mType)) {
			mTitleView.setText("修改密码");
			findViewById(R.id.ll_password).setVisibility(View.VISIBLE);
		} else {
			mTitleView.setText("修改邮箱");
			findViewById(R.id.ll_email).setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void configView() {
		super.configView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.change_userinfo_back:
			finish();
			break;
		case R.id.change_confirm:
			change();
			break;

		default:
			break;
		}
	}

	private void change() {
		if ("username".equals(mType)) {
			String newUserName = mUserNameView.getText()
					.toString().trim();
			if (newUserName.length() == 0) {
				TNHandleError.handleErrorCode(this,
						TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_NewUserNameBlank));
				return;
			}

			if (!TNUtils.checkRegex(TNUtils.USERNAME_REGEX, newUserName)) {
				TNUtilsUi.alert(this, R.string.alert_Reg_UsernameWrong);
				return;
			}

			String userPwd = mUserNamePswView.getText().toString();
			if (userPwd.length() == 0) {
				TNHandleError.handleErrorCode(this,
						TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_UserPwdBlank));
				return;
			}
			mProgressDialog.show();
			TNAction.runActionAsync(TNActionType.ChangeUserNameOrEmail,
					newUserName, "userName", userPwd);
		} else if ("password".equals(mType)) {
			String newPwd = mNewPswView.getText().toString().trim();
			if (newPwd.length() == 0) {
				TNHandleError.handleErrorCode(this,
						TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_NewPwdBlank));
				return;
			}
			if (newPwd.length() > 20) {
				TNUtilsUi.alert(this,
						R.string.alert_UserInfo_Password_Long);
				return;
			}
			String confirmPwd = mNewPswAgainView.getText()
					.toString().trim();
			if (!confirmPwd.equals(newPwd)) {
				TNHandleError.handleErrorCode(this,
						TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_ConfirmPwdUnmatch));
				return;
			}
			
			String oldPwd = mOldPswView.getText()
					.toString().trim();
			if (oldPwd.length() == 0) {
				TNHandleError.handleErrorCode(this,
						TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_OldPwdBlank));
				return;
			}
//			if (!TNSettings.getInstance().password.equals(oldPwd)){
//				TNHandleError.handleErrorCode(this,
//						TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_UserPwdWrong));
//				return;
//			}
			// try modify password
			mProgressDialog.show();
			TNAction.runActionAsync(TNActionType.ChangePassword,
					oldPwd, newPwd);
		} else {
			String newEmail = mEmailView.getText().toString().trim();
			if (newEmail.length() == 0) {
				TNHandleError.handleErrorCode(this,
						TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_NewEmailBlank));
				return;
			}
			if (!TNUtils.checkRegex(TNUtils.FULL_EMAIL_REGEX, newEmail)) {
				TNHandleError.handleErrorCode(this,
						TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_EmailWrong));
				return;
			}

			String userPwd = mEmailPswView.getText().toString().trim();
			if (userPwd.length() == 0) {
				TNHandleError.handleErrorCode(this,
						TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_UserPwdBlank));
				return;
			}
			mProgressDialog.show();
			TNAction.runActionAsync(TNActionType.ChangeUserNameOrEmail,
					newEmail, "email", userPwd);
		}
	}
	
	private void getUserInfo() {
		TNAction.runActionAsync(TNActionType.Profile);
	}
	
	//==========================================================
	public void respondChangePassword(TNAction aAction) {
		if (!TNHandleError.handleResult(this, aAction)) {
			TNSettings settings = TNSettings.getInstance();
			settings.password = (String)aAction.inputs.get(1);
			settings.savePref(false);
			TNUtilsUi.showShortToast("密码修改成功");
			getUserInfo();
		} else {
			mProgressDialog.hide();
		}
	}

	public void respondChangeUserNameOrEmail(TNAction aAction) {
		if (!TNHandleError.handleResult(this, aAction)) {
			if (((String)aAction.inputs.get(1)).equals("userName")) {
				TNSettings settings = TNSettings.getInstance();
				settings.loginname = (String)aAction.inputs.get(0);
				settings.savePref(false);
			}
			TNUtilsUi.showShortToast("修改成功");
			getUserInfo();
		} else {
			mProgressDialog.hide();
		}
	}
	
	public void respondProfile(TNAction aAction) {
		mProgressDialog.hide();
		finish();
	}
}
