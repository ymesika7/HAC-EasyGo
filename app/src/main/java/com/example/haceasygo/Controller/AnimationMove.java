package com.example.haceasygo.Controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.example.haceasygo.Model.Database.DatabaseHelper;
import com.example.haceasygo.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnimationMove {
    private static final int TILT = 15;
    private static final int BEARING = 40;

    private List<LatLng> markers = new ArrayList<LatLng>();
    protected Context context;
    private GoogleMap googleMap;
    private DatabaseHelper db;
    private boolean difFloor = false;

    /** Constructor
     * @param  map map reference from the Navigation screen activity
     * @param context Navigation screen activity context
     * @param origin user origin
     * @param destination user required destination
     */
    public void startAnimation(GoogleMap map, Context context, String origin, String destination) {
        googleMap = map;
        this.context = context;
        db = new DatabaseHelper(context);

        // Set Direction list by coordinates
        addDefaultLocations(origin, destination);
        // Draw instruction(by polyline) over the map
        drawPol();
        // Change camera position and angle
        cameraPos();
    }

    /**
     * Set list of coordinates to draw the polyline over the map
     * @param origin user origin
     * @param destination user required destination
     */
    private void addDefaultLocations(String origin, String destination) {
        List<LatLng> list = new ArrayList<LatLng>();

        // Check if we navigate from the origin to the current floor exit
        if(destination == null) {
            list = db.getCoorListDB(String.valueOf(origin), "");
            List<LatLng> tempList = db.getCoorListDB(Integer.toString(
                    (Character.getNumericValue(origin.charAt(0))*10)
                            + Character.getNumericValue(origin.charAt(1)))
                            , context.getString(R.string.type_building) );
            Collections.reverse(tempList);
            list.addAll(tempList);
        }
        // Check if we navigate from the current floor entrance to the destination
        else if(origin == null) {
            list = db.getCoorListDB(Integer.toString(
                    (Character.getNumericValue(destination.charAt(0))*10)
                            + Character.getNumericValue(destination.charAt(1)))
                            , context.getString(R.string.type_building) );
            List<LatLng> tempList = db.getCoorListDB(String.valueOf(destination) , "");
            Collections.reverse(tempList);
            list.addAll(tempList);

        }
        // Navigate in the same floor
        else {
            list = db.getCoorListDB(String.valueOf(origin), "");
            List<LatLng> tempList = db.getCoorListDB(String.valueOf(destination), "");
            Collections.reverse(tempList);
            list.addAll(tempList);

            List<Integer> indexesToDelete = new ArrayList<Integer>();
            for (int i = 0; i < list.size(); i++) { //remove duplicates
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(i).equals(list.get(j))) {
                        indexesToDelete.add(i);
                        indexesToDelete.add(j);
                    }
                }
            }
            Collections.sort(indexesToDelete);

            for (int i = indexesToDelete.size() - 1; i >= 0; i--) {
                int temp = indexesToDelete.get(i);
                list.remove(temp);
            }
        }
        markers.clear();
        markers = list;
    }

    /**
     * Draw the instruction(by polyline) over the map
     */
    public void drawPol() {
        //If we navigate in different floors
        if(difFloor) {
            for(int i=0; i<(markers.size()/2)-1; i++) {
                drawPolylineWithArrowEndcap(markers.get(i), markers.get(i + 1));
            }
        }
        // If we navigate from origin to destination in the same floor
        else{
            for(int i=0; i<markers.size()-1; i++) {
                drawPolylineWithArrowEndcap(markers.get(i), markers.get(i + 1));
            }
        }
    }

    /**
     * Set up the view of the map to navigation mode(change target, bearing, tilt and zoom)
     */
    public void cameraPos(){
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(markers.get(0))
                        .bearing(BEARING)
                        .tilt(TILT)
                        .zoom(21.f)
                        .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);
    }


    /**
     * Draw the polyline over the map with adding arrow end cap icon for easy visual direction
     */
    public void drawPolylineWithArrowEndcap(LatLng fromLatLng, LatLng toLatLng) {

        int arrowColor = context.getResources().getColor(R.color.arrow_color);
        int lineColor =  context.getResources().getColor(R.color.line_color);


        BitmapDescriptor endCapIcon = getEndCapIcon(arrowColor, false);

        // have googleMap create the line with the arrow endcap
        PolylineOptions polyLineOptions = new PolylineOptions()
                .geodesic(true)
                .color(lineColor)
                .width(20)
                .startCap(new RoundCap())
                .endCap(new CustomCap(endCapIcon,googleMap.getCameraPosition().zoom * 2.f))
                .jointType(JointType.ROUND)
                .add(fromLatLng, toLatLng);

        Polyline polyline = googleMap.addPolyline(polyLineOptions);
        polyline.setZIndex(1000);
    }

    /**
     * Set up the arrow end cap icon for easy visual direction
     */
    public BitmapDescriptor getEndCapIcon(int color, boolean smallSize) {
        Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.ic_arrow);

        // set the bounds to the whole image
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        // overlay (multiply) your color over the white icon
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);

        // create a bitmap from the drawable
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // render the bitmap on a blank canvas
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        if(smallSize)
            bitmap = Bitmap.createScaledBitmap(bitmap, 56, 56, false);

        // create a BitmapDescriptor from the new bitmap
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
