package edu.grinnell.glicious;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import edu.grinnell.glicious.menucontent.Entree;
import edu.grinnell.glicious.menucontent.MenuContent;

public class DishListFragment extends SherlockListFragment {
	
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String MENU = "menu";
    private static final String DLF = "DishListFragment";

    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {

        public void onItemSelected(String id);
        public void setListActivateState();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
        @Override
        public void setListActivateState() {
        	
        }
    };

    private String mMenuKey;
    List<Entree> mMenuList;
    private static Map<String, DishListFragment> mInstances = new HashMap<String, DishListFragment>(); 
    protected MenuListAdapter mListAdapter;
    
    public DishListFragment() {
    	super();
    }
    

    public static DishListFragment getInstance(String menuKey) {
    	if (mInstances.containsKey(menuKey)) {
    		return mInstances.get(menuKey);
    	} else {
    		Bundle b = new Bundle();
    		b.putString(MENU, menuKey);
    		DishListFragment dlf = new DishListFragment();
    		dlf.setArguments(b);
    		mInstances.put(menuKey, dlf);
    		return dlf;
    	}
    }
    
    
    public static void refresh() {
    	
    	for (String key : mInstances.keySet()) {
    		mInstances.get(key).mListAdapter.notifyDataSetChanged();
    		Log.d(DLF, "DishListFragment: " + key + " refreshed");
    	}
    	
    	Log.d(DLF, "DishListFragments refreshed");
    	
    }
    
    
    public static void clearAdapters() {
    	for (String key : mInstances.keySet()) {
    		mInstances.get(key).mListAdapter.clear();
    		Log.d(DLF, "DishListFragment: " + key + " adapter cleared");
    	}
    	Log.d(DLF, "DishListFragments cleared");
    }
    
    public static void setAdapters() {
    	for (String key : mInstances.keySet()) {
    		mInstances.get(key).setAdapter();
    		Log.d(DLF, "DishListFragment: " + key + " adapter set");
    	}
    	Log.d(DLF, "DishListFragments adapters set");
    }
    
    private void setAdapter() {
    	Log.d("DishListFrag", "menu key = " + mMenuKey);
    	mListAdapter = new MenuListAdapter((DishListActivity) getActivity(), 
    			R.layout.entree_row, MenuContent.mMealsMap.get(mMenuKey));
    	setListAdapter(mListAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }
        
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setRetainInstance(true);
        
        Bundle args = getArguments();
        if (args != null)
        	mMenuKey = args.getString(MENU);
        Log.d(DLF, mMenuKey);
        
        setAdapter();
        /*
        mMenuList = MenuContent.mMealsMap.get(mMenuKey);
        
        mListAdapter = new MenuListAdapter((DishListActivity) getActivity(),
                R.layout.entree_row,
                mMenuList);
                */
        
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	return inflater.inflate(R.layout.fragment_dish_list, container, false);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    }
    
    @Override
    public void onActivityCreated(Bundle ofJoy) {
    	super.onActivityCreated(ofJoy);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState
                .containsKey(STATE_ACTIVATED_POSITION)) {
            //setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        
    }

    @Override
    public void onResume() {
    	super.onResume();
    	if (mMenuList != null)
    		setListAdapter(mListAdapter);
    }
    
    public static void doSetListAdapter() {
    	for (String instance : mInstances.keySet()) {
    		DishListFragment dlf = mInstances.get(instance);
    		dlf.setListAdapter(dlf.mListAdapter);
    	}
    	
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }
    
    @Override
    public void onDestroy() {
    	//mInstances.remove(mMenuKey);
    	super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(MenuContent.mMealsMap.get(mMenuKey).get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
    
}
