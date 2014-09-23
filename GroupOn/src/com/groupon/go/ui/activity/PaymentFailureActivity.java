package com.groupon.go.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.groupon.go.R;

/**
 * @author vineet.rajpoot
 */
public class PaymentFailureActivity extends GroupOnBaseActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_failure);

		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getString(R.string.header_payment_summary));

		findViewById(R.id.txv_try_again).setOnClickListener(this);
		findViewById(R.id.txv_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txv_try_again: {
			finish();
			break;
		}
		case R.id.txv_cancel: {
			Intent intent = new Intent(this, UserCart.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		}
		}
	}
}
