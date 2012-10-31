package edu.grinnell.glicious;

import edu.grinnell.glicious.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class DishDetailActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(DishDetailFragment.ARG_ENTREE_ID,
                    getIntent().getStringExtra(DishDetailFragment.ARG_ENTREE_ID));
            DishDetailFragment fragment = new DishDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.dish_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        	
        	Intent upIntent = new Intent(this, DishListActivity.class);
        	upIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP 
        			| Intent.FLAG_ACTIVITY_SINGLE_TOP);
            NavUtils.navigateUpTo(this, upIntent);
            
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
