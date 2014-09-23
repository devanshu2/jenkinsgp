package com.kelltontech.ui.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.groupon.go.BuildConfig;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.IScreen;
import com.kelltontech.ui.activity.BaseActivity;

/**
 * @author sachn.guta
 */
public abstract class BaseFragment extends Fragment implements IScreen {

	/**
	 * @return
	 */
	protected BaseActivity getBaseActivity() {
		FragmentActivity activity = getActivity();
		if (!(activity instanceof BaseActivity) || activity.isFinishing()) {
			return null;
		}
		return (BaseActivity) activity;
	}

	/**
	 * @param serviceResponse
	 */
	@Override
	public final void handleUiUpdate(final ServiceResponse serviceResponse) {
		BaseActivity baseActivity = getBaseActivity();
		if (baseActivity == null) {
			return;
		}
		baseActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				BaseActivity baseActivity = getBaseActivity();
				if (baseActivity == null) {
					return;
				}
				if (BuildConfig.DEBUG) {
					updateUi(serviceResponse);
				} else {
					try {
						updateUi(serviceResponse);
					} catch (Exception e) {
						Log.e(getClass().getSimpleName(), "updateUi()", e);
					}
				}
				baseActivity.updateUiDelegate(serviceResponse);
			}
		});
	}

	/**
	 * Subclass should over-ride this method to update the UI with response,
	 * this base class promises to this method from UI thread.
	 * 
	 * @param serviceResponse
	 */
	protected abstract void updateUi(ServiceResponse serviceResponse);
}
