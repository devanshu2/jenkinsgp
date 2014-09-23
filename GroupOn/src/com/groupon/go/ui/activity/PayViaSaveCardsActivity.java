package com.groupon.go.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.PaymentController;
import com.groupon.go.model.GetSavedCardsResponse.GetSavedCardsResult;
import com.groupon.go.model.SavedCardsModel;
import com.groupon.go.ui.adapter.SavedCardsPagerAdapter;
import com.groupon.go.ui.widget.CirclePageIndicator;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class PayViaSaveCardsActivity extends GroupOnBaseActivity implements OnPageChangeListener, OnClickListener {

	private static final String			LOG_TAG	= "PayViaSaveCardsActivity";

	private ViewPager					mCardsPager;
	private ArrayList<SavedCardsModel>	mSavedCardsListToBeShown;
	private SavedCardsModel				mCurrentSelectCard;
	private TextView					mTxvCardName;
	private EditText					mEdtCvv;
	private int							mCvvMaxAllowedDigits;
	private float						mGrandTotal;

	private TextView					mTxvBtnPayAmount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pay_via_save_cards);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.header_payment));

		mCardsPager = (ViewPager) findViewById(R.id.pager_saved_cards);
		int pagerPosition = 0;
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(Constants.STATE_PAGER_INDEX);
		}
		mCardsPager.setCurrentItem(pagerPosition);
		mCardsPager.setOffscreenPageLimit(2);

		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20 * 2, getResources().getDisplayMetrics());
		mCardsPager.setPageMargin(-margin);

		mTxvCardName = (TextView) findViewById(R.id.txv_card_name);

		mEdtCvv = (EditText) findViewById(R.id.edt_cvv);
		mEdtCvv.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					startPaymentActivity();
					return true;
				}
				return false;
			}
		});
		mEdtCvv.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// nothing to do here
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// nothing to do here
			}

			@Override
			public void afterTextChanged(Editable s) {
				mTxvBtnPayAmount.setEnabled(s.length() >= mCvvMaxAllowedDigits);
			}
		});

		mGrandTotal = getIntent().getFloatExtra(Constants.EXTRA_GRAND_TOTAL, 0);

		mTxvBtnPayAmount = (TextView) findViewById(R.id.txv_pay_save_card);
		mTxvBtnPayAmount.setOnClickListener(this);
		mTxvBtnPayAmount.setText(getString(R.string.button_pay_amount, StringUtils.getFormatDecimalAmount(mGrandTotal)));

		findViewById(R.id.txv_btn_use_new_card).setOnClickListener(this);
		findViewById(R.id.txv_btn_pay_via_net_banking).setOnClickListener(this);

		showProgressDialog(getString(R.string.prog_loading));
		PaymentController paymentController = new PaymentController(this, this);
		paymentController.getData(ApiConstants.GET_SAVED_CARDS, null);
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
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_SAVED_CARDS: {
			removeProgressDialog();
			if (serviceResponse.getResponseObject() instanceof GetSavedCardsResult) {
				GetSavedCardsResult getSavedCardsResult = (GetSavedCardsResult) serviceResponse.getResponseObject();
				mSavedCardsListToBeShown = getSavedCardsResult.getCards();
				if (mSavedCardsListToBeShown.isEmpty()) {
					startNextActivity(true);
					finish();
				} else {
					populatePagerView();
				}
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
			}
			break;
		}
		}
	}

	/**
	 * populate pager view with mSavedCardsListToBeShown
	 */
	private void populatePagerView() {
		SavedCardsPagerAdapter adapter = new SavedCardsPagerAdapter(mSavedCardsListToBeShown, this);
		mCardsPager.setAdapter(adapter);
		CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(mCardsPager);
		indicator.setOnPageChangeListener(this);
		updateScreenOnCardSelectionChange();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(Constants.STATE_PAGER_INDEX, mCardsPager.getCurrentItem());
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// Nothing to do here
	}

	@Override
	public void onPageScrolled(int position, float arg1, int arg2) {
		updateScreenOnCardSelectionChange();
	}

	/**
	 * @param position
	 */
	private void updateScreenOnCardSelectionChange() {
		int position = mCardsPager.getCurrentItem();
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "updateScreenOnCardSelectionChange() position: " + position);
		}
		mCurrentSelectCard = mSavedCardsListToBeShown.get(position);
		mTxvCardName.setText(mCurrentSelectCard.getCard_name());
		if (ApiConstants.VALUE_CARD_TYPE_AMERICAN_EXPRESS.equalsIgnoreCase(mCurrentSelectCard.getCard_brand())) {
			mCvvMaxAllowedDigits = 4;
		} else {
			mCvvMaxAllowedDigits = 3;
		}
		InputFilter[] FilterArray = { new InputFilter.LengthFilter(mCvvMaxAllowedDigits) };
		mEdtCvv.setFilters(FilterArray);
		mEdtCvv.setText("");
	}

	@Override
	public void onPageSelected(int position) {
		// Nothing to do here
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txv_pay_save_card: {
			startPaymentActivity();
			break;
		}
		case R.id.txv_btn_use_new_card: {
			startNextActivity(true);
			break;
		}
		case R.id.txv_btn_pay_via_net_banking: {
			startNextActivity(false);
			break;
		}
		}
	}

	/**
	 * validates CVV input and starts Payment Activity
	 */
	private void startPaymentActivity() {
		String cvv = mEdtCvv.getText().toString();
		if (cvv.trim().length() < mCvvMaxAllowedDigits) {
			ToastUtils.showToast(getApplicationContext(), getString(R.string.toast_enter_valid_cvv));
			return;
		}
		Intent intent = new Intent(this, PaymentWebView.class);
		intent.putExtra(Constants.EXTRA_PAYMENT_MODE, mCurrentSelectCard.getCard_mode());
		intent.putExtra(ApiConstants.PARAM_CVV, cvv);
		intent.putExtra(ApiConstants.PARAM_CARD_TOKEN, mCurrentSelectCard.getCard_token());
		startActivity(intent);
	}

	/**
	 * starts AddNewCardActivity or PayViaNetBankingActivity
	 */
	private void startNextActivity(boolean addNewCard) {
		Intent intent = null;
		if (addNewCard) {
			intent = new Intent(this, AddNewCardActivity.class);
		} else {
			if (!ConnectivityUtils.isNetworkEnabled(this)) {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
				return;
			}
			intent = new Intent(this, PayViaNetBankingActivity.class);
		}
		intent.putExtra(Constants.EXTRA_GRAND_TOTAL, mGrandTotal);
		startActivity(intent);
	}
}
