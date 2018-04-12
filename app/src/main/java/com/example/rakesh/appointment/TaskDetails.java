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


public class TaskDetails extends AppCompatActivity {

    TextView tvName,tvDesignation,tvTaskAssigned,tvDepartment,tvAssigneeType,tvAssignedDate,tvCompletionDate,tvEmailId,tvMobileNo,tvTaskStatus,tvOthers;
    LinearLayout othersLayout,departmentLayout,emailLayout,mobileNoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmanagement__details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Task details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        final TaskDataModel taskDataModel = intent.getParcelableExtra("particular_task");

        tvName = findViewById(R.id.tvName);
        tvDesignation = findViewById(R.id.tvDesignation);
        tvTaskAssigned = findViewById(R.id.tvTaskAssigned);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);
        tvDepartment = findViewById(R.id.tvDepartment);
        tvAssigneeType = findViewById(R.id.tvAssigneeType);
        tvAssignedDate = findViewById(R.id.tvAssignedDate);
        tvCompletionDate = findViewById(R.id.tvCompletionDate);
        tvEmailId = findViewById(R.id.tvEmailId);
        tvMobileNo = findViewById(R.id.tvMobileNo);
        tvOthers = findViewById(R.id.tvOthers);
        othersLayout = findViewById(R.id.othersLayout);
        departmentLayout = findViewById(R.id.departmentLayout);
        emailLayout = findViewById(R.id.emailLayout);
        mobileNoLayout = findViewById(R.id.mobileNoLayout);

        tvName.setText(taskDataModel.getName());
        tvDesignation.setText(taskDataModel.getDesignation());
        tvTaskAssigned.setText(taskDataModel.getTask_info());
        tvAssignedDate.setText(taskDataModel.getAssigned_date());
        tvCompletionDate.setText(taskDataModel.getCompletion_date());
        tvTaskStatus.setText(taskDataModel.getStatus_name());

        if(taskDataModel.getOther_info() != null && (!taskDataModel.getOther_info().equalsIgnoreCase("null") && !taskDataModel.getOther_info().equalsIgnoreCase("") )){
            othersLayout.setVisibility(View.VISIBLE);
            tvOthers.setText(taskDataModel.getOther_info());
        }

        if(taskDataModel.getEmail() != null && (!taskDataModel.getEmail().equalsIgnoreCase("null") && !taskDataModel.getEmail().equalsIgnoreCase("") )){
            emailLayout.setVisibility(View.VISIBLE);
            tvEmailId.setText(taskDataModel.getEmail());
        }

        if(taskDataModel.getMobile_no() != null && (!taskDataModel.getMobile_no().equalsIgnoreCase("null") && !taskDataModel.getMobile_no().equalsIgnoreCase("") )){
            mobileNoLayout.setVisibility(View.VISIBLE);
            tvMobileNo.setText(taskDataModel.getMobile_no());
        }

        if(taskDataModel.getDepartment() != null && (!taskDataModel.getDepartment().equalsIgnoreCase("null") && !taskDataModel.getDepartment().equalsIgnoreCase("") )){
            departmentLayout.setVisibility(View.VISIBLE);
            tvDepartment.setText(taskDataModel.getDepartment());
        }

        tvEmailId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
                emailIntent.setType("message/rfc822");
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                        new String[]{taskDataModel.getEmail()});
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
                ClipData clip = ClipData.newPlainText("Mobile_No", taskDataModel.getMobile_no());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(TaskDetails.this,"Mobile number copied to clipboard.",Toast.LENGTH_LONG).show();
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
