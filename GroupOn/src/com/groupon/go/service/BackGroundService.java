package com.groupon.go.service;

import org.json.JSONArray;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.groupon.go.BuildConfig;
import com.groupon.go.application.GrouponGoApp;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.DealsController;
import com.groupon.go.controller.UserCartController;
import com.groupon.go.controller.UserProfileController;
import com.groupon.go.database.MyCouponLocationsTable;
import com.groupon.go.database.MyPurchasesHelper;
import com.groupon.go.model.AllOrdersResponse.AllOrdersResult;
import com.groupon.go.model.GetProfileResponse.CategorySelected;
import com.groupon.go.model.GetProfileResponse.GetProfileResultModel;
import com.groupon.go.model.MyPurchasesResponse.MyPurchasesResult;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;

/**
 * @author sachin.gupta
 */
public class BackGroundService extends IntentService {

	private static final String	LOG_TAG	= BackGroundService.class.getSimpleName();

	/**
	 * No-arg constructor
	 */
	public BackGroundService() {
		super(BackGroundService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		int apiType = intent.getIntExtra(Constants.EXTRA_API_TYPE, 0);
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onHandleIntent() apiType: " + apiType);
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Device is out of network coverage area.");
			}
			if (apiType == ApiConstants.GET_MY_PURCHASES) {
				CouponsNotificationService.start(BackGroundService.this);
			}
			return;
		}

		IScreen fakeScreen = new IScreen() {
			@Override
			public void handleUiUpdate(ServiceResponse serviceResponse) {
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "handleUiUpdate: " + serviceResponse.isSuccess());
				}
				switch (serviceResponse.getDataType()) {
				case ApiConstants.GET_PROFILE_FROM_CACHE: {
					if (serviceResponse.getResponseObject() instanceof GetProfileResultModel) {
						GetProfileResultModel profileResult = (GetProfileResultModel) serviceResponse.getResponseObject();
						String userNameToBeUpdated = intent.getStringExtra(Constants.EXTRA_USER_NAME);
						sendUpdateProfileRequest(profileResult, userNameToBeUpdated);
					}
					break;
				}
				case ApiConstants.GET_MY_PURCHASES: {
					if (serviceResponse.getResponseObject() instanceof MyPurchasesResult) {
						MyPurchasesResult myPurchasesResult = (MyPurchasesResult) serviceResponse.getResponseObject();

						String dealsImageUrl = myPurchasesResult.getDeal_img_url();
						if (!StringUtils.isNullOrEmpty(dealsImageUrl)) {
							ProjectUtils.completeDealsImageBaseUrl(BackGroundService.this, dealsImageUrl);
						}
						if (myPurchasesResult.getRadius() > 0) {
							GrouponGoPrefs.setVicinityRadius(BackGroundService.this, myPurchasesResult.getRadius());
						}

						GrouponGoApp grouponGoApp = (GrouponGoApp) getApplication();
						MyPurchasesHelper myPurchasesHelper = grouponGoApp.getMyPurchasesHelper();
						myPurchasesHelper.saveMyPurchases(myPurchasesResult);

						if (GrouponGoPrefs.canClearRedeemedCodes(BackGroundService.this)) {
							GrouponGoPrefs.clearRedeemedVoucherCodes(BackGroundService.this);
						}
					}
					CouponsNotificationService.start(BackGroundService.this);

					Intent broadcastIntent = new Intent(Constants.ACTION_MY_PURCHASES_RESP_RCVD);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
					break;
				}
				case ApiConstants.GET_ALL_ORDERS: {
					if (serviceResponse.getResponseObject() instanceof AllOrdersResult) {
						AllOrdersResult allOrdersResult = (AllOrdersResult) serviceResponse.getResponseObject();
						MyCouponLocationsTable myCouponLocationsTable = new MyCouponLocationsTable(getApplication());
						myCouponLocationsTable.updateCoupons(allOrdersResult);
						GrouponGoPrefs.setVicinityRadius(BackGroundService.this, (float) allOrdersResult.getRadius());
						// GrouponGoPrefs.setAllOrdersResponseTimestamp(BackGroundService.this,
						// allOrdersResult.getTimestamp());
					}
					CouponsNotificationService.start(BackGroundService.this);
					break;
				}
				case ApiConstants.POST_REDEEMED_VOUCHERS: {
					if (GrouponGoPrefs.shouldUpdatePurchases(BackGroundService.this)) {
						GrouponGoPrefs.setShouldUpdatePurchases(BackGroundService.this, false);
						if (serviceResponse.isSuccess()) {
							GrouponGoPrefs.setCanClearRedeemedCodes(BackGroundService.this, true);
						}
						start(BackGroundService.this, ApiConstants.GET_MY_PURCHASES);
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

		switch (apiType) {
		case ApiConstants.GET_PROFILE_FROM_CACHE:
		case ApiConstants.GET_PROFILE_FROM_SERVER: {
			UserProfileController userProfileController = new UserProfileController(this, fakeScreen);
			userProfileController.getData(apiType, true);
			break;
		}
		case ApiConstants.GET_USER_CART_FROM_SERVER:
		case ApiConstants.GET_REMOVE_SINGLE_CART: {
			UserCartController userCartController = new UserCartController(this, fakeScreen);
			userCartController.getData(apiType, null);
			break;
		}
		case ApiConstants.GET_MY_PURCHASES:
		case ApiConstants.GET_ALL_ORDERS: {
			DealsController dealsController = new DealsController(this, fakeScreen);
			dealsController.getData(apiType, null);
			break;
		}
		case ApiConstants.POST_REDEEMED_VOUCHERS: {
			String redeemedVouchersStr = GrouponGoPrefs.getRedeemedVoucherCodes(this);
			String[] redeemedVouchersArr = redeemedVouchersStr.split(",");
			JSONArray jsonArray = new JSONArray();
			if (redeemedVouchersArr != null) {
				for (String voucherCode : redeemedVouchersArr) {
					if (!StringUtils.isNullOrEmpty(voucherCode)) {
						jsonArray.put(voucherCode);
					}
				}
			}
			if (jsonArray.length() == 0) {
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "onHandleIntent(). No offline redeemed vouchers found to be posted.");
				}
				start(this, ApiConstants.GET_MY_PURCHASES);
			} else {
				DealsController dealsController = new DealsController(this, fakeScreen);
				dealsController.getData(apiType, jsonArray.toString());
			}
			break;
		}
		}
	}

