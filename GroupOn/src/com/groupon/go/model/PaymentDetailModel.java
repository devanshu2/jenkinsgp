package com.groupon.go.model;

/**
 * @author vineet.rajpoot
 */
public class PaymentDetailModel {

	private int		orderId;
	private String	txnid;
	private String	surl;
	private String	curl;
	private String	furl;
	private String	phone;
	private String	key;
	private String	hash;
	private float	amount;
	private String	email;
	private String	redirectUrl;
	private String	firstname;
	private String	lastname;
	private String	salt;
	private String	pg;
	private String	productinfo;
	private String	user_credentials;
	private int		session_period;
	
	private boolean isSessionExpired;

	/**
	 * @return the orderId
	 */
	public int getOrderId() {
		return orderId;
	}

	/**
	 * @param orderId
	 *            the orderId to set
	 */
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	/**
	 * @return the txnid
	 */
	public String getTxnid() {
		return txnid;
	}

	/**
	 * @param txnid
	 *            the txnid to set
	 */
	public void setTxnid(String txnid) {
		this.txnid = txnid;
	}

	/**
	 * @return the surl
	 */
	public String getSurl() {
		return surl;
	}

	/**
	 * @param surl
	 *            the surl to set
	 */
	public void setSurl(String surl) {
		this.surl = surl;
	}

	/**
	 * @return the curl
	 */
	public String getCurl() {
		return curl;
	}

	/**
	 * @param curl
	 *            the curl to set
	 */
	public void setCurl(String curl) {
		this.curl = curl;
	}

	/**
	 * @return the furl
	 */
	public String getFurl() {
		return furl;
	}

	/**
	 * @param furl
	 *            the furl to set
	 */
	public void setFurl(String furl) {
		this.furl = furl;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone
	 *            the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * @param hash
	 *            the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * @return the amount
	 */
	public float getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the redirectUrl
	 */
	public String getRedirectUrl() {
		return redirectUrl;
	}

	/**
	 * @param redirectUrl
	 *            the redirectUrl to set
	 */
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	/**
	 * @return the firstname
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * @param firstname
	 *            the firstname to set
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	/**
	 * @return the lastname
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * @param lastname
	 *            the lastname to set
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 * @return the salt
	 */
	public String getSalt() {
		return salt;
	}

	/**
	 * @param salt
	 *            the salt to set
	 */
	public void setSalt(String salt) {
		this.salt = salt;
	}

	/**
	 * @return the pg
	 */
	public String getPg() {
		return pg;
	}

	/**
	 * @param pg
	 *            the pg to set
	 */
	public void setPg(String pg) {
		this.pg = pg;
	}

	/**
	 * @return the productinfo
	 */
	public String getProductinfo() {
		return productinfo;
	}

	/**
	 * @param productinfo
	 *            the productinfo to set
	 */
	public void setProductinfo(String productinfo) {
		this.productinfo = productinfo;
	}

	/**
	 * @return the user_credentials
	 */
	public String getUser_credentials() {
		return user_credentials;
	}

	/**
	 * @param user_credentials
	 *            the user_credentials to set
	 */
	public void setUser_credentials(String user_credentials) {
		this.user_credentials = user_credentials;
	}

	/**
	 * @return the session_period
	 */
	public int getSession_period() {
		return session_period;
	}

	/**
	 * @param session_period
	 *            the session_period to set
	 */
	public void setSession_period(int session_period) {
		this.session_period = session_period;
	}

	/**
	 * @return the isSessionExpired
	 */
	public boolean isSessionExpired() {
		return isSessionExpired;
	}

	/**
	 * @param isSessionExpired the isSessionExpired to set
	 */
	public void setSessionExpired(boolean isSessionExpired) {
		this.isSessionExpired = isSessionExpired;
	}
}
