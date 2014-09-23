package com.groupon.go.ui.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.UserCartController;
import com.groupon.go.model.GetUserCartResponse.GetUserCartResult;
import com.groupon.go.model.OfferModel;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.ui.dialog.CommonDialog;
import com.groupon.go.ui.widget.CustomItemOffer;
import com.groupon.go.ui.widget.CustomItemOffer.CountChangeListener;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.widget.CustomTextView;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.kumar
 */
public class OfferDetailActivity extends GroupOnBaseActivity implements CountChangeListener, OnClickListener {

	private static final String		LOG_TAG	= "OfferDetailActivity";

	private View					mRelativeVisibleAreaRoot;
	private LinearLayout			mLinearOfferWiseToatlViewsRoot;
	private float					mGrandTotal;
	private ArrayList<OfferModel>	mOfferModelList;
	private int						mDealId, mOrderId;

	private String					mRupeeIconStr;
	private CustomTextView			mTxvGrandTotal;

	private boolean					mIsFromCart;
	private boolean					mIsFromCoupon;
	private UserCartController		mCartController;
	private String					mDealTitle;
	private boolean					mIsSendToCart;
	private boolean					mIsAllOfferSoldOut;

	private JSONArray				mOfferJsonArrayToAvail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_offer_detail);

		if (ConnectivityUtils.isNetworkEnabled(this)) {
			BackGroundService.start(this, ApiConstants.GET_USER_CART_FROM_SERVER);
		}

		Bundle b = getIntent().getExtras();
		mOfferModelList = b.getParcelableArrayList(Constants.EXTRA_OFFER_MODEL_LIST);
		mDealId = b.getInt(Constants.EXTRA_DEAL_ID);
		mIsFromCart = b.getBoolean(Constants.EXTRA_FROM_CART);
		mOrderId = b.getInt(Constants.EXTRA_ORDER_ID);

		mDealTitle = b.getString(Constants.EXTRA_DEAL_TITLE);
		TextView dealTitle = (TextView) findViewById(R.id.offer_headline);
		dealTitle.setText(mDealTitle);

		findViewById(R.id.linear_transparent).setOnClickListener(this);
		findViewById(R.id.offer_buy_product).setOnClickListener(this);

		findViewById(R.id.view_add_to_cart).setOnClickListener(this);

		Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
		mRelativeVisibleAreaRoot = findViewById(R.id.relative_visible_area_root);
		mRelativeVisibleAreaRoot.startAnimation(bottomUp);
		mRelativeVisibleAreaRoot.setVisibility(View.VISIBLE);

		mCartController = new UserCartController(this, this);

		if (mIsFromCart) {
			dealTitle.setSingleLine(false);
			TextView txvSave = (TextView) findViewById(R.id.txv_save);
			ImageView imgRemoveDeal = (ImageView) findViewById(R.id.img_remv_deal);
			((LinearLayout) findViewById(R.id.layout_bottom_offer)).setVisibility(View.GONE);
			imgRemoveDeal.setVisibility(View.VISIBLE);
			imgRemoveDeal.setOnClickListener(this);
			txvSave.setVisibility(View.VISIBLE);
			txvSave.setOnClickListener(this);
		}

		if (mOrderId != 0) {
			mIsFromCoupon = true;
			findViewById(R.id.layout_bottom_offer).setVisibility(View.GONE);
			findViewById(R.id.txv_ensure_merchant_location).setVisibility(View.VISIBLE);
			TextView txvRedeem = (TextView) findViewById(R.id.txv_redeem);
			txvRedeem.setVisibility(View.VISIBLE);
			txvRedeem.setOnClickListener(this);
		}

		mLinearOfferWiseToatlViewsRoot = (LinearLayout) findViewById(R.id.root_layout_total_offer);
		mTxvGrandTotal = (CustomTextView) findViewById(R.id.total_amount_offer_detail);
		mRupeeIconStr = getString(R.string.Rs) + " ";

		generateOfferList(mOfferModelList);

		if (!mIsFromCoupon) {
			inflateOfferTotalView();
		} else {
			mLinearOfferWiseToatlViewsRoot.setVisibility(View.GONE);
			findViewById(R.id.relative_grand_total).setVisibility(View.GONE);
		}
	}

	/**
	 * if app is resumed from background on this screen, it might close itself.
	 */
	protected void onAppResumeFromBackground() {
		super.onAppResumeFromBackground();
		if (mIsFromCoupon && ProjectUtils.isAnyVoucherExpired(mOfferModelList)) {
			Intent intent = new Intent(this, CouponsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		}
	}

	/**
	 * inflate total offer amount
	 */
	private void inflateOfferTotalView() {
		mGrandTotal = 0;
		for (int i = 0; i < mOfferModelList.size(); i++) {
			OfferModel modal = mOfferModelList.get(i);
			View view = getLayoutInflater().inflate(R.layout.offer_count_item, null);
			boolean isSold = modal.getOffer_sold_count() >= modal.getOffer_max_cap();
			if (!isSold && modal.getCurrent_counter() > 0) {
				mGrandTotal = mGrandTotal + modal.getCurrent_counter() * modal.getOffer_price();
				setDataOnOfferTotalView(view, modal);
			}
			view.setTag(modal.getOfferId());
			mLinearOfferWiseToatlViewsRoot.addView(view);
		}
		mTxvGrandTotal.setText(mRupeeIconStr + StringUtils.getFormatDecimalAmount(mGrandTotal));
	}

	/**
	 * @param view
	 * @param offerModel
	 */
	private void setDataOnOfferTotalView(View view, OfferModel offerModel) {
		CustomTextView txvOfferName = (CustomTextView) view.findViewById(R.id.offer_name);
		txvOfferName.setText(offerModel.getOffer_name());

		CustomTextView txvOfferCount = (CustomTextView) view.findViewById(R.id.offer_count);
		txvOfferCount.setText("x" + offerModel.getCurrent_counter());

		CustomTextView txvOfferAmount = (CustomTextView) view.findViewById(R.id.offer_amount);
		txvOfferAmount.setText(mRupeeIconStr + StringUtils.getFormatDecimalAmount(offerModel.getCurrent_counter() * offerModel.getOffer_price()));

		view.setVisibility(View.VISIBLE);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.ADD_DEAL_TO_CART: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				return;
			}
			Intent broadcastIntent = new Intent(Constants.ACTION_CART_COUNT_UPDATED);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
			if (mIsSendToCart) {
				Intent intent = new Intent(this, UserCart.class);
				intent.putExtra(Constants.EXTRA_FROM_ACTIVITY, OfferDetailActivity.class.getSimpleName());
				startActivity(intent);
			} else {
				GetUserCartResult cartResult = (GetUserCartResult) serviceResponse.getResponseObject();
				if (!StringUtils.isNullOrEmpty(cartResult.getShow_message())) {
					showCustomAlert(Events.ALERT_MESSAGE_FROM_CART_APIS, cartResult.getShow_message(), Constants.TAG_ALERT_MESSAGE_FROM_CART_APIS);
				} else {
					ToastUtils.showToast(this, getString(R.string.msg_offers_added_to_cart));
					finish();
				}
			}
			break;
		}
		case ApiConstants.DELETE_DEAL_FROM_CART:
		case ApiConstants.EDIT_DEAL_IN_CART: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				return;
			}
			setResult(RESULT_OK);
			finish();
			break;
		}
		}
	}

	@Override
	public void onBackPressed() {
		Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.slide_out_up);
		mRelativeVisibleAreaRoot.startAnimation(bottomUp);
		mRelativeVisibleAreaRoot.setVisibility(View.INVISIBLE);
		super.onBackPressed();
	}

	/**
	 * generate offer list
	 * 
	 * @param pOffersList
	 */
	public void generateOfferList(ArrayList<OfferModel> pOffersList) {
		mIsAllOfferSoldOut = true;
		String labelOfferStr = getString(R.string.offer);
		boolean counterInitialized = false;
		// root LinearLayout for CustomItemOffer instances
		LinearLayout offersLinearLayout = (LinearLayout) findViewById(R.id.root_layout_offers);
		for (int i = 0; i < pOffersList.size(); i++) {
			OfferModel offerModel = pOffersList.get(i);
			offerModel.setOffer_name(ProjectUtils.getOfferName(labelOfferStr, offerModel.getOffer_weight(), pOffersList.size()));
			if (offerModel.getOffer_sold_count() < offerModel.getOffer_max_cap()) {
				mIsAllOfferSoldOut = false;
			}
			if (mIsFromCoupon) {
				offerModel.setCurrent_counter(offerModel.getVoucher_count());
			} else if (!mIsFromCart) {
				if (counterInitialized || offerModel.getOffer_sold_count() >= offerModel.getOffer_max_cap()) {
					offerModel.setCurrent_counter(0);
				} else {
					counterInitialized = true;
					offerModel.setCurrent_counter(1);
				}
			}
			CustomItemOffer offersView = new CustomItemOffer(this, this, offerModel, mIsFromCoupon);
			offersLinearLayout.addView(offersView.createView());
		}
	}

	/**
	 * when user click on +,- or manually edit offer count
	 */
	@Override
	public void onOfferClickListener(OfferModel offerModel, int changeInCount) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "Offer Click with Id " + offerModel.getOfferId() + " Counter is: " + offerModel.getCurrent_counter());
		}
		if (!mIsFromCoupon) {
			updateOfferTotalAndGrandTotal(offerModel, changeInCount);
		}
	}

	/**
	 * @param offerModel
	 * @param changeInCount
	 */
	private void updateOfferTotalAndGrandTotal(OfferModel offerModel, int changeInCount) {
		View view = mLinearOfferWiseToatlViewsRoot.findViewWithTag(offerModel.getOfferId());
		if (view == null) {
			return;
		}
		if (offerModel.getCurrent_counter() == 0) {
			view.setVisibility(View.GONE);
		} else {
			setDataOnOfferTotalView(view, offerModel);
		}
		mGrandTotal = mGrandTotal + changeInCount * offerModel.getOffer_price();
		mTxvGrandTotal.setText(mRupeeIconStr + StringUtils.getFormatDecimalAmount(mGrandTotal));
	}

	/**
	 * 
	 */
	private void addToCartOnServer() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		JSONArray jsonArray = createOffersJsonArray();
		if (jsonArray.length() == 0) {
			ToastUtils.showToast(getApplicationContext(), getString(R.string.toast_select_one_offer));
			return;
		}
		if (jsonArray != null) {
			showProgressDialog(getString(R.string.prog_loading));
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.EXTRA_DEAL_ID, mDealId);
			bundle.putString(Constants.EXTRA_OFFERS_JSON_STRING, jsonArray.toString());
			bundle.putBoolean(Constants.EXTRA_IS_ADD_CART_FOR_PAYMENT, false);
			UserCartController userCartController = new UserCartController(this, this);
			userCartController.getData(ApiConstants.ADD_DEAL_TO_CART, bundle);
		}
	}

	/**
	 * 
	 */
	private void updateToCartOnServer() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		JSONArray jsonArray = createOffersJsonArray();
		if (jsonArray.length() == 0) {
			ToastUtils.showToast(getApplicationContext(), getString(R.string.toast_select_one_offer));
			return;
		}
		if (jsonArray != null) {
			showProgressDialog(getString(R.string.prog_loading));
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.EXTRA_DEAL_ID, mDealId);
			bundle.putString(Constants.EXTRA_OFFERS_JSON_STRING, jsonArray.toString());
			mCartController.getData(ApiConstants.EDIT_DEAL_IN_CART, bundle);
		}
	}

	/**
	 * @return
	 */
	private JSONArray createOffersJsonArray() {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < mOfferModelList.size(); i++) {
			OfferModel offerModel = mOfferModelList.get(i);
			JSONObject jsonObject = new JSONObject();
			if (mIsFromCart) {
				try {
					float totalPrice = offerModel.getCurrent_counter() * offerModel.getOffer_price();
					jsonObject.put(ApiConstants.PARAM_OFFER_ID, offerModel.getOfferId());
					jsonObject.put(ApiConstants.PARAM_OFFER_PRICE, offerModel.getOffer_price());
					jsonObject.put(ApiConstants.PARAM_OFFER_QUANTITY, offerModel.getCurrent_counter());
					jsonObject.put(ApiConstants.PARAM_TOTAL_PRICE, totalPrice);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jsonArray.put(jsonObject);
			} else if (mIsFromCoupon) {
				if (offerModel.getCurrent_counter() > 0) {
					try {
						jsonObject.put(ApiConstants.PARAM_OFFER_ID, offerModel.getOfferId());
						jsonObject.put(ApiConstants.PARAM_OFFER_QUANTITY, offerModel.getCurrent_counter());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					jsonArray.put(jsonObject);
				}
			} else {
				if (offerModel.getCurrent_counter() > 0) {
					try {
						float totalPrice = offerModel.getCurrent_counter() * offerModel.getOffer_price();
						jsonObject.put(ApiConstants.PARAM_OFFER_ID, offerModel.getOfferId());
						jsonObject.put(ApiConstants.PARAM_OFFER_PRICE, offerModel.getOffer_price());
						jsonObject.put(ApiConstants.PARAM_OFFER_QUANTITY, offerModel.getCurrent_counter());
						jsonObject.put(ApiConstants.PARAM_TOTAL_PRICE, totalPrice);
						jsonObject.put(Constants.EXTRA_OFFER_DESC, offerModel.getOfferDes());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					jsonArray.put(jsonObject);
				}
			}
		}
		return jsonArray;
	}

	/**
	 * shows a strong warning dialog
	 */
	private void showRedeemConfirmationDialog() {
		CommonDialog dialog = new CommonDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.REDEEM_COUPON_DIALOG);
		dialog.setArguments(bundle);
		dialog.show(getSupportFragmentManager(), Constants.TAG_REDEEM_COUPON_DIALOG);
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.REDEEM_COUPON_DIALOG: {
			if (eventData instanceof Boolean && (Boolean) eventData) {
				startRedeemVouchersActivity();
			}
			break;
		}
		case Events.CUSTOM_ALERT_POSITIVE_BUTTON: {
			if (eventData instanceof Integer) {
				int alertId = (Integer) eventData;
				switch (alertId) {
				case Events.ALERT_MESSAGE_FROM_CART_APIS: {
					finish();
					break;
				}
				}
				break;
			}
		}
		}
	}

	/**
	 * starts redeem voucher activity
	 */
	private void startRedeemVouchersActivity() {
		if (mOfferJsonArrayToAvail == null || mOfferJsonArrayToAvail.length() == 0) {
			return;
		}
		Intent intent = new Intent(this, RedeemVouchersActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_DEAL_ID, mDealId);
		bundle.putParcelableArrayList(Constants.EXTRA_OFFER_MODEL_LIST, mOfferModelList);
		bundle.putInt(Constants.EXTRA_ORDER_ID, mOrderId);
		bundle.putString(Constants.EXTRA_DEAL_TITLE, mDealTitle);
		bundle.putString(Constants.EXTRA_OFFERS_JSON_STRING, mOfferJsonArrayToAvail.toString());
		intent.putExtras(bundle);
		startActivity(intent);
	}

	/**
	 * remove deal from cart
	 */
	private void removeDealFromCart() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mCartController.getData(ApiConstants.DELETE_DEAL_FROM_CART, "" + mDealId);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.linear_transparent: {
			finish();
			break;
		}
		case R.id.offer_buy_product: {
			if (mIsAllOfferSoldOut) {
				ToastUtils.showToast(this, getString(R.string.msg_all_offers_sold_out));
			} else {
				mIsSendToCart = true;
				addToCartOnServer();
			}
			break;
		}
		case R.id.view_add_to_cart: {
			if (mIsAllOfferSoldOut) {
				ToastUtils.showToast(this, getString(R.string.msg_all_offers_sold_out));
			} else {
				mIsSendToCart = false;
				addToCartOnServer();
			}
			break;
		}
		case R.id.txv_save: {
			if (!mIsAllOfferSoldOut) {
				updateToCartOnServer();
			}
			break;
		}
		case R.id.txv_redeem: {
			mOfferJsonArrayToAvail = createOffersJsonArray();
			if (mOfferJsonArrayToAvail == null || mOfferJsonArrayToAvail.length() == 0) {
				ToastUtils.showToast(this, getString(R.string.toast_select_one_offer));
				return;
			}
			showRedeemConfirmationDialog();
			break;
		}
		case R.id.img_remv_deal: {
			removeDealFromCart();
			break;
		}
		}
	}
}