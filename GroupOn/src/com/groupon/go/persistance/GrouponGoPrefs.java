package com.groupon.go.persistance;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;

import com.groupon.go.R;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.CityModel;
import com.groupon.go.model.CodeVerificationResponse.CodeVerificationResultModel;
import com.groupon.go.model.GetApiAuthTokenResponse.ApiAuthTokenResult;
import com.groupon.go.model.GetProfileResponse.GetProfileResultModel;
import com.groupon.go.model.UserDetails;
import com.kelltontech.persistence.SharedPrefsUtils;
import com.kelltontech.service.ReverseGeoService;
import com.kelltontech.utils.StringUtils;

/**
 * @author sachin.gupta
 */
public class GrouponGoPrefs {

	private static final String	GROUPON_GO_PREFS_FILE_NAME		= "com.groupon.go.prefs";

	private static final String	PREF_DEVICE_DENSITY_GROUP		= "pref_device_density_group";

	private static final String	PREF_DEVICE_LATITUDE			= "pref_device_latitude";
	private static final String	PREF_DEVICE_LONGITUDE			= "pref_device_longitude";
	private static final String	PREF_DEVICE_LOC_PROVIDER		= "pref_device_loc_provider";

	private static final String	PREF_USER_ID					= "pref_user_id";
	private static final String	PREF_COUNTRY_CODE				= "pref_country_code";
	private static final String	PREF_USER_PHONE					= "pref_user_phone";
	private static final String	PREF_MY_COUPON_CODE				= "pref_my_coupon_code";

	private static final String	PREF_CATEGORIES_SCREEN_CLOSED	= "pref_categories_screen_closed";

	private static final String	PREF_USER_NAME					= "pref_user_name";
	private static final String	PREF_USER_EMAIL					= "pref_user_email";
	private static final String	PREF_PROFILE_IMAGE_URL			= "pref_profile_image_url";
	private static final String	PREF_MY_CREDITS					= "pref_my_credits";
	private static final String	PREF_COUPON_DISCOUNT_PERCENTAGE	= "pref_coupon_discount_percentage";
	private static final String	PREF_COUPON_DISCOUNT_MONEY		= "pref_coupon_discount_money";
	private static final String	PREF_CREDIT_PER_PURCHASE		= "pref_credit_per_purchase";

	private static final String	PREF_API_AUTH					= "pref_api_auth";
	private static final String	PREF_API_USER					= "pref_api_user";
	private static final String	PREF_API_TIMESTAMP				= "pref_api_timestamp";

	private static final String	PREF_FB_USER_NAME				= "pref_fb_user_name";

	private static final String	PREF_SELECTED_CITY_ID			= "pref_selected_city_id";
	private static final String	PREF_SELECTED_CITY_NAME			= "pref_selected_city_name";
	private static final String	PREF_SELECTED_CITY_LATITUDE		= "pref_selected_city_latitude";
	private static final String	PREF_SELECTED_CITY_LONGITUDE	= "pref_selected_city_longitude";

	private static final String	PREF_NEAR_BY_DEALS_LOC_PROVIDER	= "pref_near_by_deals_loc_provider";
	private static final String	PREF_NEAR_BY_DEALS_LATITUDE		= "pref_near_by_deals_latitude";
	private static final String	PREF_NEAR_BY_DEALS_LONGITUDE	= "pref_near_by_deals_longitude";

	private static final String	PREF_CART_ITEMS_COUNT			= "pref_user_cart_items_count";

	private static final String	PREF_GCM_REG_ID					= "pref_gcm_reg_id";
	private static final String	PREF_APP_VERSION_CODE			= "pref_app_version_code";
	private static final String	PREF_IS_DEVICE_REGISTERED		= "pref_is_device_registered";

