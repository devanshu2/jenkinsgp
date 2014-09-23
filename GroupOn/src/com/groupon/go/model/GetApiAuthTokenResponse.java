package com.groupon.go.model;

import java.util.List;

import com.kelltontech.utils.StringUtils;

/**
 * @author sachin.gupta
 */
public class GetApiAuthTokenResponse extends CommonJsonResponse {

	private List<ApiAuthTokenResult>	result;

	/**
	 * @return the result
	 */
	public List<ApiAuthTokenResult> getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(List<ApiAuthTokenResult> result) {
		this.result = result;
	}

	/**
	 * @author sachin.gupta
	 */
	public static class ApiAuthTokenResult {

		private String	auth;
		private String	user;
		private String	timestamp;

		/**
		 * @return the auth
		 */
		public String getAuth() {
			return auth;
		}

		/**
		 * @param auth
		 *            the auth to set
		 */
		public void setAuth(String auth) {
			this.auth = auth;
		}

		/**
		 * @return the user
		 */
		public String getUser() {
			return user;
		}

		/**
		 * @param user
		 *            the user to set
		 */
		public void setUser(String user) {
			this.user = user;
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
		 * @return
		 */
		public boolean isValid() {
			boolean authInvalid = StringUtils.isNullOrEmpty(auth);
			boolean userInvalid = StringUtils.isNullOrEmpty(user);
			boolean timestampInvalid = StringUtils.isNullOrEmpty(timestamp);
			return !(authInvalid || userInvalid || timestampInvalid);
		}
	}
}
