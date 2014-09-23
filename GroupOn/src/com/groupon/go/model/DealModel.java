package com.groupon.go.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class DealModel implements Parcelable {

	private int					cat_id;

	private String				opportunity_owner;

	private int					deal_id;
	private String				deal_title;
	private int					deal_sold_count;
	private double				deal_distance;
	private ArrayList<String>	deal_image;

	private int					offer_id;
	private String				offer_price;
	private double				offer_discount_money_value;
	private double				offer_discount_money_percentage;

	// added for my purchases
	private String				expiry_date;
	private int					voucher_count;
	private int					order_id;

	/**
	 * @return the order_id
	 */
	public int getOrder_id() {
		return order_id;
	}

	/**
	 * @param order_id
	 *            the order_id to set
	 */
	public void setOrder_id(int order_id) {
		this.order_id = order_id;
	}

	/**
	 * @return the expiry_date
	 */
	public String getExpiry_date() {
		return expiry_date;
	}

	/**
	 * @param expiry_date
	 *            the expiry_date to set
	 */
	public void setExpiry_date(String expiry_date) {
		this.expiry_date = expiry_date;
	}

	/**
	 * @return the voucher_count
	 */
	public int getVoucher_count() {
		return voucher_count;
	}

	/**
	 * @param voucher_count
	 *            the voucher_count to set
	 */
	public void setVoucher_count(int voucher_count) {
		this.voucher_count = voucher_count;
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
	 * @return the opportunity_owner
	 */
	public String getOpportunity_owner() {
		return opportunity_owner;
	}

	/**
	 * @param opportunity_owner
	 *            the opportunity_owner to set
	 */
	public void setOpportunity_owner(String opportunity_owner) {
		this.opportunity_owner = opportunity_owner;
	}

	/**
	 * @return the deal_id
	 */
	public int getDeal_id() {
		return deal_id;
	}

	/**
	 * @param deal_id
	 *            the deal_id to set
	 */
	public void setDeal_id(int deal_id) {
		this.deal_id = deal_id;
	}

	/**
	 * @return the deal_title
	 */
	public String getDeal_title() {
		return deal_title;
	}

	/**
	 * @param deal_title
	 *            the deal_title to set
	 */
	public void setDeal_title(String deal_title) {
		this.deal_title = deal_title;
	}

	/**
	 * @return the deal_sold_count
	 */
	public int getDeal_sold_count() {
		return deal_sold_count;
	}

	/**
	 * @param deal_sold_count
	 *            the deal_sold_count to set
	 */
	public void setDeal_sold_count(int deal_sold_count) {
		this.deal_sold_count = deal_sold_count;
	}

	/**
	 * @return the deal_distance
	 */
	public double getDeal_distance() {
		return deal_distance;
	}

	/**
	 * @param deal_distance
	 *            the deal_distance to set
	 */
	public void setDeal_distance(double deal_distance) {
		this.deal_distance = deal_distance;
	}

	/**
	 * @return the deal_image
	 */
	public ArrayList<String> getDeal_image() {
		return deal_image;
	}

	/**
	 * @param deal_image
	 *            the deal_image to set
	 */
	public void setDeal_image(ArrayList<String> deal_image) {
		this.deal_image = deal_image;
	}

	/**
	 * @return the offer_id
	 */
	public int getOffer_id() {
		return offer_id;
	}

	/**
	 * @param offer_id
	 *            the offer_id to set
	 */
	public void setOffer_id(int offer_id) {
		this.offer_id = offer_id;
	}

	/**
	 * @return the offer_price
	 */
	public String getOffer_price() {
		return offer_price;
	}

	/**
	 * @param offer_price
	 *            the offer_price to set
	 */
	public void setOffer_price(String offer_price) {
		this.offer_price = offer_price;
	}

	/**
	 * @return the offer_discount_money_value
	 */
	public double getOffer_discount_money_value() {
		return offer_discount_money_value;
	}

	/**
	 * @param offer_discount_money_value
	 *            the offer_discount_money_value to set
	 */
	public void setOffer_discount_money_value(double offer_discount_money_value) {
		this.offer_discount_money_value = offer_discount_money_value;
	}

	/**
	 * @return the offer_discount_money_percentage
	 */
	public double getOffer_discount_money_percentage() {
		return offer_discount_money_percentage;
	}

	/**
	 * @param offer_discount_money_percentage
	 *            the offer_discount_money_percentage to set
	 */
	public void setOffer_discount_money_percentage(double offer_discount_money_percentage) {
		this.offer_discount_money_percentage = offer_discount_money_percentage;
	}

	// code for Parcelable

	/**
	 * Default constructor
	 */
	public DealModel() {
		// nothing to do here
	}

	/**
	 * @param in
	 *            to create object from Parcel
	 */
	public DealModel(Parcel in) {
		this.cat_id = in.readInt();
		this.opportunity_owner = in.readString();

		this.deal_id = in.readInt();
		this.deal_title = in.readString();
		this.deal_sold_count = in.readInt();
		this.deal_distance = in.readDouble();
		int dealImageArrSize = in.readInt();
		if (dealImageArrSize >= 0) {
			deal_image = new ArrayList<String>(dealImageArrSize);
			for (int i = 0; i < dealImageArrSize; i++) {
				deal_image.add(in.readString());
			}
		}
		this.offer_id = in.readInt();
		this.offer_price = in.readString();
		this.offer_discount_money_value = in.readDouble();
		this.offer_discount_money_percentage = in.readDouble();
		this.expiry_date = in.readString();
		this.voucher_count = in.readInt();
		this.order_id = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(cat_id);
		parcel.writeString(opportunity_owner);

		parcel.writeInt(deal_id);
		parcel.writeString(deal_title);
		parcel.writeInt(deal_sold_count);
		parcel.writeDouble(deal_distance);
		if (deal_image == null) {
			parcel.writeInt(-1);
		} else {
			int dealImageArrSize = deal_image.size();
			parcel.writeInt(dealImageArrSize);
			for (String anUrl : deal_image) {
				parcel.writeString(anUrl);
			}
		}

		parcel.writeInt(offer_id);
		parcel.writeString(offer_price);
		parcel.writeDouble(offer_discount_money_value);
		parcel.writeDouble(offer_discount_money_percentage);
		parcel.writeString(expiry_date);
		parcel.writeInt(voucher_count);
		parcel.writeInt(order_id);
	}

	/**
	 * to create list of objects to/from parcel
	 */
	public static final Parcelable.Creator<DealModel>	CREATOR;

	static {
		CREATOR = new Creator<DealModel>() {
			public DealModel createFromParcel(Parcel source) {
				return new DealModel(source);
			}

			@Override
			public DealModel[] newArray(int size) {
				return new DealModel[size];
			}
		};
	}

	@Override
	public int describeContents() {
		// nothing to do here
		return 0;
	}
}
