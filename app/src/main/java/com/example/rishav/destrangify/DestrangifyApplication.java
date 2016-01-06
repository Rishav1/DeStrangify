package com.example.rishav.destrangify;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseUser;

/**
 * Created by Rishav on 07-12-2015.
 */
public class DestrangifyApplication extends Application{

    private static double MAX_RADIUS=3.0;
    @Override
    public void onCreate(){
        super.onCreate();
        Parse.enableLocalDatastore(getApplicationContext());
        ParseUser.registerSubclass(People.class);
        Parse.initialize(getApplicationContext(),"Ow14FldbDpsG0Zy9AuUqnnD3VJx3C7efwDSjyuqv","QnChezP8PYLRRrJcyD00yfLXuixHKz17Ivqaom6g");
    }
    public static double getMaxRadius() {
        return MAX_RADIUS;
    }
}
