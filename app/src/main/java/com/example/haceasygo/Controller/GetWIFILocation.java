package com.example.haceasygo.Controller;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;

import com.example.haceasygo.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class GetWIFILocation {
    private final static int N = 80; // 1000 = 1sec
    private static String url;
    private Handler mHandler = new Handler();
    private String requierdStr ="";

    /** Constructor
     * @param context activity context
     */
    public GetWIFILocation(Context context){
        url = context.getString(R.string.wifi_server_url);
    }

    /**
     * Get the user current access point by unique codec
     */
    public String getString() {
        getWebsite();
        if(requierdStr.equalsIgnoreCase("9971"))
            return "411";
        else if(requierdStr.equalsIgnoreCase("9973"))
            return "412";
        else if(requierdStr.equalsIgnoreCase("9972"))
            return "413";
        else if(requierdStr.equalsIgnoreCase("9935"))
            return "311";
        else if(requierdStr.equalsIgnoreCase("9950"))
            return "312";
        else
            return "412";
    }

    /**
     * Get the user current floor
     */
    public String getFloor(){
        getWebsite();
        if(requierdStr.equalsIgnoreCase("9971")
                || requierdStr.equalsIgnoreCase("9972")
                || requierdStr.equalsIgnoreCase("9973"))
            return "41";
        else if(requierdStr.equalsIgnoreCase("9935")
                || requierdStr.equalsIgnoreCase("9950"))
            return "31";
        else
            return "41";

    }

    /**
     * Get from the WiFI server the current access point the user connected
     */
    private void getWebsite(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    Document doc = Jsoup.connect(url).get();
                    String title = doc.text();

                    requierdStr += title ;

                } catch (IOException e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }
            }
        }).start();
    }

}

