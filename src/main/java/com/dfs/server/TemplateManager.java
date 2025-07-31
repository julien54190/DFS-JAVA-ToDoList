package com.dfs.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TemplateManager {
    
    private static final String TEMPLATES_DIR = "src/main/resources/templates/";
    
    public static String loadTemplate(String templateName) {
        try {
            Path templatePath = Paths.get(TEMPLATES_DIR + templateName + ".html");
            return Files.readString(templatePath);
        } catch (IOException e) {
            return "<h1>Erreur: Template non trouvé</h1>";
        }
    }
    
    public static String renderTemplate(String templateName, String content) {
        String template = loadTemplate(templateName);
        return template.replace("{{" + templateName.toUpperCase() + "_CONTENT}}", content);
    }
    
    public static String renderTasksTemplate(List<String> tasksHtml) {
        String template = loadTemplate("tasks");
        String content = String.join("\n", tasksHtml);
        return template.replace("{{TASKS_CONTENT}}", content);
    }
    
    public static String renderUsersTemplate(List<String> usersHtml) {
        String template = loadTemplate("users");
        String content = String.join("\n", usersHtml);
        return template.replace("{{USERS_CONTENT}}", content);
    }
    
    public static String renderAddTaskTemplate(String usersOptions) {
        String template = loadTemplate("add-task");
        return template.replace("{{USERS_OPTIONS}}", usersOptions);
    }
} 