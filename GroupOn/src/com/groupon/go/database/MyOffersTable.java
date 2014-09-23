package com.groupon.go.database;

import java.util.ArrayList;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.groupon.go.model.OfferModel;
import com.kelltontech.database.BaseTable;
import com.kelltontech.model.BaseModel;

/**
 * @author sachin.gupta
 */
public class MyOffersTable extends BaseTable {

	private static final String	CN_PURCHASE_KEY				= "purchase_key";

	private static final String	CN_OFFER_ID					= "offer_id";
	private static final String	CN_OFFER_WEIGHT				= "offer_weight";
	private static final String	CN_OFFER_DESCRIPTION		= "offer_description";

	private static final String	CN_OFFER_PRICE				= "offer_price";
	private static final String	CN_EXP_EXPEND_FOR			= "offer_expected_expenditure_for";
	private static final String	CN_EXP_EXPEND_PRICE			= "offer_expected_expenditure_price";
	private static final String	CN_DISC_MONEY_VALUE			= "offer_discount_money_value";
	private static final String	CN_DISC_MONEY_PERCENTAGE	= "offer_discount_money_percentage";
	private static final String	CN_MIN_PURCHASE_VALUE		= "offer_min_purchase_value";

	private static final String	CN_OFFER_START_DATE			= "offer_start_date";
	private static final String	CN_OFFER_END_DATE			= "offer_end_date";
	private static final String	CN_OFFER_START_TIME			= "offer_start_time";
	private static final String	CN_OFFER_REDEM_END_DATE		= "offer_redem_end_date";
	private static final String	CN_OFFER_REDEM_START_TIME	= "offer_redem_start_time";
	private static final String	CN_OFFER_REDEM_END_TIME		= "offer_redem_end_time";
	private static final String	CN_OFFER_EXPIRY_TIMESTAMP	= "offer_expiry_timestamp";
	private static final String	CN_OFFER_VALID_DAYS			= "offer_valid_days";

	private static final String	CN_QUANTITY					= "quantity";
	private static final String	CN_VOUCHER_COUNT			= "voucher_count";
	private static final String	CN_VOUCHER_CODES			= "voucher_codes";

	public static final String	TN_MY_OFFERS				= "my_offers_table";

	public static final String	CREATE_MY_OFFERS_TABLE;

	static {
		String createTable = "create table " + TN_MY_OFFERS + " ( ";
		String c1 = CN_PURCHASE_KEY + " integer, ";
		String c2 = CN_OFFER_ID + " integer, ";
		String c3 = CN_OFFER_WEIGHT + " integer, ";
		String c4 = CN_OFFER_DESCRIPTION + " text, ";

		String c5 = CN_OFFER_PRICE + " real, ";
		String c6 = CN_EXP_EXPEND_FOR + " text, ";
		String c7 = CN_EXP_EXPEND_PRICE + " integer, ";
		String c8 = CN_DISC_MONEY_VALUE + " real, ";
		String c9 = CN_DISC_MONEY_PERCENTAGE + " real, ";
		String c10 = CN_MIN_PURCHASE_VALUE + " text, ";

		String c11 = CN_OFFER_START_DATE + " integer, ";
		String c12 = CN_OFFER_END_DATE + " text, ";
		String c13 = CN_OFFER_START_TIME + " text, ";
		String c14 = CN_OFFER_REDEM_END_DATE + " text, ";
		String c15 = CN_OFFER_REDEM_START_TIME + " text, ";
		String c16 = CN_OFFER_REDEM_END_TIME + " text, ";
		String c17 = CN_OFFER_EXPIRY_TIMESTAMP + " text, ";
		String c18 = CN_OFFER_VALID_DAYS + " text, ";
		String c19 = CN_QUANTITY + " integer, ";
		String c20 = CN_VOUCHER_COUNT + " integer, ";
		String c21 = CN_VOUCHER_CODES + " text )";

		CREATE_MY_OFFERS_TABLE = createTable + c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9 + c10 + c11 + c12 + c13 + c14 + c15 + c16 + c17 + c18 + c19 + c20 + c21;
	}

	/**
	 * @param pApplication
	 */
	public MyOffersTable(Application pApplication) {
		super(pApplication, TN_MY_OFFERS);
	}

