package com.groupon.go.model;

/**
 * @author sachin.gupta
 */
public class DealsListRequest {

	private int				dealType;
	private int				categoryId;
	private String			searchText;
	private int				couponType;

	private int				pageNo;
	private int				pageCount;

	private int				requestType;

	public static final int	REQUEST_NONE			= 0;
	public static final int	REQUEST_FIRST_PAGE		= 1;
	public static final int	REQUEST_ON_CITY_CHANGED	= 2;
	public static final int	REQUEST_NEXT_PAGE		= 3;
	public static final int	REQUEST_REFRESH_PAGES	= 4;
	public static final int	REQUEST_IF_NO_DEALS		= 5;

	/**
	 * no-arg constructor
	 */
	public DealsListRequest() {
		// no-arg constructor
	}

	/**
	 * @param pDealsRequestParams
	 */
	public DealsListRequest(DealsListRequest pDealsRequestParams) {
		this.dealType = pDealsRequestParams.dealType;
		this.categoryId = pDealsRequestParams.categoryId;
		this.searchText = pDealsRequestParams.searchText;
		this.couponType = pDealsRequestParams.couponType;
	}

	/**
	 * @return the dealType
	 */
	public int getDealType() {
		return dealType;
	}

	/**
	 * @param dealType
	 *            the dealType to set
	 */
	public void setDealType(int dealType) {
		this.dealType = dealType;
	}

	/**
	 * @return the categoryId
	 */
	public int getCategoryId() {
		return categoryId;
	}

	/**
	 * @param categoryId
	 *            the categoryId to set
	 */
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * @return the searchText
	 */
	public String getSearchText() {
		return searchText;
	}

	/**
	 * @param searchText
	 *            the searchText to set
	 */
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	/**
	 * @return the couponType
	 */
	public int getCouponType() {
		return couponType;
	}

	/**
	 * @param couponType
	 *            the couponType to set
	 */
	public void setCouponType(int couponType) {
		this.couponType = couponType;
	}

	/**
	 * @return the pageNo
	 */
	public int getPageNo() {
		return pageNo;
	}

	/**
	 * @param pageNo
	 *            the pageNo to set
	 */
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	/**
	 * @return the pageCount
	 */
	public int getPageCount() {
		return pageCount;
	}

	/**
	 * @param pageCount
	 *            the pageCount to set
	 */
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	/**
	 * @return the requestType
	 */
	public int getRequestType() {
		return requestType;
	}

	/**
	 * @param requestType
	 *            the requestType to set
	 */
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}
}
