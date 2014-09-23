package com.groupon.go.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;

import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.service.CouponsNotificationService;
import com.kelltontech.utils.LocationUtils;

/**
 * @author sachin.gupta
 */
public class CommonReceiver extends BroadcastReceiver {

	private static final String	LOG_TAG	= "CommonReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "Action: " + action);
		}
		if (!GrouponGoPrefs.canLaunchHome(context)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Registration is not completed.");
			}
			return;
		}
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			CouponsNotificationService.start(context);
		} else if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {
			if (LocationUtils.isLocationEnabled(context)) {
				CouponsNotificationService.start(context);
			}
		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			BackGroundService.postRedeemedVouchers(context, true);
		} else if (Intent.ACTION_DATE_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action) || Intent.ACTION_TIME_CHANGED.equals(action)) {
			CouponsNotificationService.start(context);
		}
	}
}