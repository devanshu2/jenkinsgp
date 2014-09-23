package com.groupon.go.model;

import java.util.ArrayList;

import com.groupon.go.model.DealDetailResponse.OfferLocations;
import com.kelltontech.model.BaseModel;

/**
 * @author sachin.gupta
 */
public class MyPurchaseModel extends BaseModel {

	private int							order_id;
	private int							deal_id;
	private int							merchant_id;
	private String						opportunity_owner;
	private String						cat_name;
	private String						deal_title;
	private String						deal_highlight;
	private String						deal_fineprint;
	private ArrayList<String>			deal_image;
	private ArrayList<OfferModel>		offers;

	private ArrayList<OfferLocations>	offer_locations;

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
	 * @return the merchant_id
	 */
	public int getMerchant_id() {
		return merchant_id;
	}

	/**
	 * @param merchant_id
	 *            the merchant_id to set
	 */
	public void setMerchant_id(int merchant_id) {
		this.merchant_id = merchant_id;
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
}