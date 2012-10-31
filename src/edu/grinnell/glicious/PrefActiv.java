package edu.grinnell.glicious;

import java.util.List;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefActiv extends PreferenceActivity {
	
	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.prefmain);
	    
	    if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
	        addPreferencesFromResource(R.xml.preferences); 
	    } else {
	    	getFragmentManager().beginTransaction().replace(android.R.id.content,
	    	         new PrefFrag()).commit();
	    }

	}
	
	/*
	 @Override
	  public void onBuildHeaders(List<Header> target) {
	    loadHeadersFromResource(R.xml.preference_headers, target);
	  }
	  */
}
