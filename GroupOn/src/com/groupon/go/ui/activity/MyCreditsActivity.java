package com.groupon.go.ui.activity;

import twitter4j.StatusUpdate;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FacebookDialog.ShareDialogBuilder;
import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.UserProfileController;
import com.groupon.go.model.GetProfileResponse.GetProfileResultModel;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
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
public class MyCreditsActivity extends DrawerBaseActivity implements OnClickListener {

	private static final String		LOG_TAG	= MyCreditsActivity.class.getSimpleName();

	private UserProfileController	mUserProfileController;

	private int						mMessageType;
	private String					mLinkToShare;
	private String					mMsgToShare;
	private String					mMsgToShareViaFb;
	private String					mMsgToShareViaTwitter;

	private UiLifecycleHelper		mFbUiCycleHelper;
	private FacebookLoginHelper		mFacebookLoginHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_credits);

		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.screen_header_share_and_earn));
		initializeDrawer();

		findViewById(R.id.img_fb_share).setOnClickListener(this);
		findViewById(R.id.img_twt_share).setOnClickListener(this);
		findViewById(R.id.img_phnbk_share).setOnClickListener(this);
		findViewById(R.id.img_msg_share).setOnClickListener(this);
		findViewById(R.id.img_email_share).setOnClickListener(this);

		showProgressDialog(getString(R.string.prog_loading));
		mUserProfileController = new UserProfileController(this, this);
		mUserProfileController.getData(ApiConstants.GET_PROFILE_FROM_CACHE, null);

		mFbUiCycleHelper = new UiLifecycleHelper(this, null);
		mFbUiCycleHelper.onCreate(savedInstanceState);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_PROFILE_FROM_CACHE: {
			if (serviceResponse.getResponseObject() instanceof GetProfileResultModel) {
				setResposeDataOnUi((GetProfileResultModel) serviceResponse.getResponseObject());
			}
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				mUserProfileController.getData(ApiConstants.GET_PROFILE_FROM_SERVER, false);
			} else {
				removeProgressDialog();
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		case ApiConstants.GET_PROFILE_FROM_SERVER: {
			removeProgressDialog();
			if (serviceResponse.getResponseObject() instanceof GetProfileResultModel) {
				setResposeDataOnUi((GetProfileResultModel) serviceResponse.getResponseObject());
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (ConnectivityUtils.isNetworkEnabled(this)) {
			showProgressDialog(getString(R.string.prog_loading));
			mUserProfileController.getData(ApiConstants.GET_PROFILE_FROM_SERVER, false);
		} else {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			mUserProfileController.getData(ApiConstants.GET_PROFILE_FROM_CACHE, null);
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void setResposeDataOnUi(GetProfileResultModel result) {

		((TextView) findViewById(R.id.txv_value_my_credits)).setText("" + StringUtils.getFormatDecimalAmount(result.getCredit()));

		String myUniqueCode = GrouponGoPrefs.getSavedUserDetails(this).getCoupon_code();
		String couponDiscText = ProjectUtils.getCouponDiscountText(this, false);
		String creditsPerPurchase = result.getCredit_per_purchase();
		String appName = getString(R.string.app_name);

		TextView txvShareAndEarnMsg = (TextView) findViewById(R.id.txv_credit_msg);

		if (StringUtils.isNullOrEmpty(myUniqueCode) || StringUtils.isNullOrEmpty(couponDiscText) || StringUtils.isNullOrEmpty(creditsPerPurchase)) {
			txvShareAndEarnMsg.setVisibility(View.INVISIBLE);
		} else {
			String creditMsg = getString(R.string.msg_my_credit_screen, myUniqueCode, couponDiscText, appName, creditsPerPurchase);
			txvShareAndEarnMsg.setText(creditMsg);
		}

		mMessageType = ApiConstants.VALUE_MESSAGE_TYPE_UNIQUE_CODE;
		mLinkToShare = ProjectUtils.createLinkToShare(this, 0);

		mMsgToShare = getString(R.string.msg_share_my_promo_code_via_more_options, myUniqueCode, couponDiscText, appName, mLinkToShare);
		mMsgToShareViaFb = getString(R.string.msg_share_my_promo_code_via_facebook, myUniqueCode, appName, couponDiscText);
		mMsgToShareViaTwitter = getString(R.string.msg_share_my_promo_code_via_twitter, myUniqueCode, appName, couponDiscText, mLinkToShare);

		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "Share via More Options- " + mMsgToShare);
			Log.i(LOG_TAG, "Share via fb- " + mMsgToShareViaFb);
			Log.i(LOG_TAG, "Share via twitter- " + mMsgToShareViaTwitter);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.img_fb_share: {
			checkFbLoginAndShareDeal(null);
			break;
		}
		case R.id.img_twt_share: {
			shareDealOnTwitter();
			break;
		}
		case R.id.img_msg_share:
		case R.id.img_email_share: {
			String subject = ProjectUtils.getSubjectToShare(this, mMessageType);
			ShareUtils.sharePlainText(this, getString(R.string.screen_header_share_and_earn), subject, mMsgToShare);
			break;
		}
		case R.id.img_phnbk_share: {
			startShareOnPhoneBookActivity();
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
					ToastUtils.showToast(getApplicationContext(), getString(R.string.msg_code_share_success));
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
						FacebookUtils.requestFacebookUser(MyCreditsActivity.this);
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
			FacebookDialog shareDialog = shareDialogBuilder.build();
			mFbUiCycleHelper.trackPendingDialogCall(shareDialog.present());
		} else {
			Bundle postParams = new Bundle();
			postParams.putString("link", mLinkToShare);
			if (mMessageType == ApiConstants.VALUE_MESSAGE_TYPE_SHARE_APP || mMessageType == ApiConstants.VALUE_MESSAGE_TYPE_UNIQUE_CODE) {
				postParams.putString("name", mMsgToShare);
			} else {
				postParams.putString("name", mMsgToShareViaFb);
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
						ToastUtils.showToast(getApplicationContext(), getString(R.string.msg_code_share_success));
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
					ToastUtils.showToast(getApplicationContext(), getString(R.string.msg_code_share_success));
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
