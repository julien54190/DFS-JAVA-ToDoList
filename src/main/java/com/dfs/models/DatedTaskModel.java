package com.dfs.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatedTaskModel extends TaskModel {
    private LocalDate dueDate;
    
    public DatedTaskModel(String title, String description, UserModel createdBy, LocalDate dueDate) {
        super(title, description, createdBy);
        this.dueDate = dueDate;
    }
    
    public LocalDate getDueDate() { return dueDate; }
    
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public boolean isOverdue() { return !isDone() && LocalDate.now().isAfter(dueDate); }
    
    public boolean isDueToday() { return LocalDate.now().equals(dueDate); }
    
    @Override
    public String toString() {
        String baseString = super.toString();
        String dateStr = dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String urgency = isOverdue() ? " ⚠️ EN RETARD" : isDueToday() ? " 🔥 AUJOURD'HUI" : "";
        return baseString + " (Échéance: " + dateStr + ")" + urgency;
    }
} 