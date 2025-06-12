package com.perpussapp.perpusapp.Util;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.perpussapp.perpusapp.Model.BukuModel;
import com.perpussapp.perpusapp.Model.KembaliModel;
import com.perpussapp.perpusapp.Model.ListBukuModel;
import com.perpussapp.perpusapp.Model.PinjamModel;
import com.perpussapp.perpusapp.Model.PrintPinjamanModel;
import com.perpussapp.perpusapp.Model.UserModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;

import java.util.ArrayList;

public class PrintPinjaman2 {
    private String TAG ="PrintPinjamanTAG";
    private Context context;
    private long dariTgl;
    private long hinggaTgl;
    private ArrayList<BukuModel> bukuModels;
    private ArrayList<UserModel> userModels;
    private ArrayList<PrintPinjamanModel> printPinjamanModels = new ArrayList<>();
    private ArrayList<PinjamModel> pinjamModels = new ArrayList<>();
    private ArrayList<KembaliModel> kembaliModels;
    private   DatabaseReference mDatabase;
    private Constant constant = new Constant(context);
    public PrintPinjaman2(Context context, long dariTgl, long hinggaTgl, DatabaseReference mDatabase,
                          ArrayList<BukuModel> bukuModels, ArrayList<UserModel> userModels,
                          ArrayList<PinjamModel> pinjamModels, ArrayList<KembaliModel> kembaliModels) {

        this.context = context;
        this.dariTgl = dariTgl;
        this.hinggaTgl = hinggaTgl;
        this.bukuModels = bukuModels;
        this.userModels = userModels;
        this.mDatabase = mDatabase;
        this.pinjamModels = pinjamModels;
        this.kembaliModels = kembaliModels;
        getData();
    }

    private void getData() {
        mDatabase.child("listBookPinjam").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot :snapshot.getChildren()){
                           for (DataSnapshot ds : dataSnapshot.getChildren()){
                               PrintPinjamanModel print  = new PrintPinjamanModel();
                               ListBukuModel l = ds.getValue(ListBukuModel.class);
                               Log.d(TAG, "onDataChange: "+l.getJumlah() +dataSnapshot.getKey());
                               print.setJmlh(l.getJumlah());
                               print.setNamaBuku(getNamaBuku(l.getBukuKey()));
                               Log.d(TAG, "onDataChange: "+checkAvai(dataSnapshot.getKey()));
                               if (checkAvai(dataSnapshot.getKey())) {
                                   print.setNis(getNis(dataSnapshot.getKey()));
                                   print.setNama(getNamaSiswa(print.getNis()));
                                   print.setTangal(geTgl(dataSnapshot.getKey()));
                                    print.setTanggalKembali(checkKembali(dataSnapshot.getKey()));
                                    print.setTglBatas(geTglBatas(dataSnapshot.getKey()));
                                   printPinjamanModels.add(print);
                               }

                           }

                        }
                        pagePrint();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private String geTglBatas(String key) {
        for (int i =0 ; i<pinjamModels.size(); i++){
            if (pinjamModels.get(i).getKey().equals(key)){
                return constant.changeFromLong(pinjamModels.get(i).getTglBatas());
            }
        }
        return null;
    }

    private String checkKembali(String key) {
        for (int i =0 ; i<kembaliModels.size(); i++){
            if (kembaliModels.get(i).getPinjamKey().equals(key)){
                return constant.changeFromLong(kembaliModels.get(i).getTanggalKembali());
            }
        }
        return "-";
    }

    private boolean checkAvai(String key) {
        for (int i =0 ; i<pinjamModels.size(); i++){
            Log.d(TAG, "checkAvai: "+pinjamModels.get(i).getKey());
            if (pinjamModels.get(i).getKey().equals(key)){
                return true;
            }
        }
        return false;
    }

    private String geTgl(String key) {
        for (int i =0 ; i<pinjamModels.size(); i++){
            if (pinjamModels.get(i).getKey().equals(key)){
                return constant.changeFromLong(pinjamModels.get(i).getTanggal());
            }
        }
        return null;
    }


