package com.groupon.go.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.model.OfferModel;
import com.kelltontech.utils.StringUtils;

/**
 * @author vineet.rajpoot
 */
public class CustomOfferDetaiView extends LinearLayout {

	private OnOfferItemClickListener	mListener;
	private OfferModel					mOfferModel;
	private String						mRupeeIconStr;

	/**
	 * @param context
	 */
	public CustomOfferDetaiView(Context context) {
		super(context);
	}

	/**
	 * @author vineet.rajpoot
	 */
	public interface OnOfferItemClickListener {
		public void onOfferItemClick(int offerID);
	}

	/**
	 * @param context
	 * @param offerModel
	 * @param rupeeIconStr
	 */
	public CustomOfferDetaiView(Context context, OfferModel offerModel, String rupeeIconStr) {
		this(context);
		if (context instanceof OnOfferItemClickListener) {
			mListener = (OnOfferItemClickListener) context;
		}
		mOfferModel = offerModel;
		mRupeeIconStr = rupeeIconStr;
		init(context);
	}

	/**
	 * @param context
	 */
	private void init(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rootView = inflater.inflate(R.layout.item_offer_expand, this, true);
		TextView offerName = (TextView) rootView.findViewById(R.id.exp_offer_name);
		TextView offerPrice = (TextView) rootView.findViewById(R.id.exp_offer_price);

		boolean isSold = mOfferModel.getOffer_sold_count() >= mOfferModel.getOffer_max_cap();
		if (mOfferModel.getVoucher_count() < 1 && isSold) {
			rootView.findViewById(R.id.txv_sold_out).setVisibility(View.VISIBLE);
		}
		offerName.setText(mOfferModel.getOffer_name());
		offerPrice.setText(mRupeeIconStr + StringUtils.getFormatDecimalAmount(mOfferModel.getOffer_price()));
		rootView.findViewById(R.id.offer_exp_main).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onOfferItemClick(mOfferModel.getOfferId());
				}
			}
		});
	}
}
