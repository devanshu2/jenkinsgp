package com.groupon.go.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.DealsController;
import com.groupon.go.controller.PaymentController;
import com.groupon.go.controller.UserCartController;
import com.groupon.go.model.CreateOrderResponse;
import com.groupon.go.model.DealDetailResponse.DealDetailModel;
import com.groupon.go.model.GetUserCartResponse.GetUserCartResult;
import com.groupon.go.model.OfferModel;
import com.groupon.go.model.PaymentDetailModel;
import com.groupon.go.model.UserCartModel;
import com.groupon.go.model.UserCartModel.CartOfferModel;
import com.groupon.go.persistance.GrouponGoFiles;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.ui.dialog.CommonDialog;
import com.groupon.go.utils.ProjectUtils;
import com.groupon.go.utils.ProjectUtils.CartDealClickListener;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.Events;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.kumar
 */
public class UserCart extends GroupOnBaseActivity implements OnClickListener, CartDealClickListener {

	private ArrayList<UserCartModel>	mCartDealsList;
	private LinearLayout				mCartRootLayout;
	private TextView					mTxvPayButton;
	private UserCartModel				mClickedCartDealModel;
	private UserCartController			mCartController;
	private float						mGrandTotal;
	private String						mCouponCode;
	private GetUserCartResult			mCartResult;
	private EditText					mEdtCouponCode;
	private ImageView					mImgRemoveCoupon;
	private TextView					mChbxCreditUsed;
	private boolean						mIsCreditUsed;
	private String						mTxnId;
	private boolean						mShowCachedCartAndMsg;

	private boolean						mShowingEmptyCart;
	private Menu						mActionBarMenu;

