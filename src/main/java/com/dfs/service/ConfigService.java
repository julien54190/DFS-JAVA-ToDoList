package com.dfs.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigService {
    private static ConfigService instance;
    private final Properties properties;
    
    private ConfigService() {
        this.properties = new Properties();
        loadEnvironmentVariables();
    }
    
    public static synchronized ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }
    
    private void loadEnvironmentVariables() {
        try {
            // Charger depuis le fichier .env
            FileInputStream fis = new FileInputStream(".env");
            properties.load(fis);
            fis.close();
        } catch (IOException e) {
            // Utiliser les valeurs par défaut si le fichier .env n'existe pas
            setDefaultValues();
        }
    }
    
    private void setDefaultValues() {
        properties.setProperty("MONGODB_URL", "mongodb://localhost:27017");
        properties.setProperty("MONGODB_DATABASE", "todolist");
        properties.setProperty("MONGODB_USERS_COLLECTION", "users");
        properties.setProperty("MONGODB_TASKS_COLLECTION", "tasks");
        properties.setProperty("SERVER_PORT", "8080");
        properties.setProperty("SERVER_HOST", "localhost");
        properties.setProperty("APP_NAME", "TODO List");
        properties.setProperty("APP_VERSION", "1.0.0");
    }
    
    public String getMongoDbUrl() {
        return properties.getProperty("MONGODB_URL", "mongodb://localhost:27017");
    }
    
    public String getMongoDbDatabase() {
        return properties.getProperty("MONGODB_DATABASE", "todolist");
    }
    
    public String getMongoDbUsersCollection() {
        return properties.getProperty("MONGODB_USERS_COLLECTION", "users");
    }
    
    public String getMongoDbTasksCollection() {
        return properties.getProperty("MONGODB_TASKS_COLLECTION", "tasks");
    }
    
    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("SERVER_PORT", "8080"));
    }
    
    public String getServerHost() {
        return properties.getProperty("SERVER_HOST", "localhost");
    }
    
    public String getAppName() {
        return properties.getProperty("APP_NAME", "TODO List");
    }
    
    public String getAppVersion() {
        return properties.getProperty("APP_VERSION", "1.0.0");
    }
} 