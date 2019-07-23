package de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data;

import android.os.Parcel;
import android.os.Parcelable;



/**
 * Created by felix on 04.01.2017.
 */

public class Data implements Parcelable{

    //Constructors
    public Data(){
    }

    protected Data(Parcel in) {
    }

    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

}
