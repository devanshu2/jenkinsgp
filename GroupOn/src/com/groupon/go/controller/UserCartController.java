package com.groupon.go.controller;

import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.model.GetUserCartResponse;
import com.groupon.go.model.GetUserCartResponse.GetUserCartResult;
import com.groupon.go.persistance.GrouponGoFiles;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.volleyx.GetRequest;
import com.kelltontech.volleyx.PostRequest;
import com.kelltontech.volleyx.StringResponseListener;
import com.kelltontech.volleyx.VolleyManager;

/**
 * @author vineet.rajpoot
 */
public class UserCartController extends GrouponGoBaseController {

	private static final String	LOG_TAG	= UserCartController.class.getSimpleName();

	/**
	 * @param context
	 * @param screen
	 */
	public UserCartController(Context context, IScreen screen) {
		super(context, screen);
	}

	@Override
	public Object getData(int pRequestType, Object pRequestData) {
		StringRequest stringRequest = null;
		switch (pRequestType) {
		case ApiConstants.GET_USER_CART_FROM_CACHE: {
			sendCachedResponseToScreen(pRequestType, pRequestData);
			break;
		}
		case ApiConstants.DELETE_DEAL_FROM_CART:
		case ApiConstants.GET_USER_CART_FROM_SERVER:
		case ApiConstants.GET_USER_ORDER_STATUS:
		case ApiConstants.APPLY_COUPON_CODE:
		case ApiConstants.REMOVE_COUPON_CODE:
		case ApiConstants.DELETE_USER_CART:
		case ApiConstants.APPLY_USER_CREDITS:
		case ApiConstants.REMOVE_USER_CREDITS: {
			String urlWithParams = createGetRequestUrl(pRequestType, pRequestData);
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new GetRequest(urlWithParams, listener, createApiHeaders(pRequestType));
			break;
		}
		case ApiConstants.ADD_DEAL_TO_CART:
		case ApiConstants.EDIT_DEAL_IN_CART: {
			Map<String, String> params = createPostRequestParams(pRequestType, pRequestData);
			if (params == null) {
				sendRequestErrorToScreen(pRequestType, pRequestData);
				return null;
			}
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			if (pRequestType == ApiConstants.EDIT_DEAL_IN_CART) {
				stringRequest = new PostRequest(ApiConstants.USER_EDIT_CART_URL, listener, params, createApiHeaders(pRequestType));
			} else {
				stringRequest = new PostRequest(ApiConstants.ADD_TO_CART_URL, listener, params, createApiHeaders(pRequestType));
			}
			break;
		}
		}
		if (stringRequest != null) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Request: " + stringRequest.toString());
			}
			VolleyManager.addToDataRequestQueue(stringRequest);
		}
		return stringRequest;
	}

	@Override
	public void parseResponse(ServiceResponse serviceResponse) {
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_USER_CART_FROM_CACHE:
		case ApiConstants.DELETE_USER_CART:
		case ApiConstants.GET_USER_ORDER_STATUS: {
			parseGetUserCartResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_USER_CART_FROM_SERVER:
		case ApiConstants.APPLY_COUPON_CODE:
		case ApiConstants.REMOVE_COUPON_CODE:
		case ApiConstants.APPLY_USER_CREDITS:
		case ApiConstants.REMOVE_USER_CREDITS:
		case ApiConstants.ADD_DEAL_TO_CART:
		case ApiConstants.DELETE_DEAL_FROM_CART:
		case ApiConstants.EDIT_DEAL_IN_CART: {
			parseGetUserCartResponse(serviceResponse);
			if (serviceResponse.isSuccess()) {
				GrouponGoFiles.saveResponseJson(getContext(), serviceResponse);
			}
			break;
		}
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseGetUserCartResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		GetUserCartResponse response = GSON.fromJson(responseStr, GetUserCartResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		GetUserCartResult getUserCartResult = response.getResult();
		if (getUserCartResult != null && getUserCartResult.getDeals() != null) {
			serviceResponse.setSuccess(true);
			if (serviceResponse.getDataType() == ApiConstants.GET_USER_ORDER_STATUS || serviceResponse.getDataType() == ApiConstants.GET_USER_CART_FROM_CACHE) {
				// Nothing to do here.
			} else {
				GrouponGoPrefs.setCartItemsCount(getContext(), getUserCartResult.getCart_count());
				GrouponGoPrefs.setMyCredits(getContext(), getUserCartResult.getCredit());
			}
			serviceResponse.setResponseObject(getUserCartResult);
		}
	}

	/**
	 * @param serviceResponse
	 */
	/*
	 * private void parseEditCartResponse(ServiceResponse serviceResponse) {
	 * String responseStr = (String) serviceResponse.getRawResponse();
	 * EditCartResponse response = GSON.fromJson(responseStr,
	 * EditCartResponse.class); if (response == null || response.getError_code()
	 * == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
	 * serviceResponse.setResponseObject(response); return; }
	 * serviceResponse.setErrorMessage(response.getMessage());
	 * 
	 * EditCartResult getUserCartResult = response.getResult(); if
	 * (getUserCartResult != null) { serviceResponse.setSuccess(true);
	 * GrouponGoPrefs.setCartItemsCount(getContext(),
	 * getUserCartResult.getCar_count());
	 * serviceResponse.setResponseObject(getUserCartResult); } }
	 */
}
