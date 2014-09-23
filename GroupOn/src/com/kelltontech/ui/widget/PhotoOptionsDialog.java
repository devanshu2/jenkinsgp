package com.kelltontech.ui.widget;

import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.groupon.go.R;
import com.groupon.go.constants.Constants;
import com.kelltontech.ui.Events;
import com.kelltontech.ui.IScreen;
import com.kelltontech.utils.ToastUtils;

/**
 * @author sachin.gupta
 */
public class PhotoOptionsDialog extends DialogFragment implements OnClickListener {

	/**
	 * public constants to be used from activities using this dialog
	 */
	public static final String	TAG_PHOTO_OPTIONS_DIALOG	= "tag_photo_options_dialog";
	public static final String	EXTRA_REMOVE_OPTION_NEEDED	= "extra_remove_option_needed";

	/**
	 * default constructor needed
	 */
	public PhotoOptionsDialog() {
		// nothing to do here
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title and frame from dialog-fragment
		setStyle(STYLE_NO_TITLE, R.style.Theme_Dialog_40);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_photo_edit_options, container, false);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

		view.findViewById(R.id.linear_dialog_root).setOnClickListener(this);
		view.findViewById(R.id.linear_dialog_visible_area).setOnClickListener(this);
		view.findViewById(R.id.btn_photo_option_take_new_photo).setOnClickListener(this);
		view.findViewById(R.id.btn_photo_option_choose_photo).setOnClickListener(this);
		view.findViewById(R.id.btn_photo_option_cancel).setOnClickListener(this);

		boolean isRemovePicNeeded = true;
		if (getArguments() != null) {
			isRemovePicNeeded = getArguments().getBoolean(EXTRA_REMOVE_OPTION_NEEDED);
		}
		if (isRemovePicNeeded) {
			view.findViewById(R.id.btn_photo_option_remove_photo).setOnClickListener(this);
		} else {
			view.findViewById(R.id.btn_photo_option_remove_photo).setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	public void onClick(View pClickSource) {
		switch (pClickSource.getId()) {
		case R.id.linear_dialog_root:
		case R.id.btn_photo_option_cancel: {
			dismiss();
			break;
		}
		case R.id.linear_dialog_visible_area: {
			// nothing to do here
			break;
		}
		case R.id.btn_photo_option_take_new_photo: {
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			try {
				activity.startActivityForResult(cameraIntent, Constants.RQ_CODE_OPEN_CAMERA_FOR_IMAGES);
			} catch (Exception exc) {
				ToastUtils.showToast(activity, getString(R.string.error_opening_camera), Toast.LENGTH_LONG);
			}
			break;
		}
		case R.id.btn_photo_option_choose_photo: {
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}
			Intent intent = new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
			try {
				activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.RQ_CODE_OPEN_GALLERY_FOR_IMAGES);
			} catch (Exception exc) {
				ToastUtils.showToast(activity, getString(R.string.error_opening_gallery), Toast.LENGTH_LONG);
			}
			break;
		}
		case R.id.btn_photo_option_remove_photo: {
			Activity activity = getActivity();
			if (activity instanceof IScreen) {
				((IScreen) activity).onEvent(Events.PHOTO_OPTIONS_DIALOG, Events.EVENT_PHOTO_REMOVED);
			}
			dismiss();
			break;
		}

		}
	}

	/**
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivityResultDelegate(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		IScreen iScreen = null;
		Activity activity = getActivity();
		if (activity instanceof IScreen) {
			iScreen = (IScreen) activity;
		} else {
			return;
		}
		switch (requestCode) {
		case Constants.RQ_CODE_OPEN_CAMERA_FOR_IMAGES: {
			if (data.getExtras() != null) {
				Object extraData = data.getExtras().get("data");
				if (extraData instanceof Bitmap) {
					iScreen.onEvent(Events.PHOTO_OPTIONS_DIALOG, (Bitmap) extraData);
					dismiss();
				}
			}
			break;
		}
		case Constants.RQ_CODE_OPEN_GALLERY_FOR_IMAGES: {
			Uri selectedImgFileUri = data.getData();
			if (selectedImgFileUri == null) {
				return;
			}
			try {
				InputStream input = activity.getContentResolver().openInputStream(selectedImgFileUri);
				Bitmap selectedPhotoBmp = BitmapFactory.decodeStream(input);
				if (selectedPhotoBmp != null) {
					iScreen.onEvent(Events.PHOTO_OPTIONS_DIALOG, selectedPhotoBmp);
				}
				dismiss();
			} catch (Throwable tr) {
				ToastUtils.showToast(activity, getString(R.string.error_saving_profile_pic), Toast.LENGTH_LONG);
			}
			break;
		}
		}
	}
}