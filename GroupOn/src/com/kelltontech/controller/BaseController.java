package com.kelltontech.controller;

import android.content.Context;
import android.util.Log;

import com.groupon.go.BuildConfig;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;

/**
 * This class will be used as a base class for all controllers
 */
public abstract class BaseController implements IController {

	private static String	LOG_TAG	= "BaseController";

	private Context			context;
	private IScreen			screen;

	/**
	 * @param context
	 */
	public BaseController(Context context, IScreen screen) {
		this.context = context;
		this.screen = screen;
	}

	/**
	 * @return the context
	 */
	@Override
	public Context getContext() {
		return context;
	}

	/**
	 * @return the screen
	 */
	@Override
	public final void sendResponseToScreen(ServiceResponse serviceResponse) {
		if (screen == null) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "sendResponseToScreen(): Screen is null");
			}
			return;
		}
		screen.handleUiUpdate(serviceResponse);
	}

	/**
	 * Must be overridden by subclass to support ServiceRequest without
	 * requestType
	 */
	@Override
	public Object getData(Object requestData) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Must be overridden by subclass to support ServiceRequest with requestType
	 */
	@Override
	public Object getData(int requestType, Object requestData) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param requestModel
	 * @return common response model if request data has some error
	 */
	protected final void sendRequestErrorToScreen(int requestType, Object requestData) {
		Log.e(LOG_TAG, "sendRequestErrorToScreen()");
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse.setDataType(requestType);
		serviceResponse.setRequestData(requestData);
		serviceResponse.setErrorMessage("Some error in Request Data.");
		serviceResponse.setSuccess(false);
		sendResponseToScreen(serviceResponse);
	}
}