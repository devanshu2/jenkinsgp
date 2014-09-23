package com.groupon.go.constants;

/**
 * constants for API requests used in activities and controllers
 * 
 * @author sachin.gupta
 */
public interface ApiConstants {

	/**
	 * Base URLs
	 */
	String	BASE_URL_DEV						= "http://54.86.13.187:3000/";
	String	BASE_URL_QA							= "http://54.84.252.178/";
	String	BASE_URL_STAGING					= "http://prenodejs-912423462.us-east-1.elb.amazonaws.com:3000/";
	String	BASE_URL_PROD_LONG					= "http://groupnodejs-1216120112.ap-southeast-1.elb.amazonaws.com/";
	String	BASE_URL_PROD_TINY					= "http://api.groupongo.in/";

	/**
	 * If BASE_URL is changed here, <br/>
	 * then values for deal_share_link must be updated accordingly in <br/>
	 * 1. AndroidManifest.xml and <br/>
	 * 2. strings.xml
	 */
	String	BASE_URL							= BASE_URL_PROD_TINY;

	/**
	 * API EndPoints
	 */
	String	COUNTRY_LIST_URL					= BASE_URL + "country";
	String	USER_REGISTERATION_URL				= BASE_URL + "registration";
	String	GET_API_AUTH_TOKEN_URL				= BASE_URL + "regenToken";
	String	CODE_VERIFICATION_URL				= BASE_URL + "codeVerification";
	String	RESEND_VERIFICATION_CODE_URL		= BASE_URL + "resendCode";
	String	DEVICE_REGISTRATION_URL				= BASE_URL + "deviceRegistration";
	String	GET_PROFILE_URL						= BASE_URL + "getProfile";
	String	UPDATE_PROFILE_URL					= BASE_URL + "updateProfile";
	String	CITY_LISTING_URL					= BASE_URL + "cityListing";
	String	DEAL_LISTING_URL					= BASE_URL + "dealListing";
	String	DEAL_DETAIL_URL						= BASE_URL + "dealDetails";
	String	SEARCH_SUGGESTIONS_URL				= BASE_URL + "autoSearch";
	String	SEARCH_RESULTS_URL					= BASE_URL + "searchResult";
	String	UPDATE_CONTACTS_URL					= BASE_URL + "getRegisteredNumbers";
	String	SHARE_VIA_PHONEBOOK_URL				= BASE_URL + "userSelectedNumbers";
	String	SHARING_DEAL_DETAIL_URL				= BASE_URL + "dealDetailsForLink";
	String	ADD_TO_CART_URL						= BASE_URL + "addtoCart";
	String	CREATE_ORDER_URL					= BASE_URL + "createOrder";
	String	GET_PAYMENT_STATUS_URL				= BASE_URL + "getStatus";
	String	CREATE_ORDER_SINGLE_URL				= BASE_URL + "createordersingle";
	String	GET_SAVES_CARDS_URL					= BASE_URL + "getCards";
	String	CREATE_ORDER_FRM_CARD_URL			= BASE_URL + "createordercard";
	String	CREATE_ORDER_SINGLE_FRM_CARD_URL	= BASE_URL + "createordercardsingle";
	String	USER_CART_URL						= BASE_URL + "viewCart";
	String	USER_ORDER_STATUS_URL				= BASE_URL + "orderStatus";
	String	USER_EDIT_CART_URL					= BASE_URL + "editCart";
	String	USER_COUPONS_URL					= BASE_URL + "allCoupons";
	String	USER_COUPON_DETAIL_URL				= BASE_URL + "couponDetails";
	String	DELETE_CART_URL						= BASE_URL + "deleteCart";
	String	REDEEM_VOUCHER_URL					= BASE_URL + "redeemvoucher";
	String	PAYU_CONFIGURATION_URL				= BASE_URL + "getPaymentConfig";
	String	GET_ALL_BANK_URL					= BASE_URL + "getBanks";
	String	GET_APPLY_COUPON_CODE_URL			= BASE_URL + "applyCoupon";
	String	GET_REMOVE_COUPON_CODE_URL			= BASE_URL + "removeCoupon";
	String	GET_REMOVE_CART_URL					= BASE_URL + "resetUserCart";

