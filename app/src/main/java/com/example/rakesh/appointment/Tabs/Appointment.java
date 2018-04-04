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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.rakesh.appointment.AppointmentsTMListActivity;
import com.example.rakesh.appointment.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
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


public class Appointment extends Fragment {

    List<EventDay> events;
    CalendarView calendarView;

    SwipeRefreshLayout swipeToRefresh;

    ProgressDialog pd;

    HttpResponse httpResponse;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_appointment, container, false);
        swipeToRefresh = view.findViewById(R.id.swipeToRefresh);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        calendarView = getActivity().findViewById(R.id.calendarView);


        try {
            calendarView.setDate(Calendar.getInstance());
        } catch (OutOfDateRangeException e) {
            e.printStackTrace();
        }

        events = new ArrayList<>();

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Intent intent = new Intent(getActivity(), AppointmentsTMListActivity.class);
                Date date = eventDay.getCalendar().getTime();
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                intent.putExtra("dateSelected", df.format(date));
                startActivity(intent);
            }
        });


        pd = new ProgressDialog(getActivity());
        pd.setMessage("Please wait...");
        pd.setCancelable(false);


        //pd.show();
        new getLoginDataFromDataBase().execute();

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String jsonArray = app_preferences.getString("badgeCounts", "null");

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
                new getLoginDataFromDataBase().execute();
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        swipeToRefresh.setRefreshing(false);
    }

    @SuppressLint("StaticFieldLeak")
    public class getLoginDataFromDataBase extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.show();
                }
            });
            DefaultHttpClient httpclient = new DefaultHttpClient();

            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String username = app_preferences.getString("username", "null");
            String password = app_preferences.getString("password", "null");

            HttpPost httpost = new HttpPost("https://www.iitism.ac.in/index.php/user_verification/pseudo_login/"+username+"/"+password);

            try {
                httpResponse = httpclient.execute(httpost);
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<Cookie> cookies = httpclient.getCookieStore().getCookies();
            httpclient.getConnectionManager().shutdown();
            if (cookies.size() == 0) return "";
            return cookies.get(0).getValue().toString();
        }

        @Override
        protected void onPostExecute(String result) {

            if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {
                SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = app_preferences.edit();
                editor.putString("cookie", result);
                editor.apply();
                new getRequiredDetails().execute();
            }
            else{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        swipeToRefresh.setRefreshing(false);
                    }
                });
                Toast.makeText(getActivity(),"Please check your Internet connection",Toast.LENGTH_LONG).show();
            }
        }

    }

    public class getRequiredDetails extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String cookie = app_preferences.getString("cookie", "null");
            String auth_id = app_preferences.getString("authid", "null");
            String user_id = app_preferences.getString("username", "null");
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpost = new HttpPost("https://www.iitism.ac.in/index.php/appointment/appoint/get_badge_count_by_priority_method/"+auth_id+"/"+user_id);
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
        editor.putString("badgeCounts", jsonArray);
        editor.apply();

        JSONArray result = new JSONArray(jsonArray);

        for (int i = 0; i < result.length(); i++) {
            Calendar calendar1 = Calendar.getInstance();
            try {
                row = result.getJSONObject(i);
                temp = row.getString("appoint_date");
                counter = row.getInt("appiont_ctr");
                Log.e("here", temp+"+"+row.getString("appiont_ctr"));


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
