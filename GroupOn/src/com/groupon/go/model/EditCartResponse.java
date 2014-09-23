package com.groupon.go.model;

/**
 * @author vineet.kumar
 */
public class EditCartResponse extends CommonJsonResponse {

	private EditCartResult	result;

	/**
	 * @return the result
	 */
	public EditCartResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(EditCartResult result) {
		this.result = result;
	}

	/**
	 * @author vineet.kumar
	 */
	public static class EditCartResult {
		private int	cart_count;

		/**
		 * @return the cart_count
		 */
		public int getCar_count() {
			return cart_count;
		}

		/**
		 * @param cart_count
		 *            the cart_count to set
		 */
		public void setCart_count(int cartCount) {
			this.cart_count = cartCount;
		}
	}
}
