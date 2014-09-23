package com.groupon.go.model;

/**
 * @author vineet.kumar
 */
public class PhoneNumber {
	private String	mobile_no;
	private boolean	isGoUser;

	/**
	 * @return the mobile_no
	 */
	public String getMobile_no() {
		return mobile_no;
	}

	/**
	 * @param mobile_no
	 *            the mobile_no to set
	 */
	public void setMobile_no(String mobile_no) {
		this.mobile_no = mobile_no;
	}

	/**
	 * @return the isGoUser
	 */
	public boolean isGoUser() {
		return isGoUser;
	}

	/**
	 * @param isGoUser
	 *            the isGoUser to set
	 */
	public void setGoUser(boolean isGoUser) {
		this.isGoUser = isGoUser;
	}
}