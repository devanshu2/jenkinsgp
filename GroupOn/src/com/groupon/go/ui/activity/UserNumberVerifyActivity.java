package com.groupon.go.ui.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Telephony;
import android.support.v7.appcompat.BuildConfig;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.RegistrationController;
import com.groupon.go.model.CodeVerificationResponse;
import com.groupon.go.model.CodeVerificationResponse.CodeVerificationResultModel;
import com.groupon.go.model.CommonJsonResponse;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.dialog.CommonDialog;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.Events;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.FontUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * This activity verify the code received by SMS.
 * 
 * @author vineet.kumar, sachin.gupta
 */
public class UserNumberVerifyActivity extends GroupOnBaseActivity implements OnClickListener {

	private static final String		LOG_TAG	= UserNumberVerifyActivity.class.getSimpleName();

	private String					mCountryCode;
	private String					mMobileNumberToBeVerified;
	private String					mAutoDetectedVerifCode;
	private boolean					mIsFromNumChange;

	private ImageView				mImgHeaderRightButton;
	private TextView				mTxvHeaderLeftButton;
	private EditText				mEdtVerificationCode;
	private TextView				mTxvSmsDetectionStatus;

	private RegistrationController	mRegistrationController;
	private BroadcastReceiver		mVerifSmsReceiver;
	private IntentFilter			mVerifSmsRcvdActionFilter;
	private CountDownTimer			mCountDownTimer;

	@SuppressLint("InlinedApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_user_verification);

		int fromActivity = getIntent().getIntExtra(Constants.EXTRA_FROM_ACTIVITY, Constants.RQ_REGISTRATION_TO_VERIFY_SCREEN);
		if (fromActivity == Constants.RQ_CHANGE_NUM_TO_VERIFY_SCREEN) {
			mIsFromNumChange = true;
		}

		mCountryCode = getIntent().getStringExtra(Constants.EXTRA_COUNTRY_CODE);

		if (mIsFromNumChange) {
			mMobileNumberToBeVerified = getIntent().getStringExtra(Constants.EXTRA_NEW_NUMBER);
		} else {
			mMobileNumberToBeVerified = getIntent().getStringExtra(Constants.EXTRA_USER_NUMBER);
		}

		mImgHeaderRightButton = (ImageView) findViewById(R.id.img_groupon_header_right_btn);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			mImgHeaderRightButton.setAlpha(0.5f);
		}

		mTxvHeaderLeftButton = (TextView) findViewById(R.id.txv_groupon_header);
		mTxvHeaderLeftButton.setOnClickListener(this);
		mTxvHeaderLeftButton.setText(mCountryCode + "-" + mMobileNumberToBeVerified);

		mTxvSmsDetectionStatus = (TextView) findViewById(R.id.txv_timer);
		mCountDownTimer = new CountDownTimer(30000, 1000) {
			private String	mTimerLabel	= getString(R.string.msg_checking_your_inbox);

			@Override
			public void onTick(long millisUntilFinished) {
				mTxvSmsDetectionStatus.setText(mTimerLabel + millisUntilFinished / 1000);
			}

			@Override
			public void onFinish() {
				unregisterVerificationCodeReceiver();
				mTxvSmsDetectionStatus.setText(getString(R.string.msg_otp_sms_not_detected));
				mTxvSmsDetectionStatus.setOnClickListener(UserNumberVerifyActivity.this);
				hideSoftKeypad();
			}
		};

		setUpVerifCodeEditText();

		mVerifSmsRcvdActionFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
		mVerifSmsRcvdActionFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
		mVerifSmsReceiver = new VerificationSmsReceiver();

		mRegistrationController = new RegistrationController(this, this);
		Bundle bundle = new Bundle();
		showProgressDialog(getString(R.string.prog_loading));

