package com.groupon.go.model;

import java.util.List;

/**
 * @author sachin.gupta
 */
public class CityListResponse extends CommonJsonResponse {

	private CityListResultModel	result;

	/**
	 * @return the result
	 */
	public CityListResultModel getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(CityListResultModel result) {
		this.result = result;
	}

	/**
	 * @author sachin.gupta
	 */
	public class CityListResultModel {

		private List<CityModel>	city;

		/**
		 * @return the city
		 */
		public List<CityModel> getCity() {
			return city;
		}

		/**
		 * @param city
		 *            the city to set
		 */
		public void setCity(List<CityModel> city) {
			this.city = city;
		}
	}
}
