package com.example.todoapp_csdlnc.model;

import java.io.Serializable;
import java.util.List;

public class Task implements Serializable {
    private String id;
    private String name;
    private String description;
    private String deadline;
    private List<RelatedPerson> relatedPersons;
    private boolean isCompleted;
    private String userId;

    public Task() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public List<RelatedPerson> getRelatedPersons() {
        return relatedPersons;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setRelatedPersons(List<RelatedPerson> relatedPersons) {
        this.relatedPersons = relatedPersons;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}