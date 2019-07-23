package de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data;

import android.os.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by felix on 04.01.2017.
 */

public class DataPatient extends Data{

    private String p_mac;
    private String p_firstname;
    private String p_lastname;
    private Date p_birthdate;
    private boolean p_sex;
    private boolean p_enabled;
    private String p_key;


    //Constructors
    public DataPatient(){
        super();
    }

    public DataPatient(String p_mac, String p_firstname, String p_lastname, Date p_birthdate,
                       boolean p_sex, boolean p_enabled, String p_key) {
        super();
        this.p_mac = p_mac;
        this.p_firstname = p_firstname;
        this.p_lastname = p_lastname;
        this.p_birthdate = p_birthdate;
        this.p_sex = p_sex;
        this.p_enabled = p_enabled;
        this.p_key = p_key;
    }

    public DataPatient(String p_mac, String p_firstname, String p_lastname, long p_birthdate,
                       boolean p_sex, boolean p_enabled, String p_key) {
        super();
        this.p_mac = p_mac;
        this.p_firstname = p_firstname;
        this.p_lastname = p_lastname;
        this.p_birthdate = new Date(p_birthdate);
        this.p_sex = p_sex;
        this.p_enabled = p_enabled;
        this.p_key = p_key;
    }

    public DataPatient(String p_mac, String p_firstname, String p_lastname, String p_birthdateString,
                       boolean p_sex, boolean p_enabled, String p_key) {
        super();
        this.p_mac = p_mac;
        this.p_firstname = p_firstname;
        this.p_lastname = p_lastname;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        try {
            Date date = df.parse(p_birthdateString);
            this.p_birthdate = date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.p_sex = p_sex;
        this.p_enabled = p_enabled;
        this.p_key = p_key;
    }



    /**
     * getter, setter
     */
    public String getP_mac() {
        return p_mac;
    }

    public String getP_firstname() {
        return p_firstname;
    }

    public String getP_lastname() {
        return p_lastname;
    }

    public Date getP_birthdate() {
        return p_birthdate;
    }

    public String getP_birthdateString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        return df.format(this.p_birthdate);
    }

    public boolean isP_sex() {
        return p_sex;
    }

    public boolean isP_enabled() {
        return p_enabled;
    }

    public String getP_key() {
        return p_key;
    }

    public void setP_mac(String p_mac) {
        this.p_mac = p_mac;
    }

    public void setP_firstname(String p_firstname) {
        this.p_firstname = p_firstname;
    }

    public void setP_lastname(String p_lastname) {
        this.p_lastname = p_lastname;
    }

    public void setP_birthdate(Date p_birthdate) {
        this.p_birthdate = p_birthdate;
    }

    public void setP_birthdate(long p_birthdate){
        this.p_birthdate = new Date(p_birthdate);
    }

    public void setP_birthdate(String p_birthdateString){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        try {
            Date date = df.parse(p_birthdateString);
            this.p_birthdate = date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setP_sex(boolean p_sex) {
        this.p_sex = p_sex;
    }

    public void setP_enabled(boolean p_enabled) {
        this.p_enabled = p_enabled;
    }

    public void setP_key(String p_key) {
        this.p_key = p_key;
    }

    @Override
    public String toString() {
        String sex;
        if(isP_sex()){
            sex = "maennlich";
        } else{
            sex = "weiblich";
        }
        String enabled;
        if(isP_enabled()){
            enabled = "ja";
        } else{
          enabled = "nein";
        }
        return  String.format("Strohhalm-MAC:\t%s\t, Vorname:\t%s\t, Nachname:\t%s\t, Geburtsdatum:\t%s\t, " +
                "Geschlecht:\t%s\t, Aktiv:\t%s\t, Patientenkey:\t%s\t",
                getP_mac(),
                getP_firstname(),
                getP_lastname(),
                getP_birthdateString(),
                sex,
                enabled,
                getP_key());
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
        dest.writeString(this.p_mac);
        dest.writeString(this.p_firstname);
        dest.writeString(this.p_lastname);
        dest.writeLong(this.p_birthdate != null ? this.p_birthdate.getTime() : -1);
        dest.writeByte(this.p_sex ? (byte) 1 : (byte) 0);
        dest.writeByte(this.p_enabled ? (byte) 1 : (byte) 0);
        dest.writeString(this.p_key);
    }

    protected DataPatient(Parcel in) {
        super(in);
        this.p_mac = in.readString();
        this.p_firstname = in.readString();
        this.p_lastname = in.readString();
        long tmpP_birthdate = in.readLong();
        this.p_birthdate = tmpP_birthdate == -1 ? null : new Date(tmpP_birthdate);
        this.p_sex = in.readByte() != 0;
        this.p_enabled = in.readByte() != 0;
        this.p_key = in.readString();
    }

    public static final Creator<DataPatient> CREATOR = new Creator<DataPatient>() {
        @Override
        public DataPatient createFromParcel(Parcel source) {
            return new DataPatient(source);
        }

        @Override
        public DataPatient[] newArray(int size) {
            return new DataPatient[size];
        }
    };
}
