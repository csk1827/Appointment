package com.example.rakesh.appointment;

import android.os.Parcel;
import android.os.Parcelable;

public class Data implements Parcelable {
    String name,designation,department,email,mobile_no;
    String appointee_type,appoint_date,appoint_time,venue,purpose,other_info,statusName;
    int appointee_master_key,appointment_status;

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    String getAppoint_time() {
        return appoint_time;
    }

    void setAppoint_time(String appoint_time) {
        this.appoint_time = appoint_time;
    }

    Data(){}

    public Data(String name, String appoint_date, String venue) {
        this.name = name;
        this.appoint_date = appoint_date;
        this.venue = venue;
    }

    public int getAppointment_status() {
        return appointment_status;
    }

    public void setAppointment_status(int appointment_status) {
        this.appointment_status = appointment_status;
    }

    public int getAppointee_master_key() {
        return appointee_master_key;
    }

    void setAppointee_master_key(int appointee_master_key) {
        this.appointee_master_key = appointee_master_key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getAppointee_type() {
        return appointee_type;
    }

    public void setAppointee_type(String appointee_type) {
        this.appointee_type = appointee_type;
    }

    public String getAppoint_date() {
        return appoint_date;
    }

    public void setAppoint_date(String appoint_date) {
        this.appoint_date = appoint_date;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getOther_info() {
        return other_info;
    }

    public void setOther_info(String other_info) {
        this.other_info = other_info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(this.name);
        parcel.writeString(this.designation);
        parcel.writeString(this.department);
        parcel.writeString(this.email);
        parcel.writeString(this.mobile_no);
        parcel.writeString(this.appointee_type);
        parcel.writeString(this.appoint_date);
        parcel.writeString(this.appoint_time);
        parcel.writeString(this.venue);
        parcel.writeString(this.purpose);
        parcel.writeString(this.other_info);
        parcel.writeInt(this.appointee_master_key);
        parcel.writeInt(this.appointment_status);
        parcel.writeString(this.statusName);
    }

    public static final Parcelable.Creator CREATOR = new Creator<Data>() {

        @Override
        public Data createFromParcel(Parcel source) {
            Data data = new Data();
            data.setName(source.readString());
            data.setDesignation(source.readString());
            data.setDepartment(source.readString());
            data.setEmail(source.readString());
            data.setMobile_no(source.readString());
            data.setAppointee_type(source.readString());
            data.setAppoint_date(source.readString());
            data.setAppoint_time(source.readString());
            data.setVenue(source.readString());
            data.setPurpose(source.readString());
            data.setOther_info(source.readString());
            data.setAppointee_master_key(source.readInt());
            data.setAppointment_status(source.readInt());
            data.setStatusName(source.readString());
            return data;
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

}