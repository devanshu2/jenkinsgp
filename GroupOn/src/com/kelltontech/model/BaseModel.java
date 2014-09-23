package com.kelltontech.model;


import android.os.Parcel;
import android.os.Parcelable;
/**
 * To be used with all database tables.
 * @author kapil.vij
 */
public class BaseModel implements IModel, Parcelable {

	private int primaryKey;

	/**
	 * @return the primaryKey
	 */
	public int getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey the primaryKey to set
	 */
	public void setPrimaryKey(int primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	/**
	 * no-arg constructor
	 */
	public BaseModel() {
		// nothing to do here
	}

	/**
	 * Reconstruct from the Parcel
	 * 
	 * @param source
	 */
	public BaseModel(Parcel source) {
		primaryKey = source.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(primaryKey);
	}
	
	public static Parcelable.Creator<BaseModel>	CREATOR	= new Parcelable.Creator<BaseModel>() {
		@Override
		public BaseModel createFromParcel(Parcel source) {
			return new BaseModel(source);
		}
		@Override
		public BaseModel[] newArray(int size) {
			return new BaseModel[size];
		}
	};
}
