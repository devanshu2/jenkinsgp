package com.kelltontech.volleyx;

import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;

/**
 * @author sachin.gupta
 */
public class PostRequest extends StringRequestWithHeaders {

	private Map<String, String>	mPostParamsMap;

	/**
	 * @param url
	 * @param listener
	 */
	public PostRequest(String url, StringResponseListener listener) {
		this(url, listener, null);
	}

	/**
	 * @param url
	 * @param listener
	 * @param postParams
	 */
	public PostRequest(String url, StringResponseListener listener, Map<String, String> postParams) {
		this(url, listener, postParams, null);
	}

	/**
	 * @param url
	 * @param listener
	 * @param postParams
	 * @param httpHeaders
	 */
	public PostRequest(String url, StringResponseListener listener, Map<String, String> postParams, Map<String, String> httpHeaders) {
		super(Request.Method.POST, url, listener, httpHeaders);
		mPostParamsMap = postParams;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return mPostParamsMap;
	}
}
