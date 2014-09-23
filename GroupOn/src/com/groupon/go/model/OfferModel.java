package com.groupon.go.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.groupon.go.model.DealDetailResponse.OfferLocations;
import com.kelltontech.model.BaseModel;

/**
 * @author vineet.rajpoot
 */
public class OfferModel extends BaseModel implements Parcelable {

	private int							offer_id;
	private int							offer_weight;
	private String						offer_description;
	private String						merchant_address;

	private int							offer_max_cap;
	private int							offer_sold_count;

	private float						offer_price;
	private String						offer_expected_expenditure_for;
	private float						offer_expected_expenditure_price;
	private float						offer_discount_money_value;
	private float						offer_discount_money_percentage;
	private String						offer_min_purchase_value;

	private String						offer_start_date;
	private String						offer_end_date;
	private String						offer_start_time;
	private String						offer_redem_end_date;
	private String						offer_redem_start_time;
	private String						offer_redem_end_time;
	private String						offer_redem_timestamp;
	private String						offer_valid_days;

	private int							quantity;
	private int							voucher_count;
	private String						voucher_codes;

	// below fields are not received from server
	private String						offer_name;
	private int							purchase_key;
	private int							current_counter;
	private ArrayList<OfferLocations>	offer_locations;
	private float						offer_min_distance;

	/**
	 * @return the offer_id
	 */
	public int getOfferId() {
		return offer_id;
	}

	/**
	 * @param offer_id
	 *            the offer_id to set
	 */
	public void setOfferId(int offer_id) {
		this.offer_id = offer_id;
	}

	/**
	 * @return the offer_weight
	 */
	public int getOffer_weight() {
		return offer_weight;
	}

	/**
	 * @param offer_weight
	 *            the offer_weight to set
	 */
	public void setOffer_weight(int offer_weight) {
		this.offer_weight = offer_weight;
	}

	/**
	 * @return the offer_description
	 */
	public String getOfferDes() {
		return offer_description;
	}

	/**
	 * @param offer_description
	 *            the offer_description to set
	 */
	public void setOfferDes(String offer_description) {
		this.offer_description = offer_description;
	}

	/**
	 * @return the merchant_address
	 */
	public String getMerchant_address() {
		return merchant_address;
	}

	/**
	 * @param merchant_address
	 *            the merchant_address to set
	 */
	public void setMerchant_address(String merchant_address) {
		this.merchant_address = merchant_address;
	}

	/**
	 * @return the offer_max_cap
	 */
	public int getOffer_max_cap() {
		return offer_max_cap;
	}

	/**
	 * @param offer_max_cap
	 *            the offer_max_cap to set
	 */
	public void setOffer_max_cap(int offer_max_cap) {
		this.offer_max_cap = offer_max_cap;
	}

	/**
	 * @return the offer_sold_count
	 */
	public int getOffer_sold_count() {
		return offer_sold_count;
	}

	/**
	 * @param offer_sold_count
	 *            the offer_sold_count to set
	 */
	public void setOffer_sold_count(int offer_sold_count) {
		this.offer_sold_count = offer_sold_count;
	}

	/**
	 * @return the offer_price
	 */
	public float getOffer_price() {
		return offer_price;
	}

	/**
	 * @param offer_price
	 *            the offer_price to set
	 */
	public void setOffer_price(float offer_price) {
		this.offer_price = offer_price;
	}

	/**
	 * @return the offer_expected_expenditure_for
	 */
	public String getOffer_expected_expenditure_for() {
		return offer_expected_expenditure_for;
	}

	/**
	 * @param offer_expected_expenditure_for
	 *            the offer_expected_expenditure_for to set
	 */
	public void setOffer_expected_expenditure_for(String offer_expected_expenditure_for) {
		this.offer_expected_expenditure_for = offer_expected_expenditure_for;
	}

	/**
	 * @return the offer_expected_expenditure_price
	 */
	public float getOffer_expected_expenditure_price() {
		return offer_expected_expenditure_price;
	}

	/**
	 * @param offer_expected_expenditure_price
	 *            the offer_expected_expenditure_price to set
	 */
	public void setOffer_expected_expenditure_price(float offer_expected_expenditure_price) {
		this.offer_expected_expenditure_price = offer_expected_expenditure_price;
	}

	/**
	 * @return the offer_discount_money_value
	 */
	public float getOffer_discount_money_value() {
		return offer_discount_money_value;
	}

	/**
	 * @param offer_discount_money_value
	 *            the offer_discount_money_value to set
	 */
	public void setOffer_discount_money_value(float offer_discount_money_value) {
		this.offer_discount_money_value = offer_discount_money_value;
	}

