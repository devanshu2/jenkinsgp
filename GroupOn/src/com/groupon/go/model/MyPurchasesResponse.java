package com.groupon.go.model;

import java.util.ArrayList;

import com.groupon.go.model.DealDetailResponse.OfferLocations;

/**
 * @author sachin.gupta
 */
public class MyPurchasesResponse extends CommonJsonResponse {

	private MyPurchasesResult	result;

	/**
	 * @return the result
	 */
	public MyPurchasesResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(MyPurchasesResult result) {
		this.result = result;
	}

	/**
	 * @author sachin.gupta
	 */
	public class MyPurchasesResult {

		private String						deal_img_url;
		private int							radius;
		private ArrayList<MyPurchaseModel>	deal;
		private ArrayList<OfferLocations>	offer_locations;

		/**
		 * @return the deal_img_url
		 */
		public String getDeal_img_url() {
			return deal_img_url;
		}

		/**
		 * @param deal_img_url
		 *            the deal_img_url to set
		 */
		public void setDeal_img_url(String deal_img_url) {
			this.deal_img_url = deal_img_url;
		}

		/**
		 * @return the radius
		 */
		public int getRadius() {
			return radius;
		}

		/**
		 * @param radius
		 *            the radius to set
		 */
		public void setRadius(int radius) {
			this.radius = radius;
		}

		/**
		 * @return the deal
		 */
		public ArrayList<MyPurchaseModel> getDeal() {
			return deal;
		}

		/**
		 * @param deal
		 *            the deal to set
		 */
		public void setDeal(ArrayList<MyPurchaseModel> deal) {
			this.deal = deal;
		}

		/**
		 * @return the offer_locations
		 */
		public ArrayList<OfferLocations> getOffer_locations() {
			return offer_locations;
		}

		/**
		 * @param offer_locations
		 *            the offer_locations to set
		 */
		public void setOffer_locations(ArrayList<OfferLocations> offer_locations) {
			this.offer_locations = offer_locations;
		}

	}
}
