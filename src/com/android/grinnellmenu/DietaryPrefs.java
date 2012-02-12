package com.android.grinnellmenu;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class DietaryPrefs extends PreferenceActivity {

	/* Bundle Keys */
	public static final String DP_KEY = "dietary_prefs";
	
	/* Dish Preferences */
	private int mDishPrefs;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    /* Obtain the current preferences from the user */
	    //TODO: Change to sharedPreferences
	
	    addPreferencesFromResource(R.xml.dietary_prefs);
	}

}
