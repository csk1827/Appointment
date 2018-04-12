package com.example.rakesh.appointment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AppointmentDetails extends AppCompatActivity {

    TextView tvName,tvDesignation,tvTime,tvVenue,tvEmail,tvMobileNo,tvPurpose,tvAppointeeType,tvOthers,tvStatus;
    LinearLayout othersLayout,emailLayout,mobileNoLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment__details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Appointment details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final AppointmentDataModel appointmentDataModel = intent.getParcelableExtra("particular_appointment");

        tvName = findViewById(R.id.tvName);
        tvDesignation = findViewById(R.id.tvDesignation);
        tvTime = findViewById(R.id.tvTime);
        tvVenue = findViewById(R.id.tvVenue);
        tvEmail = findViewById(R.id.tvEmail);
        tvMobileNo = findViewById(R.id.tvMobileNo);
        tvPurpose = findViewById(R.id.tvPurpose);
        tvAppointeeType = findViewById(R.id.tvAppointeeType);
        tvOthers = findViewById(R.id.tvOthers);
        othersLayout = findViewById(R.id.othersLayout);
        tvStatus = findViewById(R.id.tvStatus);
        emailLayout = findViewById(R.id.emailLayout);
        mobileNoLayout = findViewById(R.id.mobileNoLayout);

        tvName.setText(appointmentDataModel.getName());
        tvDesignation.setText(appointmentDataModel.getDesignation());
        tvTime.setText(appointmentDataModel.getAppoint_time());
        tvVenue.setText(appointmentDataModel.getVenue());
        tvPurpose.setText(appointmentDataModel.getPurpose());
        tvStatus.setText(appointmentDataModel.getStatusName());

        if(appointmentDataModel.getOther_info() != null && (!appointmentDataModel.getOther_info().equalsIgnoreCase("null") && !appointmentDataModel.getOther_info().equalsIgnoreCase("") )){
            othersLayout.setVisibility(View.VISIBLE);
            tvOthers.setText(appointmentDataModel.getOther_info());
        }

        if(appointmentDataModel.getEmail() != null && (!appointmentDataModel.getEmail().equalsIgnoreCase("null") && !appointmentDataModel.getEmail().equalsIgnoreCase("") )){
            emailLayout.setVisibility(View.VISIBLE);
            tvEmail.setText(appointmentDataModel.getEmail());
        }

        if(appointmentDataModel.getMobile_no() != null && (!appointmentDataModel.getMobile_no().equalsIgnoreCase("null") && !appointmentDataModel.getMobile_no().equalsIgnoreCase("") )){
            mobileNoLayout.setVisibility(View.VISIBLE);
            tvMobileNo.setText(appointmentDataModel.getMobile_no());
        }

        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
                emailIntent.setType("message/rfc822");
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                        new String[]{appointmentDataModel.getEmail()});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        "Regarding Appointment");
                startActivity(Intent.createChooser(emailIntent, "Regarding Appointment"));
            }
        });

        tvMobileNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make a call to the phone no
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Mobile_No", appointmentDataModel.getMobile_no());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(AppointmentDetails.this,"Mobile number copied to clipboard.",Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
