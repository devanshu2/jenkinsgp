package com.groupon.go.ui.activity;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.application.AppStaticVars;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.gcm.DeviceGcmRegService;
import com.groupon.go.model.DealsListRequest;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.ui.dialog.CommonDialog;
import com.groupon.go.ui.dialog.CustomProgressDialog;
import com.groupon.go.ui.fragment.DealsListFragment;
import com.groupon.go.utils.AnalyticsHelper;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.service.ReverseGeoService;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.activity.BaseActivity;
import com.kelltontech.ui.widget.AppRater;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * This activity will hold methods common to more than one activity.
 * 
 * @author sachin.gupta
 */
public class GroupOnBaseActivity extends BaseActivity implements ConnectionCallbacks, OnConnectionFailedListener {

	protected LocalBroadcastManager	mLocalBroadcastManager;

	private LocationClient			mLocationClient;
	private BroadcastReceiver		mDeviceCityReceiver;
	private IntentFilter			mDeviceCityActionFilter;

	protected BroadcastReceiver		mDealsRefreshBroadcastReceiver;
	private IntentFilter			mDealsRefreshActionFilter;

	private boolean					mCanShowAnyDialog;
	private boolean					mIsGooglePlayOpened;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

		mLocationClient = new LocationClient(this, this, this);

		AnalyticsHelper.onActivityCreate(this);

