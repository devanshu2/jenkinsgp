package com.groupon.go.ui.fragment;

import com.kelltontech.network.ServiceResponse;
import com.kelltontech.ui.fragment.BaseFragment;

/**
 * @author sachn.guta
 */
public class GrouponBaseFragment extends BaseFragment {

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		// Subclass should over-ride this method to update the UI with response,
		// its super class promises to call this method from UI thread.
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		// nothing to do here
	}
}
