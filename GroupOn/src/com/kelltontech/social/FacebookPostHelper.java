package com.kelltontech.social;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.kelltontech.utils.StringUtils;

/**
 * Facebook login helper class to check session.
 */
public class FacebookPostHelper {

	private final static String			LOG_TAG		= "FacebookPostHelper";

	private static final List<String>	PERMISSIONS	= Arrays.asList("publish_actions");

	/**
	 * @author sachin.gupta
	 */
	public interface FacebookPostResponseListener {
		public void onFacebookPostComplete(String postId, FacebookRequestError error);
	}

	/**
	 * @param activity
	 */
	public static void publishStory(final Activity activity, final Bundle postParams, final FacebookPostResponseListener facebookPostResponseListener) {
		Session session = Session.getActiveSession();
		if (session != null) {
			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!StringUtils.isSubsetOf(PERMISSIONS, permissions)) {
				NewPermissionsRequest newPermissionsRequest = new NewPermissionsRequest(activity, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					String postId = null;
					FacebookRequestError error = null;
					try {
						JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
						postId = graphResponse.getString("id");
						error = response.getError();
					} catch (JSONException e) {
						Log.i(LOG_TAG, "JSON error " + e.getMessage());
					}
					facebookPostResponseListener.onFacebookPostComplete(postId, error);
				}
			};

			Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}
	}

}
