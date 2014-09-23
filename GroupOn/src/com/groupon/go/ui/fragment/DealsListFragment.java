package com.groupon.go.ui.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.application.AppStaticVars;
import com.groupon.go.application.GrouponGoApp;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.DealsController;
import com.groupon.go.database.MyPurchasesHelper;
import com.groupon.go.model.DealModel;
import com.groupon.go.model.DealsListRequest;
import com.groupon.go.model.DealsListResponse.DealsListResultModel;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.activity.ProductDetailActivity;
import com.groupon.go.ui.activity.SearchResultsActivity;
import com.groupon.go.ui.adapter.DealsListAdapter;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.activity.BaseActivity;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author sachin.gupta, vineet.rajpoot
 */
public class DealsListFragment extends GrouponBaseFragment implements OnItemClickListener, OnScrollListener {

	private static final String		LOG_TAG	= "DealsListFragment";

	private DealsListRequest		mDealsRequestParams;
	private DealsController			mDealsController;
	private int						mLatestDealsRequestType;

	private View					mFragmentRootView;
	private ListView				mDealsListView;
	private DealsListAdapter		mDealsAdapter;
	private ArrayList<DealModel>	mDealsArrayList;
	private RemoveGridView			mRemoveGridView;

	private String					mFragmentTag;
	private int						mLastFirstVisibleItem;

	private String					mDealsCityName;
	private int						mTotalNumberOfPages;
	private int						mPageSize;

	private View					mBottomLoaderView;
	private Handler					mHandler;
	private Runnable				mRunnable;

	/**
	 * @author vineet.kumar
	 */
	public interface RemoveGridView {
		public void removeViewOnScroll(boolean animate);

		public void addViewOnScroll();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof RemoveGridView) {
			mRemoveGridView = (RemoveGridView) activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFragmentTag = getTag() + ": ";

		Bundle bundle = getArguments();
		mDealsRequestParams = new DealsListRequest();
		mDealsRequestParams.setDealType(bundle.getInt(Constants.EXTRA_DEAL_TYPE));
		mDealsRequestParams.setCategoryId(bundle.getInt(Constants.EXTRA_CATEGORY_ID));
		mDealsRequestParams.setSearchText(bundle.getString(Constants.EXTRA_SEARCH_TEXT));
		mDealsRequestParams.setCouponType(bundle.getInt(Constants.EXTRA_COUPON_TYPE));

		// mDealsArrayList = DummyData.getDealsList();
		mDealsArrayList = new ArrayList<DealModel>();
		mPageSize = 10;

		BaseActivity baseActivity = getBaseActivity();
		if (baseActivity == null) {
			return;
		}

		mDealsAdapter = new DealsListAdapter(baseActivity, mDealsArrayList, mDealsRequestParams.getCouponType());

		mDealsController = new DealsController(baseActivity, this);

		mHandler = new Handler();
		mRunnable = new Runnable() {
			@Override
			public void run() {
				if (mBottomLoaderView != null) {
					mBottomLoaderView.setVisibility(View.GONE);
				}
			}
		};
	}

