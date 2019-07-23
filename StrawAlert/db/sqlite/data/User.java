package de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by felix on 09.01.2017.
 */

public class User implements Parcelable {

    public static final String PREFGROUP_USER = "PREFGROUP_USER";
    public static final String PREF_USERID = "PREF_USERID";
    public static final String PREF_PASSWORD = "PREF_PASSWORD";
    public static final String PREF_ADMIN = "PREF_ADMIN";
    public static final String PREF_IS_LOGGED_IN = "PREF_IS_LOGGED_IN";

    private String u_id;
    private String u_password;
    private boolean u_admin;

    // Constructors
    public User(String u_id) {
        this.u_id = u_id;
    }

    public User(String u_id, boolean u_admin) {
        this.u_id = u_id;
        this.u_admin = u_admin;
    }

    public User(String u_id, String u_password, boolean u_admin) {
        this.u_id = u_id;
        this.u_password = u_password;
        this.u_admin = u_admin;
    }

    /**
     * Parcelable
     */
    protected User(Parcel in) {
        u_id = in.readString();
        u_password = in.readString();
        u_admin = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(u_id);
        dest.writeString(u_password);
        dest.writeByte((byte) (u_admin ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    /**
     * getter/setter
     */
    public String getU_id() {
        return u_id;
    }

    public String getU_password() {
        return u_password;
    }

    public boolean isU_admin() {
        return u_admin;
    }

    public void setU_id(String u_id) {
        this.u_id = u_id;
    }

    public void setU_admin(boolean u_admin) {
        this.u_admin = u_admin;
    }

    public void setU_password(String u_password) {
        this.u_password = u_password;
    }
}
