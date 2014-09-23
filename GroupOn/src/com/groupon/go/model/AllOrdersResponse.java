package com.groupon.go.model;

import java.util.List;

/**
 * @author sachin.gupta
 */
public class AllOrdersResponse extends CommonJsonResponse {

	private AllOrdersResult	result;

	/**
	 * @return the result
	 */
	public AllOrdersResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(AllOrdersResult result) {
		this.result = result;
	}

	/**
	 * @author sachin.gupta
	 */
	public static class AllOrdersResult {

		private List<MyOfferLocation>	added;
		private List<MyOfferLocation>	updated;
		private List<MyOfferLocation>	deleted;
		private String						timestamp;
		private double						radius;

		/**
		 * @return the added
		 */
		public List<MyOfferLocation> getAdded() {
			return added;
		}

		/**
		 * @param added
		 *            the added to set
		 */
		public void setAdded(List<MyOfferLocation> added) {
			this.added = added;
		}

		/**
		 * @return the updated
		 */
		public List<MyOfferLocation> getUpdated() {
			return updated;
		}

		/**
		 * @param updated
		 *            the updated to set
		 */
		public void setUpdated(List<MyOfferLocation> updated) {
			this.updated = updated;
		}

		/**
		 * @return the deleted
		 */
		public List<MyOfferLocation> getDeleted() {
			return deleted;
		}

		/**
		 * @param deleted
		 *            the deleted to set
		 */
		public void setDeleted(List<MyOfferLocation> deleted) {
			this.deleted = deleted;
		}

		/**
		 * @return the timestamp
		 */
		public String getTimestamp() {
			return timestamp;
		}

		/**
		 * @param timestamp
		 *            the timestamp to set
		 */
		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		/**
		 * @return the radius
		 */
		public double getRadius() {
			return radius;
		}

		/**
		 * @param radius
		 *            the radius to set
		 */
		public void setRadius(double radius) {
			this.radius = radius;
		}
	}
}
