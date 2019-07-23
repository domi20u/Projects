package de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data;

import android.os.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by felix on 04.01.2017.
 */

public class DataDrink extends Data{

    private String devent_p_key;
    private double devent_volumen;
    private Date devent_timestamp;

    //Constructors
    public DataDrink(){
        super();
    }

    public DataDrink(String devent_p_key, double devent_volumen, Date devent_timestamp) {
        super();
        this.devent_p_key = devent_p_key;
        this.devent_volumen = devent_volumen;
        this.devent_timestamp = devent_timestamp;
    }

    public DataDrink(String devent_p_key, double devent_volumen, long devent_timestamp) {
        super();
        this.devent_p_key = devent_p_key;
        this.devent_volumen = devent_volumen;
        this.devent_timestamp = new Date(devent_timestamp);
    }

    public DataDrink(String devent_p_key, double devent_volumen, String devent_timeString){
        super();
        this.devent_p_key = devent_p_key;
        this. devent_volumen = devent_volumen;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        try {
            Date date = df.parse(devent_timeString);
            this.devent_timestamp = date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * getter, setter
     */
    public String getDevent_p_key() {
        return devent_p_key;
    }

    public double getDevent_volumen() {
        return devent_volumen;
    }

    public Date getDevent_timestamp() {
        return devent_timestamp;
    }

    public String getDevent_timeString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        return df.format(this.devent_timestamp);
    }

    public void setDevent_p_key(String devent_p_key) {
        this.devent_p_key = devent_p_key;
    }

    public void setDevent_volumen(double devent_volumen) {
        this.devent_volumen = devent_volumen;
    }

    public void setDevent_timestamp(Date devent_timestamp) {
        this.devent_timestamp = devent_timestamp;
    }

    public void setDevent_timestamp (long devent_timestamp){
        this.devent_timestamp = new Date(devent_timestamp);
    }

    public void setDevent_timestamp(String devent_timeString){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        try {
            Date date = df.parse(devent_timeString);
            this.devent_timestamp = date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return  String.format(Locale.GERMAN, "Patienten-ID:\t%s\t, Trinkvolumen:\t%.2f\t, Zeitstempel:\t%s",
                getDevent_p_key(),
                getDevent_volumen(),
                getDevent_timeString());
    }

    /**
     * Parcelable
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.devent_p_key);
        dest.writeDouble(this.devent_volumen);
        dest.writeLong(this.devent_timestamp != null ? this.devent_timestamp.getTime() : -1);
    }

    protected DataDrink(Parcel in) {
        super(in);
        this.devent_p_key = in.readString();
        this.devent_volumen = in.readDouble();
        long tmpDevent_timestamp = in.readLong();
        this.devent_timestamp = tmpDevent_timestamp == -1 ? null : new Date(tmpDevent_timestamp);
    }

    public static final Creator<DataDrink> CREATOR = new Creator<DataDrink>() {
        @Override
        public DataDrink createFromParcel(Parcel source) {
            return new DataDrink(source);
        }

        @Override
        public DataDrink[] newArray(int size) {
            return new DataDrink[size];
        }
    };
}
