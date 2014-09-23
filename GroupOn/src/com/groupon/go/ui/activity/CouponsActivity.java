package com.groupon.go.ui.activity;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.application.GrouponGoApp;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.database.MyPurchasesHelper;
import com.groupon.go.model.DealsListRequest;
import com.groupon.go.model.DealsListResponse.DealsListResultModel;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.ui.fragment.DealsListFragment;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.widget.CustomTextView;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.ToastUtils;
import com.kelltontech.utils.UiUtils;

/**
 * @author vineet.rajpoot
 */
public class CouponsActivity extends DrawerBaseActivity implements OnTabChangeListener, OnClickListener {

	private CustomTextView		mTxvCartIconWithCount;
	private String				mCurrentFragmentTag;
	private View				mRelativeOnScreenNotification;
	private IntentFilter		mPurchasesUpdateFilter;
	private BroadcastReceiver	mPurchasesUpdateReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coupon);

		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));

		mRelativeOnScreenNotification = ProjectUtils.showNotificationOnScreen(this);

		initializeDrawer();
		FragmentTabHost fragmentTabHost = setUpTabHost();
		fragmentTabHost.setOnTabChangedListener(this);

		showMyPurchasesCount();

		mPurchasesUpdateFilter = new IntentFilter(Constants.ACTION_MY_PURCHASES_RESP_RCVD);
		mPurchasesUpdateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updatePurchasesCountAndLists();
				removeProgressDialog();
			}
		};

		if (ConnectivityUtils.isNetworkEnabled(this)) {
			showProgressDialog(getString(R.string.prog_loading));
			BackGroundService.start(this, ApiConstants.GET_MY_PURCHASES);
		} else {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
		}
	}

	/**
	 * 
	 */
	private void updatePurchasesCountAndLists() {
		showMyPurchasesCount();
		updateDealsListOnFragments(DealsListRequest.REQUEST_FIRST_PAGE, mCurrentFragmentTag);
	}

	/**
	 * 
	 */
	private void showMyPurchasesCount() {
		GrouponGoApp grouponGoApp = (GrouponGoApp) getApplication();
		MyPurchasesHelper myPurchasesHelper = grouponGoApp.getMyPurchasesHelper();
		int vouchersCount = myPurchasesHelper.getTotalVouchers(this);
		getSupportActionBar().setTitle(getString(R.string.screen_header_my_purchases, vouchersCount));
	}

	/**
	 * Same string array is used as tab-titles and tab-id(s)
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected FragmentTabHost setUpTabHost() {
		FragmentTabHost fragTabHost = (FragmentTabHost) findViewById(R.id.tabhost);
		fragTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			fragTabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
		}

		String[] tabIdsArr = getResources().getStringArray(R.array.tab_title_coupons);
		Class<?>[] fragClassesArr = { DealsListFragment.class,
				DealsListFragment.class, DealsListFragment.class };

		Bundle allCouponsBundle = new Bundle();
		allCouponsBundle.putInt(Constants.EXTRA_COUPON_TYPE, ApiConstants.VALUE_COUPON_TYPE_ALL);
		Bundle expiringCouponBundle = new Bundle();
		expiringCouponBundle.putInt(Constants.EXTRA_COUPON_TYPE, ApiConstants.VALUE_COUPON_TYPE_EXPIRING);
		Bundle nearByCouponBundle = new Bundle();
		nearByCouponBundle.putInt(Constants.EXTRA_COUPON_TYPE, ApiConstants.VALUE_COUPON_TYPE_NEARBY);

		Bundle[] bundlesArr = { allCouponsBundle, expiringCouponBundle,
				nearByCouponBundle };
		mCurrentFragmentTag = tabIdsArr[0];
		for (int i = 0; i < tabIdsArr.length; i++) {
			View tabView = getLayoutInflater().inflate(R.layout.tab_design, null);
			((TextView) tabView.findViewById(R.id.tab_title)).setText(tabIdsArr[i]);
			fragTabHost.addTab(fragTabHost.newTabSpec(tabIdsArr[i]).setIndicator(tabView), fragClassesArr[i], bundlesArr[i]);
		}
		return fragTabHost;
	}

	@Override
	public void onTabChanged(String tabId) {
		mCurrentFragmentTag = tabId;
		getCurrentLocation();
		updateDealsListOnFragments(DealsListRequest.REQUEST_IF_NO_DEALS, mCurrentFragmentTag);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLocalBroadcastManager.registerReceiver(mPurchasesUpdateReceiver, mPurchasesUpdateFilter);
		if (mTxvCartIconWithCount != null) {
			updateCartBadge(mTxvCartIconWithCount, false);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		updatePurchasesCountAndLists();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocalBroadcastManager.unregisterReceiver(mPurchasesUpdateReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_screen_menu, menu);
		MenuItem menuCart = menu.findItem(R.id.action_cart);
		View actionView = MenuItemCompat.getActionView(menuCart);
		if (actionView instanceof RelativeLayout) {
			RelativeLayout rootActionCart = (RelativeLayout) actionView;
			mTxvCartIconWithCount = (CustomTextView) rootActionCart.findViewById(R.id.txv_header_cart);
			rootActionCart.setOnClickListener(this);
			updateCartBadge(mTxvCartIconWithCount, false);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search: {
			Intent intent = new Intent(this, SearchProductActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.action_cart: {
			Intent intent = new Intent(this, UserCart.class);
			startActivity(intent);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.root_item_cart: {
			Intent intent = new Intent(this, UserCart.class);
			startActivity(intent);
			break;
		}
		case R.id.img_close_notification_on_screen: {
			if (mRelativeOnScreenNotification != null) {
				UiUtils.collapse(mRelativeOnScreenNotification);
				findViewById(R.id.view_bottom_divider_notification_on_screen).setVisibility(View.GONE);
			}
			break;
		}
		}
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_COUPONS: {
			if (serviceResponse.getResponseObject() instanceof DealsListResultModel) {
				DealsListResultModel dealsListResultModel = (DealsListResultModel) serviceResponse.getResponseObject();
				int vouchersCount = dealsListResultModel.getTotal_vouchers();
				getSupportActionBar().setTitle(getString(R.string.screen_header_my_purchases, vouchersCount));
			}
			break;
		}
		}
	}
}