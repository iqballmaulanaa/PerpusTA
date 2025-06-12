package com.perpussapp.perpusapp.Model;

public class ListBukuModel {
    private String bukuKey;
    private int jumlah;

    public ListBukuModel() {
    }

    public ListBukuModel(String bukuKey, int jumlah) {
        this.bukuKey = bukuKey;
        this.jumlah = jumlah;
    }

    public String getBukuKey() {
        return bukuKey;
    }

    public void setBukuKey(String bukuKey) {
        this.bukuKey = bukuKey;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }
}
