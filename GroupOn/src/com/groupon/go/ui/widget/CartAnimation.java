package com.groupon.go.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.groupon.go.R;

public class CartAnimation extends View {

	int					SLEEP_TIME			= 2;

	float				MOVE_DELTA_TOP		= 1.7f;
	float				MOVE_DELTA_RIGHT	= 1.75f;

	Bitmap				bitmap;
	Path				path				= new Path();
	IRemoveAnimation	iRemoveAnimation;
	boolean				IS_RUNNING;
	int					RIGHT_END, TOP_END;
	// initial x1
	float				xposition			= 0;
	// final x2
	float				devicewidth;

	// initial y1
	float				deviceheight;
	// final y2
	float				yposition;

	float				slopeM;
	double				slopeX;
	double				slopeY;
	double				disctanceDiagonal;

	public CartAnimation(Context context) {

		super(context);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_add_to_cart);
		// ypostion = (int)
		// (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350,
		// getResources().getDisplayMetrics()));
		xposition = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
		RIGHT_END = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, getResources().getDisplayMetrics()));
		Thread thread = new Thread(runnable);
		IS_RUNNING = true;
		thread.start();

	}

	public CartAnimation(Context context, double top) {

		super(context);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		devicewidth = metrics.widthPixels;
		deviceheight = metrics.heightPixels;
		Log.i("vipin", "devicewidth=" + devicewidth + "deviceheight =" + deviceheight);

		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_add_to_cart);
		this.yposition = (int) top;
		xposition = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()));
		RIGHT_END = (int) (yposition - (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())));

		// (y2-y1)/(x2-x1)
		slopeM = (yposition - deviceheight) / (devicewidth - xposition);

		disctanceDiagonal = Math.sqrt(Math.pow(Double.valueOf((yposition - deviceheight)), 2) + Math.pow(Double.valueOf((devicewidth - xposition)), 2));

		slopeM = Math.abs(slopeM);

		slopeX = disctanceDiagonal / devicewidth;
		slopeY = disctanceDiagonal / deviceheight;
		// Log.i("vipin", "slopeX=" + slopeX + "slopeY =" + slopeY);

		Thread thread = new Thread(runnable);
		IS_RUNNING = true;
		thread.start();

	}

	public void setRemoveAnimationCallBack(IRemoveAnimation iRemoveAnimation) {
		this.iRemoveAnimation = iRemoveAnimation;
	}

	public CartAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_add_to_cart);
		yposition = getHeight() / 2;
	}

	public CartAnimation(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_add_to_cart);
		yposition = getHeight() / 2;
	}

	protected void onDraw(android.graphics.Canvas canvas) {
		canvas.drawBitmap(bitmap, xposition, yposition, null);

	};

	Runnable	runnable	= new Runnable() {

								@SuppressLint("NewApi")
								@Override
								public void run() {

									while (IS_RUNNING) {
										postInvalidate();

										xposition = (float) (xposition + slopeY + .1f);
										yposition = (float) (yposition - slopeX - .1f);
										// Log.i("vipin", "x = " + xposition +
										// " y =" + yposition);
										try {
											Thread.sleep(SLEEP_TIME);

										} catch (InterruptedException e) {

										}
										postInvalidate();
										if ((xposition >= devicewidth) || yposition < 0) {
											iRemoveAnimation.onFinish();
											IS_RUNNING = false;
											postInvalidate();
										}
									}

								}
							};

}