	/**
	 * @param profileResult
	 * @param userNameToBeUpdated
	 */
	private void sendUpdateProfileRequest(GetProfileResultModel profileResult, final String userNameToBeUpdated) {
		UserDetails userDetails;
		if (profileResult.getProfile() != null && !profileResult.getProfile().isEmpty()) {
			userDetails = profileResult.getProfile().get(0);
		} else {
			userDetails = new UserDetails();
		}
		userDetails.setUsername(userNameToBeUpdated);
		JSONArray jsonArray = new JSONArray();
		if (profileResult.getCategory_selected() != null) {
			for (CategorySelected categorySelected : profileResult.getCategory_selected()) {
				jsonArray.put(categorySelected.getCategory_id());
			}
		}
		if (profileResult.getCategory() != null && profileResult.getCategory().size() == jsonArray.length()) {
			jsonArray = new JSONArray();
		}
		userDetails.setPreferences(jsonArray);

		IScreen fakeScreen = new IScreen() {
			@Override
			public void handleUiUpdate(ServiceResponse serviceResponse) {
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "handleUiUpdate: " + serviceResponse.isSuccess());
				}
				switch (serviceResponse.getDataType()) {
				case ApiConstants.UPDATE_PROFILE: {
					GrouponGoPrefs.setUserName(getApplicationContext(), userNameToBeUpdated);
					break;
				}
				}
			}

			@Override
			public void onEvent(int eventId, Object eventData) {
				// nothing to do here
			}
		};
		UserProfileController userProfileController = new UserProfileController(BackGroundService.this, fakeScreen);
		userProfileController.getData(ApiConstants.UPDATE_PROFILE, userDetails);
	}

	/**
	 * @param pContext
	 * @param pRequestType
	 */
	public static void start(Context pContext, int pRequestType) {
		Intent serviceIntent = new Intent(pContext, BackGroundService.class);
		serviceIntent.putExtra(Constants.EXTRA_API_TYPE, pRequestType);
		pContext.startService(serviceIntent);
	}

	/**
	 * @param pContext
	 * @param pUserName
	 */
	public static void updateUserName(Context pContext, String pUserName) {
		Intent serviceIntent = new Intent(pContext, BackGroundService.class);
		serviceIntent.putExtra(Constants.EXTRA_API_TYPE, ApiConstants.GET_PROFILE_FROM_CACHE);
		serviceIntent.putExtra(Constants.EXTRA_USER_NAME, pUserName);
		pContext.startService(serviceIntent);
	}

	/**
	 * @param pContext
	 * @param pRequestType
	 */
	public static void postRedeemedVouchers(Context pContext, boolean pUpdatePurchasesOnSuccess) {
		GrouponGoPrefs.setShouldUpdatePurchases(pContext, pUpdatePurchasesOnSuccess);
		Intent serviceIntent = new Intent(pContext, BackGroundService.class);
		serviceIntent.putExtra(Constants.EXTRA_API_TYPE, ApiConstants.POST_REDEEMED_VOUCHERS);
		pContext.startService(serviceIntent);
	}
}