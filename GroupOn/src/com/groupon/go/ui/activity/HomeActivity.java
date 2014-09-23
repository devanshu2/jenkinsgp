package com.groupon.go.ui.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.application.AppStaticVars;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.UserProfileController;
import com.groupon.go.model.CityListResponse.CityListResultModel;
import com.groupon.go.model.CityModel;
import com.groupon.go.model.DealsListRequest;
import com.groupon.go.model.DealsListResponse.DealsListResultModel;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.service.CouponsNotificationService;
import com.groupon.go.ui.fragment.DealsListFragment.RemoveGridView;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.widget.AppRater;
import com.kelltontech.ui.widget.CustomTextView;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.ShareUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;
import com.kelltontech.utils.UiUtils;

/**
 * @author sachin.gupta, vineet.kumar
 */
public class HomeActivity extends DrawerBaseActivity implements OnClickListener, OnTabChangeListener, RemoveGridView {

	private static final String	LOG_TAG	= "HomeActivity";

	private int					mPendingDealsRequestType;
	private boolean				mOnCreateIsInProgress;
	private String				mCurrentFragmentTag;

	private View				mRelativeOnScreenNotification;
	private View				mLinearToAnimate;
	private TextView			mTxvDealsCount;
	private TextView			mTxvDealsCity;

	private String				mCityNameAsOnHome;
	private List<CityModel>		mCitiesArrayList;

	private CustomTextView		mTxvCartIconWithCount;
	private IntentFilter		mGetProfileRespActionFilter;
	private BroadcastReceiver	mGetProfileRespActionReceiver;

	private boolean				mShouldUseGeoCodedCity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		setupActionBar();

		if (!GrouponGoPrefs.canLaunchHome(this)) {
			// registration flow is incomplete, launching splash from here with
			// a flag to auto-finish and open next activity in registration flow
			Intent splashIntent = new Intent(this, PreSplashActivity.class);
			splashIntent.putExtra(Constants.EXTRA_AUTO_OPEN_NEXT_SCREEN, true);
			startActivity(splashIntent);
			finish();
			return;
		}

		AppRater.getInstance().startSession(null);

		checkIntentForUriData(getIntent());

		mLinearToAnimate = findViewById(R.id.linear_to_animate);

		initializeDrawer();

		mTxvDealsCount = (TextView) findViewById(R.id.txv_exploring_deals_in);
		mTxvDealsCity = (TextView) findViewById(R.id.txv_deals_in_city);
		mTxvDealsCity.setOnClickListener(this);
		updateScreenForCity(false);

		mOnCreateIsInProgress = true;
		mCurrentFragmentTag = setUpTabHost(0, this); // deals list API called

		UserProfileController userProfileController = new UserProfileController(this, this);
		userProfileController.getData(ApiConstants.GET_CITY_LIST_FROM_CACHE, null);
		userProfileController.getData(ApiConstants.GET_PROFILE_FROM_CACHE, null);

		mDealsRefreshBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				refreshLocationAndDeals();
			}
		};

		mGetProfileRespActionFilter = new IntentFilter(Constants.ACTION_GET_PROFILE_RESP_RCVD);
		mGetProfileRespActionReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateCartBadge(mTxvCartIconWithCount, false);
			}
		};

		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			CouponsNotificationService.start(this);
		}
	}

	/**
	 * attempts to refresh location and then deals
	 */
	private void refreshLocationAndDeals() {
		Location currentLocation = getCurrentLocation();
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "refreshLocationAndDeals() " + currentLocation);
		}
		if (LocationUtils.isLocationEnabled(this) && currentLocation == null) {
			mPendingDealsRequestType = DealsListRequest.REQUEST_REFRESH_PAGES;
			connectLocationClient(true);
		} else {
			updateDealsListOnFragments(DealsListRequest.REQUEST_REFRESH_PAGES, mCurrentFragmentTag);
		}
		if (GrouponGoPrefs.shouldCheckCurrentCity(this)) {
			mShouldUseGeoCodedCity = true;
			startReverseGeoService(true);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		super.onConnected(connectionHint);
		if (mOnCreateIsInProgress) {
			updateDealsListOnFragments(DealsListRequest.REQUEST_FIRST_PAGE, mCurrentFragmentTag);
		} else if (mPendingDealsRequestType != DealsListRequest.REQUEST_NONE) {
			updateDealsListOnFragments(mPendingDealsRequestType, mCurrentFragmentTag);
			mPendingDealsRequestType = DealsListRequest.REQUEST_NONE;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		super.onConnectionFailed(connectionResult);
		if (mOnCreateIsInProgress) {
			updateDealsListOnFragments(DealsListRequest.REQUEST_FIRST_PAGE, mCurrentFragmentTag);
		} else if (mPendingDealsRequestType != DealsListRequest.REQUEST_NONE) {
			updateDealsListOnFragments(mPendingDealsRequestType, mCurrentFragmentTag);
			mPendingDealsRequestType = DealsListRequest.REQUEST_NONE;
		}
	}

	/**
	 * 
	 */
	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setLogo(getResources().getDrawable(R.drawable.ic_groupon_logo));
		actionBar.setTitle("");
		actionBar.setBackgroundDrawable(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_screen_menu, menu);
		MenuItem menuCart = menu.findItem(R.id.action_cart);
		View actionView = MenuItemCompat.getActionView(menuCart);
		if (actionView instanceof RelativeLayout) {
			RelativeLayout rootActionCart = (RelativeLayout) actionView;
			mTxvCartIconWithCount = (CustomTextView) rootActionCart.findViewById(R.id.txv_header_cart);
			rootActionCart.setOnClickListener(this);
			updateCartBadge(mTxvCartIconWithCount, false);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.LOCATION_IS_OFF_DIALOG:
		case Events.GPS_IS_OFF_DIALOG: {
			if (eventData instanceof Boolean && (Boolean) eventData) {
				return; // location settings are opened
			}
			AppRater.getInstance().showDialog(this, true);
			if (GrouponGoPrefs.shouldCheckCurrentCity(this)) {
				mShouldUseGeoCodedCity = true;
				startReverseGeoService(true);
			}
			break;
		}
		}
	}

	/**
	 * update city name and deal listing
	 * 
	 * @param refreshDealsFromServer
	 */
	private void updateScreenForCity(boolean refreshDealsFromServer) {
		CityModel cityToShowDeals = GrouponGoPrefs.getCityToShowDeals(this);
		mCityNameAsOnHome = cityToShowDeals.getCity_name();
		mTxvDealsCity.setText(Html.fromHtml("<u>" + mCityNameAsOnHome + "</u>"));

		if (refreshDealsFromServer) {
			mTxvDealsCount.setText(getString(R.string.msg_exploring_deals_in));
			updateDealsListOnFragments(DealsListRequest.REQUEST_ON_CITY_CHANGED, mCurrentFragmentTag);
		}
	}

	@Override
	@SuppressLint("NewApi")
	protected void onResume() {
		super.onResume();
		mLocalBroadcastManager.registerReceiver(mGetProfileRespActionReceiver, mGetProfileRespActionFilter);
		if (AppStaticVars.REQUEST_FIRST_PAGE_ON_HOME) {
			AppStaticVars.REQUEST_FIRST_PAGE_ON_HOME = false;
			updateDealsListOnFragments(DealsListRequest.REQUEST_FIRST_PAGE, mCurrentFragmentTag);
		} else if (AppStaticVars.REQUEST_REFRESH_PAGES_ON_HOME) {
			AppStaticVars.REQUEST_REFRESH_PAGES_ON_HOME = false;
			refreshLocationAndDeals();
		}
		if (mTxvCartIconWithCount != null) {
			updateCartBadge(mTxvCartIconWithCount, false);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (GrouponGoPrefs.shouldCheckCurrentCity(this)) {
			mShouldUseGeoCodedCity = true;
			startReverseGeoService(true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocalBroadcastManager.unregisterReceiver(mDealsRefreshBroadcastReceiver);
		mLocalBroadcastManager.unregisterReceiver(mGetProfileRespActionReceiver);
	}

	@Override
	protected void onAppResumeFromBackground() {
		super.onAppResumeFromBackground();
		if (GrouponGoPrefs.shouldCheckCurrentCity(this)) {
			mShouldUseGeoCodedCity = true;
			startReverseGeoService(true);
		}
	}

	@Override
	protected void onReverseGeoServiceResponse(Intent intent) {
		super.onReverseGeoServiceResponse(intent);
		if (mShouldUseGeoCodedCity) {
			checkCurrentCityInCitiesList();
			mShouldUseGeoCodedCity = false;
		}
	}

	/**
	 * checks city and updates deals if required
	 */
	private void checkCurrentCityInCitiesList() {
		if (StringUtils.isNullOrEmpty(AppStaticVars.REVERSE_GEO_CODED_CITY)) {
			return;
		}
		CityModel cityToShowDeals = GrouponGoPrefs.getCityToShowDeals(this);
		if (cityToShowDeals.getCity_name().equalsIgnoreCase(AppStaticVars.REVERSE_GEO_CODED_CITY)) {
			return;
		}
		CityModel cityModel = ProjectUtils.getSelectableCurrentCity(mCitiesArrayList, AppStaticVars.REVERSE_GEO_CODED_CITY);
		if (cityModel == null) {
			cityModel = ProjectUtils.getAppropriateCity(mCitiesArrayList, getCurrentLocation());
		}
		if (cityModel != null) {
			GrouponGoPrefs.setCityToShowDeals(this, cityModel);
			updateScreenForCity(true);
		}
	}

	@Override
	public void onClick(View pClickSource) {
		switch (pClickSource.getId()) {
		case R.id.root_item_cart: {
			Intent intent = new Intent(this, UserCart.class);
			startActivity(intent);
			break;
		}
		case R.id.txv_deals_in_city: {
			Intent intent = new Intent(this, SelectYourCityActivity.class);
			startActivityForResult(intent, Constants.RQ_HOME_TO_CITY_SELECTION);
			break;
		}
		case R.id.img_close_notification_on_screen: {
			closeOnScreenNotification();
			break;
		}
		}
	}

	/**
	 * 1. closes on screen notification and <br/>
	 * 2. also shows next notification
	 */
	private void closeOnScreenNotification() {
		if (mRelativeOnScreenNotification != null) {
			UiUtils.collapse(mRelativeOnScreenNotification);
			mRelativeOnScreenNotification = null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Constants.RQ_HOME_TO_CITY_SELECTION: {
			if (resultCode == RESULT_OK) {
				updateScreenForCity(true);
				int notifyType = ProjectUtils.getOnScreenNotificationType(this);
				if (notifyType == ApiConstants.VALUE_NOTIFY_TYPE_CITY_CHANGED) {
					closeOnScreenNotification();
				}
			}
			break;
		}
		}
	}

	@Override
	public void removeViewOnScroll(boolean animate) {
		if (mLinearToAnimate.getVisibility() == View.VISIBLE) {
			mLinearToAnimate.setVisibility(View.GONE);
		}
	}

	@Override
	public void addViewOnScroll() {
		if (mLinearToAnimate.getVisibility() != View.VISIBLE) {
			mLinearToAnimate.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_CITY_LIST_FROM_CACHE: {
			if (serviceResponse.getResponseObject() instanceof CityListResultModel) {
				mCitiesArrayList = ((CityListResultModel) serviceResponse.getResponseObject()).getCity();
			}
			break;
		}
		case ApiConstants.GET_DEALS_LIST_FROM_CACHE: {
			if (serviceResponse.isSuccess()) {
				updateDealsCount(serviceResponse);
			}
			break;
		}
		case ApiConstants.GET_DEALS_LIST_FROM_SERVER: {
			if (mOnCreateIsInProgress) {
				startPostOnCreateApis();
				mOnCreateIsInProgress = false;
			}
			updateDealsCount(serviceResponse);
			ProjectUtils.setPeriodicDealsRefreshBroadcast(this, false);
			break;
		}
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void updateDealsCount(ServiceResponse serviceResponse) {
		int totalDealsInFifties = 0;
		if (serviceResponse.getResponseObject() instanceof DealsListResultModel) {
			DealsListResultModel resultModel = (DealsListResultModel) serviceResponse.getResponseObject();
			totalDealsInFifties = (resultModel.getTotal_deals() / 50) * 50;
		}
		if (totalDealsInFifties == 0) {
			mTxvDealsCount.setText(getString(R.string.msg_exploring_deals_in));
		} else {
			mTxvDealsCount.setText(getString(R.string.msg_count_deals_in, totalDealsInFifties));
		}
	}

	/**
	 */
	private void startPostOnCreateApis() {
		if (LocationUtils.isGpsEnabled(this)) {
			if (GrouponGoPrefs.shouldCheckCurrentCity(this)) {
				mShouldUseGeoCodedCity = true;
				startReverseGeoService(true);
			}
		} else {
			showLocationRequiredDialog();
		}
		BackGroundService.postRedeemedVouchers(this, true);
		BackGroundService.start(this, ApiConstants.GET_USER_CART_FROM_SERVER);
		startDeviceGcmRegService();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search: {
			Intent intent = new Intent(this, SearchProductActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.action_cart: {
			Intent intent = new Intent(this, UserCart.class);
			startActivity(intent);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabChanged(String tabId) {
		mCurrentFragmentTag = tabId;
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		getCurrentLocation();
		updateDealsListOnFragments(DealsListRequest.REQUEST_IF_NO_DEALS, mCurrentFragmentTag);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		checkIntentForUriData(intent);
	}

	/**
	 * @return true if dealId found in intent
	 */
	private boolean checkIntentForUriData(Intent intent) {
		Uri uri = intent.getData();
		if (uri == null) {
			return false;
		}
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "Uri: " + uri);
		}
		int dealId = StringUtils.parseInt(uri.getQueryParameter(ApiConstants.PARAM_ONLY_ID), -1, 0);
		int notifyType = StringUtils.parseInt(uri.getQueryParameter(ApiConstants.PARAM_NOTIFY_TYPE), -1, 0);
		String notificationMessage = intent.getStringExtra(Constants.EXTRA_MESSAGE);

		if (dealId != 0) {
			Intent dealDetailsIntent = new Intent(this, ProductDetailActivity.class);
			dealDetailsIntent.putExtra(Constants.EXTRA_DEAL_ID, dealId);
			dealDetailsIntent.putExtra(Constants.EXTRA_NOTIFY_TYPE, notifyType);
			dealDetailsIntent.putExtra(Constants.EXTRA_MESSAGE, notificationMessage);
			startActivity(dealDetailsIntent);
			return true;
		}
		switch (notifyType) {
		case ApiConstants.VALUE_NOTIFY_TYPE_SHOW_COUPONS: {
			Intent couponsListIntent = new Intent(this, CouponsActivity.class);
			couponsListIntent.putExtra(Constants.EXTRA_NOTIFY_TYPE, notifyType);
			couponsListIntent.putExtra(Constants.EXTRA_MESSAGE, notificationMessage);
			startActivity(couponsListIntent);
			return true;
		}
		case ApiConstants.VALUE_NOTIFY_TYPE_UNIQUE_CODE:
		case ApiConstants.VALUE_NOTIFY_TYPE_PROMO_CODE:
		default: {
			String promoCode = intent.getStringExtra(Constants.EXTRA_COUPON_CODE);
			if (!StringUtils.isNullOrEmpty(promoCode)) {
				ShareUtils.copyToClipboard(this, promoCode);
				ToastUtils.showToast(this, getString(R.string.msg_coupon_code_copied));
			}
			if (!StringUtils.isNullOrEmpty(notificationMessage)) {
				mRelativeOnScreenNotification = ProjectUtils.showNotificationOnScreen(this, notifyType, notificationMessage);
			}
			return true;
		}
		}
	}

	@Override
	public void onBackPressed() {
		setAppNotInBackground();
		super.onBackPressed();
	}
}