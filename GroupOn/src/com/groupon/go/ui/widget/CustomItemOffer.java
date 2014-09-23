package com.groupon.go.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.OfferModel;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.activity.BaseActivity;
import com.kelltontech.ui.widget.CustomTextView;
import com.kelltontech.utils.StringUtils;

/**
 * @author vineet.kumar, sachin.gupta
 */
public class CustomItemOffer {

	private BaseActivity		mBaseActivity;
	private OfferModel			mOfferModel;
	private int					mCounter;
	private CountChangeListener	mCountChangeListener;
	private EditText			mEdtOfferCount;
	private boolean				mIsFromCoupons;
	private int					mLastEditCounter;

	/**
	 * @author vineet.kumar
	 */
	public interface CountChangeListener {
		public void onOfferClickListener(OfferModel offerModel, int changeInOfferCount);
	}

	/**
	 * @param context
	 * @param listener
	 * @param offerModel
	 */
	public CustomItemOffer(BaseActivity pBaseActivity, CountChangeListener listener, OfferModel offerModel, boolean isFromCoupons) {
		mBaseActivity = pBaseActivity;
		mOfferModel = offerModel;
		mCountChangeListener = listener;
		if (isFromCoupons) {
			mCounter = offerModel.getVoucher_count();
		} else {
			mCounter = offerModel.getCurrent_counter();
		}
		mLastEditCounter = mCounter;
		mIsFromCoupons = isFromCoupons;
	}

	/**
	 * @param context
	 */
	public View createView() {
		LayoutInflater inflater = (LayoutInflater) mBaseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rootView = inflater.inflate(R.layout.offer_item, null, true);
		mEdtOfferCount = (EditText) rootView.findViewById(R.id.edt_offer_count);
		CustomTextView offer_name = (CustomTextView) rootView.findViewById(R.id.txv_offer_name);
		TextView offerPrice = (TextView) rootView.findViewById(R.id.offer_price);
		TextView offerDesc = (TextView) rootView.findViewById(R.id.offer_desc);

		boolean isSold = mOfferModel.getOffer_sold_count() >= mOfferModel.getOffer_max_cap();

		if (!mIsFromCoupons && !isSold) {
			addTextChangeListener();
		} else {
			mEdtOfferCount.setKeyListener(null);
			mEdtOfferCount.setFocusable(false);
			mEdtOfferCount.setFocusableInTouchMode(false);
		}

		offer_name.setText(mOfferModel.getOffer_name());
		offerDesc.setText(StringUtils.decode(mOfferModel.getOfferDes(), StringUtils.CHARSET_UTF_8, true));

		if (!mIsFromCoupons) {
			offerPrice.setText(mBaseActivity.getString(R.string.Rs) + " " + StringUtils.getFormatDecimalAmount(mOfferModel.getOffer_price()));
		}

		mEdtOfferCount.setText("" + mCounter);
		mEdtOfferCount.setSelection(1);

		CustomTextView minusTextView = (CustomTextView) rootView.findViewById(R.id.txv_btn_minus);

		minusTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCounter > 0) {
					mCounter--;
				} else {
					return;
				}
				mEdtOfferCount.setText(mCounter + "");

				if (mIsFromCoupons) {
					mOfferModel.setCurrent_counter(mCounter);
					mCountChangeListener.onOfferClickListener(mOfferModel, -1);
				}
			}
		});

		CustomTextView plusTextView = (CustomTextView) rootView.findViewById(R.id.txv_btn_plus);
		plusTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsFromCoupons && mCounter == mOfferModel.getVoucher_count()) {
					return;
				}
				mCounter++;
				mEdtOfferCount.setText(mCounter + "");
				if (mIsFromCoupons) {
					mOfferModel.setCurrent_counter(mCounter);
					mCountChangeListener.onOfferClickListener(mOfferModel, +1);
				}
			}
		});
		if (!mIsFromCoupons && isSold) {
			rootView.findViewById(R.id.root_edit_button).setVisibility(View.GONE);
			rootView.findViewById(R.id.divider_offer_count).setVisibility(View.GONE);
			rootView.findViewById(R.id.edt_offer_count).setVisibility(View.GONE);
			rootView.findViewById(R.id.txv_sold).setVisibility(View.VISIBLE);
			minusTextView.setOnClickListener(null);
			plusTextView.setOnClickListener(null);
		}

		return rootView;
	}

	/**
	 * 
	 */
	private void addTextChangeListener() {
		mEdtOfferCount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// nothing to do here
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// nothing to do here
			}

			@Override
			public void afterTextChanged(Editable text) {
				String count = text.toString().trim();
				if (StringUtils.isNullOrEmpty(count)) {
					mEdtOfferCount.setText("0");
					return;
				}
				if (count.startsWith("0") && count.length() > 1) {
					mEdtOfferCount.setText(count.substring(1));
					return;
				}
				try {
					mCounter = Integer.parseInt(count);
				} catch (Exception e) {
					// ignore
				}
				if (mCounter < 0) {
					mCounter = 0;
				}
				int maxAllowedCount = mOfferModel.getOffer_max_cap() - mOfferModel.getOffer_sold_count();
				if (mCounter > maxAllowedCount) {
					mCounter = maxAllowedCount;
					mEdtOfferCount.setText("" + mCounter);
					mBaseActivity.showCustomAlert(Events.ALERT_OFFER_COUNT_REVISED, mBaseActivity.getString(R.string.dlg_msg_added_more_offer), Constants.TAG_ALERT_OFFER_COUNT_REVISED);
				} else {
					mOfferModel.setCurrent_counter(mCounter);
					mEdtOfferCount.setSelection(String.valueOf(mCounter).length());
					if (mCounter != mLastEditCounter) {
						mCountChangeListener.onOfferClickListener(mOfferModel, mCounter - mLastEditCounter);
					}
					mLastEditCounter = mCounter;
				}
			}
		});
	}
}