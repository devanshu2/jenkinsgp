package com.kelltontech.database;

import java.util.ArrayList;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kelltontech.application.BaseApplication;
import com.kelltontech.model.BaseModel;

/**
 * base class with common DB methods to be used as super class for all tables
 * 
 * @author sachin.gupta
 */
public abstract class BaseTable {

	protected static final String	CN_PRIMARY_KEY	= "primary_key";

	protected SQLiteDatabase		mWritableDatabase;
	protected String				mTableName;

	/**
	 * Get the global instance of the SQLiteDatabase.
	 * 
	 * @param callerActivity
	 */
	public BaseTable(Application pApplication, String pTableName) {
		if (pApplication instanceof BaseApplication) {
			BaseApplication baseApplication = (BaseApplication) pApplication;
			mWritableDatabase = baseApplication.getWritableDbInstance();
		} else {
			throw new RuntimeException("BaseApplication implementation is wrong.");
		}
		mTableName = pTableName;
	}

	/**
	 * @param pModel
	 * @return the rowId of newly inserted row, DB_INVALID_ID in case of any
	 *         error
	 */
	public final boolean insertData(BaseModel pModel) {
		try {
			return mWritableDatabase.insert(mTableName, null, getContentValues(pModel, false)) > 0;
		} catch (Exception e) {
			Log.e(mTableName, "insertData()", e);
			return false;
		}
	}

	/**
	 * inserts or updates data by primary key
	 * 
	 * @param pModel
	 * @return
	 */
	public final boolean insertOrUpdate(BaseModel pModel) {
		BaseModel matchingModel = getMatchingData(pModel);
		if (matchingModel == null || matchingModel.getPrimaryKey() == 0) {
			return insertData(pModel);
		} else {
			pModel.setPrimaryKey(matchingModel.getPrimaryKey());
			return updateData(pModel) > 0;
		}
	}

	/**
	 * delete data by whereClause and whereArgs
	 * 
	 * @param pWhereClause
	 * @param pWhereArgs
	 * @return count of deleted rows
	 */
	protected final int deleteData(String pWhereClause, String[] pWhereArgs) {
		try {
			return mWritableDatabase.delete(mTableName, pWhereClause, pWhereArgs);
		} catch (Exception e) {
			Log.e(mTableName, "deleteData()", e);
			return 0;
		}
	}

	/**
	 * delete data by primary key
	 * 
	 * @param pPrimaryKey
	 * @return true if one or more rows are deleted
	 */
	public final boolean deleteData(int pPrimaryKey) {
		String whereClause = CN_PRIMARY_KEY + " = ?";
		String[] whereArgs = { "" + pPrimaryKey };
		return deleteData(whereClause, whereArgs) > 0;
	}

	/**
	 * update data by whereClause and whereArgs
	 * 
	 * @param contentValues
	 * @param whereClause
	 * @param whereArgs
	 * @return count of affected rows
	 */
	protected final int updateData(ContentValues contentValues, String whereClause, String[] whereArgs) {
		try {
			return mWritableDatabase.update(mTableName, contentValues, whereClause, whereArgs);
		} catch (Exception e) {
			Log.e(mTableName, "updateData()", e);
			return 0;
		}
	}

	/**
	 * update data by primary key
	 * 
	 * @param baseModel
	 * @return count of affected rows
	 */
	public final int updateData(BaseModel baseModel) {
		String whereClause = CN_PRIMARY_KEY + " = ?";
		String[] whereArgs = { "" + baseModel.getPrimaryKey() };
		return updateData(getContentValues(baseModel, true), whereClause, whereArgs);
	}

	/**
	 * @return array list of all data in table
	 */
	public final ArrayList<BaseModel> getAllData() {
		return getAllData(null, null);
	}

	/**
	 * @return the number of rows deleted
	 */
	public final int deleteAll() {
		return deleteData("1", null);
	}

	/**
	 * @return count of total rows in table, -1 in case of any exception.
	 */
	public final int getRowsCount() {
		String columnName = "rowsCount";
		String query = "select count(*) as " + columnName + "  from " + mTableName;
		int rowsCount = -1;
		Cursor cursor = null;
		try {
			cursor = mWritableDatabase.rawQuery(query, null);
			if (cursor.moveToNext()) {
				rowsCount = cursor.getInt(cursor.getColumnIndex(columnName));
			}
		} catch (Exception e) {
			Log.e(mTableName, "getRowsCount()", e);
		} finally {
			closeCursor(cursor);
		}
		return rowsCount;
	}

	/**
	 * Closes the pCursor.
	 * 
	 * @param pCursor
	 */
	protected final void closeCursor(Cursor pCursor) {
		if (pCursor != null && !pCursor.isClosed())
			pCursor.close();
	}

	/**
	 * Helper method to create content value from BaseModel
	 * 
	 * @param pModel
	 * @param onlyUpdates
	 * @return
	 */
	protected abstract ContentValues getContentValues(BaseModel pModel, boolean onlyUpdates);

	/**
	 * @param pSelection
	 * @param pSelectionArgs
	 * @return array list of data selected from table
	 */
	protected abstract ArrayList<BaseModel> getAllData(String pSelection, String[] pSelectionArgs);

	/**
	 * @param pModel
	 * @return
	 */
	protected BaseModel getMatchingData(BaseModel pModel) {
		throw new UnsupportedOperationException("Operation not implemented yet.");
	}
}