	@Override
	protected ArrayList<BaseModel> getAllData(String pSelection, String[] pSelectionArgs) {
		ArrayList<BaseModel> _offersList = new ArrayList<BaseModel>();
		Cursor _cursor = null;
		try {
			// query(table, columns, selection, selectionArgs, groupBy, having,
			// orderBy)
			_cursor = mWritableDatabase.query(TN_MY_OFFERS, null, pSelection, pSelectionArgs, null, null, null);

			while (_cursor.moveToNext()) {
				OfferModel _model = new OfferModel();
				_model.setPurchase_key(_cursor.getInt(_cursor.getColumnIndex(CN_PURCHASE_KEY)));
				_model.setOfferId(_cursor.getInt(_cursor.getColumnIndex(CN_OFFER_ID)));
				_model.setOffer_weight(_cursor.getInt(_cursor.getColumnIndex(CN_OFFER_WEIGHT)));
				_model.setOfferDes(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_DESCRIPTION)));
				_model.setOffer_price(_cursor.getFloat(_cursor.getColumnIndex(CN_OFFER_PRICE)));
				_model.setOffer_expected_expenditure_for(_cursor.getString(_cursor.getColumnIndex(CN_EXP_EXPEND_FOR)));
				_model.setOffer_expected_expenditure_price(_cursor.getInt(_cursor.getColumnIndex(CN_EXP_EXPEND_PRICE)));
				_model.setOffer_discount_money_value(_cursor.getFloat(_cursor.getColumnIndex(CN_DISC_MONEY_VALUE)));
				_model.setOffer_discount_money_percentage(_cursor.getFloat(_cursor.getColumnIndex(CN_DISC_MONEY_PERCENTAGE)));
				_model.setOffer_min_purchase_value(_cursor.getString(_cursor.getColumnIndex(CN_MIN_PURCHASE_VALUE)));
				_model.setOffer_start_date(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_START_DATE)));
				_model.setOffer_end_date(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_END_DATE)));
				_model.setOffer_start_time(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_START_TIME)));
				_model.setOffer_redem_end_date(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_REDEM_END_DATE)));
				_model.setOffer_redem_start_time(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_REDEM_START_TIME)));
				_model.setOffer_redem_end_time(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_REDEM_END_TIME)));
				_model.setOffer_redem_timestamp(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_EXPIRY_TIMESTAMP)));
				_model.setOffer_valid_days(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_VALID_DAYS)));
				_model.setQuantity(_cursor.getInt(_cursor.getColumnIndex(CN_QUANTITY)));
				_model.setVoucher_count(_cursor.getInt(_cursor.getColumnIndex(CN_VOUCHER_COUNT)));
				_model.setVoucher_codes(_cursor.getString(_cursor.getColumnIndex(CN_VOUCHER_CODES)));
				_offersList.add(_model);
			}
		} catch (Exception e) {
			Log.e(TN_MY_OFFERS, "getAllData()", e);
		} finally {
			closeCursor(_cursor);
		}
		return _offersList;
	}

	/**
	 * Prepare values to be inserted/updated in db.
	 * 
	 * @param pModel
	 * @param onlyUpdates
	 * @return
	 */
	@Override
	protected ContentValues getContentValues(BaseModel pModel, boolean onlyUpdates) {
		OfferModel _model = (OfferModel) pModel;
		ContentValues contentValues = new ContentValues();

		contentValues.put(CN_PURCHASE_KEY, _model.getPurchase_key());
		contentValues.put(CN_OFFER_ID, _model.getOfferId());
		contentValues.put(CN_OFFER_WEIGHT, _model.getOffer_weight());
		contentValues.put(CN_OFFER_DESCRIPTION, _model.getOfferDes());

		contentValues.put(CN_OFFER_PRICE, _model.getOffer_price());
		contentValues.put(CN_EXP_EXPEND_FOR, _model.getOffer_expected_expenditure_for());
		contentValues.put(CN_EXP_EXPEND_PRICE, _model.getOffer_expected_expenditure_price());
		contentValues.put(CN_DISC_MONEY_VALUE, _model.getOffer_discount_money_value());
		contentValues.put(CN_DISC_MONEY_PERCENTAGE, _model.getOffer_discount_money_percentage());
		contentValues.put(CN_MIN_PURCHASE_VALUE, _model.getOffer_min_purchase_value());

		contentValues.put(CN_OFFER_START_DATE, _model.getOffer_start_date());
		contentValues.put(CN_OFFER_END_DATE, _model.getOffer_end_date());
		contentValues.put(CN_OFFER_START_TIME, _model.getOffer_start_time());
		contentValues.put(CN_OFFER_REDEM_END_DATE, _model.getOffer_redem_end_date());
		contentValues.put(CN_OFFER_REDEM_START_TIME, _model.getOffer_redem_start_time());
		contentValues.put(CN_OFFER_REDEM_END_TIME, _model.getOffer_redem_end_time());
		contentValues.put(CN_OFFER_EXPIRY_TIMESTAMP, _model.getOffer_redem_timestamp());
		contentValues.put(CN_OFFER_VALID_DAYS, _model.getOffer_valid_days());

		contentValues.put(CN_QUANTITY, _model.getQuantity());
		contentValues.put(CN_VOUCHER_COUNT, _model.getVoucher_count());
		contentValues.put(CN_VOUCHER_CODES, _model.getVoucher_codes());

		return contentValues;
	}
}
