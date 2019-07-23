package de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps;

import android.app.Activity;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper.Functions;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.MainActivity;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PatientRegistrationActivity;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PatientViewActivity;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 *  interface
 * to handle interaction events.
 * Use the {@link PopUpKnownOKFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PopUpKnownOKFragment extends DialogFragment {

    Functions functions;

    //private static final String num = "num";
    private double mNum;
    private String mName1;
    private String mName2;

    public PopUpKnownOKFragment() {
    }

    public static PopUpKnownOKFragment newInstance(double num, String name1, String name2) {
        PopUpKnownOKFragment f = new PopUpKnownOKFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putDouble("num", num);
        args.putString("name1", name1);
        args.putString("name2", name2);
        f.setArguments(args);

        return f;
    }

    /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks.
         * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        //public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mNum = (int) getArguments().getDouble("num");
        mName1 = getArguments().getString("name1");
        mName2 = getArguments().getString("name2");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //LayoutInflater inflater = getActivity().getLayoutInflater();
        //View view = inflater.inflate(R.layout.fragment_pop_up_known_ok, null);
        //builder.setView(view);



        builder.setTitle(mName1 + " " + mName2);
        builder.setMessage("Die Trinkmenge " + mNum + " ml ist in Ordnung.\nMöchten Sie in die Patientenansicht wechseln?");
        builder.setIcon(R.drawable.ic_person_black_48dp);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getActivity(), ""+mNum, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

                mListener.onDialogPositiveClick(PopUpKnownOKFragment.this);
            }
        });

        Dialog dialog = builder.create();
        return dialog;
    }


}
