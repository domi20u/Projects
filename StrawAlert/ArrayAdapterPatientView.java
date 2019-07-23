package de.tum.mw.ftm.praktikum.androidapp_strohhalm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper.Functions;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.SQLiteManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;

/**
 * Created by Dominik on 27.01.2017.
 */

public class ArrayAdapterPatientView extends ArrayAdapter<String> {
    Context context;
    int[] progImages;
    ///List<DataDrink> list1;
    int[] drinks;
    String[] message;
    Functions functions;


    SQLiteManager sqLiteManager = new SQLiteManager(context);


    public String name;
    public String room;
    public String room_number;
    public String sex;

    public ArrayAdapterPatientView(Context context, int resource, String[] message, int[] drinks,
                                   int[] progImages) {
        super(context, resource);
        this.context = context;
        this.message = message;
        this.drinks = drinks;
        this.progImages = progImages;
    }

    @NonNull

    public View getView(final int position, View rowView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.list_item_pv, parent, false);

        TextView list_txt = (TextView) rowView.findViewById(R.id.text1);
        ImageView list_bld = (ImageView) rowView.findViewById(R.id.bild1);
        //TextView list_Drink = (TextView) rowView.findViewById(R.id.list_drink);


        functions = new Functions(getContext());
        //String key_test = list1.get(position).getP_key();

        list_txt.setText(message[position]);
        list_bld.setImageResource(R.drawable.drink_rec);

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


        rowView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {


            }
        });

        return rowView;
    }
}