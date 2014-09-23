package com.kelltontech.social;

import java.lang.ref.WeakReference;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

import com.groupon.go.R;

/**
 * @edited sachin.gupta
 */
public class TwitterApp {

	private static final String		LOG_TAG			= "TwitterApp";
	private static final String		OAUTH_VERIFIER	= "oauth_verifier";
	protected static final String	CALLBACK_URL	= "https://api.twitter.com/oauth/callback";

	private Twitter					mTwitter;
	private RequestToken			mRequestToken;

	private Activity				mActivity;
	private ProgressDialog			mProgressDlg;
	private AuthResponseHandler		mAuthResponseHandler;

	private AuthResponseListener	mAuthResponseListener;
	private TwitterPrefs			mTwitterPrefs;
	private AccessToken				mAccessToken;

	/**
	 * @param activity
	 * @param consumerKey
	 * @param secretKey
	 */
	public TwitterApp(Activity activity, String consumerKey, String secretKey) {
		this.mActivity = activity;

		mProgressDlg = new ProgressDialog(activity);
		mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mProgressDlg.setCancelable(false);

		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(consumerKey);
		builder.setOAuthConsumerSecret(secretKey);
		Configuration configuration = builder.build();
		TwitterFactory factory = new TwitterFactory(configuration);
		mTwitter = factory.getInstance();

		mTwitterPrefs = new TwitterPrefs(mActivity);
		mAccessToken = mTwitterPrefs.getAccessToken();
		if (mAccessToken != null) {
			mTwitter.setOAuthAccessToken(mAccessToken);
		}

		mAuthResponseHandler = new AuthResponseHandler(this);
	}

	/**
	 * @return the mProgressDlg
	 */
	private ProgressDialog getProgressDialog() {
		return mProgressDlg;
	}

	/**
	 * @return the mAuthResponseListener
	 */
	private AuthResponseListener getAuthResponseListener() {
		return mAuthResponseListener;
	}

	/**
	 * @param authResponseListener
	 */
	public void authorize(final AuthResponseListener authResponseListener) {

		mProgressDlg.setMessage("Initializing twitter...");
		mProgressDlg.show();

		mAuthResponseListener = authResponseListener;

		new Thread() {
			@Override
			public void run() {
				String authUrl = "";
				int what = 1;
				try {
					mRequestToken = mTwitter.getOAuthRequestToken(CALLBACK_URL);
					authUrl = mRequestToken.getAuthenticationURL();
					what = 0;
				} catch (Exception e) {
					Log.e(LOG_TAG, "authorize(): " + e);
				}
				mAuthResponseHandler.sendMessage(mAuthResponseHandler.obtainMessage(what, 1, 0, authUrl));
			}
		}.start();
	}

	/**
	 * @param callbackUrl
	 */
	public void processToken(final String callbackUrl) {
		mProgressDlg.setMessage("Processing ...");
		mProgressDlg.show();

		new Thread() {
			@Override
			public void run() {
				int what = 1;
				try {
					Uri uri = Uri.parse(callbackUrl);
					mAccessToken = mTwitter.getOAuthAccessToken(mRequestToken, uri.getQueryParameter(OAUTH_VERIFIER));
					mTwitterPrefs.storeAccessToken(mAccessToken);
					what = 0;
				} catch (Exception e) {
					Log.e(LOG_TAG, "processToken(): " + e);
				}

				mAuthResponseHandler.sendMessage(mAuthResponseHandler.obtainMessage(what, 2, 0));
			}
		}.start();
	}

	/**
	 * @param url
	 */
	private void showLoginDialog(String url) {
		final TwitterDialog.TwDialogListener twDialogListener = new TwitterDialog.TwDialogListener() {
			public void onComplete(String value) {
				processToken(value);
			}

			public void onError(String value) {
				mAuthResponseListener.authResponse(false, false, "Failed opening authorization page");
			}

			public void onBackPressed() {
				mAuthResponseListener.authResponse(false, true, "");
			}
		};

		new TwitterDialog(mActivity, url, twDialogListener).show();
	}

	/**
	 * @param tweetStr
	 * @param tweetRespListener
	 */
	public void tweet(StatusUpdate statusUpdate, final TweetResponseListener tweetRespListener) {
		if (statusUpdate == null ) {
			statusUpdate = new StatusUpdate(mActivity.getString(R.string.app_name));
		}
		if (mAccessToken == null) {
			tweetWithAuth(statusUpdate, tweetRespListener);
		} else {
			tweetWithoutAuth(statusUpdate, tweetRespListener);
		}
	}

	/**
	 * @param tweetStr
	 * @param tweetRespListener
	 */
	private void tweetWithoutAuth(final StatusUpdate statusUpdate, final TweetResponseListener tweetRespListener) {
		mProgressDlg.setMessage("Sending your tweet...");
		mProgressDlg.show();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mTwitter.updateStatus(statusUpdate);
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mProgressDlg.dismiss();
							tweetRespListener.tweetResponse(true, false, "");
						}
					});
				} catch (final TwitterException e) {
					Log.e(LOG_TAG, "updateStatus(): " + e);
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							String errorMsg = "Exception: " + e;
							if (e != null && e.getMessage() != null && e.getMessage().trim().length() > 0) {
								errorMsg = e.getMessage();
							}
							mProgressDlg.dismiss();
							tweetRespListener.tweetResponse(false, false, errorMsg);
						}
					});
				}
			}
		}).start();
	}

	/**
	 * @param tweetStr
	 * @param tweetRespListener
	 */
	private void tweetWithAuth(final StatusUpdate statusUpdate, final TweetResponseListener tweetRespListener) {
		AuthResponseListener authResListener = new AuthResponseListener() {
			public void authResponse(boolean isSuccess, boolean isCancelled, String errorMsg) {
				if (isSuccess) {
					tweetWithoutAuth(statusUpdate, tweetRespListener);
				} else if (isCancelled) {
					tweetRespListener.tweetResponse(false, true, "Twitter login cancelled.");
				} else {
					tweetRespListener.tweetResponse(false, false, errorMsg);
				}
			}
		};
		authorize(authResListener);
	}

	// Nested class and interfaces

	/**
	 * @author sachin.gupta
	 */
	private static class AuthResponseHandler extends Handler {

		private WeakReference<TwitterApp>	wOwnerTwApp;

		public AuthResponseHandler(TwitterApp mTwitterApp) {
			wOwnerTwApp = new WeakReference<TwitterApp>(mTwitterApp);
		}

		@Override
		public void handleMessage(Message msg) {
			TwitterApp twitterApp = wOwnerTwApp.get();
			if (twitterApp == null) {
				return;
			}
			if (twitterApp.getProgressDialog() != null) {
				twitterApp.getProgressDialog().dismiss();
			}
			AuthResponseListener authResponseListener = twitterApp.getAuthResponseListener();
			if (authResponseListener == null) {
				return;
			}

			if (msg.what == 1) {
				if (msg.arg1 == 1)
					authResponseListener.authResponse(false, false, "Error getting request token");
				else
					authResponseListener.authResponse(false, false, "Error getting access token");
			} else {
				if (msg.arg1 == 1)
					twitterApp.showLoginDialog((String) msg.obj);
				else
					authResponseListener.authResponse(true, false, null);
			}
		}
	}

	public interface AuthResponseListener {
		public void authResponse(boolean isSuccess, boolean isCancelled, String errorMsg);
	}

	public interface TweetResponseListener {
		public void tweetResponse(boolean isSuccess, boolean isCancelled, String errorMsg);
	}
}
