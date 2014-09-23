package com.groupon.go.application;

import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.database.MyPurchasesHelper;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.application.BaseApplication;
import com.kelltontech.ui.widget.AppRater;
import com.kelltontech.utils.FontUtils;
import com.kelltontech.volleyx.VolleyManager;
import com.mobileapptracker.MobileAppTracker;

/**
 * This class holds some application-global instances.
 */
public class GrouponGoApp extends BaseApplication {

	private final String		LOG_TAG	= "GrouponGoApp";

	private Tracker				mGoogleAnalyticsTracker;
	private MobileAppTracker	mMobileAppTracker;

	@Override
	public void onCreate() {
		super.onCreate();
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onCreate()");
		}
		VolleyManager.initialize(this);

		boolean isChanged = FontUtils.changeDeviceTypeface(this, "MONOSPACE", getString(R.string.font_museosans_500));
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "changeDeviceTypeface() " + isChanged);
		}

		AppRater.initialize(this);
		AppRater.getInstance().setEnabled(false);
	}

	@Override
	protected void onAppStateSwitched(boolean isAppInBackground) {
		super.onAppStateSwitched(isAppInBackground);
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onAppStateSwitched() " + isAppInBackground);
		}
		if (isAppInBackground) {
			ProjectUtils.setPeriodicDealsRefreshBroadcast(this, true);
		}
	}

	/**
	 * @return non-static singleton instance of GA Tracker
	 */
	public synchronized Tracker getGoogleAnalyticsTracker() {
		if (mGoogleAnalyticsTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			mGoogleAnalyticsTracker = analytics.newTracker(R.xml.app_tracker);
		}
		return mGoogleAnalyticsTracker;
	}

	/**
	 * @return non-static singleton instance of Mobile App Tracker
	 */
	public synchronized MobileAppTracker getMobileAppTracker() {
		if (mMobileAppTracker == null) {
			MobileAppTracker.init(getApplicationContext(), getString(R.string.mat_advertiser_id), getString(R.string.mat_conversion_key));
			mMobileAppTracker = MobileAppTracker.getInstance();
		}
		return mMobileAppTracker;
	}

	/**
	 * non-static singleton
	 */
	private MyPurchasesHelper	mMyPurchasesHelper;

	/**
	 * @return non-static singleton
	 */
	public MyPurchasesHelper getMyPurchasesHelper() {
		if (mMyPurchasesHelper == null) {
			mMyPurchasesHelper = new MyPurchasesHelper(this);
		}
		return mMyPurchasesHelper;
	}
}