	private static final String	PREF_CAT_ICONS_BASE_URL			= "pref_cat_icons_base_url";
	private static final String	PREF_DEALS_IMAGE_BASE_URL		= "deals_img_base_url";
	private static final String	PREF_DEALS_UPDATE_INTERVAL		= "pref_deals_update_interval";

	private static final String	PREF_PAYMENT_MESSAGE_INDEX		= "pref_payment_message_index";

	private static final String	PREF_REDEEMED_VOUCHER_CODES		= "pref_redeemed_voucher_codes";
	private static final String	PREF_CAN_CLEAR_REDEEMED_CODES	= "pref_can_clear_redeemed_codes";
	private static final String	PREF_SHOULD_UPDATE_PURCHASES	= "pref_should_update_purchases";

	private static final String	PREF_VICINITY_RADIUS			= "pref_vicinity_radius";
	private static final String	PREF_NOTIFIED_BY_VICINITY_IDS	= "pref_notified_by_vicinity_ids";
	private static final String	PREF_LAST_VICINITY_NOTIFIED_ON	= "pref_last_vicinity_notified_on";

	private static final String	PREF_NEAR_BY_DEALS_RESET_AT		= "pref_near_by_deals_reset_at";
	private static final String	PREF_CITY_SELECTED_AT			= "pref_city_selected_at";

	/**
	 * singleton
	 */
	private static UserDetails	mSavedUserDetails;

