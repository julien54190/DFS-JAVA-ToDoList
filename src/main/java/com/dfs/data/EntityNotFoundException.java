package com.dfs.data;

public class EntityNotFoundException extends Exception {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String entityType, String identifier) {
        super(entityType + " avec l'identifiant '" + identifier + "' n'a pas été trouvé.");
    }
    
    public EntityNotFoundException(String entityType, Object identifier) {
        this(entityType, identifier.toString());
    }
} 