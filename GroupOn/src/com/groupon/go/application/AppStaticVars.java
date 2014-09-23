package com.groupon.go.application;

import java.util.Date;

import android.content.Context;
import android.text.format.DateUtils;

import com.groupon.go.persistance.GrouponGoPrefs;

/**
 * This class maintain values for few such variables: <br/>
 * 1. which need to be initialized in one class. <br/>
 * 2. and then need to be used by some other class.
 * 
 * @note To initialize and then use any variable in different activities, use
 *       SharedPreferences instead.
 * 
 * @note Any new variable should be added in this class after considering
 *       following: <br/>
 *       1. Either variable is initialized by the using activity itself. OR <br/>
 *       2. Variable can be used, even if it is set to its java default value.
 */
public class AppStaticVars {
	/**
	 * This boolean will be used to show Location Dialog only once in each run
	 * of application.
	 */
	public static boolean	LOCATION_OR_GPS_DIALOG_SHOWN;

	/**
	 * This boolean will be used to handle start and finish of location
	 * connection attempt
	 */
	public static boolean	CONNECTING_LOCATION_CLIENT;

	/**
	 * This String will be used to save geo-coded-city only for current session
	 * of application.
	 */
	public static String	REVERSE_GEO_CODED_CITY;

	/**
	 * These boolean will be used to reset deals list on various screens.<br/>
	 */
	public static boolean	REQUEST_FIRST_PAGE_ON_HOME;
	public static boolean	REQUEST_REFRESH_PAGES_ON_HOME;

	/**
	 * Application may not be completely closed instantly even if back is
	 * pressed on home screen. <br/>
	 * This method reset static variables of this class to handle above.
	 * 
	 * @param pContext
	 *            not null only if application is resuming from background
	 */
	public static void resetDefaults(Context pContext) {
		REVERSE_GEO_CODED_CITY = null;
		if (pContext != null) {
			long resumedAtMillis = new Date().getTime();
			long nearByDealsRestAt = GrouponGoPrefs.getNearByDealsResetAt(pContext);
			long dealsUpdateInterval = GrouponGoPrefs.getDealsUpdateInterval(pContext) * DateUtils.MINUTE_IN_MILLIS;
			if (resumedAtMillis > nearByDealsRestAt + dealsUpdateInterval) {
				REQUEST_REFRESH_PAGES_ON_HOME = true;
			}
			return;
		}
		LOCATION_OR_GPS_DIALOG_SHOWN = false;
		REQUEST_FIRST_PAGE_ON_HOME = false;
		REQUEST_REFRESH_PAGES_ON_HOME = false;
	}
}
