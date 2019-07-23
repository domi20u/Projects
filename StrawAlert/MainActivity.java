package de.tum.mw.ftm.praktikum.androidapp_strohhalm;



import roid.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.FragmentLists.FragmentListAll;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.FragmentLists.FragmentListOpen;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.FragmentLists.FragmentListRed;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper.BLEManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps.PopUpList;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper.Functions;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps.PopUpKnownNOKFragment;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps.PopUpKnownOKFragment;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps.PopUpNewFragment;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.SQLiteManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.AppConfig;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.User;


public class MainActivity extends AppCompatActivity implements PopUpKnownOKFragment.NoticeDialogListener, PopUpKnownNOKFragment.NoticeDialogListener, PopUpList.NoticeDialogListener, BLEManager.NoticeBLEListener {

    //Parameters for Bluetooth
    private BLEManager MyBLEManager;
    private DataPatient dataPatientStraw;
    private DataDrink dataDrinkStraw;
    SQLiteManager sqLiteManager = new SQLiteManager(this);

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    // Patient of last detected Drink-Event to open PatientView on Dialog positive click
    List<DataPatient> currentPatientlist;
    static DataPatient currentPatient;

    public FragmentListRed tab1_red;
    public FragmentListOpen tab2_open;
    public FragmentListAll tab3_all;



    Functions functions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        MyBLEManager = new BLEManager(getApplicationContext(), this);
        dataPatientStraw = new DataPatient();
        dataDrinkStraw = new DataDrink();
        MyBLEManager.BLEPreparation();

