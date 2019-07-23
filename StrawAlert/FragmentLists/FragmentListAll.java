package de.tum.mw.ftm.praktikum.androidapp_strohhalm.FragmentLists;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.R;


/**
 * Created by Dominik on 08.01.2017.
 Die Activity, welche die Liste mit den Patienten-Infos herausgibt.
 */

public class FragmentListAll extends Fragment {

    FragmentManager fragmentManager;
    View view;
    public SubFragmentListAll fragmentList;

    public FragmentListAll(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_all, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentManager = getChildFragmentManager();

        fragmentList = new SubFragmentListAll();
        fragmentManager.beginTransaction()
                .add(R.id.frag_list_all, fragmentList)
                .commit();


    }



}
