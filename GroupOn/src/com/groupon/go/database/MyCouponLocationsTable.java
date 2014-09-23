package com.groupon.go.database;

import java.util.ArrayList;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.groupon.go.model.AllOrdersResponse.AllOrdersResult;
import com.groupon.go.model.MyOfferLocation;
import com.kelltontech.database.BaseTable;
import com.kelltontech.model.BaseModel;
import com.kelltontech.utils.DateTimeUtils;

/**
 * @author sachin.gupta
 */
public class MyCouponLocationsTable extends BaseTable {

	private static final String	CN_ORDER_ID				= "order_id";
	private static final String	CN_DEAL_ID				= "deal_id";
	private static final String	CN_OFFER_ID				= "offer_id";
	private static final String	CN_MERCHANT_ID			= "merchant_id";
	private static final String	CN_MERCHANT_NAME		= "merchant_name";
	private static final String	CN_ADDRESS_ID			= "address_id";
	private static final String	CN_LATITUDE				= "latitude";
	private static final String	CN_LONGITUDE			= "longitude";
	private static final String	CN_VOUCHERS				= "vouchers";
	private static final String	CN_LAST_NOTIFIED_AT		= "last_notified_at";

	public static final String	TN_MY_OFFER_LOCATIONS	= "my_offer_locations_table";

	public static final String	CREATE_MY_OFFER_LOCATIONS_TABLE;

	static {
		String createTable = "create table " + TN_MY_OFFER_LOCATIONS + " ( ";
		String c1 = CN_ORDER_ID + " integer, ";
		String c2 = CN_DEAL_ID + " integer, ";
		String c3 = CN_OFFER_ID + " integer, ";
		String c4 = CN_MERCHANT_ID + " integer, ";
		String c5 = CN_MERCHANT_NAME + " text, ";
		String c6 = CN_ADDRESS_ID + " integer, ";
		String c7 = CN_LATITUDE + " real, ";
		String c8 = CN_LONGITUDE + " real, ";
		String c9 = CN_VOUCHERS + " integer, ";
		String c10 = CN_LAST_NOTIFIED_AT + " integer default 0 )";
		CREATE_MY_OFFER_LOCATIONS_TABLE = createTable + c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9 + c10;
	}

	/**
	 * @param pApplication
	 */
	public MyCouponLocationsTable(Application pApplication) {
		super(pApplication, TN_MY_OFFER_LOCATIONS);
	}

	@Override
	protected ArrayList<BaseModel> getAllData(String pSelection, String[] pSelectionArgs) {
		ArrayList<BaseModel> _offersList = new ArrayList<BaseModel>();
		Cursor _cursor = null;
		try {
			// query(table, columns, selection, selectionArgs, groupBy, having,
			// orderBy)
			_cursor = mWritableDatabase.query(TN_MY_OFFER_LOCATIONS, null, pSelection, pSelectionArgs, null, null, null);

			while (_cursor.moveToNext()) {
				MyOfferLocation _model = new MyOfferLocation();
				_model.setOrder_id(_cursor.getInt(_cursor.getColumnIndex(CN_ORDER_ID)));
				_model.setDeal_id(_cursor.getInt(_cursor.getColumnIndex(CN_DEAL_ID)));
				_model.setOffer_id(_cursor.getInt(_cursor.getColumnIndex(CN_OFFER_ID)));
				_model.setMerchant_id(_cursor.getInt(_cursor.getColumnIndex(CN_MERCHANT_ID)));
				_model.setMerchant_name(_cursor.getString(_cursor.getColumnIndex(CN_MERCHANT_NAME)));
				_model.setAddress_id(_cursor.getInt(_cursor.getColumnIndex(CN_ADDRESS_ID)));
				_model.setLattitude(_cursor.getFloat(_cursor.getColumnIndex(CN_LATITUDE)));
				_model.setLongitude(_cursor.getFloat(_cursor.getColumnIndex(CN_LONGITUDE)));
				_model.setVouchers(_cursor.getInt(_cursor.getColumnIndex(CN_VOUCHERS)));
				_model.setLast_notified_at(_cursor.getLong(_cursor.getColumnIndex(CN_LAST_NOTIFIED_AT)));
				_offersList.add(_model);
			}
		} catch (Exception e) {
			Log.e(TN_MY_OFFER_LOCATIONS, "getAllData()", e);
		} finally {
			closeCursor(_cursor);
		}
		return _offersList;
	}

