package com.groupon.go.ui.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.database.SearchHistoryTable;
import com.groupon.go.model.SearchItemModel;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.model.BaseModel;
import com.kelltontech.utils.ToastUtils;

/**
 * @author vineet.kumar
 */
public class SearchListAdapter extends ArrayAdapter<BaseModel> implements OnClickListener {

	private LayoutInflater			mLayoutInflater;
	private ArrayList<BaseModel>	mSearchItemsList;
	private SearchHistoryTable		mSearchHistoryTable;

	/**
	 * @param activity
	 * @param searches
	 */
	public SearchListAdapter(Activity activity, ArrayList<BaseModel> searches, SearchHistoryTable searchHistoryTable) {
		super(activity, R.layout.list_item_search, searches);
		mLayoutInflater = activity.getLayoutInflater();
		mSearchHistoryTable = searchHistoryTable;
		mSearchItemsList = searches;
	}

	/**
	 * @param pSearchItemsList
	 */
	public void refreshListFromDb() {
		mSearchItemsList.clear();
		ArrayList<BaseModel> searchItemsList = mSearchHistoryTable.getAllData();
		mSearchItemsList.addAll(searchItemsList);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_item_search, null);
			viewHolder = new ViewHolder();
			viewHolder.mTxvSearch = (TextView) convertView.findViewById(R.id.txv_search_product);
			viewHolder.mImgCross = (ImageView) convertView.findViewById(R.id.img_search_cross);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		SearchItemModel searchItemModel = (SearchItemModel) getItem(position);
		String recentSearchStr = ProjectUtils.getCompleteSearchText(searchItemModel, true);
		viewHolder.mTxvSearch.setText(Html.fromHtml(recentSearchStr));

		viewHolder.mImgCross.setTag(position);
		viewHolder.mImgCross.setOnClickListener(this);
		return convertView;
	}

	private class ViewHolder {
		private TextView	mTxvSearch;
		private ImageView	mImgCross;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_search_cross: {
			if (v.getTag() instanceof Integer) {
				handleSearchItemDelete((Integer) v.getTag());
			}
			break;
		}
		}
	}

	/**
	 * @param pPosition
	 */
	private void handleSearchItemDelete(int pPosition) {
		BaseModel searchItemModel = mSearchItemsList.get(pPosition);
		boolean deleted = mSearchHistoryTable.deleteData(searchItemModel.getPrimaryKey());
		if (deleted) {
			mSearchItemsList.remove(pPosition);
			notifyDataSetChanged();
		} else {
			ToastUtils.showToast(getContext(), getContext().getString(R.string.error_generic_message));
		}
	}
}
