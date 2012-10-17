package edu.grinnell.glicious;

import edu.grinnell.glicious.menucontent.Entree;
import edu.grinnell.glicious.menucontent.MenuContent;
import edu.grinnell.glicious.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DishDetailFragment extends Fragment {

    public static final String ARG_ENTREE_ID = "entree_id";

    Entree mDish;

    public DishDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setRetainInstance(true);
        
        if (getArguments().containsKey(ARG_ENTREE_ID)) {
            mDish = MenuContent.mDishesMap.get(getArguments().getString(ARG_ENTREE_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dish_detail, container, false);
        if (mDish != null) {
            ((TextView) rootView.findViewById(R.id.dish_detail)).setText(mDish.name);
        }
        return rootView;
    }
}
