package com.groupon.go.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.groupon.go.BuildConfig;
import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.model.CategoryModel;
import com.groupon.go.model.CityModel;
import com.groupon.go.model.DealModel;
import com.groupon.go.model.DealsListRequest;
import com.groupon.go.model.GetProfileResponse.CategorySelected;
import com.groupon.go.model.GetProfileResponse.GetProfileResultModel;
import com.groupon.go.model.MyOfferLocation;
import com.groupon.go.model.OfferModel;
import com.groupon.go.model.SavedCardsModel;
import com.groupon.go.model.SearchItemModel;
import com.groupon.go.model.UserCartModel;
import com.groupon.go.model.UserCartModel.CartOfferModel;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.ui.activity.ProductDetailActivity;
import com.kelltontech.service.LocationService;
import com.kelltontech.utils.DateTimeUtils;
import com.kelltontech.utils.DateTimeUtils.Format;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.StringUtils;
import com.kelltontech.utils.UiUtils;

/**
 * @author sachin.gupta
 * 
 *         This class is only to hold project related methods.
 */
public class ProjectUtils {

	private final static String	LOG_TAG			= ProjectUtils.class.getSimpleName();
	private final static String	REGEX_AMEX_CARD	= "^3[47][0-9]{13}$";

	/**
	 * @author vineet.rajpoot
	 */
	public interface CartDealClickListener {
		public void onCartDealClick(int pClickedCartDealIndex);
	}

	/**
	 * @param pContext
	 */
	public static void saveDeviceDensityGroupName(Context pContext) {
		String densityGroupNameStr = GrouponGoPrefs.getDeviceDensityGroup(pContext);
		if (!StringUtils.isNullOrEmpty(densityGroupNameStr)) {
			return;
		}
		String densityGroupName = UiUtils.getDeviceDensityGroupName(pContext);
		if (densityGroupName.equals("ldpi")) {
			densityGroupName = "mdpi";
		}
		GrouponGoPrefs.setDeviceDensityGroup(pContext, densityGroupName);
	}

	/**
	 * @param profileResult
	 */
	public static void syncCategoriesSelectionState(GetProfileResultModel profileResult) {
		if (profileResult.getCategory_selected() == null || profileResult.getCategory_selected().isEmpty()) {
			for (CategoryModel categoryModel : profileResult.getCategory()) {
				categoryModel.setSelectedTemp(true);
			}
			return;
		}
		ArrayList<CategorySelected> selectedCategoriesList = new ArrayList<CategorySelected>(profileResult.getCategory_selected());
		for (CategoryModel categoryModel : profileResult.getCategory()) {
			for (int i = 0; i < selectedCategoriesList.size(); i++) {
				CategorySelected catSelected = selectedCategoriesList.get(i);
				if (catSelected.getCategory_id() == categoryModel.getTid()) {
					categoryModel.setCat_selected(true);
					categoryModel.setSelectedTemp(true);
					selectedCategoriesList.remove(i);
					break;
				}
			}
		}
	}

	/**
	 * Only 1st page of all 3 types of deals will be saved and shown to/from
	 * cache. Rest all response will be fetched directly from server.
	 * 
	 * @param pRequestData
	 * 
	 */
	public static int getDealsListRespTypeToBeCached(Object pRequestData) {
		DealsListRequest dealsListRequest = null;
		if (pRequestData instanceof DealsListRequest) {
			dealsListRequest = (DealsListRequest) pRequestData;
		}
		if (dealsListRequest == null) {
			return 0;
		}
		if (dealsListRequest.getPageNo() > 1) {
			return 0;
		}
		if (dealsListRequest.getCategoryId() != 0) {
			return 0;
		}
		if (!StringUtils.isNullOrEmpty(dealsListRequest.getSearchText())) {
			return 0;
		}
		return dealsListRequest.getDealType();
	}

	/**
	 * @param deal
	 */
	public static void updateDealsList(List<DealModel> dealsList) {
		if (dealsList == null || dealsList.isEmpty()) {
			return;
		}
		for (DealModel deal : dealsList) {
			updateDealModel(deal);
		}
	}

