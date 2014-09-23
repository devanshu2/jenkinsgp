package com.groupon.go.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author vineet.kumar
 */
public class CategoryModel implements Parcelable {

	private int		tid;
	private String	cat_name;
	private String	cat_icon_selected;
	private String	cat_icon_unselected;

	private boolean	cat_selected;
	private boolean	isSelectedTemp;

	/**
	 * @return the tid
	 */
	public int getTid() {
		return tid;
	}

	/**
	 * @param tid
	 *            the tid to set
	 */
	public void setTid(int tid) {
		this.tid = tid;
	}

	/**
	 * @return the Cat_name
	 */
	public String getCat_name() {
		return cat_name;
	}

	/**
	 * @param cat_name
	 *            the cat_name to set
	 */
	public void setCat_name(String cat_name) {
		this.cat_name = cat_name;
	}

	/**
	 * @param cat_icon_selected
	 *            the cat_icon_selected to set
	 */
	public void setCat_icon_selected(String cat_icon_selected) {
		this.cat_icon_selected = cat_icon_selected;
	}

	/**
	 * @return the cat_icon_unselected
	 */
	public String getCat_icon_unselected() {
		return cat_icon_unselected;
	}

	/**
	 * @param cat_icon_unselected
	 *            the cat_icon_unselected to set
	 */
	public void setCat_icon_unselected(String cat_icon_unselected) {
		this.cat_icon_unselected = cat_icon_unselected;
	}

	/**
	 * @return the cat_icon_selected
	 */
	public String getCat_icon_selected() {
		return cat_icon_selected;
	}

	/**
	 * @return the cat_selected
	 */
	public boolean isCat_selected() {
		return cat_selected;
	}

	/**
	 * @param cat_selected
	 *            the cat_selected to set
	 */
	public void setCat_selected(boolean cat_selected) {
		this.cat_selected = cat_selected;
	}

	/**
	 * @return the isSelectedTemp
	 */
	public boolean isSelectedTemp() {
		return isSelectedTemp;
	}

	/**
	 * @param isSelectedTemp
	 *            the isSelectedTemp to set
	 */
	public void setSelectedTemp(boolean isSelectedTemp) {
		this.isSelectedTemp = isSelectedTemp;
	}

	// code for Parcelable

	/**
	 * Default constructor
	 */
	public CategoryModel() {
		// nothing to do here
	}

	/**
	 * @param in
	 *            to create object from Parcel
	 */
	public CategoryModel(Parcel in) {
		this.tid = in.readInt();
		this.cat_name = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(tid);
		dest.writeString(cat_name);
	}

	/**
	 * to create list of objects to/from parcel
	 */
	public static final Parcelable.Creator<CategoryModel>	CREATOR;

	static {
		CREATOR = new Parcelable.Creator<CategoryModel>() {
			@Override
			public CategoryModel createFromParcel(Parcel in) {
				return new CategoryModel(in);
			}

			@Override
			public CategoryModel[] newArray(int size) {
				return new CategoryModel[size];
			}
		};
	}

	@Override
	public int describeContents() {
		// nothing to do here
		return 0;
	}
}