	String	GET_CHANGE_MOBILE_NUMBER_URL		= BASE_URL + "updateMobile";
	String	GET_VERIFY_CHANGE_MOBILE_NUMBER_URL	= BASE_URL + "verifyMobile";

	String	OFFER_SUMMARY_LIST_URL				= BASE_URL + "allOrders";
	String	GET_IMAGE_SIGNATURE_URL				= BASE_URL + "getSignature";
	String	REMOVE_USER_IMAGE_URL				= BASE_URL + "removePic";
	String	REDEEM_USER_CREDIT_URL				= BASE_URL + "redeemCredits";
	String	REMOVE_USER_CREDIT_URL				= BASE_URL + "removeCredit";
	String	PURCHASE_WITHOUT_PAYMENT_URL		= BASE_URL + "pay";
	String	GET_STATIC_WEB_PAGE_URL				= BASE_URL + "staticPage";
	String	GET_SUMMARY_BY_EMAIL_URL			= BASE_URL + "sendMail";
	String	GET_MY_PURCHASES_URL				= BASE_URL + "myPurchases";
	String	POST_REDEEMED_VOUCHERS_URL			= BASE_URL + "voucherRedeem";

	/**
	 * API Param VALUES
	 */
	int		VALUE_OS_ANDROID					= 1;
	int		VALUE_OS_IOS						= 2;
	String	VALUE_API_VERSION					= "2.0";

	/**
	 * category tid - hardcoded to map with icons
	 */
	int		VALUE_TID_DUMMY_FOR_ALL_DEALS		= -1;
	int		VALUE_TID_FOOD_AND_DRINKS			= 219;
	int		VALUE_TID_BEAUTY_AND_SPA			= 225;
	int		VALUE_TID_HEALTH_AND_FITNESS		= 236;
	int		VALUE_TID_EVENT_AND_ACTIVITIES		= 243;
	int		VALUE_TID_SERVICES					= 261;
	int		VALUE_TID_SHOPPING					= 276;
	int		VALUE_TID_GETAWAYS					= 290;

	/**
	 * FEATURED is renamed to Our Picks. Its position is 3rd now
	 */
	int		VALUE_DEAL_TYPE_FEATURED			= 1;

	/**
	 * TRENDING is renamed to Best Selling. Its position is 2nd
	 */
	int		VALUE_DEAL_TYPE_TRENDING			= 2;

	/**
	 * NEAR_BY position is 1st now
	 */
	int		VALUE_DEAL_TYPE_NEAR_BY				= 3;

	/**
	 * string constants used as store card codes
	 */
	String	VALUE_STORE_CARD_ON_PAYMENT			= "1";
	String	VALUE_NOT_STORE_CARD_ON_PAYMENT		= "0";

	/**
	 * int constants used as payuStatus
	 */
	int		PAYU_STATUS_FAIL					= 0;

	/**
	 * common values for Payment Mode, card-type and bank-code
	 */
	String	VALUE_CREDIT_CARD					= "CC";
	String	VALUE_DEBIT_CARD					= "DC";

	/**
	 * additional values for Payment Gateway
	 */
	String	VALUE_PAY_BY_NET_BANKING			= "NB";
	String	VALUE_PAY_BY_GROUPON_CREDIT			= "GC";

	/**
	 * additional values for cardTypes
	 */
	String	VALUE_CARD_TYPE_MASTERCARD			= "MASTERCARD";
	String	VALUE_CARD_TYPE_VISA				= "VISA";
	String	VALUE_CARD_TYPE_AMERICAN_EXPRESS	= "AMEX";

	/**
	 * Net Banking bank codes
	 */
	String	VALUE_NB_CODE_CITY_BANK				= "CITNB";

	/**
	 * value for check cardType ,IssuingBank
	 */
	String	VALUE_CARD_TYPE_UNKNOWN				= "UNKNOWN";

