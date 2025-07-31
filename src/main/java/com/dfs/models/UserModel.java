package com.dfs.models;

import java.util.UUID;

public class UserModel {
    private final UUID id;
    private String firstName;
    
    public UserModel(String firstName) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
    }
    
    public UUID getId() { return id; }
    
    public String getFirstName() { return firstName; }
    
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    @Override
    public String toString() { return "UserModel{id=" + id + ", firstName='" + firstName + "'}"; }
} 