		if (mIsFromNumChange) {
			bundle.putString(ApiConstants.PARAM_MOBILE_NUMBER, getIntent().getStringExtra(Constants.EXTRA_USER_NUMBER));
			bundle.putString(ApiConstants.PARAM_NEW_NUMBER, mMobileNumberToBeVerified);
			mRegistrationController.getData(ApiConstants.GET_CHANGE_NUMBER, bundle);
		} else {
			bundle.putString(ApiConstants.PARAM_COUNTRY_CODE, mCountryCode);
			bundle.putString(ApiConstants.PARAM_MOBILE_NUMBER, mMobileNumberToBeVerified);
			mRegistrationController.getData(ApiConstants.USER_REGISTRATION, bundle);
		}
	}

	/**
	 * called from on resume and from updateUi
	 */
	private void registerVerificationCodeReceiver() {
		try {
			registerReceiver(mVerifSmsReceiver, mVerifSmsRcvdActionFilter);
		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * called from on resume and from updateUi
	 */
	private void unregisterVerificationCodeReceiver() {
		try {
			unregisterReceiver(mVerifSmsReceiver);
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerVerificationCodeReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterVerificationCodeReceiver();
	}

	/**
	 * set TextWatcher on edit text for highlight the header done button and
	 * make that click-able.
	 */
	private void setUpVerifCodeEditText() {
		mEdtVerificationCode = (EditText) findViewById(R.id.edt_verification_code);

		FontUtils.setCustomFont(mEdtVerificationCode, this, getString(R.string.font_museosans_100));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mEdtVerificationCode.setGravity(Gravity.CENTER);
		} else {
			mEdtVerificationCode.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		}

		mEdtVerificationCode.addTextChangedListener(new TextWatcher() {
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
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
					if (text.toString().trim().length() == 0) {
						mEdtVerificationCode.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					} else {
						mEdtVerificationCode.setGravity(Gravity.CENTER);
					}
				}
				if (text.toString().trim().length() > 5) {
					mImgHeaderRightButton.setClickable(true);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mImgHeaderRightButton.setAlpha(1.0f);
					}
					mImgHeaderRightButton.setOnClickListener(UserNumberVerifyActivity.this);
					mEdtVerificationCode.setOnEditorActionListener(new OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_DONE) {
								sendCodeVerificationRequest();
								return true;
							}
							return false;
						}
					});
				} else {
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
						mImgHeaderRightButton.setAlpha(0.5f);
					}
					mImgHeaderRightButton.setClickable(false);
					mEdtVerificationCode.setOnEditorActionListener(null);
				}
			}
		});
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.USER_REGISTRATION:
		case ApiConstants.GET_CHANGE_NUMBER: {
			if (!StringUtils.isNullOrEmpty(mAutoDetectedVerifCode)) {
				return; // OTP SMS is already detected.
			}
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				mCountDownTimer.start();
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
			}
			break;
		}
		case ApiConstants.CODE_VERIFICATION: {
			removeProgressDialog();
			mTxvSmsDetectionStatus.setText("");
			if (serviceResponse.isSuccess() && serviceResponse.getResponseObject() instanceof CodeVerificationResultModel) {
				GrouponGoPrefs.initializeUser(this, mCountryCode, mMobileNumberToBeVerified, (CodeVerificationResultModel) serviceResponse.getResponseObject());
				mRegistrationController.getData(ApiConstants.GET_API_AUTH_TOKEN, null);
			} else if (serviceResponse.getResponseObject() instanceof CodeVerificationResponse) {
				CodeVerificationResponse response = (CodeVerificationResponse) serviceResponse.getResponseObject();
				if (response.getError_code() == ApiConstants.ERROR_CODE_NUM_VERIFICATION_FAILED || response.getError_code() == ApiConstants.ERROR_CODE_OTP_EXPIRED) {
					showResendVerificationCodeDialog(response.getError_code());
				} else {
					ToastUtils.showToast(this, response.getMessage());
				}
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		case ApiConstants.GET_API_AUTH_TOKEN: {
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				startGatherInterestsActivity();
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		case ApiConstants.RESEND_VERIFICATION_CODE: {
			if (!StringUtils.isNullOrEmpty(mAutoDetectedVerifCode)) {
				return; // OTP SMS is already detected.
			}
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				mCountDownTimer.start();
				mTxvSmsDetectionStatus.setOnClickListener(null);
				mEdtVerificationCode.setText("");
			}
			ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			break;
		}
		case ApiConstants.GET_VERIFY_CHANGE_NUMBER: {
			removeProgressDialog();
			mTxvSmsDetectionStatus.setText("");
			if (serviceResponse.isSuccess()) {
				GrouponGoPrefs.setUserPhone(this, mMobileNumberToBeVerified);
				startUserProfileActivity();
			} else {
				CommonJsonResponse response = (CommonJsonResponse) serviceResponse.getResponseObject();
				if (response.getError_code() == ApiConstants.ERROR_CODE_NUM_VERIFICATION_FAILED || response.getError_code() == ApiConstants.ERROR_CODE_OTP_EXPIRED) {
					showResendVerificationCodeDialog(response.getError_code());
				} else {
					ToastUtils.showToast(this, response.getMessage());
				}
			}
			break;
		}
		}
	}

	/**
	 * called after number change
	 */
	private void startUserProfileActivity() {
		Intent intent = new Intent(this, UserProfileActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	/**
	 * shows wrong/expired code dialog
	 * 
	 * @param pErrorCode
	 */
	private void showResendVerificationCodeDialog(int pErrorCode) {
		CommonDialog dialog = new CommonDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.RESEND_VERIF_CODE_DIALOG);
		bundle.putInt(Constants.EXTRA_ERROR_CODE, pErrorCode);
		dialog.setArguments(bundle);
		dialog.show(getSupportFragmentManager(), Constants.TAG_RESEND_VERIF_CODE_DIALOG);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_groupon_header_right_btn: {
			sendCodeVerificationRequest();
			break;
		}
		case R.id.txv_groupon_header: {
			finish();
			break;
		}
		case R.id.txv_timer: {
			resendCodeRequest();
			break;
		}
		}
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.RESEND_VERIF_CODE_DIALOG: {
			if (eventData instanceof Boolean && (Boolean) eventData) {
				resendCodeRequest();
			}
			break;
		}
		}
	}

	/**
	 * sends code resend request
	 */
	private void resendCodeRequest() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		mAutoDetectedVerifCode = null;
		registerVerificationCodeReceiver();
		showProgressDialog(getString(R.string.prog_loading));
		Bundle data = new Bundle();
		data.putString(ApiConstants.PARAM_COUNTRY_CODE, mCountryCode);
		data.putString(ApiConstants.PARAM_MOBILE_NUMBER, mMobileNumberToBeVerified);
		if (mIsFromNumChange) {
			data.putInt(ApiConstants.PARAM_IS_NUMBER_CHANGE, 1);
		}
		mRegistrationController.getData(ApiConstants.RESEND_VERIFICATION_CODE, data);
	}

	/**
	 * @param verificationcode
	 */
	private void sendCodeVerificationRequest() {
		String veifCode = null;
		if (StringUtils.isNullOrEmpty(mAutoDetectedVerifCode)) {
			veifCode = mEdtVerificationCode.getText().toString();
			if (StringUtils.isNullOrEmpty(veifCode)) {
				return;
			}
		} else {
			veifCode = mAutoDetectedVerifCode;
		}

		showProgressDialog(getString(R.string.prog_loading));
		if (mIsFromNumChange) {
			mRegistrationController.getData(ApiConstants.GET_VERIFY_CHANGE_NUMBER, veifCode);
		} else {
			Bundle data = new Bundle();
			data.putString(ApiConstants.PARAM_COUNTRY_CODE, mCountryCode);
			data.putString(ApiConstants.PARAM_MOBILE_NUMBER, mMobileNumberToBeVerified);
			data.putString(ApiConstants.PARAM_VERIFICATION_CODE, veifCode);
			mRegistrationController.getData(ApiConstants.CODE_VERIFICATION, data);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void startGatherInterestsActivity() {
		setResult(RESULT_OK);
		finish();
		Intent intent = new Intent(this, UserPrefrenceActivity.class);
		startActivity(intent);
	}

	/**
	 * @author vineet.kumar, sachin.gupta
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private class VerificationSmsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			SmsMessage[] msgs = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
			} else {
				try {
					Object[] pdus = (Object[]) intent.getExtras().get("pdus");
					msgs = new SmsMessage[pdus.length];
					for (int i = 0; i < msgs.length; i++) {
						msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					}
				} catch (Exception e) {
					Log.e(LOG_TAG, "onReceive() " + e.getMessage());
				}
			}
			if (msgs == null || msgs.length == 0) {
				return;
			}
			String templateVerficationSmsStartsWith = "Your Go verification code is ";

			for (int i = 0; i < msgs.length; i++) {
				String msgBody = msgs[i].getMessageBody();
				if (msgBody == null || !msgBody.startsWith(templateVerficationSmsStartsWith)) {
					continue;
				}
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "OTP SMS: " + msgBody);
				}
				int preOtpTemplateLength = templateVerficationSmsStartsWith.length();
				mAutoDetectedVerifCode = msgBody.substring(preOtpTemplateLength, preOtpTemplateLength + 6);
				if (StringUtils.isNullOrEmpty(mAutoDetectedVerifCode)) {
					continue;
				}
				unregisterVerificationCodeReceiver();
				mEdtVerificationCode.setText(mAutoDetectedVerifCode);
				mTxvSmsDetectionStatus.setText(getString(R.string.msg_otp_sms_detected));
				sendCodeVerificationRequest();
				break;
			}
		}
	}
}