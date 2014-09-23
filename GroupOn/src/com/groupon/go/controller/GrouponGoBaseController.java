package com.groupon.go.controller;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.TimeoutError;
import com.google.gson.Gson;
import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.CityModel;
import com.groupon.go.model.CommonJsonResponse;
import com.groupon.go.model.DealsListRequest;
import com.groupon.go.model.GetApiAuthTokenResponse.ApiAuthTokenResult;
import com.groupon.go.model.PaymentDetailModel;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoFiles;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.controller.BaseController;
import com.kelltontech.network.HttpClientConnection;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.ui.activity.BaseActivity;
import com.kelltontech.utils.ApplicationUtils;
import com.kelltontech.utils.Installation;
import com.kelltontech.utils.StringUtils;

/**
 * This class will be used as a base class for all controllers
 */
public abstract class GrouponGoBaseController extends BaseController {

	private final String	LOG_TAG	= "GrouponGoBaseController";

	protected static Gson	GSON	= new Gson();

	/**
	 * @param context
	 * @param screen
	 */
	public GrouponGoBaseController(Context context, IScreen screen) {
		super(context, screen);
		if (GSON == null) {
			GSON = new Gson();
		}
	}

	/**
	 * Setting default request time-out time as 60 seconds for all requests.
	 */
	static {
		HttpClientConnection.getInstance().setDefaultRequestTimeOut(60000);
	}

