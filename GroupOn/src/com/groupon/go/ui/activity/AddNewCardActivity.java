package com.groupon.go.ui.activity;

import java.util.Calendar;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.PaymentController;
import com.groupon.go.model.GetPayUConfigResponse.GetPayUConfigResult;
import com.groupon.go.model.PayUCardVerificationResponse;
import com.groupon.go.model.UserCardResponse;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.widget.DatePickerFragment;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.DateTimeUtils;
import com.kelltontech.utils.EncryptionUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;
import com.kelltontech.utils.UiUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class AddNewCardActivity extends GroupOnBaseActivity implements OnClickListener, OnCheckedChangeListener {

	private TextView			mRdBtnCreditCard;
	private TextView			mRdBtnDebitCard;
	private EditText			mEdtUserName;
	private EditText			mEdtUserNumber;
	private EditText			mEdtCvv;
	private TextView			mTxvExpiryMmYyyy;
	private CheckBox			mChbxSaveCard;
	private EditText			mEdtCardName;

	/**
	 * same value is being used as payment mode, card-type and bank-mode as well
	 */
	private String				mSelectedCardType;
	private boolean				mIsCreditCardSelected;
	private String				mUserName;
	private String				mUserCardNumber;
	private String				mCardName;
	private int					mExpiryYear;
	private int					mExpiryMonthIndex;
	private String				mExpiryMonthStr;
	private String				mExpiryMmYyyyStr;
	private String				mCvv;

	private int					mRequestFrom;
	private float				mGrandTotal;
	private PaymentController	mPaymentController;
	private GetPayUConfigResult	mPayUConfigResult;
	private boolean				mIsUserNameUpdateRequested;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new_card);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.button_add_your_card));

		mRdBtnCreditCard = (TextView) findViewById(R.id.rdbtn_credit_card);
		mRdBtnDebitCard = (TextView) findViewById(R.id.rdbtn_debit_card);
		mEdtUserName = (EditText) findViewById(R.id.edt_user_name);
		mEdtUserNumber = (EditText) findViewById(R.id.edt_user_card_num);
		mTxvExpiryMmYyyy = (TextView) findViewById(R.id.txv_exp_date);
		mEdtCvv = (EditText) findViewById(R.id.edt_user_cvv);
		mChbxSaveCard = (CheckBox) findViewById(R.id.chbx_save_card);
		mEdtCardName = (EditText) findViewById(R.id.edt_card_name);

		mRequestFrom = getIntent().getIntExtra(Constants.EXTRA_FROM_ACTIVITY, 0);

		TextView txvBtnSaveOrPay = (TextView) findViewById(R.id.txv_btn_pay_via_card);

		if (mRequestFrom == Constants.RQ_USER_CARDS_TO_ADD_NEW_CARD) {
			findViewById(R.id.txv_card_name_tag).setVisibility(View.VISIBLE);
			findViewById(R.id.lay_card_name).setVisibility(View.VISIBLE);
			findViewById(R.id.lay_cvv).setVisibility(View.GONE);
			findViewById(R.id.txv_btn_pay_via_net_banking).setVisibility(View.GONE);
			findViewById(R.id.view_divider_above_net_banking).setVisibility(View.GONE);
			findViewById(R.id.view_divider_below_net_banking).setVisibility(View.GONE);
		} else {
			mGrandTotal = getIntent().getFloatExtra(Constants.EXTRA_GRAND_TOTAL, 0);
			txvBtnSaveOrPay.setText(getString(R.string.button_pay_amount, StringUtils.getFormatDecimalAmount(mGrandTotal)));
			mChbxSaveCard.setVisibility(View.VISIBLE);
			findViewById(R.id.txv_btn_pay_via_net_banking).setOnClickListener(this);
		}

		txvBtnSaveOrPay.setOnClickListener(this);
		mRdBtnCreditCard.setOnClickListener(this);
		mRdBtnDebitCard.setOnClickListener(this);
		mChbxSaveCard.setOnCheckedChangeListener(this);
		mTxvExpiryMmYyyy.setOnClickListener(this);

		mExpiryMmYyyyStr = getString(R.string.hint_mm_yyyy);

		mIsCreditCardSelected = true;
		updateCardTypeAndPaymentMode(false);

		mPaymentController = new PaymentController(this, this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rdbtn_credit_card: {
			if (!mIsCreditCardSelected) {
				mIsCreditCardSelected = true;
				updateCardTypeAndPaymentMode(true);
			}
			break;
		}
		case R.id.rdbtn_debit_card: {
			if (mIsCreditCardSelected) {
				mIsCreditCardSelected = false;
				updateCardTypeAndPaymentMode(true);
			}
			break;
		}
		case R.id.txv_exp_date: {
			openDateSelector();
			break;
		}
		case R.id.txv_btn_pay_via_card: {
			validateUserDetail();
			break;
		}
		case R.id.txv_btn_pay_via_net_banking: {
			startPayViaNetBankingActivity();
			break;
		}
		}
	}

	/**
	 * achieve radio button functionality
	 */
	private void updateCardTypeAndPaymentMode(boolean isDataCleared) {
		if (mIsCreditCardSelected) {
			mRdBtnCreditCard.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_radio_on_disabled_focused_holo_light, 0, 0, 0);
			mRdBtnDebitCard.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_radio_off_disabled_focused_holo_light, 0, 0, 0);
			mSelectedCardType = ApiConstants.VALUE_CREDIT_CARD;
		} else {
			mRdBtnCreditCard.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_radio_off_disabled_focused_holo_light, 0, 0, 0);
			mRdBtnDebitCard.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_radio_on_disabled_focused_holo_light, 0, 0, 0);
			mSelectedCardType = ApiConstants.VALUE_DEBIT_CARD;
		}
		if (isDataCleared) {
			clearAllInputs();
		}
	}

	/**
	 * open date fragment for expiry month/year selection
	 */
	private void openDateSelector() {
		DatePickerFragment datePickerFragment = new DatePickerFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(DatePickerFragment.EXTRA_SHOW_ONLY_MM_YYYY, true);
		if (mExpiryYear != 0) {
			bundle.putInt(DatePickerFragment.EXTRA_YEAR, mExpiryYear);
			bundle.putInt(DatePickerFragment.EXTRA_MONTH_INDEX, mExpiryMonthIndex);
			bundle.putInt(DatePickerFragment.EXTRA_DAY_OF_MONTH, 1);
		} else {
			bundle.putBoolean(DatePickerFragment.EXTRA_OPEN_WITH_TODAY, true);
		}
		datePickerFragment.setArguments(bundle);
		datePickerFragment.show(getSupportFragmentManager(), DatePickerFragment.TAG_DATE_PICKER_DIALOG);
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.DATE_PICKER_DIALOG: {
			if (eventData instanceof Calendar) {
				Calendar calendar = (Calendar) eventData;
				int[] expiryDateFields = DateTimeUtils.getDateFields(calendar.getTimeInMillis());
				mExpiryYear = expiryDateFields[0];
				mExpiryMonthIndex = expiryDateFields[1];
				int month = mExpiryMonthIndex + 1;
				mExpiryMonthStr = (month < 10 ? "0" : "") + month;
				mExpiryMmYyyyStr = mExpiryMonthStr + "/" + mExpiryYear;
				mTxvExpiryMmYyyy.setText(mExpiryMmYyyyStr);
			}
			break;
		}
		}
	}

	/**
	 * validate user card input
	 */
	private void validateUserDetail() {
		mUserName = mEdtUserName.getText().toString();
		if (StringUtils.isNullOrEmpty(mUserName)) {
			ToastUtils.showToast(this, getString(R.string.toast_enter_name_as_on_card));
			return;
		}

		mUserCardNumber = mEdtUserNumber.getText().toString();
		if (StringUtils.isNullOrEmpty(mUserCardNumber)) {
			ToastUtils.showToast(this, getString(R.string.toast_enter_card_num));
			return;
		}

		if (mUserCardNumber.trim().length() < 12) {
			ToastUtils.showToast(this, getString(R.string.toast_enter_valid_card_num));
			return;
		}

		if (mExpiryMmYyyyStr.equalsIgnoreCase(getString(R.string.hint_mm_yyyy))) {
			ToastUtils.showToast(this, getString(R.string.toast_select_expiry_date));
			return;
		}

		if (mRequestFrom != Constants.RQ_USER_CARDS_TO_ADD_NEW_CARD) {
			mCvv = mEdtCvv.getText().toString().trim();
			if (StringUtils.isNullOrEmpty(mCvv) || mCvv.length() < 3) {
				ToastUtils.showToast(this, getString(R.string.toast_enter_valid_cvv));
				return;
			}

			if (mChbxSaveCard.isChecked()) {
				mCardName = mEdtCardName.getText().toString();
				if (StringUtils.isNullOrEmpty(mCardName)) {
					ToastUtils.showToast(this, getString(R.string.toast_enter_card_name));
					return;
				}
			} else {
				mCardName = "";
			}
		} else {
			mCardName = mEdtCardName.getText().toString();
			if (StringUtils.isNullOrEmpty(mCardName)) {
				ToastUtils.showToast(this, getString(R.string.toast_enter_card_name));
				return;
			}
		}

		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}

		showProgressDialog(getString(R.string.prog_loading));
		mPaymentController.getData(ApiConstants.GET_PAYU_CONFIGURATION, null);

		UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(this);
		if (StringUtils.isNullOrEmpty(userDetails.getUsername())) {
			BackGroundService.updateUserName(this, mUserName);
			mIsUserNameUpdateRequested = true;
		}
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_PAYU_CONFIGURATION: {
			if (!serviceResponse.isSuccess()) {
				removeProgressDialog();
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				return;
			}
			mPayUConfigResult = (GetPayUConfigResult) serviceResponse.getResponseObject();
			String regexBasedCardType = ProjectUtils.getRegexBasedCardType(mUserCardNumber);
			if (StringUtils.isNullOrEmpty(regexBasedCardType)) {
				sendPayUCardVerificationRequest();
			} else {
				continueToAddCardOrToPayViaCard(regexBasedCardType);
			}
			break;
		}
		case ApiConstants.GET_SAVE_USER_CARD: {
			removeProgressDialog();
			if (serviceResponse.getResponseObject() instanceof UserCardResponse) {
				UserCardResponse saveUserCardResponse = (UserCardResponse) serviceResponse.getResponseObject();
				ToastUtils.showToast(this, saveUserCardResponse.getMsg());
				if (saveUserCardResponse.getStatus() != ApiConstants.PAYU_STATUS_FAIL) {
					if (mIsUserNameUpdateRequested) {
						setResult(RESULT_FIRST_USER);
					} else {
						setResult(RESULT_OK);
					}
					finish();
				}
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		case ApiConstants.GET_PAYU_CONFIG_TO_VERIFY_CARD: {
			if (serviceResponse.getResponseObject() instanceof PayUCardVerificationResponse) {
				PayUCardVerificationResponse cardVerifResp = (PayUCardVerificationResponse) serviceResponse.getResponseObject();
				if (!mSelectedCardType.equalsIgnoreCase(cardVerifResp.getCardCategory())) {
					removeProgressDialog();
					ToastUtils.showToast(this, getString(R.string.msg_wrong_card_type));
					return;
				}
				if (cardVerifResp.getIssuingBank().equalsIgnoreCase(ApiConstants.VALUE_CARD_TYPE_UNKNOWN)) {
					removeProgressDialog();
					ToastUtils.showToast(this, getString(R.string.msg_unknown_card_type));
					return;
				}
				if (!mIsCreditCardSelected && cardVerifResp.getCardType().equalsIgnoreCase(ApiConstants.VALUE_CARD_TYPE_UNKNOWN)) {
					removeProgressDialog();
					ToastUtils.showToast(this, getString(R.string.msg_unknown_card_type));
					return;
				}
				continueToAddCardOrToPayViaCard(cardVerifResp);
			} else {
				removeProgressDialog();
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		}
	}

	/**
	 * @param pCardTypeOrCardVerifResp
	 */
	private void continueToAddCardOrToPayViaCard(Object pCardTypeOrCardVerifResp) {
		if (mRequestFrom == Constants.RQ_USER_CARDS_TO_ADD_NEW_CARD) {
			HashMap<String, String> params = createParamsForSaveCard(pCardTypeOrCardVerifResp);
			mPaymentController.getData(ApiConstants.GET_SAVE_USER_CARD, params);
		} else {
			removeProgressDialog();
			Intent intent = new Intent(this, PaymentWebView.class);
			intent.putExtra(Constants.EXTRA_PAYMENT_MODE, mSelectedCardType);
			intent.putExtra(Constants.EXTRA_USER_CREDENTIAL, createUserCredentialString(pCardTypeOrCardVerifResp));
			startActivity(intent);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void sendPayUCardVerificationRequest() {
		String bankIdentificationNumber = mUserCardNumber.substring(0, 6);
		String key = mPayUConfigResult.getKey() + "|" + mPayUConfigResult.getCommand_is_domestic() + "|" + bankIdentificationNumber + "|" + mPayUConfigResult.getSalt();
		String hash = EncryptionUtils.convertStringToSha(key);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(ApiConstants.PARAM_PAYU_KEY, mPayUConfigResult.getKey());
		params.put(ApiConstants.PARAM_PAYU_COMMAND, mPayUConfigResult.getCommand_is_domestic());
		params.put(ApiConstants.PARAM_PAYU_HASH, hash);
		params.put(ApiConstants.PARAM_CARD_BIN, bankIdentificationNumber);
		params.put(ApiConstants.PARAM_PAYU_REDIRECT_URL, mPayUConfigResult.getService_url());

		mPaymentController.getData(ApiConstants.GET_PAYU_CONFIG_TO_VERIFY_CARD, params);
	}

	/**
	 * @param pCardTypeOrCardVerifResp
	 * @return
	 */
	private String createUserCredentialString(Object pCardTypeOrCardVerifResp) {
		String valueStoreCard = null;
		if (mChbxSaveCard.isChecked()) {
			valueStoreCard = ApiConstants.VALUE_STORE_CARD_ON_PAYMENT;
		} else {
			valueStoreCard = ApiConstants.VALUE_NOT_STORE_CARD_ON_PAYMENT;
		}
		String bankCode = mSelectedCardType;
		if (pCardTypeOrCardVerifResp instanceof String) {
			bankCode = (String) pCardTypeOrCardVerifResp;
		} else if (pCardTypeOrCardVerifResp instanceof PayUCardVerificationResponse && !mIsCreditCardSelected) {
			bankCode = ((PayUCardVerificationResponse) pCardTypeOrCardVerifResp).getCardType();
		}
		String userCredential = "&bankcode=" + bankCode + "&ccnum=" + mUserCardNumber + "&ccname=" + mUserName + "&ccvv=" + mCvv + "&ccexpmon=" + mExpiryMonthStr + "&ccexpyr=" + mExpiryYear + "&card_name=" + mCardName + "&store_card=" + valueStoreCard;
		return userCredential;
	}

	/**
	 * 
	 * @param pCardTypeOrCardVerifResp
	 * @return
	 */
	private HashMap<String, String> createParamsForSaveCard(Object pCardTypeOrCardVerifResp) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(ApiConstants.PARAM_PAYU_KEY, mPayUConfigResult.getKey());
		params.put(ApiConstants.PARAM_PAYU_COMMAND, mPayUConfigResult.getCommand());
		params.put(ApiConstants.PARAM_PAYU_HASH, mPayUConfigResult.getHash());

		params.put(ApiConstants.PARAM_USER_CREDENTIAL, mPayUConfigResult.getUser_token());
		params.put(ApiConstants.PARAM_CARD_NICK_NAME, mCardName);

		String cardType = mSelectedCardType;
		if (pCardTypeOrCardVerifResp instanceof String) {
			cardType = (String) pCardTypeOrCardVerifResp;
		} else if (pCardTypeOrCardVerifResp instanceof PayUCardVerificationResponse && !mIsCreditCardSelected) {
			cardType = ((PayUCardVerificationResponse) pCardTypeOrCardVerifResp).getCardType();
		}
		params.put(ApiConstants.PARAM_CARD_TYPE, cardType);

		if (cardType.equals(ApiConstants.VALUE_CARD_TYPE_AMERICAN_EXPRESS)) {
			params.put(ApiConstants.PARAM_CARD_MODE, ApiConstants.VALUE_CREDIT_CARD);
		} else {
			params.put(ApiConstants.PARAM_CARD_MODE, mSelectedCardType);
		}

		params.put(ApiConstants.PARAM_NAME_AS_ON_CARD, mUserName);
		params.put(ApiConstants.PARAM_CARD_NUMBER, mUserCardNumber);
		params.put(ApiConstants.PARAM_CARD_EXP_MONTH, mExpiryMonthStr);
		params.put(ApiConstants.PARAM_CARD_EXP_YEAR, "" + mExpiryYear);
		params.put(ApiConstants.PARAM_PAYU_REDIRECT_URL, mPayUConfigResult.getService_url());

		return params;
	}

	/**
	 * @note {@link AddNewCardActivity#mExpiryMonthIndex} and
	 *       {@link AddNewCardActivity#mExpiryMonthStr} are not cleared.
	 */
	private void clearAllInputs() {
		mEdtCardName.setText("");
		mEdtCvv.setText("");
		mEdtUserName.setText("");
		mEdtUserNumber.setText("");
		mCardName = "";
		mCvv = "";
		mUserName = "";
		mUserCardNumber = "";
		mExpiryYear = 0;
		mExpiryMmYyyyStr = getString(R.string.hint_mm_yyyy);
		mTxvExpiryMmYyyy.setText(mExpiryMmYyyyStr);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			UiUtils.expand((TextView) findViewById(R.id.txv_card_name_tag));
			UiUtils.expand((LinearLayout) findViewById(R.id.lay_card_name));
		} else {
			UiUtils.collapse((TextView) findViewById(R.id.txv_card_name_tag));
			UiUtils.collapse((LinearLayout) findViewById(R.id.lay_card_name));
		}
	}

	/**
	 * starts PayViaNetBankingActivity
	 */
	private void startPayViaNetBankingActivity() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		Intent intent = new Intent(this, PayViaNetBankingActivity.class);
		intent.putExtra(Constants.EXTRA_GRAND_TOTAL, mGrandTotal);
		startActivity(intent);
	}
}