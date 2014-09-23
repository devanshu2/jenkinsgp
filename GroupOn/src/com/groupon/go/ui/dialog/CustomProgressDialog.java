package com.groupon.go.ui.dialog;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.activity.PaymentWebView;
import com.kelltontech.utils.StringUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class CustomProgressDialog extends DialogFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title and frame from dialog-fragment
		setStyle(STYLE_NO_TITLE, R.style.Theme_Dialog_40);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
		getDialog().setCanceledOnTouchOutside(false);

		if (!(getActivity() instanceof PaymentWebView)) {
			return inflater.inflate(R.layout.dialog_progress_bar, null, false);
		}

		String progressMessage = getProgressMessage();
		if (StringUtils.isNullOrEmpty(progressMessage)) {
			return inflater.inflate(R.layout.dialog_progress_bar, null, false);
		}

		View view = inflater.inflate(R.layout.dialog_payment_progress, null, false);
		TextView txvProgressMsg = (TextView) view.findViewById(R.id.txv_progress_message);
		txvProgressMsg.setText(progressMessage);
		return view;
	}

	/**
	 * @return a message from R.array.array_msg_progress_dialog as per
	 *         PREF_PAYMENT_MESSAGE_INDEX
	 */
	private String getProgressMessage() {
		Activity activity = getActivity();
		if (activity == null) {
			return "Please wait while the content loads...";
		}
		int msgIndexToShow = GrouponGoPrefs.getPaymentMessageIndex(activity);
		String[] msgArray = activity.getResources().getStringArray(R.array.array_msg_progress_dialog);
		if (msgIndexToShow <= -1 || msgArray == null || msgArray.length == 0) {
			return null;
		}
		if (msgIndexToShow >= msgArray.length) {
			msgIndexToShow = 0;
		}
		return msgArray[msgIndexToShow];
	}
}
