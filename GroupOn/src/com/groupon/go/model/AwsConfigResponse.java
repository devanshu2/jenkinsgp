package com.groupon.go.model;

/**
 * @author vineet.kumar model has configurationData related to upload image on
 *         s3 server.
 */
public class AwsConfigResponse extends CommonJsonResponse {

	private AwsConfigResult	result;

	/**
	 * @return the result
	 */
	public AwsConfigResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(AwsConfigResult result) {
		this.result = result;
	}

	/**
	 * @author vineet.kumar
	 */
	public static class AwsConfigResult {

		private String	AWSAccessKeyId;
		private String	acl;
		private String	success_action_redirect;
		private String	form_action;
		private String	policy;
		private String	signature;
		private String	key;

		private String	file_path;
		private byte[]	file_data;

		/**
		 * @return the aWSAccessKeyId
		 */
		public String getAWSAccessKeyId() {
			return AWSAccessKeyId;
		}

		/**
		 * @param aWSAccessKeyId
		 *            the aWSAccessKeyId to set
		 */
		public void setAWSAccessKeyId(String aWSAccessKeyId) {
			AWSAccessKeyId = aWSAccessKeyId;
		}

		/**
		 * @return the acl
		 */
		public String getAcl() {
			return acl;
		}

		/**
		 * @param acl
		 *            the acl to set
		 */
		public void setAcl(String acl) {
			this.acl = acl;
		}

		/**
		 * @return the success_action_redirect
		 */
		public String getSuccess_action_redirect() {
			return success_action_redirect;
		}

		/**
		 * @param success_action_redirect
		 *            the success_action_redirect to set
		 */
		public void setSuccess_action_redirect(String success_action_redirect) {
			this.success_action_redirect = success_action_redirect;
		}

		/**
		 * @return the form_action
		 */
		public String getForm_action() {
			return form_action;
		}

		/**
		 * @param form_action
		 *            the form_action to set
		 */
		public void setForm_action(String form_action) {
			this.form_action = form_action;
		}

		/**
		 * @return the policy
		 */
		public String getPolicy() {
			return policy;
		}

		/**
		 * @param policy
		 *            the policy to set
		 */
		public void setPolicy(String policy) {
			this.policy = policy;
		}

		/**
		 * @return the signature
		 */
		public String getSignature() {
			return signature;
		}

		/**
		 * @param signature
		 *            the signature to set
		 */
		public void setSignature(String signature) {
			this.signature = signature;
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
		 * @return the file_path
		 */
		public String getFile_path() {
			return file_path;
		}

		/**
		 * @param file_path
		 *            the file_path to set
		 */
		public void setFile_path(String file_path) {
			this.file_path = file_path;
		}

		/**
		 * @return the file_data
		 */
		public byte[] getFile_data() {
			return file_data;
		}

		/**
		 * @param file_data the file_data to set
		 */
		public void setFile_data(byte[] file_data) {
			this.file_data = file_data;
		}

	}

}
