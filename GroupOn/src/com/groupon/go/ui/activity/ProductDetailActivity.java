package com.groupon.go.ui.activity;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.groupon.go.R;
import com.groupon.go.application.GrouponGoApp;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.DealsController;
import com.groupon.go.database.MyPurchasesHelper;
import com.groupon.go.model.DealDetailResponse.DealDetailModel;
import com.groupon.go.model.DealDetailResponse.OfferLocations;
import com.groupon.go.model.DealModel;
import com.groupon.go.model.OfferModel;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.ui.adapter.DealDetailPagerAdapter;
import com.groupon.go.ui.widget.CustomOfferDetaiView;
import com.groupon.go.ui.widget.CustomOfferDetaiView.OnOfferItemClickListener;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.widget.CustomScrollView;
import com.kelltontech.ui.widget.CustomScrollView.OnScrollViewListener;
import com.kelltontech.ui.widget.CustomTextView;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;
import com.kelltontech.utils.UiUtils;
import com.kelltontech.utils.WebFontUtils;

/**
 * @author vineet.kumar
 */
public class ProductDetailActivity extends GroupOnBaseActivity implements OnScrollViewListener, OnClickListener, OnOfferItemClickListener, OnPageChangeListener {

	private GoogleMap				mMap;
	private CustomScrollView		mScrollView;
	private CustomTextView			mDragToSeeMore;
	private RelativeLayout			mProductDetailHeader;
	private LinearLayout			mDealImageFooter;
	private ArrayList<OfferModel>	mOfferList;
	private DealModel				mDealModel;
	private DealDetailModel			mDealDetailModel;
	private LinearLayout			mExpOffersLinearLayout;
	private DecimalFormat			mDistanceFormatter;
	private ViewPager				mPager;
	private ImageView				mFwdArrow, mBckwdArrow;
	private ArrayList<String>		mDealImages;

	private BroadcastReceiver		mCartUpdatedActionReceiver;
	private IntentFilter			mCartUpdatedActionFilter;
	private TextView				mTxvCartIconWithCount;
	private String					mRupeeIconStr;
	private int						mDealIdForSharingDeal;
	private double[]				mOfferLatLngArr;
	private ArrayList<String>		mNearestOfferLocations;
	private View					mRelativeOnScreenNotification;
	private int						mCartCountBeingShown;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();

		setContentView(R.layout.activity_product_detail);

		mRelativeOnScreenNotification = ProjectUtils.showNotificationOnScreen(this);

		mDistanceFormatter = new DecimalFormat("0.0");

		mTxvCartIconWithCount = (TextView) findViewById(R.id.txv_header_cart);
		mTxvCartIconWithCount.setOnClickListener(this);
		findViewById(R.id.cart_image).setOnClickListener(this);
		findViewById(R.id.img_share).setOnClickListener(this);

		mOfferLatLngArr = new double[2];

		mRupeeIconStr = getString(R.string.Rs) + " ";

