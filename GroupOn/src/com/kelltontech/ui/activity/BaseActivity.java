package com.kelltontech.ui.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.kelltontech.application.BaseApplication;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.ui.widget.CustomAlert;

/**
 * This class is used as base-class for application-base-activity.
 */
public abstract class BaseActivity extends ActionBarActivity implements IScreen {

	private String	LOG_TAG	= getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onCreate()");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onResume()");
		}

		Application application = this.getApplication();
		if (application instanceof BaseApplication) {
			BaseApplication baseApplication = (BaseApplication) application;
			if (baseApplication.isAppInBackground()) {
				onAppResumeFromBackground();
			}
			baseApplication.onActivityResumed();
		}
	}

	/**
	 * This callback will be called after onResume if application is being
	 * resumed from background. <br/>
	 * 
	 * Subclasses can override this method to get this callback.
	 */
	protected void onAppResumeFromBackground() {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onAppResumeFromBackground()");
		}
	}

	/**
	 * This method should be called to force app assume itself not in
	 * background.
	 */
	public final void setAppNotInBackground() {
		Application application = this.getApplication();
		if (application instanceof BaseApplication) {
			BaseApplication baseApplication = (BaseApplication) application;
			baseApplication.setAppInBackground(false);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onPause()");
		}

		Application application = this.getApplication();
		if (application instanceof BaseApplication) {
			BaseApplication baseApplication = (BaseApplication) application;
			baseApplication.onActivityPaused();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "onNewIntent()");
		}
	}

	/**
	 * @param serviceResponse
	 */
	@Override
	public final void handleUiUpdate(final ServiceResponse serviceResponse) {
		if (isFinishing()) {
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateUiDelegate(serviceResponse);
			}
		});
	}

	/**
	 * Users of this method should call this method only from UI thread.
	 */
	public final void updateUiDelegate(ServiceResponse serviceResponse) {
		if (BuildConfig.DEBUG) {
			updateUi(serviceResponse);
		} else {
			try {
				updateUi(serviceResponse);
			} catch (Exception e) {
				Log.e(LOG_TAG, "updateUi()", e);
			}
		}
	}

	/**
	 * Subclass should over-ride this method to update the UI with response,
	 * this base class promises to call this method from UI thread.
	 * 
	 * @param serviceResponse
	 */
	protected abstract void updateUi(ServiceResponse serviceResponse);

	// ////////////////////////////// show and hide ProgressDialog

	private ProgressDialog	mProgressDialog;

	/**
	 * Shows a simple native progress dialog<br/>
	 * Subclass can override below two methods for custom dialogs- <br/>
	 * 1. showProgressDialog <br/>
	 * 2. removeProgressDialog
	 * 
	 * @param bodyText
	 */
	public void showProgressDialog(String bodyText) {
		if (isFinishing()) {
			return;
		}
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(BaseActivity.this);
			mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setOnKeyListener(new Dialog.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_SEARCH) {
						return true; //
					}
					return false;
				}
			});
		}

		mProgressDialog.setMessage(bodyText);

		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
	}

	/**
	 * Removes the simple native progress dialog shown via showProgressDialog <br/>
	 * Subclass can override below two methods for custom dialogs- <br/>
	 * 1. showProgressDialog <br/>
	 * 2. removeProgressDialog
	 */
	public void removeProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	// ////////////////////////////// show and hide key-board

	/**
	 * shows the soft key pad
	 */
	public void showSoftKeypad() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				try {
					imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
				} catch (Exception e) {
					Log.e(LOG_TAG, "showSoftKeypad()", e);
				}
			}
		}, 100);
	}

	/**
	 * hides the soft key pad
	 */
	public void hideSoftKeypad() {
		hideSoftKeypad(getWindow().getCurrentFocus());
	}

	/**
	 * hides the soft key pad
	 */
	public void hideSoftKeypad(View view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		try {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		} catch (Exception e) {
			Log.e(LOG_TAG, "hideSoftKeypad()", e);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		View v = getCurrentFocus();
		boolean ret = super.dispatchTouchEvent(event);

		if (v instanceof EditText) {
			View w = getCurrentFocus();
			int scrcoords[] = new int[2];
			w.getLocationOnScreen(scrcoords);
			float x = event.getRawX() + w.getLeft() - scrcoords[0];
			float y = event.getRawY() + w.getTop() - scrcoords[1];

			if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {
				hideSoftKeypad();
			}
		}
		return ret;
	}

	/**
	 * @param pAlertId
	 * @param pAlertMessage
	 * @param pAlertTag
	 */
	public final void showCustomAlert(int pAlertId, String pAlertMessage, String pAlertTag) {
		CustomAlert customAlert = new CustomAlert();
		customAlert.setAlertId(pAlertId);
		customAlert.setTitle(getString(R.string.app_name));
		customAlert.setMessage(pAlertMessage);
		customAlert.setCancelable(false);
		customAlert.show(this, pAlertTag);
	}
}