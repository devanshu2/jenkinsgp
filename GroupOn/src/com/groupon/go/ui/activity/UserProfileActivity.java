package com.groupon.go.ui.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.facebook.model.GraphUser;
import com.groupon.go.R;
import com.groupon.go.application.AppStaticVars;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.UserProfileController;
import com.groupon.go.model.AwsConfigResponse.AwsConfigResult;
import com.groupon.go.model.CategoryModel;
import com.groupon.go.model.GetProfileResponse.GetProfileResultModel;
import com.groupon.go.model.UploadUserImageResponse.UploadUserImageResult;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.service.BackGroundService;
import com.groupon.go.ui.adapter.CategoriesGridAdapter;
import com.groupon.go.ui.dialog.CommonDialog;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.social.FacebookLoginHelper;
import com.kelltontech.social.FacebookLoginHelper.OnFacebookLoginListener;
import com.kelltontech.social.FacebookUtils;
import com.kelltontech.social.TwitterApp;
import com.kelltontech.social.TwitterApp.AuthResponseListener;
import com.kelltontech.social.TwitterPrefs;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.widget.CircularImageView;
import com.kelltontech.ui.widget.CustomGridView;
import com.kelltontech.ui.widget.PhotoOptionsDialog;
import com.kelltontech.utils.BitmapUtils;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;
import com.kelltontech.volleyx.VolleyManager;

/**
 * @author sachin.gupta
 */
public class UserProfileActivity extends DrawerBaseActivity implements OnClickListener, OnItemClickListener {

	private List<CategoryModel>		mCategoriesArrayList;
	private CategoriesGridAdapter	mCategoriesGridAdapter;

	private CircularImageView		mImgProfilePic;
	private EditText				mEdtUserName;
	private EditText				mEdtPhone;
	private EditText				mEdtEmail;
	private PhotoOptionsDialog		mPhotoOptionsDialog;
	private UserDetails				mUserProfile;
	private UserProfileController	mUserProfileController;

	private boolean					mIsProfilePicExists;
	private boolean					mIsCategorySelectionChanged;
	private boolean					mIsProfileOrCategoriesChanged;

	private ImageView				mImgFbChkBx;
	private FacebookLoginHelper		mFacebookLoginHelper;
	private TextView				mTxvFbConnectedAs;