		mPager = (ViewPager) findViewById(R.id.pager_deal_detail);
		int pagerPosition = 0;

		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(Constants.STATE_PAGER_INDEX);
		}
		mPager.setCurrentItem(pagerPosition);

		mFwdArrow = (ImageView) findViewById(R.id.img_fwd_arrow);
		mBckwdArrow = (ImageView) findViewById(R.id.img_bck_arrow);

		mProductDetailHeader = (RelativeLayout) findViewById(R.id.header_prd_dtl);
		mDealImageFooter = (LinearLayout) findViewById(R.id.linear_deal_image_bottom_overlay);
		mDragToSeeMore = (CustomTextView) findViewById(R.id.txv_drag_up);
		mScrollView = (CustomScrollView) findViewById(R.id.scroll_view);

		findViewById(R.id.header_right).setOnClickListener(this);
		findViewById(R.id.buy_product).setOnClickListener(this);

		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mMap = fragment.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.getUiSettings().setScrollGesturesEnabled(false);
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mMap.getUiSettings().setZoomGesturesEnabled(false);
		mMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng arg0) {
				startFullScreenMapActivity();
			}
		});
		mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
			@Override
			public View getInfoWindow(Marker marker) {
				return null;
			}

			@Override
			public View getInfoContents(Marker marker) {
				startFullScreenMapActivity();
				return null;
			}
		});

		mScrollView.setOnScrollViewListener(this);

		// Receiver registration/unregistration is done in onSart/onStop to
		// fix issue found with below STR-
		// 1. Offers added to cart by Pay Now, Cart Screen is opened now.
		// 2. Cart modified there. (cart-cleared/offers-edited/offers-deleted)
		// 3. Cart Screen is closed, cart count should be updated here.
		mCartCountBeingShown = GrouponGoPrefs.getCartItemsCount(this);
		mCartUpdatedActionFilter = new IntentFilter(Constants.ACTION_CART_COUNT_UPDATED);
		mCartUpdatedActionReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateCartBadge();
			}
		};

		mDealIdForSharingDeal = getIntent().getIntExtra(Constants.EXTRA_DEAL_ID, 0);
		if (mDealIdForSharingDeal == 0) {
			mDealModel = getIntent().getParcelableExtra(Constants.EXTRA_DEAL_MODEL);
			if (mDealModel.getVoucher_count() == 0) {
				findViewById(R.id.layout_bottom).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.product_price)).setText(mRupeeIconStr + mDealModel.getOffer_price());
				getDealDetailFromServer(mDealModel.getDeal_id());
			} else {
				((TextView) findViewById(R.id.txv_redeem)).setVisibility(View.VISIBLE);
				GrouponGoApp grouponGoApp = (GrouponGoApp) getApplication();
				MyPurchasesHelper myPurchasesHelper = grouponGoApp.getMyPurchasesHelper();
				mDealDetailModel = myPurchasesHelper.getDealDetailModel(mDealModel);
				if (mDealDetailModel == null) {
					ToastUtils.showToast(this, getString(R.string.error_generic_message));
					finish();
					return;
				}
				mOfferList = mDealDetailModel.getOffers();
				populateView();
				View redeemHelpView = findViewById(R.id.relative_redeem_help);
				redeemHelpView.setVisibility(View.VISIBLE);
				redeemHelpView.setOnClickListener(this);
			}
		} else {
			getDeatilFrmServerForSharingDeal(mDealIdForSharingDeal);
		}
	}

	/**
	 * called from on click of map, ful-screen-button and on info window click
	 */
	private void startFullScreenMapActivity() {
		Intent intent = new Intent(ProductDetailActivity.this, DealDeatailMapActivity.class);
		intent.putParcelableArrayListExtra(Constants.EXTRA_LOCATION_LIST, mDealDetailModel.getOffer_locations());
		intent.putExtra(Constants.EXTRA_MERCHANT_NAME, mDealModel.getOpportunity_owner());
		intent.putExtra(Constants.EXTRA_CAT_NAME, mDealDetailModel.getCat_name());
		intent.putExtra(Constants.EXTRA_NEAREST_MARKER_LATLNG, mOfferLatLngArr);
		startActivity(intent);
	}

	/**
	 * isFlipCount should be true only if count is changed
	 */
	private void updateCartBadge() {
		int cartCountToShowNow = GrouponGoPrefs.getCartItemsCount(this);
		updateCartBadge(mTxvCartIconWithCount, mCartCountBeingShown != cartCountToShowNow);
		mCartCountBeingShown = cartCountToShowNow;
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocalBroadcastManager.registerReceiver(mCartUpdatedActionReceiver, mCartUpdatedActionFilter);
		updateCartBadge();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocalBroadcastManager.unregisterReceiver(mCartUpdatedActionReceiver);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(Constants.STATE_PAGER_INDEX, mPager.getCurrentItem());
	}

	/**
	 * if app is resumed from background on this screen, it might close itself.
	 */
	protected void onAppResumeFromBackground() {
		super.onAppResumeFromBackground();
		if (mDealModel.getVoucher_count() != 0 && ProjectUtils.isAnyVoucherExpired(mOfferList)) {
			finish();
		}
	}

	/**
	 * @param dealId
	 */
	private void getDealDetailFromServer(int dealId) {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		DealsController controller = new DealsController(this, this);
		if (mDealModel.getVoucher_count() != 0) {
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.EXTRA_DEAL_ID, dealId);
			bundle.putInt(ApiConstants.PARAM_ORDER_ID, mDealModel.getOrder_id());
			controller.getData(ApiConstants.GET_COUPON_DETAILS, bundle);
		} else {
			controller.getData(ApiConstants.GET_DEALS_DETAIL, "" + dealId);
		}
	}

	/**
	 * get deal detail of sharing deal
	 * 
	 * @param dealId
	 */
	private void getDeatilFrmServerForSharingDeal(int dealId) {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		DealsController controller = new DealsController(this, this);
		controller.getData(ApiConstants.GET_SHARING_DEALS_DETAIL, "" + dealId);
	}

	/**
	 * 
	 */
	private void setUpImgViewPager() {
		if (mDealModel.getDeal_image().size() == 0) {
			mFwdArrow.setVisibility(View.GONE);
			mBckwdArrow.setVisibility(View.GONE);
		}
		mDealImages = mDealModel.getDeal_image();
		DealDetailPagerAdapter pagerAdapter = new DealDetailPagerAdapter(mDealImages, this);
		mPager.setAdapter(pagerAdapter);
		mPager.setOnPageChangeListener(this);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_COUPON_DETAILS:
		case ApiConstants.GET_DEALS_DETAIL: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
				return;
			}
			mDealDetailModel = (DealDetailModel) serviceResponse.getResponseObject();
			mOfferList = (ArrayList<OfferModel>) mDealDetailModel.getOffers();
			populateView();
			break;
		}
		case ApiConstants.GET_SHARING_DEALS_DETAIL: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
				return;
			}
			mDealDetailModel = (DealDetailModel) serviceResponse.getResponseObject();
			mOfferList = (ArrayList<OfferModel>) mDealDetailModel.getOffers();
			mDealModel = mDealDetailModel.getShared_deal();
			populateView();
			break;
		}
		}
	}

	/**
	 * 
	 */
	private void populateView() {
		setUpImgViewPager();
		setDealDetail();
		setOfferDetail();

		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		Location userLocation = GrouponGoPrefs.getDeviceLocation(this);
		if (userLocation != null) {
			getOfferLocation(mDealModel.getOffer_id());
			LatLng userlatlng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
			builder.include(userlatlng);
			if (mNearestOfferLocations != null && !mNearestOfferLocations.isEmpty()) {
				LatLng nearestOfferLatlng = ProjectUtils.getNearestOffer(mNearestOfferLocations, userLocation);
				mOfferLatLngArr[0] = nearestOfferLatlng.latitude;
				mOfferLatLngArr[1] = nearestOfferLatlng.longitude;
				builder.include(nearestOfferLatlng);
				ProjectUtils.placeMarker(mMap, nearestOfferLatlng, null, R.drawable.ic_groupon_pin);
			}
		} else {
			OfferLocations nearestLocation = getNearestOfferModelLocation(mDealModel.getOffer_id());
			if (!StringUtils.isNullOrEmpty(nearestLocation.getOffer_location())) {
				mOfferLatLngArr = ProjectUtils.getLatLongFromString(nearestLocation.getOffer_location());
				builder.include(new LatLng(mOfferLatLngArr[0], mOfferLatLngArr[1]));
				ProjectUtils.placeMarker(mMap, new LatLng(mOfferLatLngArr[0], mOfferLatLngArr[1]), null, R.drawable.ic_groupon_pin);
			}
		}
		CameraUpdate cu;
		if (LocationUtils.isGpsEnabled(this)) {
			LatLngBounds bounds = builder.build();
			cu = CameraUpdateFactory.newLatLngBounds(bounds, 100, 100, 0);
			mMap.animateCamera(cu);
		} else {
			CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mOfferLatLngArr[0], mOfferLatLngArr[1])).zoom(14f).build();
			cu = CameraUpdateFactory.newCameraPosition(cameraPosition);
			mMap.animateCamera(cu);
		}
	}

	/**
	 * 
	 */
	private void setOfferDetail() {
		mExpOffersLinearLayout = (LinearLayout) findViewById(R.id.root_layout_offer_exp);
		if (mOfferList == null || mOfferList.isEmpty()) {
			return;
		}

		String labelOfferStr = getString(R.string.offer);

		for (int i = 0; i < mOfferList.size(); i++) {
			OfferModel offerModel = mOfferList.get(i);
			offerModel.setOffer_name(ProjectUtils.getOfferName(labelOfferStr, offerModel.getOffer_weight(), mOfferList.size()));
			CustomOfferDetaiView view = new CustomOfferDetaiView(this, offerModel, mRupeeIconStr);
			view.setTag(offerModel.getOfferId());
			mExpOffersLinearLayout.addView(view);
			LinearLayout layout = (LinearLayout) view.findViewById(R.id.view_offer_expand);
			setOfferDetailData(layout, offerModel.getOfferId());
			if (i == 0) {
				ImageView expcolImg = (ImageView) view.findViewById(R.id.img_exp_indicator);
				expcolImg.setImageResource(R.drawable.expander_ic_maximized);
				layout.setVisibility(View.VISIBLE);
				UiUtils.expand(layout);
			}
		}
	}

	/**
	 * 
	 */
	private void setDealDetail() {
		TextView merchantName = (TextView) findViewById(R.id.merchant_name_header);
		merchantName.setText(mDealModel.getOpportunity_owner());

		TextView mrchName = (TextView) findViewById(R.id.txv_merchant_name);
		mrchName.setText(mDealModel.getOpportunity_owner());

		TextView dealTitle = (TextView) findViewById(R.id.deal_title);
		dealTitle.setSingleLine(false);
		dealTitle.setMaxLines(3);
		dealTitle.setText(mDealModel.getDeal_title());

		TextView dealCat = (TextView) findViewById(R.id.deal_cat_header);
		dealCat.setText(mDealDetailModel.getCat_name());

		TextView dealBought = (TextView) findViewById(R.id.deals_bought);
		TextView dealPrice = (TextView) findViewById(R.id.deal_price);

		if (mDealModel.getVoucher_count() != 0) {
			TextView dealBoughtTag = (TextView) findViewById(R.id.deals_bought_tag);
			TextView dealPriceTag = (TextView) findViewById(R.id.deal_price_tag);
			dealPrice.setTextColor(getResources().getColor(android.R.color.white));
			dealPrice.setText(getString(R.string.tag_expire_on));
			dealPriceTag.setText(ProjectUtils.getFormattedDate(mDealModel.getExpiry_date()));
			dealBought.setText("" + mDealModel.getVoucher_count());
			dealBoughtTag.setText(getString(R.string.tag_vouchers));
			((LinearLayout) findViewById(R.id.lay_deal_discount)).setVisibility(View.INVISIBLE);
			TextView buttonRedeem = (TextView) findViewById(R.id.txv_redeem);
			buttonRedeem.setVisibility(View.VISIBLE);
			buttonRedeem.setOnClickListener(this);
		} else {
			findViewById(R.id.deal_price_tag).setVisibility(View.GONE);
			findViewById(R.id.layout_bottom).setVisibility(View.VISIBLE);
			TextView dealDiscount = (TextView) findViewById(R.id.deal_discount);
			if (mDealModel.getOffer_discount_money_percentage() == 0) {
				dealDiscount.setText(mRupeeIconStr + (int) mDealModel.getOffer_discount_money_value());
			} else {
				dealDiscount.setText((int) mDealModel.getOffer_discount_money_percentage() + "%");
			}
			dealBought.setText("" + mDealModel.getDeal_sold_count());
			dealPrice.setText(mRupeeIconStr + mDealModel.getOffer_price());
		}

		TextView placeDistance = (TextView) findViewById(R.id.place_distance);
		if (mDealModel.getDeal_distance() > 0) {
			placeDistance.setVisibility(View.VISIBLE);
			placeDistance.setText(mDistanceFormatter.format(mDealModel.getDeal_distance()) + " km");
		}

		TextView validUpto = (TextView) findViewById(R.id.deal_validity);

		OfferModel currentShowingOffer = getCurrentOfferModel(mDealModel.getOffer_id());
		String offerEndDate = currentShowingOffer.getOffer_end_date().split(",")[0];
		if (!StringUtils.isNullOrEmpty(offerEndDate)) {
			validUpto.setText(ProjectUtils.getFormattedDate(offerEndDate));
		}

		RelativeLayout costForView = (RelativeLayout) findViewById(R.id.cost_for);
		if (StringUtils.isNullOrEmpty(currentShowingOffer.getOffer_expected_expenditure_for())) {
			costForView.setVisibility(View.GONE);
		} else {
			TextView costFor = (TextView) findViewById(R.id.txv_cost_for);
			TextView costForPrice = (TextView) findViewById(R.id.txv_deal_cost_for);

			costFor.setText(getString(R.string.cost_for) + " " + currentShowingOffer.getOffer_expected_expenditure_for());
			costForPrice.setText(mRupeeIconStr + (int) currentShowingOffer.getOffer_expected_expenditure_price());
		}

		LinearLayout finePrint = (LinearLayout) findViewById(R.id.view_fine_print);
		String finePrints = StringUtils.decode(mDealDetailModel.getDeal_fineprint(), StringUtils.CHARSET_UTF_8, true);
		if (StringUtils.isNullOrEmpty(mDealDetailModel.getDeal_fineprint())) {
			finePrint.setVisibility(View.GONE);
		} else {
			finePrints = WebFontUtils.wrapBodyIntoTemplate(this, finePrints, Constants.TEMPLATE_DEAL_DETAILS_WEB_VIEW);
			WebView fineprintWebview = (WebView) findViewById(R.id.txv_fine_print);
			fineprintWebview.loadDataWithBaseURL(null, finePrints, StringUtils.MIME_TEXT_HTML, StringUtils.CHARSET_UTF_8, null);
		}

		TextView merchantAddress = (TextView) findViewById(R.id.txv_merchant_add);
		String merchantAdd = currentShowingOffer.getMerchant_address();
		if (StringUtils.isNullOrEmpty(merchantAdd)) {
			merchantAddress.setVisibility(View.GONE);
		} else {
			merchantAddress.setText(merchantAdd);
		}

		TextView productPrice = (TextView) findViewById(R.id.product_price);
		productPrice.setText(mRupeeIconStr + mDealModel.getOffer_price());
	}

	/**
	 * used to change header background on scrolling
	 */
	@Override
	public void onScrollChanged(CustomScrollView v, int x, int y, int oldx, int oldy) {
		Rect scrollBounds = new Rect();
		mScrollView.getHitRect(scrollBounds);
		int locationOnScreen[] = new int[2];
		mDealImageFooter.getLocationOnScreen(locationOnScreen);

		if (locationOnScreen[1] > mProductDetailHeader.getHeight()) {
			mProductDetailHeader.setBackgroundResource(R.drawable.gradient_prod_detail_header);
		} else {
			mProductDetailHeader.setBackgroundResource(R.drawable.gradient_green_no_border_no_round);
		}

		View view = (View) mScrollView.getChildAt(mScrollView.getChildCount() - 1);
		int diff = (view.getBottom() - (mScrollView.getHeight() + mScrollView.getScrollY()));

		// if diff is zero, then the bottom has been reached
		if (diff == 0) {
			mDragToSeeMore.setVisibility(View.GONE);
		} else {
			mDragToSeeMore.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.header_right: {
			finish();
			break;
		}
		case R.id.relative_redeem_help: {
			findViewById(R.id.relative_redeem_help).setVisibility(View.GONE);
			break;
		}
		case R.id.txv_header_cart:
		case R.id.cart_image: {
			Intent intent = new Intent(this, UserCart.class);
			startActivity(intent);
			break;
		}
		case R.id.img_share: {
			((ScrollView) findViewById(R.id.scroll_view)).fullScroll(ScrollView.FOCUS_UP);
			Intent intent = new Intent(this, ShareOptionsActivity.class);
			if (mDealIdForSharingDeal != 0) {
				intent.putExtra(Constants.EXTRA_DEAL_ID, mDealIdForSharingDeal);
			} else {
				intent.putExtra(Constants.EXTRA_DEAL_ID, mDealModel.getDeal_id());
			}
			intent.putExtra(Constants.EXTRA_MERCHANT_NAME, mDealModel.getOpportunity_owner());
			intent.putExtra(Constants.EXTRA_CAT_NAME, mDealDetailModel.getCat_name());
			if (mDealModel.getVoucher_count() == 0) {
				intent.putExtra(Constants.EXTRA_MESSAGE_TYPE, ApiConstants.VALUE_MESSAGE_TYPE_SHARED_DEAL);
			} else {
				intent.putExtra(Constants.EXTRA_MESSAGE_TYPE, ApiConstants.VALUE_MESSAGE_TYPE_REDEEMED_DEAL);
			}
			int currentImageIndex = mPager.getCurrentItem();
			if (mDealImages != null && currentImageIndex >= 0 && currentImageIndex < mDealImages.size()) {
				intent.putExtra(Constants.EXTRA_DEAL_IMAGE_URL, mDealImages.get(mPager.getCurrentItem()));
			}
			startActivity(intent);
			break;
		}
		case R.id.img_close_notification_on_screen: {
			if (mRelativeOnScreenNotification != null) {
				UiUtils.collapse(mRelativeOnScreenNotification);
			}
			break;
		}
		case R.id.buy_product: {
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				BackGroundService.start(this, ApiConstants.GET_USER_CART_FROM_SERVER);
			}
			((ScrollView) findViewById(R.id.scroll_view)).fullScroll(ScrollView.FOCUS_UP);
			Intent intent = new Intent(this, OfferDetailActivity.class);
			Bundle b = new Bundle();
			b.putParcelableArrayList(Constants.EXTRA_OFFER_MODEL_LIST, mOfferList);
			b.putString(Constants.EXTRA_DEAL_TITLE, mDealModel.getDeal_title());
			if (mDealIdForSharingDeal != 0) {
				b.putInt(Constants.EXTRA_DEAL_ID, mDealIdForSharingDeal);
			} else {
				b.putInt(Constants.EXTRA_DEAL_ID, mDealModel.getDeal_id());
			}
			intent.putExtras(b);
			startActivity(intent);
			break;
		}
		case R.id.txv_redeem: {
			((ScrollView) findViewById(R.id.scroll_view)).fullScroll(ScrollView.FOCUS_UP);
			Intent intent = new Intent(this, OfferDetailActivity.class);
			Bundle b = new Bundle();
			b.putParcelableArrayList(Constants.EXTRA_OFFER_MODEL_LIST, mOfferList);
			b.putString(Constants.EXTRA_DEAL_TITLE, mDealModel.getDeal_title());
			b.putInt(Constants.EXTRA_DEAL_ID, mDealModel.getDeal_id());
			b.putInt(Constants.EXTRA_ORDER_ID, mDealModel.getOrder_id());
			intent.putExtras(b);
			startActivity(intent);
			break;
		}
		}
	}

	@Override
	public void onOfferItemClick(int offerID) {
		View view = mExpOffersLinearLayout.findViewWithTag(offerID);
		if (view == null) {
			return;
		}
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.view_offer_expand);
		ImageView expcolImg = (ImageView) view.findViewById(R.id.img_exp_indicator);
		if (layout.getVisibility() == View.VISIBLE) {
			expcolImg.setImageResource(R.drawable.expander_ic_minimized);
			UiUtils.collapse(layout);
		} else {
			expcolImg.setImageResource(R.drawable.expander_ic_maximized);
			layout.setVisibility(View.VISIBLE);
			UiUtils.expand(layout);
		}
		handleExpColView(offerID);
	}

	/**
	 * @param layout
	 * @param offerId
	 */
	private void setOfferDetailData(LinearLayout layout, int offerId) {
		TextView offerTitle = (TextView) layout.findViewById(R.id.discount_title);
		TextView offerDesc = (TextView) layout.findViewById(R.id.exp_offer_desc);
		TextView validUpto = (TextView) layout.findViewById(R.id.exp_offer_validity);
		TextView txvRedeemDate = (TextView) layout.findViewById(R.id.exp_redeem_date);

		OfferModel offerModel = getCurrentOfferModel(offerId);
		if (offerModel == null) {
			return;
		}

		String offerTitleStr = null;
		if (offerModel.getOffer_discount_money_percentage() != 0) {
			offerTitleStr = StringUtils.getFormatDecimalAmount(offerModel.getOffer_discount_money_percentage()) + "%" + " discount";
		} else if (offerModel.getOffer_discount_money_value() != 0) {
			offerTitleStr = mRupeeIconStr + StringUtils.getFormatDecimalAmount(offerModel.getOffer_discount_money_value()) + " Off";
		}
		if (offerTitleStr == null) {
			offerTitle.setVisibility(View.GONE);
		} else {
			if (!StringUtils.isNullOrEmpty(offerModel.getOffer_min_purchase_value())) {
				offerTitleStr += getString(R.string.on_min_purchase_of) + mRupeeIconStr + offerModel.getOffer_min_purchase_value();
			}
			offerTitle.setText(offerTitleStr);
		}
		offerDesc.setText(StringUtils.decode(offerModel.getOfferDes(), StringUtils.CHARSET_UTF_8, true));

		String offerEndDate = offerModel.getOffer_end_date();
		if (!StringUtils.isNullOrEmpty(offerEndDate)) {
			validUpto.setText(ProjectUtils.getFormattedDate(offerEndDate));
		}

		String redeemDate = offerModel.getOffer_redem_end_date();
		String finalRedeemString = ProjectUtils.getFormattedDate(redeemDate);

		// String redeemStartTime = offerModel.getOffer_redem_start_time();
		// String redeemEndTime = offerModel.getOffer_redem_end_time();
		// if (!"00:00 AM".equalsIgnoreCase(redeemStartTime) &&
		// !"11:59 PM".equalsIgnoreCase(redeemEndTime)) {
		// finalRedeemString += " between " + redeemStartTime + "-" +
		// redeemEndTime;
		// }

		txvRedeemDate.setText(finalRedeemString);

		RelativeLayout costForView = (RelativeLayout) layout.findViewById(R.id.exp_cost_for);
		String costForValue = offerModel.getOffer_expected_expenditure_for();
		if (StringUtils.isNullOrEmpty(costForValue)) {
			costForView.setVisibility(View.GONE);
		} else {
			TextView costFor = (TextView) layout.findViewById(R.id.exp_txv__cost_for);
			TextView costForPrice = (TextView) layout.findViewById(R.id.exp_txv_deal_cost_for);

			costFor.setText(getString(R.string.cost_for) + " " + costForValue);
			costForPrice.setText(mRupeeIconStr + (int) offerModel.getOffer_expected_expenditure_price());
		}

		String applicableOnDays = offerModel.getOffer_valid_days();
		if (StringUtils.isNullOrEmpty(applicableOnDays)) {
			layout.findViewById(R.id.txv_label_applicable_on).setVisibility(View.GONE);
		} else {
			((TextView) layout.findViewById(R.id.txv_applicable_on_days)).setText(applicableOnDays);
		}

		ArrayList<String> offerLocationsList = getOfferLocation(offerId);
		if (offerLocationsList == null || offerLocationsList.isEmpty()) {
			layout.findViewById(R.id.txv_label_avail_at).setVisibility(View.GONE);
		} else {
			LinearLayout offerLocationView = (LinearLayout) layout.findViewById(R.id.root_layout_offer_add);
			for (int i = 0; i < offerLocationsList.size(); i++) {
				View view = getLayoutInflater().inflate(R.layout.offer_item_address, null);
				((TextView) view.findViewById(R.id.offer_add)).setText(offerLocationsList.get(i));
				offerLocationView.addView(view);
			}
		}
	}

	/**
	 * @param offerId
	 * @return
	 */
	private ArrayList<String> getOfferLocation(int offerId) {
		ArrayList<OfferLocations> offerLocation = mDealDetailModel.getOffer_locations();
		mNearestOfferLocations = new ArrayList<String>();
		ArrayList<String> locations = null;
		if (offerLocation != null && offerLocation.size() > 0) {
			locations = new ArrayList<String>();
			for (int i = 0; i < offerLocation.size(); i++) {
				OfferLocations locationModel = offerLocation.get(i);
				if (locationModel.getOffer_id() == offerId) {
					locations.add(locationModel.getMerchant_address());
					mNearestOfferLocations.add(locationModel.getOffer_location());
				}
			}
		}
		return locations;
	}

	private OfferLocations getNearestOfferModelLocation(int offerId) {
		ArrayList<OfferLocations> offerLocation = mDealDetailModel.getOffer_locations();
		OfferLocations locations = null;
		if (offerLocation != null && offerLocation.size() > 0) {
			for (int i = 0; i < offerLocation.size(); i++) {
				OfferLocations locationModel = offerLocation.get(i);
				if (locationModel.getOffer_id() == offerId) {
					locations = offerLocation.get(i);
				}
			}
		}
		return locations;
	}

	/**
	 * @param offerId
	 * @return
	 */
	private OfferModel getCurrentOfferModel(int offerId) {
		OfferModel offerModel = null;
		for (int i = 0; i < mOfferList.size(); i++) {
			offerModel = mOfferList.get(i);
			int id = offerModel.getOfferId();
			if (id == offerId) {
				return offerModel;
			}
		}
		return offerModel;
	}

	/**
	 * @param offerID
	 */
	private void handleExpColView(int offerID) {
		for (int i = 0; i < mOfferList.size(); i++) {
			int id = mOfferList.get(i).getOfferId();
			View view = mExpOffersLinearLayout.findViewWithTag(id);
			ImageView expcolImg = (ImageView) view.findViewById(R.id.img_exp_indicator);
			if (view != null) {
				if (id != offerID) {
					LinearLayout layout = (LinearLayout) view.findViewById(R.id.view_offer_expand);
					if (layout.getVisibility() == View.VISIBLE) {
						// layout.setVisibility(View.GONE);
						UiUtils.collapse(layout);
						expcolImg.setImageResource(R.drawable.expander_ic_minimized);
					}
				}
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// nothing to do here
	}

	@Override
	public void onPageScrolled(int position, float arg1, int arg2) {
		if (position == 0) {
			mBckwdArrow.setVisibility(View.INVISIBLE);
		} else {
			mBckwdArrow.setVisibility(View.VISIBLE);
		}

		if (position == mDealImages.size() - 1) {
			mFwdArrow.setVisibility(View.INVISIBLE);
		} else {
			mFwdArrow.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		// nothing to do here
	}
}
