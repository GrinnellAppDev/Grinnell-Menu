package edu.grinnell.glicious;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class PrefActiv extends PreferenceActivity {
	
	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.prefmain);
	    
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
	        addPreferencesFromResource(R.xml.preferences); 
	    } else {
	    	getFragmentManager().beginTransaction().replace(android.R.id.content,
	    	         new PrefFrag()).commit();
	    }

	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        	
        	Intent upIntent = new Intent(this, DishListActivity.class);
        	upIntent.putExtra(DishListActivity.REFRESH, true);
            NavUtils.navigateUpTo(this, upIntent);
            
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
	
	/*
	 @Override
	  public void onBuildHeaders(List<Header> target) {
	    loadHeadersFromResource(R.xml.preference_headers, target);
	  }
	  */
	
	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_OK);
		finish();
	}
}
