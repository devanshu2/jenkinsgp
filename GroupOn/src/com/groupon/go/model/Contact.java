package com.groupon.go.model;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Contact {

	private String					mRowId;
	private String					mDisplayName;
	private ArrayList<PhoneNumber>	mPhoneNumbers;
	private Bitmap					mContactImage;
	private int						mContactId;
	private boolean					isChecked = false;

	/**
	 * @return the isChecked
	 */
	public boolean isChecked() {
		return isChecked;
	}

	/**
	 * @param isChecked the isChecked to set
	 */
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	/**
	 * @return the mContactImage
	 */
	public Bitmap getContactImage() {
		return mContactImage;
	}

	/**
	 * @param mContactImage
	 *            the mContactImage to set
	 */
	public void setContactImage(Bitmap mContactImage) {
		this.mContactImage = mContactImage;
	}

	/**
	 * @return the mRowId
	 */
	public String getRowId() {
		return mRowId;
	}

	/**
	 * @param mRowId
	 *            the mRowId to set
	 */
	public void setRowId(String mRowId) {
		this.mRowId = mRowId;
	}

	/**
	 * @return the mDisplayName
	 */
	public String getDisplayName() {
		return mDisplayName;
	}

	/**
	 * @param mDisplayName
	 *            the mDisplayName to set
	 */
	public void setDisplayName(String mDisplayName) {
		this.mDisplayName = mDisplayName;
	}

	/**
	 * @return the mPhoneNumbers
	 */
	public ArrayList<PhoneNumber> getPhoneNumbers() {
		return mPhoneNumbers;
	}

	/**
	 * @param mPhoneNumbers
	 *            the mPhoneNumbers to set
	 */
	public void setPhoneNumbers(ArrayList<PhoneNumber> mPhoneNumbers) {
		this.mPhoneNumbers = mPhoneNumbers;
	}

	/**
	 * @return the mContactId
	 */
	public int getContactId() {
		return mContactId;
	}

	/**
	 * @param mContactId
	 *            the mContactId to set
	 */
	public void setContactId(int mContactId) {
		this.mContactId = mContactId;
	}

}
