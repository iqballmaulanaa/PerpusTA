package com.perpussapp.perpusapp.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class BukuModel implements Parcelable {
    String key, nama, gambar, category, penulis, tahun;

    public BukuModel() {
    }

    public BukuModel(String key, String nama, String gambar, String category, String penulis, String tahun) {
        this.key = key;
        this.nama = nama;
        this.gambar = gambar;
        this.category = category;
        this.penulis = penulis;
        this.tahun = tahun;
    }

    protected BukuModel(Parcel in) {
        key = in.readString();
        nama = in.readString();
        gambar = in.readString();
        category = in.readString();
        penulis = in.readString();
        tahun = in.readString();
    }

    public static final Creator<BukuModel> CREATOR = new Creator<BukuModel>() {
        @Override
        public BukuModel createFromParcel(Parcel in) {
            return new BukuModel(in);
        }

        @Override
        public BukuModel[] newArray(int size) {
            return new BukuModel[size];
        }
    };

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPenulis() {
        return penulis;
    }

    public void setPenulis(String penulis) {
        this.penulis = penulis;
    }

    public String getTahun() {
        return tahun;
    }

    public void setTahun(String tahun) {
        this.tahun = tahun;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(nama);
        dest.writeString(gambar);
        dest.writeString(category);
        dest.writeString(penulis);
        dest.writeString(tahun);
    }
}
