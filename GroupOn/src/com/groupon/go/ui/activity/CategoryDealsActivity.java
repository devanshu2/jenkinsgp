package com.groupon.go.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.CategoryModel;
import com.groupon.go.model.DealsListRequest;
import com.kelltontech.ui.Events;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author sachin.gupta
 */
public class CategoryDealsActivity extends GroupOnBaseActivity implements OnClickListener, OnTabChangeListener {

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

		View imgSearch = findViewById(R.id.img_search);
		imgSearch.setVisibility(View.VISIBLE);
		imgSearch.setOnClickListener(this);

		CategoryModel selectedCategory = getIntent().getParcelableExtra(Constants.EXTRA_SELECTED_CATEGORY);
		TextView txvScreenHeader = (TextView) findViewById(R.id.txv_groupon_header);
		txvScreenHeader.setText(selectedCategory.getCat_name());
		txvScreenHeader.setOnClickListener(this);

		mCurrentFragmentTag = setUpTabHost(selectedCategory.getTid(), this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateCartBadge(mTxvCartIconWithCount, false);
	}

	@Override
	public void onClick(View pClickSource) {
		switch (pClickSource.getId()) {
		case R.id.txv_groupon_header: {
			finish();
			break;
		}
		case R.id.img_search: {
			Intent intent = new Intent(this, SearchProductActivity.class);
			startActivity(intent);
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
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.CITY_CHANGE_CONFIRM_DIALOG: {
			if (eventData instanceof Boolean && (Boolean) eventData) {
				updateDealsListOnFragments(DealsListRequest.REQUEST_FIRST_PAGE, mCurrentFragmentTag);
			}
			break;
		}
		}
	}
}