	/**
	 * Coupon types
	 */
	int		VALUE_COUPON_TYPE_ALL				= 1;
	int		VALUE_COUPON_TYPE_EXPIRING			= 2;
	int		VALUE_COUPON_TYPE_NEARBY			= 3;

	/**
	 * values for {@link ApiConstants#PARAM_MESSAGE_TYPE} <br/>
	 * to be sent in {@link ApiConstants#API_SHARE_VIA_PHONEBOOK}
	 */
	int		VALUE_MESSAGE_TYPE_SHARED_DEAL		= 1;
	int		VALUE_MESSAGE_TYPE_PURCHASED_DEAL	= 2;
	int		VALUE_MESSAGE_TYPE_REDEEMED_DEAL	= 3;
	int		VALUE_MESSAGE_TYPE_UNIQUE_CODE		= 4;
	int		VALUE_MESSAGE_TYPE_SHARE_APP		= 5;

	/**
	 * values for {@link ApiConstants#PARAM_PAGE_ID} <br/>
	 * to be sent in {@link ApiConstants#GET_HTML_PAGE_FROM_SERVER}
	 */
	int		VALUE_HTML_TERMS_OF_USE				= 1;
	int		VALUE_HTML_PRIVACY_POLICY			= 2;

	/**
	 * notify_type to be received in push messages
	 * 
	 * @note we are also using same values for EXTRA_NOTIFY_TYPEtypes
	 */
	int		VALUE_NOTIFY_TYPE_SHOW_DEAL			= 1;
	int		VALUE_NOTIFY_TYPE_SHOW_COUPONS		= 2;
	int		VALUE_NOTIFY_TYPE_PROMO_CODE		= 3;
	int		VALUE_NOTIFY_TYPE_UNIQUE_CODE		= 4;
	int		VALUE_NOTIFY_TYPE_CITY_CHANGED		= 5;

	/**
	 * API error codes
	 */
	int		ERROR_CODE_AUTH_FAILURE				= 400;
	int		ERROR_CODE_NUM_VERIFICATION_FAILED	= 2;
	int		ERROR_CODE_OTP_EXPIRED				= 8;
	int		ERROR_CODE_OFFER_OUT_OF_STOCK		= 3;

	/**
	 * API Header Names
	 */
	String	HEADER_API_KEY						= "api_key";

	/**
	 * status success in case of without charge purchase
	 */
	String	VALUE_STATUS						= "success";

	/**
	 * value for requestType for delete userPaymentCard
	 */
	String	VALUE_DELETE_CARD					= "1";

	/**
	 * Push Param Names
	 */
	String	PARAM_ALERT							= "alert";
	String	PARAM_SENDER_NAME					= "sender_name";
	String	PARAM_NOTIFY_TYPE					= "notify_type";
	String	PARAM_PROMO_CODE					= "promo_code";

	/**
	 * API Param Names
	 */
	String	PARAM_OS							= "os";
	String	PARAM_VERSION						= "version";
	String	PARAM_DEVICE_MODEL					= "device_model";
	String	PARAM_OS_VERSION					= "os_version";
	String	PARAM_DEVICE_UNIQUE_ID				= "device_unique_id";
	String	PARAM_APP_VERSION					= "app_version";
	String	PARAM_PUSH_TOKEN					= "push_token";

