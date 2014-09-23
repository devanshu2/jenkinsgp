package com.kelltontech.social;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

/**
 * @edited sachin.gupta
 */
public class TwitterDialog extends Dialog {

	private ProgressDialog		mWbVwPrgDlg;
	private boolean				mWbVwPrgDlgRunning	= false;

	private TwDialogListener	mTwDialogListener;
	private String				mUrl;

	/**
	 * @param context
	 * @param url
	 * @param listener
	 */
	public TwitterDialog(Context context, String url, TwDialogListener listener) {
		super(context);
		mUrl = url;
		mTwDialogListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		LinearLayout twDlgRootLayout = new LinearLayout(getContext());
		twDlgRootLayout.setOrientation(LinearLayout.VERTICAL);

		WebView webView = setUpWebView(twDlgRootLayout);

		DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
		LayoutParams lpFixedDimen = new LayoutParams(displayMetrics.widthPixels - 50, displayMetrics.heightPixels - 50);
		addContentView(twDlgRootLayout, lpFixedDimen);

		mWbVwPrgDlg = new ProgressDialog(getContext());
		mWbVwPrgDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mWbVwPrgDlg.setMessage("Processing...");
		mWbVwPrgDlg.setCancelable(false);

		webView.loadUrl(mUrl);
		webView.requestFocus();
	}

	/**
	 * @param pTwDlgRootLayout
	 */
	private WebView setUpWebView(LinearLayout pTwDlgRootLayout) {
		WebView webView = new WebView(getContext());

		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.setWebViewClient(new TwitterWebViewClient());

		webView.getSettings().setJavaScriptEnabled(true);
		CookieManager.getInstance().setAcceptCookie(false);

		webView.setFocusable(true);
		webView.setFocusableInTouchMode(true);

		LayoutParams lpMatchParent = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		webView.setLayoutParams(lpMatchParent);
		pTwDlgRootLayout.addView(webView);
		return webView;
	}

	/**
	 * @edited sachin.gupta
	 */
	private class TwitterWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith(TwitterApp.CALLBACK_URL)) {
				mTwDialogListener.onComplete(url);
				TwitterDialog.this.dismiss();
				return true;
			} else if (url.startsWith("authorize")) {
				return false;
			}
			return true;
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			mTwDialogListener.onError(description);
			TwitterDialog.this.dismiss();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			mWbVwPrgDlg.show();
			mWbVwPrgDlgRunning = true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			mWbVwPrgDlgRunning = false;
			mWbVwPrgDlg.dismiss();
		}
	}

	@Override
	protected void onStop() {
		mWbVwPrgDlgRunning = false;
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (!mWbVwPrgDlgRunning) {
			TwitterDialog.this.dismiss();
			mTwDialogListener.onBackPressed();
		}
	}

	/**
	 * @author sachin.gupta
	 */
	public interface TwDialogListener {
		public void onComplete(String value);

		public void onError(String value);

		public void onBackPressed();
	}
}
