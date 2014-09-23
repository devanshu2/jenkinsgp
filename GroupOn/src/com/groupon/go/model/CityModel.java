package com.groupon.go.model;

/**
 * @author sachin.gupta
 */
public class CityModel {

	private int		city_id;
	private String	city_name;
	private float	city_lat;
	private float	city_long;

	/**
	 * @return the city_id
	 */
	public int getCity_id() {
		return city_id;
	}

	/**
	 * @param city_id
	 *            the city_id to set
	 */
	public void setCity_id(int city_id) {
		this.city_id = city_id;
	}

	/**
	 * @return the city_name
	 */
	public String getCity_name() {
		return city_name;
	}

	/**
	 * @param city_name
	 *            the city_name to set
	 */
	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	/**
	 * @return the city_lat
	 */
	public float getCity_lat() {
		return city_lat;
	}

	/**
	 * @param city_lat
	 *            the city_lat to set
	 */
	public void setCity_lat(float city_lat) {
		this.city_lat = city_lat;
	}

	/**
	 * @return the city_long
	 */
	public float getCity_long() {
		return city_long;
	}

	/**
	 * @param city_long
	 *            the city_long to set
	 */
	public void setCity_long(float city_long) {
		this.city_long = city_long;
	}
}
