package com.groupon.go.ui.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.demach.konotor.Konotor;
import com.groupon.go.R;
import com.groupon.go.model.NavDrawerItemModel;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.adapter.NavDrawerListAdapter;
import com.kelltontech.utils.StringUtils;

/**
 * @author vineet.rajpoot, sachin.gupta
 */
public abstract class DrawerBaseActivity extends GroupOnBaseActivity {

	private DrawerLayout			mDrawerLayout;
	private RelativeLayout			mDrawerWindow;
	private ActionBarDrawerToggle	mDrawerToggle;

	private boolean					mIsGooglePlayOpened;

	/**
	 * 
	 */
	protected void initializeDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerWindow = (RelativeLayout) findViewById(R.id.drawer_window);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_navigation_drawer, 0, 0);
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			// presumably, not relevant
		}
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		ArrayList<NavDrawerItemModel> drawerItemsList = createDrawerItemsList();
		NavDrawerListAdapter drawerItemsAdapter = new NavDrawerListAdapter(this, drawerItemsList);
		ListView drawerItemsListView = (ListView) findViewById(R.id.list_slidermenu);
		drawerItemsListView.setAdapter(drawerItemsAdapter);

		drawerItemsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mDrawerLayout.closeDrawer(mDrawerWindow);
				changeActivityView(position);
			}
		});
	}

	@Override
	protected void onResume() {
		if (mIsGooglePlayOpened) {
			mIsGooglePlayOpened = false;
			setAppNotInBackground();
		}
		super.onResume();

		// Instantiate Konotor.
		Konotor konotor = Konotor.getInstance(this);
		UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(this);
		if (StringUtils.isNullOrEmpty(userDetails.getUsername())) {
			konotor.withUserName(userDetails.getCountryCode() + "-" + userDetails.getUserPhone());
		} else {
			konotor.withUserName(userDetails.getUsername()); // optional name
		}
		if (!StringUtils.isNullOrEmpty(userDetails.getEmailId())) {
			konotor.withUserEmail(userDetails.getEmailId()); // optional email
		}
		konotor.withNoGcmRegistration(true);
		konotor.withFeedbackScreenTitle(getString(R.string.screen_header_feedback));
		konotor.withIdentifier(userDetails.getUserPhone()); // optional unique
		konotor.init(getString(R.string.konotor_app_id), getString(R.string.konotor_app_key));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * @return
	 */
	private ArrayList<NavDrawerItemModel> createDrawerItemsList() {
		ArrayList<NavDrawerItemModel> drawerItemsList = new ArrayList<NavDrawerItemModel>();
		String[] titlesArr = getResources().getStringArray(R.array.drawer_item);
		int[] iconsArr = { R.drawable.ic_home, R.drawable.ic_coupon,
				R.drawable.ic_profile, R.drawable.ic_share_and_earn,
				R.drawable.ic_rate_app, R.drawable.ic_about };
		NavDrawerItemModel model;
		for (int i = 0; i < 6; i++) {
			model = new NavDrawerItemModel();
			model.setTitle(titlesArr[i]);
			model.setImg_id(iconsArr[i]);
			drawerItemsList.add(model);
		}
		return drawerItemsList;
	}

	/**
	 * @param position
	 */
	private void changeActivityView(int position) {
		Class<?> newActivityClass = null;
		switch (position) {
		case 0: {
			newActivityClass = HomeActivity.class;
			break;
		}
		case 1: {
			newActivityClass = CouponsActivity.class;
			break;
		}
		case 2: {
			newActivityClass = UserProfileActivity.class;
			break;
		}
		case 3: {
			newActivityClass = MyCreditsActivity.class;
			break;
		}
		case 4: {
			Konotor.getInstance(this).launchFeedbackScreen(this);
			break;
		}
		case 5: {
			newActivityClass = AboutUsActivity.class;
			break;
		}
		}
		if (position != 4 && this.getClass() != newActivityClass) {
			Intent intent = new Intent(DrawerBaseActivity.this, newActivityClass);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		}
		mDrawerLayout.closeDrawer(mDrawerWindow);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
