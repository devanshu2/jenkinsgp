package com.groupon.go.controller;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.util.Log;

import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.kelltontech.network.HttpClientConnection;
import com.kelltontech.network.ServiceRequest;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.StringUtils;

/**
 * @author sachin.gupta
 */
public class ReverseGeoController extends GrouponGoBaseController {

	private static final String	LOG_TAG						= "ReverseGeoController";

	private static final String	GOOGLE_REVERSE_GEO_API_URL	= "http://maps.google.com/maps/api/geocode/json?sensor=true&latlng=";

	// Constants for JSON Keys
	private static final String	JSON_KEY_RESULTS			= "results";
	private static final String	JSON_KEY_ADDRESS_COMPONENTS	= "address_components";
	private static final String	JSON_KEY_TYPES				= "types";
	private static final String	JSON_KEY_LOCALITY			= "locality";
	private static final String	JSON_KEY_LONG_NAME			= "long_name";

	/**
	 * @param context
	 * @param pScreen
	 */
	public ReverseGeoController(Context context, IScreen pScreen) {
		super(context, pScreen);
	}

	@Override
	public ServiceRequest getData(final int requestType, final Object requestData) {
		switch (requestType) {
		case ApiConstants.GET_GEO_ADDRESS_BY_LAT_LONG: {
			new Thread(new Runnable() {
				@Override
				public void run() {
					ServiceResponse response = new ServiceResponse();
					response.setDataType(requestType);
					response.setRequestData(requestData);
					Address address = null;
					if (requestData instanceof Location) {
						address = LocationUtils.getAddressByGeoCoder(getContext(), (Location) requestData);
					}
					if (address == null) {
						response.setSuccess(false);
					} else {
						response.setResponseObject(address);
						response.setSuccess(true);
					}
					sendResponseToScreen(response);
				}
			}).start();
			break;
		}
		case ApiConstants.GET_CITY_BY_LAT_LONG_GOOGLE_API: {
			String latLongStr = null;
			if (requestData instanceof Location) {
				Location location = (Location) requestData;
				latLongStr = location.getLatitude() + "," + location.getLongitude();
			} else {
				sendRequestErrorToScreen(requestType, requestData);
				return null;
			}
			ServiceRequest serviceRq = new ServiceRequest();
			serviceRq.setRequestData(requestData);
			serviceRq.setResponseController(this);
			serviceRq.setDataType(requestType);
			serviceRq.setPriority(HttpClientConnection.PRIORITY.LOW);
			serviceRq.setUrl(GOOGLE_REVERSE_GEO_API_URL + latLongStr);
			serviceRq.setHttpMethod(HttpClientConnection.HTTP_METHOD.GET);
			HttpClientConnection connection = HttpClientConnection.getInstance();
			connection.addRequest(serviceRq);
			return serviceRq;
		}
		case ApiConstants.GET_PATH_FOR_DIRECTION: {
			ServiceRequest serviceRq = new ServiceRequest();
			serviceRq.setResponseController(this);
			serviceRq.setDataType(requestType);
			serviceRq.setPriority(HttpClientConnection.PRIORITY.LOW);
			serviceRq.setUrl((String) requestData);
			serviceRq.setHttpMethod(HttpClientConnection.HTTP_METHOD.GET);
			HttpClientConnection connection = HttpClientConnection.getInstance();
			connection.addRequest(serviceRq);
			return serviceRq;
		}
		}
		return null;
	}

	@Override
	public void parseResponse(ServiceResponse serviceResponse) {
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_CITY_BY_LAT_LONG_GOOGLE_API: {
			parseGoogleReverseGeoApiResponse(serviceResponse);
			break;
		}
		case ApiConstants.GET_PATH_FOR_DIRECTION: {
			parseGooglePathDirectionResponse(serviceResponse);
			break;
		}
		}
	}

	private void parseGooglePathDirectionResponse(ServiceResponse serviceResponse) {
		boolean isSuccess = false;
		try {
			byte[] responseData = (byte[]) serviceResponse.getRawResponse();
			String responseStr = new String(responseData);
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Response Json: " + responseStr);
			}
			serviceResponse.setResponseObject(responseStr);
			isSuccess = true;
		} catch (Exception e) {
			Log.e(LOG_TAG, "parseGoogleDirectionResponse()", e);
		} finally {
			if (serviceResponse != null) {
				serviceResponse.setSuccess(isSuccess);
			}
		}	
		
	}

	/**
	 * @param serviceResponse
	 */
	private void parseGoogleReverseGeoApiResponse(ServiceResponse serviceResponse) {
		boolean isSuccess = false;
		try {
			byte[] responseData = (byte[]) serviceResponse.getRawResponse();
			String responseStr = new String(responseData);
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Response Json: " + responseStr);
			}
			JSONObject jsonObject = new JSONObject(responseStr);
			serviceResponse.setResponseObject(getCityFromJSon(jsonObject));
			isSuccess = true;
		} catch (Exception e) {
			Log.e(LOG_TAG, "parseGoogleReverseGeoApiResponse()", e);
		} finally {
			if (serviceResponse != null) {
				serviceResponse.setSuccess(isSuccess);
			}
		}
	}

	/**
	 * @param jsonObject
	 * @return
	 */
	private String getCityFromJSon(JSONObject jsonObject) {
		String cityStr = "";
		try {
			JSONObject location = jsonObject.getJSONArray(JSON_KEY_RESULTS).getJSONObject(0);
			JSONArray addressComponents = location.getJSONArray(JSON_KEY_ADDRESS_COMPONENTS);
			for (int i = 0; i < addressComponents.length(); i++) {
				JSONObject addressComp = addressComponents.getJSONObject(i);
				JSONArray addressCompType = addressComp.getJSONArray(JSON_KEY_TYPES);
				for (int j = 0; j < addressCompType.length(); j++) {
					if (addressCompType.getString(j).equals(JSON_KEY_LOCALITY)) {
						cityStr = addressComp.getString(JSON_KEY_LONG_NAME);
						if (!StringUtils.isNullOrEmpty(cityStr)) {
							return cityStr;
						}
					}
				}
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, "getCityFromJSon()", ex);
		}
		return cityStr;
	}

}
