package com.groupon.go.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.application.AppStaticVars;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.controller.UserProfileController;
import com.groupon.go.model.CityModel;
import com.groupon.go.model.UserDetails;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.utils.AnalyticsHelper;
import com.kelltontech.utils.ApplicationUtils;
import com.kelltontech.utils.ConnectivityUtils;

/**
 * Application starts from splash activity. <br/>
 * 
 * @author sachin.gupta
 */
public class PreSplashActivity extends Activity implements OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback, OnCompletionListener, OnErrorListener {

	private static final String	LOG_TAG	= "PreSplashActivity";

	private SurfaceHolder		mHolder;
	private MediaPlayer			mMediaPlayer;

	private boolean				mOnPreparedCalled;
	private int					mVideoWidth;
	private int					mVideoHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pre_splash);

		AnalyticsHelper.onActivityCreate(this);

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
		mHolder = surfaceView.getHolder();
		mHolder.addCallback(this);

		mMediaPlayer = new MediaPlayer();

		// set the listeners
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnVideoSizeChangedListener(this);

		AppStaticVars.resetDefaults(null);

		if (BuildConfig.DEBUG) {
			ApplicationUtils.printHashKey(this);
		}

		if (GrouponGoPrefs.canLaunchHome(this) && ConnectivityUtils.isNetworkEnabled(this)) {
			UserProfileController userProfileController = new UserProfileController(this, null);
			userProfileController.getData(ApiConstants.GET_PROFILE_FROM_SERVER, false);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		openNextScreenAsPerUserStatus();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onCompletion()");
		}
		SystemClock.sleep(1000);
		openNextScreenAsPerUserStatus();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "surfaceChanged()");
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "surfaceDestroyed()");
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "surfaceCreated()");
		}
		setMediaPlayerDataSource();
	}

	// ///////////////////////// method of OnPreparedListener interface

	@Override
	public void onPrepared(MediaPlayer mediaplayer) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onPrepared()");
		}
		mOnPreparedCalled = true;
		playVideo();
	}

	// /////////////////////// method of OnVideoSizeChangedListener interface

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onVideoSizeChanged(). width:" + width + ", height:" + height);
		}
		mVideoWidth = width;
		mVideoHeight = height;
		playVideo();
	}

	/**
	 * reset MediapPlayer and then setDataSource
	 */
	private void setMediaPlayerDataSource() {
		mMediaPlayer.setDisplay(mHolder); // mSurfaceCreated is true
		mMediaPlayer.reset();
		mVideoWidth = 0;
		mVideoHeight = 0;
		mOnPreparedCalled = false;
		try {
			Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.g14thjuly14_1280);
			mMediaPlayer.setDataSource(this, uri);
			mMediaPlayer.prepareAsync();
		} catch (Exception e) {
			Log.e(LOG_TAG, "setMediaPlayerDataSource: " + e);
			openNextScreenAsPerUserStatus();
		}
	}

	/**
	 * opens Next Screen As Per User Status
	 */
	private void openNextScreenAsPerUserStatus() {
		Intent intent = null;
		UserDetails userDetails = GrouponGoPrefs.getSavedUserDetails(this);
		if (userDetails.getUser_id() == 0) {
			intent = new Intent(PreSplashActivity.this, UserRegisterationActivity.class);
		} else {
			if (!GrouponGoPrefs.isCategoriesScreenClosed(this)) {
				intent = new Intent(PreSplashActivity.this, UserPrefrenceActivity.class);
			} else {
				CityModel cityToShowDeals = GrouponGoPrefs.getCityToShowDeals(this);
				if (cityToShowDeals == null) {
					intent = new Intent(PreSplashActivity.this, SelectYourCityActivity.class);
				} else {
					intent = new Intent(PreSplashActivity.this, HomeActivity.class);
				}
			}
		}
		if (intent != null) {
			startActivity(intent);
			finish();
		}
	}

	/**
	 * plays video or start splash in case of error
	 */
	private void playVideo() {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "playVideo()");
		}
		if (!mOnPreparedCalled || mVideoWidth == 0 || mVideoHeight == 0) {
			return;
		}
		try {
			mMediaPlayer.start();
		} catch (Exception e) {
			Log.e(LOG_TAG, "playVideo(): " + e);
			openNextScreenAsPerUserStatus();
		}
	}

	@Override
	public void onBackPressed() {
		// back is disabled
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMediaPlayer.release();
	}
}