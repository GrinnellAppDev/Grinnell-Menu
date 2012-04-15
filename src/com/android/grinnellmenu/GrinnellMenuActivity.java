package com.android.grinnellmenu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.grinnellmenu.GetMenuTask.Result;

import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAgent;

public class GrinnellMenuActivity extends ExpandableListActivity {

	/* JSON Menu Server Information: */
	public static final String 					MENU_SERVER = 
													"www.cs.grinnell.edu";
	public static final String 					DATA_PATH =
													"/~knolldug/parser/";
	
	public static final String					CACHE_FILE = "menu_cache";
	
	/* Request code constants: */
	public static final int 					WIRELESS_SETTINGS	= 1;
	
	/* Meal Constants */
	public static final int 					BREAKFAST = 0, 
												LUNCH = 1, 
												DINNER = 2, 
												OUTTAKES =3;
	
	/* AsyncTask used to issue web requests and load cache menu data. */
	private GetMenuTask						 	mGetMenuTask;
	/* Date of Menu to Retrieve */
	protected GregorianCalendar 				mRequestedDate;
	/* Meal to show in the list. */
	protected int	 							mMealRequest;
	protected String 							mMealString;
	
	
	/* Menus retrieved for mRequestedDate */
	protected JSONObject 						mBreakfast,
												mLunch,
												mDinner,
												mOuttakes;

	/* Expandable List View Configuration Items */ 
	protected List<Map<String, String>> 		mGroupList;
	protected List<List<Map<String, String>>> 	mChildList;
	public static final String 					VENUE 	= "venue",
												ENTREE 	= "entree";
	/* Adapter used by the expandable list. */
	private SimpleExpandableListAdapter 		mSELAdapter;
	
	/* Keys! */
	public static final String  				YEAR 	= "year",
												MONTH 	= "month",
												DAY 	= "day";
	
	public static final String  				REQMEAL = "reqmeal";
	
	public static final String 					K_B		= "breakfast",
												K_L		= "lunch",
												K_D		= "dinner",
												K_O		= "outtakes";
	
	/* SharedPreferences */
	private SharedPreferences 					mPrefs;
	private boolean								mPrefsDirty;
	
	/* Dietary Dish Preferences */
	protected boolean 							mFilterVegan;
	protected boolean 							mFilterOvolacto;
	public static final String 					F_OVO 	= "filterovolacto";
	public static final String 					F_VEG	= "filtervegan";
	
	/* Debug Tags */
	public static final String 					JSON 	= "JSON Parsing";
	public static final String 					UITHREAD = "GrinnellMenu UI";
	public static final String 					DEBUG 	= "Print";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		/* Crittercism crash and error tracking */
		Crittercism.init(getApplicationContext(), "4f8ab556b0931573b000033e");
		
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
		
		
		/* Load Stored Dish preferences and set mDishPrefs accordingly */
		PreferenceManager.setDefaultValues(this, R.xml.dietary_prefs, false);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		mFilterOvolacto = mPrefs.getBoolean(F_OVO, false);
		mFilterVegan 	= mPrefs.getBoolean(F_VEG, false);
		mPrefsDirty = false;
		
		mRequestedDate = new GregorianCalendar();
		
