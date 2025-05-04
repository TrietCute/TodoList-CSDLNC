package com.example.todoapp_csdlnc.model;

import java.io.Serializable;

public class Task implements Serializable {
    private String name;
    private String description;
    private String deadline;
    private String relatedPersons;

    public Task() {}

    public Task(String name, String description, String deadline, String relatedPersons) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.relatedPersons = relatedPersons;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getRelatedPersons() { return relatedPersons; }
    public void setRelatedPersons(String relatedPersons) { this.relatedPersons = relatedPersons; }
}