package edu.grinnell.glicious;

import java.util.ArrayList;
import java.util.List;

import edu.grinnell.glicious.menucontent.Entree;
import android.R.anim;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MenuListAdapter extends ArrayAdapter<Entree> {
	private DishListActivity mActivity;
	private List<Entree> mData;
	
	public MenuListAdapter(DishListActivity a, int layoutId, List<Entree> data) {
		super(a, layoutId, data);
		mActivity = a;
		mData = data;
	}
	
	private static class ViewHolder
    {
        TextView label;
        TextView nut;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup  parent) {
		
		ViewHolder holder;
		
		
		if (convertView == null) {
			LayoutInflater li = mActivity.getLayoutInflater();
			convertView = li.inflate(R.layout.entree_row, parent, false);
			holder = new ViewHolder();
			holder.label = (TextView) convertView.findViewById(R.id.namebox);
			holder.nut = (TextView) convertView.findViewById(R.id.nutritionbox);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		final Entree e = mData.get(position);
		
		
		if (e != null) {
			holder.label.setText(e.name);
			
			Resources r = mActivity.getResources();
			holder.label.setPadding(3, 3, 3, 3);
			
			
			if (e.type == Entree.VENUENTREE) {
				convertView.setBackgroundColor(
						r.getColor(R.color.gred));
				convertView.setClickable(false);
				holder.label.setTextSize(21.0f);
				holder.label.setGravity(Gravity.LEFT);
				convertView.setPadding(3, 10, 3, 10);
				holder.label.setTextColor(r.getColor(R.color.gcream));
				holder.nut.setText("");
				
				
			} else {
				convertView.setBackgroundColor(r.getColor(R.color.gcream));
				holder.label.setTextColor(Color.BLACK);
				holder.label.setGravity(Gravity.LEFT);
				convertView.setPadding(17, 7, 7, 7);
				//convertView.setClickable(false);
				
				if (e.nutrition != null)
					holder.nut.setText(">");
			}
		}
		
		return convertView;
	}
}
