package com.groupon.go.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.groupon.go.R;
import com.groupon.go.constants.Constants;
import com.groupon.go.ui.dialog.CommonDialog;
import com.kelltontech.ui.Events;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.FontUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.kumar, sachin.gupta
 */
public class ChangeUserPhoneNumActivity extends GroupOnBaseActivity implements OnClickListener {

	private ImageView	mImgHeaderRightButton;
	private String		mCountryCodeWithHyphen;
	private String		mCountryCode;
	private EditText	mEdtOldMobileNumber;
	private EditText	mEdtNewMobileNumber;
	private String		mUserNewNumber;
	private String		mUserOldNumber;

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().hide();
		setContentView(R.layout.activity_change_user_number);

		mImgHeaderRightButton = (ImageView) findViewById(R.id.img_groupon_header_right_btn);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			mImgHeaderRightButton.setAlpha(0.5f);
		}

		TextView txvHeaderleft = (TextView) findViewById(R.id.txv_groupon_header);
		txvHeaderleft.setOnClickListener(this);
		txvHeaderleft.setText(getString(R.string.change_your_phone_number));

		mEdtOldMobileNumber = (EditText) findViewById(R.id.edt_old_phone_number);
		mEdtNewMobileNumber = (EditText) findViewById(R.id.edt_new_phone_number);
		setUpMobileNumberEditText(mEdtOldMobileNumber, false);
		setUpMobileNumberEditText(mEdtNewMobileNumber, true);
	}

	/**
	 * @param edtMobileNumber
	 * @param isNewNumber
	 */
	private void setUpMobileNumberEditText(final EditText edtMobileNumber, final boolean isNewNumber) {

		FontUtils.setCustomFont(edtMobileNumber, this, getString(R.string.font_museosans_100));

		// TODO - block of code is for hard-coded country code
		mCountryCode = "+91";
		mCountryCodeWithHyphen = mCountryCode + "-";
		edtMobileNumber.setText(mCountryCodeWithHyphen);
		edtMobileNumber.setSelection(mCountryCodeWithHyphen.length());

		edtMobileNumber.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// nothing to do here
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// nothing to do here
			}

			@Override
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			public void afterTextChanged(Editable text) {
				// TODO - below one if block is for hard-code country code
				if (!text.toString().startsWith(mCountryCodeWithHyphen)) {
					String textToSet = mCountryCodeWithHyphen;
					if (text.length() > mCountryCodeWithHyphen.length()) {
						CharSequence userInputNumber = text.subSequence(mCountryCodeWithHyphen.length() + 1, text.length());
						textToSet += userInputNumber;
					}
					edtMobileNumber.setText(textToSet);
					edtMobileNumber.setSelection(textToSet.length());
					return;
				}
				if (text.length() == mCountryCodeWithHyphen.length() + 10) {
					EditText edtOther = mEdtNewMobileNumber;
					if (isNewNumber) {
						edtOther = mEdtOldMobileNumber;
					}
					if (edtOther.getText().length() < mCountryCodeWithHyphen.length() + 10) {
						return;
					}
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mImgHeaderRightButton.setAlpha(1.0f);
					}
					mImgHeaderRightButton.setOnClickListener(ChangeUserPhoneNumActivity.this);

					mEdtNewMobileNumber.setOnEditorActionListener(new OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_DONE) {
								showNumberConfirmationDialog();
								return true;
							}
							return false;
						}
					});
				} else {
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
						mImgHeaderRightButton.setAlpha(0.5f);
					}
					mImgHeaderRightButton.setOnClickListener(null);
					mEdtNewMobileNumber.setOnEditorActionListener(null);
				}
			}
		});
	}

	/**
	 * // TODO - few code lines are for hard-code country code
	 */
	private void showNumberConfirmationDialog() {
		mUserNewNumber = mEdtNewMobileNumber.getText().toString().trim();
		mUserNewNumber = mUserNewNumber.substring(mCountryCodeWithHyphen.length());

		mUserOldNumber = mEdtOldMobileNumber.getText().toString().trim();
		mUserOldNumber = mUserOldNumber.substring(mCountryCodeWithHyphen.length());

		if (mUserOldNumber.length() == 0 || !TextUtils.isDigitsOnly(mUserOldNumber) || mUserOldNumber.charAt(0) == '0') {
			ToastUtils.showToast(this, getString(R.string.toast_msg_invalid_old_num));
			return;
		}

		if (mUserNewNumber.length() == 0 || !TextUtils.isDigitsOnly(mUserNewNumber) || mUserNewNumber.charAt(0) == '0') {
			ToastUtils.showToast(this, getString(R.string.toast_msg_invalid_new_num));
			return;
		}

		if (mUserOldNumber.equalsIgnoreCase(mUserNewNumber)) {
			ToastUtils.showToast(this, getString(R.string.toast_msg_same_num));
			return;
		}

		CommonDialog dialog = new CommonDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.USER_REGISTER_DIALOG);
		bundle.putString(Constants.EXTRA_COUNTRY_CODE, mCountryCode);
		bundle.putString(Constants.EXTRA_USER_NUMBER, mUserNewNumber);
		dialog.setArguments(bundle);
		dialog.show(getSupportFragmentManager(), Constants.TAG_USER_REGISTER_DIALOG);
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.USER_REGISTER_DIALOG: {
			if (eventData instanceof Boolean && (Boolean) eventData) {
				sendUserNumChangeRequest();
			}
			break;
		}
		}
	}

	/**
	 * Code of this method is changed as below- <br/>
	 * 1. Instead of sending change number request from here, <br/>
	 * 2. now next activity is launched and request is sent from there. <br/>
	 * 3. So that we start SMS listening immediately even before API responds.
	 */
	private void sendUserNumChangeRequest() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		Intent intent = new Intent(this, UserNumberVerifyActivity.class);
		intent.putExtra(Constants.EXTRA_COUNTRY_CODE, mCountryCode);
		intent.putExtra(Constants.EXTRA_USER_NUMBER, mUserOldNumber);
		intent.putExtra(Constants.EXTRA_NEW_NUMBER, mUserNewNumber);
		intent.putExtra(Constants.EXTRA_FROM_ACTIVITY, Constants.RQ_CHANGE_NUM_TO_VERIFY_SCREEN);
		startActivityForResult(intent, Constants.RQ_CHANGE_NUM_TO_VERIFY_SCREEN);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.img_groupon_header_right_btn: {
			showNumberConfirmationDialog();
			break;
		}
		case R.id.txv_groupon_header: {
			finish();
			break;
		}
		}
	}
}
