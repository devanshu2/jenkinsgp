package com.kelltontech.controller;

import android.content.Context;

import com.kelltontech.network.ServiceResponse;

/**
 * This interface will be used as a base interface for all controllers
 */
public interface IController {

	Context getContext();

	Object getData(int dataType, Object requestData);

	Object getData(Object requestData);

	void handleResponse(ServiceResponse serviceResponse);

	void parseResponse(ServiceResponse serviceResponse);
	
	void sendResponseToScreen(ServiceResponse serviceResponse);
}
