package com.groupon.go.model;

/**
 * 
 * @author vineet.kumar
 *
 */
public class UserCardResponse {
	private int		status;
	private String	msg;
	private String	cardToken;
	private String	card_number;
	private String	card_label;
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
	/**
	 * @return the cardToken
	 */
	public String getCardToken() {
		return cardToken;
	}
	/**
	 * @param cardToken the cardToken to set
	 */
	public void setCardToken(String cardToken) {
		this.cardToken = cardToken;
	}
	/**
	 * @return the card_number
	 */
	public String getCard_number() {
		return card_number;
	}
	/**
	 * @param card_number the card_number to set
	 */
	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}
	/**
	 * @return the card_label
	 */
	public String getCard_label() {
		return card_label;
	}
	/**
	 * @param card_label the card_label to set
	 */
	public void setCard_label(String card_label) {
		this.card_label = card_label;
	}

	

}