	private ImageView				mImgTwitterChkBx;
	private TextView				mTxvTwitterConnectedAs;
	private TwitterPrefs			mTwitterPrefs;
	private Bitmap					mUserImageBitMap;
	private boolean					mIsIntiallyNoneSelected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);

		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.screen_header_my_profile));
		initializeDrawer();

		findViewById(R.id.img_header_cancel_btn).setOnClickListener(this);
		findViewById(R.id.img_header_done_btn).setOnClickListener(this);
		findViewById(R.id.linear_stored_cards).setOnClickListener(this);
		findViewById(R.id.linear_change_pnh_number).setOnClickListener(this);

		mImgProfilePic = (CircularImageView) findViewById(R.id.img_profile_pic);
		mImgProfilePic.setDiameter((int) (0.5f + getResources().getDimension(R.dimen.profile_screen_square_avatar_size)));
		mImgProfilePic.setFocusable(true);
		mImgProfilePic.setFocusableInTouchMode(true);
		mImgProfilePic.setOnClickListener(this);

		mImgProfilePic.setImageResource(R.drawable.ic_profile_pic_default);

		mEdtUserName = (EditText) findViewById(R.id.edt_profile_user_name);
		mEdtPhone = (EditText) findViewById(R.id.edt_profile_mobile);
		mEdtEmail = (EditText) findViewById(R.id.edt_profile_email);

		mEdtPhone.setEnabled(false);

		OnFocusChangeListener focusListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					getSupportActionBar().hide();
					setScreenMode(true, false);
				}
			}
		};
		mEdtUserName.setOnFocusChangeListener(focusListener);
		mEdtEmail.setOnFocusChangeListener(focusListener);

		mEdtUserName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					mEdtEmail.requestFocus();
					return true;
				}
				return false;
			}
		});

		mEdtEmail.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					hideSoftKeypad();
					return true;
				}
				return false;
			}
		});

		mImgFbChkBx = (ImageView) findViewById(R.id.img_facebook_chbx);
		mImgFbChkBx.setOnClickListener(this);
		mTxvFbConnectedAs = (TextView) findViewById(R.id.txv_connected_with_fb_as);

		mImgTwitterChkBx = (ImageView) findViewById(R.id.img_twitter_chbx);
		mImgTwitterChkBx.setOnClickListener(this);
		mTxvTwitterConnectedAs = (TextView) findViewById(R.id.txv_connected_with_twitter_as);

		mTwitterPrefs = new TwitterPrefs(this);

		updateTwitterLoginStatus(false);

		CustomGridView categoriesGridView = (CustomGridView) findViewById(R.id.grid_prefs);
		mCategoriesArrayList = new ArrayList<CategoryModel>();
		mCategoriesGridAdapter = new CategoriesGridAdapter(this, mCategoriesArrayList);
		categoriesGridView.setAdapter(mCategoriesGridAdapter);
		categoriesGridView.setOnItemClickListener(this);

		mUserProfileController = new UserProfileController(this, this);
		showProgressDialog(getString(R.string.prog_loading));
		mUserProfileController.getData(ApiConstants.GET_PROFILE_FROM_CACHE, null);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		FacebookUtils.requestFacebookUser(this);
		updateTwitterLoginStatus(false);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		showUserProfileDetails(false);
		if (intent.getBooleanExtra(Constants.EXTRA_NAME_SET_VIA_ADD_NEW_CARD, false)) {
			if (StringUtils.isNullOrEmpty(mUserProfile.getUsername())) {
				if (ConnectivityUtils.isNetworkEnabled(this)) {
					showProgressDialog(getString(R.string.prog_loading));
					mUserProfileController.getData(ApiConstants.GET_PROFILE_FROM_SERVER, true);
				} else {
					ToastUtils.showToast(this, getString(R.string.error_no_network));
				}
			}
		}
	}

	/**
	 * will be called from onClick of header-cross
	 */
	private void revertUserChanges() {
		mEdtUserName.setText(mUserProfile.getUsername());
		mEdtEmail.setText(mUserProfile.getEmailId());
		syncCategoriesSelectionStates(true);
		mCategoriesGridAdapter.notifyDataSetChanged();
	}

	/**
	 * will be called from onClick
	 */
	private void saveUserDetailsFromUI() {
		String enteredUserName = mEdtUserName.getText().toString().trim();
		mIsProfileOrCategoriesChanged = false;
		if (!enteredUserName.equals(mUserProfile.getUsername())) {
			mIsProfileOrCategoriesChanged = true;
		}
		String enteredEmail = mEdtEmail.getText().toString().trim();
		if (!StringUtils.isValidEmail(enteredEmail, true)) {
			ToastUtils.showToast(this, getString(R.string.error_invalid_email));
			return;
		}
		if (!enteredEmail.equals(mUserProfile.getEmailId())) {
			mIsProfileOrCategoriesChanged = true;
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		JSONArray jsonArray = checkCategorySelectionChanges();
		if (mIsProfileOrCategoriesChanged) {
			UserDetails userDetailsToBeSaved = new UserDetails();
			userDetailsToBeSaved.setUsername(enteredUserName);
			userDetailsToBeSaved.setEmailId(enteredEmail);
			userDetailsToBeSaved.setProfile_image("");
			userDetailsToBeSaved.setPreferences(jsonArray);
			showProgressDialog(getString(R.string.prog_loading));
			mUserProfileController.getData(ApiConstants.UPDATE_PROFILE, userDetailsToBeSaved);
		}
	}

	/**
	 * @param isEditingText
	 * @param isEditingPrefs
	 */
	private void setScreenMode(boolean isEditingText, boolean isEditingPrefs) {
		boolean isEditing = isEditingText || isEditingPrefs;
		if (isEditing) {
			getSupportActionBar().hide();
		} else {
			getSupportActionBar().show();
		}
		if (isEditingText) {
			showSoftKeypad();
		} else {
			hideSoftKeypad();
			mImgProfilePic.requestFocus();
		}
	}

	@Override
	public void onClick(View pClickSource) {
		int id = pClickSource.getId();
		switch (id) {
		case R.id.img_header_cancel_btn: {
			revertUserChanges();
			setScreenMode(false, false);
			break;
		}
		case R.id.img_header_done_btn: {
			saveUserDetailsFromUI();
			break;
		}
		case R.id.img_profile_pic: {
			showPhotoOptionsDialog();
			break;
		}
		case R.id.img_facebook_chbx: {
			toggleFacebookConnectionStatus();
			break;
		}
		case R.id.img_twitter_chbx: {
			toggleTwitterConnectionStatus();
			break;
		}
		case R.id.linear_stored_cards: {
			if (!ConnectivityUtils.isNetworkEnabled(this)) {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
				return;
			}
			Intent intent = new Intent(this, UserStoredCardsActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.linear_change_pnh_number: {
			Intent intent = new Intent(this, ChangeUserPhoneNumActivity.class);
			startActivity(intent);
			break;
		}
		}
	}

	/**
	 * initiate log in, if logged out and vice versa
	 */
	private void toggleFacebookConnectionStatus() {
		String updatedUserName = GrouponGoPrefs.getFbUserName(this);
		if (!StringUtils.isNullOrEmpty(updatedUserName)) {
			FacebookUtils.clearAccessToken();
			mImgFbChkBx.setImageResource(R.drawable.ic_unchecked);
			mTxvFbConnectedAs.setVisibility(View.GONE);
			GrouponGoPrefs.setFbUserName(this, "");
			return;
		}
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		if (mFacebookLoginHelper == null) {
			mFacebookLoginHelper = new FacebookLoginHelper(this, new OnFacebookLoginListener() {
				@Override
				public void onFacebookLoginComplete() {
					showProgressDialog(getString(R.string.prog_loading));
					FacebookUtils.requestFacebookUser(UserProfileActivity.this);
				}
			});
		}
		mFacebookLoginHelper.openFacebookSession();
	}

	/**
	 * initiate log in, if logged out and vice versa
	 */
	private void toggleTwitterConnectionStatus() {
		if (StringUtils.isNullOrEmpty(mTwitterPrefs.getScreenName())) {
			if (!ConnectivityUtils.isNetworkEnabled(this)) {
				ToastUtils.showToast(this, getString(R.string.error_no_network));
				return;
			}
			showTwitterLoginDialog();
		} else {
			mTwitterPrefs.storeAccessToken(null);
			updateTwitterLoginStatus(false);
		}
	}

	/**
	 * shows twitter login status on UI
	 */
	private void updateTwitterLoginStatus(boolean showLoginFailure) {
		String twitterUserName = mTwitterPrefs.getScreenName();
		if (StringUtils.isNullOrEmpty(twitterUserName)) {
			mImgTwitterChkBx.setImageResource(R.drawable.ic_unchecked);
			mTxvTwitterConnectedAs.setVisibility(View.GONE);
			if (showLoginFailure) {
				ToastUtils.showToast(getApplicationContext(), "Twitter login failed.");
			}
		} else {
			mImgTwitterChkBx.setImageResource(R.drawable.ic_checked);
			mTxvTwitterConnectedAs.setVisibility(View.VISIBLE);
			mTxvTwitterConnectedAs.setText(getString(R.string.connected_as) + " " + twitterUserName);
		}
	}

	/**
	 * shows login dialog for twitter
	 */
	private void showTwitterLoginDialog() {
		final TwitterApp twitterApp = new TwitterApp(this, getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));
		AuthResponseListener authResListener = new AuthResponseListener() {
			public void authResponse(boolean isSuccess, boolean isCancelled, String errorMsg) {
				if (isSuccess) {
					updateTwitterLoginStatus(true);
				} else if (isCancelled) {
					ToastUtils.showToast(getApplicationContext(), "Twitter login cancelled.");
				} else {
					if (StringUtils.isNullOrEmpty(errorMsg)) {
						ToastUtils.showToast(getApplicationContext(), "Twitter login failed.");
					} else {
						ToastUtils.showToast(getApplicationContext(), errorMsg);
					}
				}
			}
		};
		twitterApp.authorize(authResListener);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_PROFILE_FROM_CACHE: {
			if (serviceResponse.getResponseObject() instanceof GetProfileResultModel) {
				showProfileDetailsAndCategories((GetProfileResultModel) serviceResponse.getResponseObject());
			} else {
				// not possible
			}
			if (ConnectivityUtils.isNetworkEnabled(this)) {
				mUserProfileController.getData(ApiConstants.GET_PROFILE_FROM_SERVER, true);
			} else {
				FacebookUtils.requestFacebookUser(this);
				ToastUtils.showToast(this, getString(R.string.error_no_network));
			}
			break;
		}
		case ApiConstants.GET_PROFILE_FROM_SERVER: {
			if (serviceResponse.getResponseObject() instanceof GetProfileResultModel) {
				showProfileDetailsAndCategories((GetProfileResultModel) serviceResponse.getResponseObject());
				FacebookUtils.requestFacebookUser(this);
			} else {
				removeProgressDialog();
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
			}
			break;
		}
		case ApiConstants.UPDATE_PROFILE: {
			removeProgressDialog();
			ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			if (!serviceResponse.isSuccess()) {
				return;
			}
			GrouponGoPrefs.setUserName(this, mEdtUserName.getText().toString());
			GrouponGoPrefs.setUserEmail(this, mEdtEmail.getText().toString());
			mUserProfile.setUsername(mEdtUserName.getText().toString());
			mUserProfile.setEmailId(mEdtEmail.getText().toString());
			setScreenMode(false, false);
			if (mIsCategorySelectionChanged) {
				BackGroundService.start(this, ApiConstants.GET_PROFILE_FROM_SERVER);
				syncCategoriesSelectionStates(false);
				updateCategoriesSection();
				AppStaticVars.REQUEST_FIRST_PAGE_ON_HOME = true;
			}
			break;
		}
		case ApiConstants.GET_IMAGE_SIGNATURE: {
			if (!serviceResponse.isSuccess()) {
				removeProgressDialog();
				showUserProfileImage();
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				return;
			}
			AwsConfigResult awsConfig = (AwsConfigResult) serviceResponse.getResponseObject();
			awsConfig.setFile_data(BitmapUtils.getPNGImageData(mUserImageBitMap));
			uploadImageOnServer(awsConfig);
			break;
		}
		case ApiConstants.UPLOAD_USER_IMAGE: {
			String imageUrlFromAws = null;
			if (serviceResponse.getResponseObject() instanceof UploadUserImageResult) {
				UploadUserImageResult awsConfig = (UploadUserImageResult) serviceResponse.getResponseObject();
				imageUrlFromAws = awsConfig.getProfile_image();
			}
			if (StringUtils.isNullOrEmpty(imageUrlFromAws)) {
				removeProgressDialog();
				showUserProfileImage();
				if (serviceResponse.isSuccess() || StringUtils.isNullOrEmpty(serviceResponse.getErrorMessage())) {
					ToastUtils.showToast(this, getString(R.string.error_invalid_response_from_server));
				} else {
					ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				}
				return;
			}
			GrouponGoPrefs.setProfileImageUrl(this, imageUrlFromAws);
			mUserProfile.setProfile_image(imageUrlFromAws);
			mIsProfilePicExists = true;
			// below code is just to cache new profile json and pic
			VolleyManager.getImageLoader().get(imageUrlFromAws, new ImageListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					removeProgressDialog();
				}

				@Override
				public void onResponse(ImageContainer response, boolean isImmediate) {
					if (!isImmediate) {
						removeProgressDialog();
					}
				}
			});
			BackGroundService.start(this, ApiConstants.GET_PROFILE_FROM_SERVER);
			break;
		}
		case ApiConstants.REMOVE_USER_IMAGE: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				return;
			}
			GrouponGoPrefs.setProfileImageUrl(this, "");
			mUserProfile.setProfile_image("");
			showUserProfileImage();
			BackGroundService.start(this, ApiConstants.GET_PROFILE_FROM_SERVER);
			break;
		}
		}
	}

	/**
	 * @param profileResult
	 */
	private void showProfileDetailsAndCategories(GetProfileResultModel profileResult) {
		showUserProfileDetails(true);
		mCategoriesGridAdapter.setCategoryIconsBaseUrl(GrouponGoPrefs.getCategoryIconsBaseUrl(this));
		mCategoriesArrayList.clear();
		mCategoriesArrayList.addAll(profileResult.getCategory());
		updateCategoriesSection();
	}

	/**
	 * update user image by download or from cache<br/>
	 * set default image, if URL is null/blank
	 */
	private void showUserProfileImage() {
		if (StringUtils.isNullOrEmpty(mUserProfile.getProfile_image())) {
			mIsProfilePicExists = false;
			mImgProfilePic.setImageResource(R.drawable.ic_profile_pic_default);
		} else {
			mIsProfilePicExists = true;
			VolleyManager.getImageLoader().get(mUserProfile.getProfile_image(), new ImageListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					// nothing to do here
				}

				@Override
				public void onResponse(ImageContainer response, boolean isImmediate) {
					if (response.getBitmap() != null) {
						mImgProfilePic.setImageBitmap(response.getBitmap());
					}
				}
			});
		}
	}

	/**
	 * @param awsConfig
	 */
	private void uploadImageOnServer(AwsConfigResult awsConfig) {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mUserProfileController.getData(ApiConstants.UPLOAD_USER_IMAGE, awsConfig);
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.REQUEST_FACEBOOK_USER: {
			removeProgressDialog();
			updateFacebookLoginStatus(eventData);
			break;
		}
		case Events.PHOTO_OPTIONS_DIALOG: {
			if (eventData instanceof Bitmap) {
				showAndUploadSelectedBitmap((Bitmap) eventData);
			} else if (eventData instanceof Integer && (Integer) eventData == Events.EVENT_PHOTO_REMOVED) {
				CommonDialog couponDialog = new CommonDialog();
				Bundle bundle = new Bundle();
				bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.CONFIRM_REMOVE_PROFILE_PIC);
				couponDialog.setArguments(bundle);
				couponDialog.show(getSupportFragmentManager(), Constants.TAG_CONFIRM_REMOVE_STORED_CARD);
			}
			break;
		}
		case Events.CONFIRM_REMOVE_PROFILE_PIC: {
			if (eventData instanceof Boolean && (Boolean) eventData) {
				removeUserImageFromServer();
			}
			break;
		}
		}
	}

	/**
	 * @param pSelectedBitmap
	 */
	private void showAndUploadSelectedBitmap(Bitmap pSelectedBitmap) {
		mImgProfilePic.setImageBitmap(pSelectedBitmap);
		int squareSidePxToUpload = (int) (0.5f + getResources().getDimension(R.dimen.upload_profile_image_size));
		mUserImageBitMap = BitmapUtils.getScaledCroppedBitmap(pSelectedBitmap, squareSidePxToUpload, squareSidePxToUpload);
		getSignatureForImageUpload();
	}

	/**
	 * @param pImageAlso
	 */
	private void showUserProfileDetails(boolean pImageAlso) {
		mUserProfile = GrouponGoPrefs.getSavedUserDetails(this);
		if (mUserProfile.getUsername() == null) {
			mUserProfile.setUsername("");
		}
		if (mUserProfile.getEmailId() == null) {
			mUserProfile.setEmailId("");
		}
		mEdtUserName.setText(mUserProfile.getUsername());
		mEdtPhone.setText(mUserProfile.getCountryCode() + "-" + mUserProfile.getUserPhone());
		mEdtEmail.setText(mUserProfile.getEmailId());

		if (pImageAlso) {
			showUserProfileImage();
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void updateFacebookLoginStatus(Object pGraphUser) {
		String updatedUserName = GrouponGoPrefs.getFbUserName(this);
		if (pGraphUser instanceof GraphUser) {
			updatedUserName = FacebookUtils.getUserFbName((GraphUser) pGraphUser);
			GrouponGoPrefs.setFbUserName(this, updatedUserName);
		}
		if (StringUtils.isNullOrEmpty(updatedUserName)) {
			mImgFbChkBx.setImageResource(R.drawable.ic_unchecked);
			mTxvFbConnectedAs.setVisibility(View.GONE);
		} else {
			mImgFbChkBx.setImageResource(R.drawable.ic_checked);
			mTxvFbConnectedAs.setVisibility(View.VISIBLE);
			mTxvFbConnectedAs.setText(getString(R.string.connected_as) + " " + updatedUserName);
		}
	}

	/**
	 * @param pServerToTemp
	 */
	private void syncCategoriesSelectionStates(boolean pServerToTemp) {
		if (pServerToTemp && mIsIntiallyNoneSelected) {
			for (CategoryModel categoryModel : mCategoriesArrayList) {
				categoryModel.setSelectedTemp(true);
			}
			return;
		}
		boolean isAllSelectedTemp = true;
		for (CategoryModel categoryModel : mCategoriesArrayList) {
			if (pServerToTemp) {
				categoryModel.setSelectedTemp(categoryModel.isCat_selected());
			} else {
				categoryModel.setCat_selected(categoryModel.isSelectedTemp());
				if (isAllSelectedTemp && !categoryModel.isSelectedTemp()) {
					isAllSelectedTemp = false;
				}
			}
		}
		if (!pServerToTemp && isAllSelectedTemp) {
			for (CategoryModel categoryModel : mCategoriesArrayList) {
				categoryModel.setCat_selected(false);
			}
		}
	}

	/**
	 * @param categoryModels
	 */
	private void updateCategoriesSection() {
		mIsIntiallyNoneSelected = true;
		for (CategoryModel categoryModel : mCategoriesArrayList) {
			if (categoryModel.isCat_selected()) {
				mIsIntiallyNoneSelected = false;
				break;
			}
		}
		mCategoriesGridAdapter.notifyDataSetChanged();
	}

	/**
	 * set onItemClick listener on preference gridView item when user click on
	 * item method check that item is selected or unselected by boolean
	 * isSelected in model than according to that change the view by calling
	 * notifyDataSetChanged.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		CategoryModel model = mCategoriesGridAdapter.getItem(position);
		if (model != null) {
			if (model.isSelectedTemp()) {
				model.setSelectedTemp(false);
			} else {
				model.setSelectedTemp(true);
			}
		}
		mCategoriesGridAdapter.notifyDataSetChanged();
		checkCategorySelectionChanges();
	}

	/**
	 * @return
	 */
	private JSONArray checkCategorySelectionChanges() {
		boolean isAnySelectionChanged = false;
		JSONArray jsonArray = new JSONArray();
		for (CategoryModel categoryModel : mCategoriesArrayList) {
			if (categoryModel.isCat_selected() != categoryModel.isSelectedTemp()) {
				isAnySelectionChanged = true;
			}
			if (categoryModel.isSelectedTemp()) {
				jsonArray.put(categoryModel.getTid());
			}
		}
		if (jsonArray.length() == mCategoriesArrayList.size()) {
			jsonArray = new JSONArray();
			if (mIsIntiallyNoneSelected) {
				isAnySelectionChanged = false;
			}
		}
		if (isAnySelectionChanged) {
			mIsCategorySelectionChanged = true;
			mIsProfileOrCategoriesChanged = true;
			setScreenMode(false, true);
		} else {
			mIsCategorySelectionChanged = false;
			setScreenMode(false, false);
		}

		return jsonArray;
	}

	/**
	 * Method to open Photo Options Dialog
	 */
	private void showPhotoOptionsDialog() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		if (mPhotoOptionsDialog == null) {
			mPhotoOptionsDialog = new PhotoOptionsDialog();
		}
		Bundle bundle = new Bundle();
		bundle.putBoolean(PhotoOptionsDialog.EXTRA_REMOVE_OPTION_NEEDED, mIsProfilePicExists);
		mPhotoOptionsDialog.setArguments(bundle);
		mPhotoOptionsDialog.show(getSupportFragmentManager(), PhotoOptionsDialog.TAG_PHOTO_OPTIONS_DIALOG);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mFacebookLoginHelper != null) {
			mFacebookLoginHelper.onActivityStart();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mFacebookLoginHelper != null) {
			mFacebookLoginHelper.onActivityStop();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		FacebookUtils.onActivityResult(this, requestCode, resultCode, data);
		if (mPhotoOptionsDialog != null) {
			mPhotoOptionsDialog.onActivityResultDelegate(requestCode, resultCode, data);
		}
	}

	/**
	 * 
	 */
	private void getSignatureForImageUpload() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mUserProfileController.getData(ApiConstants.GET_IMAGE_SIGNATURE, null);
	}

	/**
	 * 
	 */
	private void removeUserImageFromServer() {
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));
		mUserProfileController.getData(ApiConstants.REMOVE_USER_IMAGE, null);
	}
}
