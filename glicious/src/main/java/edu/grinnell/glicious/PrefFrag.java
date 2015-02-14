package edu.grinnell.glicious;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PrefFrag extends PreferenceFragment {

	@Override
	public void onCreate(Bundle ofJoy) {
		super.onCreate(ofJoy);
		
		addPreferencesFromResource(R.xml.preferences);
	}
	
}
