package com.kelltontech.volleyx;

import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.StringRequest;

/**
 * @author sachin.gupta
 */
public class StringRequestWithHeaders extends StringRequest {

	private Map<String, String>	mHttpHeadersMap;

	/**
	 * @param url
	 * @param listener
	 * @param errorListener
	 */
	public StringRequestWithHeaders(String url, StringResponseListener listener, Map<String, String> headers) {
		super(url, listener, listener);
		mHttpHeadersMap = headers;
	}

	/**
	 * @param method
	 * @param url
	 * @param listener
	 * @param errorListener
	 */
	public StringRequestWithHeaders(int method, String url, StringResponseListener listener, Map<String, String> headers) {
		super(method, url, listener, listener);
		mHttpHeadersMap = headers;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		if (mHttpHeadersMap == null) {
			return super.getHeaders();
		} else {
			return mHttpHeadersMap;
		}
	}
}
