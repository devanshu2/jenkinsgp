package com.groupon.go.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.groupon.go.BuildConfig;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.DealDetailResponse.DealDetailModel;
import com.groupon.go.model.DealDetailResponse.OfferLocations;
import com.groupon.go.model.DealModel;
import com.groupon.go.model.MyOfferLocation;
import com.groupon.go.model.MyPurchaseModel;
import com.groupon.go.model.MyPurchasesResponse.MyPurchasesResult;
import com.groupon.go.model.OfferModel;
import com.groupon.go.model.RedeemVoucherResponse.RedeemVoucherResult;
import com.groupon.go.model.VouchersModel;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.model.BaseModel;
import com.kelltontech.utils.DateTimeUtils;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.StringUtils;

/**
 * @author sachin.gupta
 */
public class MyPurchasesHelper {

	private static final String			LOG_TAG	= "MyPurchasesHelper";

	private MyPurchasesTable			mMyPurchasesTable;
	private MyOffersTable				mMyOffersTable;
	private OfferLocationsTable			mOfferLocationsTable;

	private ArrayList<BaseModel>		mMyPurchasesList;
	private ArrayList<BaseModel>		mMyOffersList;
	private ArrayList<BaseModel>		mOfferLocationsList;

	private ArrayList<MyOfferLocation>	mMyOfferLocationsList;
	private ArrayList<DealModel>		mMyPurchasedDealsList;
	private int							mTotalVouchers;

	private Comparator<OfferModel>		mOfferWeightComparator;
	private Comparator<OfferModel>		mOfferExpiryComparator;
	private Comparator<OfferModel>		mOffersDistanceComparator;

	private Comparator<DealModel>		mDealsOrderIdComparator;
	private Comparator<DealModel>		mDealsExpiryComparator;
	private Comparator<DealModel>		mDealsDistanceComparator;

	/**
	 * @param pApplication
	 */
	public MyPurchasesHelper(Application pApplication) {
		mMyPurchasesTable = new MyPurchasesTable(pApplication);
		mMyOffersTable = new MyOffersTable(pApplication);
		mOfferLocationsTable = new OfferLocationsTable(pApplication);
	}

