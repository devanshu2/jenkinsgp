/**
 * 
 */
package com.groupon.go.model;

/**
 * @author sachin.gupta
 * 
 */
public class CountryModel {

	private int		country_id;
	private String	country_name;
	private String	country_code;
	private String	flag_img_url;

	/**
	 * @return the country_id
	 */
	public int getCountry_id() {
		return country_id;
	}

	/**
	 * @param country_id
	 *            the country_id to set
	 */
	public void setCountry_id(int country_id) {
		this.country_id = country_id;
	}

	/**
	 * @return the country_name
	 */
	public String getCountry_name() {
		return country_name;
	}

	/**
	 * @param country_name
	 *            the country_name to set
	 */
	public void setCountry_name(String country_name) {
		this.country_name = country_name;
	}

	/**
	 * @return the country_code
	 */
	public String getCountry_code() {
		return country_code;
	}

	/**
	 * @param country_code
	 *            the country_code to set
	 */
	public void setCountry_code(String country_code) {
		this.country_code = country_code;
	}

	/**
	 * @return the flag_img_url
	 */
	public String getFlag_img_url() {
		return flag_img_url;
	}

	/**
	 * @param flag_img_url the flag_img_url to set
	 */
	public void setFlag_img_url(String flag_img_url) {
		this.flag_img_url = flag_img_url;
	}
}
