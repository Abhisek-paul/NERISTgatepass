package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.ui.main.MainFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import android.database.sqlite.SQLiteDatabase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {
    private Button generate,scan,logout,verify;
    Integer State;
    SQLiteDatabase sqlitedb;
    private EditText mytext;
    private ImageView qr_code;
    @Override
    public void onBackPressed() {
        // your code.
        this.finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        generate=findViewById(R.id.generate);
        scan=findViewById(R.id.scan);
        logout=findViewById(R.id.logout);
        verify=findViewById(R.id.verify);
        verify.setVisibility(View.GONE);
        String in,out;
        sqlitedb=openOrCreateDatabase("Login_state", Context.MODE_PRIVATE,null);

logout.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        sqlitedb.execSQL("Delete From login Where State='"+1+"'");
        sqlitedb.execSQL("Insert into login(State)VALUES('"+0+"');");
        Intent intent = new Intent(MainActivity.this,login.class);
        startActivity(intent);
    }
});
      generate.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              State = 1;
              IntentIntegrator intentIntegrator= new IntentIntegrator (MainActivity.this);
              intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
              intentIntegrator.setCameraId(0);
              intentIntegrator.setOrientationLocked(false);
              intentIntegrator.setPrompt("Scanning");
              intentIntegrator.setBeepEnabled(true);
              intentIntegrator.setBarcodeImageEnabled(true);
              intentIntegrator.initiateScan();
          }
      });
      scan.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              State=2;
              IntentIntegrator intentIntegrator= new IntentIntegrator (MainActivity.this);
              intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
              intentIntegrator.setCameraId(0);
              intentIntegrator.setOrientationLocked(false);
              intentIntegrator.setPrompt("Scanning");
              intentIntegrator.setBeepEnabled(true);
              intentIntegrator.setBarcodeImageEnabled(true);
              intentIntegrator.initiateScan();

          }
      });
    verify.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {State=3;
            IntentIntegrator intentIntegrator= new IntentIntegrator (MainActivity.this);
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            intentIntegrator.setCameraId(0);
            intentIntegrator.setOrientationLocked(false);
            intentIntegrator.setPrompt("Scanning");
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.setBarcodeImageEnabled(true);
            intentIntegrator.initiateScan();
        }
    });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        /*if (result!=null && result.getContents()!=null){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Scan Result")
                    .setMessage(result.getContents())
                    .setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData data = ClipData.newPlainText("result",result.getContents());
                            manager.setPrimaryClip(data);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();

        }*/if(result!=null && result.getContents()!=null){
        if (State == 1) {String path = check(result.getContents());
            String url = null;
            try {
                url = "http://15.206.157.177/api/"+path+ URLEncoder.encode(result.getContents().substring(2),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    new SweetAlertDialog(MainActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Allow").show();
                    verify.setVisibility(View.GONE);
                    requestQueue.stop();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    new SweetAlertDialog(MainActivity.this,SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Stop").show();
                    verify.setVisibility(View.GONE);
                    requestQueue.stop();
                }
            })
            {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    final Cursor c = sqlitedb.rawQuery("Select *From login Where State='"+1+"'",null);
                    if(c.moveToFirst()){
                        String x_auth_token = c.getString(1);

                    headers.put("x-auth-token",x_auth_token);}
                    return headers;
                }
            };
            requestQueue.add(stringRequest);
        } else if(State==2){/*Coming Inside*/
            String path = check(result.getContents());
            String url = null;
            try {
                url = "http://15.206.157.177/api/"+path+ URLEncoder.encode(result.getContents().substring(2),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    new SweetAlertDialog(MainActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Allow").show();
                    verify.setVisibility(View.GONE);
                    requestQueue.stop();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    new SweetAlertDialog(MainActivity.this,SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Stop").show();
                    verify.setVisibility(View.VISIBLE);
                    requestQueue.stop();
                }
            });
            requestQueue.add(stringRequest);
        }
        else{
            String path = check(result.getContents());
            String url = null;
            try {
                url = "http://15.206.157.177/api/"+path+ URLEncoder.encode(result.getContents().substring(2),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    new SweetAlertDialog(MainActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Allow").show();
                    verify.setVisibility(View.GONE);
                    requestQueue.stop();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    new SweetAlertDialog(MainActivity.this,SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Stop").show();
                    verify.setVisibility(View.VISIBLE);
                    requestQueue.stop();
                }
            });
            requestQueue.add(stringRequest);
        }
    }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private String check(String contents) {
        String x=contents.substring(0,2);
        if (x.equals("t1")) {
            return "studentEntry/";
        }
        if(x.equals("t2")){
            return "facultyEntry/";
        }
        if (x.equals("t3")) {
            return "staffEntry/";
        }
        return "";
    }
}
