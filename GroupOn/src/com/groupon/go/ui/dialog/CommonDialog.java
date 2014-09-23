package com.groupon.go.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.activity.BaseActivity;
import com.kelltontech.utils.UiUtils;

/**
 * @author sachin.gupta
 */
public class CommonDialog extends DialogFragment implements OnClickListener {

	private Bundle			mDialogData;
	private int				mDialogId;
	private BaseActivity	mBaseActivity;
	private EditText		mEdtCouponCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title and frame from dialog-fragment
		Activity activity = getActivity();
		if (activity instanceof BaseActivity) {
			mBaseActivity = (BaseActivity) activity;
		}
		setStyle(STYLE_NO_TITLE, 0);
		mDialogData = getArguments();
		mDialogId = mDialogData.getInt(Constants.EXTRA_DIALOG_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_common, container, false);

		Button leftButton = (Button) view.findViewById(R.id.dialog_left_button);
		Button rightButton = (Button) view.findViewById(R.id.dialog_right_button);
		leftButton.setOnClickListener(this);
		rightButton.setOnClickListener(this);

		TextView dialogContent = (TextView) view.findViewById(R.id.txv_dialog_content);
		TextView dialogTitle = (TextView) view.findViewById(R.id.txv_dialog_title);
		TextView dialogFooter = (TextView) view.findViewById(R.id.txv_dialog_footer);
		Resources res = getResources();

