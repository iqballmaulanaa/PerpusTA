package com.perpussapp.perpusapp.Model;

public class KembaliModel {
    private String pinjamKey;
    private long tanggalKembali;

    public KembaliModel() {
    }

    public KembaliModel(String pinjamKey, long tanggalKembali) {
        this.pinjamKey = pinjamKey;
        this.tanggalKembali = tanggalKembali;
    }

    public String getPinjamKey() {
        return pinjamKey;
    }

    public void setPinjamKey(String pinjamKey) {
        this.pinjamKey = pinjamKey;
    }

    public long getTanggalKembali() {
        return tanggalKembali;
    }

    public void setTanggalKembali(long tanggalKembali) {
        this.tanggalKembali = tanggalKembali;
    }
}
