package com.example.rakesh.appointment;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sai Krishna on 4/10/2018.
 */

public class TaskDataModel implements Parcelable{
    String assignee_master_key,assignee_emp_id,assignee_type,assigned_date,completion_date,task_info,domain,other_info;
    int status,priority;
    String name,designation,email,mobile_no,department,status_name,address;

    public String getAssignee_master_key() {
        return assignee_master_key;
    }

    public void setAssignee_master_key(String assignee_master_key) {
        this.assignee_master_key = assignee_master_key;
    }

    public String getAssignee_emp_id() {
        return assignee_emp_id;
    }

    public void setAssignee_emp_id(String assignee_emp_id) {
        this.assignee_emp_id = assignee_emp_id;
    }

    public String getAssignee_type() {
        return assignee_type;
    }

    public void setAssignee_type(String assignee_type) {
        this.assignee_type = assignee_type;
    }

    public String getAssigned_date() {
        return assigned_date;
    }

    public void setAssigned_date(String assigned_date) {
        this.assigned_date = assigned_date;
    }

    public String getCompletion_date() {
        return completion_date;
    }

    public void setCompletion_date(String completion_date) {
        this.completion_date = completion_date;
    }

    public String getTask_info() {
        return task_info;
    }

    public void setTask_info(String task_info) {
        this.task_info = task_info;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getOther_info() {
        return other_info;
    }

    public void setOther_info(String other_info) {
        this.other_info = other_info;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus_name() {
        return status_name;
    }

    public void setStatus_name(String status_name) {
        this.status_name = status_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.assignee_master_key);
        parcel.writeString(this.assignee_emp_id);
        parcel.writeString(this.assignee_type);
        parcel.writeString(this.assigned_date);
        parcel.writeString(this.completion_date);
        parcel.writeString(this.task_info);
        parcel.writeString(this.domain);
        parcel.writeString(this.other_info);
        parcel.writeInt(this.status);
        parcel.writeInt(this.priority);
        parcel.writeString(this.name);
        parcel.writeString(this.designation);
        parcel.writeString(this.email);
        parcel.writeString(this.mobile_no);
        parcel.writeString(this.department);
        parcel.writeString(this.status_name);
        parcel.writeString(this.address);
    }

    public static final Parcelable.Creator CREATOR = new Creator<TaskDataModel>() {

        @Override
        public TaskDataModel createFromParcel(Parcel source) {

            TaskDataModel taskDataModel = new TaskDataModel();
            taskDataModel.setAssignee_master_key(source.readString());
            taskDataModel.setAssignee_emp_id(source.readString());
            taskDataModel.setAssignee_type(source.readString());
            taskDataModel.setAssigned_date(source.readString());
            taskDataModel.setCompletion_date(source.readString());
            taskDataModel.setTask_info(source.readString());
            taskDataModel.setDomain(source.readString());
            taskDataModel.setOther_info(source.readString());
            taskDataModel.setStatus(source.readInt());
            taskDataModel.setPriority(source.readInt());
            taskDataModel.setName(source.readString());
            taskDataModel.setDesignation(source.readString());
            taskDataModel.setEmail(source.readString());
            taskDataModel.setMobile_no(source.readString());
            taskDataModel.setDepartment(source.readString());
            taskDataModel.setStatus_name(source.readString());
            taskDataModel.setAddress(source.readString());
            return taskDataModel;
        }

        @Override
        public TaskDataModel[] newArray(int size) {
            return new TaskDataModel[size];
        }
    };


}
