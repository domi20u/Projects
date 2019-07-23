package de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;

import java.io.File;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.User;

/**
 * Created by felix on 30.12.2016.
 */

public class SQLiteManager extends SQLiteOpenHelper{

    //Database Version
    public static final int VERSION_DB = 1;

    //Database Name
    public static final String NAME_DB = "straw.db";

    //Table Names
    public static final String TABLE_PATIENT = "patient_list";

    public static final String TABLE_USER = "user_list";

     public static final String TABLE_DRINKEVENT = "drink_events";

    //Table Column Names
    public static final String PATIENT_MAC = "p_mac";
    public static final String PATIENT_FIRSTNAME = "p_firstname";
    public static final String PATIENT_LASTNAME = "p_lastname";
    public static final String PATIENT_BIRTHDATE = "p_birthdate";
    public static final String PATIENT_SEX = "p_sex";
    public static final String PATIENT_ENABLED = "p_enabled";
    public static final String PATIENT_SYNCED = "p_synced";
    public static final String PATIENT_KEY = "p_key";


    public static final String USER_ID = "u_id";
    public static final String USER_PW = "u_pw";
    public static final String USER_ADMINRIGHTS = "u_admin";


    public static final String DRINKEVENT_ID = "devent_id";
    public static final String DRINKEVENT_PATIENT_KEY = "devent_p_key";
    public static final String DRINKEVENT_VOLUMEN = "devent_volumen";
    public static final String DRINKEVENT_SYNCED = "devent_synced";
    public static final String DRINKEVENT_TIMESTAMP = "devent_timestamp";

    //Drop statements (delete tables)
    private static final String DROP_STATEMENT_PATIENT = "DROP TABLE " + TABLE_PATIENT + ";";


    private static final String DROP_STATEMENT_USER = "DROP TABLE " + TABLE_USER + ";";


    private static final String DROP_STATEMENT_DRINKEVENT = "DROP TABLE " + TABLE_DRINKEVENT + ";";

    //Create statements
    private static final String CREATE_STATEMENT_PATIENT = "CREATE TABLE " + TABLE_PATIENT + " (" +
            PATIENT_MAC + " STRING, " +
            PATIENT_FIRSTNAME + " STRING, " +
            PATIENT_LASTNAME + " STRING, " +
            PATIENT_BIRTHDATE + " STRING, " +
            PATIENT_SEX + " INTEGER DEFAULT 0, " +
            PATIENT_ENABLED + " INTEGER DEFAULT 0, " +
            PATIENT_SYNCED + " STRING DEFAULT 'false', " +
            PATIENT_KEY + " STRING PRIMARY KEY)";


    private static final String CREATE_STATEMENT_USER = "CREATE TABLE " + TABLE_USER + " (" +
            USER_ID + " STRING PRIMARY KEY, " +
            USER_PW + " STRING, " +
            USER_ADMINRIGHTS + " INTEGER DEFAULT 0)";


    private static final String CREATE_STATEMENT_DRINKEVENTS = "CREATE TABLE " + TABLE_DRINKEVENT + " (" +
            DRINKEVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DRINKEVENT_PATIENT_KEY + " STRING, " +
            DRINKEVENT_VOLUMEN + " DOUBLE, " +
            DRINKEVENT_SYNCED + " STRING DEFAULT 'false', " +
            DRINKEVENT_TIMESTAMP + " STRING)";



    private SQLiteDatabase db;
    private SharedPreferences sharedPreferences;


    //Constructor
    public SQLiteManager (Context context){
        super(context, NAME_DB, null, VERSION_DB);
        //sharedPreferences = context.getSharedPreferences(User.PREFGROUP_USER, Context.MODE_PRIVATE);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STATEMENT_DRINKEVENTS);
        db.execSQL(CREATE_STATEMENT_PATIENT);
        db.execSQL(CREATE_STATEMENT_USER);
    }

