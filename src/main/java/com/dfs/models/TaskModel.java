package com.dfs.models;

import java.util.UUID;

public class TaskModel {
    private final UUID id;
    private String title;
    private String description;
    private boolean done;
    private final UserModel createdBy;
    
    public TaskModel(String title, String description, UserModel createdBy) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.done = false;
        this.createdBy = createdBy;
    }
    
    public UUID getId() { return id; }
    
    public String getTitle() { return title; }
    
    public String getDescription() { return description; }
    
    public boolean isDone() { return done; }
    
    public UserModel getCreatedBy() { return createdBy; }
    
    public void setTitle(String title) { this.title = title; }
    
    public void setDescription(String description) { this.description = description; }
    
    public void setDone(boolean done) { this.done = done; }
    
    public void toggleDone() { this.done = !this.done; }
    
    @Override
    public String toString() {
        String status = done ? "✓" : "☐";
        return String.format("[%s] %s - %s (par %s)", status, title, description, createdBy.getFirstName());
    }
} 