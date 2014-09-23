package com.groupon.go.model;

public class UploadUserImageResponse extends CommonJsonResponse {

	private UploadUserImageResult	result;

	/**
	 * @return the result
	 */
	public UploadUserImageResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(UploadUserImageResult result) {
		this.result = result;
	}

	public static class UploadUserImageResult {
		private String	profile_image;

		/**
		 * @return the profile_image
		 */
		public String getProfile_image() {
			return profile_image;
		}

		/**
		 * @param profile_image
		 *            the profile_image to set
		 */
		public void setProfile_image(String profile_image) {
			this.profile_image = profile_image;
		}
	}
}
