package de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PatientRegistrationActivity;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PatientViewActivity;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.R;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.SQLiteManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 *  interface
 * to handle interaction events.
 * Use the {@link PopUpNewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PopUpNewFragment extends DialogFragment {

    EditText txtKey;
    EditText txtDrink;
    Button btnOK;
    DataPatient dataPatientStraw;
    DataDrink dataDrinkStraw;

    public static PopUpNewFragment newInstance(DataPatient dataPatientStraw, DataDrink dataDrinkStraw){
        PopUpNewFragment f = new PopUpNewFragment();
        f.dataPatientStraw = dataPatientStraw;
        f.dataDrinkStraw = dataDrinkStraw;
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        /*
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_pop_up_new, null);
        builder.setView(view);
        */
        builder.setTitle("Unbekannter Strohhalm!");
        builder.setMessage("Wollen Sie einen neuen Patienten registrieren?\nAddresse: " + dataPatientStraw.getP_mac() + "\nTrinkmenge: " + dataDrinkStraw.getDevent_volumen() +" ml");
        builder.setIcon(R.drawable.ic_person_add_black_24dp);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getActivity(), "negative", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getActivity(), PatientRegistrationActivity.class);
                i.putExtra("DRINK_VOLULME", dataDrinkStraw.getDevent_volumen());
                i.putExtra("DRINK_TIMESTAMP", dataDrinkStraw.getDevent_timeString());
                startActivity(i);
            }
        });

        Dialog dialog = builder.create();
        return dialog;
    }
    /*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        txtKey = (EditText) findViewById(R.id.key_txt);
        txtDrink = (EditText) findViewById(R.id.drink_txt);

        final String key = txtKey.getText().toString();
        final String drink = txtDrink.getText().toString();
        final long d = Long.parseLong(drink);


        sqLiteManager = new SQLiteManager(getActivity());

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
                        // PopUpNewFragment öffnen
                        ;
                    }

                }
                if(txtDrink.getText().length() != 0){
                    Date date = new Date();
                    DataDrink dataDrink = new DataDrink(key, d, date);
                    sqLiteManager.flushDrinkData(dataDrink);
                }
                else{

                    Toast.makeText(getActivity(), "Es wurde kein neuer Trinkvorgang registriert!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
*/
/*
        protected void on(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_pop_up);

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            int width = dm.widthPixels;
            int height = dm.heightPixels;

            getWindow().setLayout((int) (width*.8), (int) (height*.6));

       }
*/


}
