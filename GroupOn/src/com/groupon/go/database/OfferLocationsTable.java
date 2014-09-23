package com.groupon.go.database;

import java.util.ArrayList;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.groupon.go.model.DealDetailResponse.OfferLocations;
import com.kelltontech.database.BaseTable;
import com.kelltontech.model.BaseModel;

/**
 * @author sachin.gupta
 */
public class OfferLocationsTable extends BaseTable {

	private static final String	CN_OFFER_ID			= "offer_id";
	private static final String	CN_ADDRESS_ID		= "address_id";
	private static final String	CN_MERCHANT_ADDRESS	= "merchant_address";
	private static final String	CN_OFFER_LOCATION	= "offer_location";

	public static final String	TN_OFFER_LOCATIONS	= "offer_locations_table";

	public static final String	CREATE_OFFER_LOCATIONS_TABLE;

	static {
		String createTable = "create table " + TN_OFFER_LOCATIONS + " ( ";
		String c1 = CN_ADDRESS_ID + " integer, ";
		String c2 = CN_OFFER_ID + " integer, ";
		String c3 = CN_MERCHANT_ADDRESS + " text, ";
		String c4 = CN_OFFER_LOCATION + " text )";
		CREATE_OFFER_LOCATIONS_TABLE = createTable + c1 + c2 + c3 + c4;
	}

	/**
	 * @param pApplication
	 */
	public OfferLocationsTable(Application pApplication) {
		super(pApplication, TN_OFFER_LOCATIONS);
	}

	@Override
	protected ArrayList<BaseModel> getAllData(String pSelection, String[] pSelectionArgs) {
		ArrayList<BaseModel> _offersList = new ArrayList<BaseModel>();
		Cursor _cursor = null;
		try {
			// query(table, columns, selection, selectionArgs, groupBy, having,
			// orderBy)
			_cursor = mWritableDatabase.query(TN_OFFER_LOCATIONS, null, pSelection, pSelectionArgs, null, null, null);

			while (_cursor.moveToNext()) {
				OfferLocations _model = new OfferLocations();
				_model.setOffer_id(_cursor.getInt(_cursor.getColumnIndex(CN_OFFER_ID)));
				_model.setMerchant_address(_cursor.getString(_cursor.getColumnIndex(CN_MERCHANT_ADDRESS)));
				_model.setOffer_location(_cursor.getString(_cursor.getColumnIndex(CN_OFFER_LOCATION)));
				_offersList.add(_model);
			}
		} catch (Exception e) {
			Log.e(TN_OFFER_LOCATIONS, "getAllData()", e);
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
		OfferLocations _model = (OfferLocations) pModel;
		ContentValues contentValues = new ContentValues();

		contentValues.put(CN_OFFER_ID, _model.getOffer_id());
		contentValues.put(CN_MERCHANT_ADDRESS, _model.getMerchant_address());
		contentValues.put(CN_OFFER_LOCATION, _model.getOffer_location());

		return contentValues;
	}
}