	String	PARAM_MOBILE_NUMBER					= "mobile_number";
	String	PARAM_COUNTRY_CODE					= "country_code";
	String	PARAM_DEVICE_ID						= "device_id";
	String	PARAM_VERIFICATION_CODE				= "verification_code";
	String	PARAM_USER_ID						= "user_id";
	String	PARAM_LAT							= "lat";
	String	PARAM_LONG							= "long";
	String	PARAM_CITY_ID						= "city_id";
	String	PARAM_PREFERENCES					= "preferences";
	String	PARAM_DEAL_TYPE						= "deal_type";
	String	PARAM_CATEGORY_ID					= "cat_id";
	String	PARAM_SEARCH_KEY					= "search_key";
	String	PARAM_DEAL_ID						= "deal_id";
	String	PARAM_PAGE_NO						= "page_no";
	String	PARAM_PAGE_COUNT					= "page_count";
	String	PARAM_TEXT							= "text";
	String	PARAM_USER_NAME						= "user_name";
	String	PARAM_EMAIL_ID						= "email_id";
	String	PARAM_IMAGE_URL						= "image_url";
	String	PARAM_MOBILE_NUMBERS				= "mobile_numbers";
	String	PARAM_GO_MOBILE_NUMBERS				= "go_mobile_numbers";
	String	PARAM_NON_GO_MOBILE_NUMBERS			= "non_go_mobile_numbers";
	String	PARAM_OFFER_ID						= "offer_id";
	String	PARAM_OFFER_QUANTITY				= "offer_quantity";
	String	PARAM_OFFER_PRICE					= "offer_price";
	String	PARAM_TOTAL_PRICE					= "total_price";
	String	PARAM_OFFER_ARRAY					= "offer_array";
	String	PARAM_PG							= "pg";
	String	PARAM_CART_ID						= "cart_id";
	String	PARAM_CVV							= "ccvv";
	String	PARAM_CARD_TOKEN					= "card_token";
	String	PARAM_SINGLE						= "single";
	String	PARAM_COUPON_TYPE					= "coupon_type";
	String	PARAM_MESSAGE_TYPE					= "message_type";
	String	PARAM_ORDER_ID						= "order_id";
	String	PARAM_COUPON_CODE					= "coupon_code";
	String	PARAM_COUPON_DISC_TEXT_GO_USER		= "coupon_disc_text_go_user";
	String	PARAM_COUPON_DISC_TEXT_NON_GO_USER	= "coupon_disc_text_non_go_user";
	String	PARAM_IS_SINGLE						= "is_single";
	String	PARAM_NEW_NUMBER					= "new_number";
	String	PARAM_IS_NUMBER_CHANGE				= "is_number_change";
	String	PARAM_DATE							= "date";
	String	PARAM_CREDIT_USED					= "credit_used";
	String	PARAM_TXN_ID						= "txnid";
	String	PARAM_STATUS						= "status";
	String	PARAM_COMMAND_TYPE					= "command_type";
	String	PARAM_PAGE_ID						= "page_id";
	String	PARAM_VOUCHERS						= "vouchers";
	String	PARAM_CAT_LIST_NEEDED				= "cat_list_needed";
	String	PARAM_SESSION_EXPIRED				= "session_expired";
	String	PARAM_ONLY_ID						= "id";

	/**
	 * PayU common parameters
	 */
	String	PARAM_PAYU_KEY						= "key";
	String	PARAM_PAYU_COMMAND					= "command";
	String	PARAM_PAYU_HASH						= "hash";
	String	PARAM_PAYU_REDIRECT_URL				= "url";

	/**
	 * PayU - CHECK_IS_DOMESTC parameters
	 */
	String	PARAM_CARD_BIN						= "var1";

	/**
	 * PayU - SAVE USER CARD parameters
	 */
	String	PARAM_USER_CREDENTIAL				= "var1";
	String	PARAM_CARD_NICK_NAME				= "var2";
	String	PARAM_CARD_MODE						= "var3";
	String	PARAM_CARD_TYPE						= "var4";
	String	PARAM_NAME_AS_ON_CARD				= "var5";
	String	PARAM_CARD_NUMBER					= "var6";
	String	PARAM_CARD_EXP_MONTH				= "var7";
	String	PARAM_CARD_EXP_YEAR					= "var8";

	/**
	 * PayU - DELETE USER CARD parameters
	 */
	String	PARAM_PAY_CARD_TOKEN				= "var2";

	/**
	 * param names for AWS upload
	 */
	String	PARAM_AWS_ACCESS_ID					= "AWSAccessKeyId";
	String	PARAM_ACL							= "acl";
	String	PARAM_KEY							= "key";
	String	PARAM_POLICY						= "policy";
	String	PARAM_SUCCESS_ACTION_REDIRECT		= "success_action_redirect";
	String	PARAM_SIGNATURE						= "signature";
	String	PARAM_CONTENT_TYPE					= "Content-Type";
	String	PARAM_FILE							= "file";