    private String getNis(String key) {
        for (int i =0 ; i<pinjamModels.size(); i++){
            if (pinjamModels.get(i).getKey().equals(key)){
                return pinjamModels.get(i).getNis();
            }
        }
        return null;
    }


    WebView myWebView;
    private void pagePrint() {
        if (myWebView==null){
            myWebView = new WebView(context);
            StringBuilder stringBuilder = new StringBuilder();
            String head = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    " <head>\n" +
                    "  <title>History Peminjaman Buku Perpustakaan</title>\n" +
                    "  <style type=\"text/css\">\n" +
                    "    table,th, td{\n" +
                    "        padding: 5px;\n" +
                    "    border: 1px solid black;\n" +
                    "    border-collapse: collapse; }\n" +
                    "    table{ width: 100%; }\n" +
                    "    th, td{ text-align:center; }\n" +
                    "  </style>\n" +
                    " </head>\n" +
                    "<body>\n" +
                    "    <center>HISTORY PEMINJAMAN BUKU PERPUSTAKAAN</center>\n" +
                    "    <center>TANGGAL"+constant.changeFromLong(dariTgl)+" / "+constant.changeFromLong(hinggaTgl)+ " </center>\n" +
                    " <table><thead> \n" +
                    "          <tr>\n" +
                    "            <th>No </th>\n" +
                    "            <th>NIS</th>\n" +
                    "            <th>Nama</th>\n" +
                    "            <th>Nama Buku</th>\n" +
                    "            <th>Jumlah Pinjam</th>\n" +
                    "            <th>Tanggal Pinjam</th>\n" +
                    "            <th>Tanggal Batas Pinjam</th>\n" +
                    "            <th>Tanggal Pengembalian</th>\n" +
                    "          </tr>\n" +
                    "    </thead>\n" +
                    "    <tbody>";
            stringBuilder.append(head);
            int o=1;
            for (int i = 0; i< printPinjamanModels.size(); i++){
                stringBuilder.append(" <tr>\n" +
                        "            <td>"+o+++"</td>\n" +
                        "            <td>"+printPinjamanModels.get(i).getNis()+"</td>\n" +
                        "            <td>"+printPinjamanModels.get(i).getNama()+"</td>\n" +
                        "            <td>"+printPinjamanModels.get(i).getNamaBuku()+"</td>\n" +
                        "            <td>"+printPinjamanModels.get(i).getJmlh()+"</td>\n" +
                        "            <td>"+printPinjamanModels.get(i).getTangal()+"</td>\n" +
                        "            <td>"+printPinjamanModels.get(i).getTglBatas()+"</td>\n" +
                        "            <td>"+printPinjamanModels.get(i).getTanggalKembali()+"</td>\n" +
                        "        </tr>");
            }
            stringBuilder.append("\n" +
                    "    </tbody>\n" +
                    "   \n" +
                    " </table>\n" +
                    " \n" +
                    "  \n" +
                    " </body>\n" +
                    "</html>");

            myWebView.getSettings().setJavaScriptEnabled(true);
            myWebView.getSettings().setBuiltInZoomControls(true);
            myWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            myWebView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    createWebPrintJob(myWebView);

                }
            });
            myWebView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    if (newProgress==100){
                    }
                    super.onProgressChanged(view, newProgress);
                }
            });
            myWebView.loadData(stringBuilder.toString(), "text/HTML", "UTF-8");
        }


    }

    private void createWebPrintJob(WebView webView) {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);

        String jobName = context.getString(R.string.app_name) ;
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        PrintAttributes.Builder builder = new PrintAttributes.Builder();

        builder.setMediaSize(PrintAttributes.MediaSize.NA_GOVT_LETTER);
        printManager.print(jobName, printAdapter,builder.build());

    }


    private String getNamaSiswa(String nis) {
        for (int i = 0;i<userModels.size();i++){
            if (userModels.get(i).getNis().equals(nis)){
                return userModels.get(i).getNama();
            }
        }
        return null;
    }

    private String getNamaBuku(String bukuKey){
        for (int i = 0;i<bukuModels.size();i++){
            if (bukuModels.get(i).getKey().equals(bukuKey)){
                return bukuModels.get(i).getNama();
            }
        }
        return null;
    }
}
