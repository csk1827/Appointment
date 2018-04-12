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
import java.util.ArrayList;

public class TaskListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TaskDataAdapter dataAdapter;
    ArrayList<TaskDataModel> dataArrayList;
    SwipeRefreshLayout swipeToRefresh;
    LinearLayout no_tasks_layout;

    String dateSelected;

    ProgressDialog pd;

    HttpResponse httpResponse;

    int show_type = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        dateSelected = intent.getStringExtra("dateSelected");
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        show_type = app_preferences.getInt("show_type_state",1);

        if(show_type == 1) getSupportActionBar().setTitle(dateSelected+" (Assigned date)");
        else getSupportActionBar().setTitle(dateSelected+" (Completion date)");


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        swipeToRefresh = findViewById(R.id.swipeToRefresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(TaskListActivity.this));
        no_tasks_layout = findViewById(R.id.no_tasks_layout);

        dataArrayList = new ArrayList<>();
        dataAdapter = new TaskDataAdapter(TaskListActivity.this, dataArrayList);
        recyclerView.setAdapter(dataAdapter);

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

        String jsonArray = app_preferences.getString(dateSelected+"task"+show_type,"null");

        new getDataFromDataBase().execute();

        if(!jsonArray.equalsIgnoreCase("null")){
            try {
                showJSON(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
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

            TaskListActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.show();
                }
            });
            DefaultHttpClient httpclient = new DefaultHttpClient();

            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(TaskListActivity.this);
            String cookie = app_preferences.getString("cookie","null");
            String auth_id = app_preferences.getString("authid", "null");
            String user_id = app_preferences.getString("username", "null");

            String finalUrl = "";
            if(show_type == 1){
                finalUrl = "https://www.iitism.ac.in/index.php/appointment/appoint/get_assign_task_appointee_name_given_date_method/"+dateSelected+"/"+auth_id+"/"+user_id;
            }else{
                finalUrl = "https://www.iitism.ac.in/index.php/appointment/appoint/get_complt_task_appointee_name_given_date_method/"+dateSelected+"/"+auth_id+"/"+user_id;
            }

            HttpPost httpost = new HttpPost(finalUrl);
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
                Toast.makeText(TaskListActivity.this,"Please check your Internet connection",Toast.LENGTH_LONG).show();
            }

            TaskListActivity.this.runOnUiThread(new Runnable() {
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

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(TaskListActivity.this);
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putString(dateSelected+"task"+show_type,json);
        editor.apply();

        JSONArray jsonArray = new JSONArray(json);
        if(jsonArray.length() != 0){
            no_tasks_layout.setVisibility(View.GONE);
            for(int i =0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                TaskDataModel taskDataModel = new TaskDataModel();
                taskDataModel.setAssignee_master_key(jsonObject.getString("assignee_master_key"));
                taskDataModel.setAssignee_emp_id(jsonObject.getString("assignee_emp_id"));
                taskDataModel.setAssignee_type(jsonObject.getString("assignee_type"));
                taskDataModel.setAssigned_date(jsonObject.getString("assigned_date"));
                taskDataModel.setCompletion_date(jsonObject.getString("completion_date"));
                taskDataModel.setTask_info(jsonObject.getString("task_info"));
                taskDataModel.setDomain(jsonObject.getString("domain"));
                taskDataModel.setOther_info(jsonObject.getString("other_info"));
                taskDataModel.setStatus(jsonObject.getInt("status"));
                taskDataModel.setPriority(jsonObject.getInt("priority"));
                taskDataModel.setName(jsonObject.getString("name"));
                taskDataModel.setDesignation(jsonObject.getString("designation"));
                taskDataModel.setEmail(jsonObject.getString("email"));
                taskDataModel.setMobile_no(jsonObject.getString("mobile_no"));
                taskDataModel.setDepartment(jsonObject.getString("department"));
                taskDataModel.setStatus_name(jsonObject.getString("status_name"));
                taskDataModel.setAddress(jsonObject.getString("address"));

                dataArrayList.add(taskDataModel);
            }

        }
        else{
            no_tasks_layout.setVisibility(View.VISIBLE);
        }

        dataAdapter.notifyDataSetChanged();
    }



}
