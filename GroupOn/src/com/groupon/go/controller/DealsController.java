package com.groupon.go.controller;

import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.model.AllOrdersResponse;
import com.groupon.go.model.AllOrdersResponse.AllOrdersResult;
import com.groupon.go.model.DealDetailResponse;
import com.groupon.go.model.DealDetailResponse.DealDetailResultModel;
import com.groupon.go.model.DealsListResponse;
import com.groupon.go.model.DealsListResponse.DealsListResultModel;
import com.groupon.go.model.MyPurchasesResponse;
import com.groupon.go.model.MyPurchasesResponse.MyPurchasesResult;
import com.groupon.go.model.RedeemVoucherResponse;
import com.groupon.go.model.RedeemVoucherResponse.RedeemVoucherResult;
import com.groupon.go.model.SuggestionsResponse;
import com.groupon.go.persistance.GrouponGoFiles;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.volleyx.GetRequest;
import com.kelltontech.volleyx.PostRequest;
import com.kelltontech.volleyx.StringResponseListener;
import com.kelltontech.volleyx.VolleyManager;

/**
 * This controller is for User related API requests. <br/>
 * 1. deals list<br/>
 * 2. deals details<br/>
 * 3. others
 */
public class DealsController extends GrouponGoBaseController {

	private static final String	LOG_TAG	= "DealsController";

	/**
	 * @param context
	 * @param pScreen
	 */
	public DealsController(Context context, IScreen pScreen) {
		super(context, pScreen);
	}

	@Override
	public Object getData(int pRequestType, Object pRequestData) {
		StringRequest stringRequest = null;
		switch (pRequestType) {
		case ApiConstants.GET_DEALS_LIST_FROM_CACHE: {
			sendCachedResponseToScreen(pRequestType, pRequestData);
			break;
		}
		case ApiConstants.GET_COUPONS:
		case ApiConstants.GET_COUPON_DETAILS:
		case ApiConstants.GET_DEALS_LIST_FROM_SERVER:
		case ApiConstants.GET_DEALS_DETAIL:
		case ApiConstants.GET_SEARCH_SUGGESTIONS:
		case ApiConstants.GET_SHARING_DEALS_DETAIL:
		case ApiConstants.GET_SEARCH_RESULTS:
		case ApiConstants.GET_ALL_ORDERS:
		case ApiConstants.GET_MY_PURCHASES: {
			String urlWithParams = createGetRequestUrl(pRequestType, pRequestData);
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new GetRequest(urlWithParams, listener, createApiHeaders(pRequestType));
			break;
		}
		case ApiConstants.GET_REDEEM_VOUCHER:
		case ApiConstants.POST_REDEEMED_VOUCHERS: {
			Map<String, String> params = createPostRequestParams(pRequestType, pRequestData);
			if (params == null) {
				sendRequestErrorToScreen(pRequestType, pRequestData);
				return null;
			}
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Request params: " + params);
			}
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			if (pRequestType == ApiConstants.GET_REDEEM_VOUCHER) {
				stringRequest = new PostRequest(ApiConstants.REDEEM_VOUCHER_URL, listener, params, createApiHeaders(pRequestType));
			} else if (pRequestType == ApiConstants.POST_REDEEMED_VOUCHERS) {
				stringRequest = new PostRequest(ApiConstants.POST_REDEEMED_VOUCHERS_URL, listener, params, createApiHeaders(pRequestType));
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
		case ApiConstants.GET_COUPONS:
		case ApiConstants.GET_DEALS_LIST_FROM_CACHE:
		case ApiConstants.GET_SEARCH_RESULTS: {
			parseDealsListResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_DEALS_LIST_FROM_SERVER: {
			parseDealsListResponse(serviceResponse);
			if (serviceResponse.isSuccess()) {
				GrouponGoFiles.saveResponseJson(getContext(), serviceResponse);
			}
			break;
		}
		case ApiConstants.GET_COUPON_DETAILS:
		case ApiConstants.GET_SHARING_DEALS_DETAIL:
		case ApiConstants.GET_DEALS_DETAIL: {
			parseDealDetailResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_SEARCH_SUGGESTIONS: {
			parseSearchSuggestionsResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_REDEEM_VOUCHER: {
			parseRedeemVoucherResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_ALL_ORDERS: {
			parseAllOrdersResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_MY_PURCHASES: {
			parseMyPurchasesResponse(serviceResponse);
			break;
		}
		case ApiConstants.POST_REDEEMED_VOUCHERS: {
			parseCommonJsonResponse(serviceResponse);
			break;
		}
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseRedeemVoucherResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		RedeemVoucherResponse response = GSON.fromJson(responseStr, RedeemVoucherResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		RedeemVoucherResult redeemVoucherResult = response.getResult();
		if (redeemVoucherResult != null && redeemVoucherResult.getVouchers() != null && !redeemVoucherResult.getVouchers().isEmpty()) {
			serviceResponse.setResponseObject(redeemVoucherResult);
			serviceResponse.setSuccess(true);
			BackGroundService.start(getContext(), ApiConstants.GET_ALL_ORDERS);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseDealsListResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		DealsListResponse response = GSON.fromJson(responseStr, DealsListResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		DealsListResultModel dealsListResult = response.getResult();
		if (dealsListResult != null && dealsListResult.getDeal() != null) {
			ProjectUtils.updateDealsList(dealsListResult.getDeal());
			serviceResponse.setResponseObject(dealsListResult);
			serviceResponse.setSuccess(true);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseDealDetailResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		DealDetailResponse response = GSON.fromJson(responseStr, DealDetailResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		DealDetailResultModel dealDetailResult = response.getResult();
		if (dealDetailResult != null && dealDetailResult.getDeal() != null) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(dealDetailResult.getDeal());
			if (serviceResponse.getDataType() == ApiConstants.GET_SHARING_DEALS_DETAIL) {
				if (dealDetailResult.getDeal().getShared_deal() == null) {
					serviceResponse.setSuccess(false);
				} else {
					ProjectUtils.updateDealModel(dealDetailResult.getDeal().getShared_deal());
				}
			}
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseSearchSuggestionsResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		SuggestionsResponse response = GSON.fromJson(responseStr, SuggestionsResponse.class);
		if (response == null) {
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());
		if (response.getResult() != null) {
			serviceResponse.setSuccess(true);
			serviceResponse.setResponseObject(response);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseAllOrdersResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		AllOrdersResponse response = GSON.fromJson(responseStr, AllOrdersResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		AllOrdersResult allOrdersResult = response.getResult();
		if (allOrdersResult != null) {
			serviceResponse.setResponseObject(allOrdersResult);
			serviceResponse.setSuccess(true);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseMyPurchasesResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		MyPurchasesResponse response = GSON.fromJson(responseStr, MyPurchasesResponse.class);
		if (response == null || response.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(response);
			return;
		}
		serviceResponse.setErrorMessage(response.getMessage());

		MyPurchasesResult myPurchasesResult = response.getResult();
		if (myPurchasesResult != null) {
			serviceResponse.setResponseObject(myPurchasesResult);
			serviceResponse.setSuccess(true);
		}
	}
}
