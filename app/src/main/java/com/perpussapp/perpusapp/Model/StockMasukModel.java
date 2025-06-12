package com.perpussapp.perpusapp.Model;

import java.util.ArrayList;

public class StockMasukModel {
    private String key;
    private long tanggalMasuk;
    private ArrayList<ListBukuModel> listBuku;

    public StockMasukModel() {
    }

    public StockMasukModel(String key, long tanggalMasuk, ArrayList<ListBukuModel> listBuku) {
        this.key = key;
        this.tanggalMasuk = tanggalMasuk;
        this.listBuku = listBuku;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTanggalMasuk() {
        return tanggalMasuk;
    }

    public void setTanggalMasuk(long tanggalMasuk) {
        this.tanggalMasuk = tanggalMasuk;
    }

    public ArrayList<ListBukuModel> getListBuku() {
        return listBuku;
    }

    public void setListBuku(ArrayList<ListBukuModel> listBuku) {
        this.listBuku = listBuku;
    }
}
