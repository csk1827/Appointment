package com.example.rakesh.appointment.Tabs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.rakesh.appointment.AppointmentsListActivity;
import com.example.rakesh.appointment.R;
import com.example.rakesh.appointment.TaskListActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TManagement extends Fragment {

    List<EventDay> events;
    CalendarView calendarView;
    ProgressDialog pd;

    SwipeRefreshLayout swipeToRefresh;

    HttpResponse httpResponse;

    static int show_type = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_tmanagement, container, false);
        swipeToRefresh = view.findViewById(R.id.swipeToRefresh);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        calendarView = getActivity().findViewById(R.id.calendarView1);
        setHasOptionsMenu(true);

        try {
            calendarView.setDate(Calendar.getInstance());
        } catch (OutOfDateRangeException e) {
            e.printStackTrace();
        }

        events = new ArrayList<>();

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Intent intent = new Intent(getActivity(), TaskListActivity.class);
                Date date = eventDay.getCalendar().getTime();
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                intent.putExtra("dateSelected", df.format(date));
                startActivity(intent);
            }
        });

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        show_type = app_preferences.getInt("show_type_state",1);
        new getRequiredDetails().execute();

        String jsonArray = app_preferences.getString("taskBadgeCounts", "null");

        if (!jsonArray.equalsIgnoreCase("null")) {
            try {
                showJSON(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new getRequiredDetails().execute();
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        swipeToRefresh.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_task_management, menu);

        //retrieves the last selected show-by position, which is saved in sharedpreferencse and display the corresponding result
        if(show_type ==1){
            MenuItem actionRestart = menu.findItem(R.id.menu_sort_by_assigned_date);
            actionRestart.setChecked(true);
        }else if(show_type ==2){
            MenuItem actionRestart = menu.findItem(R.id.menu_sort_by_compl_date);
            actionRestart.setChecked(true);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_sort){
            return true;
        }
        else if(item.getItemId() == R.id.menu_sort_by_assigned_date){
            item.setChecked(true);
            //saving the show state
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putInt("show_type_state", 1);
            editor.apply();

            show_type = 1;
            new getRequiredDetails().execute();

        }
        else if(item.getItemId() == R.id.menu_sort_by_compl_date){
            item.setChecked(true);
            //saving the show state
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putInt("show_type_state", 2);
            editor.apply();

            show_type = 2;
            new getRequiredDetails().execute();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    public class getRequiredDetails extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.show();
                }
            });

            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String cookie = app_preferences.getString("cookie", "null");
            String auth_id = app_preferences.getString("authid", "null");
            String user_id = app_preferences.getString("username", "null");
            DefaultHttpClient httpclient = new DefaultHttpClient();

            String finaleURL = "";
            if(show_type == 1){
                finaleURL = "https://www.iitism.ac.in/index.php/appointment/appoint/get_assign_task_badge_count_by_priority_method/"+auth_id+"/"+user_id;
            }else{
                finaleURL = "https://www.iitism.ac.in/index.php/appointment/appoint/get_complt_task_badge_count_by_priority_method/"+auth_id+"/"+user_id;
            }

            HttpPost httpost = new HttpPost(finaleURL);
            httpost.addHeader("Cookie", "misci_session=" + cookie);
            HttpEntity entity = null;
            try {
                httpResponse = httpclient.execute(httpost);
                entity = httpResponse.getEntity();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String jsonArray = null;

            try {
                if (entity != null) jsonArray = EntityUtils.toString(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }


            httpclient.getConnectionManager().shutdown();
            if (jsonArray != null)
                return jsonArray;

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.dismiss();
                    swipeToRefresh.setRefreshing(false);
                }
            });

            if(httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200 && !result.equalsIgnoreCase("0")){
                try {
                    showJSON(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if(httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200 && result.equalsIgnoreCase("0")){
                try {
                    showJSON("[]");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(getActivity(),"Please check your Internet connection",Toast.LENGTH_LONG).show();
            }
        }
    }


    void showJSON(String jsonArray) throws JSONException {
        events.clear();
        JSONObject row;
        String temp;
        Date date = null;
        Integer counter;
        DateFormat formatter;

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putString("taskBadgeCounts", jsonArray);
        editor.apply();

        JSONArray result = new JSONArray(jsonArray);

        for (int i = 0; i < result.length(); i++) {
            Calendar calendar1 = Calendar.getInstance();
            try {
                row = result.getJSONObject(i);
                if(show_type == 1){
                    temp = row.getString("assiged_time");
                    counter = row.getInt("assigned_task_ctr");

                }else{
                    temp = row.getString("completion_time");
                    counter = row.getInt("completion_task_ctr");
                }

                formatter = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    date = formatter.parse(temp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                calendar1.setTime(date);
                switch (counter) {
                    case 1:
                        events.add(new EventDay(calendar1, R.mipmap.one));
                        break;
                    case 2:
                        events.add(new EventDay(calendar1, R.mipmap.two));
                        break;
                    case 3:
                        events.add(new EventDay(calendar1, R.mipmap.three));
                        break;
                    case 4:
                        events.add(new EventDay(calendar1, R.mipmap.four));
                        break;
                    case 5:
                        events.add(new EventDay(calendar1, R.mipmap.five));
                        break;
                    case 6:
                        events.add(new EventDay(calendar1, R.mipmap.six));
                        break;
                    case 7:
                        events.add(new EventDay(calendar1, R.mipmap.seven));
                        break;
                    case 8:
                        events.add(new EventDay(calendar1, R.mipmap.eight));
                        break;
                    case 9:
                        events.add(new EventDay(calendar1, R.mipmap.nine));
                        break;
                    case 10:
                        events.add(new EventDay(calendar1, R.mipmap.ten));
                        break;
                    case 11:
                        events.add(new EventDay(calendar1, R.mipmap.eleven));
                        break;
                    case 12:
                        events.add(new EventDay(calendar1, R.mipmap.tweleve));
                        break;
                    case 13:
                        events.add(new EventDay(calendar1, R.mipmap.thirteen));
                        break;
                    case 14:
                        events.add(new EventDay(calendar1, R.mipmap.fourteen));
                        break;
                    case 15:
                        events.add(new EventDay(calendar1, R.mipmap.fifteen));
                        break;
                    case 16:
                        events.add(new EventDay(calendar1, R.mipmap.sixteen));
                        break;
                    case 17:
                        events.add(new EventDay(calendar1, R.mipmap.seventeen));
                        break;
                    case 18:
                        events.add(new EventDay(calendar1, R.mipmap.eighteen));
                        break;
                    case 19:
                        events.add(new EventDay(calendar1, R.mipmap.nineteen));
                        break;
                    case 20:
                        events.add(new EventDay(calendar1, R.mipmap.twenty));
                        break;
                    case 21:
                        events.add(new EventDay(calendar1, R.mipmap.twenty_one));
                        break;
                    case 22:
                        events.add(new EventDay(calendar1, R.mipmap.twenty_two));
                        break;
                    case 23:
                        events.add(new EventDay(calendar1, R.mipmap.twenty_three));
                        break;
                    case 24:
                        events.add(new EventDay(calendar1, R.mipmap.twenty_four));
                        break;
                    case 25:
                        events.add(new EventDay(calendar1, R.mipmap.twenty_five));
                        break;
                    case 26:
                        events.add(new EventDay(calendar1, R.mipmap.twenty_six));
                        break;
                    case 27:
                        events.add(new EventDay(calendar1, R.mipmap.twenty_seven));
                        break;
                    case 28:
                        events.add(new EventDay(calendar1, R.mipmap.twenty_eight));
                        break;
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        calendarView.setEvents(events);

    }


}
