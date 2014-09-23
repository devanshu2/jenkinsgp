package com.groupon.go.ui.activity;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.application.AppStaticVars;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.controller.UserProfileController;
import com.groupon.go.model.CityListResponse.CityListResultModel;
import com.groupon.go.model.CityModel;
import com.groupon.go.persistance.GrouponGoFiles;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.adapter.CityListAdapter;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.service.ReverseGeoService;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author sachin.gupta
 */
public class SelectYourCityActivity extends GroupOnBaseActivity implements OnClickListener {

	private UserProfileController	mUserProfileController;
	private ArrayList<CityModel>	mCitiesArrayList;
	private CityListAdapter			mCityListAdapter;
	private ListView				mListViewCities;

	private BroadcastReceiver		mLocProvidersChangeReceiver;
	private IntentFilter			mLocProvidersChangeFilter;
	private String					mReverseGeoCodedCityName;
	private CityModel				mSelectableCurrentCityModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.screen_header_select_your_city));

		setContentView(R.layout.activity_select_your_city);

		setUpCityListView();
		mUserProfileController = new UserProfileController(this, this);
		mUserProfileController.getData(ApiConstants.GET_CITY_LIST_FROM_CACHE, null);

		mLocProvidersChangeFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
		mLocProvidersChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (ReverseGeoService.shouldStart(SelectYourCityActivity.this)) {
					startReverseGeoService(false);
				} else {
					AppStaticVars.REVERSE_GEO_CODED_CITY = null;
					updateCurrentLocationView();
				}
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_select_your_city, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_close_screen: {
			closeWithUserCitySelectionCheck(null);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(mLocProvidersChangeReceiver, mLocProvidersChangeFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (ReverseGeoService.shouldStart(this)) {
			startReverseGeoService(false);
		} else {
			AppStaticVars.REVERSE_GEO_CODED_CITY = null;
			updateCurrentLocationView();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mLocProvidersChangeReceiver);
	}

	/**
	 * set city list view and set onClickListener on city list items
	 */
	private void setUpCityListView() {
		mCitiesArrayList = new ArrayList<CityModel>();
		mListViewCities = (ListView) findViewById(R.id.listview_cities);
		mCityListAdapter = new CityListAdapter(this, mCitiesArrayList);
		mListViewCities.setAdapter(mCityListAdapter);
		mListViewCities.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				closeWithUserCitySelectionCheck(mCityListAdapter.getItem(position));
			}
		});
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_CITY_LIST_FROM_CACHE: {
			if (serviceResponse.getResponseObject() instanceof CityListResultModel) {
				showCitiesListOnScreen((CityListResultModel) serviceResponse.getResponseObject());
			}
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				showProgressDialog(getString(R.string.prog_loading));
				mUserProfileController.getData(ApiConstants.GET_CITY_LIST_FROM_SERVER, null);
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		case ApiConstants.GET_CITY_LIST_FROM_SERVER: {
			removeProgressDialog();
			if (serviceResponse.getResponseObject() instanceof CityListResultModel) {
				showCitiesListOnScreen((CityListResultModel) serviceResponse.getResponseObject());
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				if (mCitiesArrayList == null || mCitiesArrayList.isEmpty()) {
					finish();
				}
			}
			break;
		}
		}
	}

	/**
	 * @param cityListResult
	 */
	private void showCitiesListOnScreen(CityListResultModel cityListResult) {
		mCitiesArrayList.clear();
		mCitiesArrayList.addAll(cityListResult.getCity());
		mCityListAdapter.notifyDataSetChanged();
		setSelectableCurrentCityModel();
	}

	@Override
	protected void onReverseGeoServiceResponse(Intent intent) {
		super.onReverseGeoServiceResponse(intent);
		updateCurrentLocationView();
	}

	/**
	 * show/hide current location and its layout
	 */
	private void updateCurrentLocationView() {
		mReverseGeoCodedCityName = AppStaticVars.REVERSE_GEO_CODED_CITY;
		if (StringUtils.isNullOrEmpty(mReverseGeoCodedCityName)) {
			findViewById(R.id.relative_box_current_location).setVisibility(View.GONE);
		} else {
			findViewById(R.id.relative_box_current_location).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txv_value_current_location)).setText(mReverseGeoCodedCityName);
			setSelectableCurrentCityModel();
		}
	}

	/**
	 * 1. checks if mReverseGeoCodedCityName is in mCitiesArrayList <br/>
	 * 2. set click listener on relative_box_current_location accordingly
	 */
	private void setSelectableCurrentCityModel() {
		mSelectableCurrentCityModel = ProjectUtils.getSelectableCurrentCity(mCitiesArrayList, mReverseGeoCodedCityName);
		if (mSelectableCurrentCityModel == null) {
			findViewById(R.id.relative_box_current_location).setOnClickListener(null);
		} else {
			findViewById(R.id.relative_box_current_location).setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View pClickSource) {
		switch (pClickSource.getId()) {
		case R.id.relative_box_current_location: {
			closeWithUserCitySelectionCheck(mSelectableCurrentCityModel);
			break;
		}
		}
	}

	/**
	 * @param pSelectedCityObj
	 */
	private void closeWithUserCitySelectionCheck(CityModel pSelectedCityModel) {
		CityModel cityAsOnHome = GrouponGoPrefs.getCityToShowDeals(this);
		if (cityAsOnHome == null) {
			// user is here from gather interests screen or splash screen
			if (pSelectedCityModel == null) {
				ToastUtils.showToast(this, getString(R.string.msg_select_city));
				return;
			}
		} else {
			// user is here from home screen
			if (!ConnectivityUtils.isNetworkEnabled(this)) {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
				finish();
				return;
			}
		}
		if (pSelectedCityModel != null) {
			GrouponGoPrefs.setCityToShowDeals(this, pSelectedCityModel);
			GrouponGoPrefs.saveCitySelectedAtMillis(this);
			if (cityAsOnHome != null && !cityAsOnHome.getCity_name().equalsIgnoreCase(pSelectedCityModel.getCity_name())) {
				GrouponGoFiles.deleteCachedDealsJson(this);
			}
			setResult(RESULT_OK);
		}

		if (cityAsOnHome == null) {
			Intent intent = new Intent(this, HomeActivity.class);
			startActivity(intent);
		}
		finish();
	}

	@Override
	public void onBackPressed() {
		closeWithUserCitySelectionCheck(null);
	}
}
