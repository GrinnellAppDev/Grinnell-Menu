package edu.grinnell.glicious;

import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.grinnell.glicious.menucontent.GetMenuTask;
import edu.grinnell.glicious.menucontent.MenuContent;
import edu.grinnell.glicious.menucontent.GetMenuTask.Result;
import edu.grinnell.glicious.menucontent.GetMenuTask.RetrieveDataListener;
import edu.grinnell.glicious.R;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;

public class DishListActivity extends FragmentActivity
        implements DishListFragment.Callbacks {
	
    private boolean 			mTwoPane;
    
    protected GregorianCalendar mRequestedDate,
								mPendingDate;
    
    protected GliciousPrefs 	mGPrefs;
    
    private GetMenuTask 		mGetMenuTask;
    
    private ViewPager 			mMenuPager;
    private MenuPagerAdapter	mMenuPagerAdapter;
    
    /* Debug Tags */
	public static final String 					JSON 	= "JSON Parsing";
	public static final String 					UITHREAD = "GrinnellMenu UI";
	public static final String 					DEBUG 	= "Print";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_list);
        
        // Asynchronously load the menu data from nearest location (cache or server)..
        mPendingDate = new GregorianCalendar();
        loadMenu(this, mPendingDate, new GetMenuTaskListener(this));

        // Obtain a reference to the pager..
        mMenuPager = (ViewPager) findViewById(R.id.menu_pager);
        mMenuPagerAdapter = new MenuPagerAdapter(getSupportFragmentManager());
        mMenuPager.setAdapter(mMenuPagerAdapter);
        
        
        if (findViewById(R.id.dish_detail_container) != null) {
            mTwoPane = true;
            
            ((DishListFragment) mMenuPagerAdapter.getItem(mMenuPager.getCurrentItem()))
            .setActivateOnItemClick(true);
        	
        }
        
        // Get a reference to the preferences class..
        mGPrefs 	= new GliciousPrefs(this);
    }

    @Override
    public void onItemSelected(String id) {
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
	private void loadMenu(Context context, GregorianCalendar pendingDate, RetrieveDataListener rdl) {
		if (mGetMenuTask == null || mGetMenuTask.getStatus() == AsyncTask.Status.FINISHED)
			mGetMenuTask = new GetMenuTask(context, rdl);
			mGetMenuTask.execute(	pendingDate.get(Calendar.MONTH),
									pendingDate.get(Calendar.DAY_OF_MONTH),
									pendingDate.get(Calendar.YEAR) );
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
				DishListFragment.refresh();
				mMenuPagerAdapter.notifyDataSetChanged();
				break;
			case Result.NO_NETWORK:
				Log.i(UITHREAD, "No network connection was available through which to retrieve the menu.");
				//showDialog(Result.NO_NETWORK);
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
	
	
	@Override
	public void onDestroy() {
		// Clean up the cache data.
		GetMenuTask.pruneCache(this);
		
		super.onDestroy();
	}
	
}
