package com.groupon.go.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.kelltontech.model.BaseModel;

/**
 * common model for search suggestion item and search history item
 * 
 * @author vineet.kumar
 */
public class SearchItemModel extends BaseModel {

	private String	searchText;
	private int		cat_id;
	private String	cat_name;
	private long	searchedAtMillis;

	/**
	 * @return the searchText
	 */
	public String getSearchText() {
		return searchText;
	}

	/**
	 * @param searchText
	 *            the searchText to set
	 */
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	/**
	 * @return the cat_id
	 */
	public int getCat_id() {
		return cat_id;
	}

	/**
	 * @param cat_id
	 *            the cat_id to set
	 */
	public void setCat_id(int cat_id) {
		this.cat_id = cat_id;
	}

	/**
	 * @return the cat_name
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
	 * @return the searchedAtMillis
	 */
	public long getSearchedAtMillis() {
		return searchedAtMillis;
	}

	/**
	 * @param searchedAtMillis
	 *            the searchedAtMillis to set
	 */
	public void setSearchedAtMillis(long searchedAtMillis) {
		this.searchedAtMillis = searchedAtMillis;
	}

	@Override
	public String toString() {
		return searchText;
	}

	// code for Parcelable

	/**
	 * Default constructor
	 */
	public SearchItemModel() {
		// nothing to do here
	}

	/**
	 * @param in
	 *            to create object from Parcel
	 */
	public SearchItemModel(Parcel in) {
		this.searchText = in.readString();
		this.cat_id = in.readInt();
		this.cat_name = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(searchText);
		dest.writeInt(cat_id);
		dest.writeString(cat_name);
	}

	/**
	 * to create list of objects to/from parcel
	 */
	public static final Parcelable.Creator<SearchItemModel>	CREATOR;

	static {
		CREATOR = new Parcelable.Creator<SearchItemModel>() {
			@Override
			public SearchItemModel createFromParcel(Parcel in) {
				return new SearchItemModel(in);
			}

			@Override
			public SearchItemModel[] newArray(int size) {
				return new SearchItemModel[size];
			}
		};
	}

	@Override
	public int describeContents() {
		// nothing to do here
		return 0;
	}
}
