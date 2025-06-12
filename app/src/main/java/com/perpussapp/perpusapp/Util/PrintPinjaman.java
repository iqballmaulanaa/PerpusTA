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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.perpussapp.perpusapp.Model.BukuModel;
import com.perpussapp.perpusapp.Model.ListBukuModel;
import com.perpussapp.perpusapp.Model.PinjamModel;
import com.perpussapp.perpusapp.Model.PrintPinjamanModel;
import com.perpussapp.perpusapp.Model.UserModel;
import com.perpussapp.perpusapp.R;
import com.perpussapp.perpusapp.Util.BaseActivity;

import java.util.ArrayList;

public class PrintPinjaman {
    private String TAG ="PrintPinjamanTAG";
    private Context context;
    private long dariTgl;
    private long hinggaTgl;
    private ArrayList<BukuModel> bukuModels;
    private ArrayList<UserModel> userModels;
    private ArrayList<PrintPinjamanModel> printPinjamanModels = new ArrayList<>();
    private ArrayList<PinjamModel> pinjamModels = new ArrayList<>();
    private   DatabaseReference mDatabase;
    private Constant constant = new Constant(context);
    public PrintPinjaman( Context context, long dariTgl, long hinggaTgl,  DatabaseReference mDatabase, ArrayList<BukuModel> bukuModels, ArrayList<UserModel> userModels) {

        this.context = context;
        this.dariTgl = dariTgl;
        this.hinggaTgl = hinggaTgl;
        this.bukuModels = bukuModels;
        this.userModels = userModels;
        this.mDatabase = mDatabase;
        getData();
    }

    private void getData() {
        Query query =  mDatabase.child("listPinjam")
                .orderByChild("tanggal")
                .startAt(dariTgl)
                .endAt(hinggaTgl);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pinjamModels.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    PinjamModel pinjamModel = dataSnapshot.getValue(PinjamModel.class);
                    pinjamModel.setKey(dataSnapshot.getKey());
                    pinjamModels.add(pinjamModel);
                    Log.i(TAG, "onDataChange: "+dataSnapshot.getKey());
                    getList(pinjamModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    int y = 0;
    int x = 0;
    private void getList(PinjamModel pinjamModel) {
        mDatabase.child("listBookPinjam").child(pinjamModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        y = (int) (snapshot.getChildrenCount()+y);
                        Log.d(TAG, "onDataChangepan: "+ snapshot.getChildrenCount());
                        PrintPinjamanModel print  = new PrintPinjamanModel();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            ListBukuModel l = dataSnapshot.getValue(ListBukuModel.class);
                            print.setJmlh(l.getJumlah());
                            print.setNamaBuku(getNamaBuku(l.getBukuKey()));
                            print.setNis(pinjamModel.getNis());
                            print.setTangal(constant.changeFromLong(pinjamModel.getTanggal()));
                            print.setNama(getNamaSiswa(pinjamModel.getNis()));
                            print.setTanggalKembali("-");
                            Log.d(TAG, "onDataChangegetList: "+getNamaBuku(l.getBukuKey()));

                            Query query =  mDatabase.child("listKembali")
                                    .orderByChild("tanggalKembali")
                                    .startAt(dariTgl)
                                    .endAt(hinggaTgl);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    x = (int) (snapshot.getChildrenCount()+x);
                                    Log.d(TAG, "onDataChangekey: "+snapshot.getChildrenCount());
                                    for (DataSnapshot d :snapshot.getChildren()){
                                        if (d.getKey().equals(pinjamModel.getKey())) {

                                            long tgl = d.child("tanggalKembali").getValue(Long.class);
                                            print.setTanggalKembali(constant.changeFromLong(tgl));
                                            Log.d(TAG, "onDataChangeb: "+print.getTanggalKembali());
                                        }
                                    }

                                    printPinjamanModels.add(print);
                                    if (x==y){
                                        pagePrint();
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }

                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
