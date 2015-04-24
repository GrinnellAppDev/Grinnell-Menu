package edu.grinnell.glicious;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class FavoritesPrefs {
	static Map<String, Boolean> favoritesMap = new HashMap<String, Boolean>();
	SharedPreferences.Editor keyValuesEditor;
	private static SharedPreferences keyValues = null;

	FavoritesPrefs(Context c) {
		if (keyValues == null) {
			keyValues = c.getSharedPreferences("glicious-madhacks-faves",
					Context.MODE_PRIVATE);
		}
		keyValuesEditor = keyValues.edit();
	}

	void addFavorite(String key){
		keyValuesEditor.putBoolean(key, true).commit();
	}


	/*
	 * Saves favorited items into active memory. Intended to be used on launch.
	 */
//	void saveFavorites() {
//		/*
//		 * Method taken from
//		 * https://stackoverflow.com/questions/7944601/saving-a
//		 * -hash-map-into-shared-preferences
//		 */
//
//		for (String s : favoritesMap.keySet()) {
//			keyValuesEditor.putBoolean(s, favoritesMap.get(s));
//		}
//
//		keyValuesEditor.commit();
//		Log.v("PREFS", favoritesMap.toString());
//	}

//	/*
//	 * Loads previously favorited items into active memory. Intended to be used
//	 * on creation of this object.
//	 */
//	@SuppressWarnings("unchecked")
//	void loadFavorites() {
//		favoritesMap
//				.putAll((Map<? extends String, ? extends Boolean>) keyValues
//						.getAll());
//	} // loadFavorites()

	/*
	 * Removes the unfavorited item.
	 */
	void removeFavorite(String key) {
		keyValuesEditor.remove(key).commit();
	}



	/*
	 * Determines if the given key is in favoritesMap
	 */
	boolean favoriteDetector(String key) {
		return keyValues.contains(key);
	} // favoriteDetector(String)

}