	/**
	 * Integer constants to define request types between activities and
	 * controllers
	 */
	int		GET_COUNTRY_LIST					= 1;
	int		USER_REGISTRATION					= 2;
	int		CODE_VERIFICATION					= 3;
	int		RESEND_VERIFICATION_CODE			= 4;
	int		GET_PROFILE_FROM_SERVER				= 5;
	int		GET_PROFILE_FROM_CACHE				= 6;
	int		UPDATE_PROFILE						= 7;
	int		GET_CITY_LIST_FROM_SERVER			= 8;
	int		GET_CITY_LIST_FROM_CACHE			= 9;
	int		GET_GEO_ADDRESS_BY_LAT_LONG			= 10;
	int		GET_CITY_BY_LAT_LONG_GOOGLE_API		= 11;
	int		API_DEVICE_REGISTRATION				= 12;
	int		GET_DEALS_LIST_FROM_CACHE			= 13;
	int		GET_DEALS_LIST_FROM_SERVER			= 14;
	int		GET_DEALS_DETAIL					= 15;
	int		GET_SEARCH_SUGGESTIONS				= 16;
	int		GET_SEARCH_RESULTS					= 17;
	int		UPDATE_CONTACT_LIST					= 18;
	int		API_SHARE_VIA_PHONEBOOK				= 19;
	int		GET_SHARING_DEALS_DETAIL			= 20;
	int		ADD_DEAL_TO_CART					= 21;
	int		GET_CREATE_ORDER					= 22;
	int		GET_PAYMENT_STATUS					= 23;
	int		GET_CREATE_ORDER_SINGLE				= 24;
	int		GET_API_AUTH_TOKEN					= 25;
	int		GET_SAVED_CARDS						= 26;
	int		GET_CREATE_ORDER_FRM_CARD			= 27;
	int		GET_CREATE_ORDER_SINGLE_FRM_CARD	= 28;
	int		GET_USER_CART_FROM_CACHE			= 29;
	int		GET_USER_CART_FROM_SERVER			= 30;
	int		GET_USER_ORDER_STATUS				= 31;
	int		EDIT_DEAL_IN_CART					= 32;
	int		GET_COUPONS							= 33;
	int		GET_COUPON_DETAILS					= 34;
	int		DELETE_DEAL_FROM_CART				= 35;
	int		GET_REDEEM_VOUCHER					= 36;
	int		GET_PAYU_CONFIGURATION				= 37;
	int		GET_SAVE_USER_CARD					= 38;
	int		GET_PAYU_CONFIG_TO_VERIFY_CARD		= 39;
	int		GET_ALL_BANK						= 40;
	int		APPLY_COUPON_CODE					= 41;
	int		REMOVE_COUPON_CODE					= 42;
	int		GET_REMOVE_SINGLE_CART				= 43;
	int		GET_CHANGE_NUMBER					= 44;
	int		GET_VERIFY_CHANGE_NUMBER			= 45;
	int		GET_ALL_ORDERS						= 46;
	int		GET_IMAGE_SIGNATURE					= 47;
	int		UPLOAD_USER_IMAGE					= 48;
	int		REMOVE_USER_IMAGE					= 49;
	int		DELETE_USER_CART					= 50;
	int		APPLY_USER_CREDITS					= 51;
	int		REMOVE_USER_CREDITS					= 52;
	int		GET_PATH_FOR_DIRECTION				= 53;
	int		PURCHASE_WITHOUT_PAYMENT			= 54;
	int		DELETE_PAY_CARD_ON_PAY_U			= 55;
	int		GET_HTML_PAGE_FROM_CACHE			= 56;
	int		GET_HTML_PAGE_FROM_SERVER			= 57;
	int		GET_SUMMARY_BY_EMAIL				= 58;
	int		GET_MY_PURCHASES					= 59;
	int		POST_REDEEMED_VOUCHERS				= 60;
}
