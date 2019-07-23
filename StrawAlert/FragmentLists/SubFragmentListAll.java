package de.tum.mw.ftm.praktikum.androidapp_strohhalm.FragmentLists;


import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper.Functions;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.R;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.SQLiteManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;

/**
 * Created by Dominik on 08.01.2017.
 Ist die Activity vom Layout "list_item".
 */

public class SubFragmentListAll extends ListFragment {
    SQLiteManager sqLiteManager;
    Functions functions;
    List<DataPatient> list;
    public ArrayAdapterListType adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public SubFragmentListAll(){

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sqLiteManager = new SQLiteManager(getActivity());
        functions = new Functions(getActivity());
        List<DataPatient> list = sqLiteManager.getAllPatients();
        Integer[] volumes = functions.meanVolume(list);
        String[] dates = functions.LatestTimes(list);
        Integer[] imageArray = functions.createImageArray(list);


        //ArrayAdapterListType adapter = new ArrayAdapterListType(getActivity(), R.layout.list_item, list, volumes, dates, imageArray);
        adapter = new ArrayAdapterListType(getActivity(), R.layout.list_item, list, volumes, dates, imageArray);
        setListAdapter(adapter);


        getListView().setDivider(null);
        getListView().setDividerHeight(0);

    }

    /*
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(getActivity(), getListView().getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
    }
    */


    public static Integer[] createImageArray(List<DataPatient> dataPatients){
        Integer[] Images = new Integer[dataPatients.size()];
        for (int i = 0; i < dataPatients.size(); i++){
            if (dataPatients.get(i).isP_sex()) {
                Images[i] = R.drawable.male_symbol;
            }
            else {
                Images[i] = R.drawable.female_symbol;

            }
        }
        return Images;
    }
    public static String[] createRoomArray(String[] rooms){
        String[] room = new String[rooms.length];
        for(int i = 0; i<rooms.length; i++){
            room[i] = "Raum: " + rooms[i];
        }
        return room;
    }
}
