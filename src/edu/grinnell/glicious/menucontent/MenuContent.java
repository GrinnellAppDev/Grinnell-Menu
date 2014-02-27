package edu.grinnell.glicious.menucontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import edu.grinnell.glicious.GliciousPrefs;
import edu.grinnell.glicious.Utility;

public class MenuContent {

	private static final String JSONError = "JSON Error";

	// Menu keys..
	public static final String BREAKFAST = "breakfast";
	public static final String LUNCH = "lunch";
	public static final String DINNER = "dinner";
	public static final String OUTTAKES = "outtakes";

	public static boolean valid = false;

	// Menu map

	private static Map<String, Integer> MEALINDEX = new HashMap<String, Integer>();

	// JSON for the menu..
	private static JSONObject mMenuData = null;

	// List and map for entrees loaded from the JSON..
	public static List<String> mMenuOrder = new LinkedList<String>();
	public static Map<String, Entree> mDishesMap = new HashMap<String, Entree>();
	public static Map<String, List<Entree>> mMealsMap = new HashMap<String, List<Entree>>();

	static {
		// Add meal map
		MEALINDEX.put(BREAKFAST, 0);
		MEALINDEX.put(LUNCH, 1);
		MEALINDEX.put(DINNER, 2);
		MEALINDEX.put(OUTTAKES, 3);

		// Create a default list..
	}

	public static void setMenuData(String json, Context c) {
		Assert.assertNotNull(json);

		try {
			mMenuData = new JSONObject(json);
		} catch (JSONException jsone) {
			Log.e(JSONError, jsone.getMessage());
		}

		// the MenuData has been set, this static instance is valid..
		valid = true;

		// Update..
		populateMealTable(c);

	}

	/*
	 * Refresh the Dishes and Venues present in the collections observed by the
	 * ListFragment's ArrayAdapter. This method should be called after dietary
	 * preferences are updated, for example..
	 */
	public static void refresh(Context c) {
		if (valid)
			populateMealTable(c);
		else
			Log.d("MenuContent", "Cannot refresh(), class is invalid.");
	}

	private static void addMealAsAvailable(String menu, List<Entree> menuList) {
		// Add the list of venues (menuList) into the map..
		mMealsMap.put(menu, menuList);

		// Determine where to put the menu in the order list
		// Breakfast -> Lunch -> Dinner -> Outtakes
		int x;
		for (x = 0; x < mMenuOrder.size() && MEALINDEX.get(mMenuOrder.get(x)) < MEALINDEX.get(menu); x++)
			;

		mMenuOrder.add(x, menu);
	}

	private static void populateMealTable(Context c) {
		// Assert.assertNotNull(mMenuData);
		try {
			// Clear out the old data..
			// for ( String meal : mMealsMap.keySet() )
			// mMealsMap.get(meal).clear();
			mMealsMap.clear();
			mDishesMap.clear();
			mMenuOrder.clear();

			// Iterate over meal keys..
			@SuppressWarnings("unchecked")
			Iterator<String> it = (Iterator<String>) mMenuData.keys();
			while (it.hasNext()) {
				String menu = it.next();
				// Create the list of dishes..
				JSONObject meal = null;

				meal = mMenuData.optJSONObject(menu);
				// TODO: temporary fix; tell Collin to make passover have a true
				// / false value
				if (meal == null || menu.trim().toLowerCase().contains("passover"))
					continue;
				// --
				// Log.i("populateMealTable: ", meal.toString());
				// Assert.assertNotNull(meal);

				List<Entree> menuList = createEntreeList(meal);

				if (menuList != null && !menuList.isEmpty())
					addMealAsAvailable(menu.toLowerCase().trim(), menuList);
			}
		} catch (NullPointerException e) {
			Utility.showToast(c, Utility.Result.NO_ROUTE);
			Crashlytics.logException(e);
		}

		// TODO: finish..
		// populateMenuList(menuData);
	}

	private static List<Entree> createEntreeList(JSONObject meal) {

		Assert.assertNotNull(meal);
		List<Entree> mealList = new ArrayList<Entree>(meal.length());

		// Iterate over meal keys (venues)..
		@SuppressWarnings("unchecked")
		Iterator<String> meals = (Iterator<String>) meal.keys();
		while (meals.hasNext()) {
			String venueKey = meals.next();
			Entree e = new Entree(venueKey, venueKey.trim(), Entree.VENUENTREE);
			mealList.add(e);

			JSONArray venue = meal.optJSONArray(venueKey);
			mealList.addAll(createVenueDishList(venue));

		}

		return mealList;
	}

	/*
	 * Create a list of Dishe entrees from a given JSON venue array. This method
	 * also addes the dishes to the DishesMap hashmap.
	 */
	private static List<Entree> createVenueDishList(JSONArray venue) {

		Assert.assertNotNull(venue);
		List<Entree> venueList = new ArrayList<Entree>(venue.length());

		for (int i = 0; i < venue.length(); i++) {
			if (venue.optJSONObject(i) != null) {
				Entree e = new Entree(venue.optJSONObject(i), Entree.DISHENTREE);

				if ((!GliciousPrefs.mFilterVegan) || // allow all
						(e.vegan) || // allow vegan
						(GliciousPrefs.mFilterOvolacto && e.ovolacto)) { // allow
																			// vegetarian
					// Add entree to the list
					venueList.add(e);
					// Add to the entree to the entree map..
					mDishesMap.put(e.id, e);
				}

			}
		}

		return venueList;
	}

	public static List<Entree> retrieveMenu(String key) {
		if (valid)
			return mMealsMap.get(key);
		else
			Log.d("MenuContent", "Cannot retrieveMenu(" + key + "), class is invalid.");
		return null;
	}

	public JSONObject getJSONData() {
		if (!valid)
			Log.d("MenuContent", "no JSONData: MenuContent class is invalid.");
		return mMenuData;
	}

}
