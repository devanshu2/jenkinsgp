package com.kelltontech.ui;

import com.kelltontech.network.ServiceResponse;

/**
 * @author sachin.gupta
 */
public interface IScreen {
	/**
	 * Subclass should over-ride this method to update the UI with response. <br/>
	 * Subclass should note that it might being called from non-UI thread.
	 * 
	 * @param serviceResponse
	 */
	void handleUiUpdate(ServiceResponse serviceResponse);

	/**
	 * Subclass should over-ride this method to update the UI on event <br/>
	 * Caller should note that it should be called only from UI thread.
	 * 
	 * @param eventId
	 * @param eventData
	 */
	void onEvent(int eventId, Object eventData);
}
