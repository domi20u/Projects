package de.tum.mw.ftm.praktikum.androidapp_strohhalm;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper.BLEManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps.PopUpList;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.SQLiteManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.Data;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;

/**
 * Created by Dominik on 07.01.2017.
 */



public class PatientRegistrationActivity extends AppCompatActivity implements BLEManager.NoticeBLEListener, PopUpList.NoticeDialogListener, DatePickerDialog.OnDateSetListener {



    private EditText vorname;
    private EditText nachname;
    private EditText geburtsdatum;
    private Button btnAbbrechen;
    private Button btnRegistrieren;
    private CheckBox male;
    private CheckBox female;

    private String name1;
    private String name2;
    private String bdateString;
    private Date bdate;
    private String mac;
    private String key;
    private boolean sex;
    private boolean enabled;

    private DataPatient dataPatient;
    private SQLiteManager sqLiteManager;
    private BLEManager MyBLEManager;

    private double drinkVolume;
    private String timeStamp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_registration);

        vorname = (EditText) findViewById(R.id.reg_p_name);
        nachname = (EditText) findViewById(R.id.reg_p_surname);
        geburtsdatum = (EditText) findViewById(R.id.reg_p_bday);
        male = (CheckBox) findViewById(R.id.reg_p_checkBox_male);
        female = (CheckBox) findViewById(R.id.reg_p_checkBox_female);

        MyBLEManager = new BLEManager(getApplicationContext(), this);
        Intent i = this.getIntent();
        //drinkVolume = i.getExtras().getDouble("DRINK_VOLULME");
        //timeStamp = i.getExtras().getString("DRINK_TIMESTAMP");
        sqLiteManager = new SQLiteManager(PatientRegistrationActivity.this);

        btnAbbrechen = (Button) findViewById(R.id.reg_p_cancel_btn);
        btnAbbrechen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientRegistrationActivity.this, MainActivity.class);
                PatientRegistrationActivity.this.startActivity(intent);
            }
        });

        //Damit nicht beides angekreuzt ist
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(male.isChecked()){
                    female.setChecked(false);
                }
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(female.isChecked()){
                    male.setChecked(false);
                }
            }
        });

        btnRegistrieren = (Button) findViewById(R.id.reg_p_btn);
        btnRegistrieren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vorname.getText().length()!= 0 && nachname.getText().length() != 0 && geburtsdatum.getText().length() != 0) {
                    if (male.isChecked() || female.isChecked()) {
                        //if(geburtsdatum.getText())
                        int i = (int) (Math.random()*1000000);
                        mac = "Test"+i;
                        name1 = vorname.getText().toString().trim();
                        name2 = nachname.getText().toString().trim();
                        bdateString = geburtsdatum.getText().toString();
                        sex = male.isChecked();
                        enabled = true;
                        key = mac + name1 + name2;

                        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
                        //int i = 0;
                        //while(true){
                        try {
                            Date date = df.parse(bdateString);
                            bdate = date;
                            //break;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        //    Snackbar.make(view, "Bitte Geburtsdatum im Format 'YYYY-MM-DD' eingeben", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        //    if(++a == 10) {
                        //        throw e;
                        //    }
                        //}

                        }

                        //bdate = new Date();
                        //SQLiteManager sqLiteManager = new SQLiteManager(PatientRegistrationActivity.this);
                        //DataPatient dataPatient = new DataPatient(mac, name1, name2, bdate, sex, enabled, key);
                        //key und mac werden nach dem Speichern auf dem Strohhalm ergänzt
                        key = "temp";//GregorianCalendar.getInstance().getTime().toString();
                        mac = "temp";
                        dataPatient = new DataPatient(mac, name1, name2, bdate, sex, enabled, key);
 //******************** mac und key müssen bestimmt werden

                        if (MyBLEManager.BLEPreparation()) {
                            //MyBLEManager.ScanForBLEDevices(true);
                            MyBLEManager.forWriting = true;
                            MyBLEManager.WriteCharacteristics(dataPatient);
                        }
                        else
                            Toast.makeText(getApplicationContext(), R.string.ble_scan_impossible, Toast.LENGTH_SHORT).show();


 //******************** Drinkevent muss evtl ebenfalls ausgelesen und gespeichert werden

 //******************** Möglichkeit Geburtsdatum nochmals im richtigen Format eingeben zu können (kein Zusamennbruch der App)

 //******************** Key muss auf Strohhalm übertragen werden
/*
                        sqLiteManager.flushPatientData (dataPatient);

                        Toast.makeText(getApplicationContext(), "Neuer Patient wurde erstellt!", Toast.LENGTH_SHORT).show();*/
                        /*Intent intent = new Intent(PatientRegistrationActivity.this, MainActivity.class);
                        PatientRegistrationActivity.this.startActivity(intent);*/
                    } else {
                        Toast.makeText(getApplicationContext(), "Bitte das Geschlecht auswählen!", Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    Toast.makeText(getApplicationContext(), "Bitte Patientendaten eingeben!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PatientRegistrationActivity.this, MainActivity.class);
        PatientRegistrationActivity.this.startActivity(intent);
    }

    public void datePicker (View view){
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "date");
    }

    private void setDate(final Calendar calendar){
        //final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
        ((EditText) findViewById(R.id.reg_p_bday)).setText(dateFormat.format(calendar.getTime()));
    }

    public void onDateSet(DatePicker view, int year, int month, int day){
        Calendar car = new GregorianCalendar(year ,month, day);
        setDate(car);
    }

    public static class DatePickerFragment extends DialogFragment{
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),android.R.style.Theme_Holo_Light_Dialog,
                    (DatePickerDialog.OnDateSetListener)getActivity(), year, month, day)
            {
                @Override
                protected void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
            };

            return datePickerDialog;
        }
    }

    @Override
    public void onBLEScanAndReadFinished(DataPatient dataPatientStraw, DataDrink dataDrinkStraw, int batteryLevel) {}

    @Override
    public void onBLEWriteFinished(final DataPatient dataPatientStraw) {

        dataPatient.setP_mac(dataPatientStraw.getP_mac());
        dataPatient.setP_key(dataPatientStraw.getP_key());
        sqLiteManager.flushPatientData(dataPatient);

        DataPatient dataPatientDB = sqLiteManager.getPatient(dataPatientStraw.getP_key()).get(0);
        Log.d("DevicePairing", "-- DataPatientDB: " + dataPatientDB.getP_firstname() + ", " +
                dataPatientDB.getP_lastname() + ", " +
                dataPatientDB.getP_birthdate() + ", " +
                dataPatientDB.getP_mac() + ", " +
                dataPatientDB.getP_key() + ", ");

       /* AlertDialog.Builder builder = new AlertDialog.Builder(getApplication());
        builder.setTitle("Trinkevent");
        builder.setMessage("Möchten Sie das vorhandene Trinkevent (" + String.valueOf(drinkVolume) + " ml, " + timeStamp + ") für den neuen Patienten speichern?");
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sqLiteManager.flushDrinkData(new DataDrink(dataPatientStraw.getP_key(), drinkVolume, timeStamp));
                    }
                });
        builder.setCancelable(true);
        Dialog dialog = builder.create();
        dialog.show();*/

        Intent intent = new Intent(PatientRegistrationActivity.this, MainActivity.class);
      PatientRegistrationActivity.this.startActivity(intent);
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
                    MyBLEManager.DisconnectFromGatt();
                    MyBLEManager.CloseFromGatt();
                    MyBLEManager.connect_next = dialogReturnValue;
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

}
