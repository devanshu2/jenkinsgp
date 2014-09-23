package com.groupon.go.utils;

import java.util.ArrayList;

import com.groupon.go.database.SearchHistoryTable;
import com.groupon.go.model.CountryModel;
import com.groupon.go.model.DealModel;
import com.groupon.go.model.OfferModel;
import com.groupon.go.model.SearchItemModel;

/**
 * This class is only to hold project related methods.
 */
public class DummyData {

	/**
	 * @param pContext
	 */
	public static ArrayList<CountryModel> getCountries() {
		ArrayList<CountryModel> mListCountryModel = new ArrayList<CountryModel>();
		String[] countryName = { "India", "Australia", "South Africa", "Usa", "Endland" };
		String[] countrCode = { "+91", "+92", "+93", "+94", "+95" };
		String[] countryFlag = { " ", " ", "  ", " ", " " };
		CountryModel countryModel;

		for (int i = 0; i < countrCode.length; i++) {
			countryModel = new CountryModel();
			countryModel.setCountry_name(countryName[i]);
			countryModel.setCountry_code(countrCode[i]);
			countryModel.setFlag_img_url(countryFlag[i]);
			mListCountryModel.add(countryModel);
		}
		return mListCountryModel;
	}

	/**
	 * @param pContext
	 */
	public static ArrayList<DealModel> getDealsList() {
		ArrayList<DealModel> dealsList = new ArrayList<DealModel>();
		for (int i = 1; i < 10; i++) {
			dealsList.add(getDummyDeal(i));
		}
		return dealsList;
	}

	/**
	 * @param pContext
	 */
	public static void insertDummySearches(SearchHistoryTable searchHistoryTable) {
		SearchItemModel model1 = new SearchItemModel();
		model1.setPrimaryKey(1);
		model1.setSearchText("Android Phones");
		searchHistoryTable.insertData(model1);

		SearchItemModel model2 = new SearchItemModel();
		model2.setPrimaryKey(2);
		model2.setSearchText("BlackBerry Phones");
		searchHistoryTable.insertData(model2);

		SearchItemModel model3 = new SearchItemModel();
		model3.setPrimaryKey(3);
		model3.setSearchText("Shoes");
		searchHistoryTable.insertData(model3);

		SearchItemModel model4 = new SearchItemModel();
		model4.setPrimaryKey(4);
		model4.setSearchText("Travel Package");
		searchHistoryTable.insertData(model4);

		SearchItemModel model5 = new SearchItemModel();
		model5.setPrimaryKey(5);
		model5.setSearchText("Fashion & Style");
		searchHistoryTable.insertData(model5);
	}

	/**
	 * @param pContext
	 */
	public static DealModel getDummyDeal(int dealId) {
		DealModel model = new DealModel();
		model.setDeal_id(dealId);
		model.setOpportunity_owner("Test Merchant Name " + dealId);
		model.setDeal_title("Test Deal Title " + dealId);
		model.setDeal_sold_count(9999);
		model.setDeal_image(getDummyImage());
		return model;
	}

	/**
	 * @param pContext
	 */
	public static ArrayList<String> getDummyImage() {
		ArrayList<String> images = new ArrayList<String>();
		images.add("https://lh5.googleusercontent.com/-kI_QdYx7VlU/URqvLXCB6gI/AAAAAAAAAbs/N31vlZ6u89o/s1024/Yet%252520Another%252520Rockaway%252520Sunset.jpg");
		images.add("https://lh4.googleusercontent.com/-e9NHZ5k5MSs/URqvMIBZjtI/AAAAAAAAAbs/1fV810rDNfQ/s1024/Yosemite%252520Tree.jpg");
		images.add("http://4.bp.blogspot.com/-LEvwF87bbyU/Uicaskm-g6I/AAAAAAAAZ2c/V-WZZAvFg5I/s800/Pesto+Guacamole+500w+0268.jpg");
		return images;
	}

	/**
	 * @param pContext
	 */
	public static ArrayList<OfferModel> getDummyOffers() {
		ArrayList<OfferModel> offers = new ArrayList<OfferModel>();
		OfferModel modal = new OfferModel();
		modal.setOfferId(1);
		modal.setOfferDes("Offer 1");
		modal.setOffer_price(49);
		offers.add(modal);

		modal = new OfferModel();
		modal.setOfferId(2);
		modal.setOfferDes("Offer 2");
		modal.setOffer_price(49);
		offers.add(modal);

		modal = new OfferModel();
		modal.setOfferId(3);
		modal.setOfferDes("Offer 3");
		modal.setOffer_price(49);
		offers.add(modal);

		return offers;
	}
}
