package edu.grinnell.glicious;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.grinnell.glicious.NutritionListAdapter.Label;
import edu.grinnell.glicious.menucontent.Entree;
import edu.grinnell.glicious.menucontent.MenuContent;
import edu.grinnell.glicious.menucontent.NutritionUtil;
import edu.grinnell.glicious.menucontent.NutritionUtil.NutritionInfo;

public class DishDetailFragment extends ListFragment {

    public static final String ARG_ENTREE_ID = "entree_id";

    Entree mDish;
    ArrayList<Label> mNutrition = new ArrayList<Label>();
    NutritionListAdapter mNLA;

    public DishDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments().containsKey(ARG_ENTREE_ID)) {
            mDish = MenuContent.mDishesMap.get(getArguments().getString(ARG_ENTREE_ID));
            fillNutrition(mDish.nutrition);
            mNLA = new NutritionListAdapter(getActivity(), R.layout.nutrition_row, mNutrition);
        }
        
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dish_detail, container, false);
        if (mDish != null) {
            ((TextView) rootView.findViewById(R.id.dish_detail)).setText(mDish.name);
            Log.d("detailcCV","\"" + mDish.servsize + "\"");
            ((TextView) rootView.findViewById(R.id.dish_serv_size_amt)).setText(mDish.servsize);
        }
        return rootView;
    }
    
    @Override
    public void onViewCreated(View view, Bundle ofJoy) {
    	super.onViewCreated(view, ofJoy);
    	
    	if(mDish != null) {
    		setListAdapter(mNLA);
    	}
    }
    
    public void fillNutrition(JSONObject list) {
    	
    	if (list != null) {
	    	@SuppressWarnings("unchecked")
			Iterator<String> it = list.keys();
	    	while(it.hasNext()) {
	    		String label = it.next();
	    		NutritionInfo ni = NutritionUtil.INFO.get(label);
	    		String value = (list.optString(label, "0"));
	    		if ("0".equals(value) ) 
	    			value = "~";
	    		else
	    			value = value + " " + ni.unit;
	    		
	    		mNutrition.add(new Label(ni.name, value, ni.order));
	    	}
	    	Collections.sort(mNutrition);
    	} else
    		mNutrition.add(new Label("Nutritional Information Unavailable", ""));
    }
}
