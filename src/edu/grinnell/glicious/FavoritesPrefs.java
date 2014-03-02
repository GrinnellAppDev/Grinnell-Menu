package edu.grinnell.glicious;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class FavoritesPrefs {
	static Map<String, Boolean> favoritesMap = new HashMap<String, Boolean>();
	private static SharedPreferences keyValues = null;

	FavoritesPrefs(Context c) {
		if (keyValues == null) {
			keyValues = c.getSharedPreferences("glicious-madhacks-faves",
					Context.MODE_PRIVATE);
		}

		loadFavorites();
	}

	/*
	 * Saves favorited items into active memory. Intended to be used on launch.
	 */
	void saveFavorites() {
		/*
		 * Method taken from
		 * https://stackoverflow.com/questions/7944601/saving-a
		 * -hash-map-into-shared-preferences
		 */

		SharedPreferences.Editor keyValuesEditor = keyValues.edit();

		for (String s : favoritesMap.keySet()) {
			keyValuesEditor.putBoolean(s, favoritesMap.get(s));
		}

		keyValuesEditor.commit();
	}

	/*
	 * Loads previously favorited items into active memory. Intended to be used
	 * on creation of this object.
	 */
	@SuppressWarnings("unchecked")
	void loadFavorites() {
		favoritesMap
				.putAll((Map<? extends String, ? extends Boolean>) keyValues
						.getAll());
	} // loadFavorites()

	/*
	 * Removes the unfavorited item.
	 */
	void removeFavorite(String key) {
		if (favoriteDetector(key)) {
			favoritesMap.remove(key);
			saveFavorites();
		} // if
	} // removeFavorite(String)

	/*
	 * Determines if the given key is in favoritesMap
	 */
	boolean favoriteDetector(String key) {
		return favoritesMap.containsKey(key);
	} // favoriteDetector(String)

}
