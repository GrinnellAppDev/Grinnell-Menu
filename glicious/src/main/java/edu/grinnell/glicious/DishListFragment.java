package edu.grinnell.glicious;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.grinnell.glicious.menucontent.Entree;
import edu.grinnell.glicious.menucontent.MenuContent;

public class DishListFragment extends ListFragment{
	
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String MENU = "menu";
    private static final String DLF = "DishListFragment";
    private final static int ANIMATION_DURATION_FOOTER_SHOW = 120;
    private final static int ANIMATION_DURATION_FOOTER_HIDE = 150;
    private static HashMap<String, String> HOURS = new HashMap<String, String>();
    private ToggleButton mStar;
    private TextView mHours;
    private View mHoursFooter;
    private int mLastItem = 0;
    private boolean mIsAnimating = false;
    public FavoritesPrefs prefs;
    private AnimatorListenerAdapter mAnimatorListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mIsAnimating = false;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            mIsAnimating = true;
        }
    };


    private Callbacks mCallbacks = sDummyCallbacks;
    private static GregorianCalendar mDate;
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
    
    
    public static void refresh(GregorianCalendar currentDate) {

        mDate = currentDate;
    	for (String key : mInstances.keySet()) {
    		mInstances.get(key).mListAdapter.notifyDataSetChanged();
    		Log.d(DLF, "DishListFragment: " + key + " refreshed");
    	}
    	
    	Log.d(DLF, "DishListFragments refreshed");
    	
    }
    
    private void setAdapter() {
    	Log.d("DishListFrag", "menu key = " + mMenuKey);
    	mListAdapter = new MenuListAdapter((DishListActivity) getActivity(), 
    			R.layout.entree_row, mMenuList);
    	setListAdapter(mListAdapter);
    }


    private void hideFooter(View footer){
        footer.animate()
                .translationY(footer.getHeight())
                .setDuration(ANIMATION_DURATION_FOOTER_HIDE)
                .setListener(mAnimatorListenerAdapter);
    }

    private void showFooter(View footer){
        footer.animate()
                .translationY(0)
                .setDuration(ANIMATION_DURATION_FOOTER_SHOW)
                .setListener(mAnimatorListenerAdapter);
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

        prefs = new FavoritesPrefs(getActivity());

        HOURS.put("breakfast", "7am - 10am");
        HOURS.put("lunch", "11am - 2pm");
        HOURS.put("dinner", "5pm - 8pm");
        HOURS.put("breakfastW", "9am - 10am");
        HOURS.put("lunchW",  "11am - 1:30pm");
        HOURS.put("dinnerW", "5pm - 7pm");

        Bundle args = getArguments();
        if (args != null)
        	mMenuKey = args.getString(MENU);
        Log.d(DLF, mMenuKey);
        
        mMenuList = new ArrayList<Entree>(MenuContent.mMealsMap.get(mMenuKey));
        
        setAdapter();
        
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.fragment_dish_list, container, false);

        mHours = (TextView) v.findViewById(R.id.hours);
        mHoursFooter = v.findViewById(R.id.hours_footer);
        mStar = (ToggleButton) v.findViewById(R.id.fav_star);

        setHours(mHours, mMenuKey);

    	return v;
    }


//    public void setFavs(){
//
//        mStar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
//
//                if (isChecked)
//                    prefs.favoritesMap.put()
//            }
//        });
//
//    }


    public void setHours(TextView hours, String menukey){

        int day = mDate.get(Calendar.DAY_OF_WEEK);
        Log.v("DAY IS ", day + "");
        int[] weekend = {1,6,7};
        /*
           Make sure hours match with the given menu
         */
        if (Arrays.binarySearch(weekend, day) >= 0) {
            Log.v("WEEKEND 6, 7", day + "");
            if(!menukey.equals("dinner") && day == 6)
                hours.setText(HOURS.get(mMenuKey));
            else
                hours.setText(HOURS.get(mMenuKey + "W"));
        }
        else
            hours.setText(HOURS.get(mMenuKey));

    }
    
    @Override
    public void onStart() {
    	super.onStart();
    }
    
    @Override
    public void onActivityCreated(Bundle ofJoy) {
    	super.onActivityCreated(ofJoy);

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
//                hideFooter(mHoursFooter);


            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            if (!mIsAnimating) {
                if (mLastItem > firstVisibleItem)
                    showFooter(mHoursFooter);
                else if (mLastItem < firstVisibleItem)
                    hideFooter(mHoursFooter);
            }

                mLastItem = firstVisibleItem;
            }
        });
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
//        mCallbacks.onItemSelected(MenuContent.mMealsMap.get(mMenuKey).get(position).id);
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
    
    
}
