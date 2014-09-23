package com.groupon.go.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.application.GrouponGoApp;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.database.MyPurchasesHelper;
import com.groupon.go.model.MyOfferLocation;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.activity.HomeActivity;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.utils.DateTimeUtils;
import com.kelltontech.utils.DateTimeUtils.Format;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.NotificationUtils;
import com.kelltontech.utils.StringUtils;

/**
 * @author sachin.gupta
 */
public class CouponsNotificationService extends IntentService implements ConnectionCallbacks, OnConnectionFailedListener {

	private static final String	LOG_TAG					= CouponsNotificationService.class.getSimpleName();

	private static final long	DEFAULT_RESTART_DELAY	= DateUtils.MINUTE_IN_MILLIS;

	private static final int	RESTART_DEFAULT_DELAYED	= 1;
	private static final int	RESTART_ON_NEXT_DAY		= 2;

	private LocationClient		mLocationClient;
	private boolean				mIsInProgress;

	/**
	 * No argument constructor
	 */
	public CouponsNotificationService() {
		super(CouponsNotificationService.class.getSimpleName());
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "CouponsNotificationService()");
		}
	}

	/**
	 * @param pContext
	 */
	public static void start(Context pContext) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "start()");
		}
		Intent intent = new Intent(pContext, CouponsNotificationService.class);
		pContext.startService(intent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onHandleIntent");
		}
		if (mIsInProgress) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Already processing coupon locations.");
			}
			return;
		}
		if (!LocationUtils.isLocationEnabled(this)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Location Services are not enabled.");
			}
			return;
		}
		mIsInProgress = true;
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(this, this, this);
		}
		mLocationClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onConnectionFailed() " + connectionResult);
		}
		mIsInProgress = false;
		scheduleRestart(RESTART_DEFAULT_DELAYED);
	}

	@Override
	public void onConnected(Bundle arg0) {
		Location deviceLocation = null;
		if (mLocationClient != null && mLocationClient.isConnected()) {
			deviceLocation = mLocationClient.getLastLocation();
			mLocationClient.disconnect();
		}
		if (deviceLocation == null) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Device Location is not available.");
			}
			scheduleRestart(RESTART_DEFAULT_DELAYED);
			return;
		}
		if (!LocationUtils.isRecentLocation(deviceLocation)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Device Location is not recent.");
			}
			scheduleRestart(RESTART_DEFAULT_DELAYED);
			return;
		}
		GrouponGoPrefs.setDeviceLocation(this, deviceLocation);

		GrouponGoApp grouponGoApp = (GrouponGoApp) this.getApplication();
		MyPurchasesHelper myPurchasesHelper = grouponGoApp.getMyPurchasesHelper();
		ArrayList<MyOfferLocation> myOfferLocationsList = myPurchasesHelper.getNotifiableOfferLocations(this);
		if (myOfferLocationsList == null || myOfferLocationsList.isEmpty()) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Notifiable Offer Locations List is empty.");
			}
			scheduleRestart(RESTART_ON_NEXT_DAY);
		} else {
			boolean allNotified = sendNotificationForCoupons(myOfferLocationsList, deviceLocation);
			if (allNotified) {
				scheduleRestart(RESTART_ON_NEXT_DAY);
			} else {
				scheduleRestart(RESTART_DEFAULT_DELAYED);
			}
		}
		mIsInProgress = false;
	}

	@Override
	public void onDisconnected() {
		// Nothing to do here
	}

	/**
	 * Post notification of received message.
	 * 
	 * @param pMyOfferLocationsList
	 * @param pDeviceLocation
	 */
	private boolean sendNotificationForCoupons(List<MyOfferLocation> pMyOfferLocationsList, Location pDeviceLocation) {
		if (!DateTimeUtils.equalsIgnoreTime(GrouponGoPrefs.getLastNotifiedByVicinityOn(this), new Date().getTime())) {
			GrouponGoPrefs.clearNotifiedByVicinityIds(this);
		}
		boolean allNotified = true;
		String notifiedCustomIds = GrouponGoPrefs.getNotifiedByVicinityIds(this);
		double endLat = pDeviceLocation.getLatitude();
		double endLong = pDeviceLocation.getLongitude();
		float vicinityRadius = GrouponGoPrefs.getVicinityRadius(getApplicationContext());
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "Vicinity Radius is " + vicinityRadius + " meters.");
		}
		for (MyOfferLocation myOfferLocation : pMyOfferLocationsList) {
			if (notifiedCustomIds.contains(myOfferLocation.getCustomId())) {
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "Already notified. " + myOfferLocation.getCustomId());
				}
				continue;
			}
			double startLat = myOfferLocation.getLatitude();
			double startLong = myOfferLocation.getLongitude();
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "From " + startLat + ", " + startLong + ", to " + endLat + ", " + endLong);
			}
			float minDistance = LocationUtils.getDistance(startLat, startLong, endLat, endLong);
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Min Distance is " + minDistance + " meters.");
			}
			if (minDistance == -1 || minDistance > vicinityRadius) {
				allNotified = false;
				continue;
			}

			int vouchers = myOfferLocation.getVouchers();
			List<MyOfferLocation> countedLocationsList = new ArrayList<MyOfferLocation>();
			countedLocationsList.add(myOfferLocation);

			for (MyOfferLocation otherOfferLocation : pMyOfferLocationsList) {
				if (otherOfferLocation.getMerchant_id() != myOfferLocation.getMerchant_id()) {
					continue;
				}
				startLat = otherOfferLocation.getLatitude();
				startLong = otherOfferLocation.getLongitude();
				float moreCouponsAtDistance = LocationUtils.getDistance(startLat, startLong, endLat, endLong);
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "Distance is " + moreCouponsAtDistance + " meters.");
				}
				if (moreCouponsAtDistance != -1 && moreCouponsAtDistance <= vicinityRadius) {

					if (moreCouponsAtDistance < minDistance) {
						minDistance = moreCouponsAtDistance;
					}
					if (!ProjectUtils.isInAlreadyCountedLocationsList(otherOfferLocation, countedLocationsList)) {
						vouchers += otherOfferLocation.getVouchers();
						countedLocationsList.add(otherOfferLocation);
					}
				}
			}
			Builder builder = Uri.parse("groupongo://notification").buildUpon();
			builder.appendQueryParameter(ApiConstants.PARAM_NOTIFY_TYPE, "" + ApiConstants.VALUE_NOTIFY_TYPE_SHOW_COUPONS);

			// below param is to prevent pending intent equality.
			builder.appendQueryParameter("unique_param", "" + myOfferLocation.getMerchant_id());

			String minDistanceStr;
			if (minDistance > 1000) {
				minDistanceStr = StringUtils.getFormatDecimalAmount(minDistance / 1000, 1) + " km";
			} else {
				minDistance = (int) (minDistance / 25) * 25;
				minDistanceStr = (int) minDistance + " meters";
			}

			String notificationMessage = this.getString(R.string.msg_vicinity_notification, vouchers, myOfferLocation.getMerchant_name(), minDistanceStr);
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Merchant Name: " + myOfferLocation.getMerchant_name());
			}
			Intent activityIntent = new Intent(this, HomeActivity.class);
			activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			activityIntent.setData(builder.build());
			activityIntent.putExtra(Constants.EXTRA_MESSAGE, notificationMessage);

			NotificationUtils.showNotification(this, notificationMessage, activityIntent, "" + myOfferLocation.getMerchant_id(), ApiConstants.VALUE_NOTIFY_TYPE_SHOW_COUPONS);

			GrouponGoPrefs.setLastNotifiedByVicinityOn(this, new Date().getTime());
			GrouponGoPrefs.appendNotifiedByVicinityId(this, myOfferLocation.getCustomId());
		}
		return allNotified;
	}

	/**
	 * @param pRestartType
	 */
	@SuppressLint({ "InlinedApi", "NewApi" })
	private void scheduleRestart(int pRestartType) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "scheduleRestart() Required Restart Type: " + pRestartType);
		}
		Intent intent = new Intent(this, CouponsNotificationService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		}
		PendingIntent pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		switch (pRestartType) {
		case RESTART_DEFAULT_DELAYED: {
			long triggerTime = SystemClock.elapsedRealtime() + DEFAULT_RESTART_DELAY;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
			} else {
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
			}
			break;
		}
		case RESTART_ON_NEXT_DAY: {
			long triggerTime = DateTimeUtils.getMidNightMillis(1);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pi);
			} else {
				am.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
			}
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "scheduleRestart() currentTime: " + DateTimeUtils.getFormattedDate(System.currentTimeMillis(), Format.DD_Mmmm_YYYY_HH_MM));
				Log.i(LOG_TAG, "scheduleRestart() triggerTime: " + DateTimeUtils.getFormattedDate(triggerTime, Format.DD_Mmmm_YYYY_HH_MM));
			}
			break;
		}
		}
	}
}