    /**
     * Drops all tables and recreates them.
     */
    public void dropAndRecreateAllTables() {
        if(open()) {
            db.execSQL(DROP_STATEMENT_DRINKEVENT);
            db.execSQL(DROP_STATEMENT_PATIENT);
            db.execSQL(DROP_STATEMENT_USER);
            db.execSQL(CREATE_STATEMENT_DRINKEVENTS);
            db.execSQL(CREATE_STATEMENT_PATIENT);
            db.execSQL(CREATE_STATEMENT_USER);
            close();
        }
    }

    public void drobAndRecreateDrinkEvent(){
        if (open()){
            db.execSQL(DROP_STATEMENT_DRINKEVENT);
            db.execSQL(CREATE_STATEMENT_DRINKEVENTS);
            close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAndRecreateAllTables();
    }

    /**
     * Trys to open a writable database connection.
     * @return <b>true</b> if the connection was opened successfully, <b>false</b> otherwise.
     */
    public boolean open(){
        try {
            db = this.getWritableDatabase();
            return true;
        }
        catch (SQLiteException e){
            Log.d(this.getClass().getName(),"Unable to Open Database" );
            return false;
        }
    }

    /**
     * Closes the writable database connection afterwards
     */
    @Override
    public void close() {
        if(db != null) {
            db.close();
            db = null; }
    }

    /**
     * Returns the actual size of the database in bytes
     * @return size of database in bytes
     */
    public long getDBSize(){
        open();
        long size =  new File(db.getPath()).length();
        close();
        return size;
    }


    /**
     * Flushing Data to the internal database
     */
    public void flushPatientData(DataPatient dataPatient){
        if (open()) {
            SQLiteStatement statement = db.compileStatement(String.format("INSERT OR IGNORE INTO %s VALUES (?,?,?,?,?,?,?,?);", TABLE_PATIENT));
            db.beginTransaction();
            statement.clearBindings();
            statement.bindString(1, dataPatient.getP_mac());
            statement.bindString(2, dataPatient.getP_firstname());
            statement.bindString(3, dataPatient.getP_lastname());
            statement.bindString(4, dataPatient.getP_birthdateString());
            statement.bindNull(5);
            statement.bindNull(6);
            statement.bindString(7, "false");
            statement.bindString(8, dataPatient.getP_key());
            statement.execute();

            ContentValues values = new ContentValues();
            int flag1 = (dataPatient.isP_sex())? 1:0;
            values.put(PATIENT_SEX, flag1);
            int flag2 = (dataPatient.isP_enabled())? 1:0;
            values.put(PATIENT_ENABLED, flag2);
            db.update(TABLE_PATIENT, values, PATIENT_KEY + " = ?", new String[] {dataPatient.getP_key()});

            db.setTransactionSuccessful();
            db.endTransaction();
            Log.d("DEBUG", "flushingPATIENT");
            close();
        }
    }

    public void flushPatientData(List<DataPatient> patients){
        if (open()) {
            SQLiteStatement statement = db.compileStatement(String.format("INSERT OR IGNORE INTO %s VALUES (?,?,?,?,?,?,?,?);", TABLE_PATIENT));
            db.beginTransaction();
            for (int i=0; i<patients.size(); i++) {
                statement.clearBindings();
                statement.bindString(1, patients.get(i).getP_mac());
                statement.bindString(2, patients.get(i).getP_firstname());
                statement.bindString(3, patients.get(i).getP_lastname());
                statement.bindString(4, patients.get(i).getP_birthdateString());
                statement.bindNull(5);
                statement.bindNull(6);
                statement.bindString(7, "true");
                statement.bindString(8, patients.get(i).getP_key());
                statement.execute();

                ContentValues values = new ContentValues();
                int flag1 = (patients.get(i).isP_sex()) ? 1 : 0;
                values.put(PATIENT_SEX, flag1);
                int flag2 = (patients.get(i).isP_enabled()) ? 1 : 0;
                values.put(PATIENT_ENABLED, flag2);
                db.update(TABLE_PATIENT, values, PATIENT_KEY + " = ?", new String[]{patients.get(i).getP_key()});
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            Log.d("DEBUG", "flushingPATIENT");
            close();
        }
    }

    public void flushDrinkData(DataDrink dataDrink){
        if (open()) {
            SQLiteStatement statement = db.compileStatement(String.format("INSERT OR IGNORE INTO %s VALUES (?,?,?,?,?);", TABLE_DRINKEVENT));
            db.beginTransaction();
            statement.clearBindings();
            statement.bindNull(1);
            statement.bindString(2, dataDrink.getDevent_p_key());
            statement.bindDouble(3, dataDrink.getDevent_volumen());
            statement.bindString(4, "false");
            statement.bindString(5, dataDrink.getDevent_timeString());
            statement.execute();
            db.setTransactionSuccessful();
            db.endTransaction();
            Log.d("DEBUG", "flushingDRINKDATA");
            close();
        }
    }

    public void flushDrinkData(List<DataDrink> drinks){
        if(open()){
            String helper = "DELETE FROM " + TABLE_DRINKEVENT +";";
            db.execSQL(helper);
            db.close();
        }
        if (open()) {
            db.beginTransaction();
            for (int i=0; i<drinks.size(); i++) {
                SQLiteStatement statement = db.compileStatement(String.format("INSERT OR IGNORE INTO %s VALUES (?,?,?,?,?);", TABLE_DRINKEVENT));
                statement.clearBindings();
                statement.bindNull(1);
                statement.bindString(2, drinks.get(i).getDevent_p_key());
                statement.bindDouble(3, drinks.get(i).getDevent_volumen());
                statement.bindString(4, "true");
                statement.bindString(5, drinks.get(i).getDevent_timeString());
                statement.execute();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            Log.d("DEBUG", "flushingDRINKDATA");
            close();
        }
    }

    public void flushUserData(User user){
        if (open()) {
            SQLiteStatement statement = db.compileStatement(String.format("INSERT OR IGNORE INTO %s VALUES (?,?,?);", TABLE_USER));
            db.beginTransaction();
            statement.clearBindings();
            statement.bindString(1, user.getU_id());
            statement.bindString(2, user.getU_password());
            statement.bindNull(3);
            statement.execute();

            ContentValues values = new ContentValues();
            int flag = (user.isU_admin())? 1:0;
            values.put(USER_ADMINRIGHTS, flag);
            db.update(TABLE_USER, values, USER_ID + " = ?", new String[] {user.getU_id()});

            db.setTransactionSuccessful();
            db.endTransaction();
            Log.d("DEBUG", "flushingUSER");
            close();
        }
    }

    /**
     * Retrieves a list of all drink_events to the specified key of table Drinkevent
     * @param key
     */
    public List<DataDrink> getDrinkEventList (String key){
        open();
        List<DataDrink> drinkList = new ArrayList<DataDrink>();
        String sqlQuery = String.format("select * from %s where %s = ? order by %s",
                TABLE_DRINKEVENT, DRINKEVENT_PATIENT_KEY, DRINKEVENT_TIMESTAMP);
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{key});
        if (cursor.moveToFirst()) {
            do {
                DataDrink data = new DataDrink(
                        cursor.getString(1),  //patient_key
                        cursor.getDouble(2),     //volumen
                        cursor.getString(4)   //timestamp
                );
                //adding data to list
                drinkList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return drinkList;
    }

    /**
     * Retrieves a list of a certain patient to the specified key of table Patient
     * a list with only one element (list as every return should be consistent)
     * @param key
     */
    public List<DataPatient> getPatient (String key){
        open();
        List<DataPatient> patientList = new ArrayList<DataPatient>();
        String sqlQuery = String.format("select * from %s where %s = ?",
                TABLE_PATIENT, PATIENT_KEY);
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{key});
        if(cursor.moveToFirst()){
            do {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
                try {
                    Date date = df.parse(cursor.getString(3));
                    Boolean flag1 = (cursor.getInt(4) == 1) ? true : false;
                    Boolean flag2 = (cursor.getInt(5) == 1) ? true : false;

                    DataPatient patient = new DataPatient(
                            cursor.getString(0),    //mac
                            cursor.getString(1),    //firstname
                            cursor.getString(2),    //lastname
                            date,                   //birthdate
                            flag1,                  //sex
                            flag2,                  //enabled
                            cursor.getString(7)     //key
                    );
                    patientList.add(patient);
                } catch (ParseException e) {
                    e.printStackTrace();
                    cursor.close();
                    close();
                    return null;
                }
            } while(cursor.moveToNext());

        }
        cursor.close();
        close();
        return patientList;
    }


    /**
     * Retrieves a list of patientData to the specified name
     * Distinguishes between different inputs (with(out) space) for search input
     * @param userinput
     */
    public List<DataPatient> getPatientList (String userinput){
        open();
        String name = userinput.trim();
        List<DataPatient> patientList = new ArrayList<DataPatient>();
        CharSequence space = " ";
        if(name.contains(space)){
            String[] names = name.split(" ");
            String sqlQuery = String.format("select * from %s where %s like ? and %s like ?",
                        TABLE_PATIENT, PATIENT_FIRSTNAME, PATIENT_LASTNAME);
            Cursor cursor = db.rawQuery(sqlQuery, new String[]{names[0], names[1]});
            if (cursor.moveToFirst()) {
                do {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
                    try {
                        Date date = df.parse(cursor.getString(3));
                        Boolean flag1 = (cursor.getInt(4) == 1) ? true : false;
                        Boolean flag2 = (cursor.getInt(5) == 1) ? true : false;

                        DataPatient patient = new DataPatient(
                                cursor.getString(0),    //mac
                                cursor.getString(1),    //firstname
                                cursor.getString(2),    //lastname
                                date,                   //birthdate
                                flag1,                  //sex
                                flag2,                  //enabled
                                cursor.getString(7)     //key
                        );
                        patientList.add(patient);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        cursor.close();
                        close();
                        return null;
                    }
                } while (cursor.moveToNext());

            }

            cursor.close();
            close();
            return patientList;
        }else {
            String name2 = name + "%";
            String sqlQuery = "select * from " + TABLE_PATIENT + " where " + PATIENT_FIRSTNAME +
                    " like ? or " + PATIENT_LASTNAME + " like ?";
            //String sqlQuery = String.format("select * from %s where %s like %s or %s like %s",
            //        TABLE_PATIENT, PATIENT_FIRSTNAME, name2, PATIENT_LASTNAME, name2);
            Cursor cursor = db.rawQuery(sqlQuery, new String[]{name2, name2});
            if (cursor.moveToFirst()) {
                do {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
                    try {
                        Date date = df.parse(cursor.getString(3));
                        Boolean flag1 = (cursor.getInt(4) == 1) ? true : false;
                        Boolean flag2 = (cursor.getInt(5) == 1) ? true : false;

                        DataPatient patient = new DataPatient(
                                cursor.getString(0),    //mac
                                cursor.getString(1),    //firstname
                                cursor.getString(2),    //lastname
                                date,                   //birthdate
                                flag1,                  //sex
                                flag2,                  //enabled
                                cursor.getString(7)     //key
                        );
                        patientList.add(patient);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        cursor.close();
                        close();
                        return null;
                    }
                } while (cursor.moveToNext());

            }

            cursor.close();
            close();
            return patientList;
        }
    }

    /**
     * Retrieves a list of all Patients
     */
    public List<DataPatient> getAllPatients (){
        open();
        List<DataPatient> patientList = new ArrayList<DataPatient>();
        String sqlQuery = String.format("select * from %s order by %s collate nocase, %s collate nocase",
                TABLE_PATIENT, PATIENT_LASTNAME, PATIENT_FIRSTNAME);
        Cursor cursor = db.rawQuery(sqlQuery, null);
        if(cursor.moveToFirst()){
            do {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
                try {
                    Date date = df.parse(cursor.getString(3));
                    Boolean flag1 = (cursor.getInt(4) == 1) ? true : false;
                    Boolean flag2 = (cursor.getInt(5) == 1) ? true : false;

                    DataPatient patient = new DataPatient(
                            cursor.getString(0),    //mac
                            cursor.getString(1),    //firstname
                            cursor.getString(2),    //lastname
                            date,                   //birthdate
                            flag1,                  //sex
                            flag2,                  //enabled
                            cursor.getString(7)     //key
                    );
                    patientList.add(patient);
                } catch (ParseException e) {
                    e.printStackTrace();
                    cursor.close();
                    close();
                    return null;
                }
            } while(cursor.moveToNext());

        }
        cursor.close();
        close();
        return patientList;
    }

    /**
     * Retrieves a list of Patients which haven't been checked since 05:00
     * @return
     */
    public List<DataPatient> getMissingPatients (){
        open();
        Date currentDate = new Date();
        currentDate.setHours(5);
        currentDate.setMinutes(0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        String date = df.format(currentDate);
        List<DataPatient> patientList = new ArrayList<DataPatient>();
        String sqlQuery = String.format("select * from %s where %s = 1 and %s not in (select distinct %s from %s where %s >=?) order by %s collate nocase, %s collate nocase;",
                TABLE_PATIENT, PATIENT_ENABLED, PATIENT_KEY, DRINKEVENT_PATIENT_KEY, TABLE_DRINKEVENT, DRINKEVENT_TIMESTAMP, PATIENT_LASTNAME, PATIENT_FIRSTNAME);
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{date});
        if(cursor.moveToFirst()){
            do {
                try {
                    Date date2 = df.parse(cursor.getString(3));
                    Boolean flag1 = (cursor.getInt(4) == 1) ? true : false;
                    Boolean flag2 = (cursor.getInt(5) == 1) ? true : false;

                    DataPatient patient = new DataPatient(
                            cursor.getString(0),    //mac
                            cursor.getString(1),    //firstname
                            cursor.getString(2),    //lastname
                            date,                   //birthdate
                            flag1,                  //sex
                            flag2,                  //enabled
                            cursor.getString(7)     //key
                    );
                    patientList.add(patient);
                } catch (ParseException e) {
                    e.printStackTrace();
                    cursor.close();
                    close();
                    return null;
                }
            } while(cursor.moveToNext());
        }
        cursor.close();
        close();
        return patientList;
    }

    /**
     * Retrieves a list of Patients whose average drink volume is below the given threshold
     * @param threshold
     * @return
     */
    public List<DataPatient> getRedPatients (int threshold){
        open();
        List<DataPatient> patientList = new ArrayList<DataPatient>();
        String sqlQuery = String.format("select distinct * from %s where %s = 1 and %s in(" +
                        "select %s from %s group by %s having avg(%s)<%d) order by %s collate nocase, %s collate nocase;",
                TABLE_PATIENT, PATIENT_ENABLED, PATIENT_KEY, DRINKEVENT_PATIENT_KEY, TABLE_DRINKEVENT,
                DRINKEVENT_PATIENT_KEY, DRINKEVENT_VOLUMEN, threshold, PATIENT_LASTNAME, PATIENT_FIRSTNAME);

        Cursor cursor = db.rawQuery(sqlQuery, null);
        if(cursor.moveToFirst()){
            do {
                try {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
                    Date date = df.parse(cursor.getString(3));
                    Boolean flag1 = (cursor.getInt(4) == 1) ? true : false;
                    Boolean flag2 = (cursor.getInt(5) == 1) ? true : false;

                    DataPatient patient = new DataPatient(
                            cursor.getString(0),    //mac
                            cursor.getString(1),    //firstname
                            cursor.getString(2),    //lastname
                            date,                   //birthdate
                            flag1,                  //sex
                            flag2,                  //enabled
                            cursor.getString(7)     //key
                    );
                    patientList.add(patient);
                } catch (ParseException e) {
                    e.printStackTrace();
                    cursor.close();
                    close();
                    return null;
                }
            } while(cursor.moveToNext());
        }
        cursor.close();
        close();
        return patientList;
    }



    /**
     * Retrieves data of type user to the specified id (unused)
     * @param id
     * @return
     */
    public User getUser (String id){
        open();
        User user;
        String sqlQuery = String.format("select * from %s where %s = ?",
                TABLE_USER, USER_ID);
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{id});
        if (cursor.moveToFirst()) {
            Boolean flag = (cursor.getInt(2) == 1) ? true : false;
            user = new User(
                    cursor.getString(0),    //user_id
                    cursor.getString(1),    //password
                    flag                    //admin
            );
            cursor.close();
            close();
            return user;
        }
        cursor.close();
        close();
        return null;
    }


    /**
     *  Check for empty lists
     */
    public boolean isPatientListEmpty(){
        open();
        String sqlQuery = String.format("select * from %s",
                TABLE_PATIENT);
        Cursor cursor = db.rawQuery(sqlQuery, null);
        if(!cursor.moveToFirst()){
            cursor.close();
            close();
            return true;
        }else {
            cursor.close();
            close();
            return false;
        }
    }

    public boolean isDrinkEventListEmpty(){
        open();
        String sqlQuery = String.format("select * from %s",
                TABLE_DRINKEVENT);
        Cursor cursor = db.rawQuery(sqlQuery, null);
        if(!cursor.moveToFirst()){
            cursor.close();
            close();
            return true;
        }else {
            cursor.close();
            close();
            return false;
        }
    }

    public boolean isDrinkEventListEmpty(String key){
        open();
        String sqlQuery = String.format("select * from %s where %s =?",
                TABLE_DRINKEVENT, DRINKEVENT_PATIENT_KEY);
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{key});
        if(!cursor.moveToFirst()){
            cursor.close();
            close();
            return true;
        }else {
            cursor.close();
            close();
            return false;
        }
    }



    /**
     * Changes the status of retrievability
     * @param key
     */
    public void setPatientEnabled (String key){
        if(open()){
            String helper = "UPDATE " + TABLE_PATIENT + " SET " + PATIENT_ENABLED + " = 1 WHERE " + PATIENT_KEY + " = '" + key + "';";
            db.execSQL(helper);
            db.close();
        }
    }

    public void setPatientDisabled (String key){
        if(open()){
            String helper = "UPDATE " + TABLE_PATIENT + " SET " + PATIENT_ENABLED + " = 0 WHERE " + PATIENT_KEY + " = '" + key + "';";
            db.execSQL(helper);
            db.close();
        }
    }

    public boolean getPatientStatus(String key){
        open();
        String helper = "false";
        String sqlQuery1 = String.format("select %s from %s where %s = ?",
                PATIENT_ENABLED ,TABLE_PATIENT,PATIENT_KEY, key);
        Cursor cursor = db.rawQuery(sqlQuery1, new String[]{helper});
        if(!cursor.moveToFirst()){
            Boolean flag2 = (cursor.getInt(5) == 1) ? true : false;
            if(flag2){
                cursor.close();
                close();
                return true;
            } else return false;
        }else return false;
    }


    /**
     * Changes the status of Patient/Drinkevent to synced
     * @param key
     */
    public void setPatientSynced (String key){
        if(open()){
            String helper = "UPDATE " + TABLE_PATIENT + " SET " + PATIENT_SYNCED + " = 'true' WHERE " + PATIENT_KEY + " = '" + key + "';";
            db.execSQL(helper);
            db.close();
        }
    }

    public void setDrinkEventSynced (int id){
        if(open()){
            String helper = "UPDATE " + TABLE_DRINKEVENT + " SET " + DRINKEVENT_SYNCED + " = 'true' WHERE " + DRINKEVENT_ID + " = " + id + ";";
            db.execSQL(helper);
            db.close();
        }
    }


    /**
     * Compose JSON out of SQLite data
     */
    public String composeJSONpatient (){
        open();
        String helper = "false";
        ArrayList <HashMap<String, Object>> wordlist = new ArrayList<HashMap<String, Object>>();
        String sqlQuery = String.format("select * from %s where %s = ?",
                TABLE_PATIENT, PATIENT_SYNCED);
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{helper});
        if(cursor.moveToFirst()){
            do {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(PATIENT_MAC, cursor.getString(0));
                map.put(PATIENT_FIRSTNAME, cursor.getString(1));
                map.put(PATIENT_LASTNAME, cursor.getString(2));
                map.put(PATIENT_BIRTHDATE, cursor.getString(3));
                map.put(PATIENT_SEX, cursor.getInt(4));
                map.put(PATIENT_ENABLED, cursor.getInt(5));
                map.put(PATIENT_KEY, cursor.getString(7));
                wordlist.add(map);
            } while(cursor.moveToNext());
        }
        cursor.close();
        close();
        Gson gson = new GsonBuilder().create();
        return gson.toJson(wordlist);
    }

