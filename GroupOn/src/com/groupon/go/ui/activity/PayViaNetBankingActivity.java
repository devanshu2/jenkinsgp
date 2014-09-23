package com.groupon.go.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.PaymentController;
import com.groupon.go.model.GetAllBanksResponse.BankDetailModel;
import com.groupon.go.model.GetAllBanksResponse.GetAllBankResult;
import com.groupon.go.ui.adapter.BankListAdapter;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.kumar, sachin.gupta
 */
public class PayViaNetBankingActivity extends GroupOnBaseActivity implements OnClickListener, OnItemClickListener {

	private BankListAdapter				mBanksListAdapter;
	private BankDetailModel				mSelectedBankModel;
	private EditText					mEdtSearch;
	private ArrayList<BankDetailModel>	mBanksArrayList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pay_via_net_banking);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.header_net_banking));

		findViewById(R.id.img_search_cross).setOnClickListener(this);

		float grandTotal = getIntent().getFloatExtra(Constants.EXTRA_GRAND_TOTAL, 0);
		TextView payAmount = (TextView) findViewById(R.id.txv_pay_net_banking);
		payAmount.setText(getString(R.string.button_pay_amount, StringUtils.getFormatDecimalAmount(grandTotal)));
		payAmount.setOnClickListener(this);

		showProgressDialog(getString(R.string.prog_loading));
		PaymentController paymentController = new PaymentController(this, this);
		paymentController.getData(ApiConstants.GET_ALL_BANK, null);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_ALL_BANK: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
				return;
			}
			GetAllBankResult getAllBankResult = (GetAllBankResult) serviceResponse.getResponseObject();
			populateBankList(getAllBankResult);
			break;
		}
		}
	}

	/**
	 * @param getAllBankResult
	 */
	private void populateBankList(GetAllBankResult getAllBankResult) {
		ListView bankListView = (ListView) findViewById(R.id.list_bank);
		mBanksArrayList = getAllBankResult.getBanks();
		mBanksListAdapter = new BankListAdapter(this, mBanksArrayList);
		bankListView.setAdapter(mBanksListAdapter);
		bankListView.setOnItemClickListener(this);
		initailizeSearch();
	}

	/**
	 * 
	 */
	private void initailizeSearch() {
		mEdtSearch = (EditText) findViewById(R.id.edt_search_contacts);
		mEdtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// nothing to do here
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// nothing to do here
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mBanksListAdapter.getFilter().filter(s);
			}
		});
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
		case R.id.txv_pay_net_banking: {
			sendToPaymentWebView();
			break;
		}
		case R.id.img_search_cross: {
			mEdtSearch.setText("");
			break;
		}
		}
	}

	/**
	 * Called from onClick and also from onItemClick
	 */
	private void sendToPaymentWebView() {
		if (mSelectedBankModel == null || !mSelectedBankModel.isSelected()) {
			ToastUtils.showToast(this, getString(R.string.msg_select_bank));
			return;
		}
		if (ApiConstants.VALUE_NB_CODE_CITY_BANK.equalsIgnoreCase(mSelectedBankModel.getCode())) {
			ToastUtils.showToast(this, getString(R.string.msg_citi_bank_net_banking));
			return;
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		String userCredential = "&bankcode=" + mSelectedBankModel.getCode();
		Intent intent = new Intent(this, PaymentWebView.class);
		intent.putExtra(Constants.EXTRA_PAYMENT_MODE, ApiConstants.VALUE_PAY_BY_NET_BANKING);
		intent.putExtra(Constants.EXTRA_USER_CREDENTIAL, userCredential);
		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		mSelectedBankModel = mBanksListAdapter.getItem(position);
		for (int i = 0; i < mBanksArrayList.size(); i++) {
			BankDetailModel bankDetailModel = mBanksArrayList.get(i);
			if (i != position) {
				bankDetailModel.setSelected(false);
			} else {
				bankDetailModel.setSelected(true);
			}
		}
		mBanksListAdapter.notifyDataSetChanged();
		sendToPaymentWebView();
	}
}
