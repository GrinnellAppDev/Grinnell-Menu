package com.android.grinnellmenu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.ExpandableListView.OnChildClickListener;

public class GrinnellMenuActivity extends ExpandableListActivity {

	/* Request codes: */
	public static final int GET_DATE 			= 1;
	public static final int SET_DIETARY_PREFS 	= 2;
	
	
	/* Expandable List View Configuration Items */ 
	protected List<Map<String, String>> mGroupList;
	protected List<List<Map<String, String>>> mChildList;
	public static String VENUE = "venue";
	public static String ENTREE = "entree";
	
	/* Date of Menu to Retrieve */
	protected GregorianCalendar mRequestedDate;
	
	/* Dietary Dish Preferences */
	protected int mDishPrefs;
	/* Dietary Dish Flags */
	public static final int ALL 	= 0x00000000;
	public static final int VEGAN 	= 0x00000001;
	public static final int OL    	= 0x00000002;
	


	private ExpandableListAdapter mSELAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
 
		/* Assume user will want to view all dishes initially: */
		mDishPrefs &= ALL;
	
		
		/* Initialize the ExpandableListView. */
		
		Resources r = getResources();
		//Setup the GroupList:
		mGroupList = new ArrayList<Map<String, String>>();
		String[] venues = r.getStringArray(R.array.venues);
		for (int i = 0; i < venues.length; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(VENUE, venues[i]);
			mGroupList.add(map);			
		}
		
		//Setup the ChildLists to be empty at first since we have not read data yet:
		mChildList = new ArrayList<List<Map<String, String>>>(); 
		//--temporary default children values --
		String empty = r.getString(R.string.empty);
		for (int i = 0; i < venues.length; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(ENTREE, empty);
			List<Map<String, String>> entrees = new ArrayList<Map<String, String>>();
			entrees.add(map);
			mChildList.add(entrees);
		}
		
		//-- --
		//Setup the entrees:
		populateEntrees(); // TODO: do this in updateMenu() instead
		
		//Setup the adapter:
		mSELAdapter = new SimpleExpandableListAdapter(
				this,
				mGroupList,
				R.layout.venue_field,
				new String[] { VENUE },
				new int[] { R.id.venueBox },
				mChildList,
				R.layout.entree_field,
				new String[] { ENTREE },
				new int[] { R.id.entreeBox } );
		
		setListAdapter(mSELAdapter);
		
		/* Register Entree Click Listeners */
		ExpandableListView elv = getExpandableListView();
		elv.setOnChildClickListener(new OnEntreeClick());

		/* Get the current date and store as the default requested date. */
		mRequestedDate = new GregorianCalendar();
		updateMenu();

	}

	private void populateEntrees() {
		
		
	}

	private class OnEntreeClick implements OnChildClickListener {
		
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			
			Long p = ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
			startActivityForResult(new Intent(v.getContext(),
					MenuItemDetails.class), parent.getFlatListPosition(p));
			
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		startActivityForResult(new Intent(this, MenuCalendar.class), GET_DATE);

		return;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		
		if(resultCode == Activity.RESULT_OK) {
			Bundle b;
		switch (requestCode) {
		case GET_DATE:
			b = intent.getExtras();
			int offset = b.getInt(MenuCalendar.DATEKEY);
			mRequestedDate.roll(Calendar.DAY_OF_MONTH, offset);
			updateMenu();
			break;
		case SET_DIETARY_PREFS:
			b = intent.getExtras();
			int dp = b.getInt(DietaryPrefs.DP_KEY);
			mDishPrefs &= dp;
			updateMenu();
			break;	
			}
		}
	}

	void updateMenu() {
		//TODO: write this
		
		return;
	}

	/* Options Menu Code */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = new MenuInflater(this);
		mi.inflate(R.menu.mainmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.dietaryprefs:
			Intent i = new Intent(this, DietaryPrefs.class);
			Bundle b = new Bundle();
			b.putInt(DietaryPrefs.DP_KEY, mDishPrefs);
			i.putExtras(b);
			startActivityForResult(i, SET_DIETARY_PREFS);
			break;
		
		}
		
		return super.onOptionsItemSelected(item);
	}
	

	
	
	
}