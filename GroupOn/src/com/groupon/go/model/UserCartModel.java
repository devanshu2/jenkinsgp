package com.groupon.go.model;

import java.util.ArrayList;

/**
 * @author vineet.kumar
 */
public class UserCartModel {

	private int							deal_id;
	private String						deal_title;
	private String						opportunity_owner;
	private String						cat_name;
	private ArrayList<CartOfferModel>	offers;

	/**
	 * @return the deal_id
	 */
	public int getDeal_id() {
		return deal_id;
	}

	/**
	 * @param deal_id
	 *            the deal_id to set
	 */
	public void setDeal_id(int deal_id) {
		this.deal_id = deal_id;
	}

	/**
	 * @return the deal_title
	 */
	public String getDeal_title() {
		return deal_title;
	}

	/**
	 * @param deal_title
	 *            the deal_title to set
	 */
	public void setDeal_title(String deal_title) {
		this.deal_title = deal_title;
	}

	/**
	 * @return the offer_list
	 */
	public ArrayList<CartOfferModel> getOffer_list() {
		return offers;
	}

	/**
	 * @param offer_list
	 *            the offer_list to set
	 */
	public void setOffer_list(ArrayList<CartOfferModel> offer_list) {
		this.offers = offer_list;
	}

	/**
	 * @return the merchant_name
	 */
	public String getMerchant_name() {
		return opportunity_owner;
	}

	/**
	 * @param merchant_name
	 *            the merchant_name to set
	 */
	public void setMerchant_name(String merchant_name) {
		this.opportunity_owner = merchant_name;
	}

	/**
	 * @return the cat_name
	 */
	public String getCat_name() {
		return cat_name;
	}

	/**
	 * @param cat_name
	 *            the cat_name to set
	 */
	public void setCat_name(String cat_name) {
		this.cat_name = cat_name;
	}

	/**
	 * @author vineet.kumar
	 */
	public static class CartOfferModel {

		private String	offer_description;
		private int		offer_quantity;
		private float	offer_price;
		private int		offer_id;
		private float	total_price;
		private float	offer_discount_price;
		private int		offer_weight;

		/**
		 * @return the total_price
		 */
		public float getTotal_price() {
			return total_price;
		}

		/**
		 * @param total_price
		 *            the total_price to set
		 */
		public void setTotal_price(float total_price) {
			this.total_price = total_price;
		}

		/**
		 * @return the offer_id
		 */
		public int getOffer_id() {
			return offer_id;
		}

		/**
		 * @param offer_id
		 *            the offer_id to set
		 */
		public void setOffer_id(int offer_id) {
			this.offer_id = offer_id;
		}

		/**
		 * @return the offer_desc
		 */
		public String getOffer_desc() {
			return offer_description;
		}

		/**
		 * @param offer_desc
		 *            the offer_desc to set
		 */
		public void setOffer_desc(String offer_desc) {
			this.offer_description = offer_desc;
		}

		/**
		 * @return the offer_count
		 */
		public int getOffer_count() {
			return offer_quantity;
		}

		/**
		 * @param offer_count
		 *            the offer_count to set
		 */
		public void setOffer_count(int offer_count) {
			this.offer_quantity = offer_count;
		}

		/**
		 * @return the offer_price
		 */
		public float getOffer_price() {
			return offer_price;
		}

		/**
		 * @param offer_price
		 *            the offer_price to set
		 */
		public void setOffer_price(float offer_price) {
			this.offer_price = offer_price;
		}

		/**
		 * @return the offer_discount_price
		 */
		public float getOffer_discount_price() {
			return offer_discount_price;
		}

		/**
		 * @param offer_discount_price
		 *            the offer_discount_price to set
		 */
		public void setOffer_discount_price(float offer_discount_price) {
			this.offer_discount_price = offer_discount_price;
		}

		/**
		 * @return the offer_weight
		 */
		public int getOffer_weight() {
			return offer_weight;
		}

		/**
		 * @param offer_weight
		 *            the offer_weight to set
		 */
		public void setOffer_weight(int offer_weight) {
			this.offer_weight = offer_weight;
		}
	}
}
