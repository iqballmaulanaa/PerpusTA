package com.perpussapp.perpusapp.Model;

public class PinjamModel {
    private String key, nis;
    private long tanggal, tglBatas ;

    public PinjamModel() {
    }

    public PinjamModel(String key, String nis, long tanggal, long tglBatas) {
        this.key = key;
        this.nis = nis;
        this.tanggal = tanggal;
        this.tglBatas = tglBatas;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNis() {
        return nis;
    }

    public void setNis(String nis) {
        this.nis = nis;
    }

    public long getTanggal() {
        return tanggal;
    }

    public void setTanggal(long tanggal) {
        this.tanggal = tanggal;
    }

    public long getTglBatas() {
        return tglBatas;
    }

    public void setTglBatas(long tglBatas) {
        this.tglBatas = tglBatas;
    }
}