	/**
	 * @return the offer_discount_money_percentage
	 */
	public float getOffer_discount_money_percentage() {
		return offer_discount_money_percentage;
	}

	/**
	 * @param offer_discount_money_percentage
	 *            the offer_discount_money_percentage to set
	 */
	public void setOffer_discount_money_percentage(float offer_discount_money_percentage) {
		this.offer_discount_money_percentage = offer_discount_money_percentage;
	}

	/**
	 * @return the offer_min_purchase_value
	 */
	public String getOffer_min_purchase_value() {
		return offer_min_purchase_value;
	}

	/**
	 * @param offer_min_purchase_value
	 *            the offer_min_purchase_value to set
	 */
	public void setOffer_min_purchase_value(String offer_min_purchase_value) {
		this.offer_min_purchase_value = offer_min_purchase_value;
	}

	/**
	 * @return the offer_start_date
	 */
	public String getOffer_start_date() {
		return offer_start_date;
	}

	/**
	 * @param offer_start_date
	 *            the offer_start_date to set
	 */
	public void setOffer_start_date(String offer_start_date) {
		this.offer_start_date = offer_start_date;
	}

	/**
	 * @return the offer_end_date
	 */
	public String getOffer_end_date() {
		return offer_end_date;
	}

	/**
	 * @param offer_end_date
	 *            the offer_end_date to set
	 */
	public void setOffer_end_date(String offer_end_date) {
		this.offer_end_date = offer_end_date;
	}

	/**
	 * @return the offer_start_time
	 */
	public String getOffer_start_time() {
		return offer_start_time;
	}

	/**
	 * @param offer_start_time
	 *            the offer_start_time to set
	 */
	public void setOffer_start_time(String offer_start_time) {
		this.offer_start_time = offer_start_time;
	}

	/**
	 * @return the offer_redem_start_time
	 */
	public String getOffer_redem_start_time() {
		return offer_redem_start_time;
	}

	/**
	 * @param offer_redem_start_time
	 *            the offer_redem_start_time to set
	 */
	public void setOffer_redem_start_time(String offer_redem_start_time) {
		this.offer_redem_start_time = offer_redem_start_time;
	}

	/**
	 * @return the offer_redem_end_date
	 */
	public String getOffer_redem_end_date() {
		return offer_redem_end_date;
	}

	/**
	 * @param offer_redem_end_date
	 *            the offer_redem_end_date to set
	 */
	public void setOffer_redem_end_date(String offer_redem_end_date) {
		this.offer_redem_end_date = offer_redem_end_date;
	}

	/**
	 * @return the offer_redem_end_time
	 */
	public String getOffer_redem_end_time() {
		return offer_redem_end_time;
	}

	/**
	 * @param offer_redem_end_time
	 *            the offer_redem_end_time to set
	 */
	public void setOffer_redem_end_time(String offer_redem_end_time) {
		this.offer_redem_end_time = offer_redem_end_time;
	}

	/**
	 * @return the offer_redem_timestamp
	 */
	public String getOffer_redem_timestamp() {
		return offer_redem_timestamp;
	}

	/**
	 * @param offer_redem_timestamp
	 *            the offer_redem_timestamp to set
	 */
	public void setOffer_redem_timestamp(String offer_redem_timestamp) {
		this.offer_redem_timestamp = offer_redem_timestamp;
	}

	/**
	 * @return the offer_valid_days
	 */
	public String getOffer_valid_days() {
		return offer_valid_days;
	}

	/**
	 * @param offer_valid_days
	 *            the offer_valid_days to set
	 */
	public void setOffer_valid_days(String offer_valid_days) {
		this.offer_valid_days = offer_valid_days;
	}

