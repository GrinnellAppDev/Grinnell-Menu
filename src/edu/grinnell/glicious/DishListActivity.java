package edu.grinnell.glicious;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import edu.grinnell.glicious.menucontent.Entree;
import edu.grinnell.glicious.menucontent.GetMenuTask;
import edu.grinnell.glicious.menucontent.GetMenuTask.Result;
import edu.grinnell.glicious.menucontent.GetMenuTask.RetrieveDataListener;
import edu.grinnell.glicious.menucontent.MenuContent;

public class DishListActivity extends FragmentActivity
        implements DishListFragment.Callbacks {
	
    private boolean 			mTwoPane;
    
    protected GregorianCalendar mRequestedDate,
								mPendingDate;
    protected static int 		mDaysRemaining = 7;
    
    protected GliciousPrefs 	mGPrefs;
    
    private GetMenuTask 		mGetMenuTask;
    
    private ViewPager 			mMenuPager;
    private MenuPagerAdapter	mMenuPagerAdapter;
    
    
    /* Debug Tags */
	public static final String 		JSON 		= "JSON Parsing";
	public static final String 		UITHREAD 	= "glic dla UI";
	public static final String 		DEBUG 		= "Generic Debug";

	/* Request codes: */
	public static final int PREFS 	= 2;
	public static final int NETWORK = 3;
	
	/* Intent Keys */
	public static final String REFRESH = "refresh";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_list);
        // Set default preferences..
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // Get a reference to the preferences class..
        mGPrefs = new GliciousPrefs(this);
        
        // Obtain a reference to the pager..
        mMenuPager = (ViewPager) findViewById(R.id.menu_pager);
        mMenuPagerAdapter = new MenuPagerAdapter(getSupportFragmentManager());
        mMenuPager.setAdapter(mMenuPagerAdapter);
        
        // Asynchronously load the menu data from nearest location (cache or server)..
        mPendingDate = new GregorianCalendar();
        loadMenu(this, mPendingDate, new GetMenuTaskListener(this));
        
        if (findViewById(R.id.dish_detail_container) != null) {
            mTwoPane = true;
        }
        
        
        
        
    }
    
    @Override
    public void onNewIntent(Intent incoming) {
    	super.onNewIntent(incoming);
    	Log.i(DEBUG, "onNewIntent called");
    	
    	if (incoming.getBooleanExtra(REFRESH, false)) {
    		GliciousPrefs.refresh();
    		MenuContent.refresh();
    		refreshPager();
    	}
    	
    }

    public void setListActivateState() {
    	if (mTwoPane) {
    		((DishListFragment) mMenuPagerAdapter.getItem(mMenuPager.getCurrentItem()))
    		.setActivateOnItemClick(true);
    	}
    }
    
    @Override
    public void onItemSelected(String id) {
    	
    	Entree e = MenuContent.mDishesMap.get(id);
    	if( e == null || e.type == Entree.VENUENTREE)
    		return;
    	
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(DishDetailFragment.ARG_ENTREE_ID, id);
            DishDetailFragment fragment = new DishDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dish_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, DishDetailActivity.class);
            detailIntent.putExtra(DishDetailFragment.ARG_ENTREE_ID, id);
            startActivity(detailIntent);
        }
    }
    
    
	/* Since GetMenuTask is asynchronous, we only attempt to load the menu if there 
	 * is no current instance of our task thread OR if the previous instance has 
	 * FINISHED executing. */
	protected void loadMenu(Context context, GregorianCalendar pendingDate, RetrieveDataListener rdl) {
		refreshMenu(context, pendingDate, rdl, false);
	}
	
	private void refreshMenu(Context c, GregorianCalendar pending, RetrieveDataListener rdl, boolean force) {
		if (mGetMenuTask == null || mGetMenuTask.getStatus() == AsyncTask.Status.FINISHED)
			mPendingDate = pending;
			mGetMenuTask = new GetMenuTask(c, rdl, force);
			mGetMenuTask.execute(	pending.get(Calendar.MONTH),
									pending.get(Calendar.DAY_OF_MONTH),
									pending.get(Calendar.YEAR) );
	}
	
	
	/* GetMenuTask handles acquiring the menu from either the local cache or the
	 * web server.  An instance of this listener is passed to GetMenuTask so that
	 * the proper methods can be called (by the UI thread and not the separate 
	 * thread which GetMenuTask runs on) once the data is acquired (or not).  See 
	 * the source for GetMenuTask for more details. */
	class GetMenuTaskListener implements GetMenuTask.RetrieveDataListener {
		
		Context mContext;
		
		public GetMenuTaskListener(Context c) {
			mContext = c;
		}
		
		//TODO: fix..
		@Override
		public void onRetrieveData(Result result) {
			switch(result.getCode()) {
			case Result.SUCCESS:
				Log.i(UITHREAD, "Menu successfully loaded!");
				/* On SUCCESS the menu string should be parsed into JSONObjects
				 * and the venues and entrees should be put into the list. */
				mRequestedDate = mPendingDate;
				MenuContent.setMenuData(result.getValue());
				refreshPager();
				
				break;
			case Result.NO_MEAL_DATA:
				Log.i(UITHREAD, "No meal data returned from server.");
				Utility.showToast(mContext, R.string.noMealContent);
				
				break;
			case Result.NO_NETWORK:
				Log.i(UITHREAD, "No network connection was available through which to retrieve the menu.");
				DialogFragment df = new NoNetworkDialogFragment();
				df.show(getSupportFragmentManager(), "network:dialog");
				break;
			case Result.NO_ROUTE:
				Log.i(UITHREAD, "Could not find a route to the menu server through the available connections");
				Utility.showToast(mContext, Result.NO_ROUTE);
				break;
			case Result.HTTP_ERROR:
				Log.i(UITHREAD, "Bad HTTP response was recieved.");
				Utility.showToast(mContext, Result.HTTP_ERROR);
			case Result.UNKNOWN:
				Log.i(UITHREAD, "Unknown result in method 'onRetrieveDate'");
				break;
			}
		}
	}	
	
	
	public static class NoNetworkDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.noNetworkMessage)
				   .setPositiveButton(R.string.settings,
						   new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   startActivityForResult(
							new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS),
							DishListActivity.NETWORK);
					   }
				   }).setNegativeButton(R.string.exit,
						   new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								getActivity().finish();
							}
						});
			return builder.create();
	    }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if(resultCode == Activity.RESULT_OK) {
		switch (requestCode) {
		case PREFS:
			Log.i(UITHREAD, "prefs result");
			GliciousPrefs.refresh();
			MenuContent.refresh();
			refreshPager();
			break;	
		case NETWORK:
			refreshMenu(this, mPendingDate, new GetMenuTaskListener(this), true);
			}
		}
	}

	public void refreshPager() {
		
		
		DishListFragment.refresh();
		mMenuPagerAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.menu_bar, menu);
	    //CalendarView cv = (CalendarView) menu.findItem(R.id.menu_date).getActionView();
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.action_refresh:
			refreshMenu(this, mPendingDate, new GetMenuTaskListener(this), true);
			break;
		case R.id.menu_date:
			DialogFragment df = new DatePickerFragment(mRequestedDate);
			df.show(getSupportFragmentManager(), "date_picker");
			break;
		case R.id.settings:
			// Display the fragment as the main content.
			startActivityForResult(new Intent(this, PrefActiv.class), PREFS);
			//TODO: start pref fragment
		
		
		}
		
		
		return false;
	}
	
	@SuppressLint("ValidFragment")
	public static class DatePickerFragment extends DialogFragment {
	    
		private GregorianCalendar date;
		
		public DatePickerFragment() {
			super();
		}
		
		public DatePickerFragment(GregorianCalendar req) {
			date = req;
		}
		
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	
	    	DatePickerDialog dpd = new DatePickerDialog(getActivity(), 
	    			new DatePickerDialog.OnDateSetListener() {
						
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear,
								int dayOfMonth) {
							final DishListActivity dla = (DishListActivity) getActivity();
							dla.loadMenu(dla, new GregorianCalendar(year, monthOfYear, dayOfMonth), 
									dla.new GetMenuTaskListener(dla));
							//dismiss();
							
						}
					}, date.get(Calendar.YEAR), 
					date.get(Calendar.MONTH), 
					date.get(Calendar.DAY_OF_MONTH));
	    	
	    	DatePicker dp = dpd.getDatePicker();
	    	//dp.setCalendarViewShown(true);
	    	GregorianCalendar d = new GregorianCalendar();
	    	
	    	//dp.setMinDate(Math.min(d.getTimeInMillis(), date.getTimeInMillis()));
	    	d.add(Calendar.DAY_OF_YEAR, mDaysRemaining);
	    	dp.setMaxDate(d.getTimeInMillis());
		    
			return dpd;
	    }
	}
	
	@Override
	public void onDestroy() {
		// Clean up the cache data.
		GetMenuTask.pruneCache(this);
		
		super.onDestroy();
	}
	
}
