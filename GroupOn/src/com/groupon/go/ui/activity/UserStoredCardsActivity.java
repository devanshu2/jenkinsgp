package com.groupon.go.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.PaymentController;
import com.groupon.go.model.GetPayUConfigResponse.GetPayUConfigResult;
import com.groupon.go.model.GetSavedCardsResponse.GetSavedCardsResult;
import com.groupon.go.model.SavedCardsModel;
import com.groupon.go.model.UserCardResponse;
import com.groupon.go.ui.dialog.CommonDialog;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.Events;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class UserStoredCardsActivity extends GroupOnBaseActivity implements OnClickListener {

	private PaymentController	mPaymentController;
	private boolean				mIsUserNameUpdateRequested;
	private SavedCardsModel		mStoredCardToBeDeleted;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_stored_cards);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.header_store_cards_title));

		findViewById(R.id.txv_btn_add_your_card).setOnClickListener(this);

		mPaymentController = new PaymentController(this, this);
		getStoredCardsFromServer();
	}

	/**
	 * shows a progress dialog and fetch stored cards list from server
	 */
	private void getStoredCardsFromServer() {
		showProgressDialog(getString(R.string.prog_loading));
		mPaymentController.getData(ApiConstants.GET_SAVED_CARDS, null);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_SAVED_CARDS: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
				finish();
			} else {
				populateViewWithCards(serviceResponse);
			}
			break;
		}
		case ApiConstants.GET_PAYU_CONFIGURATION: {
			if (mStoredCardToBeDeleted == null) {
				removeProgressDialog();
				ToastUtils.showToast(getApplicationContext(), getString(R.string.error_generic_message));
			} else if (serviceResponse.getResponseObject() instanceof GetPayUConfigResult) {
				GetPayUConfigResult payUConfigResult = (GetPayUConfigResult) serviceResponse.getResponseObject();
				HashMap<String, String> params = createParamsForDeleteCard(payUConfigResult);
				mPaymentController.getData(ApiConstants.DELETE_PAY_CARD_ON_PAY_U, params);
			} else {
				removeProgressDialog();
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		case ApiConstants.DELETE_PAY_CARD_ON_PAY_U: {
			if (serviceResponse.getResponseObject() instanceof UserCardResponse) {
				UserCardResponse saveUserCardResponse = (UserCardResponse) serviceResponse.getResponseObject();
				if (saveUserCardResponse.getStatus() == ApiConstants.PAYU_STATUS_FAIL) {
					removeProgressDialog();
					ToastUtils.showToast(this, saveUserCardResponse.getMsg());
				} else {
					mPaymentController.getData(ApiConstants.GET_SAVED_CARDS, null);
				}
			} else {
				removeProgressDialog();
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void populateViewWithCards(ServiceResponse serviceResponse) {
		GetSavedCardsResult getSavedCardsResult = (GetSavedCardsResult) serviceResponse.getResponseObject();
		ArrayList<SavedCardsModel> savedCards = getSavedCardsResult.getCards();
		LinearLayout cardsLayout = (LinearLayout) findViewById(R.id.linear_stored_cards_root);
		if (savedCards.isEmpty()) {
			findViewById(R.id.txv_stored_cards_title).setVisibility(View.GONE);
			cardsLayout.setVisibility(View.GONE);
			findViewById(R.id.linear_no_stored_cards_root).setVisibility(View.VISIBLE);
			return;
		}
		findViewById(R.id.txv_stored_cards_title).setVisibility(View.VISIBLE);
		findViewById(R.id.linear_no_stored_cards_root).setVisibility(View.GONE);
		cardsLayout.setVisibility(View.VISIBLE);
		cardsLayout.removeAllViews();
		for (int i = 0; i < savedCards.size(); i++) {
			final SavedCardsModel savedCardsModel = savedCards.get(i);
			if (StringUtils.isNullOrEmpty(savedCardsModel.getCard_number())) {
				continue;
			}
			View view = getLayoutInflater().inflate(R.layout.item_stored_cards, null);
			((TextView) view.findViewById(R.id.txv_user_name)).setText(savedCardsModel.getCard_name());
			((TextView) view.findViewById(R.id.txv_user_card_num)).setText(ProjectUtils.getFormatedCardNum(savedCardsModel.getCard_number()));
			((ImageView) view.findViewById(R.id.img_delete_card)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (StringUtils.isNullOrEmpty(savedCardsModel.getCard_token())) {
						ToastUtils.showToast(getApplicationContext(), getString(R.string.error_generic_message));
						return;
					}
					mStoredCardToBeDeleted = savedCardsModel;
					CommonDialog couponDialog = new CommonDialog();
					Bundle bundle = new Bundle();
					bundle.putInt(Constants.EXTRA_DIALOG_ID, Events.CONFIRM_REMOVE_STORED_CARD);
					bundle.putString(Constants.EXTRA_DIALOG_TITLE, mStoredCardToBeDeleted.getCard_number());
					couponDialog.setArguments(bundle);
					couponDialog.show(getSupportFragmentManager(), Constants.TAG_CONFIRM_REMOVE_STORED_CARD);
				}
			});
			ImageView cardImage = (ImageView) view.findViewById(R.id.img_card_type_default_visa);
			String cardType = savedCardsModel.getCard_brand();
			if (cardType.equalsIgnoreCase(ApiConstants.VALUE_CARD_TYPE_MASTERCARD)) {
				cardImage.setImageResource(R.drawable.ic_mastercard_small);
			} else if (cardType.equalsIgnoreCase(ApiConstants.VALUE_CARD_TYPE_AMERICAN_EXPRESS)) {
				cardImage.setImageResource(R.drawable.ic_american_express_small);
			}
			cardsLayout.addView(view);
		}
	}

	/**
	 * @param pPayUConfigResult
	 * @return
	 */
	private HashMap<String, String> createParamsForDeleteCard(GetPayUConfigResult pPayUConfigResult) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(ApiConstants.PARAM_PAYU_KEY, pPayUConfigResult.getKey());
		params.put(ApiConstants.PARAM_PAYU_COMMAND, pPayUConfigResult.getCommand());
		params.put(ApiConstants.PARAM_PAYU_HASH, pPayUConfigResult.getHash());
		params.put(ApiConstants.PARAM_USER_CREDENTIAL, pPayUConfigResult.getUser_token());
		params.put(ApiConstants.PARAM_PAYU_REDIRECT_URL, pPayUConfigResult.getService_url());
		params.put(ApiConstants.PARAM_PAY_CARD_TOKEN, mStoredCardToBeDeleted.getCard_token());
		return params;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mIsUserNameUpdateRequested) {
				showUserProfileActivity();
			} else {
				finish();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (mIsUserNameUpdateRequested) {
			showUserProfileActivity();
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * shows UserProfileActivity by clearing top instead of finish
	 */
	private void showUserProfileActivity() {
		Intent intent = new Intent(this, UserProfileActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(Constants.EXTRA_NAME_SET_VIA_ADD_NEW_CARD, true);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txv_btn_add_your_card: {
			Intent intent = new Intent(this, AddNewCardActivity.class);
			intent.putExtra(Constants.EXTRA_FROM_ACTIVITY, Constants.RQ_USER_CARDS_TO_ADD_NEW_CARD);
			startActivityForResult(intent, Constants.RQ_USER_CARDS_TO_ADD_NEW_CARD);
			break;
		}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.RQ_USER_CARDS_TO_ADD_NEW_CARD) {
			if (resultCode == RESULT_FIRST_USER) {
				mIsUserNameUpdateRequested = true;
				getStoredCardsFromServer();
			} else if (resultCode == RESULT_OK) {
				getStoredCardsFromServer();
			}
		}
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		super.onEvent(eventId, eventData);
		switch (eventId) {
		case Events.CONFIRM_REMOVE_STORED_CARD: {
			if (mStoredCardToBeDeleted == null) {
				ToastUtils.showToast(getApplicationContext(), getString(R.string.error_generic_message));
				return;
			}
			if (eventData instanceof Boolean && (Boolean) eventData) {
				showProgressDialog(getString(R.string.prog_loading));
				mPaymentController.getData(ApiConstants.GET_PAYU_CONFIGURATION, ApiConstants.VALUE_DELETE_CARD);
			}
			break;
		}
		}
	}
}