package com.groupon.go.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.PaymentController;
import com.groupon.go.controller.UserCartController;
import com.groupon.go.model.GetUserCartResponse.GetUserCartResult;
import com.groupon.go.model.UserCartModel;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoFiles;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.utils.AnalyticsHelper;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class PaymentSuccessActivity extends GroupOnBaseActivity implements OnClickListener {

	private int			mDealId;
	private String		mMerchantName;
	private String		mCatName;
	private EditText	mEdtEmail;
	private String		mTransxId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_success);

		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.header_payment_summary));

		mEdtEmail = (EditText) findViewById(R.id.edt_email_summary);
		UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(this);
		if (!StringUtils.isNullOrEmpty(userDetails.getEmailId())) {
			mEdtEmail.setText(userDetails.getEmailId());
		}
		findViewById(R.id.txv_continue).setOnClickListener(this);
		mTransxId = getIntent().getStringExtra(Constants.EXTRA_TRANSX_ID);

		showProgressDialog(getString(R.string.prog_loading));
		UserCartController cartController = new UserCartController(this, this);
		cartController.getData(ApiConstants.GET_USER_CART_FROM_CACHE, null);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_USER_CART_FROM_CACHE: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
				return;
			}
			GrouponGoPrefs.setCartItemsCount(this, 0);
			GrouponGoFiles.deleteCachedCartJson(this);
			populateOrderView(serviceResponse);
			break;
		}
		case ApiConstants.GET_SUMMARY_BY_EMAIL: {
			removeProgressDialog();
			clearTopTillHomeActivity();
			ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			if (serviceResponse.isSuccess()) {
				BackGroundService.start(this, ApiConstants.GET_PROFILE_FROM_SERVER);
			}
			break;
		}
		}
	}

	/**
	 * populate order summaryView
	 */
	private void populateOrderView(ServiceResponse serviceResponse) {
		GetUserCartResult userCartResult = (GetUserCartResult) serviceResponse.getResponseObject();
		ArrayList<UserCartModel> mCartitemList = userCartResult.getDeals();
		mDealId = mCartitemList.get(0).getDeal_id();
		mMerchantName = mCartitemList.get(0).getMerchant_name();
		mCatName = mCartitemList.get(0).getCat_name();
		LinearLayout mOrderRootLayout = (LinearLayout) findViewById(R.id.order_item_layout);
		ProjectUtils.populateView(mCartitemList, this, mOrderRootLayout, false, null, false);
		TextView txvGrandTotal = (TextView) findViewById(R.id.total_amount_pay_success);
		txvGrandTotal.setText(getString(R.string.Rs) + " " + StringUtils.getFormatDecimalAmount(userCartResult.getTotal()));

		AnalyticsHelper.onTransaction(this, mTransxId, userCartResult);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.share_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_share: {
			Intent intent = new Intent(this, ShareOptionsActivity.class);
			intent.putExtra(Constants.EXTRA_DEAL_ID, mDealId);
			intent.putExtra(Constants.EXTRA_MERCHANT_NAME, mMerchantName);
			intent.putExtra(Constants.EXTRA_CAT_NAME, mCatName);
			intent.putExtra(Constants.EXTRA_MESSAGE_TYPE, ApiConstants.VALUE_MESSAGE_TYPE_PURCHASED_DEAL);
			startActivity(intent);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	public void onBackPressed() {
		sendGetEmailRequest();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txv_continue: {
			sendGetEmailRequest();
			break;
		}
		}
	}

	/**
	 * 
	 */
	private void sendGetEmailRequest() {
		String email = mEdtEmail.getText().toString().trim();
		if (StringUtils.isNullOrEmpty(email)) {
			clearTopTillHomeActivity();
			return;
		}
		if (!StringUtils.isValidEmail(email, false)) {
			ToastUtils.showToast(this, getString(R.string.error_invalid_email));
			return;
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putString(Constants.EXTRA_TRANSX_ID, mTransxId);
		bundle.putString(Constants.EXTRA_EMAIL_ID, email);
		showProgressDialog(getString(R.string.prog_loading));
		new PaymentController(this, this).getData(ApiConstants.GET_SUMMARY_BY_EMAIL, bundle);
	}
}
