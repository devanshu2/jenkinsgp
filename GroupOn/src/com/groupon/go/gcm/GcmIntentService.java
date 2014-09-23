package com.groupon.go.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

import com.demach.konotor.Konotor;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.ui.activity.HomeActivity;
import com.kelltontech.utils.NotificationUtils;
import com.kelltontech.utils.StringUtils;

/**
 * @author GCM Sample
 * @edited sachin.gupta
 */
public class GcmIntentService extends IntentService {

	private static final String	LOG_TAG	= "GcmIntentService";

	/**
	 * No argument constructor
	 */
	public GcmIntentService() {
		super(GcmIntentService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "GCM Push Type: " + messageType);
			Log.i(LOG_TAG, "Complete Message: " + intent.getExtras());
		}
		if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			if (Konotor.getInstance(getApplicationContext()).isKonotorMessage(intent)) {
				Konotor.getInstance(getApplicationContext()).handleGcmOnMessage(intent);
			} else {
				sendNotification(intent);
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);

		// if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
		// {
		// sendNotification("Send error: " + extras.toString());
		// } else if
		// (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
		// sendNotification("Deleted messages on server: " + extras.toString());
		// }
	}

	/**
	 * Post notification of received message.
	 * 
	 * @param msg
	 */
	private void sendNotification(Intent rcvdIntent) {
		int notifyType = 0;
		if (rcvdIntent != null) {
			notifyType = StringUtils.parseInt(rcvdIntent.getStringExtra(ApiConstants.PARAM_NOTIFY_TYPE), -1, 0);
		}
		if (notifyType == 0) {
			return;
		}
		Builder builder = Uri.parse("groupongo://notification").buildUpon();
		builder.appendQueryParameter(ApiConstants.PARAM_NOTIFY_TYPE, "" + notifyType);

		String dealId = "";
		if (notifyType == ApiConstants.VALUE_NOTIFY_TYPE_SHOW_DEAL) {
			dealId = rcvdIntent.getStringExtra(ApiConstants.PARAM_DEAL_ID);
			builder.appendQueryParameter(ApiConstants.PARAM_ONLY_ID, dealId);
		}

		// String senderName =
		// rcvdIntent.getStringExtra(ApiConstants.PARAM_SENDER_NAME);
		String notificationMessage = rcvdIntent.getStringExtra(ApiConstants.PARAM_ALERT);

		// below param is to prevent pending intent equality.
		builder.appendQueryParameter("unique_param", "" + System.currentTimeMillis());

		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "alert: " + notificationMessage);
			Log.i(LOG_TAG, "notify_type: " + notifyType);
			Log.i(LOG_TAG, "deal_id: " + dealId);
		}

		Intent activityIntent = new Intent(this, HomeActivity.class);
		activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		activityIntent.setData(builder.build());
		activityIntent.putExtra(Constants.EXTRA_MESSAGE, notificationMessage);

		if (notifyType == ApiConstants.VALUE_NOTIFY_TYPE_PROMO_CODE || notifyType == ApiConstants.VALUE_NOTIFY_TYPE_UNIQUE_CODE) {
			activityIntent.putExtra(Constants.EXTRA_COUPON_CODE, rcvdIntent.getStringExtra(ApiConstants.PARAM_PROMO_CODE));

			NotificationUtils.showNotification(this, /* senderName */null, notificationMessage, activityIntent, false);
		} else {
			NotificationUtils.showNotification(this, /* senderName */null, notificationMessage, activityIntent, true);
		}
	}
}
