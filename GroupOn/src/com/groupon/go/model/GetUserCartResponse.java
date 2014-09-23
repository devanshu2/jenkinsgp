package com.groupon.go.model;

import java.util.ArrayList;

/**
 * @author vineet.kumar
 */
public class GetUserCartResponse extends CommonJsonResponse {

	private GetUserCartResult	result;

	/**
	 * @return the result
	 */
	public GetUserCartResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(GetUserCartResult result) {
		this.result = result;
	}

	public static class GetUserCartResult {

		private ArrayList<UserCartModel>	deals;
		private float						total;
		private float						total_original;
		private String						coupon_code;
		private String						show_message;
		private float						credit;
		private float						credit_used;
		private int							cart_count;
		private boolean						credit_status;

		/**
		 * @return the total_original
		 */
		public float getTotal_original() {
			return total_original;
		}

		/**
		 * @param total_original
		 *            the total_original to set
		 */
		public void setTotal_original(float total_original) {
			this.total_original = total_original;
		}

		/**
		 * @return the total
		 */
		public float getTotal() {
			return total;
		}

		/**
		 * @param total
		 *            the total to set
		 */
		public void setTotal(float total) {
			this.total = total;
		}

		/**
		 * @return the deals
		 */
		public ArrayList<UserCartModel> getDeals() {
			return deals;
		}

		/**
		 * @param deals
		 *            the deals to set
		 */
		public void setDeals(ArrayList<UserCartModel> deals) {
			this.deals = deals;
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

		public String getShow_message() {
			return show_message;
		}

		public void setShow_message(String show_message) {
			this.show_message = show_message;
		}

		public float getCredit() {
			return credit;
		}

		public void setCredit(float credit) {
			this.credit = credit;
		}

		public float getCredit_used() {
			return credit_used;
		}

		public void setCredit_used(float credit_used) {
			this.credit_used = credit_used;
		}

		public int getCart_count() {
			return cart_count;
		}

		public void setCart_count(int cart_count) {
			this.cart_count = cart_count;
		}

		/**
		 * @return the credit_status
		 */
		public boolean isCredit_status() {
			return credit_status;
		}

		/**
		 * @param credit_status
		 *            the credit_status to set
		 */
		public void setCredit_status(boolean credit_status) {
			this.credit_status = credit_status;
		}
	}
}
