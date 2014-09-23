package com.groupon.go.controller;

import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.model.RegisteredMobNumResponse;
import com.groupon.go.model.RegisteredMobNumResponse.RegisterMobNumResult;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.volleyx.PostRequest;
import com.kelltontech.volleyx.StringResponseListener;
import com.kelltontech.volleyx.VolleyManager;

/**
 * @author vineet.kumar
 */
public class SharingDealsController extends GrouponGoBaseController {

	private static final String	LOG_TAG	= "SharingDealsController";

	/**
	 * @param context
	 * @param screen
	 */
	public SharingDealsController(Context context, IScreen screen) {
		super(context, screen);
	}

	@Override
	public Object getData(int pRequestType, Object pRequestData) {
		StringRequest stringRequest = null;
		Map<String, String> params = createPostRequestParams(pRequestType, pRequestData);
		if (params == null) {
			sendRequestErrorToScreen(pRequestType, pRequestData);
			return null;
		}
		StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
		switch (pRequestType) {
		case ApiConstants.UPDATE_CONTACT_LIST: {
			stringRequest = new PostRequest(ApiConstants.UPDATE_CONTACTS_URL, listener, params, createApiHeaders(pRequestType));
			break;
		}
		case ApiConstants.API_SHARE_VIA_PHONEBOOK: {
			stringRequest = new PostRequest(ApiConstants.SHARE_VIA_PHONEBOOK_URL, listener, params, createApiHeaders(pRequestType));
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
		case ApiConstants.UPDATE_CONTACT_LIST: {
			parseGetRegisteredNumResponse(serviceResponse);
			break;
		}
		case ApiConstants.API_SHARE_VIA_PHONEBOOK: {
			parseCommonJsonResponse(serviceResponse);
			break;
		}
		}
	}

	

	/**
	 * @param serviceResponse
	 */
	private void parseGetRegisteredNumResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		RegisteredMobNumResponse response = GSON.fromJson(responseStr, RegisteredMobNumResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		RegisterMobNumResult registerMobNumResult = response.getResult();
		if (registerMobNumResult != null && registerMobNumResult.getRegistered_numbers() != null) {
			serviceResponse.setResponseObject(registerMobNumResult);
			serviceResponse.setSuccess(true);
		}
	}
}
