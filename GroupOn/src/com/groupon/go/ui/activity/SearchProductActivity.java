package com.groupon.go.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.DealsController;
import com.groupon.go.controller.UserProfileController;
import com.groupon.go.database.SearchHistoryTable;
import com.groupon.go.model.CategoryModel;
import com.groupon.go.model.GetProfileResponse.GetProfileResultModel;
import com.groupon.go.model.SearchItemModel;
import com.groupon.go.model.SuggestionsResponse;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.adapter.CategoriesGridAdapter;
import com.groupon.go.ui.adapter.SearchListAdapter;
import com.groupon.go.ui.adapter.SuggestionsAdapter;
import com.kelltontech.model.BaseModel;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author sachin.gupta, vineet.kumar
 */
public class SearchProductActivity extends GroupOnBaseActivity implements OnClickListener, OnItemClickListener, OnEditorActionListener, TextWatcher {

	private ListView				mRecentSearchesListView;
	private SearchListAdapter		mRecentSearchesAdapter;
	private SearchHistoryTable		mSearchHistoryTable;

	private AutoCompleteTextView	mAutoCompleteSearch;
	private SuggestionsAdapter		mSuggestionsAdapter;
	private DealsController			mDealsController;

	private TextView				mTxvCartIconWithCount;

	private GridView				mCategoriesGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_product_search);

		findViewById(R.id.img_nav_back).setOnClickListener(this);
		findViewById(R.id.img_search_cross).setOnClickListener(this);

		mTxvCartIconWithCount = (TextView) findViewById(R.id.txv_header_cart);
		mTxvCartIconWithCount.setOnClickListener(this);
		mTxvCartIconWithCount.setVisibility(View.VISIBLE);

		View imgCart = findViewById(R.id.cart_image);
		imgCart.setVisibility(View.VISIBLE);
		imgCart.setOnClickListener(this);

		mAutoCompleteSearch = (AutoCompleteTextView) findViewById(R.id.auto_complete_txv_search);
		mAutoCompleteSearch.setOnEditorActionListener(this);
		mAutoCompleteSearch.setThreshold(2);
		mAutoCompleteSearch.setOnItemClickListener(this);
		mAutoCompleteSearch.addTextChangedListener(this);

		mSuggestionsAdapter = new SuggestionsAdapter(this, new ArrayList<String>());
		mAutoCompleteSearch.setAdapter(mSuggestionsAdapter);

		mRecentSearchesListView = (ListView) findViewById(R.id.list_recent_searches);
		mRecentSearchesListView.setOnItemClickListener(this);

		mSearchHistoryTable = new SearchHistoryTable(getApplication());
		ArrayList<BaseModel> recentSearchesArrayList = mSearchHistoryTable.getAllData();
		mRecentSearchesAdapter = new SearchListAdapter(this, recentSearchesArrayList, mSearchHistoryTable);
		mRecentSearchesListView.setAdapter(mRecentSearchesAdapter);

		mDealsController = new DealsController(this, this);

		new UserProfileController(this, this).getData(ApiConstants.GET_PROFILE_FROM_CACHE, null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateCartBadge(mTxvCartIconWithCount, false);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		if (parent == mCategoriesGridView) {
			CategoriesGridAdapter categoriesGridAdapter = (CategoriesGridAdapter) mCategoriesGridView.getAdapter();
			CategoryModel selectedCategory = categoriesGridAdapter.getItem(position);
			Intent intent = new Intent(this, CategoryDealsActivity.class);
			intent.putExtra(Constants.EXTRA_SELECTED_CATEGORY, selectedCategory);
			startActivity(intent);
			return;
		}
		SearchItemModel recentOrSuggestion = null;
		if (parent == mRecentSearchesListView) {
			recentOrSuggestion = (SearchItemModel) mRecentSearchesAdapter.getItem(position);
		} else {
			recentOrSuggestion = new SearchItemModel();
			recentOrSuggestion.setSearchText(mSuggestionsAdapter.getItem(position));
		}
		startSearchResultsActivity(recentOrSuggestion);
		mSearchHistoryTable.insertOrUpdate(recentOrSuggestion);
		mRecentSearchesAdapter.refreshListFromDb();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			handleUserInputSearchText(v.getText().toString().trim());
			return true;
		}
		return false;
	}

	/**
	 * @param pSearchText
	 */
	private void startSearchResultsActivity(SearchItemModel searchItemModel) {
		Intent intent = new Intent(this, SearchResultsActivity.class);
		intent.putExtra(Constants.EXTRA_SEARCH_TEXT, searchItemModel);
		startActivity(intent);
	}

	/**
	 * @param pSearchText
	 * @return
	 */
	private void handleUserInputSearchText(String pSearchText) {
		if (StringUtils.isNullOrEmpty(pSearchText)) {
			ToastUtils.showToast(this, getString(R.string.msg_enter_search_text));
			return;
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		SearchItemModel searchItemModel = new SearchItemModel();
		searchItemModel.setSearchText(pSearchText);
		startSearchResultsActivity(searchItemModel);
		mSearchHistoryTable.insertOrUpdate(searchItemModel);
		mRecentSearchesAdapter.refreshListFromDb();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_nav_back: {
			finish();
			break;
		}
		case R.id.img_search_cross: {
			mAutoCompleteSearch.setText("");
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
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_PROFILE_FROM_CACHE: {
			removeProgressDialog();
			if (serviceResponse.getResponseObject() instanceof GetProfileResultModel) {
				GetProfileResultModel profileResult = (GetProfileResultModel) serviceResponse.getResponseObject();
				updateCategoriesSection(profileResult);
			} else {
				// not possible
			}
			break;
		}
		case ApiConstants.GET_SEARCH_SUGGESTIONS: {
			if (serviceResponse.isSuccess() && serviceResponse.getResponseObject() instanceof SuggestionsResponse) {
				String searchText = (String) serviceResponse.getRequestData();
				SuggestionsResponse response = (SuggestionsResponse) serviceResponse.getResponseObject();
				mSuggestionsAdapter.setSuggestions(searchText, response.getResult());
			}
			break;
		}
		}
	}

	/**
	 * @param categoryModels
	 */
	private void updateCategoriesSection(GetProfileResultModel getProfileResultModel) {
		mCategoriesGridView = (GridView) findViewById(R.id.grid_prefs);
		CategoriesGridAdapter categoriesGridAdapter = new CategoriesGridAdapter(this, getProfileResultModel.getCategory());
		categoriesGridAdapter.setCategoryIconsBaseUrl(GrouponGoPrefs.getCategoryIconsBaseUrl(this));
		mCategoriesGridView.setAdapter(categoriesGridAdapter);
		mCategoriesGridView.setOnItemClickListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// nothing to do here
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String userInput = null;
		if (mAutoCompleteSearch.getText() != null) {
			userInput = mAutoCompleteSearch.getText().toString().trim();
		}
		if (userInput != null && userInput.length() > 2) {
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				mDealsController.getData(ApiConstants.GET_SEARCH_SUGGESTIONS, userInput);
			} else {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
		} else {
			mSuggestionsAdapter.clear();
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		// nothing to do here
	}
}
