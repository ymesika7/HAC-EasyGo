package com.example.haceasygo.Controller;

import androidx.fragment.app.Fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.haceasygo.Model.CustomMapTileProvider;
import com.example.haceasygo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;


public class MapControl extends Fragment implements OnMapReadyCallback,
        View.OnClickListener{
    private final static int NOT_SET = 42000; //high randum number

    private static final int PERMISSION_REQUEST_CODE = 1;   //For location access

    private final static int minValue = -1; //min value to choose floor
    private final static int maxValue = 4;  //max value to choose floor.

    private static GoogleMap map;
    private static AnimationMove animationMove = new AnimationMove();
    private ImageButton btnFloor;  //btn opn numberPicker to choose floor to show on map
    private ImageButton btnClearFloor; //btn clear floor from the map
    private TextView floorTextView;
    public int floorLevel = NOT_SET; // for start, NOT_SET = 4200, just to indicate to not show any floor.
    public int building = NOT_SET; // for start, NOT_SET = 4200, just to indicate to not show any floor.
    private NumberPicker numberPicker; //choose floor to show on map
    private boolean readyToNav = false;
    private boolean focusOn = false;
    private String origin;
    private String destination;
    private String latLng;

    /** Create an the required data for the map fragment
     * @param  savedInstanceState object that contain the activity's previously saved state.
     *                           If the activity has never existed before, the value of the Bundle object is null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up UI variables
        View v = inflater.inflate( R.layout.fragment_map_control, container, false );  // Inflate the layout for this fragment
        btnFloor = (ImageButton) v.findViewById(R.id.floor);
        btnClearFloor = (ImageButton) v.findViewById(R.id.clearFloor);
        numberPicker = (NumberPicker) v.findViewById(R.id.np);
        floorTextView = (TextView) v.findViewById(R.id.textfloor);

        // Set up floor number picker
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(maxValue - minValue);
        numberPicker.setValue(0 - minValue);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int index) {
                return Integer.toString(index + minValue);
            }
        });

        // Set up listeners
        btnFloor.setOnClickListener(this);
        btnClearFloor.setOnClickListener(this);
        numberPicker.setOnClickListener(this);

        // Get arguments
        Bundle bundle = getArguments();
        if(bundle != null) {
            focusOn = bundle.getBoolean(getString(R.string.focus_on));
            readyToNav = bundle.getBoolean(getString(R.string.ready_to_nav));
        }
        // If the user already choose destination, focus on this place
        if(focusOn) {
            destination = bundle.getString(getString(R.string.destination));
            latLng = bundle.getString(getString(R.string.lat_lng));
            destination = getSiteNumber(destination);

            // Set up dta by destination
            building = (int)destination.charAt(1) - '0';
            floorLevel = (int)destination.charAt(0) - '0';
        }

        // If the user want to start navigation (choose origin and destination)
        if(readyToNav) {
            // Make irrelevant UI buttons to be invisible
            btnFloor.setVisibility(View.INVISIBLE);
            btnClearFloor.setVisibility(View.INVISIBLE);
            numberPicker.setVisibility(View.INVISIBLE);
            floorTextView.setVisibility(View.INVISIBLE);

            origin = bundle.getString(getString(R.string.origin));
            destination = bundle.getString(getString(R.string.destination));
            origin = getSiteNumber(origin);
            if(destination != null)
                destination = getSiteNumber(destination);
        }

        return v;
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


    /**
     * Set up the view for the map
     */
    @Override
    public void onViewCreated( View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

    }

    /**
     * Listener, when the user press on UI buttons
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Listener : if the user press on the required floor on the floor number picker.
            case R.id.floor:
                if (floorLevel == NOT_SET) {
                    floorLevel = 0;
                }
                btnClearFloor.setVisibility(View.VISIBLE);
                floorTextView.setVisibility(View.INVISIBLE);
                numberPicker.setVisibility(View.VISIBLE);
                btnFloor.setVisibility(View.INVISIBLE);
                //Set a value change listener for NumberPicker
                numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        //Display the newly selected number from picker
                        floorLevel = newVal + minValue;
                    }
                });

                System.out.println(map.getCameraPosition());
                return;

            // Listener : if the user press on the floor number picker button.
            case R.id.np:
                btnClearFloor.setVisibility(View.INVISIBLE);
                numberPicker.setVisibility(View.INVISIBLE);
                btnFloor.setVisibility(View.VISIBLE);
                setUpMap();
                return;

            // Listener : if the user press on the clear floor button.
            case R.id.clearFloor:
                btnClearFloor.setVisibility(View.INVISIBLE);
                floorTextView.setVisibility(View.INVISIBLE);
                numberPicker.setVisibility(View.INVISIBLE);
                btnFloor.setVisibility(View.VISIBLE);
                floorTextView.setText("");
                floorLevel = NOT_SET;
                setUpMap();
                return;
        }
    }

    /**
     * Request the user premission to access his gps data.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }


    /**
     * When the map ready to show
     * @param googleMap google map session
     */
    public void onMapReady(GoogleMap googleMap) {
        // Set up map details
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NONE); //Type NONE = empty map
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        map.setMinZoomPreference(18.0f); // Set a preference for minimum zoom (Zoom out).
        map.setMaxZoomPreference(21.0f); // Set a preference for minimum zoom (Zoom out).

        // Set up map required zone
        LatLngBounds bounds = new LatLngBounds(new LatLng(31.78237817,35.22021534), new LatLng( 31.78381934, 35.22218804)); //Bounde of the Collage
        map.setLatLngBoundsForCameraTarget(bounds); //Set the map to focus on the center of the Collage
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(31.783101112981832,35.22097337990999), 19)); //move the camera to the center of the Collage

        // Set up the current data on the map
        setUpMap();

        // Set up map listener
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                numberPicker.setVisibility(View.INVISIBLE);
                if(!readyToNav)
                    btnFloor.setVisibility(View.VISIBLE);
            }
        });

        // If the user want to start navigation (choose origin and destination)
        if(readyToNav)
            startNav();
    }

    /**
     *  Set up(draw) the current data on the map
     */
    public void setUpMap() {
        map.clear();
        floorTextView.setVisibility(View.VISIBLE);

        // If the user not choose specific building to focus on
        if(building == NOT_SET) {
            map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets(), NOT_SET, NOT_SET)));
            // If the user choose specific floor to focus on
            if (floorLevel != NOT_SET) {
                map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets(), floorLevel, 1)));
                map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets(), floorLevel, 5)));
                floorTextView.setText(Integer.toString(floorLevel));
            }
        }
        // If the user choose specific building to focus on
        else {
            map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets(), NOT_SET, NOT_SET)));
            if (floorLevel != NOT_SET) {
                map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets(), floorLevel, building)));
                floorTextView.setText(Integer.toString(floorLevel));
            }

            // If the user already choose destination, focus on this place
            if (focusOn) {
                String[] latlong = latLng.split(",");
                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 21)); //move the camera to the choosen destination
            }
        }
    }

    /**
     *  When the user want to start navigation (choose origin and destination)
     */
    public void startNav(){
        setupDataForNav(origin);
        floorTextView.setVisibility(View.INVISIBLE);
        animationMove.startAnimation(map, getActivity(), origin, destination);
    }

    /**
     *  When the user choose to navigate between different floors/buildings
     */
    public void endNav(String destination) {
        this.destination = destination;
        setupDataForNav(destination);
        floorTextView.setVisibility(View.INVISIBLE);
        animationMove.startAnimation(map, getActivity(), null, destination);
        this.destination = null;
    }

    /**
     *  Set up the required data for the navigation
     */
    private void setupDataForNav(String str) {
        map.clear();
        building = (int)str.charAt(1) - '0';
        floorLevel = (int)str.charAt(0) - '0';
        setUpMap();
    }


}
