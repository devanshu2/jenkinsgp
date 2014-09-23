package com.groupon.go.controller;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.model.CreateOrderResponse;
import com.groupon.go.model.GetAllBanksResponse;
import com.groupon.go.model.GetAllBanksResponse.GetAllBankResult;
import com.groupon.go.model.GetPayUConfigResponse;
import com.groupon.go.model.GetPayUConfigResponse.GetPayUConfigResult;
import com.groupon.go.model.GetSavedCardsResponse;
import com.groupon.go.model.GetSavedCardsResponse.GetSavedCardsResult;
import com.groupon.go.model.PayUCardVerificationResponse;
import com.groupon.go.model.PaymentDetailModel;
import com.groupon.go.model.PaymentStatusResponse;
import com.groupon.go.model.PaymentStatusResponse.PaymentStatusResult;
import com.groupon.go.model.UserCardResponse;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.volleyx.GetRequest;
import com.kelltontech.volleyx.PostRequest;
import com.kelltontech.volleyx.StringResponseListener;
import com.kelltontech.volleyx.VolleyManager;

/**
 * @author vineet.kumar
 */
public class PaymentController extends GrouponGoBaseController {

	private static final String	LOG_TAG	= "PaymentController";

	/**
	 * @param context
	 * @param screen
	 */
	public PaymentController(Context context, IScreen screen) {
		super(context, screen);
	}

	@Override
	public Object getData(int pRequestType, Object pRequestData) {
		StringRequest stringRequest = null;
		switch (pRequestType) {
		case ApiConstants.GET_ALL_BANK:
		case ApiConstants.GET_PAYU_CONFIGURATION:
		case ApiConstants.GET_CREATE_ORDER:
		case ApiConstants.GET_CREATE_ORDER_FRM_CARD:
		case ApiConstants.GET_PAYMENT_STATUS:
		case ApiConstants.GET_SAVED_CARDS:
		case ApiConstants.GET_SUMMARY_BY_EMAIL: {
			String urlWithParams = createGetRequestUrl(pRequestType, pRequestData);
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new GetRequest(urlWithParams, listener, createApiHeaders(pRequestType));
			break;
		}
		case ApiConstants.GET_PAYU_CONFIG_TO_VERIFY_CARD:
		case ApiConstants.GET_SAVE_USER_CARD:
		case ApiConstants.DELETE_PAY_CARD_ON_PAY_U: {
			Map<String, String> params = null;
			if (pRequestData instanceof HashMap<?, ?>) {
				params = (Map<String, String>) pRequestData;
			}
			if (params == null) {
				sendRequestErrorToScreen(pRequestType, pRequestData);
				return null;
			}
			String url = null;
			if (params.containsKey(ApiConstants.PARAM_PAYU_REDIRECT_URL)) {
				url = params.remove(ApiConstants.PARAM_PAYU_REDIRECT_URL);
			}
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Request: " + params.toString());
			}
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new PostRequest(url, listener, params, createApiHeaders(pRequestType));
			break;
		}
		case ApiConstants.PURCHASE_WITHOUT_PAYMENT: {
			Map<String, String> params = createPostRequestParams(pRequestType, pRequestData);
			if (params == null) {
				sendRequestErrorToScreen(pRequestType, pRequestData);
				return null;
			}
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new PostRequest(ApiConstants.PURCHASE_WITHOUT_PAYMENT_URL, listener, params, createApiHeaders(pRequestType));
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
		case ApiConstants.GET_CREATE_ORDER_FRM_CARD:
		case ApiConstants.GET_CREATE_ORDER: {
			parseCreateOrderResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_PAYMENT_STATUS: {
			parsePaymentStatusResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_SAVED_CARDS: {
			parseGetSavedCardsResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_PAYU_CONFIGURATION: {
			parsePayUConfigResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_SAVE_USER_CARD:
		case ApiConstants.DELETE_PAY_CARD_ON_PAY_U: {
			parseSaveUserCardResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_PAYU_CONFIG_TO_VERIFY_CARD: {
			parsePayUConfigResponseForDebitCard(serviceResponse);
			break;
		}
		case ApiConstants.GET_ALL_BANK: {
			parseGetAllBankResponse(serviceResponse);
			break;
		}
		case ApiConstants.PURCHASE_WITHOUT_PAYMENT:
		case ApiConstants.GET_SUMMARY_BY_EMAIL: {
			parseCommonJsonResponse(serviceResponse);
			break;
		}
		}
	}

	private void parseGetAllBankResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		GetAllBanksResponse response = GSON.fromJson(responseStr, GetAllBanksResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());
		GetAllBankResult getAllBankResult = response.getResult();
		if (getAllBankResult != null && getAllBankResult.getBanks() != null && !getAllBankResult.getBanks().isEmpty()) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(getAllBankResult);
		}

	}

	private void parsePayUConfigResponseForDebitCard(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		PayUCardVerificationResponse response = GSON.fromJson(responseStr, PayUCardVerificationResponse.class);
		if (response != null) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(response);
		}
	}

	private void parseSaveUserCardResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		UserCardResponse response = GSON.fromJson(responseStr, UserCardResponse.class);
		if (response != null) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(response);
		}
	}

	private void parsePayUConfigResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		GetPayUConfigResponse response = GSON.fromJson(responseStr, GetPayUConfigResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());
		GetPayUConfigResult payConfigResult = response.getResult();
		if (payConfigResult != null) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(payConfigResult);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseGetSavedCardsResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		GetSavedCardsResponse response = GSON.fromJson(responseStr, GetSavedCardsResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		GetSavedCardsResult getSavedCardsResult = response.getResult();
		if (getSavedCardsResult != null && getSavedCardsResult.getCards() != null) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(getSavedCardsResult);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parsePaymentStatusResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		PaymentStatusResponse response = GSON.fromJson(responseStr, PaymentStatusResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		PaymentStatusResult paymentStatusResult = response.getResult();
		if (paymentStatusResult != null) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(paymentStatusResult);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseCreateOrderResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		CreateOrderResponse response = GSON.fromJson(responseStr, CreateOrderResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE || response.getError_code() == ApiConstants.ERROR_CODE_OFFER_OUT_OF_STOCK) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		PaymentDetailModel payDetailModel = response.getResult();
		if (payDetailModel != null) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(payDetailModel);
		}
	}
}
