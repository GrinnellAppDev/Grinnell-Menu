package edu.grinnell.glicious;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.flurry.android.FlurryAgent;

public class DietaryPrefs extends PreferenceActivity {	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    /* Obtain the current preferences from the user */
	   PreferenceManager.setDefaultValues(this, R.xml.dietary_prefs, false);
	    
	    addPreferencesFromResource(R.xml.dietary_prefs);
	    
	    
	}
	
	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, "S7MM444QPIJP91NGWGTA");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

}
