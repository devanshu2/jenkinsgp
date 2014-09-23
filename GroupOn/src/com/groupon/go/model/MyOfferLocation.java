package com.groupon.go.model;

import com.kelltontech.model.BaseModel;

/**
 * @author sachin.gupta
 */
public class MyOfferLocation extends BaseModel {

	private int		order_id;
	private int		deal_id;
	private int		offer_id;
	private int		merchant_id;
	private String	opportunity_owner;
	private int		address_id;
	private double	latitude;
	private double	longitude;
	private int		vouchers;
	private long	last_notified_at;

	/**
	 * @return the order_id
	 */
	public int getOrder_id() {
		return order_id;
	}

	/**
	 * @param order_id
	 *            the order_id to set
	 */
	public void setOrder_id(int order_id) {
		this.order_id = order_id;
	}

	/**
	 * @return the deal_id
	 */
	public int getDeal_id() {
		return deal_id;
	}

	/**
	 * @param deal_id
	 *            the deal_id to set
	 */
	public void setDeal_id(int deal_id) {
		this.deal_id = deal_id;
	}

	/**
	 * @return the offer_id
	 */
	public int getOffer_id() {
		return offer_id;
	}

	/**
	 * @param offer_id
	 *            the offer_id to set
	 */
	public void setOffer_id(int offer_id) {
		this.offer_id = offer_id;
	}

	/**
	 * @return the merchant_id
	 */
	public int getMerchant_id() {
		return merchant_id;
	}

	/**
	 * @param merchant_id
	 *            the merchant_id to set
	 */
	public void setMerchant_id(int merchant_id) {
		this.merchant_id = merchant_id;
	}

	/**
	 * @return the merchant_name
	 */
	public String getMerchant_name() {
		return opportunity_owner;
	}

	/**
	 * @param merchant_name
	 *            the merchant_name to set
	 */
	public void setMerchant_name(String merchant_name) {
		this.opportunity_owner = merchant_name;
	}

	/**
	 * @return the address_id
	 */
	public int getAddress_id() {
		return address_id;
	}

	/**
	 * @param address_id
	 *            the address_id to set
	 */
	public void setAddress_id(int address_id) {
		this.address_id = address_id;
	}

	/**
	 * @return the lattitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param lattitude
	 *            the lattitude to set
	 */
	public void setLattitude(double lattitude) {
		this.latitude = lattitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the vouchers
	 */
	public int getVouchers() {
		return vouchers;
	}

	/**
	 * @param vouchers
	 *            the vouchers to set
	 */
	public void setVouchers(int vouchers) {
		this.vouchers = vouchers;
	}

	/**
	 * @return the last_notified_at
	 */
	public long getLast_notified_at() {
		return last_notified_at;
	}

	/**
	 * @param last_notified_at
	 *            the last_notified_at to set
	 */
	public void setLast_notified_at(long last_notified_at) {
		this.last_notified_at = last_notified_at;
	}

	/**
	 * @return
	 */
	public String getCustomId() {
		return order_id + "|" + deal_id + "|" + offer_id + "|" + address_id + ",";
	}
}