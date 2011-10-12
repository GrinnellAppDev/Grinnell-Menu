package com.android.grinnellmenu;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class GrinnellMenuActivity extends Activity {
	
	public static int GET_DATE = 1;
	GregorianCalendar mRequestedDate;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mRequestedDate = new GregorianCalendar();
        updateMenu();
        
    }
    
    public void onBackPressed() {
    	startActivityForResult(
    			new Intent(this, MenuCalendar.class), GET_DATE);
    	
    	return;
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Bundle b = intent.getExtras();
    	int offset = b.getInt(MenuCalendar.DATEKEY);
    	mRequestedDate.roll(Calendar.DAY_OF_MONTH, offset);
    	updateMenu();
    }
    
    void updateMenu() {
    	
    return;
    }
}