	private TextView					mTxvGrandTotal;
	private TextView					mTxvGrandTotalBeforeDiscount;
	private String						mRupeeIconStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_cart);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));

		int cartItemsCount = GrouponGoPrefs.getCartItemsCount(this);
		getSupportActionBar().setTitle("Your Cart (" + cartItemsCount + ")");

		findViewById(R.id.buy_product_cart).setOnClickListener(this);

		mChbxCreditUsed = (TextView) findViewById(R.id.chbx_redeem_credit);
		mChbxCreditUsed.setOnClickListener(this);
		mIsCreditUsed = false;

		mEdtCouponCode = (EditText) findViewById(R.id.edt_coupon_code);
		mImgRemoveCoupon = (ImageView) findViewById(R.id.img_remv_coupon);

		mTxvGrandTotal = (TextView) findViewById(R.id.txv_total_after_discount);
		mTxvGrandTotalBeforeDiscount = (TextView) findViewById(R.id.txv_total_before_discount);
		mTxvGrandTotalBeforeDiscount.setPaintFlags(mTxvGrandTotalBeforeDiscount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

		mTxvPayButton = (TextView) findViewById(R.id.buy_product_cart);

		mTxvGrandTotal.requestFocus();
		mTxvGrandTotal.requestFocusFromTouch();
		mRupeeIconStr = getString(R.string.Rs) + " ";

		mCartController = new UserCartController(this, this);

		mShowCachedCartAndMsg = false;
		String fromActivityName = getIntent().getStringExtra(Constants.EXTRA_FROM_ACTIVITY);
		if (OfferDetailActivity.class.getSimpleName().equals(fromActivityName)) {
			mShowCachedCartAndMsg = true;
		}

		mCartRootLayout = (LinearLayout) findViewById(R.id.cart_item_layout);

		mGrandTotal = getIntent().getFloatExtra(Constants.EXTRA_GRAND_TOTAL, mGrandTotal);

		mEdtCouponCode.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					applyCouponCode();
				}
				return false;
			}
		});

		if (mShowCachedCartAndMsg) {
			mCartController.getData(ApiConstants.GET_USER_CART_FROM_CACHE, null);
		} else if (ConnectivityUtils.isNetworkEnabled(this)) {
			showProgressDialog(getString(R.string.prog_loading));
			mCartController.getData(ApiConstants.GET_USER_CART_FROM_SERVER, null);
		} else if (GrouponGoPrefs.getCartItemsCount(this) == 0) {
			showEmptyCartScreen(true);
		} else {
			mCartController.getData(ApiConstants.GET_USER_CART_FROM_CACHE, null);
			ToastUtils.showToast(this, getString(R.string.error_no_network));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.user_cart_menu, menu);
		mActionBarMenu = menu;
		if (mShowingEmptyCart) {
			showEmptyCartScreen(false);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (ConnectivityUtils.isNetworkEnabled(this)) {
			showProgressDialog(getString(R.string.prog_loading));
			mCartController.getData(ApiConstants.GET_USER_CART_FROM_SERVER, null);
		} else {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			break;
		}
		case R.id.action_delete_cart: {
			if (!mShowingEmptyCart) {
				showClearCartConfirmationDlg();
			}
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private void showClearCartConfirmationDlg() {
		CommonDialog couponDialog = new CommonDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.CLEAR_CART);
		couponDialog.setArguments(bundle);
		couponDialog.show(getSupportFragmentManager(), Constants.TAG_CONFIRMATION_CLR_CART_DIALOG);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_USER_CART_FROM_CACHE: {
			if (serviceResponse.isSuccess()) {
				mCartResult = (GetUserCartResult) serviceResponse.getResponseObject();
				if (!StringUtils.isNullOrEmpty(mCartResult.getShow_message()) && mShowCachedCartAndMsg) {
					showCustomAlert(Events.ALERT_MESSAGE_FROM_CART_APIS, mCartResult.getShow_message(), Constants.TAG_ALERT_MESSAGE_FROM_CART_APIS);
					mShowCachedCartAndMsg = false;
				}
				populateCartView();
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
			}
			break;
		}
		case ApiConstants.GET_USER_CART_FROM_SERVER: {
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				mCartResult = (GetUserCartResult) serviceResponse.getResponseObject();
				if (!StringUtils.isNullOrEmpty(mCartResult.getShow_message())) {
					showCustomAlert(Events.ALERT_MESSAGE_FROM_CART_APIS, mCartResult.getShow_message(), Constants.TAG_ALERT_MESSAGE_FROM_CART_APIS);
				}
				populateCartView();
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
			}
			break;
		}
		case ApiConstants.REMOVE_COUPON_CODE: {
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				mCartResult = (GetUserCartResult) serviceResponse.getResponseObject();
				if (!StringUtils.isNullOrEmpty(mCartResult.getShow_message())) {
					showCustomAlert(Events.ALERT_MESSAGE_FROM_CART_APIS, mCartResult.getShow_message(), Constants.TAG_ALERT_MESSAGE_FROM_CART_APIS);
				}
				populateCartView();
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		case ApiConstants.APPLY_COUPON_CODE: {
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				mCartResult = (GetUserCartResult) serviceResponse.getResponseObject();
				if (!StringUtils.isNullOrEmpty(mCartResult.getShow_message())) {
					showCustomAlert(Events.ALERT_MESSAGE_FROM_CART_APIS, mCartResult.getShow_message(), Constants.TAG_ALERT_MESSAGE_FROM_CART_APIS);
				}
				populateCartView();
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		case ApiConstants.GET_DEALS_DETAIL: {
			removeProgressDialog();
			if (serviceResponse.getResponseObject() instanceof DealDetailModel) {
				sendToOfferDetailsScreen((DealDetailModel) serviceResponse.getResponseObject());
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		case ApiConstants.DELETE_USER_CART: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				return;
			}
			showEmptyCartScreen(true);
			GrouponGoPrefs.setCartItemsCount(this, 0);
			GrouponGoFiles.deleteCachedCartJson(this);
			BackGroundService.start(this, ApiConstants.GET_USER_CART_FROM_SERVER);

			break;
		}
		case ApiConstants.APPLY_USER_CREDITS:
		case ApiConstants.REMOVE_USER_CREDITS: {
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				mCartResult = (GetUserCartResult) serviceResponse.getResponseObject();
				if (!StringUtils.isNullOrEmpty(mCartResult.getShow_message())) {
					showCustomAlert(Events.ALERT_MESSAGE_FROM_CART_APIS, mCartResult.getShow_message(), Constants.TAG_ALERT_MESSAGE_FROM_CART_APIS);
				}
				populateCartView();
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		case ApiConstants.GET_CREATE_ORDER: {
			if (!serviceResponse.isSuccess()) {
				removeProgressDialog();
				if (serviceResponse.getResponseObject() instanceof CreateOrderResponse) {
					CreateOrderResponse response = (CreateOrderResponse) serviceResponse.getResponseObject();
					if (response.getError_code() == ApiConstants.ERROR_CODE_OFFER_OUT_OF_STOCK) {
						showCustomAlert(Events.ALERT_SERVER_MSG_TO_REVIEW_CART, mCartResult.getShow_message(), Constants.TAG_ALERT_SERVER_MSG_TO_REVIEW_CART);
					}
				} else {
					ToastUtils.showToast(this, serviceResponse.getErrorMessage());
					return;
				}
			} else {
				PaymentDetailModel paymentDetailModel = (PaymentDetailModel) serviceResponse.getResponseObject();
				mTxnId = paymentDetailModel.getTxnid();
				purchaseDealWithZeroPayment();
			}
			break;
		}
		case ApiConstants.PURCHASE_WITHOUT_PAYMENT: {
			removeProgressDialog();
			Intent intent;
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				intent = new Intent(this, PaymentFailureActivity.class);
			} else {
				BackGroundService.start(this, ApiConstants.GET_MY_PURCHASES);
				intent = new Intent(this, PaymentSuccessActivity.class);
				intent.putExtra(Constants.EXTRA_TRANSX_ID, mTxnId);
			}
			startActivity(intent);
			break;
		}
		}
	}

	/**
	 * @param availableCredit
	 */
	private void updateCreditRedeemChBx() {
		if (mCartResult.getCredit() == 0 && mCartResult.getCredit_used() == 0) {
			mChbxCreditUsed.setVisibility(View.GONE);
			return;
		} else {
			mChbxCreditUsed.setVisibility(View.VISIBLE);
		}
		if (mCartResult.isCredit_status()) {
			mIsCreditUsed = true;
			mChbxCreditUsed.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checked, 0, 0, 0);
		} else {
			mIsCreditUsed = false;
			mChbxCreditUsed.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unchecked, 0, 0, 0);
		}
		float availableCredit = mCartResult.getCredit() - mCartResult.getCredit_used();
		mChbxCreditUsed.setText(getString(R.string.label_checkbox_use_credits) + " (" + StringUtils.getFormatDecimalAmount(availableCredit) + ")");
	}

	/**
	 * @param pOffersList
	 */
	private void sendToOfferDetailsScreen(DealDetailModel pDealDetailModel) {
		ArrayList<OfferModel> offersList = pDealDetailModel.getOffers();
		if (offersList == null || offersList.isEmpty()) {
			ToastUtils.showToast(this, getString(R.string.error_generic_message));
			return;
		}
		ArrayList<CartOfferModel> cartOfferModels = mClickedCartDealModel.getOffer_list();
		for (int i = 0; i < cartOfferModels.size(); i++) {
			CartOfferModel cartOfferModel = cartOfferModels.get(i);
			for (int k = 0; k < offersList.size(); k++) {
				OfferModel offerModel = offersList.get(k);
				if (cartOfferModel.getOffer_id() == offerModel.getOfferId()) {
					offerModel.setCurrent_counter(cartOfferModel.getOffer_count());
					break;
				}
			}
		}
		Intent intent = new Intent(this, OfferDetailActivity.class);
		Bundle b = new Bundle();
		b.putParcelableArrayList(Constants.EXTRA_OFFER_MODEL_LIST, offersList);
		b.putString(Constants.EXTRA_DEAL_TITLE, pDealDetailModel.getDeal_title());
		b.putInt(Constants.EXTRA_DEAL_ID, mClickedCartDealModel.getDeal_id());
		b.putBoolean(Constants.EXTRA_FROM_CART, true);
		intent.putExtras(b);
		startActivityForResult(intent, Constants.RQ_USER_CART_TO_EDIT_CART);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Constants.RQ_USER_CART_TO_EDIT_CART: {
			if (resultCode == RESULT_OK) {
				mShowCachedCartAndMsg = true;
				mCartController.getData(ApiConstants.GET_USER_CART_FROM_CACHE, null);
			}
			break;
		}
		}
	}

	/**
	 * method used to populate cart view
	 */
	private void populateCartView() {
		mCartDealsList = mCartResult.getDeals();
		int cartItemsCount = GrouponGoPrefs.getCartItemsCount(this);

		if (cartItemsCount == 0 || mCartDealsList == null || mCartDealsList.isEmpty()) {
			showEmptyCartScreen(true);
			return;
		}

		getSupportActionBar().setTitle("Your Cart (" + cartItemsCount + ")");
		mCartRootLayout.removeAllViews();

		mCouponCode = mCartResult.getCoupon_code();
		if (StringUtils.isNullOrEmpty(mCouponCode)) {
			ProjectUtils.populateView(mCartDealsList, this, mCartRootLayout, true, this, false);
			mImgRemoveCoupon.setVisibility(View.GONE);
			mEdtCouponCode.setFocusable(true);
			mEdtCouponCode.setFocusableInTouchMode(true);
			mEdtCouponCode.setText("");
		} else {
			ProjectUtils.populateView(mCartDealsList, this, mCartRootLayout, true, this, true);
			mEdtCouponCode.setFocusable(false);
			mEdtCouponCode.setFocusableInTouchMode(false);
			mEdtCouponCode.setText(mCouponCode);
			mImgRemoveCoupon.setVisibility(View.VISIBLE);
			mImgRemoveCoupon.setOnClickListener(this);
		}

		mGrandTotal = mCartResult.getTotal();

		mTxvGrandTotal.setText(mRupeeIconStr + StringUtils.getFormatDecimalAmount(mGrandTotal));
		mTxvPayButton.setText(getString(R.string.button_pay_amount, StringUtils.getFormatDecimalAmount(mGrandTotal)));

		if (mCartResult.getTotal() != mCartResult.getTotal_original()) {
			mTxvGrandTotalBeforeDiscount.setText(mRupeeIconStr + StringUtils.getFormatDecimalAmount(mCartResult.getTotal_original()));
			mTxvGrandTotalBeforeDiscount.setVisibility(View.VISIBLE);
		} else {
			mTxvGrandTotalBeforeDiscount.setVisibility(View.GONE);
		}

		updateCreditRedeemChBx();
	}

	/**
	 * @param pSetContentView
	 */
	private void showEmptyCartScreen(boolean pSetContentView) {
		if (pSetContentView) {
			mShowingEmptyCart = true;
			setContentView(R.layout.activity_empty_cart);
		}
		getSupportActionBar().setTitle("Your Cart (0)");
		if (mActionBarMenu != null) {
			mActionBarMenu.removeItem(R.id.action_delete_cart);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.buy_product_cart: {
			String textCoupon = mEdtCouponCode.getText().toString();
			if (StringUtils.isNullOrEmpty(mCouponCode) && !StringUtils.isNullOrEmpty(textCoupon)) {
				showApplyCouponDialog(textCoupon);
			} else {
				buyProduct();
			}
			break;
		}
		case R.id.img_remv_coupon: {
			showDialogForRemoveCouponCode();
			break;
		}
		case R.id.chbx_redeem_credit: {
			if (mIsCreditUsed) {
				sendRequestToRemoveCredit();
			} else {
				sendRequestToApplyCredit();
			}
			break;
		}
		}
	}

	/**
	 * @param textCoupon
	 */
	private void showApplyCouponDialog(String textCoupon) {
		CommonDialog couponDialog = new CommonDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.APPLY_COUPON_CODE_DIALOG);
		bundle.putString(Constants.EXTRA_COUPON_CODE, textCoupon);
		couponDialog.setArguments(bundle);
		couponDialog.show(getSupportFragmentManager(), Constants.TAG_APPLY_COUPON_CODE_DIALOG);
	}

	/**
	 * 
	 */
	private void buyProduct() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		if (mGrandTotal == 0) {
			showProgressDialog(getString(R.string.prog_loading));
			Bundle bundle = new Bundle();
			bundle.putString(ApiConstants.PARAM_PG, ApiConstants.VALUE_PAY_BY_GROUPON_CREDIT);
			PaymentController paymentController = new PaymentController(this, this);
			paymentController.getData(ApiConstants.GET_CREATE_ORDER, bundle);
		} else {
			Intent intent = new Intent(this, PayViaSaveCardsActivity.class);
			intent.putExtra(Constants.EXTRA_GRAND_TOTAL, mGrandTotal);
			startActivity(intent);
		}
	}

	/**
	 * 
	 */
	private void deleteUserCart() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mCartController.getData(ApiConstants.DELETE_USER_CART, null);
	}

	/**
	 * 
	 */
	private void showDialogForRemoveCouponCode() {
		CommonDialog couponDialog = new CommonDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.REMOVE_COUPON_CODE);
		bundle.putString(Constants.EXTRA_COUPON_CODE, mCouponCode);
		couponDialog.setArguments(bundle);
		couponDialog.show(getSupportFragmentManager(), Constants.TAG_REMOVE_COUPON_CODE_DIALOG);
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.APPLY_COUPON_CODE_DIALOG: {
			if (eventData instanceof Boolean && (Boolean) eventData) {
				applyCouponCode();
			} else {
				buyProduct();
			}
			break;
		}
		case Events.REMOVE_COUPON_CODE: {
			removeCoupon();
			break;
		}
		case Events.CLEAR_CART: {
			deleteUserCart();
			break;
		}
		case Events.CUSTOM_ALERT_POSITIVE_BUTTON: {
			if (eventData instanceof Integer) {
				int alertId = (Integer) eventData;
				switch (alertId) {
				case Events.ALERT_SERVER_MSG_TO_REVIEW_CART: {
					if (ConnectivityUtils.isNetworkEnabled(UserCart.this)) {
						showProgressDialog(getString(R.string.prog_loading));
						mCartController.getData(ApiConstants.GET_USER_CART_FROM_SERVER, null);
					} else {
						ToastUtils.showToast(this, getString(R.string.error_no_network));
					}
					break;
				}
				}
				break;
			}
		}
		}
	}

	/**
	 * 
	 */
	private void removeCoupon() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mCartController.getData(ApiConstants.REMOVE_COUPON_CODE, null);
	}

	/**
	 * 
	 */
	private void applyCouponCode() {
		String couponCode = mEdtCouponCode.getText().toString().trim();
		if (StringUtils.isNullOrEmpty(couponCode)) {
			ToastUtils.showToast(this, getString(R.string.enter_coupon_code));
			return;
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mCartController.getData(ApiConstants.APPLY_COUPON_CODE, couponCode);
	}

	@Override
	public void onCartDealClick(int pClickedCartDealIndex) {
		if (pClickedCartDealIndex < 0 || mCartDealsList == null || pClickedCartDealIndex >= mCartDealsList.size()) {
			return;
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		mClickedCartDealModel = mCartDealsList.get(pClickedCartDealIndex);
		showProgressDialog(getString(R.string.prog_loading));
		new DealsController(this, this).getData(ApiConstants.GET_DEALS_DETAIL, "" + mClickedCartDealModel.getDeal_id());
	}

	/**
	 * 
	 */
	private void sendRequestToRemoveCredit() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mCartController.getData(ApiConstants.REMOVE_USER_CREDITS, null);
	}

	/**
	 * 
	 */
	private void sendRequestToApplyCredit() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mCartController.getData(ApiConstants.APPLY_USER_CREDITS, "" + mCartResult.getCredit());
	}

	/**
	 * 
	 */
	private void purchaseDealWithZeroPayment() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			removeProgressDialog();
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		if (StringUtils.isNullOrEmpty(mTxnId)) {
			removeProgressDialog();
			ToastUtils.showToast(this, getString(R.string.error_generic_message));
			return;
		}
		PaymentController paymentController = new PaymentController(this, this);
		paymentController.getData(ApiConstants.PURCHASE_WITHOUT_PAYMENT, mTxnId);
	}
}