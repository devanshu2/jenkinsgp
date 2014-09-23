package com.groupon.go.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.RegistrationController;
import com.groupon.go.persistance.GrouponGoFiles;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;
import com.kelltontech.utils.WebFontUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class CommonWebViewActivity extends GroupOnBaseActivity {

	private RegistrationController	mRegistrationController;
	private int						mHtmlPageType;
	private boolean					mIsShowingFromCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common_webview);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));

		mHtmlPageType = getIntent().getIntExtra(ApiConstants.PARAM_PAGE_ID, 0);
		mIsShowingFromCache = false;

		if (mHtmlPageType == ApiConstants.VALUE_HTML_TERMS_OF_USE) {
			getSupportActionBar().setTitle(getString(R.string.terms_of_use));
			mIsShowingFromCache = GrouponGoFiles.isTermsOfUseCached(this);
		} else if (mHtmlPageType == ApiConstants.VALUE_HTML_PRIVACY_POLICY) {
			mIsShowingFromCache = GrouponGoFiles.isPrivacyPolicyCached(this);
			getSupportActionBar().setTitle(getString(R.string.privacy_policy));
		}

		mRegistrationController = new RegistrationController(this, this);
		if (mIsShowingFromCache) {
			mRegistrationController.getData(ApiConstants.GET_HTML_PAGE_FROM_CACHE, mHtmlPageType);
		} else {
			showProgressDialog(getString(R.string.prog_loading));
			mRegistrationController.getData(ApiConstants.GET_HTML_PAGE_FROM_SERVER, mHtmlPageType);
		}
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_HTML_PAGE_FROM_CACHE: {
			if (serviceResponse.isSuccess()) {
				loadDataOnWebView((String) serviceResponse.getResponseObject());
			} else {
				mIsShowingFromCache = false;
			}
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				showProgressDialog(getString(R.string.prog_loading));
				mRegistrationController.getData(ApiConstants.GET_HTML_PAGE_FROM_SERVER, mHtmlPageType);
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		case ApiConstants.GET_HTML_PAGE_FROM_SERVER: {
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				loadDataOnWebView((String) serviceResponse.getResponseObject());
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				if (!mIsShowingFromCache) {
					finish();
				}
			}
			break;
		}
		}
	}

	/**
	 * @param responseObject
	 */
	private void loadDataOnWebView(String responseObject) {
		String htmlPageBody = StringUtils.decode(responseObject, StringUtils.CHARSET_UTF_8, true);
		htmlPageBody = WebFontUtils.wrapBodyIntoTemplate(this, htmlPageBody, Constants.TEMPLATE_COMMON_WEB_VIEW);
		WebView webData = (WebView) findViewById(R.id.web_view_common);
		webData.loadDataWithBaseURL(null, htmlPageBody, StringUtils.MIME_TEXT_HTML, StringUtils.CHARSET_UTF_8, null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}
}