package de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by bg on 22.01.2017.
 */

public class PopUpList extends DialogFragment {

    private String DialogTitle;
    private String DialogType;
    private String[] itemsStrings;
    private String[] returnStrings;
    NoticeDialogListener MyNoticeDialogListener;

    public void setDialogType(String DialogType){ this.DialogType = DialogType; }

    public void setDialogTitle(String DialogTitle) { this.DialogTitle = DialogTitle; }

    public void setItemsAndReturnValues(String[] itemsStrings, String[] returnStrings){
        this.itemsStrings = itemsStrings;
        this.returnStrings = returnStrings;
    }

    public interface NoticeDialogListener {
        void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog, String DialogType, String address);
        //void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog, String DialogType, String address);
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            MyNoticeDialogListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //--- Dialog Yes & No
        /*AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Connect with device: " + address + "\n(Device " + listIndex + " of " + listSize + ")")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MyNoticeDialogListener.onDialogPositiveClick(PopUpList.this, address);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MyNoticeDialogListener.onDialogNegativeClick(PopUpList.this, address);
                    }
                });*/

        //--- Dialog List
        //String[] strings = new String[items.size()];
        //strings = items.toArray(strings);//stored the array items in the string

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(DialogTitle)
                .setItems(itemsStrings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position of the selected item
                        MyNoticeDialogListener.onDialogPositiveClick(PopUpList.this, DialogType, returnStrings[which]);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

}
