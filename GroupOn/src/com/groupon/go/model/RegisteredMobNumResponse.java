package com.groupon.go.model;

import java.util.ArrayList;

/**
 * @author vineet.kumar
 */
public class RegisteredMobNumResponse extends CommonJsonResponse {

	private RegisterMobNumResult	result;

	/**
	 * @return the result
	 */
	public RegisterMobNumResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(RegisterMobNumResult result) {
		this.result = result;
	}

	/**
	 * @author vineet.kumar
	 */
	public class RegisterMobNumResult {
		private ArrayList<PhoneNumber>	registered_numbers;

		/**
		 * @return the registeredNumbers
		 */
		public ArrayList<PhoneNumber> getRegistered_numbers() {
			return registered_numbers;
		}

		/**
		 * @param registered_numbers
		 *            the registered_numbers to set
		 */
		public void setRegistered_numbers(ArrayList<PhoneNumber> registered_numbers) {
			this.registered_numbers = registered_numbers;
		}
	}
}