	/**
	 * @param deal
	 */
	public static void updateDealModel(DealModel dealModel) {
		if (dealModel.getDeal_image() == null || dealModel.getDeal_image().isEmpty()) {
			return;
		}
		String csvURLsStr = dealModel.getDeal_image().remove(0);
		if (StringUtils.isNullOrEmpty(csvURLsStr)) {
			return;
		}
		dealModel.getDeal_image().clear();
		String[] imageURLsArr = csvURLsStr.split(",");
		for (String imageUrl : imageURLsArr) {
			dealModel.getDeal_image().add(imageUrl);
		}
	}

	/**
	 * @param suggestion
	 * @param formatted
	 * @return
	 */
	public static String getCompleteSearchText(SearchItemModel suggestion, boolean formatted) {
		StringBuilder searchTextWithCat = new StringBuilder(suggestion.getSearchText());
		if (!StringUtils.isNullOrEmpty(suggestion.getCat_name())) {
			searchTextWithCat.append(" in ");
			if (formatted) {
				searchTextWithCat.append("<b>");
			}
			searchTextWithCat.append(suggestion.getCat_name());
			if (formatted) {
				searchTextWithCat.append("</b>");
			}
		}
		return searchTextWithCat.toString();
	}

	/**
	 * @param pContext
	 * @param pDealId
	 *            or 0 if app is to be shared
	 * 
	 * @return linkToShare
	 */
	public static String createLinkToShare(Context pContext, int pDealId) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(pContext.getString(R.string.share_deal_link_scheme));
		stringBuilder.append("://");
		stringBuilder.append(pContext.getString(R.string.share_deal_link_share_host));
		stringBuilder.append("/");
		Builder builder = Uri.parse(stringBuilder.toString()).buildUpon();
		if (pDealId != 0) {
			builder.appendQueryParameter(ApiConstants.PARAM_ONLY_ID, "" + pDealId);
		}
		return builder.toString();
	}

	/**
	 * @param pContext
	 * @param pMessageType
	 * @return
	 */
	public static String getSubjectToShare(Context pContext, int pMessageType) {
		String appName = pContext.getString(R.string.app_name);
		switch (pMessageType) {
		case ApiConstants.VALUE_MESSAGE_TYPE_SHARE_APP: {
			return pContext.getString(R.string.subject_share_app, appName);
		}
		case ApiConstants.VALUE_MESSAGE_TYPE_UNIQUE_CODE: {
			return pContext.getString(R.string.subject_share_promo_code, appName);
		}
		default: {
			return pContext.getString(R.string.subject_share_deal, appName);
		}
		}
	}

	/**
	 * @param position
	 * @return
	 */
	public static double[] getLatLongFromString(String latLongStr) {
		try {
			String[] latlng = latLongStr.split(",");
			double[] latlngdoub = { Double.parseDouble(latlng[0]),
					Double.parseDouble(latlng[1]) };
			return latlngdoub;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * method use to inflate deals and offerView
	 * 
	 * @param cartitemList
	 * @param activity
	 * @param cartRootLayout
	 * @param isAppliedCoupon
	 * @return grand Total of cart and payment summary
	 */

	public static int populateView(final ArrayList<UserCartModel> cartitemList, Activity activity, LinearLayout cartRootLayout, boolean isCartView, final CartDealClickListener cartItemListener, boolean isAppliedCoupon) {
		int totalCartPrice = 0;
		if (cartitemList != null && cartitemList.size() > 0) {
			ArrayList<CartOfferModel> offerList;
			for (int i = 0; i < cartitemList.size(); i++) {
				final int counter = i + 1;
				View view = activity.getLayoutInflater().inflate(R.layout.user_cart_item, null, false);
				RelativeLayout mainLayout = (RelativeLayout) view.findViewById(R.id.user_cart_item_main);
				TextView dealTitle = (TextView) view.findViewById(R.id.offer_headline);
				TextView dealAmount = (TextView) view.findViewById(R.id.cart_offer_amount);
				LinearLayout cartItemRoot = (LinearLayout) view.findViewById(R.id.cart_item_root);
				float[] totalPriceDeal = getTotalDealPrice(cartitemList.get(i), isAppliedCoupon);
				TextView txvDisAmount = (TextView) view.findViewById(R.id.offer_amount_after_discount);
				// totalCartPrice += totalPriceDeal;
				dealAmount.setText(activity.getResources().getString(R.string.Rs) + " " + StringUtils.getFormatDecimalAmount(totalPriceDeal[0]) + " ");
				if (isAppliedCoupon && totalPriceDeal[0] != totalPriceDeal[1]) {
					dealAmount.setPaintFlags(dealAmount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					txvDisAmount.setText(activity.getResources().getString(R.string.Rs) + " " + StringUtils.getFormatDecimalAmount(totalPriceDeal[1]));
					txvDisAmount.setVisibility(View.VISIBLE);
				} else {
					dealAmount.setPaintFlags(dealAmount.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
					txvDisAmount.setVisibility(View.GONE);
				}

				dealTitle.setText("" + cartitemList.get(i).getMerchant_name());
				offerList = cartitemList.get(i).getOffer_list();
				inflateOfferItem(cartItemRoot, offerList, activity, isCartView, isAppliedCoupon);
				if (isCartView) {
					dealTitle.setSingleLine(true);
					if (cartItemListener != null) {
						((ImageView) view.findViewById(R.id.img_remv_deal)).setVisibility(View.VISIBLE);
						view.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								cartItemListener.onCartDealClick(counter - 1);
							}
						});
					}
				}
				if (counter % 2 == 0) {
					mainLayout.setBackgroundColor(activity.getResources().getColor(R.color.common_grey_3));
				} else {
					mainLayout.setBackgroundColor(activity.getResources().getColor(R.color.white));
				}
				cartRootLayout.addView(view);
			}
		}
		return totalCartPrice;
	}

	/**
	 * 
	 * @param userCartModel
	 * @return
	 */
	private static float[] getTotalDealPrice(UserCartModel userCartModel, boolean isCouponApplied) {
		ArrayList<CartOfferModel> cartOfferModels = userCartModel.getOffer_list();
		float totalDealPrice = 0;
		float totalDiscountPrice = 0;
		for (int i = 0; i < cartOfferModels.size(); i++) {
			CartOfferModel cartModel = cartOfferModels.get(i);
			if (isCouponApplied) {
				totalDiscountPrice = totalDiscountPrice + (cartModel.getOffer_discount_price() * cartModel.getOffer_count());
			}
			totalDealPrice = totalDealPrice + (cartModel.getOffer_count() * cartModel.getOffer_price());
		}
		float[] prices = { totalDealPrice, totalDiscountPrice };
		return prices;

	}

	/**
	 * 
	 * @param view
	 * @param offerList
	 * @param activity
	 * @param isCartView
	 * @param isAppliedCoupon
	 */
	private static void inflateOfferItem(LinearLayout view, ArrayList<CartOfferModel> cartOfferList, Activity activity, boolean isCartView, boolean isAppliedCoupon) {
		if (cartOfferList == null || cartOfferList.isEmpty()) {
			return;
		}
		String labelOfferStr = activity.getString(R.string.offer);
		String iconRsStr = activity.getString(R.string.Rs);

		for (CartOfferModel cartOfferModel : cartOfferList) {
			View childView = activity.getLayoutInflater().inflate(R.layout.user_cart_item_offer, null);
			TextView offerName = (TextView) childView.findViewById(R.id.cart_offer_name);
			TextView offerDesc = (TextView) childView.findViewById(R.id.cart_offer_title);
			TextView offerCount = (TextView) childView.findViewById(R.id.cart_offer_count);
			TextView offerPrice = (TextView) childView.findViewById(R.id.cart_offer_amount);

			offerName.setText(getOfferName(labelOfferStr, cartOfferModel.getOffer_weight(), cartOfferList.size()));
			offerDesc.setText(StringUtils.decode(cartOfferModel.getOffer_desc(), StringUtils.CHARSET_UTF_8, true));

			if (!isCartView) {
				offerCount.setBackgroundResource(0);
				offerPrice.setBackgroundResource(0);
			}
			offerCount.setText(cartOfferModel.getOffer_count() + "x");
			offerPrice.setText(iconRsStr + " " + StringUtils.getFormatDecimalAmount(cartOfferModel.getOffer_price()) + " ");

			TextView txvDisAmount = (TextView) childView.findViewById(R.id.cart_offer_amount_after_discount);

			if (isAppliedCoupon && cartOfferModel.getOffer_price() != cartOfferModel.getOffer_discount_price()) {
				offerPrice.setPaintFlags(offerPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				txvDisAmount.setText(activity.getResources().getString(R.string.Rs) + " " + StringUtils.getFormatDecimalAmount(cartOfferModel.getOffer_discount_price()));
				txvDisAmount.setVisibility(View.VISIBLE);
			} else {
				offerPrice.setPaintFlags(offerPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
				txvDisAmount.setVisibility(View.GONE);
			}

			view.addView(childView);
		}
	}

	/**
	 * @param cardNum
	 * @return
	 */
	public static String getFormatedCardNum(String cardNum) {
		StringBuilder formatedCardNum = new StringBuilder();
		int count = 0;
		for (int i = 0; i < cardNum.length(); i++) {
			if (count == 4) {
				count = 0;
				formatedCardNum.append(" " + cardNum.charAt(i));
			} else {
				formatedCardNum.append(cardNum.charAt(i));
			}
			count++;
		}
		return formatedCardNum.toString();
	}

	/**
	 * @param pExtendedDateString
	 * @return
	 */
	public static String getFormattedDate(String pExtendedDateString) {
		long epochMillis = DateTimeUtils.parseExtendedDate(pExtendedDateString);
		if (epochMillis == 0) {
			return "";
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(epochMillis);
		return DateTimeUtils.getFormattedDate(calendar, Format.DD_Mmm_YY);
	}

	/**
	 * @param pMap
	 * @param pLatlng
	 * @param pTitle
	 */
	public static void placeMarker(GoogleMap pMap, LatLng pLatlng, String pTitle, int resId) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(pLatlng);
		if (!StringUtils.isNullOrEmpty(pTitle)) {
			markerOptions.title(pTitle);
		}
		markerOptions.icon(BitmapDescriptorFactory.fromResource(resId));
		pMap.addMarker(markerOptions);
	}

	/**
	 * @param allCards
	 * @param pCardMode
	 * @return
	 */
	public static ArrayList<SavedCardsModel> filterCards(ArrayList<SavedCardsModel> allCards, String pCardMode) {
		ArrayList<SavedCardsModel> filteredList = new ArrayList<SavedCardsModel>();
		for (SavedCardsModel savedCard : allCards) {
			if (savedCard.getCard_mode() == null || savedCard.getCard_mode().equalsIgnoreCase(pCardMode)) {
				filteredList.add(savedCard);
			}
		}
		return filteredList;
	}

	/**
	 * @param mActivity
	 * @param tid
	 * @return
	 * @return
	 */
	public static void setDefaultAndErrorIconForCategory(CategoryModel pCategoryModel, NetworkImageView pNetworkImageView) {
		int catIconResId = 0;
		switch (pCategoryModel.getTid()) {
		case ApiConstants.VALUE_TID_DUMMY_FOR_ALL_DEALS: {
			if (pCategoryModel.isSelectedTemp()) {
				catIconResId = R.drawable.cat_all_deals_selected;
			} else {
				catIconResId = R.drawable.cat_all_deals;
			}
			break;
		}
		case ApiConstants.VALUE_TID_FOOD_AND_DRINKS: {
			if (pCategoryModel.isSelectedTemp()) {
				catIconResId = R.drawable.cat_tid219_selected;
			} else {
				catIconResId = R.drawable.cat_tid219_food_and_drinks;
			}
			break;
		}
		case ApiConstants.VALUE_TID_BEAUTY_AND_SPA: {
			if (pCategoryModel.isSelectedTemp()) {
				catIconResId = R.drawable.cat_tid225_selected;
			} else {
				catIconResId = R.drawable.cat_tid225_beauty_and_spa;
			}
			break;
		}
		case ApiConstants.VALUE_TID_HEALTH_AND_FITNESS: {
			if (pCategoryModel.isSelectedTemp()) {
				catIconResId = R.drawable.cat_tid236_selected;
			} else {
				catIconResId = R.drawable.cat_tid236_health_and_fitness;
			}
			break;
		}
		case ApiConstants.VALUE_TID_EVENT_AND_ACTIVITIES: {
			if (pCategoryModel.isSelectedTemp()) {
				catIconResId = R.drawable.cat_tid243_selected;
			} else {
				catIconResId = R.drawable.cat_tid243_events_and_activities;
			}
			break;
		}
		case ApiConstants.VALUE_TID_SERVICES: {
			if (pCategoryModel.isSelectedTemp()) {
				catIconResId = R.drawable.cat_tid261_selected;
			} else {
				catIconResId = R.drawable.cat_tid261_services;
			}
			break;
		}
		case ApiConstants.VALUE_TID_SHOPPING: {
			if (pCategoryModel.isSelectedTemp()) {
				catIconResId = R.drawable.cat_tid276_selected;
			} else {
				catIconResId = R.drawable.cat_tid276_shopping;
			}
			break;
		}
		case ApiConstants.VALUE_TID_GETAWAYS: {
			if (pCategoryModel.isSelectedTemp()) {
				catIconResId = R.drawable.cat_tid290_selected;
			} else {
				catIconResId = R.drawable.cat_tid290_getaways;
			}
			break;
		}
		default: {
			if (pCategoryModel.isSelectedTemp()) {
				catIconResId = R.drawable.ic_default_cat_sel;
			} else {
				catIconResId = R.drawable.ic_default_cat_unsel;
			}
			break;
		}
		}
		pNetworkImageView.setImageResource(catIconResId);
		pNetworkImageView.setErrorImageResId(catIconResId);
		pNetworkImageView.setDefaultImageResId(catIconResId);
	}

	/**
	 * @param offerLocationsList
	 * @param userLocation
	 * @return
	 */
	public static LatLng getNearestOffer(ArrayList<String> offerLocationsList, Location userLocation) {
		double[] offerLatLngArr = null;
		double[] nearestOfferLatLngArr = null;
		float distance = 0;
		float minDistance = 0;
		for (String offerLocation : offerLocationsList) {
			offerLatLngArr = getLatLongFromString(offerLocation);
			if (offerLatLngArr == null) {
				continue;
			}
			if (userLocation == null || offerLocationsList.size() == 1) {
				nearestOfferLatLngArr = offerLatLngArr;
				break;
			}
			distance = LocationUtils.getDistance(userLocation.getLatitude(), userLocation.getLongitude(), offerLatLngArr[0], offerLatLngArr[1]);
			if (distance != -1 && (minDistance == 0 || distance < minDistance)) {
				minDistance = distance;
				nearestOfferLatLngArr = offerLatLngArr;
			}
		}
		return new LatLng(nearestOfferLatLngArr[0], nearestOfferLatLngArr[1]);
	}

	/**
	 * This method is common for deals and categories. <br/>
	 * It can be used else where as well.
	 * 
	 * @param pBaseUrlWithDensityGroupName
	 * @param pImagePath
	 * @return
	 */
	public static String createImageUrl(String pBaseUrlWithDensityGroupName, String pImagePath) {
		try {
			return Uri.parse(pBaseUrlWithDensityGroupName).buildUpon().appendPath(pImagePath).toString();
		} catch (Exception e) {
			Log.e(LOG_TAG, "createImageUrl()" + e);
			return pBaseUrlWithDensityGroupName + pImagePath;
		}

	}

	/**
	 * @param pActivity
	 */
	public static View showNotificationOnScreen(Activity pActivity) {
		int notifyType = pActivity.getIntent().getIntExtra(Constants.EXTRA_NOTIFY_TYPE, 0);
		String message = pActivity.getIntent().getStringExtra(Constants.EXTRA_MESSAGE);
		return showNotificationOnScreen(pActivity, notifyType, message);
	}

	/**
	 * @param pActivity
	 * @param pNotifyType
	 * @param pMessage
	 * 
	 * @return
	 */
	public static View showNotificationOnScreen(Activity pActivity, int pNotifyType, String pMessage) {
		View onScreenNotificationRootLayout = pActivity.findViewById(R.id.relative_notification_on_screen);
		if (onScreenNotificationRootLayout == null) {
			return null;
		}
		if (StringUtils.isNullOrEmpty(pMessage)) {
			onScreenNotificationRootLayout.setVisibility(View.GONE);
			return null;
		}
		onScreenNotificationRootLayout.setTag(pNotifyType);

		TextView txvNotificationOnScreen = (TextView) pActivity.findViewById(R.id.txv_notification_on_screen);

		txvNotificationOnScreen.setText(pMessage);
		if (pActivity instanceof OnClickListener) {
			OnClickListener onClickListener = (OnClickListener) pActivity;
			txvNotificationOnScreen.setOnClickListener(onClickListener);
			pActivity.findViewById(R.id.img_close_notification_on_screen).setOnClickListener(onClickListener);
		}
		UiUtils.expand(onScreenNotificationRootLayout);
		if (pActivity instanceof ProductDetailActivity) {
			pActivity.findViewById(R.id.view_bottom_divider_notification_on_screen).setVisibility(View.GONE);
		}
		return onScreenNotificationRootLayout;
	}

	/**
	 * @param pView
	 * @return
	 */
	public static int getOnScreenNotificationType(Activity pActivity) {
		View onScreenNotificationRootLayout = pActivity.findViewById(R.id.relative_notification_on_screen);
		Object notifyTypeTag = onScreenNotificationRootLayout.getTag();
		if (notifyTypeTag instanceof Integer) {
			return (Integer) notifyTypeTag;
		}
		return 0;
	}

	/**
	 * @param couponFromSameMerchant
	 * @param countedLocationsList
	 * @return
	 */
	public static boolean isInAlreadyCountedLocationsList(MyOfferLocation couponFromSameMerchant, List<MyOfferLocation> countedLocationsList) {
		int orderId = couponFromSameMerchant.getOrder_id();
		int deald = couponFromSameMerchant.getDeal_id();
		int offerId = couponFromSameMerchant.getOffer_id();
		for (MyOfferLocation otherLocation : countedLocationsList) {
			if (otherLocation.getOrder_id() == orderId && otherLocation.getDeal_id() == deald && otherLocation.getOffer_id() == offerId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param pContext
	 * @return
	 */
	public static String getCouponDiscountText(Context pContext, boolean pForNonGoUsers) {
		float couponDiscPercentage = GrouponGoPrefs.getCouponDiscountPercenatge(pContext);
		if (couponDiscPercentage != 0) {
			return StringUtils.getFormatDecimalAmount(couponDiscPercentage, 1) + "%";
		}
		float couponDiscMoney = GrouponGoPrefs.getCouponDiscountMoney(pContext);
		if (pForNonGoUsers) {
			return "Rs. " + StringUtils.getFormatDecimalAmount(couponDiscMoney);
		}
		if (couponDiscMoney == 0) {
			return null;
		}
		return pContext.getString(R.string.Rs) + " " + StringUtils.getFormatDecimalAmount(couponDiscMoney);
	}

	/**
	 * @param intent
	 * @return
	 */
	public static boolean shouldRefreshDeals(Context pContext, Intent pLocationFetchedIntent) {
		String locationProvider = pLocationFetchedIntent.getStringExtra(LocationService.EXTRA_LOCATION_PROVIDER);
		double currentLatitude = pLocationFetchedIntent.getDoubleExtra(LocationService.EXTRA_CURRENT_LATITUDE, 0);
		double currentLongitude = pLocationFetchedIntent.getDoubleExtra(LocationService.EXTRA_CURRENT_LONGITUDE, 0);
		Location currentLocation = new Location(locationProvider);
		currentLocation.setLatitude(currentLatitude);
		currentLocation.setLongitude(currentLongitude);

		Location nearByDealsLocation = GrouponGoPrefs.getNearByDealsLocation(pContext);
		GrouponGoPrefs.setDeviceLocation(pContext, currentLocation);
		int dealsUpdateDistance = Constants.DEFAULT_DEALS_REFRESH_METERS;
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "shouldRefreshDeals(): Deals Update Distance is " + dealsUpdateDistance + " meters");
		}
		if (dealsUpdateDistance == -1) {
			return false;
		}
		if (nearByDealsLocation == null) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "shouldRefreshDeals(): true. Last Near By Deals Location is null.");
			}
			return true;
		}
		float movedDistance = LocationUtils.getDistance(nearByDealsLocation.getLatitude(), nearByDealsLocation.getLongitude(), currentLatitude, currentLongitude);
		boolean shouldRefreshDeals = movedDistance > dealsUpdateDistance;
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "shouldRefreshDeals(): " + shouldRefreshDeals + ". Moved " + movedDistance + " meters");
		}
		return shouldRefreshDeals;
	}

	/**
	 * completes deals image base URL, also saves and return it
	 * 
	 * @param pContext
	 * @param pDealsImageBaseUrl
	 * @return
	 */
	public static String completeDealsImageBaseUrl(Context pContext, String pDealsImageBaseUrl) {
		String densityGroupNameStr = GrouponGoPrefs.getDeviceDensityGroup(pContext);
		pDealsImageBaseUrl += densityGroupNameStr;
		GrouponGoPrefs.setDealsImageBaseUrl(pContext, pDealsImageBaseUrl);
		return pDealsImageBaseUrl;
	}

	/**
	 * @param pContext
	 * @param pCancel
	 */
	public static void setPeriodicDealsRefreshBroadcast(Context pContext, boolean pCancel) {
		if (BuildConfig.DEBUG) {
			Log.i(LOG_TAG, "setPeriodicDealsRefreshBroadcast() " + pCancel);
		}
		Intent broadcastIntent = new Intent(Constants.ACTION_DEALS_REFRESH_TIMER);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(pContext, 0, broadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) pContext.getSystemService(Context.ALARM_SERVICE);
		if (pCancel) {
			am.cancel(pendingIntent);
		} else {
			int dealsRefreshMinutes = GrouponGoPrefs.getDealsUpdateInterval(pContext);
			if (dealsRefreshMinutes <= 0) {
				dealsRefreshMinutes = Constants.DEFAULT_DEALS_REFRESH_MINUTES;
			}
			long dealsRefreshMillis = dealsRefreshMinutes * DateUtils.MINUTE_IN_MILLIS;
			long triggerTimeMillis = SystemClock.elapsedRealtime() + dealsRefreshMillis;
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTimeMillis, pendingIntent);
		}
	}

	/**
	 * @param pLabelOfferStr
	 * @param pOfferWeight
	 * @param pOffersListSize
	 * @return
	 */
	public static String getOfferName(String pLabelOfferStr, int pOfferWeight, int pOffersListSize) {
		if (pOffersListSize == 1 && pOfferWeight == 1) {
			return pLabelOfferStr;
		} else {
			return pLabelOfferStr + " " + pOfferWeight;
		}
	}

	/**
	 * @param mOfferModelList
	 * @return
	 */
	public static boolean isAnyVoucherExpired(ArrayList<OfferModel> mOfferModelList) {
		for (OfferModel model : mOfferModelList) {
			if (isVoucherExpired(model)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param pMyOfferModel
	 * @return
	 */
	public static boolean isVoucherExpired(OfferModel pMyOfferModel) {
		String dateStr = pMyOfferModel.getOffer_redem_timestamp();
		long maxRedemTimeMillis = DateTimeUtils.parseExtendedDate(dateStr);
		return maxRedemTimeMillis < new Date().getTime();
	}

	/**
	 * @param pCitiesArrayList
	 * @param pReverseGeoCodedCityName
	 * @return
	 */
	public static CityModel getSelectableCurrentCity(List<CityModel> pCitiesArrayList, String pReverseGeoCodedCityName) {
		if (pCitiesArrayList == null || pCitiesArrayList.isEmpty() || StringUtils.isNullOrEmpty(pReverseGeoCodedCityName)) {
			return null;
		}
		for (CityModel cityModel : pCitiesArrayList) {
			if (pReverseGeoCodedCityName.equalsIgnoreCase(cityModel.getCity_name())) {
				return cityModel;
			}
		}
		return null;
	}

	/**
	 * @param pCitiesArrayList
	 * @param pCurrentLocation
	 * @return
	 */
	public static CityModel getAppropriateCity(List<CityModel> pCitiesArrayList, Location pCurrentLocation) {
		if (pCitiesArrayList == null || pCitiesArrayList.isEmpty() || pCurrentLocation == null) {
			return null;
		}
		CityModel nearestCityModel = null;
		float tempDistance = 0;
		float minDistance = 0;
		for (CityModel cityModel : pCitiesArrayList) {
			tempDistance = LocationUtils.getDistance(pCurrentLocation.getLatitude(), pCurrentLocation.getLongitude(), cityModel.getCity_lat(), cityModel.getCity_long());
			if (tempDistance == -1) {
				continue;
			}
			if (minDistance == 0 || tempDistance < minDistance) {
				minDistance = tempDistance;
				nearestCityModel = cityModel;
			}
		}
		if (minDistance < Constants.PROXIMITY_METERS_TO_CHANGE_CITY) {
			return nearestCityModel;
		} else {
			return null;
		}
	}

	/**
	 * @param pCardNumberStr
	 * @return
	 */
	public static String getRegexBasedCardType(String pCardNumberStr) {
		if (Pattern.matches(REGEX_AMEX_CARD, pCardNumberStr)) {
			return ApiConstants.VALUE_CARD_TYPE_AMERICAN_EXPRESS;
		}
		return null;
	}
}
