package com.groupon.go.ui.adapter;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groupon.go.R;
import com.groupon.go.model.Contact;
import com.kelltontech.utils.BitmapUtils;
import com.kelltontech.utils.StringUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public class ContactsListAdapter extends ArrayAdapter<Contact> {

	private LayoutInflater		mInflator;
	private int					mPicDiameter;
	private Bitmap				mDefaultBitmap;
	private int					mSelectedBgColor;

	private Filter				mFilter;
	private ArrayList<Contact>	mContactsListShown;
	private ArrayList<Contact>	mBackupList;
	private int					mGoFriendsCount;

	/**
	 * @param activity
	 * @param contacts
	 */
	public ContactsListAdapter(Activity activity, ArrayList<Contact> contacts) {
		super(activity, R.layout.list_item_share_phnfrnds, contacts);
		mInflator = activity.getLayoutInflater();

		mContactsListShown = contacts;
		mBackupList = new ArrayList<Contact>(mContactsListShown);

		mSelectedBgColor = activity.getResources().getColor(R.color.common_yellow_bg_color);
		mPicDiameter = (int) (0.5f + activity.getResources().getDimension(R.dimen.profile_screen_square_avatar_size));
		mDefaultBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_contact_picture);

		mFilter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				mContactsListShown.clear();
				if (constraint == null || constraint.toString().trim().length() == 0) {
					mContactsListShown.addAll(mBackupList);
				} else {
					for (Contact contact : mBackupList) {
						if (contact.getDisplayName() != null && contact.getDisplayName().toLowerCase(Locale.ENGLISH).contains(constraint.toString().trim().toLowerCase(Locale.ENGLISH))) {
							mContactsListShown.add(contact);
						}
					}
				}
				// assign filtered values and count to the FilterResults object
				FilterResults filterResults = new FilterResults();
				filterResults.values = mContactsListShown;
				filterResults.count = mContactsListShown.size();
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
	 * This method also recreate backup list
	 * 
	 * @param pGoFriendsCount
	 */
	public void setGoFriendsCount(int pGoFriendsCount) {
		mBackupList = new ArrayList<Contact>(mContactsListShown);
		mGoFriendsCount = pGoFriendsCount;
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflator.inflate(R.layout.list_item_share_phnfrnds, null);
			holder = new ViewHolder();
			holder.frndName = (TextView) convertView.findViewById(R.id.txv_frnd_name);
			holder.frndImage = (ImageView) convertView.findViewById(R.id.img_frnd);
			holder.frndNumber = (TextView) convertView.findViewById(R.id.txv_frnd_num);
			holder.chckBox = (ImageView) convertView.findViewById(R.id.img_chbx);
			holder.rootLayout = (RelativeLayout) convertView.findViewById(R.id.root_item_contact_list);
			holder.bottomLine = (View) convertView.findViewById(R.id.go_frnds_divider);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Contact contact = getItem(position);
		if (!StringUtils.isNullOrEmpty(contact.getDisplayName())) {
			holder.frndName.setText(contact.getDisplayName());
		}

		if (contact.getPhoneNumbers().size() > 0) {
			if (!StringUtils.isNullOrEmpty(contact.getPhoneNumbers().get(0).getMobile_no())) {
				holder.frndNumber.setText(contact.getPhoneNumbers().get(0).getMobile_no());
			}
		}

		if (contact.getPhoneNumbers().size() > 0) {
			for (int i = 0; i < contact.getPhoneNumbers().size(); i++) {
				if (contact.getPhoneNumbers().get(i).isGoUser()) {
					holder.frndName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_go_icon, 0);
					break;
				} else {
					holder.frndName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				}
			}
		}
		if (!contact.isChecked()) {
			holder.rootLayout.setBackgroundResource(0);
			holder.chckBox.setImageResource(R.drawable.ic_unchecked);
		} else {
			holder.rootLayout.setBackgroundColor(mSelectedBgColor);
			holder.chckBox.setImageResource(R.drawable.ic_checked);
		}

		if (contact.getContactImage() != null) {
			holder.frndImage.setImageBitmap(BitmapUtils.getCircularCenterCropBitmap(contact.getContactImage(), mPicDiameter));
		} else {
			holder.frndImage.setImageBitmap(mDefaultBitmap);
		}

		if (mGoFriendsCount != 0) {
			if (position == mGoFriendsCount) {
				holder.bottomLine.setVisibility(View.VISIBLE);
			}
		}

		return convertView;
	}

	/**
	 * @author vineet.rajpoot
	 */
	private class ViewHolder {
		private TextView		frndName;
		private TextView		frndNumber;
		private ImageView		frndImage;
		private ImageView		chckBox;
		private RelativeLayout	rootLayout;
		private View			bottomLine;
	}
}