		/* Calculate which meal (breakfast, lunch, dinner, or out-takes) should be
		 * shown based upon what time of day it is. Or, load the old menu information
		 * if it exists*/	
		int year = 0, month = -1, day = 0, meal = 0;
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			year 	= savedInstanceState.getInt(YEAR);
			month 	= savedInstanceState.getInt(MONTH);
			day 	= savedInstanceState.getInt(DAY);
			meal 	= savedInstanceState.getInt(REQMEAL);
		}
		/* Get the current date and store as the default requested date. */
		GregorianCalendar c = new GregorianCalendar();
		if (c.get(Calendar.YEAR) 	> year  || 
			c.get(Calendar.MONTH) 	> month || 
			c.get(Calendar.DAY_OF_MONTH) > day) {
			mRequestedDate = c;
			mMealRequest = calculateMeal(mRequestedDate.get(Calendar.HOUR_OF_DAY));
			loadMenu();

		} else { //or use the old date
			mRequestedDate = new GregorianCalendar(year, month, day);
			mMealRequest = meal;
			// and load the old meal values
			try {
				mBreakfast 		= new JSONObject(savedInstanceState.getString(K_B));
				mLunch 			= new JSONObject(savedInstanceState.getString(K_L));
				mDinner 		= new JSONObject(savedInstanceState.getString(K_D));
				mOuttakes 		= new JSONObject(savedInstanceState.getString(K_O));
			} catch (JSONException je) {
				Log.d(JSON, je.toString());
				loadMenu();
			} 
		}	

		
		/* Setup the meal button 'tabs' at the bottom. */
		Button b1 = (Button) findViewById(R.id.breakfastButton);
		Button b2 = (Button) findViewById(R.id.lunchButton);
		Button b3 = (Button) findViewById(R.id.dinnerButton);
		Button b4 = (Button) findViewById(R.id.outtakesButton);
		
		menuButtonQuadListener mButtonQuadListener = 
				new menuButtonQuadListener(mMealRequest);
		
		b1.setOnClickListener(mButtonQuadListener);
		b2.setOnClickListener(mButtonQuadListener);
		b3.setOnClickListener(mButtonQuadListener);
		b4.setOnClickListener(mButtonQuadListener);
		

		/* Initialize the menus. */
		setMenusNull();	
		/* Load the menu from the nearest location. (Cache or Network) */
		loadMenu();
	}
	
		
	protected void onStart() {
		super.onStart();
		/* Flurry is a user statistics reporting agent. */
		FlurryAgent.onStartSession(this, "S7MM444QPIJP91NGWGTA");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	
		/* The menu needs to be reload if the user changed preferences. */ 
		if (mPrefsDirty) {
			mFilterOvolacto = mPrefs.getBoolean(F_OVO, false);
			mFilterVegan 	= mPrefs.getBoolean(F_VEG, false);
			mPrefsDirty = false;
			populateMenuView();
		}
		
	}
	
	/* GetMenuTask handles acquiring the menu from either the local cache or the
	 * web server.  An instance of this listener is passed to GetMenuTask so that
	 * the proper methods can be called (by the UI thread and not the separate 
	 * thread which GetMenuTask runs on) once the data is acquired (or not).  See 
	 * the source for GetMenuTask for more details. */
	private class GetMenuTaskListener implements GetMenuTask.RetrieveDataListener {
		@Override
		public void onRetrieveData(Result result) {
			switch(result.getCode()) {
			case Result.SUCCESS:
				Log.i(UITHREAD, "Menu successfully loaded!");
				/* On SUCCESS the menu string should be parsed into JSONObjects
				 * and the venues and entrees should be put into the list. */
				parseMenu(result.getValue());
				showToast(populateMenuView());				
				break;
			case Result.NO_NETWORK:
				Log.i(UITHREAD, "No network connection was available through which to retrieve the menu.");
				showDialog(Result.NO_NETWORK);
				break;
			case Result.NO_ROUTE:
				Log.i(UITHREAD, "Could not find a route to the menu server through the available connections");
				showToast(Result.NO_ROUTE);
				//TODO: Toast here..
				break;
			case Result.HTTP_ERROR:
				Log.i(UITHREAD, "Bad HTTP request was issued.");
				//TODO: Handle this case..
			case Result.UNKNOWN:
				Log.i(UITHREAD, "Unknown result in method 'onRetrieveDate'");
				break;
			}
		}
	}
	
	/* Since GetMenuTask is asynchronous, we only attempt to load the menu if there 
	 * is no current instance of our task thread OR if the previous instance has 
	 * FINISHED executing. */
	private void loadMenu() {
		if (mGetMenuTask == null || 
			mGetMenuTask.getStatus() == AsyncTask.Status.FINISHED)
			(mGetMenuTask = new GetMenuTask(this, new GetMenuTaskListener()))
			.execute(mRequestedDate.get(Calendar.MONTH),
					 mRequestedDate.get(Calendar.DAY_OF_MONTH),
					 mRequestedDate.get(Calendar.YEAR));
	}
	
	/* Given a JSON string of menu data, parseMenu will set the member fields
	 * mBreakfast, mLunch, mDinner, and mOuttakes so that they contain a 
	 * JSONObject representation of the menu.
	 */
	private void parseMenu(String menu) {
		Log.i(UITHREAD, "updateMenu");
		
		/* Parse the JSON data. */
		if (menu == null || menu.isEmpty())
			setMenusNull();
		else {
			try {
				JSONObject jmenu 	= new JSONObject(menu);
				mBreakfast 			= jmenu.getJSONObject("BREAKFAST");
				mLunch 				= jmenu.getJSONObject("LUNCH");
				mDinner 			= jmenu.getJSONObject("DINNER");
				mOuttakes 			= jmenu.getJSONObject("OUTTAKES");
			} catch (JSONException je) {
				Log.e(JSON, je.toString());
				setMenusNull();
			}
		}
		return;
	}
	
	/* Fill the expandable list view with the menu items form the JSON
	 * menu data.
	 */
	private int populateMenuView() {
		
		JSONObject meal = menuFactory(mMealRequest);
		Log.i(UITHREAD, "Populating list for: " + mMealString);
		
		/* Do no populate the list if there is no data to populate it with. */
		if (meal.length() == 0) {return Result.NO_MEAL_DATA;}
		
		/* Collapse all the groups. */
		ExpandableListView elv = this.getExpandableListView();
		//elv.isGroupExpanded(groupPosition);
		for (int g = 0; g < mSELAdapter.getGroupCount(); g++)
			elv.collapseGroup(g);
		
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
						String itemName = item.getString("name").trim();
						String itemVegan = item.getString("vegan").trim();
						String itemOvolacto = item.getString("ovolacto").trim();
						//String itemHalal = item.getString("halal");
						//String itemPassover = item.getString("passover");
						//String itemNutrition = item.getString("nutrition");
						
						//TODO: check against global preference flags properly...
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
			Log.e(JSON, je.toString());
			return Result.UNKNOWN;
		}
		
		/* Tell the list adapter to redraw the list since the content changed. */
		mSELAdapter.notifyDataSetChanged();
		Log.d(DEBUG, "ListAdapter set with new values.");
		
		/* Set the menu title to display the current meal and date. */
		TextView tv = (TextView) findViewById(R.id.headerText);
		tv.setText(mMealString.substring(0,1).toUpperCase() 
					+ mMealString.substring(1) + " | " 	
					+ mRequestedDate.get(Calendar.MONTH)+1 + " - "
					+ mRequestedDate.get(Calendar.DAY_OF_MONTH) + " - "
					+ mRequestedDate.get(Calendar.YEAR));
		
		return Result.SUCCESS;
	}

	/* Return a different menu CONSTANT based on what time of day it is. */
	private static int calculateMeal(int hourOfDay) {
		int h = hourOfDay % 24;
		if(h < 10) 
			return BREAKFAST;
		else if(h < 13)
			return LUNCH;	
		else if(h < 20)
			return DINNER;
		else
			return OUTTAKES;
	}
	
	/* Return a JSONObject containing the menu requested by whichKey. */
	private JSONObject menuFactory(int whichKey) {
		switch (whichKey) {
		case BREAKFAST:
			mMealString = K_B;
			return mBreakfast;
		case LUNCH:
			mMealString = K_L;	
			return mLunch;
		case DINNER:
			mMealString = K_D;
			return mDinner;
		case OUTTAKES:
		default:
			mMealString = K_O;
			return mOuttakes;
		}
	}
	
	/* Initialize all the menus so they are null JSONObjects and not 'null'. */
	private void setMenusNull() {
		mBreakfast 	= 	new JSONObject();
		mLunch 		= 	new JSONObject();
		mDinner 	= 	new JSONObject();
		mOuttakes 	= 	new JSONObject();
	}

	/* Handle results from the preferences Activity or the Network Settings
	 * activity. */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if(resultCode == Activity.RESULT_OK) {
		switch (requestCode) {
		case WIRELESS_SETTINGS:
			loadMenu();
			break;
		default:
			break;
			}
		}
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
					mMealRequest = which;
					Log.i(DEBUG, "Item "+which+" selected.");					
				}
			});
			Dialog selector = builder.create();
			selector.setOnDismissListener(new DismissListener());
			return selector;
			
		case Result.NO_NETWORK:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.noNetworkMessage)
				   .setNeutralButton(R.string.okay,
						   new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   //TODO: take user to network settings
						   startActivityForResult(
							new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
							,WIRELESS_SETTINGS);
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
	
	public void showToast(int message) {
		Toast t;
		switch(message) {
		case Result.NO_ROUTE:
			t = Toast.makeText(this, R.string.noRoute, Toast.LENGTH_SHORT);
			t.setGravity(Gravity.TOP, 0, 70);
			t.show();
			return;
		case Result.HTTP_ERROR:
			//TODO: this..
			return;
		case Result.NO_MEAL_DATA:
			t = Toast.makeText(this, R.string.noMealContent, Toast.LENGTH_SHORT);
			t.setGravity(Gravity.TOP, 0, 70);
			t.show();
			return;
		default:
			return;		
		}
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
			mPrefsDirty = true;
			Intent i = new Intent(this, DietaryPrefs.class);
			startActivity(i);
			break;
		case R.id.mealselector:
			showDialog(R.id.mealselector);
			break;
		case R.id.menuRefresh:
			loadMenu();
			break;
		case R.id.selectDate:
			showDialog(R.id.selectDate);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	/* --- ListenerSection --- */
	// --- --- ---- ---- --- ---/
	/* Update the date and refresh the view after the user chooses a new date. */
	private class DateDialogListener implements DatePickerDialog.OnDateSetListener {
		@Override 
		public void onDateSet(DatePicker view, int year, int month, int day) {
			int hour = mRequestedDate.get(Calendar.HOUR_OF_DAY);
			int minute = mRequestedDate.get(Calendar.MINUTE);
			mRequestedDate = new GregorianCalendar(year, month, day, hour, minute);
			loadMenu();
		}
	}
	
	/* Listener for Dialog onDismiss events -- used to swicth the visible menu
	 * after the user selects a new menu from the available breakfast, lunch, 
	 * dinner, or outtakes. */
	private class DismissListener implements DialogInterface.OnDismissListener {
	
		/* Repopulate the list when a the meal dialog is dismissed. */
		@Override
		public void onDismiss(DialogInterface dialog) {
			showToast(populateMenuView());
		}
	}
	
	/* This class handles the tab-like behavior of the bottom buttons. */
	private class menuButtonQuadListener implements OnClickListener {

		int mState;
			
		public menuButtonQuadListener(int initialStateButtonId) {
			super();
			mState = initialStateButtonId;
		}
		
		@Override
		public void onClick(View v) {
			
			int buttonID = v.getId();
			
			/* Do nothing if the button is already pressed. */
			if (mState == buttonID)
				return;
			
			/* Otherwise, set the previously pressed button as not pressed
			 * and 'press' the recently pressed button. */
			//TODO: implement this, first try didn't work..		
			
			mState = buttonID;
			
			/* Reflect the changes in the enclosing class. */
			mMealRequest = getMeal(mState);
			/* Fill the list entries with the new requested meal. */
			showToast(populateMenuView());
		}
		
		private int getMeal(int buttonID) {
			switch (buttonID) {
			case R.id.breakfastButton:
				return BREAKFAST;
			case R.id.lunchButton:
				return LUNCH;
			case R.id.dinnerButton:
				return DINNER;
			default:
				return OUTTAKES;
			}
		}
	}
	
	
	/* -- Some setting should be saved. */
	@Override
	protected void onStop() {
		/*Stop the Flurry Session*/
		FlurryAgent.onEndSession(this);
		
		super.onStop();
	}
	
	
	/// --- These are rarely used and not well tested.  TODO: Test..
	/* Show the menu as it was when the user left the application. */
//	@Override
//	protected void onRestoreInstanceState(Bundle state) {
//		super.onRestoreInstanceState(state);
//		
//		int year = 0, month = -1, day = 0, meal = 0;
//		if (state != null && !state.isEmpty()) {
//			year 	= state.getInt(YEAR);
//			month 	= state.getInt(MONTH);
//			day 	= state.getInt(DAY);
//			meal 	= state.getInt(REQMEAL);
//		}
//		/* Get the current date and store as the default requested date. */
//		GregorianCalendar c = new GregorianCalendar();
//		if (c.get(Calendar.YEAR) 	> year  || 
//			c.get(Calendar.MONTH) 	> month || 
//			c.get(Calendar.DAY_OF_MONTH) > day) {
//			mRequestedDate = c;
//			mMealRequest = calculateMeal(mRequestedDate.get(Calendar.HOUR_OF_DAY));
//			loadMenu();
//
//		} else { //or use the old date
//			mRequestedDate = new GregorianCalendar(year, month, day);
//			mMealRequest = meal;
//			// and load the old meal values
//			try {
//				mBreakfast 		= new JSONObject(state.getString(K_B));
//				mLunch 			= new JSONObject(state.getString(K_L));
//				mDinner 		= new JSONObject(state.getString(K_D));
//				mOuttakes 		= new JSONObject(state.getString(K_O));
//			} catch (JSONException je) {
//				Log.d(JSON, je.toString());
//				loadMenu();
//			} 
//		}	
//	}
	
	/* Save the menu data so it only has to be retrieved from
	 * the server once every day */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		/* Save the date information. */
		outState.putInt(YEAR, mRequestedDate.get(Calendar.YEAR));
		outState.putInt(MONTH, mRequestedDate.get(Calendar.MONTH));
		outState.putInt(DAY, mRequestedDate.get(Calendar.DAY_OF_MONTH));

		/* Save the active meal. */
		outState.putInt(REQMEAL, mMealRequest);
		/* Save the meal information. */
		outState.putCharSequence(K_B, mBreakfast.toString());
		outState.putCharSequence(K_L, mLunch.toString());
		outState.putCharSequence(K_D, mDinner.toString());
		outState.putCharSequence(K_O, mOuttakes.toString());	
	}
	
} // class