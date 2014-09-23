package com.groupon.go.model;

import java.util.ArrayList;

/**
 * @author vineet.rajpoot
 */
public class RedeemVoucherResponse extends CommonJsonResponse {

	private RedeemVoucherResult	result;

	/**
	 * @return the result
	 */
	public RedeemVoucherResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(RedeemVoucherResult result) {
		this.result = result;
	}

	/**
	 * @author vineet.rajpoot
	 */
	public static class RedeemVoucherResult {

		private ArrayList<VouchersModel>	vouchers;

		/**
		 * @return the vouchers
		 */
		public ArrayList<VouchersModel> getVouchers() {
			return vouchers;
		}

		/**
		 * @param vouchers
		 *            the vouchers to set
		 */
		public void setVouchers(ArrayList<VouchersModel> vouchers) {
			this.vouchers = vouchers;
		}
	}
}
