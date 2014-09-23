package com.groupon.go.ui.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.groupon.go.R;
import com.groupon.go.application.AppStaticVars;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.controller.UserProfileController;
import com.groupon.go.model.CategoryModel;
import com.groupon.go.model.CityListResponse.CityListResultModel;
import com.groupon.go.model.CityModel;
import com.groupon.go.model.GetProfileResponse.GetProfileResultModel;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.adapter.CategoriesGridAdapter;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.service.ReverseGeoService;
import com.kelltontech.ui.Events;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.kumar, sachin.gupta
 */
public class UserPrefrenceActivity extends GroupOnBaseActivity implements OnClickListener, OnItemClickListener {

	private ArrayList<CategoryModel>	mCategoriesArrayList;
	private CategoriesGridAdapter		mCategoriesGridAdapter;
	private UserProfileController		mUserProfileController;
	private CategoryModel				mDummyCategoryForAllDeals;
	private boolean						mIsIntiallyNoneSelected;

	private List<CityModel>				mCitiesArrayList;
	private String						mReverseGeoCodedCityName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.fragment_prefrence);

		ProjectUtils.saveDeviceDensityGroupName(this);

		findViewById(R.id.txv_done).setOnClickListener(this);

		GridView categoriesGridView = (GridView) findViewById(R.id.grid_prefs);
		categoriesGridView.setOnItemClickListener(this);

		mDummyCategoryForAllDeals = new CategoryModel();
		mDummyCategoryForAllDeals.setCat_name(getString(R.string.category_name_all_deals));
		mDummyCategoryForAllDeals.setTid(ApiConstants.VALUE_TID_DUMMY_FOR_ALL_DEALS);

		mCategoriesArrayList = new ArrayList<CategoryModel>();
		mCategoriesGridAdapter = new CategoriesGridAdapter(this, mCategoriesArrayList);
		categoriesGridView.setAdapter(mCategoriesGridAdapter);

		mUserProfileController = new UserProfileController(this, this);
		mUserProfileController.getData(ApiConstants.GET_PROFILE_FROM_CACHE, null);

		if (ConnectivityUtils.isNetworkEnabled(this)) {
			mUserProfileController.getData(ApiConstants.GET_CITY_LIST_FROM_SERVER, null);
		} else {
			mUserProfileController.getData(ApiConstants.GET_CITY_LIST_FROM_CACHE, null);
		}
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_PROFILE_FROM_CACHE: {
			if (serviceResponse.getResponseObject() instanceof GetProfileResultModel) {
				showCategorisOnScreen((GetProfileResultModel) serviceResponse.getResponseObject());
			}
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				showProgressDialog(getString(R.string.prog_loading));
				mUserProfileController.getData(ApiConstants.GET_PROFILE_FROM_SERVER, true);
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		case ApiConstants.GET_PROFILE_FROM_SERVER: {
			if (serviceResponse.getResponseObject() instanceof GetProfileResultModel) {
				showCategorisOnScreen((GetProfileResultModel) serviceResponse.getResponseObject());

				if (LocationUtils.isGpsEnabled(this)) {
					if (ReverseGeoService.shouldStart(this)) {
						startReverseGeoService(false);
					} else {
						removeProgressDialog();
					}
				} else {
					removeProgressDialog();
					showLocationRequiredDialog();
				}
			} else {
				removeProgressDialog();
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				if (mCategoriesArrayList == null || mCategoriesArrayList.size() <= 1) {
					finish();
				}
			}
			break;
		}
		case ApiConstants.GET_CITY_LIST_FROM_CACHE:
		case ApiConstants.GET_CITY_LIST_FROM_SERVER: {
			if (serviceResponse.getResponseObject() instanceof CityListResultModel) {
				CityListResultModel cityListResult = (CityListResultModel) serviceResponse.getResponseObject();
				mCitiesArrayList = cityListResult.getCity();
			}
			break;
		}
		case ApiConstants.UPDATE_PROFILE: {
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				checkCityAndStartNextActivity();
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		}
	}

	/**
	 * @param pGetProfileResultModel
	 */
	private void showCategorisOnScreen(GetProfileResultModel pGetProfileResultModel) {
		mCategoriesArrayList.clear();
		mCategoriesArrayList.addAll(pGetProfileResultModel.getCategory());
		addDummyCategoryForAllDeals();
		mCategoriesGridAdapter.setCategoryIconsBaseUrl(GrouponGoPrefs.getCategoryIconsBaseUrl(this));
		mCategoriesGridAdapter.notifyDataSetChanged();
		checkDummyCategoryForAllDeals();
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.LOCATION_IS_OFF_DIALOG:
		case Events.GPS_IS_OFF_DIALOG: {
			if (eventData instanceof Boolean && (Boolean) eventData) {
				return; // location settings are opened
			}
			if (eventId == Events.GPS_IS_OFF_DIALOG) {
				if (ReverseGeoService.shouldStart(this)) {
					showProgressDialog(getString(R.string.prog_loading));
					startReverseGeoService(false);
				}
			}
			break;
		}
		}
	}

	@Override
	protected void onReverseGeoServiceResponse(Intent intent) {
		super.onReverseGeoServiceResponse(intent);
		removeProgressDialog();
		mReverseGeoCodedCityName = AppStaticVars.REVERSE_GEO_CODED_CITY;
	}

	/**
	 * checks if city is fetched by {@link ReverseGeoService} or not
	 */
	private void checkCityAndStartNextActivity() {
		GrouponGoPrefs.setCategoriesScreenClosed(this, true);
		CityModel selectableCurrentCityModel = ProjectUtils.getSelectableCurrentCity(mCitiesArrayList, mReverseGeoCodedCityName);

		Intent intent = null;
		if (selectableCurrentCityModel == null) {
			intent = new Intent(this, SelectYourCityActivity.class);
		} else {
			GrouponGoPrefs.setCityToShowDeals(this, selectableCurrentCityModel);
			intent = new Intent(this, HomeActivity.class);
		}
		setResult(RESULT_OK);
		startActivity(intent);
		finish();
	}

	/**
	 * set onItemClick listener on preference grid-view item when user click on
	 * item method check that item is selected or unselected by boolean
	 * isSelected in model than according to that change the view by calling
	 * notifyDataSetChanged.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		CategoryModel clickedModel = mCategoriesGridAdapter.getItem(position);
		if (clickedModel.isSelectedTemp()) {
			clickedModel.setSelectedTemp(false);
		} else {
			clickedModel.setSelectedTemp(true);
		}
		if (clickedModel.getTid() == ApiConstants.VALUE_TID_DUMMY_FOR_ALL_DEALS) {
			for (CategoryModel categoryModel : mCategoriesArrayList) {
				categoryModel.setSelectedTemp(clickedModel.isSelectedTemp());
			}
		} else {
			if (clickedModel.isSelectedTemp()) {
				checkDummyCategoryForAllDeals();
			} else {
				mDummyCategoryForAllDeals.setSelectedTemp(false);
			}
		}
		mCategoriesGridAdapter.notifyDataSetChanged();
	}

	/**
	 * checks if all categories are selected
	 */
	private void checkDummyCategoryForAllDeals() {
		boolean isAllSelected = true;
		for (CategoryModel categoryModel : mCategoriesArrayList) {
			if (categoryModel.getTid() == ApiConstants.VALUE_TID_DUMMY_FOR_ALL_DEALS) {
				continue;
			}
			if (!categoryModel.isSelectedTemp()) {
				isAllSelected = false;
				break;
			}
		}
		mDummyCategoryForAllDeals.setSelectedTemp(isAllSelected);
	}

	/**
	 * checks if all or none categories are selected
	 */
	private void addDummyCategoryForAllDeals() {
		int selectedCatsCount = 0;
		for (CategoryModel categoryModel : mCategoriesArrayList) {
			if (categoryModel.isCat_selected()) {
				selectedCatsCount++;
			}
		}
		if (selectedCatsCount == 0) {
			mIsIntiallyNoneSelected = true;
		}
		// show categories with an extra "All Deals" at first position
		if (selectedCatsCount == mCategoriesArrayList.size()) {
			mDummyCategoryForAllDeals.setSelectedTemp(true);
		}
		mCategoriesArrayList.add(0, mDummyCategoryForAllDeals);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txv_done: {
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				sendUpdatePreferencesRequest();
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		}
	}

	/**
	 * @return json array with category selection changes or null
	 */
	private JSONArray getSelectedCategoriesJsonArray() {
		if (mIsIntiallyNoneSelected && mDummyCategoryForAllDeals.isSelectedTemp()) {
			return null;
		}
		boolean isAnySelectionChanged = false;
		JSONArray jsonArray = new JSONArray();
		for (CategoryModel categoryModel : mCategoriesArrayList) {
			if (categoryModel.getTid() == ApiConstants.VALUE_TID_DUMMY_FOR_ALL_DEALS) {
				continue;
			}
			if (categoryModel.isCat_selected() != categoryModel.isSelectedTemp()) {
				isAnySelectionChanged = true;
			}
			if (categoryModel.isSelectedTemp()) {
				jsonArray.put(categoryModel.getTid());
			}
		}
		if (jsonArray.length() == mCategoriesArrayList.size() - 1) {
			jsonArray = new JSONArray();
		}
		return isAnySelectionChanged ? jsonArray : null;
	}

	/**
	 * checks category selection changes and <br/>
	 * send updateProfile request accordingly
	 */
	private void sendUpdatePreferencesRequest() {
		JSONArray jsonArray = getSelectedCategoriesJsonArray();
		if (jsonArray != null) {
			showProgressDialog(getString(R.string.prog_loading));
			UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(this);
			userDetails.setPreferences(jsonArray);
			mUserProfileController.getData(ApiConstants.UPDATE_PROFILE, userDetails);
		} else {
			checkCityAndStartNextActivity();
		}
	}
}
