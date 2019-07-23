package de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PatientViewActivity;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.R;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.SQLiteManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;


public class PopUpActivity extends Activity {

    private Button btnOK;
    private EditText txtKey;
    private EditText txtDrink;
    private SQLiteManager sqLiteManager;
    private int a = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop_up);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.8), (int) (height*.4));
/*
        txtKey = (EditText) findViewById(R.id.key_txt);
        txtDrink = (EditText) findViewById(R.id.drink_txt);

        final String key = txtKey.getText().toString();
        final String drink = txtDrink.getText().toString();
        final long d = Long.parseLong(drink);


        sqLiteManager = new SQLiteManager(this);

        btnOK = (Button) findViewById(R.id.ok_btn);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtKey.getText().length() != 0){
                    List<DataPatient> dataPatients = sqLiteManager.getAllPatients();
                    for(int i = 0; i<dataPatients.size();i++){
                        if(dataPatients.get(i).getP_key() == key){
                            a = 1;
                            break;
                        }
                    }
                    if(a==0){
                        PopUpNewFragment popUpNewFragment = new PopUpNewFragment();
                        popUpNewFragment.show(getFragmentManager(), "PopUpNew");
                         //PopUpNewFragment öffnen

                    }
                    else if(txtDrink.getText().length() != 0){
                        Date date = new Date();
                        DataDrink dataDrink = new DataDrink(key, d, date);
                        sqLiteManager.flushDrinkData(dataDrink);
                    }
                    else{
                        Snackbar.make(v, "Es wurde kein neuer Trinkvorgang registriert!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }

            }
        });
     */
    }



}
