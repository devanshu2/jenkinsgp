package com.groupon.go.model;

import java.util.ArrayList;

/**
 * @author sachin.gupta
 */
public class DealsListResponse extends CommonJsonResponse {

	private DealsListResultModel	result;

	/**
	 * @return the result
	 */
	public DealsListResultModel getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(DealsListResultModel result) {
		this.result = result;
	}

	/**
	 * @author sachin.gupta
	 */
	public class DealsListResultModel {

		private ArrayList<DealModel>	deal;
		private String					deal_img_url;
		private int						page_size;
		private int						total_number_of_pages;
		private int						total_deals;
		private int						total_vouchers;
		private int						deals_update_interval;

		/**
		 * @return the deal
		 */
		public ArrayList<DealModel> getDeal() {
			return deal;
		}

		/**
		 * @param deal
		 *            the deal to set
		 */
		public void setDeal(ArrayList<DealModel> deal) {
			this.deal = deal;
		}

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
		 * @return the page_size
		 */
		public int getPage_size() {
			return page_size;
		}

		/**
		 * @param page_size
		 *            the page_size to set
		 */
		public void setPage_size(int page_size) {
			this.page_size = page_size;
		}

		/**
		 * @return the total_number_of_pages
		 */
		public int getTotal_number_of_pages() {
			return total_number_of_pages;
		}

		/**
		 * @param total_number_of_pages
		 *            the total_number_of_pages to set
		 */
		public void setTotal_number_of_pages(int total_number_of_pages) {
			this.total_number_of_pages = total_number_of_pages;
		}

		/**
		 * @return the total_deals
		 */
		public int getTotal_deals() {
			return total_deals;
		}

		/**
		 * @param total_deals
		 *            the total_deals to set
		 */
		public void setTotal_deals(int total_deals) {
			this.total_deals = total_deals;
		}

		/**
		 * @return the total_vouchers
		 */
		public int getTotal_vouchers() {
			return total_vouchers;
		}

		/**
		 * @param total_vouchers
		 *            the total_vouchers to set
		 */
		public void setTotal_vouchers(int total_vouchers) {
			this.total_vouchers = total_vouchers;
		}

		/**
		 * @return the deals_update_interval
		 */
		public int getDeals_update_interval() {
			return deals_update_interval;
		}

		/**
		 * @param deals_update_interval
		 *            the deals_update_interval to set
		 */
		public void setDeals_update_interval(int deals_update_interval) {
			this.deals_update_interval = deals_update_interval;
		}
	}
}
