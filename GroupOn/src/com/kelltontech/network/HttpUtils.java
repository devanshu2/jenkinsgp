/**
 * 
 */
package com.kelltontech.network;

import java.io.IOException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import android.util.Log;

import com.android.volley.TimeoutError;
import com.groupon.go.BuildConfig;

/**
 * @author sachin.gupta
 */
public class HttpUtils {

	private static final String	LOG_TAG	= HttpUtils.class.getSimpleName();

	/**
	 * @param pException
	 * @return
	 */
	public static String getErrorMessage(Exception pException) {
		if (BuildConfig.DEBUG) {
			Log.e(LOG_TAG, "getErrorMessage() " + pException);
		}
		if (pException instanceof URISyntaxException) {
			return "Request URL is not valid.";
		}
		if (pException instanceof UnknownHostException) {
			return "No host server found for requested URL.";
		}
		if (pException instanceof SocketException || pException instanceof TimeoutError) {
			return "Request timed-out while waiting for server response.";
		}
		if (pException instanceof IOException) {
			return "Some error occured while connecting to server. Please try again.";
		}
		return "Could not connect to server. Please try again.";
	}
}
