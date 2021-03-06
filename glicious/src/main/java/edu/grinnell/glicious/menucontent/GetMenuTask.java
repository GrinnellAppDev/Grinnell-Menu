package edu.grinnell.glicious.menucontent;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.grinnell.glicious.Utility;
import edu.grinnell.glicious.Utility.Result;
import edu.grinnell.glicious.Utility.RetrieveDataListener;

/* Asynchronous task for downloading menu from the network. */
public class GetMenuTask extends AsyncTask<Integer, Void, Result> {

	/* JSON Menu Server Information: */
    public static String OFF_CAMPUS_SERVER = "appdev.grinnell.edu/glicious/";
	public static final int CACHE_AGE_LIMIT = -7;

	/* Store the app context so a progress dialog can be shown. */
	private Context mAppContext;
	private RetrieveDataListener mRetrieveDataListener;

	private ProgressDialog mStatus;

	private static final int MAX_ATTEMPTS = 3;

	public GetMenuTask(Context context, RetrieveDataListener rdl) {
		super();
		mAppContext = context;
		mRetrieveDataListener = rdl;
	}

	/* Setup the progress bar. */
	@Override
	protected void onPreExecute() {
		mStatus = ProgressDialog.show(mAppContext, "", "Loading Menu...", true);
	}

	@Override
	protected Result doInBackground(Integer... args) {
		/* Args come as 0: month, 1: day, 2: year. */
		int month = args[0] + 1, day = args[1], year = args[2];

		Result r = new Result();

		// download menu if not cached..

		ConnectivityManager cm = (ConnectivityManager) mAppContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// check connections before downloading..

		if (!networkEnabled(cm))
			return (r = new Result(Result.NO_NETWORK, ""));

		String request = "https://" + OFF_CAMPUS_SERVER + (args[0] + 1)
				+ "-" + args[1] + "-" + args[2] + ".json";
        System.out.println(request);

		String menu = downloadDataFromServer(request);

		if (menu == null)
			return r.setCode(Result.NO_MEAL_DATA);
		else if (menu.equals(Integer.valueOf(Result.HTTP_ERROR).toString())) {
			return r.setCode(Result.HTTP_ERROR);
		}

		r.setValue(menu);
		// store the file in a cache and return the result
		return Utility.writeCache(mAppContext,
				Utility.getCacheFileName(month - 1, day, year), menu) ? r
				.setCode(Result.SUCCESS) : r.setCode(Result.UNKNOWN);
	}

	/*
	 * Stop the dialog and notify the main thread that the new menu is loaded.
	 */
	@Override
	protected void onPostExecute(Result result) {

		Log.i("getMenuTask", "menu loaded from the server");

		try {
			// dismiss loading..
			mStatus.dismiss();
			// notify the UI thread listener ..
			mRetrieveDataListener.onRetrieveData(result);
		} catch (Exception e) {
			Log.d("post execute", e.toString());
		}

		super.onPostExecute(result);
	}

	/*
	 * pruneCache deletes all cache files older than the specified year, month,
	 * day. This should be run by the context in order to manage its cache data.
	 * This function is NOT called automatically by any of the methods in
	 * GetMenuTask.
	 */
	public static void pruneCache(Context app) {

		int month, day, year;

		// Clean up the cache data.
		GregorianCalendar g = new GregorianCalendar();
		g.roll(GregorianCalendar.DAY_OF_MONTH, CACHE_AGE_LIMIT);

		month = g.get(Calendar.MONTH + 1);
		day = g.get(Calendar.DAY_OF_MONTH);
		year = g.get(Calendar.YEAR);

		// Calculate the decimal value of the given date.
		final long cutDate = Utility.toDecimalDate(month, day, year);

		// Get a list of all files older than cutDate.
		File dir = app.getFilesDir();
		File[] oldFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				// Log.i(GetMenuTask.CACH, filename);
				String splits[] = filename.split("\\.");
				if (splits == null || splits.length == 0)
					return false;
				String dateString = splits[0];
				long dateValue = 0;
				try {
					dateValue = Long.parseLong(dateString);
				} catch (NumberFormatException nfe) {
					return false;
				}
				return dateValue < cutDate;
			}
		});

		// Delete the old files.
		for (File f : oldFiles) {
			f.delete();
		}
	}

	protected static String downloadDataFromServer(String request) {
		// connection is up, attempt to retrieve the menu:
		String r = null;
		int attempts = 0;
		try {
			while (attempts < MAX_ATTEMPTS) {

				// Log.i(HTTP, request);
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(request);

				HttpResponse response = client.execute(post);
				// Log.i(HTTP, response.getStatusLine().toString());
				// Make sure the result is okay.
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					r = EntityUtils.toString(response.getEntity());
					// Log.i(HTTP, "JSON = " + r);
					break;
				}
				attempts++;
			}
		} catch (IOException e) {
			Log.e(HTTP, e.toString());
			Log.e(HTTP, e.getMessage());
		} catch (ParseException p) {
			Log.e("ParseException", p.toString());
		}

		return (attempts == MAX_ATTEMPTS) ? Integer.valueOf(Result.HTTP_ERROR)
				.toString() : r;
	}

	/*
	 * Return true if the device has a network adapter that is capable of
	 * accessing the network.
	 */
	protected static boolean networkEnabled(ConnectivityManager connec) {
		// ARE WE CONNECTED TO THE NET

		if (connec == null) {
			return false;
		}

		try {
			if (connec.getNetworkInfo(1) != null
					&& connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED)
				return true;
			else if (connec.getNetworkInfo(0) != null
					&& connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED)
				return true;
			else
				return false;
		} catch (NullPointerException exception) {
			return false;
		}
	}

	/* Log Keys */
	public static final String HTTP = "HTTP Request";
}
