package com.android.grinnellmenu;

import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

public class MenuCalendar extends Activity {

	public static final String DATEKEY = "date";
	public static final int RESULTOK = 1;
	public static final int TODAY = 0;
	public static final int TOMORROW = 1;

	Button mButtonToday, mButtonTomorrow, mButtonGo;
	private DatePicker mDp;
	private int mInitialDay;
	
	private Button mBreakfast;
	private Button mLunch;
	private Button mDinner;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar);

		
		mButtonToday = (Button) findViewById(R.id.buttonToday);
		mButtonTomorrow = (Button) findViewById(R.id.buttonTomorrow);
		mButtonGo = (Button) findViewById(R.id.buttonGo);
		 
		
		mBreakfast = (Button) findViewById(R.id.buttonBreakfast);
		mLunch = (Button) findViewById(R.id.buttonLunch);
		mDinner = (Button) findViewById(R.id.buttonDinner);
		
		mDp = (DatePicker) findViewById(R.id.datePicker);
		
		Intent i = this.getIntent();
		i.getExtras();
		
		/*
		 * GregorianCalendar max = new GregorianCalendar(
		 * today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 7,
		 * today.get(Calendar.DAY_OF_MONTH));
		 */
		// mDp.setMaxDate(max.getTimeInMillis());
		// mDp.setMinDate(today.getTimeInMillis());
		mInitialDay = mDp.getDayOfMonth();

	}

	public void onButtonPress(View v) {
		Bundle b = new Bundle();

		switch (v.getId()) {
		case R.id.buttonToday:
			b.putInt(DATEKEY, TODAY);
			break;
		case R.id.buttonTomorrow:
			b.putInt(DATEKEY, TOMORROW);
			break;
		case R.id.buttonGo:
			b.putInt(DATEKEY, (mDp.getDayOfMonth() - mInitialDay));
			break;
		}

		Intent i = new Intent();
		i.putExtras(b);
		setResult(RESULTOK, i);
		finish();
	}

}
