package com.example.rishav.destrangify;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rishav on 07-11-2015.
 */


@ParseClassName("_User")
public class People extends ParseUser{


    public ParseGeoPoint getCurLoc() {
        return getParseGeoPoint("curLoc");
    }

    public void setCurLoc(ParseGeoPoint curLoc) {
        put("curLoc",curLoc);
    }

    public boolean isLogged() {
        return getBoolean("logged");
    }

    public void setLogged(boolean logged) {
        put("logged",logged);
    }

    public boolean isLocVis() {
        return getBoolean("locVis");
    }

    public void setLocVis(boolean locVis) {
        put("locVis",locVis);
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        put("name",name);
    }

    public String getAddress() {
        return getString("address");
    }

    public void setAddress(String address) {
        put("address",address);
    }

    public JSONArray getMovies() {
        return getJSONArray("movies");
    }

    public void addMovie(String movie) {
        add("movies",movie);
    }

    public JSONArray getSongs() {
        return getJSONArray("songs");
    }

    public void addSong(String song) {
        add("songs", song);
    }

    public List<?> getFriends() {
        List<Object> friends = new ArrayList<Object>(getList("friends"));
        return friends;
    }

    public void addFriend(People friend) {
        add("friends",friend);
    }

}
