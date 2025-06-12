package com.perpussapp.perpusapp.Model;

public class PrintPinjamanModel {
    private int jmlh;
    private String nis, nama, namaBuku,  tangal, tglBatas, tanggalKembali;

    public PrintPinjamanModel() {
    }

    public PrintPinjamanModel(int jmlh, String nis, String nama, String namaBuku, String tangal, String tglBatas, String tanggalKembali) {
        this.jmlh = jmlh;
        this.nis = nis;
        this.nama = nama;
        this.namaBuku = namaBuku;
        this.tangal = tangal;
        this.tglBatas = tglBatas;
        this.tanggalKembali = tanggalKembali;
    }

    public int getJmlh() {
        return jmlh;
    }

    public void setJmlh(int jmlh) {
        this.jmlh = jmlh;
    }

    public String getNis() {
        return nis;
    }

    public void setNis(String nis) {
        this.nis = nis;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNamaBuku() {
        return namaBuku;
    }

    public void setNamaBuku(String namaBuku) {
        this.namaBuku = namaBuku;
    }

    public String getTangal() {
        return tangal;
    }

    public void setTangal(String tangal) {
        this.tangal = tangal;
    }

    public String getTglBatas() {
        return tglBatas;
    }

    public void setTglBatas(String tglBatas) {
        this.tglBatas = tglBatas;
    }

    public String getTanggalKembali() {
        return tanggalKembali;
    }

    public void setTanggalKembali(String tanggalKembali) {
        this.tanggalKembali = tanggalKembali;
    }
}