	/**
	 * @param pGrouponGoApp
	 */
	private void showMyPurchasesFromDatabase() {
		mDealsArrayList.clear();
		BaseActivity baseActivity = getBaseActivity();
		if (baseActivity != null) {
			mDealsAdapter.setDealImagesBaseUrl(GrouponGoPrefs.getDealsImageBaseUrl(baseActivity));
			GrouponGoApp grouponGoApp = (GrouponGoApp) baseActivity.getApplication();
			MyPurchasesHelper myPurchasesHelper = grouponGoApp.getMyPurchasesHelper();
			List<DealModel> dealsList = myPurchasesHelper.getMyPurchasedDeals(baseActivity, mDealsRequestParams.getCouponType());
			mDealsArrayList.addAll(dealsList);
		}
		mDealsAdapter.notifyDataSetChanged();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mFragmentRootView != null && mFragmentRootView.getParent() instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) mFragmentRootView.getParent();
			viewGroup.removeView(mFragmentRootView);
			return mFragmentRootView;
		}
		mFragmentRootView = inflater.inflate(R.layout.fragment_deal_list, container, false);

		mDealsListView = (ListView) mFragmentRootView.findViewById(R.id.listview_deals);
		mDealsListView.setOnItemClickListener(this);
		mDealsListView.setOnScrollListener(this);
		mDealsListView.setAdapter(mDealsAdapter);
		final GestureDetector gestureDetector = new GestureDetector(getActivity(), new SimpleOnGestureListener() {

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (mRemoveGridView == null) {
					return false;
				}
				if (BuildConfig.DEBUG) {
					Log.i(LOG_TAG, "Fling. vX:" + velocityX + ", vY:" + velocityY);
				}
				if (Math.abs(velocityX) > Math.abs(velocityY)) {
					return false;
				}
				if (velocityY < 0) {
					mRemoveGridView.removeViewOnScroll(false);
				} else if (mLastFirstVisibleItem < 3) {
					mRemoveGridView.addViewOnScroll();
				}
				return false;
			}
		});

		mDealsListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestureDetector.onTouchEvent(event);
				return false;
			}
		});

		mBottomLoaderView = mFragmentRootView.findViewById(R.id.linear_bottom_loader);

		DealsListRequest dealsListRequest = new DealsListRequest(mDealsRequestParams);
		dealsListRequest.setPageNo(1);
		mLatestDealsRequestType = DealsListRequest.REQUEST_FIRST_PAGE;
		dealsListRequest.setRequestType(mLatestDealsRequestType);

		/**
		 * My Purchases will be shown from database. <br/>
		 * On Home Screen, 1st page of deals will be fetched from cache. <br/>
		 * Deals on other screens will be fetched from server. <br/>
		 */
		if (dealsListRequest.getCouponType() != 0) {
			showMyPurchasesFromDatabase();
		} else if (dealsListRequest.getCategoryId() == 0 && StringUtils.isNullOrEmpty(dealsListRequest.getSearchText())) {
			mDealsController.getData(ApiConstants.GET_DEALS_LIST_FROM_CACHE, dealsListRequest);
		} else {
			sendDealsListRequestToServer(dealsListRequest, true);
		}

		return mFragmentRootView;
	}

	/**
	 * send deals/coupons list requests
	 * 
	 * @param pShowProgressDialog
	 * @param pShowAtBottom
	 */
	private void sendDealsListRequestToServer(DealsListRequest pDealsListRequest, boolean pShowProgressDialog) {
		BaseActivity baseActivity = getBaseActivity();
		if (baseActivity == null) {
			return;
		}
		if (!ConnectivityUtils.isNetworkEnabled(baseActivity)) {
			ToastUtils.showToast(baseActivity, getString(R.string.error_no_network));
			return;
		}
		if (pShowProgressDialog) {
			mHandler.removeCallbacks(mRunnable);
			mBottomLoaderView.setVisibility(View.VISIBLE);
		}
		mLatestDealsRequestType = pDealsListRequest.getRequestType();
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "sendDealsListRequestToServer() mLatestDealsRequestType:" + mLatestDealsRequestType);
		}
		if (pDealsListRequest.getCouponType() != 0) {
			mDealsController.getData(ApiConstants.GET_COUPONS, pDealsListRequest);
		} else if (StringUtils.isNullOrEmpty(pDealsListRequest.getSearchText())) {
			mDealsController.getData(ApiConstants.GET_DEALS_LIST_FROM_SERVER, pDealsListRequest);
		} else {
			mDealsController.getData(ApiConstants.GET_SEARCH_RESULTS, pDealsListRequest);
		}
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		BaseActivity baseActivity = getBaseActivity();
		if (baseActivity == null) {
			return;
		}
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_DEALS_LIST_FROM_CACHE: {
			if (serviceResponse.isSuccess()) {
				updateDealsOnUI(serviceResponse);
			}
			if (!AppStaticVars.CONNECTING_LOCATION_CLIENT) {
				sendDealsListRequestToServer((DealsListRequest) serviceResponse.getRequestData(), true);
			} else if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "Cached deals shown. Waiting for current location.");
			}
			break;
		}
		case ApiConstants.GET_COUPONS:
		case ApiConstants.GET_SEARCH_RESULTS:
		case ApiConstants.GET_DEALS_LIST_FROM_SERVER: {
			updateDealsOnUI(serviceResponse);
			break;
		}
		}
	}

	/**
	 * @param pServiceResponse
	 */
	private void updateDealsOnUI(ServiceResponse pServiceResponse) {
		BaseActivity baseActivity = getBaseActivity();
		if (baseActivity == null) {
			return;
		}
		boolean shouldIgnoreRespoonse = false;
		DealsListRequest dealsListRequest = (DealsListRequest) pServiceResponse.getRequestData();
		if (mLatestDealsRequestType != dealsListRequest.getRequestType()) {
			shouldIgnoreRespoonse = mLatestDealsRequestType == DealsListRequest.REQUEST_FIRST_PAGE;
		}
		if (shouldIgnoreRespoonse) {
			return;
		}
		mDealsCityName = GrouponGoPrefs.getCityToShowDeals(baseActivity).getCity_name();

		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "updateDealsOnUI() shouldIgnoreRespoonse:" + shouldIgnoreRespoonse);
			Log.i(LOG_TAG, "updateDealsOnUI() mLatestDealsRequestType:" + mLatestDealsRequestType);
			Log.i(LOG_TAG, "updateDealsOnUI() Respoonse Type:" + dealsListRequest.getRequestType());
		}
		boolean shouldRemoveLoader = true;
		if (pServiceResponse.getDataType() == ApiConstants.GET_DEALS_LIST_FROM_CACHE && ConnectivityUtils.isNetworkEnabled(baseActivity)) {
			shouldRemoveLoader = false;
		}
		if (shouldRemoveLoader) {
			mLatestDealsRequestType = DealsListRequest.REQUEST_NONE;
			mHandler.postDelayed(mRunnable, 1000);
			baseActivity.removeProgressDialog();
		}
		if (!pServiceResponse.isSuccess()) {
			ToastUtils.showToast(baseActivity, mFragmentTag + pServiceResponse.getErrorMessage());
			if (dealsListRequest.getRequestType() != DealsListRequest.REQUEST_ON_CITY_CHANGED) {
				return;
			}
		}

		List<DealModel> dealsList = null;
		mTotalNumberOfPages = 0;
		if (pServiceResponse.getResponseObject() instanceof DealsListResultModel) {
			DealsListResultModel dealsListResultModel = (DealsListResultModel) pServiceResponse.getResponseObject();

			mTotalNumberOfPages = dealsListResultModel.getTotal_number_of_pages();
			if (dealsListResultModel.getPage_size() != 0) {
				mPageSize = dealsListResultModel.getPage_size();
			}

			String dealsImageUrl = dealsListResultModel.getDeal_img_url();
			if (!StringUtils.isNullOrEmpty(dealsImageUrl)) {
				dealsImageUrl = ProjectUtils.completeDealsImageBaseUrl(baseActivity, dealsImageUrl);
				mDealsAdapter.setDealImagesBaseUrl(dealsImageUrl);
			}

			if (dealsListResultModel.getDeals_update_interval() > 0) {
				GrouponGoPrefs.setDealsUpdateInterval(baseActivity, dealsListResultModel.getDeals_update_interval());
			}

			dealsList = (List<DealModel>) dealsListResultModel.getDeal();
		}

		if (dealsList == null) {
			if (dealsListRequest.getPageNo() == 1) {
				if (pServiceResponse.getDataType() == ApiConstants.GET_DEALS_LIST_FROM_CACHE) {
					// no toast to show in this case
				} else {
					// list is with cached data, so it is to be removed
					mDealsArrayList.clear();
					ToastUtils.showToast(baseActivity, mFragmentTag, pServiceResponse.getErrorMessage(), getString(R.string.msg_no_deals_found));
				}
			} else {
				ToastUtils.showToast(baseActivity, mFragmentTag, pServiceResponse.getErrorMessage(), getString(R.string.msg_no_more_deals_found));
			}
		} else {
			if (dealsListRequest.getPageNo() == 1) {
				if (pServiceResponse.getDataType() == ApiConstants.GET_DEALS_LIST_FROM_CACHE) {
					// no toast to show in this case
				} else {
					// cached/obsolete data is to be removed first
					mDealsArrayList.clear();
					if (dealsList.isEmpty()) {
						if (getActivity() instanceof SearchResultsActivity) {
							// SearchResultsActivity is showing its own toast
						} else {
							ToastUtils.showToast(baseActivity, mFragmentTag + getString(R.string.msg_no_deals_found));
						}
					} else {
						ToastUtils.showToast(baseActivity, mFragmentTag + getString(R.string.msg_deals_updated));
					}
					if (pServiceResponse.getDataType() == ApiConstants.GET_DEALS_LIST_FROM_SERVER) {
						if (dealsListRequest.getDealType() == ApiConstants.VALUE_DEAL_TYPE_NEAR_BY) {
							GrouponGoPrefs.setNearByDealsResetAt(baseActivity, new Date().getTime());
						}
					}
				}
			} else {
				if (dealsList.isEmpty()) {
					ToastUtils.showToast(baseActivity, mFragmentTag + getString(R.string.msg_no_more_deals_found));
				} else {
					// check and remove duplicates
					for (int i = 0; i < mDealsArrayList.size(); i++) {
						DealModel dealInExistingPage = mDealsArrayList.get(i);
						for (int j = 0; j < dealsList.size(); j++) {
							DealModel dealInNewPage = dealsList.get(j);
							if (dealInExistingPage.getDeal_id() == dealInNewPage.getDeal_id()) {
								mDealsArrayList.remove(i);
								i--;
								break;
							}
						}
					}
					ToastUtils.showToast(baseActivity, mFragmentTag + getString(R.string.msg_more_deals_added));
				}
			}
			mDealsArrayList.addAll(dealsList);
		}
		mDealsAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		BaseActivity baseActivity = getBaseActivity();
		if (baseActivity == null) {
			return;
		}
		if (mDealsRequestParams.getCouponType() == 0 && !ConnectivityUtils.isNetworkEnabled(baseActivity)) {
			ToastUtils.showToast(baseActivity, getString(R.string.error_no_network));
			return;
		}
		Intent intent = new Intent(baseActivity, ProductDetailActivity.class);
		intent.putExtra(Constants.EXTRA_DEAL_MODEL, mDealsAdapter.getItem(position));
		startActivity(intent);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// nothing to do here
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mLastFirstVisibleItem == firstVisibleItem) {
			return;
		}
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "firstVisibleItem: " + firstVisibleItem + ", mLastFirstVisibleItem: " + mLastFirstVisibleItem);
		}
		handleGridViewOnScroll(firstVisibleItem);
		handlePagination(firstVisibleItem, visibleItemCount, totalItemCount);
		mLastFirstVisibleItem = firstVisibleItem;
	}

	/**
	 * @param firstVisibleItem
	 */
	private void handleGridViewOnScroll(int firstVisibleItem) {
		if (mLastFirstVisibleItem < firstVisibleItem) {
			if (mRemoveGridView != null) {
				mRemoveGridView.removeViewOnScroll(true);
			}
		}
	}

	/**
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount
	 */
	private void handlePagination(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "visibleItemCount: " + visibleItemCount + ", totalItemCount: " + totalItemCount);
			Log.i(LOG_TAG, "resetDealsList() mLatestDealsRequestType: " + mLatestDealsRequestType);
		}
		if (firstVisibleItem + visibleItemCount < totalItemCount) {
			return; // we are not on last item
		}
		int currentPagesCount = getCurrentPagesCount();
		if (currentPagesCount >= mTotalNumberOfPages) {
			return; // last page is already fetched
		}
		if (mLatestDealsRequestType != DealsListRequest.REQUEST_NONE) {
			return;
		}
		DealsListRequest dealsListRequest = new DealsListRequest(mDealsRequestParams);
		dealsListRequest.setPageNo(currentPagesCount + 1);
		dealsListRequest.setRequestType(DealsListRequest.REQUEST_NEXT_PAGE);
		sendDealsListRequestToServer(dealsListRequest, true);
	}

	/**
	 * @param pDealsListRequestType
	 */
	public void resetDealsList(int pDealsListRequestType) {
		if (mFragmentRootView == null) {
			return;
		}
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "resetDealsList() pRequestType: " + pDealsListRequestType + ", Last Type(" + mLatestDealsRequestType + ")");
		}
		if (mDealsRequestParams.getCouponType() != 0) {
			showMyPurchasesFromDatabase();
			return;
		}
		BaseActivity baseActivity = getBaseActivity();
		if (baseActivity == null) {
			return;
		}
		DealsListRequest dealsListRequest = new DealsListRequest(mDealsRequestParams);
		switch (pDealsListRequestType) {
		case DealsListRequest.REQUEST_NONE: {
			return;
		}
		case DealsListRequest.REQUEST_ON_CITY_CHANGED: {
			mDealsCityName = GrouponGoPrefs.getCityToShowDeals(baseActivity).getCity_name();
			mDealsArrayList.clear();
			mDealsAdapter.notifyDataSetChanged();
			break;
		}
		case DealsListRequest.REQUEST_IF_NO_DEALS: {
			if (!GrouponGoPrefs.getCityToShowDeals(baseActivity).getCity_name().equals(mDealsCityName)) {
				pDealsListRequestType = DealsListRequest.REQUEST_ON_CITY_CHANGED;
				mDealsCityName = GrouponGoPrefs.getCityToShowDeals(baseActivity).getCity_name();
				mDealsArrayList.clear();
				mDealsAdapter.notifyDataSetChanged();
			} else if (mDealsArrayList.isEmpty()) {
				pDealsListRequestType = DealsListRequest.REQUEST_FIRST_PAGE;
			} else {
				return;
			}
			break;
		}
		case DealsListRequest.REQUEST_REFRESH_PAGES: {
			dealsListRequest.setPageCount(getCurrentPagesCount());
			break;
		}
		}
		dealsListRequest.setPageNo(1);
		dealsListRequest.setRequestType(pDealsListRequestType);
		sendDealsListRequestToServer(dealsListRequest, true);
	}

	/**
	 * @return
	 */
	private int getCurrentPagesCount() {
		if (mDealsArrayList == null || mPageSize <= 0) {
			return 0;
		}
		return (mDealsArrayList.size() + mPageSize - 1) / mPageSize;
	}
}
