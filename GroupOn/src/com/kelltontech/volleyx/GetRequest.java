package com.kelltontech.volleyx;

import java.util.Map;

/**
 * @author sachin.gupta
 */
public class GetRequest extends StringRequestWithHeaders {

	/**
	 * @param url
	 * @param listener
	 */
	public GetRequest(String url, StringResponseListener listener) {
		this(url, listener, null);
	}

	/**
	 * @param url
	 * @param listener
	 * @param httpHeaders
	 */
	public GetRequest(String url, StringResponseListener listener, Map<String, String> httpHeaders) {
		super(url, listener, httpHeaders);
	}
}