	/**
	 * @param pRequestType
	 * @return
	 */
	protected Map<String, String> createApiHeaders(int pRequestType) {
		ApiAuthTokenResult apiAuthToken = GrouponGoPrefs.getApiAuthTokenResult(getContext());
		HashMap<String, String> headers = new HashMap<String, String>();
		String apiPlatformKey = getContext().getString(R.string.api_platform_key);
		String apiPlatformSalt = getContext().getString(R.string.api_platform_salt);
		String apiKeyValue = apiPlatformKey + "|" + apiAuthToken.getAuth() + "|" + apiPlatformSalt;
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "Header api_key: " + apiKeyValue);
		}
		headers.put(ApiConstants.HEADER_API_KEY, apiKeyValue);
		return headers;
	}

	/**
	 * @param pRequestType
	 * @param pRequestData
	 * @return
	 */
	protected String createGetRequestUrl(int pRequestType, Object pRequestData) {
		Builder builder = null;
		switch (pRequestType) {
		case ApiConstants.GET_COUNTRY_LIST: {
			builder = Uri.parse(ApiConstants.COUNTRY_LIST_URL).buildUpon();
			break;
		}
		case ApiConstants.USER_REGISTRATION: {
			builder = Uri.parse(ApiConstants.USER_REGISTERATION_URL).buildUpon();
			appendRegistrationParams(builder, pRequestData);
			break;
		}
		case ApiConstants.GET_API_AUTH_TOKEN: {
			builder = Uri.parse(ApiConstants.GET_API_AUTH_TOKEN_URL).buildUpon();
			break;
		}
		case ApiConstants.CODE_VERIFICATION: {
			builder = Uri.parse(ApiConstants.CODE_VERIFICATION_URL).buildUpon();
			appendRegistrationParams(builder, pRequestData);
			break;
		}
		case ApiConstants.RESEND_VERIFICATION_CODE: {
			builder = Uri.parse(ApiConstants.RESEND_VERIFICATION_CODE_URL).buildUpon();
			appendRegistrationParams(builder, pRequestData);
			break;
		}
		case ApiConstants.GET_PROFILE_FROM_SERVER: {
			builder = Uri.parse(ApiConstants.GET_PROFILE_URL).buildUpon();
			if (pRequestData instanceof Boolean) {
				builder.appendQueryParameter(ApiConstants.PARAM_CAT_LIST_NEEDED, "" + (Boolean) pRequestData);
			}
			break;
		}
		case ApiConstants.GET_CITY_LIST_FROM_SERVER: {
			builder = Uri.parse(ApiConstants.CITY_LISTING_URL).buildUpon();
			break;
		}
		case ApiConstants.GET_DEALS_LIST_FROM_SERVER:
		case ApiConstants.GET_SEARCH_RESULTS: {
			if (pRequestType == ApiConstants.GET_SEARCH_RESULTS) {
				builder = Uri.parse(ApiConstants.SEARCH_RESULTS_URL).buildUpon();
			} else {
				builder = Uri.parse(ApiConstants.DEAL_LISTING_URL).buildUpon();
			}
			appendSavedLocation(builder);
			DealsListRequest dealsListRequest = null;
			if (pRequestData instanceof DealsListRequest) {
				dealsListRequest = (DealsListRequest) pRequestData;
			}
			if (dealsListRequest == null) {
				break;
			}
			if (dealsListRequest.getDealType() == ApiConstants.VALUE_DEAL_TYPE_NEAR_BY) {
				GrouponGoPrefs.saveNearByDealsLocation(getContext());
			}
			if (dealsListRequest.getDealType() != 0) {
				builder.appendQueryParameter(ApiConstants.PARAM_DEAL_TYPE, "" + dealsListRequest.getDealType());
			}
			if (dealsListRequest.getPageNo() < 1) {
				builder.appendQueryParameter(ApiConstants.PARAM_PAGE_NO, "1");
			} else {
				builder.appendQueryParameter(ApiConstants.PARAM_PAGE_NO, "" + dealsListRequest.getPageNo());
			}
			if (dealsListRequest.getPageCount() != 0) {
				builder.appendQueryParameter(ApiConstants.PARAM_PAGE_COUNT, "" + dealsListRequest.getPageCount());
			}
			if (dealsListRequest.getCategoryId() != 0) {
				builder.appendQueryParameter(ApiConstants.PARAM_CATEGORY_ID, "" + dealsListRequest.getCategoryId());
			}
			if (!StringUtils.isNullOrEmpty(dealsListRequest.getSearchText())) {
				builder.appendQueryParameter(ApiConstants.PARAM_TEXT, dealsListRequest.getSearchText());
			}
			break;
		}
		case ApiConstants.GET_DEALS_DETAIL: {
			builder = Uri.parse(ApiConstants.DEAL_DETAIL_URL).buildUpon();
			appendSavedLocation(builder);
			builder.appendQueryParameter(ApiConstants.PARAM_DEAL_ID, (String) pRequestData);
			break;
		}
		case ApiConstants.GET_COUPON_DETAILS: {
			builder = Uri.parse(ApiConstants.USER_COUPON_DETAIL_URL).buildUpon();
			appendSavedLocation(builder);
			Bundle bundle = (Bundle) pRequestData;
			int orderId = bundle.getInt(ApiConstants.PARAM_ORDER_ID);
			int dealId = bundle.getInt(Constants.EXTRA_DEAL_ID);
			builder.appendQueryParameter(ApiConstants.PARAM_DEAL_ID, "" + dealId);
			builder.appendQueryParameter(ApiConstants.PARAM_ORDER_ID, "" + orderId);
			break;
		}
		case ApiConstants.GET_SHARING_DEALS_DETAIL: {
			builder = Uri.parse(ApiConstants.SHARING_DEAL_DETAIL_URL).buildUpon();
			appendSavedLocation(builder);
			builder.appendQueryParameter(ApiConstants.PARAM_DEAL_ID, (String) pRequestData);
			break;
		}
		case ApiConstants.GET_SEARCH_SUGGESTIONS: {
			builder = Uri.parse(ApiConstants.SEARCH_SUGGESTIONS_URL).buildUpon();
			appendSavedLocation(builder);
			builder.appendQueryParameter(ApiConstants.PARAM_TEXT, (String) pRequestData);
			break;
		}
		case ApiConstants.GET_CREATE_ORDER: {
			builder = Uri.parse(ApiConstants.CREATE_ORDER_URL).buildUpon();
			appendMobileNumber(builder);
			if (pRequestData instanceof Bundle) {
				Bundle bundle = (Bundle) pRequestData;
				String payMode = bundle.getString(ApiConstants.PARAM_PG);
				builder.appendQueryParameter(ApiConstants.PARAM_IS_SINGLE, "" + 0);
				builder.appendQueryParameter(ApiConstants.PARAM_PG, payMode);
				break;
			}
		}
		case ApiConstants.GET_CREATE_ORDER_FRM_CARD: {
			builder = Uri.parse(ApiConstants.CREATE_ORDER_FRM_CARD_URL).buildUpon();
			appendMobileNumber(builder);
			if (pRequestData instanceof Bundle) {
				Bundle bundle = (Bundle) pRequestData;
				String payMode = bundle.getString(ApiConstants.PARAM_PG);
				String cardToken = bundle.getString(ApiConstants.PARAM_CARD_TOKEN);
				String cvv = bundle.getString(ApiConstants.PARAM_CVV);
				builder.appendQueryParameter(ApiConstants.PARAM_PG, payMode);
				builder.appendQueryParameter(ApiConstants.PARAM_CARD_TOKEN, cardToken);
				builder.appendQueryParameter(ApiConstants.PARAM_CVV, cvv);
				builder.appendQueryParameter(ApiConstants.PARAM_IS_SINGLE, "" + 0);
			}
			break;
		}
		case ApiConstants.GET_PAYMENT_STATUS: {
			builder = Uri.parse(ApiConstants.GET_PAYMENT_STATUS_URL).buildUpon();
			if (pRequestData instanceof PaymentDetailModel) {
				PaymentDetailModel paymentDetailModel = (PaymentDetailModel) pRequestData;
				builder.appendQueryParameter(ApiConstants.PARAM_TXN_ID, paymentDetailModel.getTxnid());
				builder.appendQueryParameter(ApiConstants.PARAM_SESSION_EXPIRED, paymentDetailModel.isSessionExpired() ? "1" : "0");
			}
			break;
		}
		case ApiConstants.GET_PAYU_CONFIGURATION: {
			builder = Uri.parse(ApiConstants.PAYU_CONFIGURATION_URL).buildUpon();
			if (pRequestData != null && pRequestData instanceof String) {
				builder.appendQueryParameter(ApiConstants.PARAM_COMMAND_TYPE, (String) pRequestData);
			}
			break;
		}
		case ApiConstants.GET_SAVED_CARDS: {
			builder = Uri.parse(ApiConstants.GET_SAVES_CARDS_URL).buildUpon();
			break;
		}
		case ApiConstants.GET_USER_CART_FROM_SERVER: {
			builder = Uri.parse(ApiConstants.USER_CART_URL).buildUpon();
			break;
		}
		case ApiConstants.GET_USER_ORDER_STATUS: {
			builder = Uri.parse(ApiConstants.USER_ORDER_STATUS_URL).buildUpon();
			builder.appendQueryParameter(ApiConstants.PARAM_TXN_ID, (String) pRequestData);
			break;
		}
		case ApiConstants.GET_COUPONS: {
			builder = Uri.parse(ApiConstants.USER_COUPONS_URL).buildUpon();
			DealsListRequest dealsListRequest = null;
			if (pRequestData instanceof DealsListRequest) {
				dealsListRequest = (DealsListRequest) pRequestData;
			}
			appendSavedLocation(builder);
			if (dealsListRequest == null) {
				break;
			}
			builder.appendQueryParameter(ApiConstants.PARAM_PAGE_NO, "" + dealsListRequest.getPageNo());
			builder.appendQueryParameter(ApiConstants.PARAM_COUPON_TYPE, "" + dealsListRequest.getCouponType());
			break;
		}
		case ApiConstants.DELETE_DEAL_FROM_CART: {
			builder = Uri.parse(ApiConstants.DELETE_CART_URL).buildUpon();
			builder.appendQueryParameter(ApiConstants.PARAM_DEAL_ID, (String) pRequestData);
			break;
		}
		case ApiConstants.GET_ALL_BANK: {
			builder = Uri.parse(ApiConstants.GET_ALL_BANK_URL).buildUpon();
			break;
		}
		case ApiConstants.APPLY_COUPON_CODE: {
			builder = Uri.parse(ApiConstants.GET_APPLY_COUPON_CODE_URL).buildUpon();
			if (pRequestData instanceof String) {
				builder.appendQueryParameter(ApiConstants.PARAM_COUPON_CODE, (String) pRequestData);
				builder.appendQueryParameter(ApiConstants.PARAM_IS_SINGLE, "" + 0);
			}
			break;
		}
		case ApiConstants.REMOVE_COUPON_CODE: {
			builder = Uri.parse(ApiConstants.GET_REMOVE_COUPON_CODE_URL).buildUpon();
			builder.appendQueryParameter(ApiConstants.PARAM_IS_SINGLE, "" + 0);
			break;
		}
		case ApiConstants.DELETE_USER_CART: {
			builder = Uri.parse(ApiConstants.GET_REMOVE_CART_URL).buildUpon();
			break;
		}
		case ApiConstants.GET_CHANGE_NUMBER: {
			builder = Uri.parse(ApiConstants.GET_CHANGE_MOBILE_NUMBER_URL).buildUpon();
			if (pRequestData instanceof Bundle) {
				Bundle bundle = (Bundle) pRequestData;
				builder.appendQueryParameter(ApiConstants.PARAM_MOBILE_NUMBER, bundle.getString(ApiConstants.PARAM_MOBILE_NUMBER));
				builder.appendQueryParameter(ApiConstants.PARAM_NEW_NUMBER, bundle.getString(ApiConstants.PARAM_NEW_NUMBER));
			}
			break;
		}
		case ApiConstants.GET_ALL_ORDERS: {
			builder = Uri.parse(ApiConstants.OFFER_SUMMARY_LIST_URL).buildUpon();
			// builder.appendQueryParameter(ApiConstants.PARAM_DATE,
			// GrouponGoPrefs.getAllOrdersResponseTimestamp(getContext()));
			break;
		}
		case ApiConstants.GET_VERIFY_CHANGE_NUMBER: {
			builder = Uri.parse(ApiConstants.GET_VERIFY_CHANGE_MOBILE_NUMBER_URL).buildUpon();
			builder.appendQueryParameter(ApiConstants.PARAM_VERIFICATION_CODE, (String) pRequestData);
			break;
		}
		case ApiConstants.GET_IMAGE_SIGNATURE: {
			builder = Uri.parse(ApiConstants.GET_IMAGE_SIGNATURE_URL).buildUpon();
			break;
		}
		case ApiConstants.REMOVE_USER_IMAGE: {
			builder = Uri.parse(ApiConstants.REMOVE_USER_IMAGE_URL).buildUpon();
			break;
		}
		case ApiConstants.APPLY_USER_CREDITS: {
			builder = Uri.parse(ApiConstants.REDEEM_USER_CREDIT_URL).buildUpon();
			if (pRequestData instanceof String) {
				builder.appendQueryParameter(ApiConstants.PARAM_IS_SINGLE, "" + 0);
				builder.appendQueryParameter(ApiConstants.PARAM_CREDIT_USED, (String) pRequestData);
			}
			break;
		}
		case ApiConstants.REMOVE_USER_CREDITS: {
			builder = Uri.parse(ApiConstants.REMOVE_USER_CREDIT_URL).buildUpon();
			builder.appendQueryParameter(ApiConstants.PARAM_IS_SINGLE, "" + 0);
			break;
		}
		case ApiConstants.GET_HTML_PAGE_FROM_SERVER: {
			builder = Uri.parse(ApiConstants.GET_STATIC_WEB_PAGE_URL).buildUpon();
			if (pRequestData instanceof Integer) {
				builder.appendQueryParameter(ApiConstants.PARAM_PAGE_ID, "" + pRequestData);
			}
			break;
		}
		case ApiConstants.GET_SUMMARY_BY_EMAIL: {
			builder = Uri.parse(ApiConstants.GET_SUMMARY_BY_EMAIL_URL).buildUpon();
			if (pRequestData instanceof Bundle) {
				Bundle bundle = (Bundle) pRequestData;
				builder.appendQueryParameter(ApiConstants.PARAM_TXN_ID, bundle.getString(Constants.EXTRA_TRANSX_ID));
				builder.appendQueryParameter(ApiConstants.PARAM_EMAIL_ID, bundle.getString(Constants.EXTRA_EMAIL_ID));
			}
			break;
		}
		case ApiConstants.GET_MY_PURCHASES: {
			builder = Uri.parse(ApiConstants.GET_MY_PURCHASES_URL).buildUpon();
			break;
		}
		}

		UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(getContext());
		if (userDetails.getUser_id() != 0) {
			builder.appendQueryParameter(ApiConstants.PARAM_USER_ID, "" + userDetails.getUser_id());
		}
		builder.appendQueryParameter(ApiConstants.PARAM_OS, "" + ApiConstants.VALUE_OS_ANDROID);
		builder.appendQueryParameter(ApiConstants.PARAM_VERSION, ApiConstants.VALUE_API_VERSION);
		return builder.toString();
	}

	/**
	 * @param builder
	 * @param pRequestData
	 */
	private void appendRegistrationParams(Builder builder, Object pRequestData) {
		if (pRequestData instanceof Bundle) {
			Bundle data = (Bundle) pRequestData;
			builder.appendQueryParameter(ApiConstants.PARAM_COUNTRY_CODE, data.getString(ApiConstants.PARAM_COUNTRY_CODE));
			builder.appendQueryParameter(ApiConstants.PARAM_MOBILE_NUMBER, data.getString(ApiConstants.PARAM_MOBILE_NUMBER));
			int isNumChange = data.getInt(ApiConstants.PARAM_IS_NUMBER_CHANGE);
			if (isNumChange == 1) {
				builder.appendQueryParameter(ApiConstants.PARAM_IS_NUMBER_CHANGE, "" + isNumChange);
				appendMobileNumber(builder);
			}
			if (data.getString(ApiConstants.PARAM_VERIFICATION_CODE) != null) {
				builder.appendQueryParameter(ApiConstants.PARAM_VERIFICATION_CODE, data.getString(ApiConstants.PARAM_VERIFICATION_CODE));
			}
		}
		builder.appendQueryParameter(ApiConstants.PARAM_DEVICE_ID, Installation.id(getContext()));
	}

	/**
	 * @param builder
	 */
	private void appendMobileNumber(Builder builder) {
		UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(getContext());
		if (!StringUtils.isNullOrEmpty(userDetails.getUserPhone())) {
			builder.appendQueryParameter(ApiConstants.PARAM_MOBILE_NUMBER, "" + userDetails.getUserPhone());
		}
	}

	/**
	 * @param builder
	 */
	private void appendSavedLocation(Builder builder) {
		CityModel cityToShowDeals = GrouponGoPrefs.getCityToShowDeals(getContext());
		if (cityToShowDeals == null) {
			return;
		}
		if (cityToShowDeals.getCity_id() != 0) {
			builder.appendQueryParameter(ApiConstants.PARAM_CITY_ID, "" + cityToShowDeals.getCity_id());
		}
		/**
		 * if device lat-long are available, these will be sent in API requests
		 * instead of lat-long of user selected city, as distance is to be
		 * calculated at API end and it should be calculated from device, even
		 * if deals are being shown of any other city
		 */
		Location location = GrouponGoPrefs.getDeviceLocation(getContext());
		if (location != null) {
			builder.appendQueryParameter(ApiConstants.PARAM_LAT, "" + (float) location.getLatitude());
			builder.appendQueryParameter(ApiConstants.PARAM_LONG, "" + (float) location.getLongitude());
		} else {
			builder.appendQueryParameter(ApiConstants.PARAM_LAT, "" + cityToShowDeals.getCity_lat());
			builder.appendQueryParameter(ApiConstants.PARAM_LONG, "" + cityToShowDeals.getCity_long());
		}
	}

	/**
	 * @param pRequestType
	 * @param pRequestData
	 * @return
	 */
	protected HashMap<String, String> createPostRequestParams(int pRequestType, Object pRequestData) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(ApiConstants.PARAM_OS, "" + ApiConstants.VALUE_OS_ANDROID);
		params.put(ApiConstants.PARAM_VERSION, ApiConstants.VALUE_API_VERSION);

		UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(getContext());
		if (userDetails.getUser_id() != 0) {
			params.put(ApiConstants.PARAM_USER_ID, "" + userDetails.getUser_id());
		}
		switch (pRequestType) {
		case ApiConstants.API_DEVICE_REGISTRATION: {
			params.put(ApiConstants.PARAM_DEVICE_UNIQUE_ID, Installation.id(getContext()));
			params.put(ApiConstants.PARAM_PUSH_TOKEN, (String) pRequestData);
			params.put(ApiConstants.PARAM_MOBILE_NUMBER, "" + userDetails.getUserPhone());
			params.put(ApiConstants.PARAM_DEVICE_MODEL, Build.BRAND);
			params.put(ApiConstants.PARAM_APP_VERSION, ApplicationUtils.getAppVersionName(getContext()));
			params.put(ApiConstants.PARAM_OS_VERSION, "" + Build.VERSION.SDK_INT);
			break;
		}
		case ApiConstants.UPDATE_PROFILE: {
			if (pRequestData instanceof UserDetails) {
				UserDetails userDetailsToBeSaved = (UserDetails) pRequestData;
				String userName = userDetailsToBeSaved.getUsername();
				if (userName == null) {
					userName = "";
				}
				params.put(ApiConstants.PARAM_USER_NAME, userName);
				String emailId = userDetailsToBeSaved.getEmailId();
				if (emailId == null) {
					emailId = "";
				}
				params.put(ApiConstants.PARAM_EMAIL_ID, emailId);
				params.put(ApiConstants.PARAM_IMAGE_URL, "");
				params.put(ApiConstants.PARAM_PREFERENCES, userDetailsToBeSaved.getPreferences().toString());
			} else {
				return null;
			}
			break;
		}
		case ApiConstants.UPDATE_CONTACT_LIST: {
			if (pRequestData instanceof JSONArray) {
				params.put(ApiConstants.PARAM_MOBILE_NUMBERS, ((JSONArray) pRequestData).toString());
			} else {
				return null;
			}
			break;
		}
		case ApiConstants.API_SHARE_VIA_PHONEBOOK: {
			Bundle bundle = null;
			if (pRequestData instanceof Bundle) {
				bundle = (Bundle) pRequestData;
			} else {
				return null;
			}
			params.put(ApiConstants.PARAM_GO_MOBILE_NUMBERS, bundle.getString(Constants.EXTRA_GO_CONTACTS_JSON));
			params.put(ApiConstants.PARAM_NON_GO_MOBILE_NUMBERS, bundle.getString(Constants.EXTRA_NON_GO_CONTACTS_JSON));
			int messageType = bundle.getInt(Constants.EXTRA_MESSAGE_TYPE);
			params.put(ApiConstants.PARAM_MESSAGE_TYPE, "" + messageType);
			switch (messageType) {
			case ApiConstants.VALUE_MESSAGE_TYPE_SHARED_DEAL:
			case ApiConstants.VALUE_MESSAGE_TYPE_PURCHASED_DEAL:
			case ApiConstants.VALUE_MESSAGE_TYPE_REDEEMED_DEAL: {
				params.put(ApiConstants.PARAM_DEAL_ID, "" + bundle.getInt(Constants.EXTRA_DEAL_ID));
				break;
			}
			case ApiConstants.VALUE_MESSAGE_TYPE_UNIQUE_CODE:
			case ApiConstants.VALUE_MESSAGE_TYPE_SHARE_APP: {
				params.put(ApiConstants.PARAM_COUPON_CODE, userDetails.getCoupon_code());
				params.put(ApiConstants.PARAM_COUPON_DISC_TEXT_GO_USER, ProjectUtils.getCouponDiscountText(getContext(), false));
				params.put(ApiConstants.PARAM_COUPON_DISC_TEXT_NON_GO_USER, ProjectUtils.getCouponDiscountText(getContext(), true));
				params.put(ApiConstants.PARAM_DEAL_ID, "0");
				break;
			}
			}
			break;
		}
		case ApiConstants.ADD_DEAL_TO_CART: {
			if (pRequestData instanceof Bundle) {
				Bundle bundle = (Bundle) pRequestData;
				String jsonString = bundle.getString(Constants.EXTRA_OFFERS_JSON_STRING);
				params.put(ApiConstants.PARAM_OFFER_ARRAY, jsonString);
				params.put(ApiConstants.PARAM_DEAL_ID, "" + bundle.getInt(Constants.EXTRA_DEAL_ID));
				boolean isForPayment = bundle.getBoolean(Constants.EXTRA_IS_ADD_CART_FOR_PAYMENT);
				if (isForPayment) {
					params.put(ApiConstants.PARAM_SINGLE, "1");
				}
			} else {
				return null;
			}
			break;
		}
		case ApiConstants.EDIT_DEAL_IN_CART: {
			if (pRequestData instanceof Bundle) {
				Bundle bundle = (Bundle) pRequestData;
				String jsonString = bundle.getString(Constants.EXTRA_OFFERS_JSON_STRING);
				params.put(ApiConstants.PARAM_OFFER_ARRAY, jsonString);
				params.put(ApiConstants.PARAM_DEAL_ID, "" + bundle.getInt(Constants.EXTRA_DEAL_ID));
			} else {
				return null;
			}
			break;
		}
		case ApiConstants.GET_CREATE_ORDER_SINGLE: {
			if (pRequestData instanceof Bundle) {
				Bundle bundle = (Bundle) pRequestData;
				String jsonString = bundle.getString(Constants.EXTRA_OFFERS_JSON_STRING);
				String payMode = bundle.getString(ApiConstants.PARAM_PG);
				params.put(ApiConstants.PARAM_CART_ID, jsonString);
				params.put(ApiConstants.PARAM_PG, payMode);
				params.put(ApiConstants.PARAM_MOBILE_NUMBER, userDetails.getUserPhone());
			} else {
				return null;
			}
			break;
		}
		case ApiConstants.GET_CREATE_ORDER_SINGLE_FRM_CARD: {
			if (pRequestData instanceof Bundle) {
				Bundle bundle = (Bundle) pRequestData;
				String jsonString = bundle.getString(Constants.EXTRA_OFFERS_JSON_STRING);
				String payMode = bundle.getString(ApiConstants.PARAM_PG);
				String cardToken = bundle.getString(ApiConstants.PARAM_CARD_TOKEN);
				String cvv = bundle.getString(ApiConstants.PARAM_CVV);
				params.put(ApiConstants.PARAM_CART_ID, jsonString);
				params.put(ApiConstants.PARAM_PG, payMode);
				params.put(ApiConstants.PARAM_CARD_TOKEN, cardToken);
				params.put(ApiConstants.PARAM_CVV, cvv);
				params.put(ApiConstants.PARAM_MOBILE_NUMBER, userDetails.getUserPhone());
			} else {
				return null;
			}
		}
		case ApiConstants.GET_REDEEM_VOUCHER: {
			if (pRequestData instanceof Bundle) {
				Bundle bundle = (Bundle) pRequestData;
				String jsonString = bundle.getString(Constants.EXTRA_OFFERS_JSON_STRING);
				params.put(ApiConstants.PARAM_OFFER_ARRAY, jsonString);
				params.put(ApiConstants.PARAM_DEAL_ID, "" + bundle.getInt(Constants.EXTRA_DEAL_ID));
				params.put(ApiConstants.PARAM_ORDER_ID, "" + bundle.getInt(Constants.EXTRA_ORDER_ID));
			} else {
				return null;
			}
			break;
		}
		case ApiConstants.PURCHASE_WITHOUT_PAYMENT: {
			if (pRequestData instanceof String) {
				params.put(ApiConstants.PARAM_TXN_ID, (String) pRequestData);
				params.put(ApiConstants.PARAM_STATUS, "" + ApiConstants.VALUE_STATUS);
			} else {
				return null;
			}
			break;
		}
		case ApiConstants.POST_REDEEMED_VOUCHERS: {
			params.put(ApiConstants.PARAM_VOUCHERS, (String) pRequestData);
			break;
		}
		}
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "Post Params: " + params);
		}
		return params;
	}

	/**
	 * This method is common for all web-services
	 */
	public final void handleResponse(ServiceResponse serviceResponse) {
		if (serviceResponse.getException() != null) {
			sendNetworkErrorToScreen(serviceResponse);
			return;
		}
		String responseStr = null;
		if (serviceResponse.getRawResponse() instanceof String) {
			responseStr = (String) serviceResponse.getRawResponse();
		}
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "handleResponse(" + serviceResponse.getDataType() + "): " + responseStr);
		}
		try {
			serviceResponse.setSuccess(false);
			parseResponse(serviceResponse);
		} catch (Exception e) {
			Log.e(LOG_TAG, "handleResponse() " + e.getMessage());
			serviceResponse.setErrorMessage(getContext().getString(R.string.error_invalid_response_from_server));
		}
		CommonJsonResponse commonJsonResponse = null;
		if (serviceResponse.getResponseObject() instanceof CommonJsonResponse) {
			commonJsonResponse = (CommonJsonResponse) serviceResponse.getResponseObject();
			if (commonJsonResponse.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
				resendCurrentRequestWithFreshToken(serviceResponse);
				return;
			}
		}
		sendResponseToScreen(serviceResponse);
	}

	/**
	 * @param response
	 */
	private void resendCurrentRequestWithFreshToken(ServiceResponse serviceResponse) {
		final int pendingRequestType = serviceResponse.getDataType();
		final Object pendingRequestData = serviceResponse.getRequestData();

		IScreen fakeScreen = new IScreen() {
			@Override
			public void handleUiUpdate(ServiceResponse serviceResponse) {
				if (serviceResponse.isSuccess()) {
					getData(pendingRequestType, pendingRequestData);
				} else {
					sendNetworkErrorToScreen(serviceResponse);
				}
			}

			@Override
			public void onEvent(int eventId, Object eventData) {
				// nothing to do here
			}
		};

		RegistrationController controller = new RegistrationController(getContext(), fakeScreen);
		controller.getData(ApiConstants.GET_API_AUTH_TOKEN, null);
	}

	/**
	 * @param serviceResponse
	 */
	private void sendNetworkErrorToScreen(ServiceResponse serviceResponse) {
		if (getContext() instanceof BaseActivity) {
			((BaseActivity) getContext()).removeProgressDialog();
		}
		Exception pException = serviceResponse.getException();
		if (pException instanceof SocketException || pException instanceof TimeoutError) {
			serviceResponse.setErrorMessage(getContext().getString(R.string.error_request_timed_out));
		} else if (pException instanceof IOException || pException instanceof UnknownHostException) {
			serviceResponse.setErrorMessage(getContext().getString(R.string.error_no_connection_with_server));
		} else {
			serviceResponse.setErrorMessage(getContext().getString(R.string.error_generic_message));
		}
		sendResponseToScreen(serviceResponse);
	}

	/**
	 * @param serviceResponse
	 */
	protected void parseCommonJsonResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		CommonJsonResponse commonJsonResponse = GSON.fromJson(responseStr, CommonJsonResponse.class);
		if (commonJsonResponse == null || commonJsonResponse.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(commonJsonResponse);
			return;
		}
		serviceResponse.setSuccess(commonJsonResponse.is_success());
		serviceResponse.setErrorMessage(commonJsonResponse.getMessage());
		serviceResponse.setResponseObject(commonJsonResponse);
	}

	/**
	 * @param requestType
	 */
	protected void sendCachedResponseToScreen(final int pDataType, final Object pRequestData) {
		new Thread() {
			@Override
			public void run() {
				String cachedData = GrouponGoFiles.getResponseJson(getContext(), pDataType, pRequestData);
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "sendCachedResponseToScreen() Cached Data: " + cachedData);
				}
				ServiceResponse cachedResponse = new ServiceResponse();
				cachedResponse.setDataType(pDataType);
				cachedResponse.setRequestData(pRequestData);
				if (StringUtils.isNullOrEmpty(cachedData)) {
					cachedResponse.setSuccess(false);
				} else {
					cachedResponse.setRawResponse(cachedData);
					cachedResponse.setSuccess(true);
					parseResponse(cachedResponse);
				}
				sendResponseToScreen(cachedResponse);
			}
		}.start();
	}
}