		mDealsRefreshActionFilter = new IntentFilter(Constants.ACTION_DEALS_REFRESH_TIMER);
		mDealsRefreshBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (BuildConfig.DEBUG) {
					Log.i(getClass().getSimpleName(), "Deals Refresh Timer.");
				}
				AppStaticVars.REQUEST_REFRESH_PAGES_ON_HOME = true;
			}
		};
	}

	/**
	 * Called when the Activity becomes visible.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		connectLocationClient(false);
		AnalyticsHelper.onActivityStart(this);
	}

	/**
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		super.onStop();
		AnalyticsHelper.onActivityStop(this);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		AppStaticVars.CONNECTING_LOCATION_CLIENT = false;
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this, Constants.RQ_PLAY_SERVICES_RESOLUTION);
			} catch (IntentSender.SendIntentException e) {
				Log.e(getClass().getSimpleName(), "onConnectionFailed() " + e);
			}
		} else {
			// display a dialog to the user with the error.
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		AppStaticVars.CONNECTING_LOCATION_CLIENT = false;
		getCurrentLocation();
	}

	@Override
	public void onDisconnected() {
		// Nothing to do here
	}

	/**
	 * @param pDisconnectFirst
	 */
	protected void connectLocationClient(boolean pDisconnectFirst) {
		if (pDisconnectFirst && mLocationClient.isConnected()) {
			mLocationClient.disconnect();
		}
		AppStaticVars.CONNECTING_LOCATION_CLIENT = true;
		mLocationClient.connect();
	}

	/**
	 * @return currentLocation or null if currentLocation is not available
	 */
	protected Location getCurrentLocation() {
		Location currentLocation = null;
		if (mLocationClient != null && mLocationClient.isConnected()) {
			currentLocation = mLocationClient.getLastLocation();
		}
		if (currentLocation != null) {
			GrouponGoPrefs.setDeviceLocation(this, currentLocation);
		}
		if (BuildConfig.DEBUG) {
			if (currentLocation == null) {
				Log.i(getClass().getSimpleName(), "Current Location is not available.");
			} else {
				Log.i(getClass().getSimpleName(), "Current Location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
			}
		}
		return currentLocation;
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		// nothing to do here
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		switch (eventId) {
		case Events.APP_RATER_DIALOG: {
			if (eventData instanceof Boolean) {
				mIsGooglePlayOpened = (Boolean) eventData;
			}
		}
		}
	}

	@Override
	protected void onAppResumeFromBackground() {
		super.onAppResumeFromBackground();
		if (!GrouponGoPrefs.canLaunchHome(this)) {
			return;
		}
		AppRater.getInstance().startSession(this);
		AppStaticVars.resetDefaults(this);
		ProjectUtils.setPeriodicDealsRefreshBroadcast(this, false);

		BackGroundService.postRedeemedVouchers(this, true);
		BackGroundService.start(this, ApiConstants.GET_USER_CART_FROM_SERVER);
		startDeviceGcmRegService();
	}

	/**
	 * update application version name and GCM registration id in background
	 * 
	 * @note this method should not be called before onResume
	 */
	protected void startDeviceGcmRegService() {
		UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(this);
		if (userDetails.getUser_id() == 0) {
			return;
		}
		if (checkPlayServices(false)) {
			Intent regServiceIntent = new Intent(this, DeviceGcmRegService.class);
			startService(regServiceIntent);
		} else {
			ToastUtils.showToast(this, getString(R.string.error_missing_play_services));
		}
	}

	/**
	 * starts ReverseGeoService and registers an receiver for response
	 * 
	 * @param pShowDeviceCityChangedAlert
	 */
	protected void startReverseGeoService(boolean pShowDeviceCityChangedAlert) {
		mDeviceCityActionFilter = new IntentFilter(ReverseGeoService.ACTION_REVERSE_GEO_RESPONSE);
		mDeviceCityReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				onReverseGeoServiceResponse(intent);
			}
		};
		registerBroadcastReceivers();
		Intent intent = new Intent(this, ReverseGeoService.class);
		startService(intent);
	}

	@Override
	protected void onResume() {
		if (mIsGooglePlayOpened) {
			mIsGooglePlayOpened = false;
			setAppNotInBackground();
		}
		super.onResume();
		AnalyticsHelper.onActivityResume(this);
		mCanShowAnyDialog = true;
		checkPlayServices(true);
		registerBroadcastReceivers();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		mCanShowAnyDialog = false;
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		mCanShowAnyDialog = false;
		super.onPause();
		unregisterBroadcastReceivers();
	}

	/**
	 * Should be called after receivers are created and/or from onResume
	 */
	private void registerBroadcastReceivers() {
		if (mDealsRefreshBroadcastReceiver != null) {
			try {
				registerReceiver(mDealsRefreshBroadcastReceiver, mDealsRefreshActionFilter);
			} catch (Exception e) {
				// ignore
			}
		}
		if (mDeviceCityReceiver != null) {
			try {
				mLocalBroadcastManager.registerReceiver(mDeviceCityReceiver, mDeviceCityActionFilter);
			} catch (Exception e) {
				// ignore
			}
		}
	}

	/**
	 * Should be called from onPause
	 */
	private void unregisterBroadcastReceivers() {
		if (mDealsRefreshBroadcastReceiver != null) {
			try {
				unregisterReceiver(mDealsRefreshBroadcastReceiver);
			} catch (Exception e) {
				// ignore
			}
		}
		if (mDeviceCityReceiver != null) {
			try {
				mLocalBroadcastManager.unregisterReceiver(mDeviceCityReceiver);
			} catch (Exception e) {
				// ignore
			}
		}
	}

	/**
	 * Any screen can override this
	 * 
	 * @param intent
	 */
	protected void onReverseGeoServiceResponse(Intent intent) {
		AppStaticVars.REVERSE_GEO_CODED_CITY = intent.getStringExtra(ReverseGeoService.EXTRA_REVERSE_GEO_CODED_CITY);
		if (BuildConfig.DEBUG) {
			Log.i(getClass().getSimpleName(), "onReverseGeoServiceResponse() City: " + AppStaticVars.REVERSE_GEO_CODED_CITY);
		}
	}

	/**
	 * Same string array is used as tab-titles and tab-id(s)
	 * 
	 * @param categoryId
	 * @param pTabChangeListener
	 * 
	 * @return first tabId
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected String setUpTabHost(int categoryId, OnTabChangeListener pOnTabChangeListener) {
		FragmentTabHost fragTabHost = (FragmentTabHost) findViewById(R.id.tabhost);
		fragTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
		fragTabHost.setOnTabChangedListener(pOnTabChangeListener);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			fragTabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
		}
		String[] tabIdsArr = getResources().getStringArray(R.array.tab_title);
		Class<?>[] fragClassesArr = { DealsListFragment.class,
				DealsListFragment.class, DealsListFragment.class };

		Bundle featuredBundle = new Bundle();
		featuredBundle.putInt(Constants.EXTRA_DEAL_TYPE, ApiConstants.VALUE_DEAL_TYPE_FEATURED);
		Bundle trendingBundle = new Bundle();
		trendingBundle.putInt(Constants.EXTRA_DEAL_TYPE, ApiConstants.VALUE_DEAL_TYPE_TRENDING);
		Bundle nearByBundle = new Bundle();
		nearByBundle.putInt(Constants.EXTRA_DEAL_TYPE, ApiConstants.VALUE_DEAL_TYPE_NEAR_BY);
		if (categoryId != 0) {
			featuredBundle.putInt(Constants.EXTRA_CATEGORY_ID, categoryId);
			trendingBundle.putInt(Constants.EXTRA_CATEGORY_ID, categoryId);
			nearByBundle.putInt(Constants.EXTRA_CATEGORY_ID, categoryId);
		}
		Bundle[] bundlesArr = { nearByBundle, trendingBundle, featuredBundle };

		for (int i = 0; i < 3; i++) {
			View tabView = getLayoutInflater().inflate(R.layout.tab_design, null);
			((TextView) tabView.findViewById(R.id.tab_title)).setText(tabIdsArr[i]);
			fragTabHost.addTab(fragTabHost.newTabSpec(tabIdsArr[i]).setIndicator(tabView), fragClassesArr[i], bundlesArr[i]);
		}
		return tabIdsArr[0];
	}

	/**
	 * Same string array is used as tab-titles and tab-id(s)
	 * 
	 * @param onAllFrags
	 * @param currentFragmentTag
	 */
	protected void updateDealsListOnFragments(int pDealsListRequestType, String currentFragmentTag) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(currentFragmentTag);
		if (fragment instanceof DealsListFragment) {
			((DealsListFragment) fragment).resetDealsList(pDealsListRequestType);
		}
		if (pDealsListRequestType == DealsListRequest.REQUEST_IF_NO_DEALS) {
			return;
		}
		String[] tabIdsArr = getResources().getStringArray(R.array.tab_title);
		if (this instanceof SearchResultsActivity) {
			tabIdsArr = getResources().getStringArray(R.array.tab_titles_search_screen_temp);
		} else if (this instanceof CouponsActivity) {
			tabIdsArr = getResources().getStringArray(R.array.tab_title_coupons);
		}
		for (String tabId : tabIdsArr) {
			if (tabId.equals(currentFragmentTag)) {
				continue;
			}
			fragment = fragmentManager.findFragmentByTag(tabId);
			if (fragment instanceof DealsListFragment) {
				((DealsListFragment) fragment).resetDealsList(pDealsListRequestType);
			}
		}
	}

	/**
	 * shows a dialog with message/buttons for Location Settings
	 */
	protected void showLocationRequiredDialog() {
		if (AppStaticVars.LOCATION_OR_GPS_DIALOG_SHOWN || !mCanShowAnyDialog) {
			if (BuildConfig.DEBUG) {
				Log.i(getClass().getSimpleName(), "showLocationRequiredDialog() mCanShowAnyDialog: " + mCanShowAnyDialog);
			}
			return;
		}
		CommonDialog dialog = new CommonDialog();
		Bundle bundle = new Bundle();
		if (LocationUtils.isNetworkProviderEnabled(this)) {
			bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.GPS_IS_OFF_DIALOG);
			dialog.setArguments(bundle);
			dialog.show(getSupportFragmentManager(), Constants.TAG_GPS_IS_OFF_DIALOG);
		} else {
			bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.LOCATION_IS_OFF_DIALOG);
			dialog.setArguments(bundle);
			dialog.show(getSupportFragmentManager(), Constants.TAG_LOCATION_IS_OFF_DIALOG);
		}
		AppStaticVars.LOCATION_OR_GPS_DIALOG_SHOWN = true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Constants.RQ_PLAY_SERVICES_RESOLUTION: {
			if (resultCode == RESULT_OK && mLocationClient != null) {
				mLocationClient.connect();
			}
			break;
		}
		case Constants.RQ_OPEN_LOCATION_SETTINGS: {
			if (ReverseGeoService.shouldStart(this)) {
				if (this instanceof UserPrefrenceActivity) {
					showProgressDialog(getString(R.string.prog_loading));
					startReverseGeoService(false);
				} else if (this instanceof HomeActivity) {
					startReverseGeoService(true);
				}
			} else {
				if (this instanceof HomeActivity) {
					if (mCanShowAnyDialog) {
						AppRater.getInstance().showDialog(this, true);
					} else {
						if (BuildConfig.DEBUG) {
							Log.i(getClass().getSimpleName(), "onActivityResult() mCanShowAnyDialog: " + mCanShowAnyDialog);
						}
					}
				}
			}
			break;
		}
		}
	}

	/**
	 * shows a dialog with message/buttons for City Changed
	 */
	protected void showCityChangeConfirmDialog() {
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(Constants.TAG_CITY_CHANGE_CONFIRM_DIALOG);
		if (fragment instanceof DialogFragment) {
			DialogFragment dialogFragment = (DialogFragment) fragment;
			dialogFragment.dismiss();
		}
		CommonDialog dialog = new CommonDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.CITY_CHANGE_CONFIRM_DIALOG);
		dialog.setArguments(bundle);
		dialog.show(getSupportFragmentManager(), Constants.TAG_CITY_CHANGE_CONFIRM_DIALOG);
	}

	/**
	 * 
	 * @param txvCartIconWithCount
	 * @param isFlipCount
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void updateCartBadge(final TextView txvCartIconWithCount, boolean isFlipCount) {
		final int cartItemsCount = GrouponGoPrefs.getCartItemsCount(this);
		if (cartItemsCount > 0) {
			txvCartIconWithCount.setBackgroundResource(R.drawable.shape_white_circle);
			txvCartIconWithCount.setText("" + cartItemsCount);
		} else {
			txvCartIconWithCount.setText("");
			txvCartIconWithCount.setBackgroundResource(0);
		}
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB && isFlipCount) {
			ObjectAnimator animation = ObjectAnimator.ofFloat(txvCartIconWithCount, "rotationY", 0.0f, 360f);
			animation.setDuration(1800);
			animation.setRepeatCount(0);
			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			animation.start();
		}
	}

	/**
	 * 
	 */
	protected void clearTopTillHomeActivity() {
		Intent intent = new Intent(this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	/**
	 * check that device has play service APK or not
	 * 
	 * @param shouldShowDialog
	 */
	protected boolean checkPlayServices(boolean shouldShowDialog) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		}
		if (!GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
			return false;
		}
		if (!shouldShowDialog || !mCanShowAnyDialog) {
			if (BuildConfig.DEBUG) {
				Log.i(getClass().getSimpleName(), "checkPlayServices() " + shouldShowDialog + ", " + mCanShowAnyDialog);
			}
			return false;
		}
		Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, Constants.RQ_PLAY_SERVICES_RESOLUTION);
		if (dialog != null) {
			dialog.show();
		}
		return false;
	}

	@Override
	public void showProgressDialog(String bodyText) {
		removeProgressDialog();
		if (isFinishing()) {
			return;
		}
		CustomProgressDialog customProgressDialog = new CustomProgressDialog();
		customProgressDialog.setCancelable(false);
		// customProgressDialog.setMessage(bodyText);
		try {
			customProgressDialog.show(getSupportFragmentManager(), Constants.TAG_CUSTOM_PROGRESS_BAR);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "showProgressDialog()", e);
		}
	}

	@Override
	public void removeProgressDialog() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(Constants.TAG_CUSTOM_PROGRESS_BAR);
		if (fragment instanceof DialogFragment) {
			DialogFragment dialogFragment = (DialogFragment) fragment;
			try {
				dialogFragment.dismissAllowingStateLoss();
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), "removeProgressDialog()", e);
			}
		}
	}
}