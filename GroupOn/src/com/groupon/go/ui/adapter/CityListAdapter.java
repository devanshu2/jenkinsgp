package com.groupon.go.ui.adapter;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.model.CityModel;

/**
 * @author sachin.gupta
 */
public class CityListAdapter extends ArrayAdapter<CityModel> {

	private LayoutInflater	mInflator;
	private int				mBgColor2;

	public CityListAdapter(Activity activity, List<CityModel> objects) {
		super(activity, R.layout.nav_drawer_item, objects);
		mInflator = activity.getLayoutInflater();
		mBgColor2 = activity.getResources().getColor(R.color.common_grey_3);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflator.inflate(R.layout.list_item_city, null);
			holder = new ViewHolder();
			holder.txvCityName = (TextView) convertView.findViewById(R.id.txv_city_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (position % 2 == 0) {
			holder.txvCityName.setBackgroundColor(Color.WHITE);
		} else {
			holder.txvCityName.setBackgroundColor(mBgColor2);
		}
		holder.txvCityName.setText(getItem(position).getCity_name());
		return convertView;
	}

	/**
	 * The Class ViewHolder.
	 */
	private class ViewHolder {
		TextView	txvCityName;
	}
}
