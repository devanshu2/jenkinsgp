package com.groupon.go.model;

import java.util.List;

/**
 * @author sachin.gupta
 */
public class GetProfileResponse extends CommonJsonResponse {

	private GetProfileResultModel	result;

	/**
	 * @return the result
	 */
	public GetProfileResultModel getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(GetProfileResultModel result) {
		this.result = result;
	}

	/**
	 * @author sachin.gupta
	 */
	public class GetProfileResultModel {

		private List<UserDetails>		profile;
		private int						cart_count;
		private int						credit;
		private float					coupon_discount_money;
		private float					coupon_discount_percentage;
		private String					credit_per_purchase;
		private List<CategorySelected>	category_selected;
		private List<CategoryModel>		category;
		private String					cat_base_url;

		// for about us screen
		private String					about;
		private String					fb_url;
		private String					twitter_url;
		private String					customer_support_number;
		private String					footer_text;

		/**
		 * @return the profile
		 */
		public List<UserDetails> getProfile() {
			return profile;
		}

		/**
		 * @param profile
		 *            the profile to set
		 */
		public void setProfile(List<UserDetails> profile) {
			this.profile = profile;
		}

		/**
		 * @return the cart_count
		 */
		public int getCart_count() {
			return cart_count;
		}

		/**
		 * @param cart_count
		 *            the cart_count to set
		 */
		public void setCart_count(int cartCount) {
			this.cart_count = cartCount;
		}

		/**
		 * @return the credit
		 */
		public int getCredit() {
			return credit;
		}

		/**
		 * @param credit
		 *            the credit to set
		 */
		public void setCredit(int credit) {
			this.credit = credit;
		}

		/**
		 * @return the coupon_discount_percentage
		 */
		public float getCoupon_discount_percentage() {
			return coupon_discount_percentage;
		}

		/**
		 * @param coupon_discount_percentage
		 *            the coupon_discount_percentage to set
		 */
		public void setCoupon_discount_percentage(float coupon_discount_percentage) {
			this.coupon_discount_percentage = coupon_discount_percentage;
		}

		/**
		 * @return the coupon_discount_money
		 */
		public float getCoupon_discount_money() {
			return coupon_discount_money;
		}

		/**
		 * @param coupon_discount_money
		 *            the coupon_discount_money to set
		 */
		public void setCoupon_discount_money(float coupon_discount_money) {
			this.coupon_discount_money = coupon_discount_money;
		}

		/**
		 * @return the credit_per_purchase
		 */
		public String getCredit_per_purchase() {
			return credit_per_purchase;
		}

		/**
		 * @param credit_per_purchase
		 *            the credit_per_purchase to set
		 */
		public void setCredit_per_purchase(String credit_per_purchase) {
			this.credit_per_purchase = credit_per_purchase;
		}

		/**
		 * @return the category_selected
		 */
		public List<CategorySelected> getCategory_selected() {
			return category_selected;
		}

		/**
		 * @param category_selected
		 *            the category_selected to set
		 */
		public void setCategory_selected(List<CategorySelected> categorySelected) {
			this.category_selected = categorySelected;
		}

		/**
		 * @return the category
		 */
		public List<CategoryModel> getCategory() {
			return category;
		}

		/**
		 * @param category
		 *            the category to set
		 */
		public void setCategory(List<CategoryModel> category) {
			this.category = category;
		}

		/**
		 * @return the cat_base_url
		 */
		public String getCat_base_url() {
			return cat_base_url;
		}

		/**
		 * @param cat_base_url
		 *            the cat_base_url to set
		 */
		public void setCat_base_url(String cat_base_url) {
			this.cat_base_url = cat_base_url;
		}

		/**
		 * @return the about
		 */
		public String getAbout() {
			return about;
		}

		/**
		 * @param about
		 *            the about to set
		 */
		public void setAbout(String about) {
			this.about = about;
		}

		/**
		 * @return the fb_url
		 */
		public String getFb_url() {
			return fb_url;
		}

		/**
		 * @param fb_url
		 *            the fb_url to set
		 */
		public void setFb_url(String fb_url) {
			this.fb_url = fb_url;
		}

		/**
		 * @return the twitter_url
		 */
		public String getTwitter_url() {
			return twitter_url;
		}

		/**
		 * @param twitter_url
		 *            the twitter_url to set
		 */
		public void setTwitter_url(String twitter_url) {
			this.twitter_url = twitter_url;
		}

		/**
		 * @return the customer_support_number
		 */
		public String getCustomer_support_number() {
			return customer_support_number;
		}

		/**
		 * @param customer_support_number
		 *            the customer_support_number to set
		 */
		public void setCustomer_support_number(String customer_support_number) {
			this.customer_support_number = customer_support_number;
		}

		/**
		 * @return the footer_text
		 */
		public String getFooter_text() {
			return footer_text;
		}

		/**
		 * @param footer_text
		 *            the footer_text to set
		 */
		public void setFooter_text(String footer_text) {
			this.footer_text = footer_text;
		}
	}

	/**
	 * @author sachin.gupta
	 * 
	 */
	public class CategorySelected {
		private int	category_id;

		/**
		 * @return the category_id
		 */
		public int getCategory_id() {
			return category_id;
		}

		/**
		 * @param category_id
		 *            the category_id to set
		 */
		public void setCategory_id(int category_id) {
			this.category_id = category_id;
		}
	}
}
