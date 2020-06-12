package com.example.haceasygo.View;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.haceasygo.Controller.MapControl;
import com.example.haceasygo.R;
import com.example.haceasygo.Controller.SearchFragment;
import com.google.android.material.navigation.NavigationView;

public class StartScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer = null;
    private Dialog myDialog;


    /** Create an loading opening page, load maps and database
     * @param  savedInstanceState object that contain the activity's previously saved state.
     *                           If the activity has never existed before, the value of the Bundle object is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen); //load activity view
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setLayoutDirection(drawer.LAYOUT_DIRECTION_RTL);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Replace part of the opening page with our own local map.
        MapControl mapsFragment = new MapControl();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.mainLayout, mapsFragment).commit();

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        myDialog = new Dialog(this);
    }


    /**
     * Handle when return key is pressed.
     * If the bar is opened - this function will close her.
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Handle the item that user select from navigation bar.
     *
     * @param item variable with data on the chosen item
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.help) {
            myDialog.setContentView(R.layout.popup_help);
        } else if (id == R.id.info) {
            myDialog.setContentView(R.layout.popup_info);
        } else if (id == R.id.contact) {
            myDialog.setContentView(R.layout.popup);
        }
        myDialog.dismiss();
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * When the user press on the search button.
     *
     * @param view activity view
     */
    public void originSearch(View view) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        SearchFragment searchFragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putBoolean(getString(R.string.is_origin), false);  //set up data that's required to the next activity.
        searchFragment.setArguments(args);

        transaction.replace(R.id.drawer_layout, searchFragment).addToBackStack(searchFragment
                .getClass().getName()
        ).commit();

        drawer.closeDrawer(GravityCompat.START);
    }


    /**
     * Close the pop up properly
     */
    public void exitPopUP(View view) {
        myDialog.cancel();
    }


    /**
     * When the user press on the make phone call icon from the navigation bar
     *
     * @param view activity view
     */
    public void makePhoneCall(View view) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:026291911"));
        startActivity(callIntent);
    }


    /**
     * When the user press on the create content icon from the navigation bar
     *
     * @param view activity view
     */
    public void sendMail(View view) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        //emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.developers_email));
        startActivity(emailIntent);
    }


    /**
     * When the user press on the go website icon from the navigation bar
     *
     * @param view activity view
     */
    public void goToWebsite(View view) {
        Intent openWebsite = new Intent(Intent.ACTION_VIEW);
        openWebsite.setData(Uri.parse(getString(R.string.web_site_url)));
        startActivity(openWebsite);
    }


    /**
     * When the user press on get help icon from the navigation bar
     *
     * @param view activity view
     */
    public void goToHelp(View view) {
        Intent openWebsite = new Intent(Intent.ACTION_VIEW);
        openWebsite.setData(Uri.parse(getString(R.string.youtube_app_demo)));
        startActivity(openWebsite);
    }
}

