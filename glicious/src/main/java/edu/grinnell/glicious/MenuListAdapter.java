package edu.grinnell.glicious;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

import edu.grinnell.glicious.menucontent.Entree;

public class MenuListAdapter extends ArrayAdapter<Entree> {
	private DishListActivity mActivity;
	private List<Entree> mData;
	private FavoritesPrefs mPrefs;
	private Resources r;
	
	public MenuListAdapter(DishListActivity a, int layoutId, List<Entree> data) {
		super(a, layoutId, data);
		mActivity = a;
		mData = data;
	}
	
	private static class ViewHolder
    {
        TextView label;
        TextView nut;
		ToggleButton star;
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup  parent) {
		
		ViewHolder holder;
		mPrefs = new FavoritesPrefs(getContext());
		r = mActivity.getResources();
		
		if (convertView == null) {
			LayoutInflater li = mActivity.getLayoutInflater();
			convertView = li.inflate(R.layout.entree_row, parent, false);
			holder = new ViewHolder();
			holder.label = (TextView) convertView.findViewById(R.id.namebox);
			holder.star = (ToggleButton) convertView.findViewById(R.id.fav_star);
			holder.nut = (TextView) convertView.findViewById(R.id.nutritionbox);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		final Entree e = mData.get(position);


		holder.star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {


				if (isChecked) {
					toggleButton.setBackgroundDrawable(r.getDrawable(R.drawable.clicked_360));
					mPrefs.addFavorite(e.name);
					Log.v("PREF", "IsChecked");
				} else {
					toggleButton.setBackgroundDrawable(r.getDrawable(R.drawable.unclicked_360));
					mPrefs.removeFavorite(e.name);
					Log.v("PREF", "IsNotChecked");
				}

			}
		});
		
		
		if (e != null) {

			holder.label.setPadding(3, 3, 3, 3);
			holder.nut.setText("");
			
			if (e.type == Entree.VENUENTREE) {
				holder.label.setText(Utility.captializeWords(e.name));
				convertView.setBackgroundResource(R.drawable.vheader);
				
				//convertView.setBackgroundColor(r.getColor(R.color.gred));
				//convertView.setClickable(false);
				holder.label.setTextSize(21.0f);
				holder.star.setVisibility(View.GONE);
				holder.label.setGravity(Gravity.RIGHT);
				
				convertView.setPadding(7, 10, 7, 10);
				holder.label.setTextColor(r.getColor(R.color.gcream));

				
			} else {
				holder.label.setText(e.name);
				holder.label.setTextSize(17.0f);

				if (mPrefs.favoriteDetector(e.name))
					holder.star.setChecked(true);
				else
					holder.star.setChecked(false);

				holder.star.setVisibility(View.VISIBLE);
				convertView.setBackgroundColor(r.getColor(R.color.gcream));
				convertView.setBackgroundDrawable(r.getDrawable(R.drawable.glistselector));
				holder.label.setTextColor(Color.BLACK);
				holder.label.setGravity(Gravity.LEFT);
				convertView.setPadding(21, 11, 11, 11);
				//convertView.setClickable(false);

				if (e.nutrition != null)
					holder.nut.setText(">");
					//holder.nut.setBackgroundColor(r.getColor(R.color.grinllightred));
			}
		}
		
		return convertView;
	}

}
