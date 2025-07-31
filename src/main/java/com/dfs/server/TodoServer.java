package com.dfs.server;

import com.dfs.service.MongoService;
import com.dfs.service.EntityNotFoundException;
import com.dfs.models.UserModel;
import com.dfs.models.TaskModel;
import com.dfs.models.DatedTaskModel;
import com.dfs.models.TaskBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

public class TodoServer {
    private final int port;
    private final MongoService database;
    
    public TodoServer(int port) {
        this.port = port;
        this.database = MongoService.getInstance();
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
            case "/users" -> getUsersPage();
            case "/add-task" -> getAddTaskPage();
            case "/add-user" -> getAddUserPage();
            case "/api/tasks" -> getTasksJson();
            case "/api/users" -> getUsersJson();
            case "/static/style.css" -> getCssFile();
            default -> HttpResponse.notFound();
        };
    }
    
    private HttpResponse handlePostRequest(String path, HttpRequest request) {
        return switch (path) {
            case "/api/tasks" -> createTask(request);
            case "/api/users" -> createUser(request);
            case "/add-task" -> createTaskFromForm(request);
            case "/add-user" -> createUserFromForm(request);
            default -> HttpResponse.notFound();
        };
    }
    
    private HttpResponse getCssFile() {
        try {
            String css = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("src/main/resources/static/style.css")));
            return HttpResponse.css(css);
        } catch (Exception e) {
            return HttpResponse.notFound();
        }
    }
    
    private HttpResponse getHomePage() {
        String html = TemplateManager.loadTemplate("home");
        return HttpResponse.ok(html);
    }
    
    private HttpResponse getTasksPage() {
        List<TaskModel> tasks = database.getAllTasks();
        List<String> tasksHtml = tasks.stream().map(task -> {
            String status = task.isDone() ? "✅ Terminée" : "☐ À faire";
            String cssClass = task.isDone() ? "task done" : "task";
            
            if (task instanceof DatedTaskModel datedTask) {
                if (datedTask.isOverdue()) {
                    cssClass += " overdue";
                } else if (datedTask.isDueToday()) {
                    cssClass += " today";
                }
            }
            
            StringBuilder html = new StringBuilder();
            html.append("<div class=\"").append(cssClass).append("\">");
            html.append("<div class=\"status\">").append(status).append("</div>");
            html.append("<h3>").append(task.getTitle()).append("</h3>");
            html.append("<p>").append(task.getDescription()).append("</p>");
            html.append("<small>Par : ").append(task.getCreatedBy().getFirstName()).append("</small>");
            
            if (task instanceof DatedTaskModel datedTask) {
                html.append("<br><small>Échéance : ").append(datedTask.getDueDate()).append("</small>");
            }
            
            html.append("</div>");
            return html.toString();
        }).toList();
        
        String html = TemplateManager.renderTasksTemplate(tasksHtml);
        return HttpResponse.ok(html);
    }
    
    private HttpResponse getUsersPage() {
        List<UserModel> users = database.getAllUsers();
        List<String> usersHtml = users.stream().map(user -> {
            return "<div class=\"user\"><h3>" + user.getFirstName() + "</h3><p>ID: " + user.getId() + "</p></div>";
        }).toList();
        
        String html = TemplateManager.renderUsersTemplate(usersHtml);
        return HttpResponse.ok(html);
    }
    
    private HttpResponse getAddTaskPage() {
        List<UserModel> users = database.getAllUsers();
        StringBuilder usersOptions = new StringBuilder();
        
        for (UserModel user : users) {
            usersOptions.append("<option value=\"").append(user.getId().toString()).append("\">")
                .append(user.getFirstName()).append("</option>");
        }
        
        String html = TemplateManager.renderAddTaskTemplate(usersOptions.toString());
        return HttpResponse.ok(html);
    }
    
    private HttpResponse getAddUserPage() {
        String html = TemplateManager.loadTemplate("add-user");
        return HttpResponse.ok(html);
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
            
            if (task instanceof DatedTaskModel datedTask) {
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
            
            UserModel user = database.findUserById(userId);
            TaskModel task = new TaskBuilder()
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
    
    private HttpResponse createUser(HttpRequest request) {
        try {
            String body = request.getBody();
            String firstName = extractParameter(body, "firstName");
            
            if (firstName == null) {
                return HttpResponse.error("Paramètre firstName manquant");
            }
            
            UserModel user = new UserModel(firstName);
            database.addUser(user);
            return HttpResponse.json("{\"success\":true,\"message\":\"Utilisateur créé\"}");
            
        } catch (Exception e) {
            return HttpResponse.error("Erreur lors de la création : " + e.getMessage());
        }
    }
    
    private HttpResponse createTaskFromForm(HttpRequest request) {
        try {
            String body = request.getBody();
            String title = extractParameter(body, "title");
            String description = extractParameter(body, "description");
            String createdBy = extractParameter(body, "createdBy");
            String dueDate = extractParameter(body, "dueDate");
            
            if (title == null || description == null || createdBy == null) {
                return HttpResponse.error("Paramètres manquants");
            }
            
            UserModel user = database.findUserById(createdBy);
            TaskModel task;
            
            if (dueDate != null && !dueDate.isEmpty()) {
                LocalDate localDueDate = LocalDate.parse(dueDate);
                task = new TaskBuilder()
                        .title(title)
                        .description(description)
                        .createdBy(user)
                        .dueDate(localDueDate)
                        .buildDatedTask();
            } else {
                task = new TaskBuilder()
                        .title(title)
                        .description(description)
                        .createdBy(user)
                        .build();
            }
            
            database.addTask(task);
            return HttpResponse.ok("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Tâche créée - TODO List</title>
                    <meta charset="UTF-8">
                    <link rel="stylesheet" href="/static/style.css">
                </head>
                <body>
                    <div class="container">
                        <h1>✅ Tâche créée avec succès !</h1>
                        <div class="success">
                            <p>La tâche <strong>%s</strong> a été ajoutée.</p>
                        </div>
                        <p><a href="/tasks">Voir toutes les tâches</a></p>
                        <p><a href="/add-task">Ajouter une autre tâche</a></p>
                        <p><a href="/">Retour à l'accueil</a></p>
                    </div>
                </body>
                </html>
                """.formatted(title));
            
        } catch (Exception e) {
            return HttpResponse.error("Erreur lors de la création : " + e.getMessage());
        }
    }
    
    private HttpResponse createUserFromForm(HttpRequest request) {
        try {
            String body = request.getBody();
            String firstName = extractParameter(body, "firstName");
            
            if (firstName == null) {
                return HttpResponse.error("Paramètre firstName manquant");
            }
            
            UserModel user = new UserModel(firstName);
            database.addUser(user);
            return HttpResponse.ok("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Utilisateur créé - TODO List</title>
                    <meta charset="UTF-8">
                    <link rel="stylesheet" href="/static/style.css">
                </head>
                <body>
                    <div class="container">
                        <h1>✅ Utilisateur créé avec succès !</h1>
                        <div class="success">
                            <p>L'utilisateur <strong>%s</strong> a été ajouté.</p>
                        </div>
                        <p><a href="/users">Voir tous les utilisateurs</a></p>
                        <p><a href="/add-user">Ajouter un autre utilisateur</a></p>
                        <p><a href="/">Retour à l'accueil</a></p>
                    </div>
                </body>
                </html>
                """.formatted(firstName));
            
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