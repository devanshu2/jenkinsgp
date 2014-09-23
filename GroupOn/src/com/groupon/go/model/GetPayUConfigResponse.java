package com.groupon.go.model;

/**
 * @author vineet.kumar
 */
public class GetPayUConfigResponse extends CommonJsonResponse {

	private GetPayUConfigResult	result;

	/**
	 * @return the result
	 */
	public GetPayUConfigResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(GetPayUConfigResult result) {
		this.result = result;
	}

	/**
	 * @author vineet.kumar
	 */
	public static class GetPayUConfigResult {

		private String	salt;
		private String	service_url;
		private String	command;
		private String	hash;
		private String	user_token;
		private String	key;
		private String	command_is_domestic;

		/**
		 * @return the salt
		 */
		public String getSalt() {
			return salt;
		}

		/**
		 * @param salt
		 *            the salt to set
		 */
		public void setSalt(String salt) {
			this.salt = salt;
		}

		/**
		 * @return the service_url
		 */
		public String getService_url() {
			return service_url;
		}

		/**
		 * @param service_url
		 *            the service_url to set
		 */
		public void setService_url(String service_url) {
			this.service_url = service_url;
		}

		/**
		 * @return the command
		 */
		public String getCommand() {
			return command;
		}

		/**
		 * @param command
		 *            the command to set
		 */
		public void setCommand(String command) {
			this.command = command;
		}

		/**
		 * @return the hash
		 */
		public String getHash() {
			return hash;
		}

		/**
		 * @param hash
		 *            the hash to set
		 */
		public void setHash(String hash) {
			this.hash = hash;
		}

		/**
		 * @return the user_token
		 */
		public String getUser_token() {
			return user_token;
		}

		/**
		 * @param user_token
		 *            the user_token to set
		 */
		public void setUser_token(String user_token) {
			this.user_token = user_token;
		}

		/**
		 * @return the key
		 */
		public String getKey() {
			return key;
		}

		/**
		 * @param key
		 *            the key to set
		 */
		public void setKey(String key) {
			this.key = key;
		}

		/**
		 * @return the command_is_domestic
		 */
		public String getCommand_is_domestic() {
			return command_is_domestic;
		}

		/**
		 * @param command_is_domestic
		 *            the command_is_domestic to set
		 */
		public void setCommand_is_domestic(String command_is_domestic) {
			this.command_is_domestic = command_is_domestic;
		}

	}
}
