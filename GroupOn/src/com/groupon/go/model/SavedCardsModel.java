package com.groupon.go.model;

public class SavedCardsModel {

	private String	name;
	private String	card_number;
	private String	card_brand;
	private String	card_mode;
	private String	card_token;
	private String	expiry_year;
	private String	expiry_month;
	private String	card_name;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the card_number
	 */
	public String getCard_number() {
		return card_number;
	}

	/**
	 * @param card_number
	 *            the card_number to set
	 */
	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}

	/**
	 * @return the card_brand
	 */
	public String getCard_brand() {
		return card_brand;
	}

	/**
	 * @param card_brand
	 *            the card_brand to set
	 */
	public void setCard_brand(String card_brand) {
		this.card_brand = card_brand;
	}

	/**
	 * @return the card_mode
	 */
	public String getCard_mode() {
		return card_mode;
	}

	/**
	 * @param card_mode
	 *            the card_mode to set
	 */
	public void setCard_mode(String card_mode) {
		this.card_mode = card_mode;
	}

	/**
	 * @return the card_token
	 */
	public String getCard_token() {
		return card_token;
	}

	/**
	 * @param card_token
	 *            the card_token to set
	 */
	public void setCard_token(String card_token) {
		this.card_token = card_token;
	}

	/**
	 * @return the expiry_year
	 */
	public String getExpiry_year() {
		return expiry_year;
	}

	/**
	 * @param expiry_year
	 *            the expiry_year to set
	 */
	public void setExpiry_year(String expiry_year) {
		this.expiry_year = expiry_year;
	}

	/**
	 * @return the expiry_month
	 */
	public String getExpiry_month() {
		return expiry_month;
	}

	/**
	 * @param expiry_month
	 *            the expiry_month to set
	 */
	public void setExpiry_month(String expiry_month) {
		this.expiry_month = expiry_month;
	}

	public String getCard_name() {
		return card_name;
	}

	public void setCard_name(String card_name) {
		this.card_name = card_name;
	}

}
