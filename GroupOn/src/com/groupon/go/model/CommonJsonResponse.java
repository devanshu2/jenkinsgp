package com.groupon.go.model;

/**
 * @author sachin.gupta
 */
public class CommonJsonResponse {

	private boolean	is_success;
	private int		error_code;
	private String	message;


	/**
	 * @return the is_success
	 */
	public boolean is_success() {
		return is_success;
	}

	/**
	 * @param is_success
	 *            the is_success to set
	 */
	public void set_success(boolean is_success) {
		this.is_success = is_success;
	}

	/**
	 * @return the error_code
	 */
	public int getError_code() {
		return error_code;
	}

	/**
	 * @param error_code
	 *            the error_code to set
	 */
	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
