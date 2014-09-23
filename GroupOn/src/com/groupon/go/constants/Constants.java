package com.groupon.go.constants;

import android.text.format.DateUtils;

public interface Constants {

	/**
	 * int constants used as settings
	 */
	int		MAX_DISTANCE_KM_TO_SHOW_ON_LISTS	= 30;
	int		DEFAULT_CATEGORY_ROWS_ON_HOME		= 0;
	int		MAX_DISTANCE_KM_TO_SHOW_ROUTE		= 100;
	int		PROXIMITY_METERS_TO_CHANGE_CITY		= 50 * 1000;
	int		DEFAULT_DEALS_REFRESH_METERS		= 500;
	int		DEFAULT_DEALS_REFRESH_MINUTES		= 10;
	long	PERSIST_SELECTED_CITY_MILLIS		= 4 * DateUtils.HOUR_IN_MILLIS;

	/**
	 * string constant use as actions in intents
	 */
	String	ACTION_GET_PROFILE_RESP_RCVD		= "action_get_profile_resp_rcvd";
	String	ACTION_DEALS_REFRESH_TIMER			= "action_deals_refresh_timer";
	String	ACTION_CART_COUNT_UPDATED			= "action_cart_count_updated";
	String	ACTION_MY_PURCHASES_RESP_RCVD		= "action_my_purchases_resp_rcvd";

	/**
	 * string constant use as keys in transferring data
	 */
	String	EXTRA_DIALOG_ID						= "extra_dialog_id";
	String	EXTRA_DIALOG_TITLE					= "extra_dialog_title";
	String	EXTRA_CITY_NAME						= "extra_city_name";

	String	EXTRA_AUTO_OPEN_NEXT_SCREEN			= "extra_auto_open_next_screen";

	String	EXTRA_COUNTRY_CODE					= "extra_country_code";
	String	EXTRA_USER_NUMBER					= "extra_user_number";
	String	EXTRA_NEW_NUMBER					= "extra_new_number";
	String	EXTRA_USER_NAME						= "extra_user_name";
	String	EXTRA_EMAIL_ID						= "extra_email_id";

	String	EXTRA_SELECTED_CATEGORY				= "extra_selected_category";
	String	EXTRA_CATEGORY_ID					= "extra_category_id";
	String	EXTRA_CAT_NAME						= "extra_cat_name";

	String	EXTRA_DEAL_TYPE						= "extra_deal_type";
	String	EXTRA_DEAL_MODEL					= "extra_deal_model";
	String	EXTRA_DEAL_ID						= "extra_deal_id";
	String	EXTRA_DEAL_IMAGE_URL				= "extra_deal_image_url";
	String	EXTRA_DEAL_TITLE					= "extra_deal_title";
	String	EXTRA_OFFER_MODEL_LIST				= "extra_offer_model_list";
	String	EXTRA_LOCATION_LIST					= "extra_location_list";
	String	EXTRA_MERCHANT_NAME					= "extra_merchant_name";

	String	EXTRA_SEARCH_TEXT					= "extra_search_text";
	String	EXTRA_PAYMENT_MODE					= "extra_pay_mode";
	String	EXTRA_OFFERS_JSON_STRING			= "extra_offers_json";
	String	EXTRA_GO_CONTACTS_JSON				= "extra_go_contacts_json";
	String	EXTRA_NON_GO_CONTACTS_JSON			= "extra_non_go_contacts_json";
	String	EXTRA_PAYMENT_FROM					= "extra_pay_from";
	String	EXTRA_IS_ADD_CART_FOR_PAYMENT		= "extra_is_add_cart_for_payment";
	String	EXTRA_TRANSX_ID						= "extra_transx_id";
	String	EXTRA_FROM_CART						= "extra_from_cart";
	String	EXTRA_COUPON_TYPE					= "extra_coupon_type";
	String	EXTRA_ORDER_ID						= "extra_order_id";
	String	EXTRA_MESSAGE_TYPE					= "extra_message_type";
	String	EXTRA_GRAND_TOTAL					= "extra_grand_total";
	String	EXTRA_FROM_ACTIVITY					= "extra_from_activity";
	String	EXTRA_USER_CREDENTIAL				= "extra_user_credential";
	String	EXTRA_COUPON_CODE					= "extra_coupon_code";
	String	EXTRA_OFFER_DESC					= "extra_offer_desc";
	String	EXTRA_API_TYPE						= "extra_api_type";
	String	EXTRA_NEAREST_MARKER_LATLNG			= "extra_nearest_marker_latlng";
	String	EXTRA_NOTIFY_TYPE					= "extra_notify_type";
	String	EXTRA_MESSAGE						= "extra_message";
	String	EXTRA_VERIFICATION_CODE				= "extra_verification_code";
	String	EXTRA_ERROR_CODE					= "extra_error_code";
	String	EXTRA_NAME_SET_VIA_ADD_NEW_CARD		= "extra_name_set_via_add_new_card";

