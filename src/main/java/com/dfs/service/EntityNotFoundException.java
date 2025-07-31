package com.dfs.service;

public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String entityType, String id) {
        super(entityType + " avec l'ID '" + id + "' non trouvé.");
    }
} 