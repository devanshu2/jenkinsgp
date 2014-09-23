package com.groupon.go.model;

import java.util.List;


/**
 * @author sachin.gupta
 */
public class CountryListResponse extends CommonJsonResponse {

	private CountryListResultModel	result;

	/**
	 * @return the result
	 */
	public CountryListResultModel getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(CountryListResultModel result) {
		this.result = result;
	}

	/**
	 * @author sachin.gupta
	 */
	public class CountryListResultModel {

		private List<CountryModel>	country;

		/**
		 * @return the country
		 */
		public List<CountryModel> getCountry() {
			return country;
		}

		/**
		 * @param country
		 *            the country to set
		 */
		public void setCountry(List<CountryModel> country) {
			this.country = country;
		}
	}
}
