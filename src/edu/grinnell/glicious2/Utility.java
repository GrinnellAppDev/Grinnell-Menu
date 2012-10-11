package edu.grinnell.glicious2;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;
import edu.grinnell.glicious2.R;
import edu.grinnell.glicious2.menucontent.GetMenuTask.Result;

public class Utility {
	
	
	
	
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
}
