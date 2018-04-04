package com.example.rakesh.appointment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AppointmentsTMListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DataAdapter dataAdapter;
    ArrayList<Data> dataArrayList;
    SwipeRefreshLayout swipeToRefresh;

    LinearLayout no_appointments_layout;

    String dateSelected;

    ProgressDialog pd;

    HttpResponse httpResponse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_tm_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        dateSelected = intent.getStringExtra("dateSelected");

        getSupportActionBar().setTitle(dateSelected);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        swipeToRefresh = findViewById(R.id.swipeToRefresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(AppointmentsTMListActivity.this));
        no_appointments_layout = findViewById(R.id.no_appointments_layout);

        dataArrayList = new ArrayList<>();
        dataAdapter = new DataAdapter(AppointmentsTMListActivity.this, dataArrayList);
        recyclerView.setAdapter(dataAdapter);

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String jsonArray = app_preferences.getString(dateSelected,"null");

        new getDataFromDataBase().execute();

        if(!jsonArray.equalsIgnoreCase("null")){
            try {
                showJSON(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            new getDataFromDataBase().execute();
        }


        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new getDataFromDataBase().execute();
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        swipeToRefresh.setRefreshing(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("StaticFieldLeak")
    public class getDataFromDataBase extends AsyncTask<String, Integer, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... params) {

            AppointmentsTMListActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.show();
                }
            });
            DefaultHttpClient httpclient = new DefaultHttpClient();

            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(AppointmentsTMListActivity.this);
            String cookie = app_preferences.getString("cookie","null");
            String auth_id = app_preferences.getString("authid", "null");
            String user_id = app_preferences.getString("username", "null");

            HttpPost httpost = new HttpPost("https://www.iitism.ac.in/index.php/appointment/appoint/get_appointee_all_given_date_method/"
                    +dateSelected+"/"+auth_id+"/"+user_id);
            httpost.addHeader("Cookie","misci_session="+cookie);
            JSONArray jsonArray = null;
            try {
                httpResponse = httpclient.execute(httpost);
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    String retSrc = EntityUtils.toString(entity);
                    jsonArray = new JSONArray(retSrc);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            if(httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200 && result != null){
                try {
                    showJSON(result.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else if(httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200 && result == null){
                try {
                    showJSON("[]");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(AppointmentsTMListActivity.this,"Please check your Internet connection",Toast.LENGTH_LONG).show();
            }

            AppointmentsTMListActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swipeToRefresh.setRefreshing(false);
                    pd.dismiss();
                }
            });
        }
    }

    public void showJSON(String json) throws JSONException, ParseException {
        dataArrayList.clear();

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(AppointmentsTMListActivity.this);
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putString(dateSelected,json);
        editor.apply();

        JSONArray jsonArray = new JSONArray(json);
        if(jsonArray.length() != 0){
            no_appointments_layout.setVisibility(View.GONE);
            for(int i =0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Data data = new Data();
                data.setName(jsonObject.getString("name"));
                data.setDesignation(jsonObject.getString("designation"));
                data.setDepartment(jsonObject.getString("department"));
                data.setEmail(jsonObject.getString("email"));
                data.setMobile_no(jsonObject.getString("mobile_no"));
                data.setAppointee_type(jsonObject.getString("appointee_type"));

                String dateAndTime = jsonObject.getString("appoint_date");
                Date dateObj = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault()).parse(dateAndTime);
                String time = new SimpleDateFormat("hh:mm a",Locale.getDefault()).format(dateObj);
                String date = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault()).format(dateObj);

                data.setAppoint_date(date);
                data.setAppoint_time(time);
                data.setVenue(jsonObject.getString("venue"));
                data.setPurpose(jsonObject.getString("purpose"));
                data.setOther_info(jsonObject.getString("other_info"));
                data.setAppointee_master_key(jsonObject.getInt("appointee_master_key"));
                data.setAppointment_status(jsonObject.getInt("status"));
                data.setStatusName(jsonObject.getString("status_name"));
                dataArrayList.add(data);
            }

        }
        else{
           no_appointments_layout.setVisibility(View.VISIBLE);
        }

        dataAdapter.notifyDataSetChanged();
    }

}
