package com.android.grinnellmenu;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class GrinnellMenuActivity extends ListActivity {

	public static int GET_DATE = 1;
	GregorianCalendar mRequestedDate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClick());

		mRequestedDate = new GregorianCalendar();
		updateMenu();

	}

	private class OnItemClick implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			startActivityForResult(new Intent(view.getContext(),
					MenuItemDetails.class), position);
		}
	}

	@Override
	public void onBackPressed() {
		startActivityForResult(new Intent(this, MenuCalendar.class), GET_DATE);

		return;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (resultCode == MenuCalendar.RESULTOK) {
			Bundle b = intent.getExtras();
			int offset = b.getInt(MenuCalendar.DATEKEY);
			mRequestedDate.roll(Calendar.DAY_OF_MONTH, offset);
			updateMenu();
		}
	}

	void updateMenu() {

		return;
	}
}