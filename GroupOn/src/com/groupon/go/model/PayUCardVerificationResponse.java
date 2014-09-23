package com.groupon.go.model;

public class PayUCardVerificationResponse {

	private String	isDomestic;
	private String	issuingBank;
	private String	cardType;
	private String	cardCategory;

	/**
	 * @return the isDomestic
	 */
	public String getIsDomestic() {
		return isDomestic;
	}

	/**
	 * @param isDomestic
	 *            the isDomestic to set
	 */
	public void setIsDomestic(String isDomestic) {
		this.isDomestic = isDomestic;
	}

	/**
	 * @return the issuingBank
	 */
	public String getIssuingBank() {
		return issuingBank;
	}

	/**
	 * @param issuingBank
	 *            the issuingBank to set
	 */
	public void setIssuingBank(String issuingBank) {
		this.issuingBank = issuingBank;
	}

	/**
	 * @return the cardType
	 */
	public String getCardType() {
		return cardType;
	}

	/**
	 * @param cardType
	 *            the cardType to set
	 */
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getCardCategory() {
		return cardCategory;
	}

	public void setCardCategory(String cardCategory) {
		this.cardCategory = cardCategory;
	}
}
