package edu.grinnell.glicious;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NutritionListAdapter extends ArrayAdapter<NutritionListAdapter.Label> {
	
	private Context mContext;
	private ArrayList<Label> mData;
	
	public NutritionListAdapter(Context c, int layoutId, ArrayList<Label> data) {
		super(c, layoutId, data);
		mContext = c;
		mData = data;
	}
	
	public static class Label {
		
		public String label;
		public String amount;
		
		public Label(String label, String amount) {
			this.label = label;
			this.amount = amount;
		}
	}
	
	private static class ViewHolder
    {
        TextView label;
        TextView amount;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup  parent) {
		
		ViewHolder holder;
		
		if (convertView == null) {
			LayoutInflater li = ((Activity) mContext).getLayoutInflater();
			convertView = li.inflate(R.layout.nutrition_row, parent, false);
			holder = new ViewHolder();
			holder.label = (TextView) convertView.findViewById(R.id.labelbox);
			holder.amount = (TextView) convertView.findViewById(R.id.amountbox);
			convertView.setTag(holder);
			
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		final Label l = mData.get(position);
		
		convertView.setClickable(false);
		
		if (l != null) {
			holder.label.setText(l.label);
			holder.amount.setText(l.amount);
		}
		
		return convertView;
	}
}