		switch (mDialogId) {
		case Events.USER_REGISTER_DIALOG: {
			String dialogText = mDialogData.getString(Constants.EXTRA_COUNTRY_CODE) + "-" + mDialogData.getString(Constants.EXTRA_USER_NUMBER);
			dialogTitle.setText(res.getString(R.string.register_dlg_title));
			dialogFooter.setText(res.getString(R.string.register_dlg_footer));
			dialogContent.setText(dialogText);
			leftButton.setText(res.getString(R.string.button_edit));
			rightButton.setText(res.getString(R.string.button_continue));
			break;
		}
		case Events.RESEND_VERIF_CODE_DIALOG: {
			int errorCode = mDialogData.getInt(Constants.EXTRA_ERROR_CODE);
			if (errorCode == ApiConstants.ERROR_CODE_OTP_EXPIRED) {
				dialogTitle.setText(res.getString(R.string.dlg_otp_expired_title));
			} else {
				dialogTitle.setText(res.getString(R.string.dlg_resend_code_title));
			}
			dialogContent.setText(res.getString(R.string.dlg_resend_code_content));
			dialogFooter.setText(res.getString(R.string.dlg_resend_code_footer));
			leftButton.setText(res.getString(R.string.button_cancel));
			rightButton.setText(res.getString(R.string.button_resend));
			break;
		}
		case Events.LOCATION_IS_OFF_DIALOG: {
			dialogTitle.setText(res.getString(R.string.dialog_location_is_off_title));
			dialogContent.setText(res.getString(R.string.dialog_location_is_off_content));
			dialogFooter.setText(res.getString(R.string.dialog_location_or_gps_is_off_footer));
			leftButton.setText(res.getString(R.string.button_not_now));
			rightButton.setText(res.getString(R.string.button_settings));
			break;
		}
		case Events.GPS_IS_OFF_DIALOG: {
			dialogTitle.setText(res.getString(R.string.dialog_gps_is_off_title));
			dialogContent.setText(res.getString(R.string.dialog_gps_is_off_content));
			dialogFooter.setText(res.getString(R.string.dialog_location_or_gps_is_off_footer));
			leftButton.setText(res.getString(R.string.button_not_now));
			rightButton.setText(res.getString(R.string.button_settings));
			break;
		}
		case Events.CITY_CHANGE_CONFIRM_DIALOG: {
			dialogTitle.setText(res.getString(R.string.dialog_title_city_changed));
			dialogContent.setText(res.getString(R.string.dialog_msg_city_changed, mDialogData.getString(Constants.EXTRA_CITY_NAME)));
			dialogFooter.setVisibility(View.GONE);
			leftButton.setText(res.getString(R.string.button_no_thanks));
			rightButton.setText(res.getString(R.string.button_update_city));
			break;
		}
		case Events.REDEEM_COUPON_DIALOG: {
			dialogTitle.setText(res.getString(R.string.app_name));
			dialogTitle.setCompoundDrawablePadding((int) UiUtils.getPixels(mBaseActivity, 5));
			dialogTitle.setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(R.drawable.ic_alert), null, null, null);
			dialogContent.setText(res.getString(R.string.dlg_redeem_couopn_content));
			dialogFooter.setText(res.getString(R.string.dlg_redeem_couopn_footer));
			leftButton.setText(res.getString(R.string.button_cancel));
			rightButton.setText(res.getString(R.string.button_continue));
			break;
		}
		case Events.APPLY_COUPON_CODE_DIALOG: {
			dialogTitle.setText(res.getString(R.string.app_name));
			String content = res.getString(R.string.msg_apply_coupon, mDialogData.getString(Constants.EXTRA_COUPON_CODE));
			dialogContent.setText(content);
			dialogFooter.setVisibility(View.GONE);
			leftButton.setText(res.getString(R.string.button_dlg_no));
			rightButton.setText(res.getString(R.string.button_dlg_yes));
			break;
		}
		case Events.REMOVE_COUPON_CODE: {
			dialogTitle.setText(mDialogData.getString(Constants.EXTRA_COUPON_CODE));
			dialogContent.setText(res.getString(R.string.dlg_msg_remove_coupon));
			dialogFooter.setVisibility(View.GONE);
			leftButton.setText(res.getString(R.string.button_dlg_no));
			rightButton.setText(res.getString(R.string.button_dlg_yes));
			break;
		}
		case Events.CLEAR_CART: {
			dialogTitle.setText(res.getString(R.string.title_clear_cart));
			dialogContent.setText(res.getString(R.string.msg_clear_all_items));
			dialogFooter.setVisibility(View.GONE);
			leftButton.setText(res.getString(R.string.button_dlg_no));
			rightButton.setText(res.getString(R.string.button_dlg_yes));
			break;
		}
		case Events.CONFIRM_REMOVE_STORED_CARD: {
			dialogTitle.setText(mDialogData.getString(Constants.EXTRA_DIALOG_TITLE));
			dialogContent.setText(res.getString(R.string.dialog_msg_confirm_delete_card));
			dialogFooter.setVisibility(View.GONE);
			leftButton.setText(res.getString(R.string.button_dlg_no));
			rightButton.setText(res.getString(R.string.button_dlg_yes));
			break;
		}
		case Events.CONFIRM_REMOVE_PROFILE_PIC: {
			dialogTitle.setText(res.getString(R.string.app_name));
			dialogContent.setText(res.getString(R.string.dialog_msg_remove_profile_pic));
			dialogFooter.setVisibility(View.GONE);
			leftButton.setText(res.getString(R.string.button_dlg_no));
			rightButton.setText(res.getString(R.string.button_dlg_yes));
			break;
		}
		}
		return view;
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.dialog_left_button: {
			onLeftButtonClick();
			break;
		}
		case R.id.dialog_right_button: {
			onRightButtonClick();
			break;
		}
		}
	}

	/**
	 * 
	 */
	private void onRightButtonClick() {
		switch (mDialogId) {
		case Events.USER_REGISTER_DIALOG:
		case Events.RESEND_VERIF_CODE_DIALOG:
		case Events.REMOVE_COUPON_CODE:
		case Events.REDEEM_COUPON_DIALOG:
		case Events.CLEAR_CART:
		case Events.CONFIRM_REMOVE_STORED_CARD:
		case Events.CONFIRM_REMOVE_PROFILE_PIC: {
			dismiss();
			if (mBaseActivity != null) {
				mBaseActivity.onEvent(mDialogId, true);
			}
			break;
		}
		case Events.LOCATION_IS_OFF_DIALOG:
		case Events.GPS_IS_OFF_DIALOG: {
			dismiss();
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			getActivity().startActivityForResult(intent, Constants.RQ_OPEN_LOCATION_SETTINGS);
			break;
		}
		case Events.APPLY_COUPON_CODE_DIALOG: {
			if (mBaseActivity != null) {
				mBaseActivity.hideSoftKeypad(mEdtCouponCode);
			}
			dismiss();
			if (mBaseActivity != null) {
				mBaseActivity.onEvent(mDialogId, true);
			}
		}
		}
	}

	/**
	 * 
	 */
	private void onLeftButtonClick() {
		switch (mDialogId) {
		case Events.USER_REGISTER_DIALOG:
		case Events.RESEND_VERIF_CODE_DIALOG:
		case Events.REDEEM_COUPON_DIALOG:
		case Events.REMOVE_COUPON_CODE:
		case Events.CLEAR_CART:
		case Events.CONFIRM_REMOVE_STORED_CARD:
		case Events.CONFIRM_REMOVE_PROFILE_PIC: {
			dismiss();
			break;
		}
		case Events.APPLY_COUPON_CODE_DIALOG: {
			if (mBaseActivity != null) {
				mBaseActivity.hideSoftKeypad(mEdtCouponCode);
			}
			dismiss();
			if (mBaseActivity != null) {
				mBaseActivity.onEvent(mDialogId, false);
			}
			break;
		}
		case Events.LOCATION_IS_OFF_DIALOG:
		case Events.GPS_IS_OFF_DIALOG:
		case Events.CITY_CHANGE_CONFIRM_DIALOG: {
			dismiss();
			if (mBaseActivity != null) {
				mBaseActivity.onEvent(mDialogId, false);
			}
			break;
		}
		}
	}
}