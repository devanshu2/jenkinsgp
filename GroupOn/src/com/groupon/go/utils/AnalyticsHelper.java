package com.groupon.go.utils;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.provider.Settings.Secure;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.HitBuilders.ItemBuilder;
import com.google.android.gms.analytics.HitBuilders.TransactionBuilder;
import com.google.android.gms.analytics.Tracker;
import com.groupon.go.application.GrouponGoApp;
import com.groupon.go.model.GetUserCartResponse.GetUserCartResult;
import com.groupon.go.model.UserCartModel;
import com.groupon.go.model.UserCartModel.CartOfferModel;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.kelltontech.utils.StringUtils;
import com.mobileapptracker.MobileAppTracker;

/**
 * @author sachin.gupta
 */
public class AnalyticsHelper {

	/**
	 * @param activity
	 */
	public static void onActivityCreate(Activity activity) {
		getGoogleAnalyticsTracker(activity);
		MobileAppTracker mobileAppTracker = getMobileAppTracker(activity);
		if (GrouponGoPrefs.canLaunchHome(activity)) {
			mobileAppTracker.setExistingUser(true);
		}
		setGoogleAdvertisingId(mobileAppTracker, activity);
	}

	/**
	 * set Google Play Advertising ID <br/>
	 * REQUIRED for attribution of Android apps distributed via Google Play
	 * 
	 * @see sample code at
	 *      http://developer.android.com/google/play-services/id.html
	 * 
	 * @param mobileAppTracker
	 * @param activity
	 */
	private static void setGoogleAdvertisingId(final MobileAppTracker mobileAppTracker, final Activity activity) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(activity.getApplicationContext());
					mobileAppTracker.setGoogleAdvertisingId(adInfo.getId(), adInfo.isLimitAdTrackingEnabled());
				} catch (Exception e) {
					try {
						mobileAppTracker.setAndroidId(Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID));
					} catch (Exception e2) {
						// ignore
					}
				}
			}
		}).start();
	}

	/**
	 * @param activity
	 */
	public static void onActivityStart(Activity activity) {
		GoogleAnalytics.getInstance(activity).reportActivityStart(activity);
	}

	/**
	 * @param activity
	 */
	public static void onActivityResume(Activity activity) {
		MobileAppTracker mobileAppTracker = getMobileAppTracker(activity);
		// Get source of open for app re-engagement
		mobileAppTracker.setReferralSources(activity);
		// MAT will not function unless the measureSession call is included
		mobileAppTracker.measureSession();
	}

	/**
	 * @param activity
	 */
	public static void onActivityStop(Activity activity) {
		GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
	}

	/**
	 * @param pActivity
	 * @param pTxnId
	 * @param pUserCart
	 */
	public static void onTransaction(Activity pActivity, String pTxnId, GetUserCartResult pUserCart) {
		Tracker appTracker = getGoogleAnalyticsTracker(pActivity);

		TransactionBuilder transactionBuilder = new HitBuilders.TransactionBuilder();
		transactionBuilder.setTransactionId(pTxnId);
		transactionBuilder.setRevenue(pUserCart.getTotal());
		transactionBuilder.setCurrencyCode("INR");
		// transactionBuilder.setShipping(0);
		// transactionBuilder.setTax(0);

		ArrayList<UserCartModel> cartitemList = pUserCart.getDeals();
		if (cartitemList == null || cartitemList.isEmpty()) {
			appTracker.send(transactionBuilder.build());
			return;
		}
		ArrayList<CartOfferModel> offerList;
		String affilationStr = "";
		for (UserCartModel userCartModel : cartitemList) {
			offerList = userCartModel.getOffer_list();
			if (offerList == null || offerList.isEmpty()) {
				continue;
			}
			String merchantName = userCartModel.getMerchant_name();
			if (!StringUtils.isNullOrEmpty(merchantName) && !affilationStr.contains(merchantName)) {
				affilationStr += merchantName + ",";
			}
			for (CartOfferModel cartOfferModel : offerList) {
				ItemBuilder itemBuilder = new HitBuilders.ItemBuilder();
				itemBuilder.setTransactionId(pTxnId);
				itemBuilder.setName("Offer " + cartOfferModel.getOffer_weight());
				itemBuilder.setSku("Offer (id:" + cartOfferModel.getOffer_id() + ")");
				itemBuilder.setCategory(userCartModel.getCat_name());
				itemBuilder.setPrice(cartOfferModel.getOffer_price());
				itemBuilder.setQuantity(cartOfferModel.getOffer_count());
				itemBuilder.setCurrencyCode("INR");
				appTracker.send(itemBuilder.build());
			}
		}

		transactionBuilder.setAffiliation(affilationStr);
		appTracker.send(transactionBuilder.build());
	}

	/**
	 * @param activity
	 * @return
	 */
	private static Tracker getGoogleAnalyticsTracker(Activity activity) {
		Application application = activity.getApplication();
		if (application instanceof GrouponGoApp) {
			GrouponGoApp grouponGoApp = (GrouponGoApp) application;
			return grouponGoApp.getGoogleAnalyticsTracker();
		}
		return null;
	}

	/**
	 * 
	 * @param activity
	 */
	private static MobileAppTracker getMobileAppTracker(Activity activity) {
		Application application = activity.getApplication();
		if (application instanceof GrouponGoApp) {
			GrouponGoApp grouponGoApp = (GrouponGoApp) application;
			return grouponGoApp.getMobileAppTracker();
		}
		return null;
	}
}