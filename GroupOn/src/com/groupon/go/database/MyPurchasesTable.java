package com.groupon.go.database;

import java.util.ArrayList;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.groupon.go.model.MyPurchaseModel;
import com.kelltontech.database.BaseTable;
import com.kelltontech.model.BaseModel;

/**
 * @author sachin.gupta
 */
public class MyPurchasesTable extends BaseTable {

	private static final String	CN_ORDER_ID				= "order_id";
	private static final String	CN_DEAL_ID				= "deal_id";
	private static final String	CN_MERCHANT_ID			= "merchant_id";

	private static final String	CN_OPPORTUNITY_OWNER	= "opportunity_owner";
	private static final String	CN_CATEGORY_NAME		= "category_name";

	private static final String	CN_DEAL_TITLE			= "deal_title";
	private static final String	CN_DEAL_HIGHLIGHTS		= "deal_highlights";
	private static final String	CN_DEAL_FINEPRINT		= "deal_fineprint";
	private static final String	CN_DEAL_IMAGES			= "deal_images";

	public static final String	TN_MY_PURCHASES			= "my_purchases_table";

	public static final String	CREATE_MY_PURCHASES_TABLE;

	static {
		String createTable = "create table " + TN_MY_PURCHASES + " ( ";
		String c1 = CN_PRIMARY_KEY + " integer, ";
		String c2 = CN_ORDER_ID + " integer, ";
		String c3 = CN_DEAL_ID + " integer, ";
		String c4 = CN_MERCHANT_ID + " integer, ";
		String c5 = CN_OPPORTUNITY_OWNER + " integer, ";
		String c6 = CN_CATEGORY_NAME + " text, ";
		String c7 = CN_DEAL_TITLE + " text, ";
		String c8 = CN_DEAL_HIGHLIGHTS + " text, ";
		String c9 = CN_DEAL_FINEPRINT + " real, ";
		String c10 = CN_DEAL_IMAGES + " text )";
		CREATE_MY_PURCHASES_TABLE = createTable + c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9 + c10;
	}

	/**
	 * @param pApplication
	 */
	public MyPurchasesTable(Application pApplication) {
		super(pApplication, TN_MY_PURCHASES);
	}

	@Override
	protected ArrayList<BaseModel> getAllData(String pSelection, String[] pSelectionArgs) {
		ArrayList<BaseModel> _offersList = new ArrayList<BaseModel>();
		Cursor _cursor = null;
		try {
			// query(table, columns, selection, selectionArgs, groupBy, having,
			// orderBy)
			_cursor = mWritableDatabase.query(TN_MY_PURCHASES, null, pSelection, pSelectionArgs, null, null, null);

			while (_cursor.moveToNext()) {
				MyPurchaseModel _model = new MyPurchaseModel();
				_model.setPrimaryKey(_cursor.getInt(_cursor.getColumnIndex(CN_PRIMARY_KEY)));
				_model.setOrder_id(_cursor.getInt(_cursor.getColumnIndex(CN_ORDER_ID)));
				_model.setDeal_id(_cursor.getInt(_cursor.getColumnIndex(CN_DEAL_ID)));
				_model.setMerchant_id(_cursor.getInt(_cursor.getColumnIndex(CN_MERCHANT_ID)));
				_model.setOpportunity_owner(_cursor.getString(_cursor.getColumnIndex(CN_OPPORTUNITY_OWNER)));
				_model.setCat_name(_cursor.getString(_cursor.getColumnIndex(CN_CATEGORY_NAME)));
				_model.setDeal_title(_cursor.getString(_cursor.getColumnIndex(CN_DEAL_TITLE)));
				_model.setDeal_highlight(_cursor.getString(_cursor.getColumnIndex(CN_DEAL_HIGHLIGHTS)));
				_model.setDeal_fineprint(_cursor.getString(_cursor.getColumnIndex(CN_DEAL_FINEPRINT)));
				ArrayList<String> dealImages = new ArrayList<String>();
				dealImages.add(_cursor.getString(_cursor.getColumnIndex(CN_DEAL_IMAGES)));
				_model.setDeal_image(dealImages);
				_offersList.add(_model);
			}
		} catch (Exception e) {
			Log.e(TN_MY_PURCHASES, "getAllData()", e);
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
		MyPurchaseModel _model = (MyPurchaseModel) pModel;
		ContentValues contentValues = new ContentValues();

		contentValues.put(CN_PRIMARY_KEY, _model.getPrimaryKey());
		contentValues.put(CN_ORDER_ID, _model.getOrder_id());
		contentValues.put(CN_DEAL_ID, _model.getDeal_id());
		contentValues.put(CN_MERCHANT_ID, _model.getMerchant_id());
		contentValues.put(CN_OPPORTUNITY_OWNER, _model.getOpportunity_owner());
		contentValues.put(CN_CATEGORY_NAME, _model.getCat_name());
		contentValues.put(CN_DEAL_TITLE, _model.getDeal_title());
		contentValues.put(CN_DEAL_HIGHLIGHTS, _model.getDeal_highlight());
		contentValues.put(CN_DEAL_FINEPRINT, _model.getDeal_fineprint());
		if (_model.getDeal_image() != null && !_model.getDeal_image().isEmpty()) {
			contentValues.put(CN_DEAL_IMAGES, _model.getDeal_image().get(0));
		}

		return contentValues;
	}

	/**
	 * @return next primary key, 1 in case of first row.
	 */
	public final int getNextPrimaryKey() {
		String columnName = "maxPrimaryKey";
		String query = "select max(" + CN_PRIMARY_KEY + ") as " + columnName + "  from " + mTableName;
		int maxPrimaryKey = 0;
		Cursor cursor = null;
		try {
			cursor = mWritableDatabase.rawQuery(query, null);
			if (cursor.moveToNext()) {
				maxPrimaryKey = cursor.getInt(cursor.getColumnIndex(columnName));
			}
		} catch (Exception e) {
			Log.e(mTableName, "getNextPrimaryKey()", e);
		} finally {
			closeCursor(cursor);
		}
		return maxPrimaryKey + 1;
	}
}
