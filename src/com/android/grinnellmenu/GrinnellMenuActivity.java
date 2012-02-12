package com.android.grinnellmenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;

public class GrinnellMenuActivity extends ExpandableListActivity {

	public static final String MENU_SERVER = 
			"http://www.cs.grinnell.edu/~knolldug/parser/menu.php";
	
	/* Request codes: */
	public static final int GET_DATE 			= 1;
	public static final int SET_DIETARY_PREFS 	= 2;
	
	public static final int B = 1;
	public static final int L = 2;
	public static final int D = 3;
	public static final int O = 4;
	
	/* Expandable List View Configuration Items */ 
	protected List<Map<String, String>> mGroupList;
	protected List<List<Map<String, String>>> mChildList;
	public static String VENUE = "venue";
	public static String ENTREE = "entree";
	
	/* Date of Menu to Retrieve */
	protected GregorianCalendar mRequestedDate;
	protected int mRequestedMeal;
	
	/* Menus retrieved for mRequestedDate */
	JSONObject mBreakfast;
	JSONObject mLunch;
	JSONObject mDinner;
	JSONObject mOuttakes;
	
	/* Dietary Dish Preferences */
	protected boolean mFilterVegan;
	protected boolean mFilterOvolacto;
	


	private ExpandableListAdapter mSELAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
 
		/* Load Stored Dish preferences and set mDishPrefs accordingly */
		//TODO: add code here
		mFilterVegan = false;
		mFilterOvolacto = false;
		
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
		
		mRequestedMeal = calculateMeal(mRequestedDate.get(Calendar.HOUR_OF_DAY));

		//Setup the entrees:
		populateEntrees(); // TODO: do this in updateMenu() instead

	}

	private void populateEntrees() {
		
		JSONObject meal;
		
		switch (mRequestedMeal) {
		case B:
			meal = mBreakfast;
			break;
		case L:
			meal = mLunch;
			break;
		case D:
			meal = mDinner;
			break;
		default:
			meal = mOuttakes;
			break;
		}
		
		@SuppressWarnings("unchecked")
		Iterator<String> it = (Iterator<String>) meal.keys();
		
		/* Prepare the new GroupList with the operating venues. */
		mGroupList.clear();
		mChildList.clear();
		
		try {
			while (it.hasNext()) {
				/* Add venues to the GroupList */
				Map<String,String> map = new HashMap<String,String>();
				String venueName = it.next();
				map.put(VENUE,venueName);
				mGroupList.add(map);
			
				/* Add entrees to the ChildLists */
				JSONArray entrees = meal.getJSONArray(venueName);
				JSONObject item;
				List<Map<String,String>> entreeList = new ArrayList<Map<String,String>>();
				for (int i = 0; i < entrees.length(); i++) {
					item = entrees.getJSONObject(i);
					if (item != null) {
						String itemName = item.getString("name");
						String itemVegan = item.getString("vegan");
						String itemOvolacto = item.getString("ovolacto");
						String itemHalal = item.getString("halal");
						String itemPassover = item.getString("passover");
						String itemNutrition = item.getString("nutrition");
						
						//TODO: check against global preference flags
						if ((!mFilterVegan    && !mFilterOvolacto) || //allow all
							( mFilterOvolacto && (itemOvolacto.equals("true") || itemVegan.equals("true"))) ||
							( mFilterVegan    && (itemVegan.equals("true")))) {
								
							Map<String, String> m = new HashMap<String,String>();
							m.put(ENTREE, itemName);
							entreeList.add(m);
						}
					}
						
				}
				
				mChildList.add(entreeList);		
			}
		} catch (JSONException je) {
			Log.d("JSON Parsing",je.toString());
		}
		
		
		
	}
	
	private void updateMenu() {
		
		Log.d("az", "updateMenu");
		
		//TODO: display a retrieving menu progress/busy icon
		//TODO: only retrieve menu from server if it has not been previously
		
		/* Get the JSON menu data from the current server. */
		String menu = getMenuFromServer();

		if (menu != null)
			Log.d("az", "menu = "+menu+" END MENU");
		
		/* Parse the JSON data. */
		if (menu != null)
			try {
				JSONObject jmenu = new JSONObject(menu);
				
				mBreakfast = jmenu.getJSONObject("BREAKFAST");
				mLunch = jmenu.getJSONObject("LUNCH");
				mDinner = jmenu.getJSONObject("DINNER");
				mOuttakes = jmenu.getJSONObject("OUTTAKES");
			} catch (JSONException je) {
				Log.d("JSON Parsing", je.toString());
			}
		
		return;
	}

	private String getMenuFromServer() {
		Log.d("az", "getMenuFromServer");
		int year = mRequestedDate.get(Calendar.YEAR);
		int month = mRequestedDate.get(Calendar.MONTH);
		int day = mRequestedDate.get(Calendar.DAY_OF_MONTH);

		String r = null;
		
		//TODO: check network connection before executing http request
		//TODO: handle unsuccessful http requests
		
		try {
			String request = MENU_SERVER + "?mon="+month+"&day="+day+"&year="+year;
			//debug
			String debugrequest = MENU_SERVER+"?mon=2&day=1&year=2012";
			
			HttpClient client = new DefaultHttpClient();
			//debug
			HttpPost post = new HttpPost(debugrequest);
			
			HttpResponse response = client.execute(post);
			Log.d("az", response.getStatusLine().toString());
			
			r = EntityUtils.toString(response.getEntity());
			Log.d("az", "JSON = " + r);
		
		} catch (IOException e) {
			Log.d("exception", e.toString());
			Log.d("exception", e.getMessage());} 
		catch (ParseException p) {Log.d("exception", p.toString());} 
		finally {Log.d("az", "finally");}
		
		return r;
	}

	public static int calculateMeal(int hourOfDay) {
		int h = hourOfDay % 24;
		if(h < 10) 
			return B;
		else if(h < 13)
			return L;	
		else if(h < 20)
			return D;
		else
			return O;
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
			updateMenu();
			break;	
			}
		}
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
			startActivityForResult(i, SET_DIETARY_PREFS);
			break;
		
		}
		
		return super.onOptionsItemSelected(item);
	}
	

	
	
	
}