	/**
	 * @param pContext
	 * @return
	 */
	public static void initializeUser(Context pContext, String pCountryCode, String pMobile, CodeVerificationResultModel pVerificationResult) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = _sharedPref.edit();
		editor.putInt(PREF_USER_ID, pVerificationResult.getUser_id());
		editor.putString(PREF_MY_COUPON_CODE, pVerificationResult.getCoupon_code());
		editor.putString(PREF_COUNTRY_CODE, pCountryCode);
		editor.putString(PREF_USER_PHONE, pMobile);
		editor.apply();
		mSavedUserDetails = null;
	}

	/**
	 * @param pContext
	 * @return
	 */
	public static boolean canLaunchHome(Context pContext) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		int userId = _sharedPref.getInt(PREF_USER_ID, 0);
		boolean categoriesScreenClosed = _sharedPref.getBoolean(PREF_CATEGORIES_SCREEN_CLOSED, false);
		CityModel cityToShowDeals = getCityToShowDeals(pContext);
		return userId != 0 && categoriesScreenClosed && cityToShowDeals != null;
	}

	/**
	 * @param pContext
	 * @return saved user details
	 */
	public static synchronized UserDetails getSavedUserDetails(Context pContext) {
		if (mSavedUserDetails != null) {
			return mSavedUserDetails;
		}
		mSavedUserDetails = new UserDetails();
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		mSavedUserDetails.setUser_id(_sharedPref.getInt(PREF_USER_ID, 0));
		mSavedUserDetails.setCountryCode(_sharedPref.getString(PREF_COUNTRY_CODE, ""));
		mSavedUserDetails.setUserPhone(_sharedPref.getString(PREF_USER_PHONE, ""));
		mSavedUserDetails.setUsername(_sharedPref.getString(PREF_USER_NAME, ""));
		mSavedUserDetails.setEmailId(_sharedPref.getString(PREF_USER_EMAIL, ""));
		mSavedUserDetails.setProfile_image(_sharedPref.getString(PREF_PROFILE_IMAGE_URL, ""));
		mSavedUserDetails.setCoupon_code(_sharedPref.getString(PREF_MY_COUPON_CODE, ""));
		return mSavedUserDetails;
	}

	/**
	 * @param pContext
	 * @return
	 */
	public static synchronized void saveUserDetails(Context pContext, GetProfileResultModel pProfileResult) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = _sharedPref.edit();
		if (pProfileResult.getProfile() != null && !pProfileResult.getProfile().isEmpty()) {
			UserDetails userDetails = pProfileResult.getProfile().get(0);
			editor.putString(PREF_USER_NAME, userDetails.getUsername());
			editor.putString(PREF_USER_PHONE, userDetails.getUserPhone());
			editor.putString(PREF_USER_EMAIL, userDetails.getEmailId());
			editor.putString(PREF_PROFILE_IMAGE_URL, userDetails.getProfile_image());
			editor.putString(PREF_MY_COUPON_CODE, userDetails.getCoupon_code());
		}
		editor.putInt(PREF_MY_CREDITS, pProfileResult.getCredit());
		editor.putInt(PREF_CART_ITEMS_COUNT, pProfileResult.getCart_count());
		editor.putFloat(PREF_COUPON_DISCOUNT_MONEY, pProfileResult.getCoupon_discount_money());
		editor.putFloat(PREF_COUPON_DISCOUNT_PERCENTAGE, pProfileResult.getCoupon_discount_percentage());
		editor.putString(PREF_CREDIT_PER_PURCHASE, pProfileResult.getCredit_per_purchase());

		if (!StringUtils.isNullOrEmpty(pProfileResult.getCat_base_url())) {
			String densityGroupNameStr = _sharedPref.getString(PREF_DEVICE_DENSITY_GROUP, "");
			editor.putString(PREF_CAT_ICONS_BASE_URL, pProfileResult.getCat_base_url() + "android/" + densityGroupNameStr);
		}

		editor.apply();
		mSavedUserDetails = null;
	}

	/**
	 * @param pContext
	 * @return value of PREF_FB_USER_NAME
	 */
	public static String getCategoryIconsBaseUrl(Context pContext) {
		return SharedPrefsUtils.getSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CAT_ICONS_BASE_URL, "");
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_USER_NAME
	 */
	public static void setUserName(Context pContext, String pValue) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_USER_NAME, pValue);
		if (mSavedUserDetails != null) {
			mSavedUserDetails.setUsername(pValue);
		}
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_USER_PHONE
	 */
	public static void setUserPhone(Context pContext, String pValue) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_USER_PHONE, pValue);
		if (mSavedUserDetails != null) {
			mSavedUserDetails.setUserPhone(pValue);
		}
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_USER_EMAIL
	 */
	public static void setUserEmail(Context pContext, String pValue) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_USER_EMAIL, pValue);
		if (mSavedUserDetails != null) {
			mSavedUserDetails.setEmailId(pValue);
		}
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_PROFILE_IMAGE_URL
	 */
	public static void setProfileImageUrl(Context pContext, String pValue) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_PROFILE_IMAGE_URL, pValue);
		if (mSavedUserDetails != null) {
			mSavedUserDetails.setProfile_image(pValue);
		}
	}

	/**
	 * @param apiAuthTokenResult
	 */
	public static void saveApiAuthTokenResult(Context pContext, ApiAuthTokenResult pApiAuthTokenResult) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = _sharedPref.edit();
		editor.putString(PREF_API_AUTH, pApiAuthTokenResult.getAuth());
		editor.putString(PREF_API_USER, pApiAuthTokenResult.getUser());
		editor.putString(PREF_API_TIMESTAMP, pApiAuthTokenResult.getTimestamp());
		editor.apply();
	}

	/**
	 * @param pContext
	 * @return
	 */
	public static ApiAuthTokenResult getApiAuthTokenResult(Context pContext) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		ApiAuthTokenResult result = new ApiAuthTokenResult();
		result.setAuth(_sharedPref.getString(PREF_API_AUTH, ""));
		result.setUser(_sharedPref.getString(PREF_API_USER, ""));
		result.setTimestamp(_sharedPref.getString(PREF_API_TIMESTAMP, ""));
		return result;
	}

	/**
	 * @param pContext
	 * @param pLocation
	 */
	public static void setDeviceLocation(Context pContext, Location pLocation) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = _sharedPref.edit();
		editor.putString(PREF_DEVICE_LOC_PROVIDER, pLocation.getProvider());
		editor.putFloat(PREF_DEVICE_LATITUDE, (float) pLocation.getLatitude());
		editor.putFloat(PREF_DEVICE_LONGITUDE, (float) pLocation.getLongitude());
		editor.apply();
	}

	/**
	 * @param pContext
	 * @return Location with value of PREF_DEVICE_LOC_PROVIDER,
	 *         PREF_DEVICE_LATITUDE and PREF_DEVICE_LONGITUDE
	 */
	public static Location getDeviceLocation(Context pContext) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		String savedLocationProvider = _sharedPref.getString(PREF_DEVICE_LOC_PROVIDER, null);
		if (StringUtils.isNullOrEmpty(savedLocationProvider)) {
			return null;
		}
		Location location = new Location(savedLocationProvider);
		location.setLatitude(_sharedPref.getFloat(PREF_DEVICE_LATITUDE, 0));
		location.setLongitude(_sharedPref.getFloat(PREF_DEVICE_LONGITUDE, 0));
		return location;
	}

	/**
	 * @param context
	 */
	public static void saveNearByDealsLocation(Context pContext) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = _sharedPref.edit();
		editor.putString(PREF_NEAR_BY_DEALS_LOC_PROVIDER, _sharedPref.getString(PREF_DEVICE_LOC_PROVIDER, null));
		editor.putFloat(PREF_NEAR_BY_DEALS_LATITUDE, _sharedPref.getFloat(PREF_DEVICE_LATITUDE, 0));
		editor.putFloat(PREF_NEAR_BY_DEALS_LONGITUDE, _sharedPref.getFloat(PREF_DEVICE_LONGITUDE, 0));
		editor.apply();
	}

	/**
	 * @param pContext
	 * @return Location with values of PREF_NEAR_BY_DEALS_LOC_PROVIDER,
	 *         PREF_NEAR_BY_DEALS_LATITUDE and PREF_NEAR_BY_DEALS_LONGITUDE
	 */
	public static Location getNearByDealsLocation(Context pContext) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		String savedLocationProvider = _sharedPref.getString(PREF_NEAR_BY_DEALS_LOC_PROVIDER, null);
		if (StringUtils.isNullOrEmpty(savedLocationProvider)) {
			return null;
		}
		Location location = new Location(savedLocationProvider);
		location.setLatitude(_sharedPref.getFloat(PREF_NEAR_BY_DEALS_LATITUDE, 0));
		location.setLongitude(_sharedPref.getFloat(PREF_NEAR_BY_DEALS_LONGITUDE, 0));
		return location;
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_NEAR_BY_DEALS_RESET_AT
	 */
	public static void setNearByDealsResetAt(Context pContext, long pValue) {
		SharedPrefsUtils.setSharedPrefLong(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_NEAR_BY_DEALS_RESET_AT, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_NEAR_BY_DEALS_RESET_AT
	 */
	public static long getNearByDealsResetAt(Context pContext) {
		return SharedPrefsUtils.getSharedPrefLong(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_NEAR_BY_DEALS_RESET_AT, 0);
	}

	/**
	 * @param pContext
	 */
	public static void saveCitySelectedAtMillis(Context pContext) {
		SharedPrefsUtils.setSharedPrefLong(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CITY_SELECTED_AT, new Date().getTime());
	}

	/**
	 * @param pContext
	 * @re
	 */
	public static boolean shouldCheckCurrentCity(Context pContext) {
		if (!ReverseGeoService.shouldStart(pContext)) {
			return false;
		}
		long citySelectedAt = SharedPrefsUtils.getSharedPrefLong(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CITY_SELECTED_AT, 0);
		return citySelectedAt + Constants.PERSIST_SELECTED_CITY_MILLIS < new Date().getTime();
	}

	/**
	 * @param pContext
	 * @param pCityToShowDeals
	 *            pCityModel with values for PREF_SELECTED_CITY_ID,
	 *            PREF_SELECTED_CITY_NAME, PREF_SELECTED_CITY_LATITUDE and
	 *            PREF_SELECTED_CITY_LONGITUDE
	 */
	public static void setCityToShowDeals(Context pContext, CityModel pCityToShowDeals) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = _sharedPref.edit();
		editor.putInt(PREF_SELECTED_CITY_ID, pCityToShowDeals.getCity_id());
		editor.putString(PREF_SELECTED_CITY_NAME, pCityToShowDeals.getCity_name());
		editor.putFloat(PREF_SELECTED_CITY_LATITUDE, pCityToShowDeals.getCity_lat());
		editor.putFloat(PREF_SELECTED_CITY_LONGITUDE, pCityToShowDeals.getCity_long());
		editor.apply();
	}

	/**
	 * @param pContext
	 * @return cityToShowDeals with values for PREF_USER_CITY_ID,
	 *         PREF_USER_CITY, PREF_USER_LAT and PREF_USER_LONG
	 * @note PREF_USER_CITY_ID will be 0 if deviceLocation itself is set to be
	 *       used as city to show deals
	 */
	public static CityModel getCityToShowDeals(Context pContext) {
		SharedPreferences _sharedPref = pContext.getSharedPreferences(GROUPON_GO_PREFS_FILE_NAME, Context.MODE_PRIVATE);
		String cityName = _sharedPref.getString(PREF_SELECTED_CITY_NAME, null);
		if (cityName == null) {
			return null;
		}
		CityModel cityToShowDeals = new CityModel();
		cityToShowDeals.setCity_name(cityName);

		cityToShowDeals.setCity_id(_sharedPref.getInt(PREF_SELECTED_CITY_ID, 0));
		cityToShowDeals.setCity_lat(_sharedPref.getFloat(PREF_SELECTED_CITY_LATITUDE, 0));
		cityToShowDeals.setCity_long(_sharedPref.getFloat(PREF_SELECTED_CITY_LONGITUDE, 0));
		return cityToShowDeals;
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_FB_USER_NAME
	 */
	public static void setFbUserName(Context pContext, String pValue) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_FB_USER_NAME, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_FB_USER_NAME
	 */
	public static String getFbUserName(Context pContext) {
		return SharedPrefsUtils.getSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_FB_USER_NAME, "");
	}

	/**
	 * @param pContext
	 * @return value of PREF_DEVICE_DENSITY_GROUP
	 */
	public static String getDeviceDensityGroup(Context pContext) {
		return SharedPrefsUtils.getSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_DEVICE_DENSITY_GROUP, "");
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_DEVICE_DENSITY_GROUP
	 */
	public static void setDeviceDensityGroup(Context pContext, String pValue) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_DEVICE_DENSITY_GROUP, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_CATEGORIES_SCREEN_CLOSED
	 */
	public static boolean isCategoriesScreenClosed(Context pContext) {
		return SharedPrefsUtils.getSharedPrefBoolean(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CATEGORIES_SCREEN_CLOSED, false);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_CATEGORIES_SCREEN_CLOSED
	 */
	public static void setCategoriesScreenClosed(Context pContext, boolean pValue) {
		SharedPrefsUtils.setSharedPrefBoolean(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CATEGORIES_SCREEN_CLOSED, pValue);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_CART_ITEMS_COUNT
	 */
	public static void setCartItemsCount(Context pContext, int pValue) {
		SharedPrefsUtils.setSharedPrefInt(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CART_ITEMS_COUNT, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_CART_ITEMS_COUNT
	 */
	public static int getCartItemsCount(Context pContext) {
		return SharedPrefsUtils.getSharedPrefInt(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CART_ITEMS_COUNT, 0);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_LAST_SHOWN_MSG_INDEX
	 */
	public static void setMyCredits(Context pContext, float pValue) {
		SharedPrefsUtils.setSharedPrefFloat(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_MY_CREDITS, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_COUPON_DISCOUNT_MONEY
	 */
	public static float getCouponDiscountMoney(Context pContext) {
		return SharedPrefsUtils.getSharedPrefFloat(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_COUPON_DISCOUNT_MONEY, 0);
	}

	/**
	 * @param pContext
	 * @return value of PREF_COUPON_DISCOUNT_PERCENTAGE
	 */
	public static float getCouponDiscountPercenatge(Context pContext) {
		return SharedPrefsUtils.getSharedPrefFloat(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_COUPON_DISCOUNT_PERCENTAGE, 0);
	}

	/**
	 * @param pContext
	 * @return value of PREF_CREDIT_PER_PURCHASE
	 */
	public static String getCreditsPerPurchase(Context pContext) {
		return SharedPrefsUtils.getSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CREDIT_PER_PURCHASE, "");
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_GCM_REG_ID
	 */
	public static void setGcmRegId(Context pContext, String pValue) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_GCM_REG_ID, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_GCM_REG_ID
	 */
	public static String getGcmRegId(Context pContext) {
		return SharedPrefsUtils.getSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_GCM_REG_ID, "");
	}

	/**
	 * @param pContext
	 * @return value of PREF_IS_DEVICE_REGISTERED
	 */
	public static boolean isDeviceRegistered(Context pContext) {
		return SharedPrefsUtils.getSharedPrefBoolean(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_IS_DEVICE_REGISTERED, false);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_IS_DEVICE_REGISTERED
	 */
	public static void setDeviceRegistered(Context pContext, boolean pValue) {
		SharedPrefsUtils.setSharedPrefBoolean(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_IS_DEVICE_REGISTERED, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_APP_VERSION_CODE
	 */
	public static int getAppVersionCode(Context pContext) {
		return SharedPrefsUtils.getSharedPrefInt(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_APP_VERSION_CODE, 0);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_APP_VERSION_CODE
	 */
	public static void setAppVersionCode(Context pContext, int pValue) {
		SharedPrefsUtils.setSharedPrefInt(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_APP_VERSION_CODE, pValue);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_GCM_REG_ID
	 */
	public static void setDealsImageBaseUrl(Context pContext, String pValue) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_DEALS_IMAGE_BASE_URL, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_GCM_REG_ID
	 */
	public static String getDealsImageBaseUrl(Context pContext) {
		return SharedPrefsUtils.getSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_DEALS_IMAGE_BASE_URL, "");
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_DEALS_UPDATE_INTERVAL
	 */
	public static void setDealsUpdateInterval(Context pContext, int pValue) {
		SharedPrefsUtils.setSharedPrefInt(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_DEALS_UPDATE_INTERVAL, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_DEALS_UPDATE_INTERVAL
	 */
	public static int getDealsUpdateInterval(Context pContext) {
		return SharedPrefsUtils.getSharedPrefInt(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_DEALS_UPDATE_INTERVAL, -1);
	}

	/**
	 * increment PREF_PAYMENT_MESSAGE_INDEX by 1
	 * 
	 * @param pContext
	 */
	public static void increasePaymentMessageIndex(Context pContext) {
		String[] msgArray = pContext.getResources().getStringArray(R.array.array_msg_progress_dialog);
		if (msgArray == null || msgArray.length == 0) {
			return;
		}
		int increasedIndex = 1 + getPaymentMessageIndex(pContext);
		if (increasedIndex >= msgArray.length) {
			increasedIndex = 0;
		}
		SharedPrefsUtils.setSharedPrefInt(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_PAYMENT_MESSAGE_INDEX, increasedIndex);
	}

	/**
	 * @param pContext
	 * @return value of PREF_PAYMENT_MESSAGE_INDEX
	 */
	public static int getPaymentMessageIndex(Context pContext) {
		return SharedPrefsUtils.getSharedPrefInt(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_PAYMENT_MESSAGE_INDEX, -1);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to append with value of PREF_REDEEMED_VOUCHER_CODES
	 */
	public static void appendRedeemedVoucherCodes(Context pContext, String pValue) {
		String currentStr = getRedeemedVoucherCodes(pContext);
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_REDEEMED_VOUCHER_CODES, currentStr + pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_REDEEMED_VOUCHER_CODES
	 */
	public static String getRedeemedVoucherCodes(Context pContext) {
		return SharedPrefsUtils.getSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_REDEEMED_VOUCHER_CODES, "");
	}

	/**
	 * @param pContext
	 */
	public static void clearRedeemedVoucherCodes(Context pContext) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_REDEEMED_VOUCHER_CODES, "");
	}

	/**
	 * @param pContext
	 * @return value of PREF_CAN_CLEAR_REDEEMED_CODES
	 */
	public static boolean canClearRedeemedCodes(Context pContext) {
		return SharedPrefsUtils.getSharedPrefBoolean(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CAN_CLEAR_REDEEMED_CODES, false);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_CAN_CLEAR_REDEEMED_CODES
	 */
	public static void setCanClearRedeemedCodes(Context pContext, boolean pValue) {
		SharedPrefsUtils.setSharedPrefBoolean(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_CAN_CLEAR_REDEEMED_CODES, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_SHOULD_UPDATE_PURCHASES
	 */
	public static boolean shouldUpdatePurchases(Context pContext) {
		return SharedPrefsUtils.getSharedPrefBoolean(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_SHOULD_UPDATE_PURCHASES, false);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_SHOULD_UPDATE_PURCHASES
	 */
	public static void setShouldUpdatePurchases(Context pContext, boolean pValue) {
		SharedPrefsUtils.setSharedPrefBoolean(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_SHOULD_UPDATE_PURCHASES, pValue);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_VICINITY_RADIUS
	 */
	public static void setVicinityRadius(Context pContext, float pValue) {
		SharedPrefsUtils.setSharedPrefFloat(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_VICINITY_RADIUS, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_VICINITY_RADIUS
	 */
	public static float getVicinityRadius(Context pContext) {
		return SharedPrefsUtils.getSharedPrefFloat(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_VICINITY_RADIUS, 0);
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to append with value of PREF_NOTIFIED_BY_VICINITY_IDS
	 */
	public static void appendNotifiedByVicinityId(Context pContext, String pValue) {
		String currentStr = getNotifiedByVicinityIds(pContext);
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_NOTIFIED_BY_VICINITY_IDS, currentStr + pValue);
	}

	/**
	 * @param pContext
	 */
	public static void clearNotifiedByVicinityIds(Context pContext) {
		SharedPrefsUtils.setSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_NOTIFIED_BY_VICINITY_IDS, "");
	}

	/**
	 * @param pContext
	 * @return value set for PREF_NOTIFIED_BY_VICINITY_IDS
	 */
	public static String getNotifiedByVicinityIds(Context pContext) {
		return SharedPrefsUtils.getSharedPrefString(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_NOTIFIED_BY_VICINITY_IDS, "");
	}

	/**
	 * @param pContext
	 * @param pValue
	 *            value to set for PREF_NOTIFIED_BY_VICINITY_ON
	 */
	public static void setLastNotifiedByVicinityOn(Context pContext, long pValue) {
		SharedPrefsUtils.setSharedPrefLong(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_LAST_VICINITY_NOTIFIED_ON, pValue);
	}

	/**
	 * @param pContext
	 * @return value of PREF_NOTIFIED_BY_VICINITY_ON
	 */
	public static long getLastNotifiedByVicinityOn(Context pContext) {
		return SharedPrefsUtils.getSharedPrefLong(pContext, GROUPON_GO_PREFS_FILE_NAME, PREF_LAST_VICINITY_NOTIFIED_ON, 0);
	}
}
