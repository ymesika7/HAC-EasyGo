package com.example.haceasygo.Controller;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haceasygo.Controller.GetWIFILocation;
import com.example.haceasygo.Controller.RecyclerTouchListener;
import com.example.haceasygo.Model.Database.Adapter.SearchAdapter;
import com.example.haceasygo.Model.Database.DatabaseHelper;
import com.example.haceasygo.R;
import com.example.haceasygo.View.SetRoute;
import com.google.android.gms.maps.model.LatLng;
import com.mancj.materialsearchbar.MaterialSearchBar;

public class SiteSearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SearchAdapter adapter;
    private boolean isOrigin;
    private MaterialSearchBar materialSearchBar;
    private static DatabaseHelper database;


    /**
     * Default constructor
     */
    public SiteSearchFragment(){}


    /**
     * Inflate the layout for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        return v;
    }

    /**
     * Create the opening screen of the fragment.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle bundle = getArguments();
        String searchBy = bundle.getString(getString(R.string.what_to_do));
        boolean toSearch = bundle.getBoolean(getString(R.string.to_search));
        isOrigin =  bundle.getBoolean(getString(R.string.is_origin));

        // Setup search bar
        materialSearchBar=(MaterialSearchBar)getView().findViewById(R.id.search_bar);
        if(isOrigin)
            materialSearchBar.setPlaceHolder(getString(R.string.ui_choose_origin));
        else
            materialSearchBar.setPlaceHolder(getString(R.string.ui_choose_destination));

        // Setup view
        recyclerView = (RecyclerView)getView().findViewById(R.id.recycler_search);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Setup database
        database = new DatabaseHelper(getActivity());

        // Get user location by the Wi-Fi access points
        GetWIFILocation getWIFILocation = new GetWIFILocation(getContext());

        if(toSearch)
            // Search after specific location
            startSearch(searchBy);
        else{
            if(searchBy.equalsIgnoreCase(getString(R.string.type_local))) {
                // Search by closing locations
                adapter = new SearchAdapter(getActivity(), database.getListDB("SiteByWIFI",getWIFILocation.getString(), ""));
            }
            else
                // Search by group
                adapter = new SearchAdapter(getActivity(),database.getListDB("SitesByType", getWIFILocation.getFloor(), searchBy));
            recyclerView.setAdapter(adapter);
        }

        // Setup the search listeners
        setupListeners();
    }


    /**
     *  Setup the search listeners
     */
    private void setupListeners() {
        // Listener : Recognize user location selection
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener
                (getActivity().getApplicationContext(), recyclerView,
                        new RecyclerTouchListener.ClickListener() {
                            @Override
                            public void onClick(View view, int position) {
                                // Get site number
                                String site = adapter.getNumber(position);
                                // Get site description
                                String holder = adapter.getDescription(position);
                                // Get the coordinate of the site
                                LatLng latLng = database.getLatLng(site);
                                double latitude = latLng.latitude;
                                double longitude = latLng.longitude;
                                String temp = Double.toString(latitude) + "," + Double.toString(longitude);

                                // If we search for the origin(starting point)
                                if(isOrigin) {
                                    SetRoute.setOrigin(site, holder);

                                    FragmentManager manager = getFragmentManager();
                                    if (manager.getBackStackEntryCount() > 0) {
                                        manager.popBackStack(manager.getBackStackEntryAt
                                                        (manager.getBackStackEntryCount()-2).getId(),
                                                manager.POP_BACK_STACK_INCLUSIVE);
                                    }
                                }
                                // If we search for the destination(end point)
                                else {
                                    Intent myIntent = new Intent(getActivity(), SetRoute.class);
                                    myIntent.putExtra(getString(R.string.destination_description), holder);
                                    myIntent.putExtra(getString(R.string.destination), site);
                                    myIntent.putExtra(getString(R.string.lat_lng), temp);
                                    startActivity(myIntent);
                                }

                            }

                            @Override
                            public void onLongClick(View view, int position) {

                            }
                        }));
    }


    /**
     *  Search for specific location
     *
     * @param byNumber The requierd id number of the choosen object
     */
    private void startSearch(String byNumber) {
        adapter = new SearchAdapter(getActivity(), database.getListDB("SitesByNumber", byNumber, ""));
        recyclerView.setAdapter(adapter);
    }

}