        sqLiteManager = new SQLiteManager(getApplicationContext());



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Snackbar.make(view, "Verbindung wird gesucht...", Snackbar.LENGTH_SHORT)
                //        .setAction("Action", null).show();

//************* Bluetooth-Verbindung aufbauen und Daten auslesen
                if (MyBLEManager.BLEPreparation())
                    MyBLEManager.ScanForBLEDevices(true);
                else
                    Toast.makeText(getApplicationContext(), R.string.ble_scan_impossible, Toast.LENGTH_SHORT).show();
/*
/*//************* Prüfen, ob neues Trinkevent vorhanden
                final String mac;
                final String key;
                final String help_key;
                double drink = (Math.random()*1000);
                String name1;
                String name2;
                final String[] macs;
                final String[] keys;
                *//*
                if(drink%1 == 0){
                    mac = "Test";
                }
                else { mac = "Test1";}
*//*

                sqLiteManager = new SQLiteManager(MainActivity.this);

                List<DataPatient> dataPatients = sqLiteManager.getAllPatients();



                macs = new String[dataPatients.size()];
                keys = new String[dataPatients.size()];
                int i = 0;
                for(i = 0; i<dataPatients.size(); i++){
                    macs[i] = dataPatients.get(i).getP_mac();
                    keys[i] = dataPatients.get(i).getP_key();
                }

                if(i>5) {
                    int d = (int)(Math.random()*dataPatients.size());
                    key = dataPatients.get(d).getP_key();
                    currentPatientlist = sqLiteManager.getPatient(key);
                    //Toast.makeText(getApplicationContext(), ""+currentPatientlist.size(), Toast.LENGTH_LONG).show();
                    currentPatient = currentPatientlist.get(0);
                    name1 = currentPatient.getP_firstname();
                    name2 = currentPatient.getP_lastname();
                    //mac = currentPatient.getP_key();

                *//*
                if(drink%3 == 0){
                    key = "Nope" + help_key;
                }
                else{
                    key = help_key;
                }
                *//*


                //Toast.makeText(getApplicationContext(), macs[0], Toast.LENGTH_LONG).show();
                //if (Arrays.asList(macs).contains(mac)) {
                    //Toast.makeText(getApplicationContext(), "vorhanden", Toast.LENGTH_LONG).show();

                    Date date = new Date();
                    DataDrink dataDrink = new DataDrink(key, drink, date);
                    sqLiteManager.flushDrinkData(dataDrink);
                    *//*
                    currentPatientlist = sqLiteManager.getPatient(key);
                    currentPatient = currentPatientlist.get(0);
                    name1 = currentPatient.getP_firstname();
                    name2 = currentPatient.getP_lastname();
                    *//*


                    if(drink < 500){
                        PopUpKnownNOKFragment popUpKnownNOKFragment = PopUpKnownNOKFragment.newInstance(drink, name1, name2);
                        popUpKnownNOKFragment.show(getSupportFragmentManager(), "PopUpNOK");
                    }
                    else{
                        PopUpKnownOKFragment popUpKnownOKFragment = PopUpKnownOKFragment.newInstance(drink, name1, name2);
                        popUpKnownOKFragment.show(getSupportFragmentManager(), "PopUpOK");

                    }
/*//************* Trink-Event vom Strohhalm löschen
                } else {
                    //Toast.makeText(getApplicationContext(), "NICHT", Toast.LENGTH_LONG).show();
                    PopUpNewFragment popUpNewFragment = new PopUpNewFragment();
                    popUpNewFragment.show(getFragmentManager(), "PopUpNew");
                }
                *//*
                for (int i = 0; i < dataPatients.size(); i++) {
                    if (dataPatients.get(i).getP_mac().equals(mac)) {
                        a = 1;
                        break;
                    }
                }
                if (a == 0) {
                    PopUpNewFragment popUpNewFragment = new PopUpNewFragment();
                    popUpNewFragment.show(getFragmentManager(), "PopUpNew");
                    //PopUpNewFragment öffnen

                } else if (txtDrink.getText().length() != 0) {
                    Date date = new Date();
                    DataDrink dataDrink = new DataDrink(key, d, date);
                    sqLiteManager.flushDrinkData(dataDrink);
                } else {
                    Snackbar.make(v, "Es wurde kein neuer Trinkvorgang registriert!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                */
            }


        });
    }



    /**
     * Overriding BackPressed to avoid Login-Activity as long as Logout has not been chosen
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
/*
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        if (id == R.id.action_logout) {

            SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(User.PREFGROUP_USER,MODE_PRIVATE).edit();
            editor.putBoolean(User.PREF_IS_LOGGED_IN, false).apply();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
            return true;
        }

        if (id == R.id.action_sync) {
            //Reihenfolge beachten
            syncDrinkEventSQLiteMySQL();
            syncPatientSQLiteMySQL();

            syncPatientMySQLSQLite();
            syncDrinkeventMySQLSQLite();
            //syncPatientMySQLSQLite();
        }
/*
        if (id == R.id.action_search) {

        }
*/
        if (id == R.id.action_newPatient) {


            Intent intent = new Intent(MainActivity.this, PatientRegistrationActivity.class);
            MainActivity.this.startActivity(intent);
            /*
            sqLiteManager = new SQLiteManager(MainActivity.this);
            List<DataDrink> drinkList = sqLiteManager.getDrinkEventList("TestTest");
            final double d = drinkList.get(0).getDevent_volumen();
            Toast.makeText(getApplicationContext(), "erste Trinkmenge: " + d ,Toast.LENGTH_LONG).show();
            */
                /*
                if (username.getText().length() != 0 && password.getText().length() != 0) {
                    new LoginTask().execute(new String[]{username.getText().toString(), password.getText().toString()});
                } else {
                    Toast.makeText(getApplicationContext(), R.string.log_error_msg, Toast.LENGTH_LONG).show();
                }
                */


            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            tab1_red = new FragmentListRed();
            tab2_open = new FragmentListOpen();
            tab3_all = new FragmentListAll();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    //FragmentListRed
                    return tab1_red;
                case 1:
                    //FragmentListAll
                    return tab2_open;
                case 2:
                    //FragmentListOpen
                    return tab3_all;

            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "AUFFÄLLIG";
                case 1:
                    return "AUSSTEHEND";
                case 2:
                    return "ALLE";
            }
            return null;
        }
    }
    /*
    public static PopUpKnownOKFragment newInstance(int drink, String name1, String name2) {
        PopUpKnownOKFragment f = new PopUpKnownOKFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong("ARG_DRINK", drink);
        args.putString("ARG_NAME1", name1);
        args.putString("ARG_NAME2", name2);
        f.setArguments(args);
        return f;
    }
*/
    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog1 = new PopUpKnownNOKFragment();
        dialog1.show(getSupportFragmentManager(), "PopUpKnownNOKFragment");

        DialogFragment dialog2 = new PopUpKnownOKFragment();
        dialog2.show(getSupportFragmentManager(), "PopUpKnownOKFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        //sqLiteManager = new SQLiteManager(MainActivity.this);
        //currentPatientlist = sqLiteManager.getAllPatients();
        //DataPatient currentPatient1 = currentPatientlist.get(0);
        //Toast.makeText(MainActivity.this, currentPatient.getP_lastname(), Toast.LENGTH_LONG).show();
        functions = new Functions(MainActivity.this);
        functions.openPatientsView(dataPatientStraw);

    }

    //public void syncData(){

    //}

    /**
     * Syncs patientData to the MySql-Server
     */
    public void syncPatientSQLiteMySQL(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params  = new RequestParams();
        if(!sqLiteManager.isPatientListEmpty()){
            if(!sqLiteManager.patientSynced()){
                params.put("patientsJSON", sqLiteManager.composeJSONpatient());
                client.post(AppConfig.URL_InsertPatient, params, new AsyncHttpResponseHandler(){
                    @Override
                    public void onSuccess(String response) {
                        System.out.println(response);
                        try {
                            String response2 = response.substring(response.indexOf("?>")+2);
                            JSONArray arr = new JSONArray(response2);
                            System.out.println(arr.length());
                            for(int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject)arr.get(i);
                                sqLiteManager.setPatientSynced(obj.get("p_key").toString());
                            }
                            //Toast.makeText(getApplicationContext(), getString(R.string.upload_patientToast), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), getString(R.string.JSON_error), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        if(statusCode == 404){
                            Toast.makeText(getApplicationContext(), getString(R.string.adress_error), Toast.LENGTH_LONG).show();
                        }else if(statusCode == 500){
                            Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                //Toast.makeText(getApplicationContext(), getString(R.string.upload_patientToastdone), Toast.LENGTH_LONG).show();
            }
        }else{
            //Toast.makeText(getApplicationContext(), getString(R.string.no_patients), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Syncs drinkData to the MySql-Server
     */
    public void syncDrinkEventSQLiteMySQL(){

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params  = new RequestParams();
        if(!sqLiteManager.isDrinkEventListEmpty()){
            if(!sqLiteManager.drinkEventSynced()){
                params.put("drinkeventsJSON", sqLiteManager.composeJSONdrinkevent());
                client.post(AppConfig.URL_InsertDrinkEvent, params, new AsyncHttpResponseHandler(){
                    @Override
                    public void onSuccess(String response) {
                        System.out.println(response);
                        try {
                            String response2 = response.substring(response.indexOf("?>")+2);
                            JSONArray arr = new JSONArray(response2);
                            System.out.println(arr.length());
                            for(int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject)arr.get(i);
                                String helper = obj.get("devent_id").toString();
                                int id = Integer.parseInt(helper);
                                sqLiteManager.setDrinkEventSynced(id);
                            }
                            //Toast.makeText(getApplicationContext(), getString(R.string.upload_drinkToast), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), getString(R.string.JSON_error), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        if(statusCode == 404){
                            Toast.makeText(getApplicationContext(), getString(R.string.adress_error), Toast.LENGTH_LONG).show();
                        }else if(statusCode == 500){
                            Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                //Toast.makeText(getApplicationContext(), getString(R.string.upload_drinkToastdone), Toast.LENGTH_LONG).show();
            }
        }else{
            //Toast.makeText(getApplicationContext(), getString(R.string.no_drinks), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Syncs patientData from the MySQL-Server to the local database
     */
    public void syncPatientMySQLSQLite(){
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params  = new RequestParams();
        Gson gson = new GsonBuilder().create();
        params.put("getPatientsJSON", gson.toJson(null));
        client.post(AppConfig.URL_getPatients, params, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                System.out.println(response);
                try {
                    String response2 = response.substring(response.indexOf("?>")+2);
                    List<DataPatient> patientList = new ArrayList<DataPatient>();
                    JSONArray arr = new JSONArray(response2);
                    System.out.println(arr.length());
                    for(int i=0; i<arr.length();i++){
                        JSONObject obj = (JSONObject)arr.get(i);
                        String mac = obj.get("p_mac").toString();
                        String firstname = obj.get("p_firstname").toString();
                        String lastname = obj.get("p_lastname").toString();
                        String birthdate = obj.get("p_birthdate").toString();
                        int sex = Integer.parseInt(obj.get("p_sex").toString());
                        Boolean flag1 = (sex == 1) ? true : false;
                        int enabled = Integer.parseInt(obj.get("p_enabled").toString());
                        Boolean flag2 = (enabled == 1) ? true : false;
                        String key = obj.get("p_key").toString();
                        DataPatient patient = new DataPatient(mac, firstname, lastname, birthdate, flag1, flag2, key);
                        patientList.add(patient);
                    }
                    sqLiteManager.flushPatientData(patientList);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.JSON_error), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), getString(R.string.adress_error), Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Syncs drinkData from the MySQL-Server to the local database
     */
    public void syncDrinkeventMySQLSQLite(){
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params  = new RequestParams();
        Gson gson = new GsonBuilder().create();
        params.put("getDrinkeventsJSON", gson.toJson(null));
        client.post(AppConfig.URL_getDrinkevents, params, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                System.out.println(response);
                try {
                    String response2 = response.substring(response.indexOf("?>")+2);
                    List<DataDrink> drinkList = new ArrayList<DataDrink>();
                    JSONArray arr = new JSONArray(response2);
                    System.out.println(arr.length());
                    for(int i=0; i<arr.length();i++){
                        JSONObject obj = (JSONObject)arr.get(i);
                        int id = Integer.parseInt(obj.get("devent_id").toString());
                        String key = obj.get("devent_p_key").toString();
                        String timestring = obj.get("devent_timestamp").toString();
                        int volumen = Integer.parseInt(obj.get("devent_volumen").toString());
                        DataDrink drink = new DataDrink(key, volumen, timestring);
                        drinkList.add(drink);
                    }
                    sqLiteManager.flushDrinkData(drinkList);
                    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
                    // Set up the ViewPager with the sections adapter.
                    mViewPager = (ViewPager) findViewById(R.id.container);
                    mViewPager.setAdapter(mSectionsPagerAdapter);

                    Toast.makeText(getApplicationContext(), getString(R.string.upload_successful), Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), getString(R.string.JSON_error), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // TODO Auto-generated method stub
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), getString(R.string.adress_error), Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    // ------------------ FOR DIALOG CALLBACK WITH YES - NO DIALOG
    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String DialogType, String dialogReturnValue) {
        // User touched the dialog's positive button

        if (DialogType.equals(getString(R.string.dialogType_ChooseDevice))){
            Log.d("DevicePairing", "-- ConnectToGatt(" + dialogReturnValue + ")");

            if (MyBLEManager.MyBluetoothGatt == null) {
                MyBLEManager.ConnectToGatt(dialogReturnValue);
                MyBLEManager.connect_next = null;
            }
            else {
                if (MyBLEManager.MyBluetoothGatt.getDevice().getAddress().equals(dialogReturnValue)){
                    MyBLEManager.MyBluetoothGatt.discoverServices();
                }
                else {
                    MyBLEManager.connect_next = dialogReturnValue;
                    MyBLEManager.DisconnectFromGatt();
                    //MyBLEManager.CloseFromGatt();
                }
            }
        }
        else if (DialogType.equals(getString(R.string.dialogType_ChooseCharacteristic)) ){
            //if (MyBLEManager.MyBluetoothGatt != null && MyBLEManager.MyBluetoothGatt.getDevice().getName().equals(getString(R.string.ble_getName_straw))) {
            MyBLEManager.AskForValueForWriting(dialogReturnValue);
        }
        else if (DialogType.equals(getString(R.string.dialogType_ChooseValue))) {
            MyBLEManager.forSingleWriting = true;
            MyBLEManager.WriteCharacteristic(dialogReturnValue);
        }
    }

    @Override
    //public void onBLEScanAndReadFinished(DataPatient dataPatientStraw, DataDrink dataDrinkStraw) {
    public void onBLEScanAndReadFinished(DataPatient dataPatientStraw, DataDrink dataDrinkStraw, int batteryLevel) {
        this.dataPatientStraw = dataPatientStraw;
        this.dataDrinkStraw = dataDrinkStraw;
        CheckForExistingPatient();
    }

    @Override
    public void onBLEWriteFinished(DataPatient dataPatientStraw) {}

    public void CheckForExistingPatient(){
        List<DataPatient> dataPatients = sqLiteManager.getAllPatients();

        List<String> macs;
        List<String> keys;

        macs = new ArrayList<>();
        keys = new ArrayList<>();
        for(DataPatient dataPatient : dataPatients){
            macs.add(dataPatient.getP_mac());
            keys.add(dataPatient.getP_key());
        }

        if (keys.contains(dataPatientStraw.getP_key())){
            sqLiteManager.flushDrinkData (dataDrinkStraw);

            if (dataDrinkStraw.getDevent_volumen() > 500){
                PopUpKnownOKFragment popUpKnownOKFragment = PopUpKnownOKFragment.newInstance(dataDrinkStraw.getDevent_volumen(), dataPatientStraw.getP_firstname(), dataPatientStraw.getP_lastname());
                popUpKnownOKFragment.show(getSupportFragmentManager(), "PopUpOK");
            }
            else{
                PopUpKnownNOKFragment popUpKnownNOKFragment = PopUpKnownNOKFragment.newInstance(dataDrinkStraw.getDevent_volumen(), dataPatientStraw.getP_firstname(), dataPatientStraw.getP_lastname());
                popUpKnownNOKFragment.show(getSupportFragmentManager(), "PopUpNOK");
            }
        }
        else{
            PopUpNewFragment popUpNewFragment = PopUpNewFragment.newInstance(dataPatientStraw, dataDrinkStraw);
            popUpNewFragment.show(getFragmentManager(), "PopUpNew");
        }

        //Write data to straw
        //DataPatient dataPatientNew = new DataPatient();
        //MyBLEManager.forWriting = true;
        //MyBLEManager.WriteCharacteristics(dataPatientNew);

    }

}
