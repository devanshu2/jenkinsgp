package com.kelltontech.database;

import com.groupon.go.database.MyCouponLocationsTable;
import com.groupon.go.database.MyOffersTable;
import com.groupon.go.database.MyPurchasesTable;
import com.groupon.go.database.OfferLocationsTable;
import com.groupon.go.database.SearchHistoryTable;

public interface DBManifest {

	/**
	 * Database name and version of the database.
	 */
	String		DATABASE_NAME				= "groupon_db";
	int			DATABASE_VERSION			= 1;

	/**
	 * Array of Table create queries...
	 */
	String[]	DB_SQL_CREATE_TABLE_QUERIES	= new String[] {
			SearchHistoryTable.CREATE_SEARCH_HISTORY_TABLE,
			MyCouponLocationsTable.CREATE_MY_OFFER_LOCATIONS_TABLE,
			MyPurchasesTable.CREATE_MY_PURCHASES_TABLE,
			MyOffersTable.CREATE_MY_OFFERS_TABLE,
			OfferLocationsTable.CREATE_OFFER_LOCATIONS_TABLE };
}
