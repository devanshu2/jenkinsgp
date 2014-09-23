package com.groupon.go.persistance;

import android.content.Context;

import com.groupon.go.constants.ApiConstants;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.AndroidFileUtils;

/**
 * @author sachin.gupta
 */
public class GrouponGoFiles {

	private static final String	FILE_JSON_PROFLIE			= "file_json_proflie";
	private static final String	FILE_JSON_CITY_LIST			= "file_json_city_list";
	private static final String	FILE_JSON_DEALS_FEATURED	= "file_json_deals_featured";
	private static final String	FILE_JSON_DEALS_TRENDING	= "file_json_deals_trending";
	private static final String	FILE_JSON_DEALS_NEAR_BY		= "file_json_deals_near_by";
	private static final String	FILE_JSON_USER_CART			= "file_json_user_cart";
	private static final String	FILE_JSON_TERMS_OF_USE		= "file_json_terms_of_use";
	private static final String	FILE_JSON_PRIVACY_POLICY	= "file_json_privacy_policy";

	/**
	 * @param pContext
	 * @param pDataType
	 * @return pRequestData
	 */
	public static String getResponseJson(Context pContext, int pDataType, Object pRequestData) {
		String cacheFileName = getCacheFileName(pDataType, pRequestData);
		if (cacheFileName == null) {
			return null; // response from this API was not cached
		}
		byte[] fileData = AndroidFileUtils.readAppPrivateFile(pContext, cacheFileName);
		if (fileData == null) {
			return null;
		}
		return new String(fileData);
	}

	/**
	 * @param pContext
	 * @param serviceResponse
	 */
	public static void saveResponseJson(Context pContext, ServiceResponse serviceResponse) {
		String cacheFileName = getCacheFileName(serviceResponse.getDataType(), serviceResponse.getRequestData());
		if (cacheFileName == null) {
			return; // response from this API is not to be cached
		}
		if (serviceResponse.getRawResponse() instanceof String) {
			AndroidFileUtils.createAppPrivateFile(pContext, cacheFileName, ((String) serviceResponse.getRawResponse()).getBytes());
		}
	}

	/**
	 * @param pDataType
	 * @param pRequestData
	 * 
	 * @return
	 */
	private static String getCacheFileName(int pDataType, Object pRequestData) {
		String cacheFileName = null;
		switch (pDataType) {
		case ApiConstants.GET_PROFILE_FROM_CACHE: {
			cacheFileName = FILE_JSON_PROFLIE;
			break;
		}
		case ApiConstants.GET_PROFILE_FROM_SERVER: {
			if (pRequestData instanceof Boolean && (Boolean) pRequestData) {
				cacheFileName = FILE_JSON_PROFLIE;
			}
			break;
		}
		case ApiConstants.GET_CITY_LIST_FROM_CACHE:
		case ApiConstants.GET_CITY_LIST_FROM_SERVER: {
			cacheFileName = FILE_JSON_CITY_LIST;
			break;
		}
		case ApiConstants.GET_USER_CART_FROM_CACHE:
		case ApiConstants.GET_USER_CART_FROM_SERVER:
		case ApiConstants.APPLY_COUPON_CODE:
		case ApiConstants.REMOVE_COUPON_CODE:
		case ApiConstants.APPLY_USER_CREDITS:
		case ApiConstants.REMOVE_USER_CREDITS:
		case ApiConstants.ADD_DEAL_TO_CART:
		case ApiConstants.DELETE_DEAL_FROM_CART:
		case ApiConstants.EDIT_DEAL_IN_CART: {
			cacheFileName = FILE_JSON_USER_CART;
			break;
		}
		case ApiConstants.GET_DEALS_LIST_FROM_CACHE:
		case ApiConstants.GET_DEALS_LIST_FROM_SERVER: {
			switch (ProjectUtils.getDealsListRespTypeToBeCached(pRequestData)) {
			case ApiConstants.VALUE_DEAL_TYPE_FEATURED: {
				cacheFileName = FILE_JSON_DEALS_FEATURED;
				break;
			}
			case ApiConstants.VALUE_DEAL_TYPE_NEAR_BY: {
				cacheFileName = FILE_JSON_DEALS_NEAR_BY;
				break;
			}
			case ApiConstants.VALUE_DEAL_TYPE_TRENDING: {
				cacheFileName = FILE_JSON_DEALS_TRENDING;
				break;
			}
			}
			break;
		}
		case ApiConstants.GET_HTML_PAGE_FROM_CACHE:
		case ApiConstants.GET_HTML_PAGE_FROM_SERVER: {
			if (pRequestData instanceof Integer) {
				switch ((Integer) pRequestData) {
				case ApiConstants.VALUE_HTML_TERMS_OF_USE: {
					cacheFileName = FILE_JSON_TERMS_OF_USE;
					break;
				}
				case ApiConstants.VALUE_HTML_PRIVACY_POLICY: {
					cacheFileName = FILE_JSON_PRIVACY_POLICY;
					break;
				}
				}
				break;
			}
			break;
		}
		}
		return cacheFileName;
	}

	/**
	 * @param pContext
	 */
	public static void deleteCachedDealsJson(Context pContext) {
		AndroidFileUtils.deleteAppPrivateFile(pContext, FILE_JSON_DEALS_NEAR_BY);
		AndroidFileUtils.deleteAppPrivateFile(pContext, FILE_JSON_DEALS_FEATURED);
		AndroidFileUtils.deleteAppPrivateFile(pContext, FILE_JSON_DEALS_TRENDING);
	}

	/**
	 * @param pContext
	 */
	public static void deleteCachedCartJson(Context pContext) {
		AndroidFileUtils.deleteAppPrivateFile(pContext, FILE_JSON_USER_CART);
	}

	/**
	 * @param pContext
	 * @return
	 */
	public static boolean isTermsOfUseCached(Context pContext) {
		return AndroidFileUtils.isAppPrivateFileExists(pContext, FILE_JSON_TERMS_OF_USE);
	}

	/**
	 * @param pContext
	 * @return
	 */
	public static boolean isPrivacyPolicyCached(Context pContext) {
		return AndroidFileUtils.isAppPrivateFileExists(pContext, FILE_JSON_PRIVACY_POLICY);
	}
}