	/**
	 * @return the voucher_count
	 */
	public int getVoucher_count() {
		return voucher_count;
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * @param voucher_count
	 *            the voucher_count to set
	 */
	public void setVoucher_count(int voucher_count) {
		this.voucher_count = voucher_count;
	}

	/**
	 * @return the offer_name
	 */
	public String getOffer_name() {
		return offer_name;
	}

	/**
	 * @param offer_name
	 *            the offer_name to set
	 */
	public void setOffer_name(String offer_name) {
		this.offer_name = offer_name;
	}

	/**
	 * @return the purchase_key
	 */
	public int getPurchase_key() {
		return purchase_key;
	}

	/**
	 * @param purchase_key
	 *            the purchase_key to set
	 */
	public void setPurchase_key(int purchase_key) {
		this.purchase_key = purchase_key;
	}

	/**
	 * @return the current_counter
	 */
	public int getCurrent_counter() {
		return current_counter;
	}

	/**
	 * @return the voucher_codes
	 */
	public String getVoucher_codes() {
		return voucher_codes;
	}

	/**
	 * @param voucher_codes
	 *            the voucher_codes to set
	 */
	public void setVoucher_codes(String voucher_codes) {
		this.voucher_codes = voucher_codes;
	}

	/**
	 * @param current_counter
	 *            the current_counter to set
	 */
	public void setCurrent_counter(int current_counter) {
		this.current_counter = current_counter;
	}

	/**
	 * @return the offer_locations
	 */
	public ArrayList<OfferLocations> getOffer_locations() {
		return offer_locations;
	}

	/**
	 * @param offer_locations
	 *            the offer_locations to set
	 */
	public void setOffer_locations(ArrayList<OfferLocations> offer_locations) {
		this.offer_locations = offer_locations;
	}

	/**
	 * @return the offer_min_distance
	 */
	public float getOffer_min_distance() {
		return offer_min_distance;
	}

	/**
	 * @param offer_min_distance
	 *            the offer_min_distance to set
	 */
	public void setOffer_min_distance(float offer_min_distance) {
		this.offer_min_distance = offer_min_distance;
	}

	/**
	 * blank constructor
	 */
	public OfferModel() {
		// nothing to do here
	}

	public OfferModel(Parcel in) {
		this.offer_id = in.readInt();
		this.offer_weight = in.readInt();

		this.offer_description = in.readString();
		this.merchant_address = in.readString();

		this.offer_max_cap = in.readInt();
		this.offer_sold_count = in.readInt();

		this.offer_price = in.readFloat();
		this.offer_expected_expenditure_for = in.readString();
		this.offer_expected_expenditure_price = in.readFloat();
		this.offer_discount_money_value = in.readFloat();
		this.offer_discount_money_percentage = in.readFloat();
		this.offer_min_purchase_value = in.readString();

		this.offer_start_date = in.readString();
		this.offer_end_date = in.readString();
		this.offer_start_time = in.readString();
		this.offer_redem_end_date = in.readString();
		this.offer_redem_start_time = in.readString();
		this.offer_redem_end_time = in.readString();
		this.offer_redem_timestamp = in.readString();
		this.offer_valid_days = in.readString();

		this.quantity = in.readInt();
		this.voucher_count = in.readInt();
		this.voucher_codes = in.readString();

		this.offer_name = in.readString();
		this.purchase_key = in.readInt();
		this.current_counter = in.readInt();

		int offerLocationsCount = in.readInt();
		if (offerLocationsCount >= 0) {
			offer_locations = new ArrayList<OfferLocations>(offerLocationsCount);
			for (int i = 0; i < offerLocationsCount; i++) {
				offer_locations.add(new OfferLocations(in));
			}
		}
		this.offer_min_distance = in.readFloat();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(offer_id);
		dest.writeInt(offer_weight);

		dest.writeString(offer_description);
		dest.writeString(merchant_address);

		dest.writeInt(offer_max_cap);
		dest.writeInt(offer_sold_count);

		dest.writeFloat(offer_price);
		dest.writeString(offer_expected_expenditure_for);
		dest.writeFloat(offer_expected_expenditure_price);
		dest.writeFloat(offer_discount_money_value);
		dest.writeFloat(offer_discount_money_percentage);
		dest.writeString(offer_min_purchase_value);

		dest.writeString(offer_start_date);
		dest.writeString(offer_end_date);
		dest.writeString(offer_start_time);
		dest.writeString(offer_redem_end_date);
		dest.writeString(offer_redem_start_time);
		dest.writeString(offer_redem_end_time);
		dest.writeString(offer_redem_timestamp);
		dest.writeString(offer_valid_days);

		dest.writeInt(quantity);
		dest.writeInt(voucher_count);
		dest.writeString(voucher_codes);

		dest.writeString(offer_name);
		dest.writeInt(purchase_key);
		dest.writeInt(current_counter);

		if (offer_locations == null) {
			dest.writeInt(-1);
		} else {
			int offerLocationsCount = offer_locations.size();
			dest.writeInt(offerLocationsCount);
			for (OfferLocations anOfferLocation : offer_locations) {
				anOfferLocation.writeToParcel(dest, flags);
			}
		}
		dest.writeFloat(offer_min_distance);
	}

	/**
	 * to create list of objects to/from parcel
	 */
	public static final Parcelable.Creator<OfferModel>	CREATOR;

	static {
		CREATOR = new Parcelable.Creator<OfferModel>() {
			@Override
			public OfferModel createFromParcel(Parcel in) {
				return new OfferModel(in);
			}

			@Override
			public OfferModel[] newArray(int size) {
				return new OfferModel[size];
			}
		};
	}

	@Override
	public int describeContents() {
		// nothing to do here
		return 0;
	}
}
