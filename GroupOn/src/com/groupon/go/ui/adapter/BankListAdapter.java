package com.groupon.go.ui.adapter;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.model.GetAllBanksResponse.BankDetailModel;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class BankListAdapter extends ArrayAdapter<BankDetailModel> {

	private Filter						mFilter;
	private ArrayList<BankDetailModel>	mBankListShown;
	private ArrayList<BankDetailModel>	mBackupList;
	private int							mSelectedBgColor;

	public BankListAdapter(Activity activity, ArrayList<BankDetailModel> bankList) {
		super(activity, R.layout.list_item_bank, bankList);
		mBankListShown = bankList;
		mBackupList = new ArrayList<BankDetailModel>(mBankListShown);
		mSelectedBgColor = activity.getResources().getColor(R.color.common_yellow_bg_color);
		mFilter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				mBankListShown.clear();
				if (constraint == null || constraint.toString().trim().length() == 0) {
					mBankListShown.addAll(mBackupList);
				} else {
					for (BankDetailModel bankDetailModel : mBackupList) {
						if (bankDetailModel.getName() != null && bankDetailModel.getName().toLowerCase(Locale.ENGLISH).contains(constraint.toString().trim().toLowerCase(Locale.ENGLISH))) {
							mBankListShown.add(bankDetailModel);
						} else {
							bankDetailModel.setSelected(false);
						}
					}
				}
				// assign filtered values and count to the FilterResults object
				FilterResults filterResults = new FilterResults();
				filterResults.values = mBankListShown;
				filterResults.count = mBankListShown.size();
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

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_item_bank, null);
			holder = new ViewHolder();
			holder.txvBankName = (TextView) convertView.findViewById(R.id.txv_bank_name);
			holder.rootLayout = (LinearLayout) convertView.findViewById(R.id.item_bank_root);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		BankDetailModel bankDetailModel = getItem(position);
		if (bankDetailModel.isSelected()) {
			holder.rootLayout.setBackgroundColor(mSelectedBgColor);
		} else {
			holder.rootLayout.setBackgroundResource(0);
		}
		holder.txvBankName.setText(bankDetailModel.getName());
		return convertView;
	}

	private class ViewHolder {
		TextView		txvBankName;
		LinearLayout	rootLayout;
	}
}