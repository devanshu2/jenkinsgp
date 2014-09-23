package com.groupon.go.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.groupon.go.R;

/**
 * @author sachin.gupta
 */
public class SuggestionsAdapter extends ArrayAdapter<String> {

	private LayoutInflater	mInflator;
	private List<String>	mSuggestionsList;
	private Filter			mFilterWithNoFiltering;

	/**
	 * @param activity
	 * @param suggestionsList
	 */
	public SuggestionsAdapter(Activity activity, ArrayList<String> suggestionsList) {
		super(activity, R.layout.list_item_search_suggestion, suggestionsList);
		mSuggestionsList = suggestionsList;
		mInflator = activity.getLayoutInflater();
		mFilterWithNoFiltering = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				// assign all values and count to the FilterResults object
				FilterResults filterResults = new FilterResults();
				filterResults.values = mSuggestionsList;
				filterResults.count = mSuggestionsList.size();
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence contraint, FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		};
	}

	/**
	 * @param searchText
	 * @param suggestionsList
	 */
	public void setSuggestions(String searchText, List<String> suggestionsList) {
		mSuggestionsList.clear();
		mSuggestionsList.addAll(suggestionsList);
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		return mFilterWithNoFiltering;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflator.inflate(R.layout.list_item_search_suggestion, null);
			holder = new ViewHolder();
			holder.txvSuggestion = (TextView) convertView.findViewById(R.id.txv_search_suggestion);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// SearchItemModel suggestion = getItem(position);
		// if (StringUtils.isNullOrEmpty(suggestion.getSearchText())) {
		// suggestion.setSearchText(mSearchText);
		// }
		// String suggestionStr = ProjectUtils.getCompleteSearchText(suggestion,
		// true);
		// holder.txvSuggestion.setText(Html.fromHtml(suggestionStr));

		holder.txvSuggestion.setText(getItem(position));
		return convertView;
	}

	/**
	 * The Class ViewHolder.
	 */
	private class ViewHolder {
		TextView	txvSuggestion;
	}
}
