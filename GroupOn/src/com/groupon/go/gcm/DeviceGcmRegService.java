package com.groupon.go.gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.demach.konotor.Konotor;
import com.demach.konotor.access.K;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.controller.RegistrationController;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.utils.ApplicationUtils;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;

/**
 * @author sachin.gupta
 */
public class DeviceGcmRegService extends IntentService {

	private static final String	LOG_TAG	= "DeviceGcmRegService";

	/**
	 * No argument constructor
	 */
	public DeviceGcmRegService() {
		super(DeviceGcmRegService.class.getSimpleName());
	}

	@Override
	public final void onHandleIntent(Intent intent) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onHandleIntent");
		}
		Context pContext = getApplicationContext();
		/**
		 * Check if app was updated; if yes, update it and clears GCM Reg ID
		 */
		int savedAppVersionCode = GrouponGoPrefs.getAppVersionCode(pContext);
		int currentVersionCode = ApplicationUtils.getAppVersionCode(pContext);
		boolean isAlreadyRegistered = false;
		if (savedAppVersionCode != currentVersionCode) {
			GrouponGoPrefs.setAppVersionCode(pContext, currentVersionCode);
			GrouponGoPrefs.setDeviceRegistered(pContext, false);
			GrouponGoPrefs.setGcmRegId(pContext, "");
		} else {
			isAlreadyRegistered = GrouponGoPrefs.isDeviceRegistered(pContext);
		}

		if (isAlreadyRegistered) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Already registered. GCM Reg ID: " + GrouponGoPrefs.getGcmRegId(pContext));
			}
			return;
		}
		String gcmRegId = GrouponGoPrefs.getGcmRegId(pContext);
		if (StringUtils.isNullOrEmpty(gcmRegId)) {
			gcmRegId = registerDeviceWithGcm();
		}
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "GCM Reg ID: " + gcmRegId);
		}
		if (StringUtils.isNullOrEmpty(gcmRegId)) {
			// GCM Reg ID not available
			return;
		} else {
			GrouponGoPrefs.setGcmRegId(pContext, gcmRegId);
			registerGcmForKonotor();
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Device is out of network coverage area.");
			}
			return;
		}
		IScreen fakeScreen = new IScreen() {
			@Override
			public void handleUiUpdate(ServiceResponse serviceResponse) {
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "handleUiUpdate: " + serviceResponse.isSuccess());
				}
				if (serviceResponse.isSuccess()) {
					GrouponGoPrefs.setDeviceRegistered(getApplicationContext(), true);
				}
			}

			@Override
			public void onEvent(int eventId, Object eventData) {
				// nothing to do here
			}
		};
		RegistrationController controller = new RegistrationController(this, fakeScreen);
		controller.getData(ApiConstants.API_DEVICE_REGISTRATION, gcmRegId);
	}

	/**
	 * @return
	 */
	private String registerDeviceWithGcm() {
		try {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
			return gcm.register(getString(R.string.gcm_sender_id));
		} catch (Exception ex) {
			Log.e(LOG_TAG, "registerDeviceWithGcm()", ex);
		}
		return null;
	}

	/**
	 * 
	 */
	private void registerGcmForKonotor() {
		try {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
			String regid = gcm.register(K.ANDROID_PROJECT_SENDER_ID);
			Konotor.getInstance(getApplicationContext()).updateGcmRegistrationId(regid);
		} catch (Exception ex) {
			Log.e(LOG_TAG, "registerGcmForKonotor()", ex);
		}
	}
}
