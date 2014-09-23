package com.groupon.go.model;

/**
 * @author vineet.kumar
 */
public class PaymentStatusResponse extends CommonJsonResponse {

	private PaymentStatusResult	result;

	/**
	 * @return the result
	 */
	public PaymentStatusResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(PaymentStatusResult result) {
		this.result = result;
	}

	/**
	 * @author vineet.kumar
	 */
	public class PaymentStatusResult {
		private String	status;

		/**
		 * @return the status
		 */
		public String getStatus() {
			return status;
		}

		/**
		 * @param status
		 *            the status to set
		 */
		public void setStatus(String status) {
			this.status = status;
		}
	}
}
