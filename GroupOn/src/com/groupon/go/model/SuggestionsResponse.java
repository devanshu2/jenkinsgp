package com.groupon.go.model;

import java.util.List;

/**
 * @author sachin.gupta
 */
public class SuggestionsResponse extends CommonJsonResponse {

	private List<String>	result;

	/**
	 * @return the result
	 */
	public List<String> getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(List<String> result) {
		this.result = result;
	}
}
