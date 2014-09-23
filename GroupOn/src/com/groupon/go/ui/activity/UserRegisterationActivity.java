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
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.persistance.GrouponGoFiles;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.dialog.CommonDialog;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.Events;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.FontUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * This activity allow user to register with their mobile number. <br/>
 * 
 * // TODO - Country code is fixed for now. XML is also changed accordingly.
 * 
 * @author vineet.kumar, sachin.gupta
 */
public class UserRegisterationActivity extends GroupOnBaseActivity implements OnClickListener {

	// private ArrayList<String> mListCountryName;
	// private ArrayList<CountryModel> mListCountryModel;
	// private Spinner mSpinnerCountryName;
	// private SpinnerAdapterCountry mSpinAdapterCountryName;
	// private TextView mTxvCountryName;
	// private ImageView mImgCountryImage;
	private EditText	mEdtMobileNumber;
	private ImageView	mImgHeaderRightButton;

	private String		mCountryCodeWithHyphen;
	private String		mCountryCode;
	private String		mUserNumber;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_user_registeration);

		// sendCountryListRequest();
		// mListCountryModel = DummyData.getCountries();
		// createListFromModel();

		// mSpinnerCountryName = (Spinner) findViewById(R.id.country_spinner);
		// mSpinAdapterCountryName = new
		// SpinnerAdapterCountry(UserRegisterationActivity.this,
		// mListCountryName);
		// mSpinAdapterCountryName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// mSpinnerCountryName.setAdapter(mSpinAdapterCountryName);
		// mTxvCountryName = (TextView) findViewById(R.id.txv_country_name);
		// mImgCountryImage = (ImageView) findViewById(R.id.flag_icon);
		// RelativeLayout layoutBelowSpinner = (RelativeLayout)
		// findViewById(R.id.layout_below_spinner);
		// layoutBelowSpinner.setOnClickListener(this);

		mImgHeaderRightButton = (ImageView) findViewById(R.id.img_groupon_header_right_btn);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			mImgHeaderRightButton.setAlpha(0.5f);
		}

		TextView txvHeaderleft = (TextView) findViewById(R.id.txv_groupon_header);
		txvHeaderleft.setOnClickListener(this);
		txvHeaderleft.setText(getString(R.string.your_phone_number));
		(findViewById(R.id.txv_terms)).setOnClickListener(this);

		setUpMobileNumberEditText();
		showSoftKeypad();
	}

	/**
	 * sends country list API request
	 */
	/*
	 * private void sendCountryListRequest() {
	 * showProgressDialog(getString(R.string.prog_loading));
	 * RegistrationController controller = new RegistrationController(this,
	 * this); controller.getData(ApiConstants.GET_COUNTRY_LIST, null); }
	 */

	/**
	 * 
	 */
	/*
	 * private void createListFromModel() { mListCountryName = new
	 * ArrayList<String>(); for (int i = 0; i < mListCountryModel.size(); i++) {
	 * String countryname = mListCountryModel.get(i).getCountry_name() + " (" +
	 * mListCountryModel.get(i).getCountry_code() + ")";
	 * mListCountryName.add(countryname); } }
	 */

	/**
	 * set TextWatcher on edit text for highlight the header done button and
	 * make that click-able.
	 */
	private void setUpMobileNumberEditText() {
		mEdtMobileNumber = (EditText) findViewById(R.id.edt_phone_number);
		FontUtils.setCustomFont(mEdtMobileNumber, this, getString(R.string.font_museosans_100));

		mEdtMobileNumber.addTextChangedListener(new TextWatcher() {
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
				// TODO - below if block is for hard-code country code
				if (!text.toString().startsWith(mCountryCodeWithHyphen)) {
					String textToSet = mCountryCodeWithHyphen;
					if (text.length() > mCountryCodeWithHyphen.length()) {
						CharSequence userInputNumber = text.subSequence(mCountryCodeWithHyphen.length() + 1, text.length());
						textToSet += userInputNumber;
					}
					mEdtMobileNumber.setText(textToSet);
					mEdtMobileNumber.setSelection(textToSet.length());
					return;
				}
				if (text.toString().trim().length() == mCountryCodeWithHyphen.length() + 10) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mImgHeaderRightButton.setAlpha(1.0f);
					}
					mImgHeaderRightButton.setOnClickListener(UserRegisterationActivity.this);
					mEdtMobileNumber.setOnEditorActionListener(new OnEditorActionListener() {
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
					mEdtMobileNumber.setOnEditorActionListener(null);
				}
			}
		});

		// TODO - block of code is for hard-coded country code
		int mobileNumberLength = 10;
		mCountryCode = "+91";
		mCountryCodeWithHyphen = mCountryCode + "-";

		String mobileNumberToBePrefilled = "";
		if (ConnectivityUtils.isSimOperatorIndian(this)) {
			mobileNumberToBePrefilled = ConnectivityUtils.getMobileNumber(this, mobileNumberLength);
		}
		if (StringUtils.isNullOrEmpty(mobileNumberToBePrefilled)) {
			mobileNumberToBePrefilled = GrouponGoPrefs.getSavedUserDetails(this).getUserPhone();
		}
		if (StringUtils.isNullOrEmpty(mobileNumberToBePrefilled)) {
			mEdtMobileNumber.setText(mCountryCodeWithHyphen);
			mEdtMobileNumber.setSelection(mCountryCodeWithHyphen.length());
		} else {
			mEdtMobileNumber.setText(mCountryCodeWithHyphen + mobileNumberToBePrefilled);
			mEdtMobileNumber.setSelection(mCountryCodeWithHyphen.length() + mobileNumberToBePrefilled.length());
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.layout_below_spinner: {
			// mSpinnerCountryName.performClick();
			// setSpinnerOnClickListener();
			break;
		}
		case R.id.img_groupon_header_right_btn: {
			showNumberConfirmationDialog();
			break;
		}
		case R.id.txv_groupon_header: {
			finish();
			break;
		}
		case R.id.txv_terms: {
			if (ConnectivityUtils.isNetworkEnabled(this) || GrouponGoFiles.isTermsOfUseCached(this)) {
				Intent intent = new Intent(this, CommonWebViewActivity.class);
				intent.putExtra(ApiConstants.PARAM_PAGE_ID, ApiConstants.VALUE_HTML_TERMS_OF_USE);
				startActivity(intent);
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		}
	}

	/**
	 * TODO - code is changed to hard-code country code
	 */
	private void showNumberConfirmationDialog() {
		mUserNumber = mEdtMobileNumber.getText().toString().trim();

		// TODO - below code lines are for hard-code country code
		mUserNumber = mUserNumber.substring(mCountryCodeWithHyphen.length());

		if (mUserNumber.length() == 0 || !TextUtils.isDigitsOnly(mUserNumber) || mUserNumber.charAt(0) == '0') {
			ToastUtils.showToast(this, "Please enter a valid mobile number.");
			return;
		}

		GrouponGoPrefs.setUserPhone(this, mUserNumber);

		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}

		CommonDialog dialog = new CommonDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.USER_REGISTER_DIALOG);
		bundle.putString(Constants.EXTRA_COUNTRY_CODE, mCountryCode);
		bundle.putString(Constants.EXTRA_USER_NUMBER, mUserNumber);
		dialog.setArguments(bundle);
		dialog.show(getSupportFragmentManager(), Constants.TAG_USER_REGISTER_DIALOG);
	}

	/**
	 * 
	 */
	/*
	 * private void setSpinnerOnClickListener() {
	 * mSpinnerCountryName.setOnItemSelectedListener(new
	 * OnItemSelectedListener() {
	 * 
	 * @Override public void onItemSelected(AdapterView<?> parent, View view,
	 * int position, long id) { String countryName =
	 * mListCountryModel.get(position).getCountry_name(); mCountryCode =
	 * mListCountryModel.get(position).getCountry_code();
	 * mTxvCountryName.setText(countryName + " (" + mCountryCode + ")"); }
	 * 
	 * @Override public void onNothingSelected(AdapterView<?> parent) { //
	 * nothing to do here } }); }
	 */

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_COUNTRY_LIST: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			} else {
				ToastUtils.showToast(this, "Country List fetched.");
			}
			break;
		}
		}
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.USER_REGISTER_DIALOG: {
			if (eventData instanceof Boolean && (Boolean) eventData) {
				sendUserRegistrationRequest();
			}
			break;
		}
		}
	}

	/**
	 * Code of this method is changed as below- <br/>
	 * 1. Instead of sending registration request from here, <br/>
	 * 2. now next activity is launched and request is sent from there. <br/>
	 * 3. So that we start SMS listening immediately even before API responds.
	 */
	private void sendUserRegistrationRequest() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		Intent intent = new Intent(this, UserNumberVerifyActivity.class);
		intent.putExtra(Constants.EXTRA_COUNTRY_CODE, mCountryCode);
		intent.putExtra(Constants.EXTRA_USER_NUMBER, mUserNumber);
		intent.putExtra(Constants.EXTRA_FROM_ACTIVITY, Constants.RQ_REGISTRATION_TO_VERIFY_SCREEN);
		startActivityForResult(intent, Constants.RQ_REGISTRATION_TO_VERIFY_SCREEN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Constants.RQ_REGISTRATION_TO_VERIFY_SCREEN: {
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK);
				finish();
			}
			break;
		}
		}
	}
}
