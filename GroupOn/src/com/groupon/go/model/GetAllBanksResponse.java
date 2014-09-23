package com.groupon.go.model;

import java.util.ArrayList;

public class GetAllBanksResponse extends CommonJsonResponse {

	private GetAllBankResult	result;

	/**
	 * @return the result
	 */
	public GetAllBankResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(GetAllBankResult result) {
		this.result = result;
	}

	public static class GetAllBankResult {

		private ArrayList<BankDetailModel>	banks;

		/**
		 * @return the banks
		 */
		public ArrayList<BankDetailModel> getBanks() {
			return banks;
		}

		/**
		 * @param banks
		 *            the banks to set
		 */
		public void setBanks(ArrayList<BankDetailModel> banks) {
			this.banks = banks;
		}
	}

	public static class BankDetailModel {
		private String	name;
		private String	code;
		private boolean isSelected = false;

		/**
		 * @return the isSelected
		 */
		public boolean isSelected() {
			return isSelected;
		}

		/**
		 * @param isSelected the isSelected to set
		 */
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the code
		 */
		public String getCode() {
			return code;
		}

		/**
		 * @param code
		 *            the code to set
		 */
		public void setCode(String code) {
			this.code = code;
		}
	}
}
