package com.kelltontech.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.controller.ReverseGeoController;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.StringUtils;

/**
 * @author sachin.gupta
 */
public class ReverseGeoService extends IntentService implements ConnectionCallbacks, OnConnectionFailedListener {

	/**
	 * string constant use as action in broadcasts
	 */
	private static final String		LOG_TAG							= ReverseGeoService.class.getSimpleName();

	public static final String		ACTION_REVERSE_GEO_RESPONSE		= "action_reverse_geo_response";
	public static final String		EXTRA_REVERSE_GEO_CODED_CITY	= "extra_reverse_geo_coded_city";

	private ReverseGeoController	mReverseGeoController;
	private LocationClient			mLocationClient;
	private Location				mLocation;
	private boolean					mIsInProgress;

	/**
	 * No argument constructor
	 */
	public ReverseGeoService() {
		super(ReverseGeoService.class.getSimpleName());
	}

	/**
	 * check if we should starts ReverseGeoService
	 */
	public static boolean shouldStart(Context context) {
		if (!LocationUtils.isLocationEnabled(context)) {
			return false;
		}
		if (!ConnectivityUtils.isNetworkEnabled(context)) {
			return false;
		}
		return true;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onHandleIntent");
		}
		if (mIsInProgress) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Response of last request is not received yet.");
			}
			return;
		}
		if (!LocationUtils.isLocationEnabled(this)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Location Services are not enabled.");
			}
			sendDeviceCityBroadcast(null);
			return;
		}
		mIsInProgress = true;
		mLocation = null;
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
		sendDeviceCityBroadcast(null);
	}

	@Override
	public void onConnected(Bundle arg0) {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			mLocation = mLocationClient.getLastLocation();
			mLocationClient.disconnect();
		}
		if (mLocation == null) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Device Location is not available.");
			}
			sendDeviceCityBroadcast(null);
			return;
		}
		if (!LocationUtils.isRecentLocation(mLocation)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Device Location is not recent.");
			}
			sendDeviceCityBroadcast(null);
			return;
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Device is out of network coverage area.");
			}
			sendDeviceCityBroadcast(null);
			return;
		}
		IScreen fakeScreen = new IScreen() {
			@Override
			public void handleUiUpdate(ServiceResponse serviceResponse) {
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "handleUiUpdate: " + serviceResponse.isSuccess());
				}
				switch (serviceResponse.getDataType()) {
				case ApiConstants.GET_GEO_ADDRESS_BY_LAT_LONG: {
					if (serviceResponse.isSuccess() && serviceResponse.getResponseObject() instanceof Address) {
						sendDeviceCityBroadcast(((Address) serviceResponse.getResponseObject()).getLocality());
					} else if (ConnectivityUtils.isNetworkEnabled(getApplicationContext())) {
						mReverseGeoController.getData(ApiConstants.GET_CITY_BY_LAT_LONG_GOOGLE_API, mLocation);
					} else {
						sendDeviceCityBroadcast(null);
					}
					break;
				}
				case ApiConstants.GET_CITY_BY_LAT_LONG_GOOGLE_API: {
					if (serviceResponse.isSuccess()) {
						if (serviceResponse.getResponseObject() instanceof String) {
							sendDeviceCityBroadcast(((String) serviceResponse.getResponseObject()).trim());
						} else {
							sendDeviceCityBroadcast(null);
						}
					} else {
						sendDeviceCityBroadcast(null);
					}
					break;
				}
				}
			}

			@Override
			public void onEvent(int eventId, Object eventData) {
				// nothing to do here
			}
		};

		mReverseGeoController = new ReverseGeoController(this, fakeScreen);
		mReverseGeoController.getData(ApiConstants.GET_GEO_ADDRESS_BY_LAT_LONG, mLocation);
	}

	@Override
	public void onDisconnected() {
		// Nothing to do here
	}

	/**
	 * @param geoCodedCity
	 */
	private void sendDeviceCityBroadcast(String geoCodedCity) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "sendDeviceCityBroadcast: " + geoCodedCity);
		}
		mIsInProgress = false;
		Intent intent = new Intent(ACTION_REVERSE_GEO_RESPONSE);
		intent.putExtra(EXTRA_REVERSE_GEO_CODED_CITY, geoCodedCity);
		if (!StringUtils.isNullOrEmpty(geoCodedCity) && mLocation != null) {
			GrouponGoPrefs.setDeviceLocation(this, mLocation);
		}
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
	}
}