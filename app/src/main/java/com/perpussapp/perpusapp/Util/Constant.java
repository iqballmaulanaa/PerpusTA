package com.perpussapp.perpusapp.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.perpussapp.perpusapp.Model.BukuModel;
import com.perpussapp.perpusapp.Model.UserModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Constant {
    private Context context;

    public Constant(Context context) {
        this.context = context;
    }

    public static String about_app="about app";
    private static String userId= "userId";
    private static String level= "level";
    private static SharedPreferences mySharedPreferences;
    private static String PREF = "pref";

    public static void setLevel(Context context, int l){
        mySharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEditor = mySharedPreferences.edit();
        myEditor.putInt(level, l);
        myEditor.commit();
    }

    public static int getLevel(Context context){
        mySharedPreferences = context.getSharedPreferences(PREF, 0);
        return mySharedPreferences.getInt(level,0);
    }


    public static void setUserId(Context context, String url){
        mySharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEditor = mySharedPreferences.edit();
        myEditor.putString(userId, url);
        myEditor.commit();
    }

    public static String getUserId(Context context){
        mySharedPreferences = context.getSharedPreferences(PREF, 0);
        return mySharedPreferences.getString(userId,null);
    }


    public long changeYyyyMMDDtoMili(String tgl){
        SimpleDateFormat sf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sf.parse (tgl);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public String changeFromLong(long date){
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormater.format(date);
    }
    public String changeFromLong2(long date){
        SimpleDateFormat dateFormater = new SimpleDateFormat("dd MMMM yyyy");
        return dateFormater.format(date);
    }

    public String changeFromDate(Date date){
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormater.format(date);
    }

    private ArrayList <UserModel> userModels = new ArrayList<>();

    public void setListUser(ArrayList<UserModel> userModels){
        this.userModels = userModels;
    }
    public String getNamaByNis(String Nis){
        for (int i =0; i<userModels.size(); i++){
            if (userModels.get(i).getNis().equals(Nis)){
                return userModels.get(i).getNama();
            }
        }
        return null;
    }


    public String getNis(int pos){
        return userModels.get(pos).getNis();
    }

    public String[] getSiswaNisNama(){
        String[] siswaNisNama   = new String[userModels.size()];
        for (int i =0; i<userModels.size(); i++){
            siswaNisNama[i] = "(NIS : "+userModels.get(i).getNis() + ") "+ userModels.get(i).getNama();
        }
        return siswaNisNama;
    }

    private ArrayList <BukuModel> bukuModels = new ArrayList<>();

    public void setListBuku(ArrayList<BukuModel> bukuModels){
        this.bukuModels = bukuModels;
    }

    public int getPos(String key){
        for (int i =0; i<bukuModels.size(); i++){
            if (bukuModels.get(i).equals(key)){
                return i;
            }
        }
        return 0;
    }

    public String getBukuKey(int pos){
        return bukuModels.get(pos).getKey();
    }

    public String[] getBukuNama(){
        String[] bukuKey   = new String[bukuModels.size()];
        for (int i =0; i<bukuModels.size(); i++){
            bukuKey[i] =  bukuModels.get(i).getNama();
        }
        return bukuKey;
    }
}
