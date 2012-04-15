package com.android.grinnellmenu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
	
/* Asynchronous task for downloading menu from the network. */
public class GetMenuTask extends AsyncTask<Integer, Void, GetMenuTask.Result> {
		
	/* Store the app context so a progress dialog can be shown. */
	private Context mAppContext;
	private RetrieveDataListener mRetrieveDataListener;
	private boolean mForceUpdate;
			
	protected GetMenuTask(Context context, RetrieveDataListener rdl) {
		this(context, rdl, false);
	}
	
	protected GetMenuTask(Context context, 
			RetrieveDataListener rdl, Boolean forceUpdate) {
		super();
		mAppContext = context;
		mRetrieveDataListener = rdl;
		mForceUpdate = forceUpdate;	
	}
	
	private ProgressDialog mStatus;
	
	/* Setup the progress bar. */
	@Override
	protected void onPreExecute() {
		mStatus = ProgressDialog.show(mAppContext,"","Retrieving Menu...", true);
	}
	
	@Override
	protected Result doInBackground(Integer... args) {
		/* Args come as 0: month, 1: day, 2: year. */
		int month = args[0]+1, day = args[1], year = args[2];
		
		Result r = new Result();	
		// check the local cache first.. this is MUCH faster...
		if (!mForceUpdate) {
			r.setValue(loadLocalMenu(mAppContext, getCacheFileName(month, day, year)));
			if (r.getValue() != null)
				return r.setCode(Result.SUCCESS);
		}
			
		//download menu if not cached..
		ConnectivityManager cm = (ConnectivityManager)
				mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		//check connections before downloading..

		if (!networkEnabled(cm)) 
			return (r = new Result(Result.NO_NETWORK, ""));
		
		// THIS IS NOT WORKING AND APARENTLY DOESN'T ON SOME DEVICES..
		//TODO: FIX IFF possible..
		/*
		 else if (!routeClear(GrinnellMenuActivity.MENU_SERVER, cm)) 
			return (r = new Result(Result.NO_ROUTE, ""));
		*/
		
		//build the resource request..
		String request = "http://" + GrinnellMenuActivity.MENU_SERVER 
				+ GrinnellMenuActivity.DATA_PATH + 
				(args[0]+1)+"-"+args[1]+"-"+args[2]+".json";
		
		String menu = downloadMenuFromServer(request);
		if (menu == null)
			return r.setCode(Result.HTTP_ERROR);
		
		r.setValue(menu);
		//store the file in a cache and return the result
		return writeCache(mAppContext, getCacheFileName(month, day, year), menu) ?
				r.setCode(Result.SUCCESS) : r.setCode(Result.UNKNOWN);
	}
	
	/* Stop the dialog and notify the main thread that the new menu
	 * is loaded. */
	@Override
	protected void onPostExecute(Result result) {
		// stop the progress dialog
		mStatus.dismiss();
		
		// notify the UI thread listener ..
		mRetrieveDataListener.onRetrieveData(result);
		super.onPostExecute(result);
	}
	
	private static String getCacheFileName(int month, int day, int year) {
		return new String(GrinnellMenuActivity.CACHE_FILE + 
				"_"+(month)+"-"+day+"-"+year+".json");
	}

	protected static String loadLocalMenu(Context AppContext, String cacheFile) {

		/* String builder to store the JSON from the cache file. */
		StringBuilder r = new StringBuilder();
		
		try {
			File cacheDir = AppContext.getFilesDir();
			File f = new File(cacheDir, cacheFile); 
			BufferedReader br = new BufferedReader(new FileReader(f));
			String l;
			while ((l = br.readLine()) != null)
				r.append(l); 
			br.close();
			
		} catch (FileNotFoundException ffe) {
			Log.e(CACH, "No Cache file found.  " +
					"One will be created on first data retrieval.");
			return null;
		} catch (IOException e) {
			Log.e(CACH, e.toString());
			return null;
		}

		return r.toString();
	}
	
	protected static boolean writeCache(Context context, String cacheFile, String json) {
		
		/* Write to the cache file. */
		try {
			File f = new File(context.getFilesDir(), cacheFile);
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write(json);
			bw.close();
			
		} catch (IOException e) {
			Log.e(CACH, e.toString());
			return false;
		}
		return true;		
	}
	
	//TODO: write a method manageCache that deletes all cache files older
	//than one week.  This will be a Runable class that can be threaded and
	//executed by the main activity onCreate or perhaps onFinish or onDestroy.
	
	protected static String downloadMenuFromServer(String request) {
		//TODO: replace with simple java URL request.. no need for HTTP..
		
			// connection is up, attempt to retrieve the menu:
		String r = null;
		try {						
			Log.i(HTTP, request);
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(request);
				
			HttpResponse response = client.execute(post);
			Log.i(HTTP, response.getStatusLine().toString());
			//TODO: handle unsuccessful HTTP requests
				
			r = EntityUtils.toString(response.getEntity());
			//Log.i(HTTP, "JSON = " + r);
				
			} catch (IOException e) {
				Log.i(HTTP, e.toString());
				Log.i(HTTP, e.getMessage());
			} catch (ParseException p) {
				Log.e("ParseException", p.toString());} 
			return r;
	}
		
	/* Return true if the device has a network adapter that is capable of 
	 * accessing the network. */
	protected static boolean networkEnabled(ConnectivityManager cm) {
		NetworkInfo n = cm.getActiveNetworkInfo();
		return (n != null) && n.isConnectedOrConnecting();
	}
	
	/* Return true if the appropriate host can be reached. */
	protected static boolean routeClear(String host, ConnectivityManager cm) {
		/* RequestRouteToHost apparently doesn't work over wifi: return true. */
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni.getType() == ConnectivityManager.TYPE_WIFI)
			return true;
		
		/* Do the DNS lookup using java.net library. */
		InetAddress a;
		try {
			a = InetAddress.getByName(GrinnellMenuActivity.MENU_SERVER);
		} catch (UnknownHostException uhe) {
			Log.d(HTTP, uhe.toString());
			return false;
		}	
		/* Convert a byte array IP address into an integer representation. */
		byte[] b = a.getAddress();			 	// aaa.bbb.ccc.ddd
		int ipint = ((b[3] << 24) | 	// ipint bits[32-25] = aaa
					 (b[2] << 16) | 	// ipint bits[24-17] = bbb
					 (b[1] << 8 ) | 	// ipint bits[16- 9] = ccc
					  b[0]		   );	// ipint bits[ 8- 1] = ddd 
		
		return cm.requestRouteToHost(ni.getType(), ipint);
	}
	
	/* Result is meant to store the status and return data from the 
		 * methods of GetMenuTask that attempt to acquire menu data. */
	public class Result {
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
		
		public Result (int ResultCode, String resultValue) {
			code = ResultCode;
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
	protected interface RetrieveDataListener {
		public void onRetrieveData(Result result);
	}
	
	/* Log Keys */
	public static final String HTTP 	= "HTTP Request";
	public static final String CACH	    = "File Input";
}