	/**
	 * int constants used as request codes between activities
	 */
	int		RQ_LAUNCHED_FROM_SPLASH_SCREEN		= 1;
	int		RQ_REGISTRATION_TO_VERIFY_SCREEN	= 2;
	int		RQ_OPEN_LOCATION_SETTINGS			= 3;
	int		RQ_SELECT_CITY_IN_REG_FLOW			= 4;
	int		RQ_HOME_TO_CITY_SELECTION			= 5;
	int		RQ_CODE_OPEN_CAMERA_FOR_IMAGES		= 6;
	int		RQ_CODE_OPEN_GALLERY_FOR_IMAGES		= 7;
	int		RQ_USER_CART_TO_EDIT_CART			= 8;
	int		RQ_PLAY_SERVICES_RESOLUTION			= 9;
	int		RQ_CHANGE_NUM_TO_VERIFY_SCREEN		= 10;
	int		RQ_USER_CARDS_TO_ADD_NEW_CARD		= 11;

	/**
	 * string constants for dialog tags
	 */
	String	TAG_USER_REGISTER_DIALOG			= "user_register_dialog";
	String	TAG_REDEEM_COUPON_DIALOG			= "redeem_coupon_dialog";
	String	TAG_RESEND_VERIF_CODE_DIALOG		= "resend_verif_code_dialog";
	String	TAG_LOCATION_IS_OFF_DIALOG			= "location_is_off_dialog";
	String	TAG_GPS_IS_OFF_DIALOG				= "gps_is_off_dialog";
	String	TAG_CITY_CHANGE_CONFIRM_DIALOG		= "city_change_confirm_dialog";
	String	TAG_APPLY_COUPON_CODE_DIALOG		= "enter_apply_code_dialog";
	String	TAG_REMOVE_COUPON_CODE_DIALOG		= "remove_coupon_code_dialog";
	String	TAG_CUSTOM_PROGRESS_BAR				= "custom_progress_dialog";
	String	TAG_CONFIRMATION_CLR_CART_DIALOG	= "clear_cart_dialog";
	String	TAG_CONFIRM_REMOVE_STORED_CARD		= "confirm_remove_stored_card";
	String	TAG_CONFIRM_REMOVE_PROFILE_PIC		= "confirm_remove_profile_pic";
	String	TAG_ALERT_OFFER_COUNT_REVISED		= "tag_alert_offer_count_revised";
	String	TAG_ALERT_MESSAGE_FROM_CART_APIS	= "alert_message_from_cart_apis";
	String	TAG_ALERT_SERVER_MSG_TO_REVIEW_CART	= "alert_server_msg_to_review_cart";

	/**
	 * string constants for dialog tags
	 */
	String	STATE_PAGER_INDEX					= "state_pager_index";

	/**
	 * string constants for dialog tags
	 */
	String	TEMPLATE_COMMON_WEB_VIEW			= "html/template.htm";
	String	TEMPLATE_DEAL_DETAILS_WEB_VIEW		= "html/deal_details_template.htm";
}