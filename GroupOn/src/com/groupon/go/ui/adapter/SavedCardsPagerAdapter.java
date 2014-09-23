package com.groupon.go.ui.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.model.SavedCardsModel;

/**
 * @author vineet.rajpoot
 */
public class SavedCardsPagerAdapter extends PagerAdapter {

	private ArrayList<SavedCardsModel>	mStoredCardsList;
	private LayoutInflater				mInflater;

	public SavedCardsPagerAdapter(ArrayList<SavedCardsModel> savCardsModels, Activity activity) {
		this.mStoredCardsList = savCardsModels;
		this.mInflater = activity.getLayoutInflater();
	}

	@Override
	public int getCount() {
		return mStoredCardsList.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public void finishUpdate(View container) {
		// nothing to do here
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		// nothing to do here
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View container) {
		// nothing to do here
	}

	@Override
	public Object instantiateItem(ViewGroup viewGroup, int position) {
		View cardview = mInflater.inflate(R.layout.item_pager_saved_cards, viewGroup, false);
		ImageView cardImage = (ImageView) cardview.findViewById(R.id.img_card);
		SavedCardsModel savedCardsModel = mStoredCardsList.get(position);
		String cardType = savedCardsModel.getCard_brand();
		if (cardType.equalsIgnoreCase(ApiConstants.VALUE_CARD_TYPE_MASTERCARD)) {
			cardImage.setImageResource(R.drawable.ic_mastercard);
		} else if (cardType.equalsIgnoreCase(ApiConstants.VALUE_CARD_TYPE_AMERICAN_EXPRESS)) {
			cardImage.setImageResource(R.drawable.ic_american_express);
		} else {
			cardImage.setImageResource(R.drawable.ic_visa);
		}
		TextView cardholderName = (TextView) cardview.findViewById(R.id.txv_card_holder_name);
		TextView cardNumber = (TextView) cardview.findViewById(R.id.txv_card_num);
		TextView cardExpDate = (TextView) cardview.findViewById(R.id.txv_card_exp);

		cardholderName.setText(savedCardsModel.getName());
		cardNumber.setText(savedCardsModel.getCard_number());
		cardExpDate.setText(savedCardsModel.getExpiry_month() + "/" + savedCardsModel.getExpiry_year());

		viewGroup.addView(cardview);
		return cardview;
	}
}
