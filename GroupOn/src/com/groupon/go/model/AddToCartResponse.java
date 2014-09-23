package com.groupon.go.model;

import java.util.ArrayList;

public class AddToCartResponse extends CommonJsonResponse {

	private AddToCartResult	result;

	/**
	 * @return the result
	 */
	public AddToCartResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(AddToCartResult result) {
		this.result = result;
	}

	public class AddToCartResult {
		private ArrayList<CartModel>	cart;
		private int						cart_count;

		/**
		 * @return the cart
		 */
		public ArrayList<CartModel> getCart() {
			return cart;
		}

		/**
		 * @param cart
		 *            the cart to set
		 */
		public void setCart(ArrayList<CartModel> cart) {
			this.cart = cart;
		}

		/**
		 * @return the cart_count
		 */
		public int getCart_count() {
			return cart_count;
		}

		/**
		 * @param cart_count the cart_count to set
		 */
		public void setCart_count(int cartCount) {
			this.cart_count = cartCount;
		}
	}
}
