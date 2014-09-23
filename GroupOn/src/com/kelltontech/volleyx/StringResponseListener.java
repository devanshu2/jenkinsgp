/**
 * 
 */
package com.kelltontech.volleyx;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.kelltontech.controller.IController;
import com.kelltontech.network.HttpUtils;
import com.kelltontech.network.ServiceResponse;

/**
 * connector between response handling of Volley and our network layer
 * 
 * @author sachin.gupta
 */
public class StringResponseListener implements Listener<String>, ErrorListener {

	private IController	mController;
	private int			mRequestType;
	private Object		mRequestData;

	public StringResponseListener(IController pController, int pRequestType, Object pRequestData) {
		this.mController = pController;
		this.mRequestType = pRequestType;
		this.mRequestData = pRequestData;
	}

	@Override
	public void onResponse(String pResponseStr) {
		ServiceResponse serviceResponse = createServiceResponse(pResponseStr);
		sendResponseToScreen(serviceResponse);
	}

	@Override
	public void onErrorResponse(VolleyError pResponseError) {
		ServiceResponse serviceResponse = createServiceResponse(pResponseError);
		sendResponseToScreen(serviceResponse);
	}

	/**
	 * @param pResponseStr
	 * @return
	 */
	private ServiceResponse createServiceResponse(String pResponseStr) {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse.setDataType(mRequestType);
		serviceResponse.setRequestData(mRequestData);
		serviceResponse.setSuccess(true);
		serviceResponse.setRawResponse(pResponseStr);
		return serviceResponse;
	}

	/**
	 * @param pResponseError
	 * @return
	 */
	private ServiceResponse createServiceResponse(VolleyError pResponseError) {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse.setDataType(mRequestType);
		serviceResponse.setRequestData(mRequestData);
		serviceResponse.setSuccess(false);
		serviceResponse.setException(pResponseError);
		serviceResponse.setErrorMessage(HttpUtils.getErrorMessage(pResponseError));
		return serviceResponse;
	}

	/**
	 * @param pServiceResponse
	 */
	private void sendResponseToScreen(final ServiceResponse pServiceResponse) {
		new Thread() {
			public void run() {
				mController.handleResponse(pServiceResponse);
			}
		}.start();
	}
}
