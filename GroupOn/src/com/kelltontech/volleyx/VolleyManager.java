package com.kelltontech.volleyx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;

/**
 * This manager tracks different volley objects like image loader etc. <br/>
 * It also has an Implementation of volley's {@link ImageCache} interface. <br/>
 * 
 * @author sachin.gupta
 */
public class VolleyManager {

	/**
	 * Volley objects and Volley Helper objects
	 */
	private static RequestQueue	mDataRequestQueue;
	private static RequestQueue	mImageRequestQueue;
	private static ImageLoader	mImageLoader;
	private static String		mDefaultRequestTag;

	/**
	 * initialize instances of different volley objects
	 */
	public static void initialize(Context context) {
		// Data Requests
		mDefaultRequestTag = context.getPackageName();
		mDataRequestQueue = Volley.newRequestQueue(context);

		// Image Requests
		mImageRequestQueue = Volley.newRequestQueue(context);
		mImageLoader = new ImageLoader(mImageRequestQueue, new DiskCache(context));
	}

	/**
	 * @return instance of the image loader
	 * @throws IllegalStatException
	 *             if initialize has not yet been called
	 */
	public static ImageLoader getImageLoader() {
		if (mImageLoader == null) {
			throw new IllegalStateException("Not initialized");
		}
		return mImageLoader;
	}

	/**
	 * Executes and image load
	 * 
	 * @param url
	 *            location of image
	 * @param listener
	 *            Listener for completion
	 * @throws IllegalStatException
	 *             if initialize has not yet been called
	 */
	public static void getImage(String url, ImageListener listener) {
		if (mDataRequestQueue == null) {
			throw new IllegalStateException("Not initialized");
		}
		mImageLoader.get(url, listener);
	}

	/**
	 * Adds the specified request to the global queue using the Default TAG.
	 * 
	 * @param pRequest
	 * @throws IllegalStatException
	 *             if initialize has not yet been called
	 */
	public static <T> void addToDataRequestQueue(Request<T> pRequest) {
		if (mDataRequestQueue == null) {
			throw new IllegalStateException("Not initialized");
		}
		// set the default tag if tag is empty
		if (pRequest.getTag() == null) {
			pRequest.setTag(mDefaultRequestTag);
		}
		pRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 30, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		mDataRequestQueue.add(pRequest);
	}

	/**
	 * Cancels all pending requests by the specified TAG, it is important to
	 * specify a TAG so that the pending/ongoing requests can be cancelled.
	 * 
	 * @param pRequestTag
	 */
	public static void cancelPendingRequests(Object pRequestTag) {
		if (mDataRequestQueue != null) {
			if (pRequestTag == null) {
				pRequestTag = mDefaultRequestTag;
			}
			mDataRequestQueue.cancelAll(pRequestTag);
		}
	}

	/**
	 * Implementation of volley's {@link ImageCache} interface.
	 * 
	 * @author sachin.gupta
	 */
	private static class DiskCache implements ImageCache {

		private static DiskLruImageCache	mDiskLruImageCache;

		public DiskCache(Context context) {
			String cacheName = context.getPackageCodePath();
			int cacheSize = 1024 * 1024 * 10;
			mDiskLruImageCache = new DiskLruImageCache(context, cacheName, cacheSize, CompressFormat.PNG, 100);
		}

		@Override
		public Bitmap getBitmap(String pImageUrl) {
			try {
				return mDiskLruImageCache.getBitmap(createKey(pImageUrl));
			} catch (NullPointerException e) {
				throw new IllegalStateException("Disk Cache Not initialized");
			}
		}

		@Override
		public void putBitmap(String pImageUrl, Bitmap pBitmap) {
			try {
				mDiskLruImageCache.put(createKey(pImageUrl), pBitmap);
			} catch (NullPointerException e) {
				throw new IllegalStateException("Disk Cache Not initialized");
			}
		}

		/**
		 * Creates a unique cache key based on a url value
		 * 
		 * @param pImageUrl
		 *            url to be used in key creation
		 * @return cache key value
		 */
		private String createKey(String pImageUrl) {
			return String.valueOf(pImageUrl.hashCode());
		}
	}
}