	/**
	 * @param pMyPurchasesResult
	 * @return
	 */
	public synchronized void saveMyPurchases(MyPurchasesResult pMyPurchasesResult) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "saveMyPurchases() started.");
		}
		mMyPurchasesTable.deleteAll();
		mMyOffersTable.deleteAll();
		mOfferLocationsTable.deleteAll();
		mMyPurchasesList = null;
		mMyOffersList = null;
		mOfferLocationsList = null;
		mMyOfferLocationsList = null;
		mMyPurchasedDealsList = null;
		mTotalVouchers = 0;
		if (pMyPurchasesResult.getDeal() == null || pMyPurchasesResult.getDeal().isEmpty()) {
			return;
		}
		for (MyPurchaseModel myPurchaseModel : pMyPurchasesResult.getDeal()) {
			int purchaseKey = mMyPurchasesTable.getNextPrimaryKey();
			myPurchaseModel.setPrimaryKey(purchaseKey);
			mMyPurchasesTable.insertData(myPurchaseModel);

			if (myPurchaseModel.getOffers() == null || myPurchaseModel.getOffers().isEmpty()) {
				continue;
			}
			for (OfferModel offerModel : myPurchaseModel.getOffers()) {
				offerModel.setPurchase_key(purchaseKey);
				mMyOffersTable.insertData(offerModel);
			}
		}
		if (pMyPurchasesResult.getOffer_locations() != null) {
			for (OfferLocations offerLocations : pMyPurchasesResult.getOffer_locations()) {
				mOfferLocationsTable.insertData(offerLocations);
			}
		}
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "saveMyPurchases() returned.");
		}
	}

	/**
	 * @return
	 */
	public int getTotalVouchers(Context pContext) {
		getMyPurchasedDeals(pContext, ApiConstants.VALUE_COUPON_TYPE_ALL);
		return mTotalVouchers;
	}

	/**
	 * @param pContext
	 * @param pSortType
	 * 
	 * @return
	 */
	public ArrayList<DealModel> getMyPurchasedDeals(Context pContext, int pSortType) {
		createMyPurchasedDealsFromDbData();
		Location deviceLocation = GrouponGoPrefs.getDeviceLocation(pContext);
		String redeemedVoucherCodesStr = GrouponGoPrefs.getRedeemedVoucherCodes(pContext);
		mTotalVouchers = 0;
		for (int indexDeal = 0; indexDeal < mMyPurchasedDealsList.size(); indexDeal++) {
			DealModel purchasedDeal = mMyPurchasedDealsList.get(indexDeal);
			MyPurchaseModel myPurchaseModel = getMyPurchaseModel(purchasedDeal);
			if (myPurchaseModel == null) {
				mMyPurchasedDealsList.remove(indexDeal);
				indexDeal--;
				continue;
			}

			ArrayList<OfferModel> purchasedOffersList = getPurchasedOffers(myPurchaseModel, mMyOffersList);

			ArrayList<OfferLocations> allOfferLocations = new ArrayList<OfferLocations>();
			int vouchersInDeal = 0;
			for (int indexOffer = 0; indexOffer < purchasedOffersList.size(); indexOffer++) {
				OfferModel myOfferModel = purchasedOffersList.get(indexOffer);

				if (ProjectUtils.isVoucherExpired(myOfferModel)) {
					purchasedOffersList.remove(indexOffer);
					indexOffer--;
					continue;
				}

				String voucherCodesStr = myOfferModel.getVoucher_codes();
				String[] voucherCodesArr = {};
				if (voucherCodesStr != null) {
					voucherCodesArr = voucherCodesStr.split(",");
				}
				if (!StringUtils.isNullOrEmpty(redeemedVoucherCodesStr)) {
					for (int i = 0; i < voucherCodesArr.length; i++) {
						if (redeemedVoucherCodesStr.contains(voucherCodesArr[i])) {
							voucherCodesStr = voucherCodesStr.replace(voucherCodesArr[i], "");
						}
					}
					voucherCodesArr = voucherCodesStr.split(",");
				}

				int vouchersInOffer = 0;
				voucherCodesStr = "";
				if (voucherCodesArr != null) {
					for (String voucherCode : voucherCodesArr) {
						if (!StringUtils.isNullOrEmpty(voucherCode)) {
							vouchersInOffer++;
							voucherCodesStr += voucherCode + ",";
						}
					}
				}
				if (vouchersInOffer == 0) {
					purchasedOffersList.remove(indexOffer);
					indexOffer--;
					continue;
				}

				myOfferModel.setVoucher_count(vouchersInOffer);
				myOfferModel.setVoucher_codes(voucherCodesStr);
				vouchersInDeal += vouchersInOffer;

				ArrayList<OfferLocations> offerLocations = getOfferLocations(mOfferLocationsList, myOfferModel.getOfferId());
				myOfferModel.setOffer_locations(offerLocations);
				allOfferLocations.addAll(offerLocations);
			}

			if (purchasedOffersList.isEmpty()) {
				mMyPurchasedDealsList.remove(indexDeal);
				indexDeal--;
				continue;
			}

			myPurchaseModel.setOffers(purchasedOffersList);
			myPurchaseModel.setOffer_locations(allOfferLocations);

			mTotalVouchers += vouchersInDeal;

			purchasedDeal.setVoucher_count(vouchersInDeal);

			if (deviceLocation != null) {
				initializeOfferMinDistances(purchasedOffersList, deviceLocation);
			}
			switch (pSortType) {
			case ApiConstants.VALUE_COUPON_TYPE_EXPIRING: {
				Collections.sort(purchasedOffersList, mOfferExpiryComparator);
				break;
			}
			case ApiConstants.VALUE_COUPON_TYPE_NEARBY: {
				Collections.sort(purchasedOffersList, mOffersDistanceComparator);
				break;
			}
			}
			OfferModel offerShownOnDealImage = (OfferModel) purchasedOffersList.get(0);
			purchasedDeal.setOffer_id(offerShownOnDealImage.getOfferId());
			purchasedDeal.setOffer_price("" + offerShownOnDealImage.getOffer_price());
			purchasedDeal.setOffer_discount_money_percentage((int) offerShownOnDealImage.getOffer_discount_money_percentage());
			purchasedDeal.setOffer_discount_money_value((int) offerShownOnDealImage.getOffer_discount_money_percentage());
			purchasedDeal.setDeal_distance(offerShownOnDealImage.getOffer_min_distance());
			purchasedDeal.setExpiry_date(offerShownOnDealImage.getOffer_redem_end_date());

			Collections.sort(purchasedOffersList, mOfferWeightComparator);
		}
		switch (pSortType) {
		case ApiConstants.VALUE_COUPON_TYPE_ALL: {
			Collections.sort(mMyPurchasedDealsList, mDealsOrderIdComparator);
			break;
		}
		case ApiConstants.VALUE_COUPON_TYPE_EXPIRING: {
			Collections.sort(mMyPurchasedDealsList, mDealsExpiryComparator);
			break;
		}
		case ApiConstants.VALUE_COUPON_TYPE_NEARBY: {
			Collections.sort(mMyPurchasedDealsList, mDealsDistanceComparator);
			break;
		}
		}

		return mMyPurchasedDealsList;
	}

	/**
	 * @param pIndexInPurchaseList
	 * @return
	 */
	public DealDetailModel getDealDetailModel(DealModel pPurchasedDealModel) {
		createMyPurchasedDealsFromDbData();
		MyPurchaseModel myPurchaseModel = getMyPurchaseModel(pPurchasedDealModel);
		if (myPurchaseModel == null || myPurchaseModel.getOffers() == null || myPurchaseModel.getOffer_locations() == null) {
			return null;
		}
		DealDetailModel dealDetailModel = new DealDetailModel();
		dealDetailModel.setDeal_id(myPurchaseModel.getDeal_id());
		dealDetailModel.setCat_name(myPurchaseModel.getCat_name());
		dealDetailModel.setDeal_highlight(myPurchaseModel.getDeal_highlight());
		dealDetailModel.setDeal_fineprint(myPurchaseModel.getDeal_fineprint());
		dealDetailModel.setOffers(myPurchaseModel.getOffers());
		dealDetailModel.setOffer_locations(myPurchaseModel.getOffer_locations());
		return dealDetailModel;
	}

	/**
	 * @param pContext
	 * @param data
	 * @return
	 */
	public RedeemVoucherResult redeemVouchers(Context pContext, Bundle data) {
		mMyPurchasedDealsList = null;
		ArrayList<OfferModel> offerModelList = data.getParcelableArrayList(Constants.EXTRA_OFFER_MODEL_LIST);
		ArrayList<VouchersModel> vouchersList = new ArrayList<VouchersModel>();
		String redeemedVoucherCodesStr = "";
		for (OfferModel offerModel : offerModelList) {
			if (offerModel.getCurrent_counter() <= 0) {
				continue;
			}
			String voucherCodesStr = offerModel.getVoucher_codes();
			String[] voucherCodesArr = voucherCodesStr.split(",");
			ArrayList<String> redeemedVouchersList = new ArrayList<String>(offerModel.getCurrent_counter());
			for (int i = 0; i < offerModel.getCurrent_counter(); i++) {
				redeemedVouchersList.add(voucherCodesArr[i]);
				redeemedVoucherCodesStr += voucherCodesArr[i] + ",";
			}
			VouchersModel vouchersModel = new VouchersModel();
			vouchersModel.setOffer_id(offerModel.getOfferId());
			vouchersModel.setVoucher_codes(redeemedVouchersList);
			vouchersList.add(vouchersModel);
		}
		GrouponGoPrefs.appendRedeemedVoucherCodes(pContext, redeemedVoucherCodesStr);
		RedeemVoucherResult redeemVoucherResult = new RedeemVoucherResult();
		redeemVoucherResult.setVouchers(vouchersList);
		return redeemVoucherResult;
	}

	/**
	 * get all data of my purchases from three tables
	 */
	public ArrayList<MyOfferLocation> getNotifiableOfferLocations(Context pContext) {
		if (mMyOfferLocationsList != null) {
			return mMyOfferLocationsList;
		}
		ArrayList<DealModel> purchasedDealsList = getMyPurchasedDeals(pContext, ApiConstants.VALUE_COUPON_TYPE_ALL);

		mMyOfferLocationsList = new ArrayList<MyOfferLocation>();

		for (DealModel purchasedDeal : purchasedDealsList) {
			MyPurchaseModel myPurchaseModel = getMyPurchaseModel(purchasedDeal);
			for (OfferModel purchasedOffer : myPurchaseModel.getOffers()) {

				for (OfferLocations purchasedOfferLocation : purchasedOffer.getOffer_locations()) {

					MyOfferLocation myOfferLocation = new MyOfferLocation();
					myOfferLocation.setOrder_id(myPurchaseModel.getOrder_id());
					myOfferLocation.setDeal_id(myPurchaseModel.getDeal_id());
					myOfferLocation.setMerchant_id(myPurchaseModel.getMerchant_id());
					myOfferLocation.setMerchant_name(myPurchaseModel.getOpportunity_owner());

					myOfferLocation.setOffer_id(purchasedOffer.getOfferId());
					myOfferLocation.setVouchers(purchasedOffer.getVoucher_count());

					myOfferLocation.setAddress_id(purchasedOfferLocation.getAddress_id());

					double[] latLong = ProjectUtils.getLatLongFromString(purchasedOfferLocation.getOffer_location());
					if (latLong == null) {
						continue;
					}
					myOfferLocation.setLattitude(latLong[0]);
					myOfferLocation.setLongitude(latLong[1]);

					mMyOfferLocationsList.add(myOfferLocation);
				}
			}
		}
		return mMyOfferLocationsList;
	}

	/**
	 * get all data of my purchases from three tables
	 */
	private synchronized void createMyPurchasedDealsFromDbData() {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "createMyPurchasedDealsFromDbData() started.");
		}
		if (mMyPurchasedDealsList != null) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "createMyPurchasedDealsFromDbData() already created.");
			}
			return;
		}

		if (mMyPurchasesList == null || mMyOffersList == null || mOfferLocationsList == null) {
			mMyPurchasesList = mMyPurchasesTable.getAllData();
			mMyOffersList = mMyOffersTable.getAllData();
			mOfferLocationsList = mOfferLocationsTable.getAllData();
		}

		mMyPurchasedDealsList = new ArrayList<DealModel>();

		for (BaseModel baseModel : mMyPurchasesList) {
			MyPurchaseModel myPurchaseModel = (MyPurchaseModel) baseModel;

			DealModel purchasedDealModel = new DealModel();
			purchasedDealModel.setDeal_id(myPurchaseModel.getDeal_id());
			purchasedDealModel.setOrder_id(myPurchaseModel.getOrder_id());
			purchasedDealModel.setDeal_title(myPurchaseModel.getDeal_title());
			purchasedDealModel.setOpportunity_owner(myPurchaseModel.getOpportunity_owner());

			purchasedDealModel.setDeal_image(myPurchaseModel.getDeal_image());
			ProjectUtils.updateDealModel(purchasedDealModel);

			mMyPurchasedDealsList.add(purchasedDealModel);
		}

		mOfferWeightComparator = new Comparator<OfferModel>() {
			@Override
			public int compare(OfferModel lhs, OfferModel rhs) {
				if (lhs.getOffer_weight() > rhs.getOffer_weight()) {
					return 1;
				}
				if (lhs.getOffer_weight() < rhs.getOffer_weight()) {
					return -1;
				}
				return 0;
			}
		};

		mOfferExpiryComparator = new Comparator<OfferModel>() {
			@Override
			public int compare(OfferModel lhs, OfferModel rhs) {
				long lhsExpiryDateLong = DateTimeUtils.parseExtendedDate(lhs.getOffer_redem_end_date());
				long rhsExpiryDateLong = DateTimeUtils.parseExtendedDate(rhs.getOffer_redem_end_date());
				if (lhsExpiryDateLong > rhsExpiryDateLong) {
					return 1;
				}
				if (lhsExpiryDateLong < rhsExpiryDateLong) {
					return -1;
				}
				return 0;
			}
		};

		mOffersDistanceComparator = new Comparator<OfferModel>() {
			@Override
			public int compare(OfferModel lhs, OfferModel rhs) {
				if (lhs.getOffer_min_distance() > rhs.getOffer_min_distance()) {
					return 1;
				}
				if (lhs.getOffer_min_distance() < rhs.getOffer_min_distance()) {
					return -1;
				}
				return 0;
			}
		};

		mDealsOrderIdComparator = new Comparator<DealModel>() {
			@Override
			public int compare(DealModel lhs, DealModel rhs) {
				if (lhs.getOrder_id() < rhs.getOrder_id()) {
					return 1;
				}
				if (lhs.getOrder_id() > rhs.getOrder_id()) {
					return -1;
				}
				return 0;
			}
		};

		mDealsExpiryComparator = new Comparator<DealModel>() {
			@Override
			public int compare(DealModel lhs, DealModel rhs) {
				long lhsExpiryDateLong = DateTimeUtils.parseExtendedDate(lhs.getExpiry_date());
				long rhsExpiryDateLong = DateTimeUtils.parseExtendedDate(rhs.getExpiry_date());
				if (lhsExpiryDateLong > rhsExpiryDateLong) {
					return 1;
				}
				if (lhsExpiryDateLong < rhsExpiryDateLong) {
					return -1;
				}
				return 0;
			}
		};

		mDealsDistanceComparator = new Comparator<DealModel>() {
			@Override
			public int compare(DealModel lhs, DealModel rhs) {
				if (lhs.getDeal_distance() > rhs.getDeal_distance()) {
					return 1;
				}
				if (lhs.getDeal_distance() < rhs.getDeal_distance()) {
					return -1;
				}
				return 0;
			}
		};
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "createMyPurchasedDealsFromDbData() returned.");
		}
	}

	/**
	 * @param pPurchasedDealModel
	 * @return
	 */
	private MyPurchaseModel getMyPurchaseModel(DealModel pPurchasedDealModel) {
		if (mMyPurchasesList == null || mMyPurchasesList.isEmpty()) {
			return null;
		}
		for (BaseModel baseModel : mMyPurchasesList) {
			MyPurchaseModel myPurchaseModel = (MyPurchaseModel) baseModel;
			if (pPurchasedDealModel.getDeal_id() == myPurchaseModel.getDeal_id() && pPurchasedDealModel.getOrder_id() == myPurchaseModel.getOrder_id()) {
				return myPurchaseModel;
			}
		}
		return null;
	}

	/**
	 * @param pMyPurchaseModel
	 * @return
	 */
	private ArrayList<OfferModel> getPurchasedOffers(MyPurchaseModel pMyPurchaseModel, ArrayList<BaseModel> pAllOffersList) {
		ArrayList<OfferModel> offersInThisPurchase = new ArrayList<OfferModel>();
		for (BaseModel baseModel : pAllOffersList) {
			OfferModel myOfferModel = (OfferModel) baseModel;
			if (pMyPurchaseModel.getPrimaryKey() == myOfferModel.getPurchase_key()) {
				offersInThisPurchase.add(myOfferModel);
			}
		}
		return offersInThisPurchase;
	}

	/**
	 * @param pAllLocationsList
	 * @param pOfferId
	 * @return
	 */
	private ArrayList<OfferLocations> getOfferLocations(ArrayList<BaseModel> pAllLocationsList, int pOfferId) {
		ArrayList<OfferLocations> offerLocationsList = new ArrayList<OfferLocations>();
		for (BaseModel baseModel : pAllLocationsList) {
			OfferLocations offerLocations = (OfferLocations) baseModel;
			if (offerLocations.getOffer_id() == pOfferId) {
				offerLocationsList.add(offerLocations);
			}
		}
		return offerLocationsList;
	}

	/**
	 * @param pDealOffersList
	 * @param pDeviceLocation
	 */
	private void initializeOfferMinDistances(ArrayList<OfferModel> pDealOffersList, Location pDeviceLocation) {
		ArrayList<OfferLocations> offerLocationsList;
		for (OfferModel offerModel : pDealOffersList) {
			offerLocationsList = offerModel.getOffer_locations();
			if (offerLocationsList == null || offerLocationsList.isEmpty()) {
				continue;
			}
			float offerMinDistance = 0;
			String offerNearestAddress = "";
			for (OfferLocations offerLocations : offerLocationsList) {
				double[] latLong = ProjectUtils.getLatLongFromString(offerLocations.getOffer_location());
				if (latLong == null) {
					continue;
				}
				float distance = LocationUtils.getDistance(latLong[0], latLong[1], pDeviceLocation.getLatitude(), pDeviceLocation.getLongitude());
				offerNearestAddress = offerLocations.getMerchant_address();
				if (offerMinDistance < distance) {
					offerMinDistance = distance;
					offerNearestAddress = offerLocations.getMerchant_address();
				}
			}
			offerModel.setOffer_min_distance(offerMinDistance / 1000);
			offerModel.setMerchant_address(offerNearestAddress);
		}
	}
}