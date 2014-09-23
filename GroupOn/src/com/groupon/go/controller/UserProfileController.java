package com.groupon.go.controller;

import java.io.File;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.AwsConfigResponse;
import com.groupon.go.model.AwsConfigResponse.AwsConfigResult;
import com.groupon.go.model.CityListResponse;
import com.groupon.go.model.CityListResponse.CityListResultModel;
import com.groupon.go.model.GetProfileResponse;
import com.groupon.go.model.GetProfileResponse.GetProfileResultModel;
import com.groupon.go.model.UploadUserImageResponse;
import com.groupon.go.model.UploadUserImageResponse.UploadUserImageResult;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoFiles;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.HttpClientConnection;
import com.kelltontech.network.ServiceRequest;
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
 */
public class UserProfileController extends GrouponGoBaseController {

	private static final String	LOG_TAG	= "UserProfileController";

	/**
	 * @param context
	 * @param pScreen
	 */
	public UserProfileController(Context context, IScreen pScreen) {
		super(context, pScreen);
	}

	@Override
	public Object getData(int pRequestType, Object pRequestData) {
		StringRequest stringRequest = null;
		switch (pRequestType) {
		case ApiConstants.GET_PROFILE_FROM_CACHE:
		case ApiConstants.GET_CITY_LIST_FROM_CACHE: {
			sendCachedResponseToScreen(pRequestType, pRequestData);
			break;
		}
		case ApiConstants.GET_PROFILE_FROM_SERVER:
		case ApiConstants.GET_CITY_LIST_FROM_SERVER:
		case ApiConstants.GET_IMAGE_SIGNATURE:
		case ApiConstants.REMOVE_USER_IMAGE: {
			String urlWithParams = createGetRequestUrl(pRequestType, pRequestData);
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new GetRequest(urlWithParams, listener, createApiHeaders(pRequestType));
			break;
		}
		case ApiConstants.UPDATE_PROFILE: {
			Map<String, String> params = createPostRequestParams(pRequestType, pRequestData);
			if (params == null) {
				sendRequestErrorToScreen(pRequestType, pRequestData);
				return null;
			}
			Map<String, String> headers = createApiHeaders(pRequestType);
			StringResponseListener listener = new StringResponseListener(this, pRequestType, pRequestData);
			stringRequest = new PostRequest(ApiConstants.UPDATE_PROFILE_URL, listener, params, headers);
			break;
		}
		case ApiConstants.UPLOAD_USER_IMAGE: {
			ServiceRequest serviceRq = new ServiceRequest();
			if (pRequestData instanceof AwsConfigResult) {
				AwsConfigResult awsConfigResult = (AwsConfigResult) pRequestData;
				serviceRq.setUrl(awsConfigResult.getForm_action());
				serviceRq.setPostData(createAwsUploadMultipartEntity(awsConfigResult));
				serviceRq.setRequestData(pRequestData);
				serviceRq.setResponseController(this);
				serviceRq.setDataType(pRequestType);
				serviceRq.setPriority(HttpClientConnection.PRIORITY.LOW);
				serviceRq.setHttpMethod(HttpClientConnection.HTTP_METHOD.POST);
				HttpClientConnection connection = HttpClientConnection.getInstance();
				connection.addRequest(serviceRq);
			} else {
				sendRequestErrorToScreen(pRequestType, pRequestData);
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
		case ApiConstants.GET_PROFILE_FROM_CACHE: {
			parseGetProfileResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_PROFILE_FROM_SERVER: {
			parseGetProfileResponse(serviceResponse);
			if (serviceResponse.isSuccess()) {
				GrouponGoFiles.saveResponseJson(getContext(), serviceResponse);
			}
			break;
		}
		case ApiConstants.UPDATE_PROFILE: {
			parseCommonJsonResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_CITY_LIST_FROM_CACHE: {
			parseCityListResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_CITY_LIST_FROM_SERVER: {
			parseCityListResponse(serviceResponse);
			if (serviceResponse.isSuccess()) {
				GrouponGoFiles.saveResponseJson(getContext(), serviceResponse);
			}
			break;
		}
		case ApiConstants.GET_IMAGE_SIGNATURE: {
			parseImageSignatureResponse(serviceResponse);
			break;
		}
		case ApiConstants.UPLOAD_USER_IMAGE: {
			parseUploadUserImageResponse(serviceResponse);
			break;
		}
		case ApiConstants.REMOVE_USER_IMAGE: {
			parseCommonJsonResponse(serviceResponse);
			break;
		}
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseUploadUserImageResponse(ServiceResponse serviceResponse) {
		byte[] responseData = (byte[]) serviceResponse.getRawResponse();
		String responseStr = new String(responseData);
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "Response Json: " + responseStr);
		}
		UploadUserImageResponse uploadUserImageResponse = GSON.fromJson(responseStr, UploadUserImageResponse.class);
		if (uploadUserImageResponse == null || uploadUserImageResponse.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(uploadUserImageResponse);
			return;
		}
		serviceResponse.setErrorMessage(uploadUserImageResponse.getMessage());

		UploadUserImageResult imageSignatureResult = uploadUserImageResponse.getResult();
		if (imageSignatureResult != null && !StringUtils.isNullOrEmpty(imageSignatureResult.getProfile_image())) {
			serviceResponse.setResponseObject(imageSignatureResult);
			serviceResponse.setSuccess(true);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseImageSignatureResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		AwsConfigResponse getImageSignatureResponse = GSON.fromJson(responseStr, AwsConfigResponse.class);
		if (getImageSignatureResponse == null || getImageSignatureResponse.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(getImageSignatureResponse);
			return;
		}
		serviceResponse.setErrorMessage(getImageSignatureResponse.getMessage());

		AwsConfigResult imageSignatureResult = getImageSignatureResponse.getResult();
		if (imageSignatureResult != null) {
			serviceResponse.setResponseObject(imageSignatureResult);
			serviceResponse.setSuccess(true);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseGetProfileResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		GetProfileResponse getProfileResponse = GSON.fromJson(responseStr, GetProfileResponse.class);
		if (getProfileResponse == null || getProfileResponse.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(getProfileResponse);
			return;
		}
		serviceResponse.setErrorMessage(getProfileResponse.getMessage());

		GetProfileResultModel profileResult = getProfileResponse.getResult();
		if (profileResult != null) {
			if (profileResult.getCategory() != null) {
				ProjectUtils.syncCategoriesSelectionState(profileResult);
			}
			serviceResponse.setResponseObject(profileResult);
			serviceResponse.setSuccess(true);
			if (serviceResponse.getDataType() == ApiConstants.GET_PROFILE_FROM_SERVER) {
				GrouponGoPrefs.saveUserDetails(getContext(), profileResult);
				Intent broadcastIntent = new Intent(Constants.ACTION_GET_PROFILE_RESP_RCVD);
				LocalBroadcastManager.getInstance(getContext()).sendBroadcast(broadcastIntent);
			}
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void parseCityListResponse(ServiceResponse serviceResponse) {
		String responseStr = (String) serviceResponse.getRawResponse();
		CityListResponse cityListResponse = GSON.fromJson(responseStr, CityListResponse.class);
		if (cityListResponse == null || cityListResponse.getError_code() == ApiConstants.ERROR_CODE_AUTH_FAILURE) {
			serviceResponse.setResponseObject(cityListResponse);
			return;
		}
		serviceResponse.setErrorMessage(cityListResponse.getMessage());

		CityListResultModel cityListResult = cityListResponse.getResult();
		if (cityListResult != null && cityListResult.getCity() != null && !cityListResult.getCity().isEmpty()) {
			serviceResponse.setResponseObject(cityListResult);
			serviceResponse.setSuccess(true);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private HttpEntity createAwsUploadMultipartEntity(AwsConfigResult awsConfigResult) {
		try {
			MultipartEntity multipartEntity = new MultipartEntity();
			multipartEntity.addPart(ApiConstants.PARAM_KEY, new StringBody(awsConfigResult.getKey()));
			multipartEntity.addPart(ApiConstants.PARAM_AWS_ACCESS_ID, new StringBody(awsConfigResult.getAWSAccessKeyId()));
			multipartEntity.addPart(ApiConstants.PARAM_ACL, new StringBody(awsConfigResult.getAcl()));
			multipartEntity.addPart(ApiConstants.PARAM_SUCCESS_ACTION_REDIRECT, new StringBody(awsConfigResult.getSuccess_action_redirect()));
			multipartEntity.addPart(ApiConstants.PARAM_POLICY, new StringBody(awsConfigResult.getPolicy()));
			multipartEntity.addPart(ApiConstants.PARAM_SIGNATURE, new StringBody(awsConfigResult.getSignature()));
			multipartEntity.addPart(ApiConstants.PARAM_CONTENT_TYPE, new StringBody("image/png"));
			UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(getContext());
			String imageName = userDetails.getUser_id() + "-" + System.currentTimeMillis() + ".png";
			if (awsConfigResult.getFile_data() != null) {
				ByteArrayBody byteArrayBody = new ByteArrayBody(awsConfigResult.getFile_data(), "image/png", imageName);
				multipartEntity.addPart(ApiConstants.PARAM_FILE, byteArrayBody);
			} else if (!(StringUtils.isNullOrEmpty(awsConfigResult.getFile_path()))) {
				FileBody fileBody = new FileBody(new File(awsConfigResult.getFile_path()), imageName, "image/jpg", "ISO-8859-1");
				multipartEntity.addPart(ApiConstants.PARAM_FILE, fileBody);
			}
			return multipartEntity;
		} catch (Exception e) {
			Log.e(LOG_TAG, "createAwsUploadMultipartEntity()", e);
			return null;
		}
	}
}
