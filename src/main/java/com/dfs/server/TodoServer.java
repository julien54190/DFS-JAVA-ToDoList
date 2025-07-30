package com.dfs.server;

import com.dfs.data.DatabaseAccess;
import com.dfs.models.TaskModel;
import com.dfs.models.UserModel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class TodoServer {
    private final int port;
    private final DatabaseAccess database;
    
    public TodoServer(int port) {
        this.port = port;
        this.database = DatabaseAccess.getInstance();
    }
    
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur TODO démarré sur le port " + port);
            System.out.println("Accédez à http://localhost:" + port);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            
            HttpRequest request = new HttpRequest(reader);
            HttpResponse response = processRequest(request);
            response.send(output);
            
        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
    
    private HttpResponse processRequest(HttpRequest request) {
        String method = request.getMethod();
        String path = request.getPath();
        
        if ("GET".equals(method)) {
            return handleGetRequest(path, request);
        } else if ("POST".equals(method)) {
            return handlePostRequest(path, request);
        }
        
        return HttpResponse.notFound();
    }
    
    private HttpResponse handleGetRequest(String path, HttpRequest request) {
        return switch (path) {
            case "/" -> getHomePage();
            case "/tasks" -> getTasksPage();
            case "/api/tasks" -> getTasksJson();
            case "/api/users" -> getUsersJson();
            default -> HttpResponse.notFound();
        };
    }
    
    private HttpResponse handlePostRequest(String path, HttpRequest request) {
        return switch (path) {
            case "/api/tasks" -> createTask(request);
            default -> HttpResponse.notFound();
        };
    }
    
    private HttpResponse getHomePage() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>TODO List</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .container { max-width: 800px; margin: 0 auto; }
                    .task { border: 1px solid #ddd; padding: 10px; margin: 10px 0; border-radius: 5px; }
                    .done { background-color: #e8f5e8; }
                    .overdue { border-color: #ff6b6b; }
                    .today { border-color: #ffd93d; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>📝 TODO List</h1>
                    <p>Bienvenue dans votre gestionnaire de tâches !</p>
                    <p><a href="/tasks">Voir toutes les tâches</a></p>
                    <p><a href="/api/tasks">API - Tâches (JSON)</a></p>
                    <p><a href="/api/users">API - Utilisateurs (JSON)</a></p>
                </div>
            </body>
            </html>
            """;
        return HttpResponse.ok(html);
    }
    
    private HttpResponse getTasksPage() {
        List<TaskModel> tasks = database.getAllTasks();
        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Tâches - TODO List</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .container { max-width: 800px; margin: 0 auto; }
                    .task { border: 1px solid #ddd; padding: 15px; margin: 10px 0; border-radius: 5px; }
                    .done { background-color: #e8f5e8; }
                    .overdue { border-color: #ff6b6b; }
                    .today { border-color: #ffd93d; }
                    .status { font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>📋 Liste des tâches</h1>
                    <p><a href="/">← Retour à l'accueil</a></p>
            """);
        
        for (TaskModel task : tasks) {
            String status = task.isDone() ? "✅ Terminée" : "☐ À faire";
            String cssClass = task.isDone() ? "task done" : "task";
            
            if (task instanceof com.dfs.models.DatedTaskModel datedTask) {
                if (datedTask.isOverdue()) {
                    cssClass += " overdue";
                } else if (datedTask.isDueToday()) {
                    cssClass += " today";
                }
            }
            
            html.append("<div class=\"").append(cssClass).append("\">");
            html.append("<div class=\"status\">").append(status).append("</div>");
            html.append("<h3>").append(task.getTitle()).append("</h3>");
            html.append("<p>").append(task.getDescription()).append("</p>");
            html.append("<small>Par : ").append(task.getCreatedBy().getFirstName()).append("</small>");
            
            if (task instanceof com.dfs.models.DatedTaskModel datedTask) {
                html.append("<br><small>Échéance : ").append(datedTask.getDueDate()).append("</small>");
            }
            
            html.append("</div>");
        }
        
        html.append("</div></body></html>");
        return HttpResponse.ok(html.toString());
    }
    
    private HttpResponse getTasksJson() {
        List<TaskModel> tasks = database.getAllTasks();
        StringBuilder json = new StringBuilder();
        json.append("{\"tasks\":[");
        
        boolean first = true;
        for (TaskModel task : tasks) {
            if (!first) json.append(",");
            json.append("{");
            json.append("\"id\":\"").append(task.getId()).append("\",");
            json.append("\"title\":\"").append(task.getTitle()).append("\",");
            json.append("\"description\":\"").append(task.getDescription()).append("\",");
            json.append("\"done\":").append(task.isDone()).append(",");
            json.append("\"createdBy\":\"").append(task.getCreatedBy().getFirstName()).append("\"");
            
            if (task instanceof com.dfs.models.DatedTaskModel datedTask) {
                json.append(",\"dueDate\":\"").append(datedTask.getDueDate()).append("\"");
            }
            
            json.append("}");
            first = false;
        }
        
        json.append("]}");
        return HttpResponse.json(json.toString());
    }
    
    private HttpResponse getUsersJson() {
        List<UserModel> users = database.getAllUsers();
        StringBuilder json = new StringBuilder();
        json.append("{\"users\":[");
        
        boolean first = true;
        for (UserModel user : users) {
            if (!first) json.append(",");
            json.append("{");
            json.append("\"id\":\"").append(user.getId()).append("\",");
            json.append("\"firstName\":\"").append(user.getFirstName()).append("\"");
            json.append("}");
            first = false;
        }
        
        json.append("]}");
        return HttpResponse.json(json.toString());
    }
    
    private HttpResponse createTask(HttpRequest request) {
        try {
            String body = request.getBody();
            String title = extractParameter(body, "title");
            String description = extractParameter(body, "description");
            String userId = extractParameter(body, "userId");
            
            if (title == null || description == null || userId == null) {
                return HttpResponse.error("Paramètres manquants");
            }
            
            UserModel user = database.findUserById(java.util.UUID.fromString(userId));
            TaskModel task = new com.dfs.models.TaskBuilder()
                    .title(title)
                    .description(description)
                    .createdBy(user)
                    .build();
            
            database.addTask(task);
            return HttpResponse.json("{\"success\":true,\"message\":\"Tâche créée\"}");
            
        } catch (Exception e) {
            return HttpResponse.error("Erreur lors de la création : " + e.getMessage());
        }
    }
    
    private String extractParameter(String body, String paramName) {
        String[] params = body.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return java.net.URLDecoder.decode(keyValue[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }
} 