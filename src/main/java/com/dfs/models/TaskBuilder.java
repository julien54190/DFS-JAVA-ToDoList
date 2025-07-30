package com.dfs.models;

import java.time.LocalDate;

public class TaskBuilder {
    private String title;
    private String description;
    private UserModel createdBy;
    private LocalDate dueDate;
    private boolean done = false;
    
    public TaskBuilder title(String title) {
        this.title = title;
        return this;
    }
    
    public TaskBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public TaskBuilder createdBy(UserModel createdBy) {
        this.createdBy = createdBy;
        return this;
    }
    
    public TaskBuilder dueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }
    
    public TaskBuilder done(boolean done) {
        this.done = done;
        return this;
    }
    
    public TaskModel build() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Le créateur est obligatoire");
        }
        
        if (dueDate != null) {
            DatedTaskModel datedTask = new DatedTaskModel(title, description, createdBy, dueDate);
            if (done) {
                datedTask.setDone(true);
            }
            return datedTask;
        } else {
            TaskModel task = new TaskModel(title, description, createdBy);
            if (done) {
                task.setDone(true);
            }
            return task;
        }
    }
    
    public DatedTaskModel buildDatedTask() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Le créateur est obligatoire");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("La date d'échéance est obligatoire pour une tâche datée");
        }
        
        DatedTaskModel datedTask = new DatedTaskModel(title, description, createdBy, dueDate);
        if (done) {
            datedTask.setDone(true);
        }
        return datedTask;
    }
} 