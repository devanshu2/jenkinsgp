package com.kelltontech.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.groupon.go.BuildConfig;
import com.kelltontech.application.BaseApplication;
import com.kelltontech.utils.LocationUtils;

/**
 * @author sachin.gupta
 */
public class LocationService extends IntentService implements ConnectionCallbacks, OnConnectionFailedListener {

	private static final String	LOG_TAG						= LocationService.class.getSimpleName();

	public static final String	ACTION_LOCATION_FETCHED		= "action_location_fetched";
	public static final String	EXTRA_LOCATION_PROVIDER		= "extra_location_provider";
	public static final String	EXTRA_CURRENT_LATITUDE		= "extra_current_latitude";
	public static final String	EXTRA_CURRENT_LONGITUDE		= "extra_current_longitude";

	private static final int	MINUTES_MAX_LOCATION_AGE	= 5;
	private static final long	MILLIS_NEXT_BROADCAST		= 5 * DateUtils.MINUTE_IN_MILLIS;
	private static final long	MILLIS_AUTO_RESTART			= 1 * DateUtils.MINUTE_IN_MILLIS;

	private LocationClient		mLocationClient;

	/**
	 * No argument constructor
	 */
	public LocationService() {
		super(LocationService.class.getSimpleName());
	}

	/**
	 * @param pContext
	 */
	public static void start(Context pContext) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "start()");
		}
		Intent intent = new Intent(pContext, LocationService.class);
		pContext.startService(intent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onHandleIntent");
		}
		if (!LocationUtils.isLocationEnabled(this)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Location Services are not enabled.");
			}
			return;
		}
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
		scheduleRestart(MILLIS_AUTO_RESTART);
	}

	@Override
	public void onConnected(Bundle arg0) {
		if (mLocationClient == null || !mLocationClient.isConnected()) {
			scheduleRestart(MILLIS_AUTO_RESTART);
		}
		Location deviceLocation = mLocationClient.getLastLocation();
		if (deviceLocation == null) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Device Location is not available.");
			}
			scheduleRestart(MILLIS_AUTO_RESTART);
			return;
		}
		if (!LocationUtils.isRecentLocation(deviceLocation, MINUTES_MAX_LOCATION_AGE)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Device Location is not recent.");
			}
			scheduleRestart(MILLIS_AUTO_RESTART);
			return;
		}
		sendLocationFetchedBroadcast(deviceLocation);
		long nextBroadcastMillis = MILLIS_NEXT_BROADCAST - LocationUtils.getLocationAge(deviceLocation);
		if (nextBroadcastMillis < MILLIS_AUTO_RESTART) {
			scheduleRestart(MILLIS_AUTO_RESTART);
		} else {
			scheduleRestart(nextBroadcastMillis);
		}
	}

	@Override
	public void onDisconnected() {
		// Nothing to do here
	}

	/**
	 * @param geoCodedCity
	 */
	private void sendLocationFetchedBroadcast(Location pLocation) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "sendLocationFetchedBroadcast()");
		}
		Intent intent = new Intent(ACTION_LOCATION_FETCHED);
		intent.putExtra(EXTRA_LOCATION_PROVIDER, pLocation.getProvider());
		intent.putExtra(EXTRA_CURRENT_LATITUDE, pLocation.getLatitude());
		intent.putExtra(EXTRA_CURRENT_LONGITUDE, pLocation.getLongitude());
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
	}

	/**
	 * @param pRestartMillis
	 */
	@SuppressLint({ "InlinedApi", "NewApi" })
	private void scheduleRestart(long pRestartMillis) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "scheduleRestart() Restart Millis: " + pRestartMillis);
		}
		Application application = getApplication();
		if (application instanceof BaseApplication) {
			if (((BaseApplication) application).isAppInBackground()) {
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "scheduleRestart() App is in background.");
				}
				return;
			}
		}
		Intent intent = new Intent(this, LocationService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		}
		PendingIntent pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		long triggerTime = SystemClock.elapsedRealtime() + pRestartMillis;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
		} else {
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
		}
	}
}