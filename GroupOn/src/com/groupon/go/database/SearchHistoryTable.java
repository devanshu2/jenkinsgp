package com.groupon.go.database;

import java.util.ArrayList;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.groupon.go.model.SearchItemModel;
import com.kelltontech.database.BaseTable;
import com.kelltontech.model.BaseModel;
import com.kelltontech.utils.StringUtils;

/**
 * @author sachin.gupta
 */
public class SearchHistoryTable extends BaseTable {

	private static final String	CN_SEARCHED_TEXT		= "searched_text";
	private static final String	CN_CATEGORY_ID			= "category_id";
	private static final String	CN_CATEGORY_NAME		= "category_name";

	private static final String	CN_SEARCHED_AT_MILLIS	= "searched_at_millis";
	private static final String	SORT_ORDER				= CN_SEARCHED_AT_MILLIS + " DESC";

	public static final String	TN_SEARCH_HISTORY		= "search_history_table";

	public static final String	CREATE_SEARCH_HISTORY_TABLE;

	static {
		String createTable = "create table " + TN_SEARCH_HISTORY + " ( ";
		String c1 = CN_PRIMARY_KEY + " integer primary key autoincrement, ";
		String c2 = CN_SEARCHED_TEXT + " text not null, ";
		String c3 = CN_CATEGORY_ID + " integer, ";
		String c4 = CN_CATEGORY_NAME + " text, ";
		String c5 = CN_SEARCHED_AT_MILLIS + " integer not null )";
		CREATE_SEARCH_HISTORY_TABLE = createTable + c1 + c2 + c3 + c4 + c5;
	}

	/**
	 * @param pApplication
	 */
	public SearchHistoryTable(Application pApplication) {
		super(pApplication, TN_SEARCH_HISTORY);
	}

	@Override
	protected ArrayList<BaseModel> getAllData(String pSelection, String[] pSelectionArgs) {
		ArrayList<BaseModel> _postsList = new ArrayList<BaseModel>();
		Cursor _cursor = null;
		try {
			// query(table, columns, selection, selectionArgs, groupBy, having,
			// orderBy)
			_cursor = mWritableDatabase.query(TN_SEARCH_HISTORY, null, pSelection, pSelectionArgs, null, null, SORT_ORDER);

			while (_cursor.moveToNext()) {
				SearchItemModel _model = new SearchItemModel();
				_model.setPrimaryKey(_cursor.getInt(_cursor.getColumnIndex(CN_PRIMARY_KEY)));
				_model.setSearchText(_cursor.getString(_cursor.getColumnIndex(CN_SEARCHED_TEXT)));
				_model.setCat_id(_cursor.getInt(_cursor.getColumnIndex(CN_CATEGORY_ID)));
				_model.setCat_name(_cursor.getString(_cursor.getColumnIndex(CN_CATEGORY_NAME)));
				_postsList.add(_model);
			}
		} catch (Exception e) {
			Log.e(TN_SEARCH_HISTORY, "getAllData", e);
		} finally {
			closeCursor(_cursor);
		}
		return _postsList;
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
		SearchItemModel searchItemModel = (SearchItemModel) pModel;
		ContentValues contentValues = new ContentValues();
		contentValues.put(CN_SEARCHED_AT_MILLIS, System.currentTimeMillis());
		contentValues.put(CN_SEARCHED_TEXT, searchItemModel.getSearchText());
		if (!onlyUpdates) {
			contentValues.put(CN_CATEGORY_ID, searchItemModel.getCat_id());
			contentValues.put(CN_CATEGORY_NAME, searchItemModel.getCat_name());
		}
		return contentValues;
	}

	@Override
	protected BaseModel getMatchingData(BaseModel pModel) {
		SearchItemModel searchItemModel = null;
		String searchText = null;
		if (pModel instanceof SearchItemModel) {
			searchItemModel = (SearchItemModel) pModel;
			searchText = searchItemModel.getSearchText();
		}
		if (StringUtils.isNullOrEmpty(searchText)) {
			return null;
		}
		String whereClause = CN_SEARCHED_TEXT + " = ? and " + CN_CATEGORY_ID + " = ? COLLATE NOCASE";
		String[] whereArgs = { searchText, "" + searchItemModel.getCat_id() };
		ArrayList<BaseModel> matchingDataList = getAllData(whereClause, whereArgs);
		if (matchingDataList != null && matchingDataList.size() > 0) {
			return matchingDataList.get(0);
		} else {
			return null;
		}
	}
}
