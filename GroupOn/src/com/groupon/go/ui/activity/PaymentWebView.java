package com.groupon.go.ui.activity;

import org.apache.http.util.EncodingUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.MenuItemCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.TextView;

import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.PaymentController;
import com.groupon.go.model.CreateOrderResponse;
import com.groupon.go.model.PaymentDetailModel;
import com.groupon.go.model.PaymentStatusResponse.PaymentStatusResult;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.service.BackGroundService;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.widget.CustomTextView;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.DateTimeUtils;
import com.kelltontech.utils.DateTimeUtils.Format;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class PaymentWebView extends GroupOnBaseActivity {

	private static String		LOG_TAG	= "PaymentWebView";
	private WebView				mWebView;
	private PaymentDetailModel	mPaymentDetailModel;
	private String				mPayMode;
	private boolean				mIsPaymentFromSavedCard;
	private String				mCardToken;
	private String				mCvv;
	private PaymentController	mPaymentController;
	private CustomTextView		mTxvSessionTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_webview);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.header_payment));

		GrouponGoPrefs.increasePaymentMessageIndex(this);

		mPaymentController = new PaymentController(this, this);

		mWebView = (WebView) findViewById(R.id.paymentWebview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setFocusable(true);
		mWebView.setFocusableInTouchMode(true);
		mPayMode = getIntent().getStringExtra(Constants.EXTRA_PAYMENT_MODE);

		mCardToken = getIntent().getStringExtra(ApiConstants.PARAM_CARD_TOKEN);
		if (!StringUtils.isNullOrEmpty(mCardToken)) {
			mIsPaymentFromSavedCard = true;
			mCvv = getIntent().getStringExtra(ApiConstants.PARAM_CVV);
		}
		sendCreateOrderRequest();
		mWebView.clearHistory();
		mWebView.clearFormData();
		WebViewDatabase.getInstance(this).clearFormData();
		mWebView.setWebViewClient(new PaymentWebViewClient());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.payment_webview_menu, menu);
		MenuItem menuSessionTimer = menu.findItem(R.id.action_session_timer);
		View actionView = MenuItemCompat.getActionView(menuSessionTimer);
		if (actionView instanceof TextView) {
			mTxvSessionTimer = (CustomTextView) actionView.findViewById(R.id.txv_session_timer);
		}
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 
	 */
	private void sendCreateOrderRequest() {
		if (mIsPaymentFromSavedCard) {
			sendPayRequestToServerFromSavedCard();
		} else {
			sendPayRequestToServer();
		}
	}

	private void sendPayRequestToServerFromSavedCard() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		Bundle data = new Bundle();
		data.putString(ApiConstants.PARAM_PG, mPayMode);
		data.putString(ApiConstants.PARAM_CVV, mCvv);
		data.putString(ApiConstants.PARAM_CARD_TOKEN, mCardToken);
		mPaymentController.getData(ApiConstants.GET_CREATE_ORDER_FRM_CARD, data);
	}

	private void sendPayRequestToServer() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		Bundle bundle = new Bundle();
		bundle.putString(ApiConstants.PARAM_PG, mPayMode);
		mPaymentController.getData(ApiConstants.GET_CREATE_ORDER, bundle);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			ToastUtils.showToast(this, getString(R.string.msg_transaction_cancelled));
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ToastUtils.showToast(this, getString(R.string.msg_transaction_cancelled));
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_CREATE_ORDER_FRM_CARD:
		case ApiConstants.GET_CREATE_ORDER: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				if (serviceResponse.getResponseObject() instanceof CreateOrderResponse) {
					CreateOrderResponse response = (CreateOrderResponse) serviceResponse.getResponseObject();
					if (response.getError_code() == ApiConstants.ERROR_CODE_OFFER_OUT_OF_STOCK) {
						showCustomAlert(Events.ALERT_SERVER_MSG_TO_REVIEW_CART, response.getMessage(), Constants.TAG_ALERT_SERVER_MSG_TO_REVIEW_CART);
					}
				} else {
					ToastUtils.showToast(this, serviceResponse.getErrorMessage());
					finish();
				}
				return;
			}
			mPaymentDetailModel = (PaymentDetailModel) serviceResponse.getResponseObject();
			if (mPaymentDetailModel.getSession_period() > 0) {
				new CountDownTimer(mPaymentDetailModel.getSession_period() * DateUtils.SECOND_IN_MILLIS, DateUtils.SECOND_IN_MILLIS) {
					@Override
					public void onTick(long millisUntilFinished) {
						mTxvSessionTimer.setText(DateTimeUtils.getFormattedDuration(millisUntilFinished, Format.M_SS));
					}

					@Override
					public void onFinish() {
						mPaymentDetailModel.setSessionExpired(true);
						getPaymentStatusFromServer();
					}
				}.start();
			}
			String postData = "txnid=" + mPaymentDetailModel.getTxnid() + "&surl=" + mPaymentDetailModel.getSurl() + "&furl=" + mPaymentDetailModel.getFurl() + "&hash=" + mPaymentDetailModel.getHash() + "&amount=" + mPaymentDetailModel.getAmount() + "&email=" + mPaymentDetailModel.getEmail() + "&key=" + mPaymentDetailModel.getKey() + "&firstname=" + mPaymentDetailModel.getFirstname() + "&lastname=" + mPaymentDetailModel.getLastname() + "&salt=" + mPaymentDetailModel.getSalt() + "&pg=" + mPaymentDetailModel.getPg() + "&productinfo=" + mPaymentDetailModel.getProductinfo() + "&user_credentials=" + mPaymentDetailModel.getUser_credentials();
			if (mIsPaymentFromSavedCard) {
				postData = postData + "&store_card_token=" + mCardToken + "&ccvv=" + mCvv;
			} else {
				String userCredential = getIntent().getStringExtra(Constants.EXTRA_USER_CREDENTIAL);
				postData = postData + userCredential;
			}
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Post Data: " + postData);
			}
			mWebView.postUrl(mPaymentDetailModel.getRedirectUrl(), EncodingUtils.getBytes(postData, "BASE64"));
			break;
		}
		case ApiConstants.GET_PAYMENT_STATUS: {
			removeProgressDialog();
			Intent intent = null;
			if (serviceResponse.isSuccess()) {
				PaymentStatusResult paymentStatusResult = (PaymentStatusResult) serviceResponse.getResponseObject();
				String status = paymentStatusResult.getStatus();
				if (status.equalsIgnoreCase("success")) {
					BackGroundService.start(this, ApiConstants.GET_MY_PURCHASES);
					intent = new Intent(this, PaymentSuccessActivity.class);
					intent.putExtra(Constants.EXTRA_TRANSX_ID, mPaymentDetailModel.getTxnid());
				} else if (status.equalsIgnoreCase("failure")) {
					intent = new Intent(this, PaymentFailureActivity.class);
				}
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			if (intent != null) {
				startActivity(intent);
			}
			finish();
		}
		}
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.CUSTOM_ALERT_POSITIVE_BUTTON: {
			if (eventData instanceof Integer) {
				int alertId = (Integer) eventData;
				switch (alertId) {
				case Events.ALERT_SERVER_MSG_TO_REVIEW_CART: {
					Intent intent = new Intent(PaymentWebView.this, UserCart.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					break;
				}
				}
				break;
			}
		}
		}
	}

	/**
	 * @author vineet.rajpoot
	 */
	private class PaymentWebViewClient extends android.webkit.WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			showProgressDialog(getString(R.string.prog_loading));
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "onPageStarted: " + url);
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			removeProgressDialog();
			view.requestFocus();
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "onPageFinished: " + url);
			}
			if (url.startsWith(mPaymentDetailModel.getSurl())) {
				getPaymentStatusFromServer();
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "shouldOverrideUrlLoading: " + url);
			}
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			try {
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, " " + error + "  ------>" + handler);
				}
				if (handler != null) {
					handler.proceed();
				}
			} catch (Exception e) {
				if (BuildConfig.DEBUG) {
					Log.e(LOG_TAG, "" + e);
				}
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Log.i(LOG_TAG, " " + description);
		}
	}

	/**
	 * 
	 */
	public void getPaymentStatusFromServer() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mPaymentController.getData(ApiConstants.GET_PAYMENT_STATUS, mPaymentDetailModel);
	}

	@Override
	protected void onDestroy() {
		mWebView.stopLoading();
		super.onDestroy();
	}
}
