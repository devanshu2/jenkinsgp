package com.kelltontech.social;

import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @edited sachin.gupta
 */
public class TwitterPrefs {

	private SharedPreferences	mSharedPrefs;

	private static final String	TWITTER_USER_PREFS	= "twitter_user_prefs";
	private static final String	TWITTER_USER_TOKEN	= "twitter_user_token";
	private static final String	TWITTER_USER_SECRET	= "twitter_user_secret";
	private static final String	TWITTER_USER_ID		= "twitter_user_id";
	private static final String	TWITTER_SCREEN_NAME	= "twitter_screen_name";

	/**
	 * @param context
	 */
	public TwitterPrefs(Context context) {
		mSharedPrefs = context.getSharedPreferences(TWITTER_USER_PREFS, Context.MODE_PRIVATE);
	}

	/**
	 * @param accessToken
	 *            or null to clear pre-stored token
	 */
	public void storeAccessToken(AccessToken accessToken) {
		if (accessToken == null) {
			accessToken = new AccessToken("", "", 0);
		}
		Editor editor = mSharedPrefs.edit();
		editor.putString(TWITTER_USER_TOKEN, accessToken.getToken());
		editor.putString(TWITTER_USER_SECRET, accessToken.getTokenSecret());
		editor.putLong(TWITTER_USER_ID, accessToken.getUserId());
		editor.putString(TWITTER_SCREEN_NAME, accessToken.getScreenName());
		editor.apply();
	}

	/**
	 * @return
	 */
	public AccessToken getAccessToken() {
		String token = mSharedPrefs.getString(TWITTER_USER_TOKEN, null);
		String tokenSecret = mSharedPrefs.getString(TWITTER_USER_SECRET, null);
		long userId = mSharedPrefs.getLong(TWITTER_USER_ID, 0);
		if (userId == 0) {
			return null;
		}
		try {
			return new AccessToken(token, tokenSecret, userId);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return
	 */
	public String getScreenName() {
		return mSharedPrefs.getString(TWITTER_SCREEN_NAME, "");
	}
}
