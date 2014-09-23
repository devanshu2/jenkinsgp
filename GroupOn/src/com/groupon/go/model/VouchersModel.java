package com.groupon.go.model;

import java.util.ArrayList;

public class VouchersModel {

	private int					offer_id;
	private ArrayList<String>	voucher_codes;

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
	 * @return the voucher_codes
	 */
	public ArrayList<String> getVoucher_codes() {
		return voucher_codes;
	}

	/**
	 * @param voucher_codes
	 *            the voucher_codes to set
	 */
	public void setVoucher_codes(ArrayList<String> voucher_codes) {
		this.voucher_codes = voucher_codes;
	}
}
