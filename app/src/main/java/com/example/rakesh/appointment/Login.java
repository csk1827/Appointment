package com.example.rakesh.appointment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Login extends AppCompatActivity {

    EditText username, password;
    Button login;
    String userName,userPassword;

    ProgressDialog pd;
    HttpResponse httpResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(Login.this);
        String usernameTemp = app_preferences.getString("username", "null");
        if(!usernameTemp.equalsIgnoreCase("null")){
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = username.getText().toString();
                userPassword = password.getText().toString();

                new getLoginDataFromDataBase().execute();
            }
        });
    }


    @SuppressLint("StaticFieldLeak")
    public class getLoginDataFromDataBase extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
           Login.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.show();
                }
            });
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpost = new HttpPost("https://www.iitism.ac.in/index.php/user_verification/pseudo_login/"+userName+"/"+userPassword);

            JSONObject jsonArray = null;
            try {
                httpResponse = httpclient.execute(httpost);
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    String retSrc = EntityUtils.toString(entity);
                    jsonArray = new JSONObject(retSrc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {

                List<Cookie> cookies = httpclient.getCookieStore().getCookies();
                httpclient.getConnectionManager().shutdown();
                if (cookies.size() != 0){
                    SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(Login.this);
                    SharedPreferences.Editor editor = app_preferences.edit();
                    editor.putString("cookie", cookies.get(0).getValue().toString());
                    editor.apply();
                }
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {
                try {
                    if(result.getString("status").equalsIgnoreCase("false")){
                        Login.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                            }
                        });
                        //login not successfull
                        Snackbar.make(findViewById(android.R.id.content),"Oops, username and password doesn't match!",Snackbar.LENGTH_LONG).show();
                        password.setText("");
                    }else if(result.getString("status").equalsIgnoreCase("true")){
                        //successfully logged in
                        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(Login.this);
                        SharedPreferences.Editor editor = app_preferences.edit();
                        editor.putString("username", userName);
                        editor.putString("password", userPassword);
                        editor.putString("authid",result.getString("auth"));
                        editor.apply();
                        Toast.makeText(Login.this,"Login successful",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                Login.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                });
                Snackbar.make(findViewById(android.R.id.content),"Please check your Internet connection",Snackbar.LENGTH_LONG).show();
            }

        }

    }




}
