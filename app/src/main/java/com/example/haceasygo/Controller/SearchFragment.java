package com.example.haceasygo.Controller;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haceasygo.Model.Database.Adapter.SearchAdapter;
import com.example.haceasygo.Model.Database.DatabaseHelper;
import com.example.haceasygo.Model.Database.Sites;
import com.example.haceasygo.R;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SearchAdapter adapter;
    private Boolean isOrigin;
    private MaterialSearchBar materialSearchBar;
    private List<String> stringSuggestList;
    private List<String> currentSuggest;
    private static DatabaseHelper database;


    /**
     * Default constructor
     */
    public SearchFragment() {}


    /**
     * Inflate the layout for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        return v;
    }


    /**
     * Create the opening screen of the fragment.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the search model
        materialSearchBar=(MaterialSearchBar)getView().findViewById(R.id.search_bar);

        // Check if we want to search for the origin or to the destination
        isOrigin =  getArguments().getBoolean(getString(R.string.is_origin));
        if(isOrigin)
            materialSearchBar.setPlaceHolder(getString(R.string.ui_choose_origin));
        else
            materialSearchBar.setPlaceHolder(getString(R.string.ui_choose_destination));

        // Setup view
        recyclerView = (RecyclerView)getView().findViewById(R.id.recycler_search);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Setup the app database
        database = new DatabaseHelper(getActivity());

        // Setup suggestions list for the user
        loadSuggestList();
        currentSuggest = stringSuggestList;

        // Setup the search listeners
        setupListeners();

        starterList();
    }


    /**
     *  Setup the search listeners
     */
    private void setupListeners(){
        // Listener : Update the suggest list while the user is typing
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentSuggest = new ArrayList<>();
                for(String search:stringSuggestList){
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        currentSuggest.add(search);
                }
                materialSearchBar.setLastSuggestions(currentSuggest);
            }
        });

        // Listener : Recognize if the user press on the search button
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!enabled) {
                    adapter = new SearchAdapter(getActivity().getBaseContext(), database.getListDB("Sites", "", ""));
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                SiteSearch(String.valueOf(text),true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        // Listener : Recognize if the user chosen location from the suggestion list
        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                String site = currentSuggest.get(position);
                materialSearchBar.setText(site);
                BaseInputConnection inputConnection = new BaseInputConnection(getView(), true);
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        // Listener : Recognize if the user chosen to search from a group
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(
                getActivity().getApplicationContext(), recyclerView
                , new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String site = adapter.getType(position);
                SiteSearch(site,false);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }


    /**
     * Setup the groups data
     */
    private void starterList(){
        List<Sites> starterList = new ArrayList<Sites>();
        if(isOrigin){
            starterList.add(new Sites(getString(R.string.type_local),getString(R.string.setup_number)));
        }
        starterList.add(new Sites(getString(R.string.type_class), getString(R.string.setup_number)));
        starterList.add(new Sites(getString(R.string.type_wc), getString(R.string.setup_number)));
        starterList.add(new Sites(getString(R.string.type_computer_lab), getString(R.string.setup_number)));
        starterList.add(new Sites(getString(R.string.type_printer), getString(R.string.setup_number)));

        adapter = new SearchAdapter(getActivity(),starterList );
        recyclerView.setAdapter(adapter);
    }


    /**
     * This function call to the next search fragment
     *
     * @param mission type of the object the user chosen
     * @param search boolean, true if user search for specific location ("class 3102"),
     *               false if the user search for a group
     */
    private void SiteSearch(String mission, Boolean search){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        SiteSearchFragment siteSearchFragment = new SiteSearchFragment();
        Bundle args = new Bundle();
        args.putString(getString(R.string.what_to_do),mission);
        args.putBoolean(getString(R.string.to_search), search);
        args.putBoolean(getString(R.string.is_origin), isOrigin);
        siteSearchFragment.setArguments(args);

        // Set the next search fragment over the current layout (depend on the father activity)
        if(isOrigin)
            transaction.replace( R.id.main, siteSearchFragment ).addToBackStack(siteSearchFragment.getClass().getName()).commit();
        else
            transaction.replace( R.id.drawer_layout, siteSearchFragment ).addToBackStack(siteSearchFragment.getClass().getName()).commit();

    }


    /**
     * Setup suggestion list for the user
     */
    private void loadSuggestList(){
        List<Sites> tempList = database.getListDB("Number", "", "");
        stringSuggestList = new ArrayList<>();
        for(Sites i : tempList)
            stringSuggestList.add(i.getNumber());

        materialSearchBar.setLastSuggestions(stringSuggestList);
    }

}

