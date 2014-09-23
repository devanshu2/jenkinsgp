package com.groupon.go.ui.activity;

import twitter4j.StatusUpdate;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.facebook.FacebookRequestError;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FacebookDialog.ShareDialogBuilder;
import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.social.FacebookLoginHelper;
import com.kelltontech.social.FacebookLoginHelper.OnFacebookLoginListener;
import com.kelltontech.social.FacebookPostHelper;
import com.kelltontech.social.FacebookPostHelper.FacebookPostResponseListener;
import com.kelltontech.social.FacebookUtils;
import com.kelltontech.social.TwitterApp;
import com.kelltontech.social.TwitterApp.TweetResponseListener;
import com.kelltontech.ui.Events;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.ShareUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author sachin.gupta
 */
public class ShareOptionsActivity extends GroupOnBaseActivity implements OnClickListener {

	private static final String	LOG_TAG	= ShareOptionsActivity.class.getSimpleName();

	private String				mMsgToShare;
	private String				mMsgToShareViaFb;
	private String				mMsgToShareViaTwitter;
	private String				mLinkToShare;
	private String				mDealImageUrlToShare;

	private UiLifecycleHelper	mFbUiCycleHelper;
	private FacebookLoginHelper	mFacebookLoginHelper;

	private int					mDealId;
	private String				mMerchantName;
	private int					mMessageType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_share_options);

		Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
		View layout = (View) findViewById(R.id.linear_share_options_root);
		layout.startAnimation(bottomUp);
		layout.setVisibility(View.VISIBLE);

		mDealId = getIntent().getIntExtra(Constants.EXTRA_DEAL_ID, 0);
		mLinkToShare = ProjectUtils.createLinkToShare(this, mDealId);

		String appName = getString(R.string.app_name);

		mMessageType = getIntent().getIntExtra(Constants.EXTRA_MESSAGE_TYPE, 0);

		mMerchantName = getIntent().getStringExtra(Constants.EXTRA_MERCHANT_NAME);
		String keyword = "bought";
		if (mMessageType == ApiConstants.VALUE_MESSAGE_TYPE_SHARED_DEAL) {
			keyword = "got";
		}

		mMsgToShare = getString(R.string.msg_share_deal_via_more_options, keyword, appName, mLinkToShare);
		mMsgToShareViaFb = getString(R.string.msg_share_deal_via_facebook, keyword, mMerchantName, appName);
		mMsgToShareViaTwitter = getString(R.string.msg_share_deal_via_twitter, keyword, mMerchantName, appName, mLinkToShare);

		if (mMessageType != ApiConstants.VALUE_MESSAGE_TYPE_PURCHASED_DEAL) {
			String baseUrlWithDensityGroupName = GrouponGoPrefs.getDealsImageBaseUrl(this);
			String imagePath = getIntent().getStringExtra(Constants.EXTRA_DEAL_IMAGE_URL);
			mDealImageUrlToShare = ProjectUtils.createImageUrl(baseUrlWithDensityGroupName, imagePath);
		}

		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "Deal Image URL: " + mDealImageUrlToShare);
			Log.i(LOG_TAG, "Generic Share Msg: " + mMsgToShare);
			Log.i(LOG_TAG, "Msg for fb: " + mMsgToShareViaFb);
			Log.i(LOG_TAG, "Msg for twitter: " + mMsgToShareViaTwitter);
		}

		findViewById(R.id.linear_transparent).setOnClickListener(this);
		findViewById(R.id.txv_share_phonebook).setOnClickListener(this);
		findViewById(R.id.txv_share_facebook).setOnClickListener(this);
		findViewById(R.id.txv_share_twitter).setOnClickListener(this);
		findViewById(R.id.txv_share_more_options).setOnClickListener(this);
		findViewById(R.id.txv_share_cancel).setOnClickListener(this);

		mFbUiCycleHelper = new UiLifecycleHelper(this, null);
		mFbUiCycleHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.linear_transparent: {
			finish();
			break;
		}
		case R.id.txv_share_phonebook: {
			startShareOnPhoneBookActivity();
			break;
		}
		case R.id.txv_share_facebook: {
			checkFbLoginAndShareDeal(null);
			break;
		}
		case R.id.txv_share_twitter: {
			shareDealOnTwitter();
			break;
		}
		case R.id.txv_share_more_options: {
			String subject = ProjectUtils.getSubjectToShare(this, mMessageType);
			ShareUtils.sharePlainText(this, getString(R.string.msg_share_deal_via), subject, mMsgToShare);
			break;
		}
		case R.id.txv_share_cancel: {
			finish();
			break;
		}
		}
	}

	/**
	 * checks message type, put extras accordingly and starts activity
	 */
	private void startShareOnPhoneBookActivity() {
		Intent intent = new Intent(this, ShareOnPhnBookActivity.class);
		intent.putExtra(Constants.EXTRA_MESSAGE_TYPE, mMessageType);
		intent.putExtra(Constants.EXTRA_DEAL_ID, mDealId);
		intent.putExtra(Constants.EXTRA_MERCHANT_NAME, mMerchantName);
		intent.putExtra(Constants.EXTRA_CAT_NAME, getIntent().getStringExtra(Constants.EXTRA_CAT_NAME));
		startActivity(intent);
	}

	/**
	 * shows login dialog for twitter
	 */
	private void shareDealOnTwitter() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		TwitterApp twitterApp = new TwitterApp(this, getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));
		TweetResponseListener tweetRespListener = new TweetResponseListener() {
			public void tweetResponse(boolean isSuccess, boolean isCancelled, String errorMsg) {
				if (isSuccess) {
					ToastUtils.showToast(getApplicationContext(), getString(R.string.msg_deal_share_success));
				} else if (isCancelled) {
					ToastUtils.showToast(getApplicationContext(), getString(R.string.msg_twitter_login_cancelled));
				} else {
					if (errorMsg != null && errorMsg.contains("duplicate")) {
						ToastUtils.showToast(getApplicationContext(), getString(R.string.msg_share_twitter_duplicate));
					} else {
						// any error or back
						ToastUtils.showToast(getApplicationContext(), getString(R.string.msg_share_twitter_no_share));
					}
				}
			}
		};
		// if (tweetStr.length() > 140) {
		// tweetStr = tweetStr.substring(0, 137) + "...";
		// }
		StatusUpdate statusUpdate = new StatusUpdate(mMsgToShareViaTwitter);
		twitterApp.tweet(statusUpdate, tweetRespListener);
	}

	/**
	 * initiate log in, if logged out and vice versa
	 */
	private void checkFbLoginAndShareDeal(Object graphUser) {
		String updatedUserName = GrouponGoPrefs.getFbUserName(this);
		if (graphUser instanceof GraphUser) {
			updatedUserName = FacebookUtils.getUserFbName((GraphUser) graphUser);
			GrouponGoPrefs.setFbUserName(this, updatedUserName);
		}
		if (StringUtils.isNullOrEmpty(updatedUserName)) {
			if (mFacebookLoginHelper == null) {
				mFacebookLoginHelper = new FacebookLoginHelper(this, new OnFacebookLoginListener() {
					@Override
					public void onFacebookLoginComplete() {
						FacebookUtils.requestFacebookUser(ShareOptionsActivity.this);
					}
				});
			}
			mFacebookLoginHelper.openFacebookSession();
		} else {
			shareDealOnFacebook();
		}
	}

	/**
	 * 
	 */
	private void shareDealOnFacebook() {
		if (FacebookDialog.canPresentShareDialog(this, FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			ShareDialogBuilder shareDialogBuilder = new ShareDialogBuilder(this);
			shareDialogBuilder.setName(mMsgToShareViaFb);
			shareDialogBuilder.setLink(mLinkToShare);
			if (!StringUtils.isNullOrEmpty(mDealImageUrlToShare)) {
				shareDialogBuilder.setPicture(mDealImageUrlToShare);
			}
			FacebookDialog shareDialog = shareDialogBuilder.build();
			mFbUiCycleHelper.trackPendingDialogCall(shareDialog.present());
		} else {
			Bundle postParams = new Bundle();
			postParams.putString("link", mLinkToShare);
			postParams.putString("name", mMsgToShareViaFb);
			if (!StringUtils.isNullOrEmpty(mDealImageUrlToShare)) {
				postParams.putString("picture", mDealImageUrlToShare);
			}
			FacebookPostResponseListener facebookPostResponseListener = new FacebookPostResponseListener() {
				@Override
				public void onFacebookPostComplete(String postId, FacebookRequestError error) {
					removeProgressDialog();
					if (error != null) {
						ToastUtils.showToast(getApplicationContext(), error.getErrorMessage());
					} else if (StringUtils.isNullOrEmpty(postId)) {
						ToastUtils.showToast(getApplicationContext(), getString(R.string.error_generic_message));
					} else {
						ToastUtils.showToast(getApplicationContext(), getString(R.string.msg_deal_share_success));
					}
				}
			};
			showProgressDialog(getString(R.string.prog_loading));
			FacebookPostHelper.publishStory(this, postParams, facebookPostResponseListener);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mFbUiCycleHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
				ToastUtils.showToast(getApplicationContext(), getString(R.string.error_generic_message));
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
				String gesture = FacebookDialog.getNativeDialogCompletionGesture(data);
				if (gesture != null && gesture.equalsIgnoreCase("post")) {
					ToastUtils.showToast(getApplicationContext(), getString(R.string.msg_deal_share_success));
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mFacebookLoginHelper != null) {
			mFacebookLoginHelper.onActivityStart();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mFbUiCycleHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mFbUiCycleHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		mFbUiCycleHelper.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mFacebookLoginHelper != null) {
			mFacebookLoginHelper.onActivityStop();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mFbUiCycleHelper.onDestroy();
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.REQUEST_FACEBOOK_USER: {
			checkFbLoginAndShareDeal(eventData);
			break;
		}
		}
	}
}
