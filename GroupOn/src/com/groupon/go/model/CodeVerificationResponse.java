package com.groupon.go.model;

/**
 * @author vineet.kumar
 */
public class CodeVerificationResponse extends CommonJsonResponse {

	private CodeVerificationResultModel	result;

	/**
	 * @return the result
	 */
	public CodeVerificationResultModel getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(CodeVerificationResultModel result) {
		this.result = result;
	}

	/**
	 * @author vineet.kumar
	 */
	public static class CodeVerificationResultModel {

		private int		user_id;
		private String	coupon_code;
		private boolean	is_existing_user;

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
		 * @return the is_existing_user
		 */
		public boolean is_existing_user() {
			return is_existing_user;
		}

		/**
		 * @param is_existing_user
		 *            the is_existing_user to set
		 */
		public void setExisting_user(boolean is_existing_user) {
			this.is_existing_user = is_existing_user;
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
	}
}
