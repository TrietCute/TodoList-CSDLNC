package com.example.todoapp_csdlnc.model;

import com.google.type.DateTime;
import java.util.ArrayList;

public class Task {
    private String id;
    private String name;
    private String description;
    private String deadline;
    //private String priority;
    private boolean isCompleted;
    ArrayList<RelatedPerson> relatedPersons;
    private DateTime createdAt;
    public Task() {}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDeadline() {
        return deadline;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public ArrayList<RelatedPerson> getRelatedPersons() {
        return relatedPersons;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }
}

