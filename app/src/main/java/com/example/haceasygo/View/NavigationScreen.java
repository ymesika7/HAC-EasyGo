package com.example.haceasygo.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.haceasygo.Model.Database.DatabaseHelper;
import com.example.haceasygo.Controller.MapControl;
import com.example.haceasygo.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NavigationScreen extends AppCompatActivity implements View.OnClickListener {
    private static final int TILT = 30;
    private static final int BEARING = 44;

    private static MapControl mapsFragment = new MapControl();

    private List<String> commandsToUser = new ArrayList<String>();
    private List<LatLng> directionList = new ArrayList<LatLng>();

    private TextView mForegroundLayout;
    private TextView mMoveFloorTxt;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private ImageButton mCancelBtn;
    private ImageButton mMoveFloor;
    private ImageButton reachDestination;
    private String origin;
    private String destination;

    private DatabaseHelper db;
    private boolean difFloor = false;
    private boolean moveFloorFlag = false;
    private int index = 0;

    /** Create an loading opening page, load maps and database
     * @param  savedInstanceState object that contain the activity's previously saved state.
     *                           If the activity has never existed before, the value of the Bundle object is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_screen);

        // Set up view elements
        mForegroundLayout = (TextView) findViewById(R.id.directions_background);
        mMoveFloorTxt = (TextView) findViewById(R.id.moveFloorText);
        mNextButton = findViewById(R.id.button_next);
        reachDestination = findViewById(R.id.reachDest);
        mPreviousButton = findViewById(R.id.button_prev);
        mCancelBtn = findViewById(R.id.button_exit);
        mMoveFloor = findViewById(R.id.moveFloor);

        // Set up buttons listeners
        mNextButton.setOnClickListener(this);
        mPreviousButton.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);
        mMoveFloor.setOnClickListener(this);

        mPreviousButton.setVisibility(View.INVISIBLE);

        // Set up arguments for the map view
        Intent intent=getIntent();
        origin = intent.getStringExtra(getString(R.string.origin));
        destination = intent.getStringExtra(getString(R.string.destination));
        Bundle args = new Bundle();
        args.putBoolean(getString(R.string.ready_to_nav), true);
        // Check if different floors
        if(origin.charAt(0) != destination.charAt(0)) {
            difFloor = true;
            args.putString(getString(R.string.origin), origin);
            args.putString(getString(R.string.destination), null);
        }else {
            args.putString(getString(R.string.origin), origin);
            args.putString(getString(R.string.destination), destination);
        }

        mapsFragment.setArguments(args);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.NavScreenMap, mapsFragment).commit();

        db = new DatabaseHelper(this);
        // Set Direction list by coordinates
        setDirectionsFirstFloor();
        showDirection();
    }

    /**
     * Handle when return key is pressed.
     */
    @Override
    public void onBackPressed() {
        exitFragmentDialog();
    }

    /**
     * Exit from the Navigation screen dialog.
     */
    private void exitFragmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NavigationScreen.this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(getString(R.string.ui_end_navigation_popup))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ui_end_navigation_no)
                        , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.ui_end_navigation_yes)
                        , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent myIntent = new Intent(NavigationScreen.this
                                , StartScreen.class);
                        startActivity(myIntent);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Listener, when the user press on UI buttons
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Listener : Recognize when user press on the next instruction button
            case R.id.button_next:
                if(commandsToUser.size() > index +1) {
                    mPreviousButton.setVisibility(View.VISIBLE);
                    index++;
                    mForegroundLayout.setText(commandsToUser.get(index));
                    if(index + 1 >= commandsToUser.size()) {
                        mNextButton.setVisibility(View.INVISIBLE);
                        if(difFloor){
                            mMoveFloorTxt.setVisibility(View.VISIBLE);
                            mMoveFloor.setVisibility(View.VISIBLE);
                        }
                        else
                            reachDestination.setVisibility(View.VISIBLE);
                    }
                }

                if(moveFloorFlag){
                    mMoveFloorTxt.setVisibility(View.INVISIBLE);
                    mMoveFloor.setVisibility(View.INVISIBLE);
                }
                return;
            // Listener : Recognize when user press on the back instruction button
            case R.id.button_prev:
                if(index > 0) {
                    reachDestination.setVisibility(View.INVISIBLE);
                    mNextButton.setVisibility(View.VISIBLE);
                    index--;
                    mForegroundLayout.setText(commandsToUser.get(index));

                    if(index == 0)
                        mPreviousButton.setVisibility(View.INVISIBLE);
                }
                if(!moveFloorFlag) {
                    mMoveFloorTxt.setVisibility(View.INVISIBLE);
                    mMoveFloor.setVisibility(View.INVISIBLE);
                }else if(moveFloorFlag && index == 0) {
                    mMoveFloorTxt.setVisibility(View.VISIBLE);
                    mMoveFloor.setVisibility(View.VISIBLE);
                }
                return;
            // Listener : Recognize when user press on the exit button
            case R.id.button_exit:
                exitFragmentDialog();
                return;
            // Listener : Recognize when user press on the next floor button
            case R.id.moveFloor:
                index = 0;
                if(!moveFloorFlag) {
                    mNextButton.setVisibility(View.VISIBLE);
                    mPreviousButton.setVisibility(View.INVISIBLE);

                    mMoveFloorTxt.setText(getString(R.string.ui_back_floor)+ origin.charAt(0));

                    if (origin.charAt(0) < destination.charAt(0))
                        mMoveFloor.setImageResource(R.mipmap.ic_arrow_down);
                    else
                        mMoveFloor.setImageResource(R.mipmap.ic_arrow_up);

                    difFloor = false;
                    moveFloorFlag = true;
                    mapsFragment.endNav(destination);
                    setDirectionsSecondFloor();
                } else { // If we want to go back to previous floor.
                    mNextButton.setVisibility(View.VISIBLE);
                    mPreviousButton.setVisibility(View.INVISIBLE);

                    mMoveFloorTxt.setVisibility(View.INVISIBLE);
                    mMoveFloor.setVisibility(View.INVISIBLE);
                    moveFloorFlag = false;
                    difFloor = true;
                    mapsFragment.startNav();
                    setDirectionsFirstFloor();
                }
                showDirection();
                return;
        }
    }

    /**
     * Set direction list by coordintes for the first floor
     */
    private void setDirectionsFirstFloor() {
        directionList.clear();
        directionList = db.getCoorListDB(String.valueOf(origin), "");
        List<LatLng> tempList = db.getCoorListDB(Integer.toString((
                Character.getNumericValue(origin.charAt(0))*10)
                + Character.getNumericValue(origin.charAt(1))),getString(R.string.type_building) );
        Collections.reverse(tempList);
        directionList .addAll(tempList); //hallway to destination
    }

    /**
     * Set direction list by coordintes for the second floor
     */
    private void setDirectionsSecondFloor() {
        directionList.clear();
        directionList = db.getCoorListDB(String.valueOf(origin), "");
        List<LatLng> tempList = db.getCoorListDB(String.valueOf(destination), "");
        Collections.reverse(tempList);
        directionList .addAll(tempList); //hallway to destination

        List<Integer> indexesToDelete = new ArrayList<Integer>();
        for (int i = 0; i < directionList .size(); i++) { //remove duplicates
            for (int j = i + 1; j < directionList .size(); j++) {
                if (directionList .get(i).equals(directionList .get(j))) {
                    indexesToDelete.add(i);
                    indexesToDelete.add(j);
                }
            }
        }
        Collections.sort(indexesToDelete);

        for (int i = indexesToDelete.size() - 1; i >= 0; i--) {
            int temp = indexesToDelete.get(i);
            directionList .remove(temp);
        }
    }

    /**
     * Get the next instruction by string
     */
    private void getInstruction(LatLng A, LatLng B, LatLng C) {
        int FirstDistanceInMeters = (int)SphericalUtil.computeDistanceBetween(A,B);
        Double FirstHeadingRotation = SphericalUtil.computeHeading(A,B); //get rotation direction
        Double SecondHeadingRotation = SphericalUtil.computeHeading(A,C); //get rotation direction

        String str = "";
        if(FirstDistanceInMeters > 2)
            str = ( getString(R.string.ui_move_forward) + FirstDistanceInMeters
                    + getString(R.string.ui_metres));

        if(SecondHeadingRotation > FirstHeadingRotation)
            str += ( getString(R.string.ui_turn_right));
        else
            str += ( getString(R.string.ui_turn_left));

        commandsToUser.add(str);
    }

    /**
     * Show the next instruction on the screen
     */
    private void showDirection() {
        commandsToUser = new ArrayList<String>();
        // Set up the instructions by strings for the user
        for (int i = 0; i < directionList.size() - 2; i++)
            getInstruction(directionList.get(i), directionList.get(i+1), directionList.get(i+2));

        int listSize = directionList.size();
        // Check if the user want to navigate between foors
        if(difFloor) {
            commandsToUser.add(getString(R.string.ui_continue_moving)
                    + (int) SphericalUtil.computeDistanceBetween(directionList.get(listSize - 1)
                        , directionList.get(listSize - 2))
                    + getString(R.string.ui_metres_and_then)
                    + ((origin.charAt(0) > destination.charAt(0)) ? getString(R.string.ui_down)
                        : getString(R.string.ui_up))
                    + getString(R.string.ui_to_floor)
                    + (destination.charAt(0)));
            mMoveFloorTxt.setText(getString(R.string.ui_take_me_to_floor) + destination.charAt(0));
            mMoveFloorTxt.setVisibility(View.INVISIBLE);
            if(origin.charAt(0) > destination.charAt(0))
                mMoveFloor.setImageResource(R.mipmap.ic_arrow_down);
            else
                mMoveFloor.setImageResource(R.mipmap.ic_arrow_up);
            mMoveFloor.setVisibility(View.INVISIBLE);
        }
        else
            commandsToUser.add( getString(R.string.ui_the_destination_is_in)+
                    (int)SphericalUtil.computeDistanceBetween(directionList.get(listSize -1)
                            , directionList.get(listSize -2)) +getString(R.string.ui_metres_ahead));

        mForegroundLayout.setText(commandsToUser.get(index));
    }
}
