package com.groupon.go.ui.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.SharingDealsController;
import com.groupon.go.model.Contact;
import com.groupon.go.model.PhoneNumber;
import com.groupon.go.model.RegisteredMobNumResponse.RegisterMobNumResult;
import com.groupon.go.provider.ContactProvider;
import com.groupon.go.ui.adapter.ContactsListAdapter;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class ShareOnPhnBookActivity extends GroupOnBaseActivity implements OnClickListener, OnItemClickListener {

	private final static String		LOG_TAG	= "ShareOnPhnBookActivity";

	private SharingDealsController	mSharingDealsController;
	private ArrayList<Contact>		mContactsArrayList;
	private ContactsListAdapter		mContactsListAdapter;

	private TextView				mTxvCartIconWithCount;
	private EditText				mEdtSearch;

	private int						mMessageType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_share_on_phnbook);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mMessageType = getIntent().getIntExtra(Constants.EXTRA_MESSAGE_TYPE, 0);

		TextView txvHeader = (TextView) findViewById(R.id.merchant_name_header);
		TextView txvSubHeader = (TextView) findViewById(R.id.deal_cat_header);
		switch (mMessageType) {
		case ApiConstants.VALUE_MESSAGE_TYPE_SHARED_DEAL:
		case ApiConstants.VALUE_MESSAGE_TYPE_PURCHASED_DEAL:
		case ApiConstants.VALUE_MESSAGE_TYPE_REDEEMED_DEAL: {
			txvHeader.setText(getIntent().getStringExtra(Constants.EXTRA_MERCHANT_NAME));
			txvSubHeader.setText(getIntent().getStringExtra(Constants.EXTRA_CAT_NAME));
			break;
		}
		case ApiConstants.VALUE_MESSAGE_TYPE_UNIQUE_CODE: {
			txvHeader.setText(getString(R.string.label_my_promo_coupon));
			txvSubHeader.setVisibility(View.GONE);
			break;
		}
		case ApiConstants.VALUE_MESSAGE_TYPE_SHARE_APP: {
			txvHeader.setText(getString(R.string.label_share_the_app));
			txvSubHeader.setVisibility(View.GONE);
			break;
		}
		}

		findViewById(R.id.share_header_layout).setBackgroundResource(R.drawable.gradient_green_no_border_no_round);
		findViewById(R.id.img_share).setVisibility(View.GONE);

		mTxvCartIconWithCount = (TextView) findViewById(R.id.txv_header_cart);
		mTxvCartIconWithCount.setOnClickListener(this);
		mTxvCartIconWithCount.setVisibility(View.VISIBLE);

		View imgCart = findViewById(R.id.cart_image);
		imgCart.setVisibility(View.VISIBLE);
		imgCart.setOnClickListener(this);

		findViewById(R.id.header_right).setOnClickListener(this);
		findViewById(R.id.txv_share_with_phnbk).setOnClickListener(this);

		findViewById(R.id.img_search_cross).setOnClickListener(this);

		ListView contactsListView = (ListView) findViewById(R.id.list_share_phnfrnds);
		contactsListView.setOnItemClickListener(this);

		mContactsArrayList = new ArrayList<Contact>();
		mContactsListAdapter = new ContactsListAdapter(ShareOnPhnBookActivity.this, mContactsArrayList);
		contactsListView.setAdapter(mContactsListAdapter);

		mEdtSearch = (EditText) findViewById(R.id.edt_search_contacts);
		mEdtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// nothing to do here
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// nothing to do here
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mContactsListAdapter.getFilter().filter(s);
			}
		});

		mSharingDealsController = new SharingDealsController(this, this);

		new FetchContactList().execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateCartBadge(mTxvCartIconWithCount, false);
	}

	/**
	 * 
	 */
	private void getGroupOnFriendsFromServer() {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < mContactsArrayList.size(); i++) {
			Contact contact = mContactsArrayList.get(i);
			ArrayList<PhoneNumber> contacList = contact.getPhoneNumbers();
			for (int k = 0; k < contacList.size(); k++) {
				try {
					jsonArray.put(URLEncoder.encode(contacList.get(k).getMobile_no(), "UTF-8"));
				} catch (Exception e) {
					Log.e(LOG_TAG, "Indices: " + i + "," + k);
				}
			}
		}
		mSharingDealsController.getData(ApiConstants.UPDATE_CONTACT_LIST, jsonArray);
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.UPDATE_CONTACT_LIST: {
			removeProgressDialog();
			if (serviceResponse.isSuccess()) {
				updateGoContactsOnList(serviceResponse);
			} else {
				ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			}
			break;
		}
		case ApiConstants.API_SHARE_VIA_PHONEBOOK: {
			removeProgressDialog();
			ToastUtils.showToast(this, serviceResponse.getErrorMessage());
			break;
		}
		}
	}

	/**
	 * @param registeredNumbers
	 */
	private int reCreateListWithGoFriends(ArrayList<PhoneNumber> registeredNumbers) {
		ArrayList<Contact> listWithGoFriends = new ArrayList<Contact>();
		ArrayList<Contact> listWithoutGoFrnds = new ArrayList<Contact>();

		for (int contanctIndex = 0; contanctIndex < mContactsArrayList.size(); contanctIndex++) {
			Contact contact = mContactsArrayList.get(contanctIndex);
			boolean isAnyNumberReg = false;
			for (int contactNumIndex = 0; contactNumIndex < contact.getPhoneNumbers().size(); contactNumIndex++) {
				PhoneNumber contactNumber = contact.getPhoneNumbers().get(contactNumIndex);
				String contactNumberStr = contactNumber.getMobile_no();
				for (int regNumIndex = 0; regNumIndex < registeredNumbers.size(); regNumIndex++) {
					String registerNumber;
					try {
						registerNumber = URLDecoder.decode(registeredNumbers.get(regNumIndex).getMobile_no(), "UTF-8");
						if (registerNumber.equalsIgnoreCase(contactNumberStr)) {
							contactNumber.setGoUser(true);
							isAnyNumberReg = true;
							registeredNumbers.remove(regNumIndex);
						}
					} catch (Exception e) {
						Log.e(LOG_TAG, "Indices: " + contactNumIndex + "," + regNumIndex);
					}
				}
			}
			if (isAnyNumberReg) {
				listWithGoFriends.add(contact);
			} else {
				listWithoutGoFrnds.add(contact);
			}
		}

		mContactsArrayList.clear();
		mContactsArrayList.addAll(listWithGoFriends);
		mContactsArrayList.addAll(listWithoutGoFrnds);
		return listWithGoFriends.size();
	}

	/**
	 * @author vineet.rajpoot
	 */
	private class FetchContactList extends AsyncTask<String, Void, ArrayList<Contact>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressDialog(getString(R.string.prog_loading));
		}

		@Override
		protected ArrayList<Contact> doInBackground(String... params) {
			return ContactProvider.getAllContacts(getContentResolver(), getApplicationContext());
		}

		@Override
		protected void onPostExecute(ArrayList<Contact> result) {
			super.onPostExecute(result);
			if (result == null) {
				removeProgressDialog();
				ToastUtils.showToast(ShareOnPhnBookActivity.this, getString(R.string.error_generic_message));
				finish();
			} else if (result.size() == 0) {
				removeProgressDialog();
				ToastUtils.showToast(ShareOnPhnBookActivity.this, getString(R.string.msg_no_contacts));
				finish();
			} else {
				mContactsArrayList.addAll(result);
				mContactsListAdapter.setGoFriendsCount(0);
				mContactsListAdapter.notifyDataSetChanged();
				getGroupOnFriendsFromServer();
			}
		}
	}

	/**
	 * @param serviceResponse
	 */
	private void updateGoContactsOnList(ServiceResponse serviceResponse) {
		RegisterMobNumResult registerMobNumResult = (RegisterMobNumResult) serviceResponse.getResponseObject();
		ArrayList<PhoneNumber> registeredNumbers = registerMobNumResult.getRegistered_numbers();
		if (registeredNumbers.size() == 0) {
			findViewById(R.id.txv_share_title).setVisibility(View.GONE);
			return;
		}
		int goFriendsCount = reCreateListWithGoFriends(registeredNumbers);
		mContactsListAdapter.setGoFriendsCount(goFriendsCount);
		mContactsListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_right: {
			finish();
			break;
		}
		case R.id.cart_image:
		case R.id.txv_header_cart: {
			Intent intent = new Intent(this, UserCart.class);
			startActivity(intent);
			break;
		}
		case R.id.img_search_cross: {
			mEdtSearch.setText("");
			break;
		}
		case R.id.txv_share_with_phnbk: {
			sendRequestForShareDealWithFrnds();
			break;
		}
		}
	}

	/**
	 * 
	 */
	private void sendRequestForShareDealWithFrnds() {
		JSONArray jsonArrGoContacts = new JSONArray();
		JSONArray jsonArrNonGoContacts = new JSONArray();
		for (int i = 0; i < mContactsArrayList.size(); i++) {
			Contact contact = mContactsArrayList.get(i);
			ArrayList<PhoneNumber> phones = contact.getPhoneNumbers();
			if (!contact.isChecked()) {
				continue;
			}
			for (int k = 0; k < phones.size(); k++) {
				try {
					if (phones.get(k).isGoUser()) {
						jsonArrGoContacts.put(URLEncoder.encode(phones.get(k).getMobile_no(), "UTF-8"));
					} else {
						jsonArrNonGoContacts.put(URLEncoder.encode(phones.get(k).getMobile_no(), "UTF-8"));
					}
				} catch (UnsupportedEncodingException e) {
					Log.e(LOG_TAG, "Indices: " + i + "," + k);
				}
			}
		}
		if (jsonArrGoContacts.length() == 0 && jsonArrNonGoContacts.length() == 0) {
			// TODO - show any toast
			return;
		}
		showProgressDialog(getString(R.string.prog_loading));

		Bundle bundle = new Bundle();
		bundle.putString(Constants.EXTRA_GO_CONTACTS_JSON, jsonArrGoContacts.toString());
		bundle.putString(Constants.EXTRA_NON_GO_CONTACTS_JSON, jsonArrNonGoContacts.toString());
		bundle.putInt(Constants.EXTRA_MESSAGE_TYPE, mMessageType);

		switch (mMessageType) {
		case ApiConstants.VALUE_MESSAGE_TYPE_SHARED_DEAL:
		case ApiConstants.VALUE_MESSAGE_TYPE_PURCHASED_DEAL:
		case ApiConstants.VALUE_MESSAGE_TYPE_REDEEMED_DEAL: {
			bundle.putInt(Constants.EXTRA_DEAL_ID, getIntent().getIntExtra(Constants.EXTRA_DEAL_ID, 0));
			break;
		}
		}

		mSharingDealsController.getData(ApiConstants.API_SHARE_VIA_PHONEBOOK, bundle);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		Contact contact = mContactsListAdapter.getItem(position);
		RelativeLayout contactBg = (RelativeLayout) view.findViewById(R.id.root_item_contact_list);
		ImageView checkBox = (ImageView) view.findViewById(R.id.img_chbx);
		if (!contact.isChecked()) {
			contact.setChecked(true);
			contactBg.setBackgroundColor(getResources().getColor(R.color.common_yellow_bg_color));
			checkBox.setImageResource(R.drawable.ic_checked);
		} else {
			contact.setChecked(false);
			contactBg.setBackgroundResource(0);
			checkBox.setImageResource(R.drawable.ic_unchecked);
		}
	}
}