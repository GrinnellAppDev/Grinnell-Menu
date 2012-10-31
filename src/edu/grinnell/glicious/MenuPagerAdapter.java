package edu.grinnell.glicious;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import edu.grinnell.glicious.menucontent.MenuContent;

public class MenuPagerAdapter extends FragmentStatePagerAdapter {

	
	
	public MenuPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int req) {
		try {
			return DishListFragment.getInstance(MenuContent.mMenuOrder.get(req));
		} catch (IndexOutOfBoundsException ioobe) {
			Log.e("MenuPagerAdapter", ioobe.getMessage());
		}
		return null;
	}

	@Override
	public int getCount() {
		return MenuContent.mMenuOrder.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return Utility.captializeWords( MenuContent.mMenuOrder.get(position) );
	}
	
	
	@Override
	public int getItemPosition(Object o) {
		return POSITION_NONE;
	}
	
}
