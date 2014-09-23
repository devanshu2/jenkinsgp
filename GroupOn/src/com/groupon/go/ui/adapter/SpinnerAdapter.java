package com.groupon.go.ui.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupon.go.R;

public class SpinnerAdapter extends ArrayAdapter<String>{

private LayoutInflater mInflator;
	
	public SpinnerAdapter(Activity context, ArrayList<String> list) {
		super(context, R.layout.item_country, list);
		mInflator = (LayoutInflater) context.getLayoutInflater();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		viewholder holder;
		if (convertView == null) {
			convertView = mInflator.inflate(R.layout.item_country, null);
			holder = new viewholder();
			holder.txt = (TextView) convertView.findViewById(R.id.country_name);
			holder.img = (ImageView) convertView.findViewById(R.id.country_flag);
			convertView.setTag(holder);
		} else {
			holder = (viewholder) convertView.getTag();
		}
		holder.txt.setText(getItem(position));
		holder.img.setImageResource(R.drawable.ic_flag);
		return convertView;
	}
	
	private class viewholder{
		private TextView txt;
		private ImageView img;
	}
}
