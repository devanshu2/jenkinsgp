package com.groupon.go.model;

import org.json.JSONArray;

/**
 * @author sachin.gupta
 */
public class UserDetails {

	// below 2 are from codeVerif API
	private int			user_id;
	private String		coupon_code;

	// below 2 are saved locally
	private String		countryCode;
	private String		mobile_no;

	// below 3 are from getProfile API, user_id is again rcvd here
	private String		username		= "";
	private String		emailId			= "";
	private String		profile_image	= "";

	// above 3 and below 1 are to be sent to updateProfile API
	private JSONArray	preferences;

	/**
	 * @return the user_id
	 */
	public int getUser_id() {
		return user_id;
	}

	/**
	 * @param user_id
	 *            the user_id to set
	 */
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	/**
	 * @return the coupon_code
	 */
	public String getCoupon_code() {
		return coupon_code;
	}

	/**
	 * @param coupon_code
	 *            the coupon_code to set
	 */
	public void setCoupon_code(String coupon_code) {
		this.coupon_code = coupon_code;
	}

	/**
	 * @return the countryCode
	 */
	public String getCountryCode() {
		return countryCode;
	}

	/**
	 * @param countryCode
	 *            the countryCode to set
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	/**
	 * @return the userPhone
	 */
	public String getUserPhone() {
		return mobile_no;
	}

	/**
	 * @param userPhone
	 *            the userPhone to set
	 */
	public void setUserPhone(String userPhone) {
		this.mobile_no = userPhone;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the emailId
	 */
	public String getEmailId() {
		return emailId;
	}

	/**
	 * @param emailId
	 *            the emailId to set
	 */
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	/**
	 * @return the profile_image
	 */
	public String getProfile_image() {
		return profile_image;
	}

	/**
	 * @param profile_image
	 *            the profile_image to set
	 */
	public void setProfile_image(String profile_image) {
		this.profile_image = profile_image;
	}

	/**
	 * @return the preferences
	 */
	public JSONArray getPreferences() {
		return preferences;
	}

	/**
	 * @param preferences
	 *            the preferences to set
	 */
	public void setPreferences(JSONArray preferences) {
		this.preferences = preferences;
	}
}