    public String composeJSONdrinkevent (){
        open();
        String helper = "false";
        ArrayList <HashMap<String, Object>> wordlist = new ArrayList<HashMap<String, Object>>();
        String sqlQuery = String.format("select * from %s where %s = ?",
                TABLE_DRINKEVENT, DRINKEVENT_SYNCED);
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{helper});
        if(cursor.moveToFirst()){
            do {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(DRINKEVENT_ID, cursor.getInt(0));
                map.put(DRINKEVENT_PATIENT_KEY, cursor.getString(1));
                map.put(DRINKEVENT_VOLUMEN, cursor.getDouble(2));
                map.put(DRINKEVENT_TIMESTAMP, cursor.getString(4));
                wordlist.add(map);
            } while(cursor.moveToNext());
        }
        cursor.close();
        close();
        Gson gson = new GsonBuilder().create();
        return gson.toJson(wordlist);
    }


    /**
     * Checks whether all local data is synced
     */
    public boolean patientSynced(){
        open();
        String helper = "false";
        String sqlQuery1 = String.format("select * from %s where %s = ?",
                TABLE_PATIENT, PATIENT_SYNCED);
        Cursor cursor = db.rawQuery(sqlQuery1, new String[]{helper});
        if(!cursor.moveToFirst()){
            cursor.close();
            close();
            return true;
        }else return false;
    }

    public boolean drinkEventSynced(){
        open();
        String helper = "false";
        String sqlQuery1 = String.format("select * from %s where %s = ?",
                TABLE_DRINKEVENT, DRINKEVENT_SYNCED);
        Cursor cursor = db.rawQuery(sqlQuery1, new String[]{helper});
        if(!cursor.moveToFirst()){
            cursor.close();
            close();
            return true;
        }else return false;
    }


    public String syncStatus(){
        if(drinkEventSynced() && patientSynced()){
            return "Local data is synced to remote";
        }else{
            return "Synchronisation needed";
        }
    }

    public void setSyncStatus(){
        if(open()){
            String helper = "UPDATE " + TABLE_PATIENT + " SET " + PATIENT_SYNCED + " = 'false' WHERE " + PATIENT_SYNCED + " = 'true';";
            db.execSQL(helper);
            db.close();
        }
    }

    public List<Integer> getDrinkEventID(String key){
        open();
        List<Integer> idList= new ArrayList<>();
        String sqlQuery = String.format("select * from %s where %s = ?",
                TABLE_DRINKEVENT, DRINKEVENT_PATIENT_KEY);
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{key});
        if (cursor.moveToFirst()) {
            do {
                    int i = cursor.getInt(0);
                //adding data to list
                idList.add(i);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return idList;
    }

}
