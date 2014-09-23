package com.groupon.go.controller;

import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.model.CodeVerificationResponse;
import com.groupon.go.model.CodeVerificationResponse.CodeVerificationResultModel;
import com.groupon.go.model.CountryListResponse;
import com.groupon.go.model.CountryListResponse.CountryListResultModel;
import com.groupon.go.model.GetApiAuthTokenResponse;
import com.groupon.go.model.GetApiAuthTokenResponse.ApiAuthTokenResult;
import com.groupon.go.model.GetStaticWebPageResponse;
import com.groupon.go.persistance.GrouponGoFiles;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.volleyx.GetRequest;
import com.kelltontech.volleyx.PostRequest;
import com.kelltontech.volleyx.StringResponseListener;
import com.kelltontech.volleyx.VolleyManager;

/**
 * This controller is for User related API requests. <br/>
 * 1. registration<br/>
 * 2. login<br/>
 * 3. others
 * 
 * @author sachin.gupta
 */
public class RegistrationController extends GrouponGoBaseController {

	private static final String	LOG_TAG	= "RegistrationController";

	/**
	 * @param context
	 * @param pScreen
	 */
	public RegistrationController(Context context, IScreen pScreen) {
		super(context, pScreen);
	}

	@Override
	public Object getData(int pRequestType, Object pRequestData) {
		StringRequest stringRequest = null;
		switch (pRequestType) {
		case ApiConstants.GET_HTML_PAGE_FROM_CACHE: {
			sendCachedResponseToScreen(pRequestType, pRequestData);
			break;
		}
		case ApiConstants.GET_COUNTRY_LIST:
		case ApiConstants.USER_REGISTRATION:
		case ApiConstants.GET_API_AUTH_TOKEN:
		case ApiConstants.CODE_VERIFICATION:
		case ApiConstants.RESEND_VERIFICATION_CODE:
		case ApiConstants.GET_HTML_PAGE_FROM_SERVER: {
			String urlWithParams = createGetRequestUrl(pRequestType, pRequestData);
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new GetRequest(urlWithParams, listener);
			break;
		}
		case ApiConstants.GET_CHANGE_NUMBER:
		case ApiConstants.GET_VERIFY_CHANGE_NUMBER: {
			String urlWithParams = createGetRequestUrl(pRequestType, pRequestData);
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new GetRequest(urlWithParams, listener, createApiHeaders(pRequestType));
			break;
		}
		case ApiConstants.API_DEVICE_REGISTRATION: {
			Map<String, String> params = createPostRequestParams(pRequestType, pRequestData);
			if (params == null) {
				sendRequestErrorToScreen(pRequestType, pRequestData);
				return null;
			}
			Map<String, String> headers = createApiHeaders(pRequestType);
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new PostRequest(ApiConstants.DEVICE_REGISTRATION_URL, listener, params, headers);
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
		case ApiConstants.GET_COUNTRY_LIST: {
			parseCountryListResponse(serviceResponse);
			break;
		}
		case ApiConstants.USER_REGISTRATION:
		case ApiConstants.RESEND_VERIFICATION_CODE:
		case ApiConstants.API_DEVICE_REGISTRATION:
		case ApiConstants.GET_CHANGE_NUMBER:
		case ApiConstants.GET_VERIFY_CHANGE_NUMBER: {
			parseCommonJsonResponse(serviceResponse);
			break;
		}
		case ApiConstants.CODE_VERIFICATION: {
			parseCodeVerificationResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_API_AUTH_TOKEN: {
			parseGetApiAuthTokenResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_HTML_PAGE_FROM_CACHE: {
			parseGetHtmlPageResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_HTML_PAGE_FROM_SERVER: {
			parseGetHtmlPageResponse(serviceResponse);
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
	private void parseGetHtmlPageResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		GetStaticWebPageResponse response = GSON.fromJson(responseStr, GetStaticWebPageResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		String webPage = response.getResult();
		if (!StringUtils.isNullOrEmpty(webPage)) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(webPage);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseGetApiAuthTokenResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		GetApiAuthTokenResponse response = GSON.fromJson(responseStr, GetApiAuthTokenResponse.class);
		if (response == null || response.getResult() == null || response.getResult().isEmpty()) {
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		ApiAuthTokenResult apiAuthTokenResult = response.getResult().get(0);
		if (apiAuthTokenResult != null && apiAuthTokenResult.isValid()) {
			serviceResponse.setSuccess(true);
			GrouponGoPrefs.saveApiAuthTokenResult(getContext(), apiAuthTokenResult);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseCodeVerificationResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		CodeVerificationResponse response = GSON.fromJson(responseStr, CodeVerificationResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setResponseObject(response);
		serviceResponse.setErrorMessage(response.getMessage());

		CodeVerificationResultModel codeVerifResult = response.getResult();
		if (codeVerifResult != null && codeVerifResult.getUser_id() != 0) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(codeVerifResult);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseCountryListResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		CountryListResponse countryListResponse = GSON.fromJson(responseStr, CountryListResponse.class);
		if (countryListResponse == null || countryListResponse.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(countryListResponse);
			return;
		}
		serviceResponse.setErrorMessage(countryListResponse.getMessage());

		CountryListResultModel countryListResult = countryListResponse.getResult();
		if (countryListResult != null && countryListResult.getCountry() != null && !countryListResult.getCountry().isEmpty()) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(countryListResult.getCountry());
		}
	}
}
