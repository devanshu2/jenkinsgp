package com.groupon.go.ui.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.groupon.go.R;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.utils.ProjectUtils;
import com.squareup.picasso.Picasso;

/**
 * @author vineet.rajpoot
 */
public class DealDetailPagerAdapter extends PagerAdapter {

	private Activity			mActivity;
	private LayoutInflater		mLayoutInflater;

	private ArrayList<String>	mDealImages;

	private String				mBaseImageUrl;
	private int					mDealImageHeight;
	private int					mDealImageWidth;

	/**
	 * @param images
	 * @param activity
	 */
	public DealDetailPagerAdapter(ArrayList<String> images, Activity activity) {
		mDealImages = images;
		mLayoutInflater = activity.getLayoutInflater();
		mBaseImageUrl = GrouponGoPrefs.getDealsImageBaseUrl(activity);
		mActivity = activity;

		DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
		mDealImageWidth = displayMetrics.widthPixels;
		mDealImageHeight = (int) activity.getResources().getDimension(R.dimen.deal_details_pager_item_height);
	}

	@Override
	public int getCount() {
		return mDealImages.size();
	}

	@Override
	public Object instantiateItem(ViewGroup viewGroup, int position) {
		View imageLayout = mLayoutInflater.inflate(R.layout.item_pager_deal_detail, viewGroup, false);
		ImageView imageView = (ImageView) imageLayout.findViewById(R.id.deal_background);
		if (mDealImages.get(position) != null) {
			Picasso.with(mActivity.getApplicationContext()).load(ProjectUtils.createImageUrl(mBaseImageUrl, mDealImages.get(position))).resize(mDealImageWidth, mDealImageHeight).placeholder(R.drawable.ic_default_deal_bg).centerCrop().into(imageView);
		} else {
			imageView.setImageResource(R.drawable.ic_default_deal_bg);
		}
		viewGroup.addView(imageLayout);
		return imageLayout;
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
}
