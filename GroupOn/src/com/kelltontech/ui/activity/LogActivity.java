package com.kelltontech.ui.activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.ShareUtils;

/**
 * @author sachin.gupta
 */
public class LogActivity extends BaseActivity implements OnClickListener {

	private String		mLogsStr;
	private TextView	mTxvLogs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().hide();

		Button btnShareLog = new Button(this);
		btnShareLog.setText("Share Logs");
		btnShareLog.setOnClickListener(this);

		ScrollView scrollView = new ScrollView(this);
		mTxvLogs = new TextView(this);
		mTxvLogs.setTextColor(Color.BLACK);
		scrollView.addView(mTxvLogs);

		LinearLayout rootLayout = new LinearLayout(this);
		rootLayout.setBackgroundColor(Color.WHITE);
		rootLayout.setOrientation(LinearLayout.VERTICAL);
		rootLayout.addView(btnShareLog);
		rootLayout.addView(scrollView);
		setContentView(rootLayout);

		new CollectLogTasks().execute();
	}

	@Override
	public void onClick(View v) {
		ShareUtils.sharePlainText(this, "Share Logs via", "Logs", mLogsStr);
	}

	/**
	 * @author sachin.gupta
	 */
	private class CollectLogTasks extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			showProgressDialog("Collecting Logs...");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			StringBuilder logsBuilder = new StringBuilder();
			try {
				Process process = Runtime.getRuntime().exec("logcat -d");
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String logLine;
				while ((logLine = bufferedReader.readLine()) != null) {
					logsBuilder.append(logLine).append("\n");
				}
				bufferedReader.close();
				process.destroy();
				mLogsStr = logsBuilder.toString();
			} catch (Exception e) {
				mLogsStr = logsBuilder.toString() + "\n" + e.getLocalizedMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mTxvLogs.setText(mLogsStr);
			removeProgressDialog();
		}
	}

	@Override
	public void onEvent(int eventId, Object eventData) {
		// nothing to do here
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		// nothing to do here
	}
}