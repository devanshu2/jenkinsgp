package com.groupon.go.ui.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.groupon.go.R;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.CategoryModel;
import com.groupon.go.ui.activity.HomeActivity;
import com.groupon.go.ui.activity.SearchProductActivity;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.volleyx.VolleyManager;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class CategoriesGridAdapter extends ArrayAdapter<CategoryModel> {

	private LayoutInflater	mLayoutInflator;
	private String			mCategoryIconsBaseUrl;

	private boolean			mIsHomeScreen;
	private boolean			mShowSelectedStates;
	private boolean			mShowAllCategories;
	private int				mShowLessCategoriesCount;
	private boolean			mAnimatedAllItems;

	/**
	 * @param activity
	 * @param modules
	 */
	public CategoriesGridAdapter(Activity activity, List<CategoryModel> modules) {
		super(activity, R.layout.item_grid_category, modules);

		mLayoutInflator = (LayoutInflater) activity.getLayoutInflater();

		if (activity instanceof HomeActivity) {
			mIsHomeScreen = true;
			mShowSelectedStates = false;
			mShowAllCategories = false;
			int iconsPerRow = activity.getResources().getInteger(R.integer.columns_on_home_grid);
			mShowLessCategoriesCount = iconsPerRow * Constants.DEFAULT_CATEGORY_ROWS_ON_HOME;
		} else {
			mShowAllCategories = true;
			if (activity instanceof SearchProductActivity) {
				mShowSelectedStates = false;
			} else {
				mShowSelectedStates = true;
			}
		}
	}

	/**
	 * @param pCategoryIconsBaseUrl
	 */
	public void setCategoryIconsBaseUrl(String pCategoryIconsBaseUrl) {
		mCategoryIconsBaseUrl = pCategoryIconsBaseUrl;
	}

	/**
	 * @return the showAllCategories
	 */
	public boolean isShowAllCategories() {
		return mShowAllCategories;
	}

	/**
	 * @param showAllCategories
	 *            the showAllCategories to set
	 */
	public void setShowAllCategories(boolean showAllCategories) {
		this.mShowAllCategories = showAllCategories;
	}

	@Override
	public int getCount() {
		int actualCount = super.getCount();
		if (actualCount > mShowLessCategoriesCount && !mShowAllCategories) {
			return mShowLessCategoriesCount;
		} else {
			return actualCount;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mLayoutInflator.inflate(R.layout.item_grid_category, null);
			holder = new ViewHolder();
			holder.txvPrefsName = (TextView) convertView.findViewById(R.id.tv_module_name);
			holder.imgPrefsIcon = (NetworkImageView) convertView.findViewById(R.id.iv_module_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CategoryModel prefModel = getItem(position);
		if (StringUtils.isNullOrEmpty(prefModel.getCat_name())) {
			holder.txvPrefsName.setText("");
		} else {
			holder.txvPrefsName.setText(prefModel.getCat_name());
		}

		if (!mShowSelectedStates) {
			prefModel.setSelectedTemp(false);
		}

		if (mIsHomeScreen && !mAnimatedAllItems && position >= mShowLessCategoriesCount) {
			startAnimation(position, convertView);
			if (position + 1 == getCount()) {
				mAnimatedAllItems = true;
			}
		}

		ProjectUtils.setDefaultAndErrorIconForCategory(prefModel, holder.imgPrefsIcon);

		String imageUrl = null;
		if (prefModel.isSelectedTemp()) {
			imageUrl = ProjectUtils.createImageUrl(mCategoryIconsBaseUrl, prefModel.getCat_icon_selected());
		} else {
			imageUrl = ProjectUtils.createImageUrl(mCategoryIconsBaseUrl, prefModel.getCat_icon_unselected());
		}
		holder.imgPrefsIcon.setImageUrl(imageUrl, VolleyManager.getImageLoader());

		return convertView;
	}

	/**
	 * @param position
	 * @param convertView
	 */
	private void startAnimation(int position, View convertView) {
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
		fadeIn.setStartOffset(position * 100);
		fadeIn.setDuration(100);

		AnimationSet animation = new AnimationSet(true); // change to false
		animation.addAnimation(fadeIn);
		convertView.startAnimation(animation);
	}

	/**
	 * The Class ViewHolder.
	 */
	private class ViewHolder {
		NetworkImageView	imgPrefsIcon;
		TextView			txvPrefsName;
	}
}
