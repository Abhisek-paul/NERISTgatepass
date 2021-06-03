package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class login extends AppCompatActivity {
private EditText Email;
private EditText Password;
private Button Login;
    SQLiteDatabase sqlitedb;
    @Override
    public void onBackPressed() {
        // your code.
        this.finishAffinity();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqlitedb=openOrCreateDatabase("Login_state", Context.MODE_PRIVATE,null);
        sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS login(ID INTEGER PRIMARY KEY AUTOINCREMENT,token VARCHAR(255),State BOOLEAN);");

        setContentView(R.layout.activity_login2);
        Email = (EditText)findViewById(R.id.txt_email);
        Password = (EditText)findViewById(R.id.txt_password);
        Login = (Button)findViewById(R.id.btn_login);
        Cursor c = sqlitedb.rawQuery("Select *From login Where State='"+1+"'",null);
        if (c.moveToFirst()){
            Intent intent = new Intent(login.this,MainActivity.class);
            startActivity(intent);
        }
    Login.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            validate(Email.getText().toString(),Password.getText().toString());
        }
    });
    }
    private void validate(String email,String password){/*
        if(email.equals("abhi") && password.equals("abhi")){

            Intent intent = new Intent(login.this,MainActivity.class);
            startActivity(intent);
            sqlitedb.execSQL("Insert into login(State)VALUES('"+1+"');");

        }else{
            new SweetAlertDialog(login.this,SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Credentials not found").show();
        }*/

        String url ="http://15.206.157.177/api/auth";
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", Email.getText().toString());
            jsonBody.put("password", Password.getText().toString());
            final String mRequestBody = jsonBody.toString();

            final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Intent intent = new Intent(login.this,MainActivity.class);
                    startActivity(intent);
                    sqlitedb.execSQL("Insert into login(State,token)VALUES('"+1+"','"+response+"');");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    new SweetAlertDialog(login.this,SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Credentials not found").show();

                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }


            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
