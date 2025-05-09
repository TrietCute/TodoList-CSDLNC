package com.example.todoapp_csdlnc.model;

import com.google.firebase.Timestamp;
import com.google.type.DateTime;

public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String avatarUrl;
    private String bio;
    private Timestamp createdAt;
    public User() {}
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getBio() {
        return bio;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
