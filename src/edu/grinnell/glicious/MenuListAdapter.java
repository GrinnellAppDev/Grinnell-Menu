package edu.grinnell.glicious;

import java.util.List;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.grinnell.glicious.menucontent.Entree;

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
			
			
			Resources r = mActivity.getResources();
			holder.label.setPadding(3, 3, 3, 3);
			holder.nut.setText("");
			
			if (e.type == Entree.VENUENTREE) {
				holder.label.setText(Utility.captializeWords(e.name));
				convertView.setBackgroundColor(r.getColor(R.color.gred));
				//convertView.setClickable(false);
				holder.label.setTextSize(21.0f);
				holder.label.setGravity(Gravity.LEFT);
				convertView.setPadding(7, 10, 7, 10);
				holder.label.setTextColor(r.getColor(R.color.gcream));
				
				
				
				
			} else {
				holder.label.setText(e.name);
				holder.label.setTextSize(17.0f);
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
