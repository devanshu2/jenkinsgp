package com.groupon.go.ui.activity;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.DealModel;
import com.groupon.go.model.DealsListRequest;
import com.groupon.go.model.DealsListResponse.DealsListResultModel;
import com.groupon.go.model.SearchItemModel;
import com.groupon.go.ui.fragment.DealsListFragment;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.Events;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author sachin.gupta
 */
public class SearchResultsActivity extends GroupOnBaseActivity implements OnClickListener, OnTabChangeListener {

	private String		mCurrentFragmentTag;
	private TextView	mTxvCartIconWithCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_category_deals);

		findViewById(R.id.img_groupon_header_right_btn).setVisibility(View.GONE);
		findViewById(R.id.view_groupon_header_vertical_dvdr).setVisibility(View.GONE);

		mTxvCartIconWithCount = (TextView) findViewById(R.id.txv_header_cart);
		mTxvCartIconWithCount.setOnClickListener(this);
		mTxvCartIconWithCount.setVisibility(View.VISIBLE);

		View imgCart = findViewById(R.id.cart_image);
		imgCart.setVisibility(View.VISIBLE);
		imgCart.setOnClickListener(this);

		SearchItemModel searchItemModel = getIntent().getParcelableExtra(Constants.EXTRA_SEARCH_TEXT);
		String searchStr = ProjectUtils.getCompleteSearchText(searchItemModel, false);
		TextView txvScreenHeader = (TextView) findViewById(R.id.txv_groupon_header);
		txvScreenHeader.setText(searchStr);
		txvScreenHeader.setOnClickListener(this);

		FragmentTabHost fragmentTabHost = setUpTabHost(searchItemModel);
		fragmentTabHost.setOnTabChangedListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateCartBadge(mTxvCartIconWithCount, false);
	}

	/**
	 * Same string array is used as tab-titles and tab-id(s)
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected FragmentTabHost setUpTabHost(SearchItemModel searchItemModel) {
		FragmentTabHost fragTabHost = (FragmentTabHost) findViewById(R.id.tabhost);
		fragTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			TabWidget tabWidget = fragTabHost.getTabWidget();
			tabWidget.setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
		}

		// TODO - BY RATING tab is not needed as of now, below 3 changes are
		// done for this.
		/**
		 * 1. tab bar is to be removed. <br/>
		 * 2. TabContent will not need top padding. <br/>
		 * 3. tabIdsArr will have only one element.
		 */
		findViewById(android.R.id.tabs).setVisibility(View.GONE);
		findViewById(android.R.id.tabcontent).setPadding(0, 0, 0, 0);
		String[] tabIdsArr = getResources().getStringArray(R.array.tab_titles_search_screen_temp);

		// String[] tabIdsArr =
		// getResources().getStringArray(R.array.tab_titles_search_screen);

		Bundle searchKeyBundle = new Bundle();
		searchKeyBundle.putString(Constants.EXTRA_SEARCH_TEXT, searchItemModel.getSearchText());
		searchKeyBundle.putInt(Constants.EXTRA_CATEGORY_ID, searchItemModel.getCat_id());
		Bundle[] bundlesArr = { searchKeyBundle, searchKeyBundle };
		Class<?>[] fragClassesArr = { DealsListFragment.class,
				DealsListFragment.class };

		mCurrentFragmentTag = tabIdsArr[0];

		for (int i = 0; i < tabIdsArr.length; i++) {
			View tabView = getLayoutInflater().inflate(R.layout.tab_design, null);
			((TextView) tabView.findViewById(R.id.tab_title)).setText(tabIdsArr[i]);
			TabSpec tabSpec = fragTabHost.newTabSpec(tabIdsArr[i]);
			tabSpec.setIndicator(tabView);
			fragTabHost.addTab(tabSpec, fragClassesArr[i], bundlesArr[i]);
		}
		return fragTabHost;
	}

	@Override
	public void onClick(View pClickSource) {
		switch (pClickSource.getId()) {
		case R.id.txv_groupon_header: {
			finish();
			break;
		}
		case R.id.cart_image:
		case R.id.txv_header_cart: {
			Intent intent = new Intent(this, UserCart.class);
			startActivity(intent);
			break;
		}
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		mCurrentFragmentTag = tabId;
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		getCurrentLocation();
		updateDealsListOnFragments(DealsListRequest.REQUEST_IF_NO_DEALS, mCurrentFragmentTag);
	}

	@Override
	public void onEvent(int dialogId, Object eventData) {
		super.onEvent(dialogId, eventData);
		switch (dialogId) {
		case Events.CITY_CHANGE_CONFIRM_DIALOG: {
			if ((Boolean) eventData) {
				updateDealsListOnFragments(DealsListRequest.REQUEST_FIRST_PAGE, mCurrentFragmentTag);
			}
			break;
		}
		}
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_SEARCH_RESULTS: {
			if (serviceResponse.getResponseObject() instanceof DealsListResultModel) {
				DealsListResultModel dealsListResultModel = (DealsListResultModel) serviceResponse.getResponseObject();
				ArrayList<DealModel> dealsList = dealsListResultModel.getDeal();
				if (dealsList != null && dealsList.isEmpty()) {
					ToastUtils.showToast(this, getString(R.string.toast_no_search_results));
				}
			}
			break;
		}
		}
	}
}