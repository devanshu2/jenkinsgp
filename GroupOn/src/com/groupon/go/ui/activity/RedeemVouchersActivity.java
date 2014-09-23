package com.groupon.go.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.application.GrouponGoApp;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.database.MyPurchasesHelper;
import com.groupon.go.model.OfferModel;
import com.groupon.go.model.RedeemVoucherResponse.RedeemVoucherResult;
import com.groupon.go.model.VouchersModel;
import com.groupon.go.service.BackGroundService;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.kumar
 */
public class RedeemVouchersActivity extends GroupOnBaseActivity implements OnClickListener {

	private ArrayList<OfferModel>	mOfferModelList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_redeem_vouchers);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.header_coupon_summary));

		((TextView) findViewById(R.id.txv_continue_coupons)).setOnClickListener(this);

		Bundle data = getIntent().getExtras();
		((TextView) findViewById(R.id.txv_coupon_deal_title)).setText(data.getString(Constants.EXTRA_DEAL_TITLE));
		mOfferModelList = data.getParcelableArrayList(Constants.EXTRA_OFFER_MODEL_LIST);

		GrouponGoApp grouponGoApp = (GrouponGoApp) getApplication();
		MyPurchasesHelper myPurchasesHelper = grouponGoApp.getMyPurchasesHelper();
		RedeemVoucherResult redeemVoucherResult = myPurchasesHelper.redeemVouchers(this, data);
		populateView(redeemVoucherResult);

		BackGroundService.postRedeemedVouchers(this, false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			clearTopTillCouponsActivity();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_REDEEM_VOUCHER: {
			removeProgressDialog();
			if (serviceResponse.isSuccess() && serviceResponse.getResponseObject() instanceof RedeemVoucherResult) {
				RedeemVoucherResult redeemVoucherResult = (RedeemVoucherResult) serviceResponse.getResponseObject();
				populateView(redeemVoucherResult);
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
			}
			break;
		}
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void populateView(RedeemVoucherResult redeemVoucherResult) {
		LinearLayout rootLayout = (LinearLayout) findViewById(R.id.root_layout_coupon_sum);
		ArrayList<VouchersModel> vouchers = redeemVoucherResult.getVouchers();
		for (int i = 0; i < vouchers.size(); i++) {
			int counter = i + 1;
			OfferModel offerModel = getOfferDetail(vouchers.get(i).getOffer_id());
			if (offerModel == null) {
				return;
			}
			View view = getLayoutInflater().inflate(R.layout.item_coupon_summary, null);
			((TextView) view.findViewById(R.id.txv_offer_name)).setText(offerModel.getOffer_name());
			((TextView) view.findViewById(R.id.txv_offer_count)).setText("x" + offerModel.getCurrent_counter());
			((TextView) view.findViewById(R.id.txv_coupon_offer_desc)).setText(StringUtils.decode(offerModel.getOfferDes(), StringUtils.CHARSET_UTF_8, true));

			if (counter % 2 == 0) {
				((RelativeLayout) view.findViewById(R.id.root_lay_summary)).setBackgroundColor(getResources().getColor(R.color.common_grey_3));
			} else {
				((RelativeLayout) view.findViewById(R.id.root_lay_summary)).setBackgroundColor(getResources().getColor(R.color.white));
			}
			LinearLayout rootLayoutHeader = (LinearLayout) view.findViewById(R.id.root_layout_coupon_header);
			inflateVoucherDetailView(rootLayoutHeader, vouchers.get(i));
			rootLayout.addView(view);
		}
	}

	/**
	 * @param rootLayoutHeader
	 * @param vouchersModel
	 */
	private void inflateVoucherDetailView(LinearLayout rootLayoutHeader, VouchersModel vouchersModel) {
		int voucherSize = vouchersModel.getVoucher_codes().size();
		for (int i = 0; i < vouchersModel.getVoucher_codes().size(); i += 2) {
			int counter = i + 1;
			View view = getLayoutInflater().inflate(R.layout.item_coupon_summary_detail, null);
			((TextView) view.findViewById(R.id.txv_voucher_name1)).setText(getString(R.string.tag_voucher) + counter);
			((TextView) view.findViewById(R.id.txv_voucher_code1)).setText(vouchersModel.getVoucher_codes().get(i));
			if (voucherSize > counter) {
				counter++;
				((TextView) view.findViewById(R.id.txv_voucher_name2)).setText(getString(R.string.tag_voucher) + counter);
				((TextView) view.findViewById(R.id.txv_voucher_code2)).setText(vouchersModel.getVoucher_codes().get(i + 1));
			}
			rootLayoutHeader.addView(view);
		}

	}

	/**
	 * @param offer_id
	 * @return
	 */
	private OfferModel getOfferDetail(int offer_id) {
		for (int i = 0; i < mOfferModelList.size(); i++) {
			OfferModel offerModel = mOfferModelList.get(i);
			if (offerModel.getOfferId() == offer_id) {
				return offerModel;
			}
		}
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txv_continue_coupons:
			clearTopTillCouponsActivity();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		clearTopTillCouponsActivity();
	}

	/**
	 * 
	 */
	private void clearTopTillCouponsActivity() {
		Intent intent = new Intent(this, CouponsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
}