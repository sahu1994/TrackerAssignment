package com.google.transporttracker.data;

public class User {

    public Double lat;
    public Double lang;
    public String number;

    public User(Double lat, Double lang, String number) {
        this.lat = lat;
        this.lang = lang;
        this.number = number;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLang() {
        return lang;
    }

    public void setLang(Double lang) {
        this.lang = lang;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


}
