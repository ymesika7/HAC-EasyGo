package com.example.haceasygo.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.haceasygo.Controller.MapControl;
import com.example.haceasygo.R;
import com.example.haceasygo.Controller.SearchFragment;

import java.util.ArrayList;

public class SetRoute extends AppCompatActivity {
    private String[] items;
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;
    private EditText editText;
    private String latLng;
    boolean destOrOrigin = true;
    private static String destination = "";
    private static String origin_description = "";
    private static String destination_description = "";
    private static String origin =  "";

    /** Create an loading opening page, load maps and database
     * @param  savedInstanceState object that contain the activity's previously saved state.
     *                           If the activity has never existed before, the value of the Bundle object is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_route);

        // Get arguments to initialize activity
        Intent intent=getIntent();
        destination = intent.getStringExtra(getString(R.string.destination));
        destination_description = intent.getStringExtra(getString(R.string.destination_description));
        latLng = intent.getStringExtra(getString(R.string.lat_lng));

        // Set up arguments for the map view
        MapControl mapsFragment = new MapControl();
        Bundle args = new Bundle();
        args.putBoolean(getString(R.string.start_nav), false);
        args.putBoolean(getString(R.string.focus_on), true);
        args.putString(getString(R.string.destination), destination);
        args.putString(getString(R.string.lat_lng), latLng);

        mapsFragment.setArguments(args);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.mainLayout, mapsFragment).commit();

        manager.addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        FragmentManager fm = getSupportFragmentManager();
                        if (fm != null) {
                            int backStackCount = fm.getBackStackEntryCount();
                            if (backStackCount == 0) {
                                    updateData();
                            }
                        }
                    }
                });
    }

    /**
     * Handle when return key is pressed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateData();
    }

    /**
     * Update the data on the UI elements
     */
    private void updateData(){
        TextView editTextView = (TextView) findViewById(R.id.textview_origin);
        editTextView.setText(origin_description);
        editTextView = (TextView) findViewById(R.id.textview_destination);
        editTextView.setText(destination_description);
    }

    /**
     * Swap the origin with the destination
     */
    private void swapDestinationOrigin(View v) {
        swap();
        updateData();
    }

    /**
     * Swap the origin with the destination, include the descriptions
     */
    private void swap() {
        String temp_sec ,temp = origin;
        origin = destination;
        destination = temp;
        temp_sec = origin_description;
        origin_description = destination_description;
        destination_description = temp_sec;
    }

    /**
     * Search for user origin
     */
    public void originSearch (View view){
        destOrOrigin = true;
        searchSites();
    }

    /**
     * Search for user required destination
     */
    public void destinationSearch (View view){
        destOrOrigin = false;
        searchSites();
    }

    /**
     * Start searching after origin/destination with updated arguments
     */
    private void searchSites(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        SearchFragment searchFragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putBoolean(getString(R.string.is_origin), true);
        searchFragment.setArguments(args);
        transaction.replace( R.id.main, searchFragment ).addToBackStack(searchFragment.getClass().getName()).commit();
    }

    /**
     * Set up user current origin
     * @param _origin user origin
     * @param _originDescription  user origin description
     */
    public static void setOrigin(String _origin, String _originDescription) {
        origin = _origin;
        origin_description = _originDescription;
    }

    /**
     * Check if the user insert his origin and destination.
     * If yes, update arguments and start navigation, otherwise pop up toast message.
     */
    public void searchRoute(View view) {
        if (origin == "") {
            Toast.makeText(this, getString(R.string.toast_choose_origin), Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination == "") {
            Toast.makeText(this, getString(R.string.toast_choose_destination), Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.equalsIgnoreCase(origin)) {
            Toast.makeText(this, getString(R.string.toast_destination_equal_origin), Toast.LENGTH_SHORT).show();
            return;
        }

        origin = getSiteNumber(origin);
        destination  = getSiteNumber(destination);

        Intent intent = new Intent(SetRoute.this, NavigationScreen.class);
        intent.putExtra(getString(R.string.origin), origin);
        intent.putExtra(getString(R.string.destination), destination);
        startActivity(intent);

    }

    /**
     * Get the id number of the site
     * @param str the full string on the origin/destination
     * @return the number of the site as described in the full string
     */
    private String getSiteNumber(String str) {
        int temp = 0;
        for(int i = 0; i < str.length(); i++)
            if(Character.isDigit(str.charAt(i))) {
                temp = i;
                break;
            }
        return str.substring(temp,str.length());
    }
}


