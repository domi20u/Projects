package de.tum.mw.ftm.praktikum.androidapp_strohhalm.FragmentLists;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper.Functions;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PatientViewActivity;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.R;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.SQLiteManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.Data;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;


/**
 * Created by Dominik on 08.01.2017.
 Array Adapter, der die ListView im Layout "fragment_list_all"
 mit den benutzerdefinierten Listelementen (Layout "list_item") füllt.
 */

public class ArrayAdapterListType extends ArrayAdapter<DataPatient> {
    Context context;
    Integer[] progImages;
    List<DataPatient> list1;
    Integer[] drinks;
    String[] dates;
    Functions functions;


    SQLiteManager sqLiteManager = new SQLiteManager(context);





    public String name;
    public String room;
    public String room_number;
    public String sex;

    public ArrayAdapterListType(Context context, int resource, List<DataPatient> objects, Integer[] obj1, String[] obj2, Integer[] images){
        super(context, resource, objects);
        this.context = context;
        this.list1 = objects;
        this.drinks = obj1;
        this.dates = obj2;
        this.progImages = images;

    }

    @NonNull
    @Override
    public View getView(final int position, View rowView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.list_item, parent, false);

        TextView list_Vorname = (TextView) rowView.findViewById(R.id.list_name);
        TextView list_Nachname = (TextView) rowView.findViewById(R.id.list_surname);
        //TextView list_Drink = (TextView) rowView.findViewById(R.id.list_drink);
        TextView list_Time = (TextView) rowView.findViewById(R.id.list_time);
        ImageView list_image = (ImageView) rowView.findViewById(R.id.list_image);
        ImageView list_image_drink = (ImageView) rowView.findViewById(R.id.imageListDrink);

        functions = new Functions(getContext());
        //String key_test = list1.get(position).getP_key();

        list_Vorname.setText(list1.get(position).getP_firstname());
        list_image.setImageResource(progImages[position]);
        list_Nachname.setText(list1.get(position).getP_lastname());
        list_image_drink.setImageResource(functions.getFullGlass(drinks[position]));

        list_Time.setText(dates[position]);
        if(dates[position].equals("HEUTE")){
            list_Time.setTextColor(ContextCompat.getColor(getContext(), R.color.mainGreen));
        }
        else{
            list_Time.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
/*
        if(drinks[position] == -1){
            list_Drink.setText("- ml");
        }
        else if(drinks[position] < 500) {
            list_Drink.setText(drinks[position]+" ml");
            list_Drink.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
        else {
            list_Drink.setText(drinks[position]+" ml");
            list_Drink.setTextColor(ContextCompat.getColor(getContext(), R.color.mainGreen));
        }
        */


        rowView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                functions = new Functions(getContext());
                functions.openPatientsView(list1.get(position));

            }
        });

        return rowView;
    }

    private String setDateString(List<DataDrink> drinks) {

        int b = drinks.size();
        String[] dates = new String[b];
        int c = 0;
        Date d1;
        if (b > 0) {
            for (int a = b - 1; a >= 0; a--) {
                d1 = drinks.get(a).getDevent_timestamp();
                if (DateUtils.isToday(d1.getTime())) {
                    dates[c] = "HEUTE";
                } else {
                    SimpleDateFormat sdf2 = new SimpleDateFormat("dd.MM.yy");
                    dates[c] = sdf2.format(d1);
                }
                c++;
            }

        }
        return dates[0];
    }


}
