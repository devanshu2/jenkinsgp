package com.kelltontech.social;

import android.app.Activity;
import android.content.Intent;

import com.facebook.Request;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.IScreen;
import com.kelltontech.utils.StringUtils;

/**
 * Facebook utils helper class.
 */
public class FacebookUtils {

	/**
	 * check if facebook's active session has non-null access token
	 * 
	 * @return non-null session only if it has non-null access token
	 */
	public static Session getSessionWithAccessToken() {
		Session session = Session.getActiveSession();
		if (session == null || StringUtils.isNullOrEmpty(session.getAccessToken())) {
			return null;
		}
		return session;
	}

	/**
	 * @param activity
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public static void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		Session session = Session.getActiveSession();
		if (session == null) {
			return;
		}
		session.onActivityResult(activity, requestCode, resultCode, data);
	}

	/**
	 * check if facebook's active session has non-null access token
	 * 
	 * @param baseActivity
	 * @return true if a request is executed
	 */
	public static boolean requestFacebookUser(final IScreen iScreen) {
		Session session = getSessionWithAccessToken();
		if (session == null) {
			iScreen.onEvent(Events.REQUEST_FACEBOOK_USER, null);
			return false;
		}
		Request.newMeRequest(session, new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, com.facebook.Response response) {
				iScreen.onEvent(Events.REQUEST_FACEBOOK_USER, user);
			}
		}).executeAsync();
		return true;
	}

	/**
	 * @param user
	 * @return user's fb name
	 */
	public static String getUserFbName(GraphUser user) {
		Object userFbNameObject = user.getProperty("name");
		if (userFbNameObject != null) {
			return userFbNameObject.toString();
		}
		return "Anonymous";
	}

	/**
	 * 
	 */
	public static void clearAccessToken() {
		Session session = Session.getActiveSession();
		if (session != null) {
			session.closeAndClearTokenInformation();
		}
	}
}
