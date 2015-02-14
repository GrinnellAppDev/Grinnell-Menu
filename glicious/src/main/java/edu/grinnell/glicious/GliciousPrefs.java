package edu.grinnell.glicious;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GliciousPrefs {

	/* SharedPreferences */
	private static SharedPreferences 	mPrefs = null;
	
	private static final String 	F_OVO 	= "filterovolacto";
	private static final String 	F_VEG	= "filtervegan";
	private static final String 	F_HAL 	= "filterhalal";
	private static final String 	F_PAS	= "filterpassover";
	
	public static boolean 	mFilterOvolacto = false,
							mFilterVegan	= false,
							mFilterHalal	= false,
							mFilterPassover	= false;
							
	
	GliciousPrefs (Context context) {
		if (mPrefs == null)
			mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		refresh();
	}
	
	public static void refresh() {
		/* Load Stored Dish preferences and set mDishPrefs accordingly */
		mFilterOvolacto = mPrefs.getBoolean(F_OVO, false);
		mFilterVegan 	= mPrefs.getBoolean(F_VEG, false);
		mFilterHalal 	= mPrefs.getBoolean(F_HAL, false);
		mFilterPassover	= mPrefs.getBoolean(F_PAS, false);
		
	}
	
}
