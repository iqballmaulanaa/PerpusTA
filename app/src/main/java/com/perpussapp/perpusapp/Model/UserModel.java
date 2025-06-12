package com.perpussapp.perpusapp.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserModel implements Parcelable {
    private String nis, nama, image, tempatLahir, alamat, password;
    private int jenisKelamin;
    private long tanggalLahir;
    private int isAdmin;
    public UserModel() {
    }

    public UserModel(String nis, String nama, String image, String tempatLahir, String alamat, String password, int jenisKelamin, long tanggalLahir, int isAdmin) {
        this.nis = nis;
        this.nama = nama;
        this.image = image;
        this.tempatLahir = tempatLahir;
        this.alamat = alamat;
        this.password = password;
        this.jenisKelamin = jenisKelamin;
        this.tanggalLahir = tanggalLahir;
        this.isAdmin = isAdmin;
    }

    protected UserModel(Parcel in) {
        nis = in.readString();
        nama = in.readString();
        image = in.readString();
        tempatLahir = in.readString();
        alamat = in.readString();
        password = in.readString();
        jenisKelamin = in.readInt();
        tanggalLahir = in.readLong();
        isAdmin = in.readInt();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTempatLahir() {
        return tempatLahir;
    }

    public void setTempatLahir(String tempatLahir) {
        this.tempatLahir = tempatLahir;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getJenisKelamin() {
        return jenisKelamin;
    }

    public void setJenisKelamin(int jenisKelamin) {
        this.jenisKelamin = jenisKelamin;
    }

    public long getTanggalLahir() {
        return tanggalLahir;
    }

    public void setTanggalLahir(long tanggalLahir) {
        this.tanggalLahir = tanggalLahir;
    }

    public int getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nis);
        dest.writeString(nama);
        dest.writeString(image);
        dest.writeString(tempatLahir);
        dest.writeString(alamat);
        dest.writeString(password);
        dest.writeInt(jenisKelamin);
        dest.writeLong(tanggalLahir);
        dest.writeInt(isAdmin);
    }
}
