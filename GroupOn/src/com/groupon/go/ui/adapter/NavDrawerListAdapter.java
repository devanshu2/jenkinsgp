package com.groupon.go.ui.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.model.NavDrawerItemModel;

public class NavDrawerListAdapter extends ArrayAdapter<NavDrawerItemModel> {

	private LayoutInflater	mInflator;
	private int				mBgColor1;
	private int				mBgColor2;

	public NavDrawerListAdapter(Activity activity, List<NavDrawerItemModel> objects) {
		super(activity, R.layout.nav_drawer_item, objects);
		mInflator = activity.getLayoutInflater();
		mBgColor1 = activity.getResources().getColor(R.color.list_nav_selecetd);
		mBgColor2 = activity.getResources().getColor(R.color.list_nav_unselecetd);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflator.inflate(R.layout.nav_drawer_item, null);
			holder = new ViewHolder();
			holder.linearItemRoot = convertView.findViewById(R.id.linear_nav_item_root);
			holder.txvNavDrawer = (TextView) convertView.findViewById(R.id.txv_nav_drawer);
			holder.imgNavDrawer = (ImageView) convertView.findViewById(R.id.drawer_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (position % 2 == 0) {
			holder.linearItemRoot.setBackgroundColor(mBgColor1);
		} else {
			holder.linearItemRoot.setBackgroundColor(mBgColor2);
		}

		holder.txvNavDrawer.setText(getItem(position).getTitle());
		holder.imgNavDrawer.setImageResource(getItem(position).getImg_id());
		return convertView;
	}

	/**
	 * The Class ViewHolder.
	 */
	private class ViewHolder {
		View		linearItemRoot;
		TextView	txvNavDrawer;
		ImageView	imgNavDrawer;
	}

}
