package de.tum.mw.ftm.praktikum.androidapp_strohhalm;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper.Functions;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps.PatientViewPopUp;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.SQLiteManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;

public class PatientViewActivity extends AppCompatActivity implements PatientViewPopUp.NoticeDialogListener {

    TextView nameTxt;
    TextView snameTxt;
    TextView bdateTxt;
    TextView keyTxt;
    ListView dates;
    ListView volumes;
    Switch enabled;
    ImageView imageSex;
    ImageView imageDrinkOk;
    TextView drinkVolMean;
    SQLiteManager sqLiteManager;
    String key;
    TextView today;
    TextView yesday;
    TextView yesyesday;
    TextView vol_today;
    TextView vol_yesday;
    TextView vol_yesyesday;
    Button button;
    ImageView drink_today;
    ImageView drink_yesday;
    ImageView drink_yesyesday;
    String[] vols;
    String[] date;
    String[] message;
    int zuWenig;
    int ok;
    int[] images1;
    int[] images2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_view);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        nameTxt = (TextView) findViewById(R.id.name_txt_detail);
        snameTxt = (TextView) findViewById(R.id.surname_txt_detail);
        bdateTxt = (TextView) findViewById(R.id.bdate_txt_detail);
        //keyTxt = (TextView) findViewById(R.id.key_txt_detail);
        //dates = (ListView) findViewById(R.id.dates_datail);
        //volumes = (ListView) findViewById(R.id.volumes_detail);
        enabled = (Switch) findViewById(R.id.switch_detail);
        imageSex = (ImageView) findViewById(R.id.image_sex_detail);
        imageDrinkOk = (ImageView) findViewById(R.id.image_drink_ok_detail);
        drinkVolMean = (TextView) findViewById(R.id.txt_drink_ok_detail);

        today = (TextView) findViewById(R.id.txt_today);
        yesday = (TextView) findViewById(R.id.txt_yesday);
        yesyesday = (TextView) findViewById(R.id.txt_yesyesday);
        vol_today = (TextView) findViewById(R.id.txt_vol_today);
        vol_yesday = (TextView) findViewById(R.id.txt_vol_yesday);
        vol_yesyesday = (TextView) findViewById(R.id.txt_vol_yesyesday);
        button = (Button) findViewById(R.id.button_detail);

        drink_today = (ImageView) findViewById(R.id.image_Drink_today);
        drink_yesday = (ImageView) findViewById(R.id.image_Drink_yesday);
        drink_yesyesday = (ImageView) findViewById(R.id.image_Drink_yesyesday);




        Intent i = this.getIntent();
        String name1 = i.getExtras().getString("NAME_KEY");
        String name2 = i.getExtras().getString("SURNAME_KEY");
        String bDate = i.getExtras().getString("BIRTH_DATE_KEY");
        key = i.getExtras().getString("KEY_KEY");
        date = i.getExtras().getStringArray("LIST_DRINK_DATES");
        final int[] volume = i.getExtras().getIntArray("LIST_DRINK_VOLUMES");
        Boolean sex = i.getExtras().getBoolean("SEX_KEY");
        //Boolean enab = i.getExtras().getBoolean("ENABLED_KEY");
        Boolean okVol10 = i.getExtras().getBoolean("IS_VOL_10_OK");
        Boolean okVolges = i.getExtras().getBoolean("IS_GES_VOL_OK");
        String meanVolGes = i.getExtras().getString("MEAN_VOL_GES");
        String meanVol10 = i.getExtras().getString("MEAN_VOL_10");

        sqLiteManager = new SQLiteManager(getApplicationContext());
        List<DataPatient> dataPatients = sqLiteManager.getPatient(key);
        DataPatient dataPatient = dataPatients.get(0);
        Boolean enab = dataPatient.isP_enabled();
        //Toast.makeText(getApplicationContext(), "Key: " + key, Toast.LENGTH_SHORT).show();


        Functions functions = new Functions(getApplicationContext());
/*
        String[] d = {
                "Maier", "Schulze", "Hi", "hu"
        };
        String[] v = {
                "245", "232", "543", "635"
        };
*/
        if(date!=null && volume!=null) {
            vols = new String[volume.length];
            message = new String[volume.length];
            ok = 0;
            zuWenig = 0;
            for(int a = 0; a < volume.length; a++ ){
                vols[a] = volume[a] + " ml";
                if(volume[a] >= 500) {
                    message[a] = " OK     "+date[a] + "     " + vols[a];
                    ok++;
                }
                else {
                    message[a] = " XX     "+date[a] + "     " + vols[a];
                    zuWenig++;
                }

            }

            //ListAdapter adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, date);
            //ListAdapter adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, volume);
            //dates.setAdapter(adapter1);
            //volumes.setAdapter(adapter2);
            today.setText(date[0]);
            String s = vols[0];
            vol_today.setText(s);
            drink_today.setImageResource(functions.getFullGlass(volume[0]));
            if(date.length > 1 && vols.length > 1) {
                yesday.setText(date[1]);
                String s1 =vols[1];
                vol_yesday.setText(s1);
                drink_yesday.setImageResource(functions.getFullGlass(volume[1]));
                if(date.length > 2 && vols.length > 2) {
                    yesyesday.setText(date[2]);
                    String s2 = vols[2];
                    vol_yesyesday.setText(s2);
                    drink_yesyesday.setImageResource(functions.getFullGlass(volume[2]));
                }
            }
        }


        if(enab){enabled.setChecked(true);}
        nameTxt.setText(name1);
        snameTxt.setText(name2);
        bdateTxt.setText(bDate);
        //keyTxt.setText(key);
        drinkVolMean.setText(meanVolGes);
        if(sex){imageSex.setImageResource(R.drawable.male_symbol);}
        if(!okVolges){
            imageDrinkOk.setImageResource(R.drawable.ic_report_problem_black_48dp);
            drinkVolMean.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        }

        sqLiteManager = new SQLiteManager(getApplicationContext());

        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sqLiteManager.setPatientEnabled(key);
                    Toast.makeText(getApplicationContext(), "Patient wurde aktiviert", Toast.LENGTH_SHORT).show();
                }
                else{
                    sqLiteManager.setPatientDisabled(key);
                    Toast.makeText(getApplicationContext(), "Patient wurde deaktiviert", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vols != null) {
                    images1 = new int[vols.length];
                    images2 = new int[vols.length];
                    PatientViewPopUp patientViewPopUp = PatientViewPopUp.newInstance(message, ok, zuWenig, images1, images2);
                    patientViewPopUp.show(getSupportFragmentManager(), "PopUpPatient");
                    List<DataDrink> dataDrinks = sqLiteManager.getDrinkEventList(key);
                    /*
                    ListAdapter adapter = new ArrayAdapterPatientView(getApplicationContext(), R.layout.list_item_pv,
                            message, images1, images2);

                    new AlertDialog.Builder(getApplicationContext()).setTitle("Select Image")
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item ) {
                                    Toast.makeText(getApplicationContext(), "Item Selected: " + item, Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                            */
                }
                else
                    Toast.makeText(getApplicationContext(), "Keine Drink-Events vorhanden", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {

    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */

    public Integer getFullGlass(int vol){
        if(vol < 150) {
            return R.drawable.drink_full_16;
        }else if(vol < 350){
            return R.drawable.drink_full_26;
        }else if(vol < 500){
            return R.drawable.drink_full_36;
        }else if(vol < 600){
            return R.drawable.drink_full_46;
        }else if(vol < 750){
            return R.drawable.drink_full_56;
        }else{return R.drawable.drink_full_66;}
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }




}