	/**
	 * @return list of all couponLocations for which user is either not notified
	 *         yet or notified on yesterday or before
	 */
	public ArrayList<BaseModel> getNotifiableCoupons() {
		String whereClause = CN_LAST_NOTIFIED_AT + " < ? ";
		String[] whereArgs = { "" + DateTimeUtils.getMidNightMillis(0) };
		return getAllData(whereClause, whereArgs);
	}

	/**
	 * @return list of all couponLocations from same merchant
	 */
	public ArrayList<BaseModel> getAllCoupons(int merchant_id) {
		String whereClause = CN_MERCHANT_ID + " = ? ";
		String[] whereArgs = { "" + merchant_id };
		return getAllData(whereClause, whereArgs);
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
		MyOfferLocation _model = (MyOfferLocation) pModel;
		ContentValues contentValues = new ContentValues();

		if (_model.getLast_notified_at() != 0) {
			contentValues.put(CN_LAST_NOTIFIED_AT, _model.getLast_notified_at());
			return contentValues;
		}

		contentValues.put(CN_MERCHANT_NAME, _model.getMerchant_name());
		contentValues.put(CN_LATITUDE, _model.getLatitude());
		contentValues.put(CN_LONGITUDE, _model.getLongitude());
		contentValues.put(CN_VOUCHERS, _model.getVouchers());

		if (onlyUpdates) {
			return contentValues;
		}

		contentValues.put(CN_ORDER_ID, _model.getOrder_id());
		contentValues.put(CN_DEAL_ID, _model.getDeal_id());
		contentValues.put(CN_OFFER_ID, _model.getOffer_id());
		contentValues.put(CN_MERCHANT_ID, _model.getMerchant_id());
		contentValues.put(CN_ADDRESS_ID, _model.getAddress_id());

		return contentValues;
	}

	/**
	 * @param pAllOrdersResult
	 */
	public void updateCoupons(AllOrdersResult pAllOrdersResult) {
		if (pAllOrdersResult.getAdded() != null) {
			for (MyOfferLocation model : pAllOrdersResult.getAdded()) {
				insertData(model);
			}
		}
		if (pAllOrdersResult.getUpdated() != null) {
			for (MyOfferLocation model : pAllOrdersResult.getUpdated()) {
				updateCoupon(model);
			}
		}
		if (pAllOrdersResult.getDeleted() != null) {
			String whereClause = CN_ORDER_ID + " = ? AND " + CN_DEAL_ID + " = ? AND " + CN_OFFER_ID + " = ? AND " + CN_ADDRESS_ID + " = ?";
			String[] whereArgs = null;
			for (MyOfferLocation model : pAllOrdersResult.getDeleted()) {
				whereArgs = new String[] { "" + model.getOrder_id(), "" + model.getDeal_id(), "" + model.getOffer_id(), "" + model.getAddress_id() };
				deleteData(whereClause, whereArgs);
			}
		}
	}

	/**
	 * @param pModel
	 * @return true if successfully updated
	 */
	public void updateCoupon(MyOfferLocation pModel) {
		String whereClause = CN_ORDER_ID + " = ? AND " + CN_DEAL_ID + " = ? AND " + CN_OFFER_ID + " = ? AND " + CN_ADDRESS_ID + " = ?";
		String[] whereArgs = { "" + pModel.getOrder_id(), "" + pModel.getDeal_id(), "" + pModel.getOffer_id(), "" + pModel.getAddress_id() };
		updateData(getContentValues(pModel, true), whereClause, whereArgs);
	}
}
