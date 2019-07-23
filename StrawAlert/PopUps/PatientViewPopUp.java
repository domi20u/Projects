package de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps;

import android.app.Activity;
import android.app.Dialog;

import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.ArrayAdapterPatientView;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.R;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;

/**
 * Created by Dominik on 26.01.2017.
 */

public class PatientViewPopUp extends DialogFragment {
    int mOk;
    int mZuWenig;
    String[] mVols;
    String[] mMessage;
    ListView lv;
    int[] mImages1;
    int[] mImages2;


    public PatientViewPopUp() {
    }

    public static PatientViewPopUp newInstance(String[] message, int ok, int zuWenig, int[] images1, int[] images2) {
        PatientViewPopUp f = new PatientViewPopUp();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putStringArray("m", message);
        args.putInt("o", ok);
        args.putInt("nO", zuWenig);
        args.putIntArray("i1", images1);
        args.putIntArray("i2", images2);


        f.setArguments(args);

        return f;
    }
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog);
        //public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    PatientViewPopUp.NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PatientViewPopUp.NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mMessage = getArguments().getStringArray("m");
        mVols = getArguments().getStringArray("v");
        mOk = getArguments().getInt("o");
        mZuWenig = getArguments().getInt("nO");
        //mImages1 = getArguments().getIntArray("i1");
        //mImages2 = getArguments().getIntArray("i2");

        String ok = "OK";
        String zuWenig = "XX";


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());



        builder.setIcon(R.drawable.ic_local_drink_black_48dp);
        builder.setTitle("OK: "+ mOk + "        XX: " + mZuWenig);
        builder.setItems(mMessage, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getActivity(), mVols[which], Toast.LENGTH_LONG).show();
            }
        });


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Intent i = new Intent(getActivity(), PatientViewActivity.class);
                //startActivity(i);
                //mListener.onDialogPositiveClick(PopUpKnownNOKFragment.this);

            }
        });

        Dialog dialog = builder.create();

        return dialog;
    }

/*
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_list_pv, container, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootview);
        lv = (ListView) rootview.findViewById(R.id.listview_pv);
        //getDialog().setTitle("Bla");

        ArrayAdapterPatientView adapter = new ArrayAdapterPatientView(getActivity(),
                R.layout.fragment_list_pv, mMessage, mImages1, mImages2);
        lv.setAdapter(adapter);


        return rootview;
    }
    */
}
