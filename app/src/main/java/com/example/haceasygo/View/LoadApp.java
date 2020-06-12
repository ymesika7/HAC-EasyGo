package com.example.haceasygo.View;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.haceasygo.R;


public class LoadApp extends AppCompatActivity {
    private static int SPLASH_TIME = 2500; //This is 2.5 seconds


    /** Create an loading opening page, load maps and database
     * @param  savedInstanceState object that contain the activity's previously saved state.
     *                           If the activity has never existed before, the value of the Bundle object is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_app);

        ImageView imageView = (ImageView)findViewById(R.id.animation);

        //loading animation
        Animation animation= AnimationUtils.loadAnimation(LoadApp.this,R.anim.rotate);
        imageView.startAnimation(animation);

        //Start timer and take action after the timer ends
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Moving to next page
                Intent mySuperIntent = new Intent(LoadApp.this, StartScreen.class);
                startActivity(mySuperIntent);
                finish();
            }
        }, SPLASH_TIME);
    }
}