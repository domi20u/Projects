package de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PatientViewActivity;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.R;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.SQLiteManager;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;

/**
 * Created by Dominik on 16.01.2017.
 */

public class Functions {
    Context mContext;

    public Functions(Context context) {
        this.mContext = context;
    }


    public void openPatientsView(DataPatient dataPatient){

        Intent i = new Intent(mContext, PatientViewActivity.class);
        SQLiteManager sqLiteManager = new SQLiteManager(mContext);
        List<DataDrink> dataDrinks = sqLiteManager.getDrinkEventList(dataPatient.getP_key());

        int b = dataDrinks.size();
        int[] volumes = new int[b];
        String[] dates = new String[b];
        int c = 0;
        int gesVol = 0;
        int vol10 = 0;
        int vol;
        Date d1;
        if(b>0) {
            for (int a = b - 1; a >= 0; a--) {
                vol = (int) dataDrinks.get(a).getDevent_volumen();
                gesVol = gesVol + vol;
                if(b>=10 && c<10){
                    vol10 = vol10 + vol;
                }
                volumes[c] = vol;

                d1 = dataDrinks.get(a).getDevent_timestamp();
                if(DateUtils.isToday(d1.getTime())){
                    dates[c] = "HEUTE";
                }
                else{
                    SimpleDateFormat sdf2 = new SimpleDateFormat("dd.MM.yy");
                    dates[c] = sdf2.format(d1);
                }
                c++;
            }
            //Toast.makeText(mContext, "c: "+c,Toast.LENGTH_SHORT).show();

            int i_meanGesVol = gesVol/b;
            int i_meanVol10 = vol10/10;
            if(b<10){i_meanVol10 = i_meanGesVol;}
            boolean meanGesVol_ok = true;
            boolean meanVol10_ok = true;

            if(i_meanGesVol<500){meanGesVol_ok = false;}
            if(i_meanVol10<500){meanVol10_ok = false;}

            String meanGesVol = i_meanGesVol + " ml";
            String meanVol10 = i_meanVol10 + " ml";

            i.putExtra("IS_GES_VOL_OK", meanGesVol_ok);
            i.putExtra("IS_VOL_10_OK", meanVol10_ok);
            i.putExtra("MEAN_VOL_GES", meanGesVol);
            i.putExtra("MEAN_VOL_10", meanVol10);
            i.putExtra("LIST_DRINK_VOLUMES", volumes);
            i.putExtra("LIST_DRINK_DATES", dates);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy");
        String bdate = sdf.format(dataPatient.getP_birthdate());
        //Toast.makeText(mContext, "bday: " + bdate, Toast.LENGTH_SHORT).show();
        i.putExtra("NAME_KEY", dataPatient.getP_firstname());
        i.putExtra("SURNAME_KEY", dataPatient.getP_lastname());
        i.putExtra("BIRTH_DATE_KEY", bdate);
        i.putExtra("SEX_KEY", dataPatient.isP_sex());
        i.putExtra("ENABLED_KEY", dataPatient.isP_enabled());
        i.putExtra("KEY_KEY", dataPatient.getP_key());


        mContext.startActivity(i);

    }

    public Integer[] meanVolume(List<DataPatient> dataPatients) {
        SQLiteManager sqLiteManager = new SQLiteManager((mContext));
        Integer[] volumes = new Integer[dataPatients.size()];
        List<DataDrink> dataDrinks;
        for (int i = 0; i < dataPatients.size(); i++) {
            int z = 0;
            dataDrinks = sqLiteManager.getDrinkEventList(dataPatients.get(i).getP_key());
            if (!sqLiteManager.isDrinkEventListEmpty(dataPatients.get(i).getP_key())) {
                for (int a = 0; a < dataDrinks.size(); a++) {
                    z = z + (int) dataDrinks.get(a).getDevent_volumen();
                }
                volumes[i] = z / dataDrinks.size();
            } else {
                volumes[i] = 0;
            }
        }
        return volumes;
    }

    public Integer[] LatestVolumes(List<DataPatient> dataPatients) {
        SQLiteManager sqLiteManager = new SQLiteManager(mContext);

        Integer[] volumes = new Integer[dataPatients.size()];
        List<DataDrink> dataDrinks;
            for (int i = 0; i < dataPatients.size(); i++) {
                dataDrinks = sqLiteManager.getDrinkEventList(dataPatients.get(i).getP_key());
                if (!sqLiteManager.isDrinkEventListEmpty(dataPatients.get(i).getP_key())) {
                    int s = (int) dataDrinks.get(dataDrinks.size() - 1).getDevent_volumen();
                    volumes[i] = s;// + " ml";
                } else {
                    volumes[i] = -1;
                }
            }

        return volumes;
    }

    public String[] LatestTimes(List<DataPatient> dataPatients) {
        SQLiteManager sqLiteManager = new SQLiteManager(mContext);

        String[] dates = new String[dataPatients.size()];
        List<DataDrink> dataDrinks;
        Date d1;

        for(int i = 0; i < dataPatients.size(); i++){
            dataDrinks = sqLiteManager.getDrinkEventList(dataPatients.get(i).getP_key());
            if (!sqLiteManager.isDrinkEventListEmpty(dataPatients.get(i).getP_key())){
                d1 = dataDrinks.get(dataDrinks.size()-1).getDevent_timestamp();
                if(DateUtils.isToday(d1.getTime())){
                    //SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
                    dates[i] = "HEUTE";
                    }
                else{
                    SimpleDateFormat sdf2 = new SimpleDateFormat("dd.MM.yy");
                    dates[i] = sdf2.format(d1);
                }

            }
            else {
                dates[i] = "-";
            }
        }
        return dates;
    }

    public Integer[] createImageArray(List<DataPatient> dataPatients){
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

    public Integer getFullGlass(int vol){
        if(vol == 0){
            return R.drawable.drink_rec;
        }else if(vol < 150) {
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
    /*
    public List<DataPatient> getRedPatients(List<DataPatient> dataPatients, double d){
        List<DataPatient> RedPatients = new List<DataPatient>();

        RedPatients.add()
        List<DataDrink> Drinks;
        SQLiteManager sqLiteManager = new SQLiteManager(mContext);

        for(int i = 0; i<dataPatients.size();i++){
            Drinks = sqLiteManager.getDrinkEventList(dataPatients.get(i).getP_key());
            int c = Drinks.size()-1;
            int z = 0;
            int mean;
            if(Drinks.size() > 10) {
                for (int a = 0; a < 10; a++) {
                    z = z + (int) Drinks.get(c).getDevent_volumen();
                    c--;
                }
                mean = z/10;
            }
            else {
                for (a = 0; a < c; a++) {
                    z = z + (int) Drinks.get(c).getDevent_volumen();
                    c--;
                }
                mean = z/ Drinks.size();
            }

            if(mean < 500){
                List<Integer> w = new ArrayList<Integer>();
                w.add()

                        add(dataPatients.get(i));



                }
            }
        }
    }
    */
}
