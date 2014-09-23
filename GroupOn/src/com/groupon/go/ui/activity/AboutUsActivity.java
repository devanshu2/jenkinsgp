package com.groupon.go.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.controller.UserProfileController;
import com.groupon.go.model.GetProfileResponse.GetProfileResultModel;
import com.groupon.go.persistance.GrouponGoFiles;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.ApplicationUtils;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class AboutUsActivity extends DrawerBaseActivity implements OnClickListener {

	private Uri	mLikeUsOnFbUri;
	private Uri	mFollowUsOnTwitterUri;
	private Uri	mCustomerSupportNumberUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_us);

		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.screen_header_about, getString(R.string.app_name)));
		initializeDrawer();

		findViewById(R.id.txv_terms_of_use).setOnClickListener(this);
		findViewById(R.id.txv_privacy_policy).setOnClickListener(this);

		TextView txvAppVersion = (TextView) findViewById(R.id.txv_app_version);
		txvAppVersion.setText(getString(R.string.label_app_version, ApplicationUtils.getAppVersionName(this)));

		new UserProfileController(this, this).getData(ApiConstants.GET_PROFILE_FROM_CACHE, null);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_PROFILE_FROM_CACHE: {
			if (serviceResponse.getResponseObject() instanceof GetProfileResultModel) {
				setAboutUsDataOnScreen((GetProfileResultModel) serviceResponse.getResponseObject());
			}
			break;
		}
		}
	}

	/**
	 * @param profileResult
	 */
	private void setAboutUsDataOnScreen(GetProfileResultModel profileResult) {
		TextView txvMsgAboutUs = (TextView) findViewById(R.id.txv_msg_about_us);
		txvMsgAboutUs.setText(profileResult.getAbout());

		if (!StringUtils.isNullOrEmpty(profileResult.getFb_url())) {
			try {
				mLikeUsOnFbUri = Uri.parse(profileResult.getFb_url());
			} catch (Exception e) {
				// ignore
			}
		}
		if (!StringUtils.isNullOrEmpty(profileResult.getTwitter_url())) {
			try {
				mFollowUsOnTwitterUri = Uri.parse(profileResult.getTwitter_url());
			} catch (Exception e) {
				// ignore
			}
		}
		if (!StringUtils.isNullOrEmpty(profileResult.getCustomer_support_number())) {
			try {
				mCustomerSupportNumberUri = Uri.parse("tel:" + profileResult.getCustomer_support_number());
			} catch (Exception e) {
				// ignore
			}
		}
		if (mLikeUsOnFbUri == null) {
			findViewById(R.id.view_divider_1).setVisibility(View.GONE);
			findViewById(R.id.txv_like_on_fb).setVisibility(View.GONE);
		} else {
			findViewById(R.id.txv_like_on_fb).setOnClickListener(this);
		}

		if (mFollowUsOnTwitterUri == null) {
			findViewById(R.id.txv_follow_on_twitter).setVisibility(View.GONE);
			findViewById(R.id.view_divider_2).setVisibility(View.GONE);
		} else {
			findViewById(R.id.txv_follow_on_twitter).setOnClickListener(this);
		}

		if (mCustomerSupportNumberUri == null) {
			findViewById(R.id.txv_customer_support).setVisibility(View.GONE);
			findViewById(R.id.view_divider_6).setVisibility(View.GONE);
		} else {
			findViewById(R.id.txv_customer_support).setOnClickListener(this);
		}

		TextView txvCopyright = (TextView) findViewById(R.id.txv_copyright);
		txvCopyright.setText(profileResult.getFooter_text());
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.txv_like_on_fb: {
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(mLikeUsOnFbUri);
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		case R.id.txv_follow_on_twitter: {
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(mFollowUsOnTwitterUri);
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		case R.id.txv_terms_of_use: {
			if (ConnectivityUtils.isNetworkEnabled(this) || GrouponGoFiles.isTermsOfUseCached(this)) {
				intent = new Intent(this, CommonWebViewActivity.class);
				intent.putExtra(ApiConstants.PARAM_PAGE_ID, ApiConstants.VALUE_HTML_TERMS_OF_USE);
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		case R.id.txv_privacy_policy: {
			if (ConnectivityUtils.isNetworkEnabled(this) || GrouponGoFiles.isPrivacyPolicyCached(this)) {
				intent = new Intent(this, CommonWebViewActivity.class);
				intent.putExtra(ApiConstants.PARAM_PAGE_ID, ApiConstants.VALUE_HTML_PRIVACY_POLICY);
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		case R.id.txv_customer_support: {
			intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(mCustomerSupportNumberUri);
			break;
		}
		}
		if (intent == null) {
			return;
		}
		try {
			startActivity(intent);
		} catch (Exception e) {
			ToastUtils.showToast(this, getString(R.string.error_generic_message));
		}
	}
}