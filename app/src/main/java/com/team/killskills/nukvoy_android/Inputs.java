package com.team.killskills.nukvoy_android;

import android.os.Parcel;
import android.os.Parcelable;

import static android.content.ContentValues.TAG;

/**
 * Created by Sacha on 12/12/2017.
 */

public class Inputs {

    private String days;
    private String iataCode;

    //Constructor
    public Inputs(String days, String iataCode){

        this.days=days;
        this.iataCode = iataCode;
        Logger.logError("Inputs",days.toString());
        Logger.logError("Inputs",iataCode.toString());
    }


    //Getter and Setter
    public String getDays() {
        return days;
    }

    public String getIataCode() {
        return iataCode;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

}
