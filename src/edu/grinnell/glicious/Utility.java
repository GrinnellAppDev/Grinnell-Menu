package edu.grinnell.glicious;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;
import edu.grinnell.glicious.menucontent.GetMenuTask.Result;
import edu.grinnell.glicious.R;

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
}
