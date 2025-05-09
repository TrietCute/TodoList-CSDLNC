package com.example.todoapp_csdlnc.model;

public class RelatedPerson {
    private String id;
    private String name;
    private String phoneNumber;

    public RelatedPerson(){}
    public RelatedPerson(String id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
