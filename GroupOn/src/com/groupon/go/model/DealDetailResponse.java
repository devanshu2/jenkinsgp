package com.groupon.go.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.groupon.go.constants.ApiConstants;
import com.kelltontech.model.BaseModel;

/**
 * @author vineet.rajpoot
 */
public class DealDetailResponse extends CommonJsonResponse {

	private DealDetailResultModel	result;

	/**
	 * @return the result
	 */
	public DealDetailResultModel getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(DealDetailResultModel result) {
		this.result = result;
	}

	public class DealDetailResultModel {
		private DealDetailModel	deal;

		/**
		 * @return the deal
		 */
		public DealDetailModel getDeal() {
			return deal;
		}

		/**
		 * @param deal
		 *            the deal to set
		 */
		public void setDeal(DealDetailModel deal) {
			this.deal = deal;
		}
	}

	/**
	 * @author vineet.rajpoot
	 */
	public static class DealDetailModel {

		private int							deal_id;
		private String						cat_name;
		private String						deal_highlight;
		private String						deal_fineprint;
		private ArrayList<OfferModel>		offers;
		private ArrayList<OfferLocations>	offer_locations;

		/**
		 * used only when {@link ApiConstants#GET_DEALS_DETAIL} is called from
		 * user cart
		 */
		private String						deal_title;

		/**
		 * used only when {@link ApiConstants#GET_SHARING_DEALS_DETAIL} is
		 * called
		 */
		private DealModel					shared_deal;

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
		 * @return the deal_highlight
		 */
		public String getDeal_highlight() {
			return deal_highlight;
		}

		/**
		 * @param deal_highlight
		 *            the deal_highlight to set
		 */
		public void setDeal_highlight(String deal_highlight) {
			this.deal_highlight = deal_highlight;
		}

		/**
		 * @return the deal_fineprint
		 */
		public String getDeal_fineprint() {
			return deal_fineprint;
		}

		/**
		 * @param deal_fineprint
		 *            the deal_fineprint to set
		 */
		public void setDeal_fineprint(String deal_fineprint) {
			this.deal_fineprint = deal_fineprint;
		}

		/**
		 * @return the offers
		 */
		public ArrayList<OfferModel> getOffers() {
			return offers;
		}

		/**
		 * @param offers
		 *            the offers to set
		 */
		public void setOffers(ArrayList<OfferModel> offers) {
			this.offers = offers;
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
		 * @return the shared_deal
		 */
		public DealModel getShared_deal() {
			return shared_deal;
		}

		/**
		 * @param shared_deal
		 *            the shared_deal to set
		 */
		public void setShared_deal(DealModel shared_deal) {
			this.shared_deal = shared_deal;
		}
	}

	public static class OfferLocations extends BaseModel implements Parcelable {

		private int		offer_id;
		private int		address_id;
		private String	offer_location;
		private String	merchant_address;

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
		 * @return the address_id
		 */
		public int getAddress_id() {
			return address_id;
		}

		/**
		 * @param address_id
		 *            the address_id to set
		 */
		public void setAddress_id(int address_id) {
			this.address_id = address_id;
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
		 * @return the offer_location
		 */
		public String getOffer_location() {
			return offer_location;
		}

		/**
		 * @param offer_location
		 *            the offer_location to set
		 */
		public void setOffer_location(String offer_location) {
			this.offer_location = offer_location;
		}

		public OfferLocations() {
			/**
			 * blank constructor
			 */
		}

		public OfferLocations(Parcel in) {
			this.offer_id = in.readInt();
			this.offer_location = in.readString();
			this.merchant_address = in.readString();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(offer_id);
			dest.writeString(offer_location);
			dest.writeString(merchant_address);

		}

		public static final Parcelable.Creator<OfferLocations>	CREATOR;

		static {
			CREATOR = new Parcelable.Creator<OfferLocations>() {
				@Override
				public OfferLocations createFromParcel(Parcel in) {
					return new OfferLocations(in);
				}

				@Override
				public OfferLocations[] newArray(int size) {
					return new OfferLocations[size];
				}
			};
		}

		@Override
		public int describeContents() {
			// nothing to do here
			return 0;
		}

	}
}
