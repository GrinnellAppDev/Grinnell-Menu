package edu.grinnell.glicious;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Utility {
	
	public static String captializeWords(String s) {
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < words.length; i++) {
                sb.append(words[i].substring(0, 1).toUpperCase())
                  .append(words[i].substring(1).toLowerCase());

                if (i != words.length - 1)
                        sb.append(" ");
        }
        return sb.toString();
	}
	
	
	public static void showToast(Context c, int message) {
		Toast t;
		switch(message) {
		case Result.NO_ROUTE:
			t = Toast.makeText(c, R.string.noRoute, Toast.LENGTH_SHORT);
			t.setGravity(Gravity.TOP, 0, 70);
			t.show();
			return;
		case Result.HTTP_ERROR:
			t = Toast.makeText(c, R.string.httpError, Toast.LENGTH_SHORT);
			t.setGravity(Gravity.TOP, 0, 70);
			t.show();
			return;
		case Result.NO_MEAL_DATA:
			t = Toast.makeText(c, R.string.noMealContent, Toast.LENGTH_LONG);
			t.setGravity(Gravity.TOP, 0, 70);
			t.show();
			return;
		default:
			return;		
		}
	}
	
	public static String dateString(GregorianCalendar c) {
		StringBuilder sb = new StringBuilder();
		sb.append(c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
		sb.append(" ");
		sb.append(c.get(Calendar.DAY_OF_MONTH));
		sb.append(", ");
		sb.append(c.get(Calendar.YEAR));
		sb.append(" | ");
		sb.append(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));
		return sb.toString();
	}
	
	public static final String 	CACH	    = "File Input";
	public static final int	CACHE_AGE_LIMIT = -7;
	
	protected static String loadLocalMenu(Context context, String cacheFile) {

		Log.i(CACH, "opening: " + cacheFile);
		
		/* String builder to store the JSON from the cache file. */
		StringBuilder r = new StringBuilder();
		
		try {
			FileInputStream fin = context.openFileInput(cacheFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));
			String l;
			while ((l = br.readLine()) != null)
				r.append(l); 
			br.close();
			
		} catch (FileNotFoundException ffe) {
			Log.i(CACH, "No Cache file found.  " +
					"One will be created on first data retrieval.");
			return null;
		} catch (IOException e) {
			Log.e(CACH, "cache file NOT written!!");
			Log.e(CACH, e.toString());
			return null;
		}
		return r.toString();
	}
	
	public static boolean writeCache(Context context, String cacheFile, String json) {
		
		/* Write to the cache file. */
		try {
			FileOutputStream fout = context.openFileOutput(
					cacheFile, Context.MODE_PRIVATE);
			fout.write(json.getBytes());
			Log.i(CACH, "cache file written: " + cacheFile);
		} catch (IOException e) {
			Log.e(CACH, "cache file not written!!");
			Log.e(CACH, e.toString());
			return false;
		}
		return true;		
	}
	
	/* pruneCache deletes all cache files older than the specified year, month, day.
	 * This should be run by the context in order to manage its cache data.  This
	 * function is NOT called automatically by any of the methods in GetMenuTask.
	 */
	protected static void pruneCache(Context app) {
		
		int month, day, year;
		
		// Clean up the cache data.
		GregorianCalendar g = new GregorianCalendar();
		g.roll(GregorianCalendar.DAY_OF_MONTH, CACHE_AGE_LIMIT);
		
		
		month 	= g.get(Calendar.MONTH + 1);
		day 	= g.get(Calendar.DAY_OF_MONTH); 
		year	= g.get(Calendar.YEAR);
		
		// Calculate the decimal value of the given date.
		final long cutDate = toDecimalDate(month, day, year);
		
		// Get a list of all files older than cutDate.
		File dir = app.getFilesDir();
		File[] oldFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				//Log.i(GetMenuTask.CACH, filename);
				String splits[] = filename.split("\\.");
				if (splits == null || splits.length == 0) return false;
				String dateString = splits[0];
				long dateValue = 0;
				try {
					dateValue = Long.parseLong(dateString);
				} catch(NumberFormatException nfe) {
					return false;
				}
				return dateValue < cutDate;
			}
		});
		
		// Delete the old files.
		for(File f : oldFiles) {
			f.delete();
		}
	}
	
	/* File names constructed such that newer files will always be greater
	 * numerically than older ones.  This way, it is easier to manage the cache.
	 */
	public static String getCacheFileName(int month, int day, int year) {
		StringBuilder filename = new StringBuilder();
		
		return filename	.append(toDecimalDate(month+1, day, year).toString())
						.append(".json").toString();
	}
	
	/* Calculate the decimal value of the given date. */
	public static Long toDecimalDate(int month, int day, int year) {
		// Calculate the decimal value of the given date.
		return Long.valueOf((day + (month * 100) + (year * 10000)));
	};

	
	/* Result is meant to store the status and return data from the 
	 * methods of GetMenuTask that attempt to acquire menu data. */
public static class Result {
	/* Result Code Constants */
	public static final int UNKNOWN = -1;
	public static final int SUCCESS = 0;
	public static final int NO_NETWORK = 1;
	public static final int NO_ROUTE = 2;
	public static final int HTTP_ERROR = 3;
	public static final int NO_CACHE = 4;
	public static final int NO_MEAL_DATA = 5;
	
	private int code;
	private String value;
	
	public Result (int resultCode, String resultValue) {
		code = resultCode;
		value = resultValue;
	}
	public Result () {this(-1, null);}
	public int getCode() {return code;}
	public String getValue() {return value;}
	public Result setCode(int c) {code = c; return this;}
	public Result setValue(String v) {value = v; return this;}
}
/* --- end result class --- */

/* Listener you should implement for the callback method in the UI thread
 * and pass to the constructor of GetMenuTask */
public interface RetrieveDataListener {
	public void onRetrieveData(Result result);
}

}
