package com.kelltontech.social;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.util.Log;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.groupon.go.BuildConfig;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * Facebook login helper class to check session.
 */
public class FacebookLoginHelper {

	private final static String		LOG_TAG						= "FacebookLoginHelper";

	private final static String		ERR_MSG_FB_LOGIN_CANCELLED	= "Facebook Login cancelled.";
	private final static String		ERR_MSG_FB_LOGIN_FAILED		= "Facebook Login failed.";

	private final List<String>		FB_ALL_PERMISSIONS			= Arrays.asList();
	private final List<String>		FB_READ_PERMISSIONS			= Arrays.asList();

	private Activity				mActivity;
	private OnFacebookLoginListener	mOnFacebookLoginListener;
	private Session.StatusCallback	mFbSessionStatusCallback;
	private boolean					mNewReadPermissionsRequested;

	/**
	 * @param activity
	 * @param onFacebookLoginListener
	 */
	public FacebookLoginHelper(Activity activity, OnFacebookLoginListener onFacebookLoginListener) {
		mActivity = activity;
		mOnFacebookLoginListener = onFacebookLoginListener;
		initializeSessionStatusCallback();
	}

	/**
	 * create instance of Session.StatusCallback
	 */
	private void initializeSessionStatusCallback() {
		mFbSessionStatusCallback = new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				Log.d(LOG_TAG, "Fb Session Status Callback.");
				if (session.getState() == SessionState.CLOSED_LOGIN_FAILED) {
					ToastUtils.showToast(mActivity, ERR_MSG_FB_LOGIN_CANCELLED);
					return;
				}
				if (!session.isOpened()) {
					return;
				}
				String facebookAccesssToken = session.getAccessToken();
				if (StringUtils.isNullOrEmpty(facebookAccesssToken)) {
					ToastUtils.showToast(mActivity, ERR_MSG_FB_LOGIN_FAILED);
					return;
				}
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "Facebook Access Token: " + facebookAccesssToken);
				}

				List<String> permissionsInSession = session.getPermissions();

				boolean newReadPermRequired = !StringUtils.isSubsetOf(FB_READ_PERMISSIONS, permissionsInSession);

				if (newReadPermRequired && !mNewReadPermissionsRequested) {
					mNewReadPermissionsRequested = true;
					Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(mActivity, FB_READ_PERMISSIONS);
					session.requestNewReadPermissions(newPermissionsRequest);
					return;
				}

				mNewReadPermissionsRequested = false;

				if (mOnFacebookLoginListener != null) {
					mOnFacebookLoginListener.onFacebookLoginComplete();
				}
			}
		};
	}

	/**
	 * 
	 */
	public void openFacebookSession() {
		if (BuildConfig.DEBUG) {
			Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		}
		Session session = Session.getActiveSession();
		if (session != null && session.getState() == SessionState.CLOSED_LOGIN_FAILED) {
			session.close();
			session = null;
		}
		if (session == null) {
			session = new Session(mActivity);
			Session.setActiveSession(session);
		}
		if (!session.isOpened() && !session.isClosed()) {
			OpenRequest openRequest = new Session.OpenRequest(mActivity);
			openRequest.setCallback(mFbSessionStatusCallback);
			openRequest.setPermissions(FB_ALL_PERMISSIONS);
			session.openForRead(openRequest);
		} else {
			Session.openActiveSession(mActivity, true, mFbSessionStatusCallback);
		}
	}

	/**
	 * @author sachin.gupta
	 */
	public interface OnFacebookLoginListener {
		public void onFacebookLoginComplete();
	}

	/**
	 * add call back when activity starts
	 */
	public void onActivityStart() {
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().addCallback(mFbSessionStatusCallback);
		}
	}

	/**
	 * remove callback when activity stops
	 */
	public void onActivityStop() {
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().removeCallback(mFbSessionStatusCallback);
		}
	}
}
