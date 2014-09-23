package com.groupon.go.model;

public class CreateOrderResponse extends CommonJsonResponse {

	private PaymentDetailModel result;

	/**
	 * @return the result
	 */
	public PaymentDetailModel getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(PaymentDetailModel result) {
		this.result = result;
	}
	
}
