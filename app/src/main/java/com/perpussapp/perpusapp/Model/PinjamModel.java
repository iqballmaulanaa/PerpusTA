package com.perpussapp.perpusapp.Model;

public class PinjamModel {
    private String key, nis;
    private long tanggal, tglBatas;
    private long denda; // ✅ Tambahan: denda

    // Tambahan untuk filter pencarian
    private String namaUser;
    private String nisUser;

    // Tambahan untuk pencarian berdasarkan nama langsung
    private String nama;

    public PinjamModel() {
    }

    public PinjamModel(String key, String nis, String nama, long tanggal, long tglBatas) {
        this.key = key;
        this.nis = nis;
        this.nama = nama;
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

    public String getNamaUser() {
        return namaUser;
    }

    public void setNamaUser(String namaUser) {
        this.namaUser = namaUser;
    }

    public String getNisUser() {
        return nisUser;
    }

    public void setNisUser(String nisUser) {
        this.nisUser = nisUser;
    }

    // Getter dan setter untuk nama (penting untuk pencarian/filter)
    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    // ✅ Getter dan setter untuk denda
    public long getDenda() {
        return denda;
    }

    public void setDenda(long denda) {
        this.denda = denda;
    }
}
