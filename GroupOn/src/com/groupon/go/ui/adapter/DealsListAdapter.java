package com.groupon.go.ui.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.DealModel;
import com.groupon.go.utils.ProjectUtils;
import com.squareup.picasso.Picasso;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class DealsListAdapter extends ArrayAdapter<DealModel> {

	private Activity		mActivity;
	private LayoutInflater	mLayoutInflater;

	private int				mCouponType;

	private String			mDealImagesBaseUrl;
	private int				mDealImageHeight;
	private int				mDealImageWidth;

	private String			mRupeeIconStr;
	private DecimalFormat	mDistanceFormatter;

	/**
	 * @param activity
	 * @param objects
	 * @param couponType
	 */
	public DealsListAdapter(Activity activity, ArrayList<DealModel> objects, int couponType) {
		super(activity, R.layout.list_item_deal, objects);
		mActivity = activity;
		mLayoutInflater = activity.getLayoutInflater();

		mCouponType = couponType;

		Resources resources = activity.getResources();
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		mDealImageWidth = displayMetrics.widthPixels - 2 * (int) resources.getDimension(R.dimen.deals_list_padding_and_divider);
		mDealImageHeight = (int) resources.getDimension(R.dimen.deals_list_item_height);

		mRupeeIconStr = activity.getString(R.string.Rs) + " ";
		mDistanceFormatter = new DecimalFormat("0.0");
	}

	/**
	 * @param pDealImagesBaseUrl
	 */
	public void setDealImagesBaseUrl(String pDealImagesBaseUrl) {
		mDealImagesBaseUrl = pDealImagesBaseUrl;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_item_deal, null);
			holder = new ViewHolder();
			holder.merchantName = (TextView) convertView.findViewById(R.id.deal_title);
			holder.dealBackground = (ImageView) convertView.findViewById(R.id.deal_background);
			holder.dealDiscount = (TextView) convertView.findViewById(R.id.deal_discount);
			holder.dealBought = (TextView) convertView.findViewById(R.id.deals_bought);
			holder.dealPrice = (TextView) convertView.findViewById(R.id.deal_price);
			holder.placeDistance = (TextView) convertView.findViewById(R.id.place_distance);
			holder.dealBoughtTag = (TextView) convertView.findViewById(R.id.deals_bought_tag);
			holder.dealPriceTag = (TextView) convertView.findViewById(R.id.deal_price_tag);
			holder.dealDiscountView = (LinearLayout) convertView.findViewById(R.id.lay_deal_discount);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		DealModel model = getItem(position);

		ArrayList<String> dealImage = model.getDeal_image();
		if (dealImage != null && !dealImage.isEmpty()) {
			Picasso.with(getContext()).load(ProjectUtils.createImageUrl(mDealImagesBaseUrl, dealImage.get(0))).placeholder(R.drawable.ic_default_deal_bg).error(R.drawable.ic_default_deal_bg).resize(mDealImageWidth, mDealImageHeight).centerCrop().into(holder.dealBackground);
		} else {
			holder.dealBackground.setImageResource(R.drawable.ic_default_deal_bg);
		}
		holder.merchantName.setText(model.getOpportunity_owner());

		if (model.getDeal_distance() > 0 && model.getDeal_distance() < Constants.MAX_DISTANCE_KM_TO_SHOW_ON_LISTS) {
			holder.placeDistance.setVisibility(View.VISIBLE);
			holder.placeDistance.setText(mDistanceFormatter.format(model.getDeal_distance()) + " km");
		} else {
			holder.placeDistance.setVisibility(View.GONE);
		}

		if (mCouponType != 0) {
			holder.dealPrice.setTextColor(mActivity.getResources().getColor(android.R.color.white));
			holder.dealPrice.setText(mActivity.getString(R.string.tag_expire_on));
			holder.dealPriceTag.setText(ProjectUtils.getFormattedDate(model.getExpiry_date()));
			holder.dealBought.setText("" + model.getVoucher_count());
			holder.dealBoughtTag.setText(mActivity.getString(R.string.tag_vouchers));
			holder.dealDiscountView.setVisibility(View.GONE);
		} else {
			holder.dealPrice.setText(mRupeeIconStr + model.getOffer_price());
			holder.dealBought.setText("" + model.getDeal_sold_count());
			holder.dealPriceTag.setVisibility(View.GONE);
			if (model.getOffer_discount_money_percentage() == 0) {
				holder.dealDiscount.setText(mRupeeIconStr + (int) model.getOffer_discount_money_value());
			} else {
				holder.dealDiscount.setText((int) model.getOffer_discount_money_percentage() + "%");
			}
		}

		return convertView;
	}

	private class ViewHolder {
		private ImageView		dealBackground;
		private TextView		merchantName;
		private TextView		placeDistance;
		private TextView		dealBought;
		private TextView		dealDiscount;
		private TextView		dealPrice;
		private LinearLayout	dealDiscountView;
		private TextView		dealPriceTag;
		private TextView		dealBoughtTag;
	}
}
