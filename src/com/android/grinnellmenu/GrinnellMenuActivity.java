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
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class GrinnellMenuActivity extends ExpandableListActivity {

	/* JSON Menu Server Information: */
	public static final String MENU_SERVER = 
			"http://www.cs.grinnell.edu/";
	public static final String DATA_PATH =
			"~knolldug/parser/";
	
	/* Dialog code constants: */
	public static final int NO_NETWORK = 0;
	public static final int NO_ROUTE = 1;
	
	/* Request code constants: */
	public static final int GET_DATE 			= 1;
	public static final int SET_DIETARY_PREFS 	= 2;
	
	/* Debug Tags */
	public static final String mJSON = "JSON Parsing";
	public static final String mHTTP = "HTTP Request";
	public static final String mUIThread = "GrinnellMenu UI";
	public static final String mDebug = "Print";
	
	/* Meal Constants */
	//TODO: replace with enumeration class??
	public static final int B = 100;
	public static final int L = 101;
	public static final int D = 102;
	public static final int O = 103;
	
	/* Date of Menu to Retrieve */
	protected GregorianCalendar mRequestedDate;
	/* Meal to show in the list. */
	protected int mRequestedMeal;
	
	/* Menus retrieved for mRequestedDate */
	protected JSONObject mBreakfast;
	protected JSONObject mLunch;
	protected JSONObject mDinner;
	protected JSONObject mOuttakes;
	
	/* Dietary Dish Preferences */
	protected boolean mFilterVegan;
	protected boolean mFilterOvolacto;

	/* Expandable List View Configuration Items */ 
	protected List<Map<String, String>> mGroupList;
	protected List<List<Map<String, String>>> mChildList;
	public static final String VENUE = "venue";
	public static final String ENTREE = "entree";
	
	/* Adapter used by the expandable list. */
	private SimpleExpandableListAdapter mSELAdapter;

	/** Called when the activity is first created. */
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
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

		/* Attempt to restore date from a previous saved instance state. */
		int year = 0, month = -1, day = 0, hour = 0, minute = 0;
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			year = savedInstanceState.getInt(""+Calendar.YEAR);
			month = savedInstanceState.getInt(""+Calendar.MONTH);
			day = savedInstanceState.getInt(""+Calendar.DAY_OF_MONTH);
			hour = savedInstanceState.getInt(""+Calendar.HOUR_OF_DAY);
			minute = savedInstanceState.getInt(""+Calendar.MINUTE);
		}
			
		/* Get the current date and store as the default requested date. */
		GregorianCalendar c = new GregorianCalendar();
		if (c.get(Calendar.YEAR) > year || 
			c.get(Calendar.MONTH) > month || 
			c.get(Calendar.DAY_OF_MONTH) > day) {
			mRequestedDate = c;
			setMenusNull();
		}
		else { //or use the old date
			mRequestedDate = new GregorianCalendar(year, month, day, hour, minute);
			// and load the old meal values
			try {
				mBreakfast = new JSONObject(savedInstanceState.getString(""+B));
				mLunch = new JSONObject(savedInstanceState.getString(""+L));
				mDinner = new JSONObject(savedInstanceState.getString(""+D));
				mOuttakes = new JSONObject(savedInstanceState.getString(""+O));
			} catch (JSONException je) {
				Log.d(mJSON, je.toString());
			} 
		}	
		
		/* Retrieve most current menu for mRequestedDate from server. */
		updateMenu();
	
		/* Calculate which meal (breakfast, lunch, dinner, or out-takes) should be
		 * shown based upon what time of day it is. */
		mRequestedMeal = calculateMeal(mRequestedDate.get(Calendar.HOUR_OF_DAY));

		/* Setup the entrees: */
		populateEntrees();

	}

	protected void populateEntrees() {
		
		JSONObject meal = calculateMenu(mRequestedMeal);
		Log.d(mDebug, "Populating list for: " + mRequestedMeal);
		
		/* Do no populate the list if there is no data to populate it with. */
		if (meal.length() == 0) {
			Toast t = Toast.makeText(this, R.string.noMealContent, Toast.LENGTH_LONG);
			t.setGravity(Gravity.TOP, 0, 70);
			t.show();
			return;
		}
		
		/* Collapse all the groups. */
		for (int g = 0; g < mSELAdapter.getGroupCount(); g++)
			this.getExpandableListView().collapseGroup(g);
		
		/* Clear the lists which the expandableListView uses. */
		mGroupList.clear();
		mChildList.clear();
		
		@SuppressWarnings("unchecked")
		Iterator<String> it = (Iterator<String>) meal.keys();
		
		/* Setup the lists to reflect the new data. */
		try {
			/* Iterate through all the venues. */
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
						//String itemHalal = item.getString("halal");
						//String itemPassover = item.getString("passover");
						//String itemNutrition = item.getString("nutrition");
						
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
		
		
		mSELAdapter.notifyDataSetChanged();
		Log.d(mDebug, "ListAdapter set with new values.");
		return;
	}
	
	private void updateMenu() {
		
		Log.d(mDebug, "updateMenu");
				
		/* Get the JSON menu data from the current server. */
		String menu = getMenuFromServer();
		
		/* Parse the JSON data. */
		if (menu != null && !menu.isEmpty()) {
			try {
				JSONObject jmenu = new JSONObject(menu);
				
				mBreakfast = jmenu.getJSONObject("BREAKFAST");
				mLunch = jmenu.getJSONObject("LUNCH");
				mDinner = jmenu.getJSONObject("DINNER");
				mOuttakes = jmenu.getJSONObject("OUTTAKES");
			} catch (JSONException je) {
				Log.d("JSON Parsing", je.toString());
				setMenusNull();
			}
		} else {
			setMenusNull();
		}
		
		return;
	}

	private String getMenuFromServer() {
		
		ProgressDialog status = ProgressDialog.show(this,"","Retrieving Menu...", true);
		
		Log.d("az", "getMenuFromServer");

		String r = null;
		
		//TODO: handle unsuccessful http requests
		
		if (!networkEnabled()) {
			status.dismiss();
			showDialog(NO_NETWORK);
		} else if (!routeClear(MENU_SERVER)) {
			status.dismiss();
			showDialog(NO_ROUTE); //TODO: implement this!
		} else {
			// connection is up, attempt to retrieve the menu:
			try {
				int year = mRequestedDate.get(Calendar.YEAR);
				int month = mRequestedDate.get(Calendar.MONTH);
				int day = mRequestedDate.get(Calendar.DAY_OF_MONTH);
				
				String request = MENU_SERVER + DATA_PATH + 
						(month+1)+"-"+(day)+"-"+year+".json";
				
				Log.d(mHTTP, request);
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(request);
				
				HttpResponse response = client.execute(post);
				Log.d("az", response.getStatusLine().toString());
				
				r = EntityUtils.toString(response.getEntity());
				Log.d("az", "JSON = " + r);
			
			} catch (IOException e) {
				Log.d(mHTTP, e.toString());
				Log.d(mHTTP, e.getMessage());} 
			catch (ParseException p) {Log.d("exception", p.toString());} 
			finally {
				Log.d("az", "finally");
				status.dismiss();
			}
		}
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
	
	protected JSONObject calculateMenu(int hourOfDay) {
		switch (hourOfDay) {
		case B:
			return mBreakfast;
		case L:
			return mLunch;
		case D:
			return mDinner;
		default:
			return mOuttakes;
		}
	}
	
	private void setMenusNull() {
		mBreakfast = new JSONObject();
		mLunch = new JSONObject();
		mDinner = new JSONObject();
		mOuttakes = new JSONObject();
	}
	
	/* Return true if the device has a network adapter that is capable of accessing
	 * the network. */
	public boolean networkEnabled() {
		NetworkInfo n = ((ConnectivityManager)getSystemService(
				Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return (n != null) && n.isConnectedOrConnecting();
	}
	
	/* Return true if the appropriate host is can be reached. */
	public boolean routeClear(String host) {
		//TODO: method stub -- possibly implement this..
		//however, need DNS lookup
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if(resultCode == Activity.RESULT_OK) {
		switch (requestCode) {
		case SET_DIETARY_PREFS:
			//load preferences then:
			populateEntrees();
			break;	
			}
		}
	}
	
	/* Save the menu data so it only has to be retrieved from
	 * the server once every day */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		/* Save the date information. */
		outState.putInt(""+Calendar.YEAR, mRequestedDate.get(Calendar.YEAR));
		outState.putInt(""+Calendar.MONTH, mRequestedDate.get(Calendar.MONTH));
		outState.putInt(""+Calendar.DAY_OF_MONTH, mRequestedDate.get(Calendar.DAY_OF_MONTH));
		outState.putInt(""+Calendar.HOUR_OF_DAY, mRequestedDate.get(Calendar.HOUR_OF_DAY));
		outState.putInt(""+Calendar.MINUTE, mRequestedDate.get(Calendar.MINUTE));
		/* Save the meal information. */
		outState.putCharSequence(""+B, mBreakfast.toString());
		outState.putCharSequence(""+L, mLunch.toString());
		outState.putCharSequence(""+D, mDinner.toString());
		outState.putCharSequence(""+O, mOuttakes.toString());	
	}
	
	/* Options Menu Code */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = new MenuInflater(this);
		mi.inflate(R.menu.mainmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handle menu selection events. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.dietaryprefs:
			Intent i = new Intent(this, DietaryPrefs.class);
			startActivityForResult(i, SET_DIETARY_PREFS);
			break;
		case R.id.mealselector:
			showDialog(R.id.mealselector);
			break;
		case R.id.menuRefresh:
			updateMenu();
			populateEntrees();
			break;
		case R.id.selectDate:
			showDialog(R.id.selectDate);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	/* Dialog Code */
	/* Setup the dialog for selecting a meal. */
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		
		switch(id) {
		case R.id.mealselector:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.menuSelectorTitle)
				   .setItems(R.array.mealsForSelector, 
							new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mRequestedMeal = 100+which;
					Toast t = Toast.makeText(getApplicationContext(), 
							"Loading " + getResources().getStringArray(R.array.mealsForSelector)[which], 
							Toast.LENGTH_SHORT);
						t.setGravity(Gravity.TOP, 0, 70);	
						t.show();
					Log.d(mDebug, "Item "+which+" selected.");					
				}
			});
			Dialog selector = builder.create();
			selector.setOnDismissListener(new DismissListener());
			return selector;
			
		case NO_NETWORK:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.noNetworkMessage)
				   .setNeutralButton(R.string.okay,
						   new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   //TODO: take user to network settings
					   }
				   });
			return builder.create();
			
		case R.id.selectDate:
			return new DatePickerDialog(this, new DateDialogListener(), 
					mRequestedDate.get(Calendar.YEAR), 
					mRequestedDate.get(Calendar.MONTH), 
					mRequestedDate.get(Calendar.DAY_OF_MONTH));
		}
		
		return super.onCreateDialog(id);
	}

	/* Listener for Dialog onDismiss events. */
	private class DismissListener implements DialogInterface.OnDismissListener {
	
		/* Repopulate the list when a the meal dialog is dismissed. */
		@Override
		public void onDismiss(DialogInterface dialog) {
			populateEntrees();
		}
	}
	
	private class DateDialogListener implements DatePickerDialog.OnDateSetListener {
		@Override 
		public void onDateSet(DatePicker view, int year, int month, int day) {
			int hour = mRequestedDate.get(Calendar.HOUR_OF_DAY);
			int minute = mRequestedDate.get(Calendar.MINUTE);
			mRequestedDate = new GregorianCalendar(year, month, day, hour, minute);
			updateMenu();
			populateEntrees();
		}
	}
	
}