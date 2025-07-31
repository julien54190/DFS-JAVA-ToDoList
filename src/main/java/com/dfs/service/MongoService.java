package com.dfs.service;

import com.dfs.models.UserModel;
import com.dfs.models.TaskModel;
import com.dfs.models.DatedTaskModel;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MongoService {
    private static MongoService instance;
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> usersCollection;
    private final MongoCollection<Document> tasksCollection;
    
    private MongoService() {
        this.mongoClient = MongoClients.create("mongodb://localhost:27017");
        this.database = mongoClient.getDatabase("todolist");
        this.usersCollection = database.getCollection("users");
        this.tasksCollection = database.getCollection("tasks");
        
        // Nettoyer la base de données au démarrage
        clearAllData();
    }
    
    public static synchronized MongoService getInstance() {
        if (instance == null) {
            instance = new MongoService();
        }
        return instance;
    }
    
    private void clearAllData() {
        usersCollection.deleteMany(new Document());
        tasksCollection.deleteMany(new Document());
        System.out.println("✅ Base de données nettoyée au démarrage");
    }
    
    public List<UserModel> getAllUsers() {
        List<UserModel> users = new ArrayList<>();
        usersCollection.find().forEach(doc -> {
            UserModel user = new UserModel(doc.getString("firstName"));
            users.add(user);
        });
        return users;
    }
    
    public UserModel findUserById(String id) throws EntityNotFoundException {
        Document doc = usersCollection.find(Filters.eq("_id", id)).first();
        if (doc == null) {
            throw new EntityNotFoundException("Utilisateur", id);
        }
        return new UserModel(doc.getString("firstName"));
    }
    
    public UserModel findUserByFirstName(String firstName) throws EntityNotFoundException {
        Document doc = usersCollection.find(Filters.eq("firstName", firstName)).first();
        if (doc == null) {
            throw new EntityNotFoundException("Utilisateur", firstName);
        }
        return new UserModel(doc.getString("firstName"));
    }
    
    public void addUser(UserModel user) {
        Document doc = new Document()
            .append("_id", user.getId().toString())
            .append("firstName", user.getFirstName());
        usersCollection.insertOne(doc);
    }
    
    public void deleteUser(String id) throws EntityNotFoundException {
        Document result = usersCollection.findOneAndDelete(Filters.eq("_id", id));
        if (result == null) {
            throw new EntityNotFoundException("Utilisateur", id);
        }
    }
    
    public void updateUser(UserModel user) throws EntityNotFoundException {
        Document result = usersCollection.findOneAndUpdate(
            Filters.eq("_id", user.getId().toString()),
            Updates.set("firstName", user.getFirstName())
        );
        if (result == null) {
            throw new EntityNotFoundException("Utilisateur", user.getId().toString());
        }
    }
    
    public List<TaskModel> getAllTasks() {
        List<TaskModel> tasks = new ArrayList<>();
        tasksCollection.find().forEach(doc -> {
            TaskModel task = documentToTask(doc);
            if (task != null) {
                tasks.add(task);
            }
        });
        return tasks;
    }
    
    public TaskModel findTaskById(String id) throws EntityNotFoundException {
        Document doc = tasksCollection.find(Filters.eq("_id", id)).first();
        if (doc == null) {
            throw new EntityNotFoundException("Tâche", id);
        }
        TaskModel task = documentToTask(doc);
        if (task == null) {
            throw new EntityNotFoundException("Tâche", id);
        }
        return task;
    }
    
    public List<TaskModel> findTasksByUser(String userId) throws EntityNotFoundException {
        List<TaskModel> tasks = new ArrayList<>();
        tasksCollection.find(Filters.eq("createdBy", userId)).forEach(doc -> {
            TaskModel task = documentToTask(doc);
            if (task != null) {
                tasks.add(task);
            }
        });
        return tasks;
    }
    
    public List<TaskModel> findTasksByUserFirstName(String firstName) throws EntityNotFoundException {
        UserModel user = findUserByFirstName(firstName);
        return findTasksByUser(user.getId().toString());
    }
    
    public List<DatedTaskModel> findOverdueTasks() {
        List<DatedTaskModel> overdueTasks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        tasksCollection.find(Filters.eq("type", "datedTask")).forEach(doc -> {
            String dueDateStr = doc.getString("dueDate");
            if (dueDateStr != null) {
                LocalDate dueDate = LocalDate.parse(dueDateStr);
                if (dueDate.isBefore(today)) {
                    TaskModel task = documentToTask(doc);
                    if (task instanceof DatedTaskModel datedTask) {
                        overdueTasks.add(datedTask);
                    }
                }
            }
        });
        return overdueTasks;
    }
    
    public List<DatedTaskModel> findTasksDueToday() {
        List<DatedTaskModel> tasksDueToday = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        tasksCollection.find(Filters.eq("type", "datedTask")).forEach(doc -> {
            String dueDateStr = doc.getString("dueDate");
            if (dueDateStr != null) {
                LocalDate dueDate = LocalDate.parse(dueDateStr);
                if (dueDate.equals(today)) {
                    TaskModel task = documentToTask(doc);
                    if (task instanceof DatedTaskModel datedTask) {
                        tasksDueToday.add(datedTask);
                    }
                }
            }
        });
        return tasksDueToday;
    }
    
    public void addTask(TaskModel task) {
        Document doc = taskToDocument(task);
        tasksCollection.insertOne(doc);
    }
    
    public void deleteTask(String id) throws EntityNotFoundException {
        Document result = tasksCollection.findOneAndDelete(Filters.eq("_id", id));
        if (result == null) {
            throw new EntityNotFoundException("Tâche", id);
        }
    }
    
    public void updateTask(TaskModel task) throws EntityNotFoundException {
        Document doc = taskToDocument(task);
        Document result = tasksCollection.findOneAndReplace(Filters.eq("_id", task.getId().toString()), doc);
        if (result == null) {
            throw new EntityNotFoundException("Tâche", task.getId().toString());
        }
    }
    
    public void deleteAllTasks() {
        tasksCollection.deleteMany(new Document());
    }
    
    public boolean userExists(String id) {
        return usersCollection.countDocuments(Filters.eq("_id", id)) > 0;
    }
    
    public boolean taskExists(String id) {
        return tasksCollection.countDocuments(Filters.eq("_id", id)) > 0;
    }
    
    public int getUserCount() {
        return (int) usersCollection.countDocuments();
    }
    
    public int getTaskCount() {
        return (int) tasksCollection.countDocuments();
    }
    
    private TaskModel documentToTask(Document doc) {
        try {
            String type = doc.getString("type");
            String title = doc.getString("title");
            String description = doc.getString("description");
            boolean done = doc.getBoolean("done", false);
            String createdById = doc.getString("createdBy");
            
            UserModel createdBy = findUserById(createdById);
            
            if ("datedTask".equals(type)) {
                String dueDateStr = doc.getString("dueDate");
                if (dueDateStr != null) {
                    LocalDate dueDate = LocalDate.parse(dueDateStr);
                    DatedTaskModel datedTask = new DatedTaskModel(title, description, createdBy, dueDate);
                    datedTask.setDone(done);
                    return datedTask;
                }
            }
            
            TaskModel task = new TaskModel(title, description, createdBy);
            task.setDone(done);
            return task;
        } catch (Exception e) {
            return null;
        }
    }
    
    private Document taskToDocument(TaskModel task) {
        Document doc = new Document()
            .append("_id", task.getId().toString())
            .append("title", task.getTitle())
            .append("description", task.getDescription())
            .append("done", task.isDone())
            .append("createdBy", task.getCreatedBy().getId().toString());
        
        if (task instanceof DatedTaskModel datedTask) {
            doc.append("type", "datedTask")
               .append("dueDate", datedTask.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else {
            doc.append("type", "task");
        }
        
        return doc;
    }
    
    public MongoCollection<Document> getUsersCollection() {
        return usersCollection;
    }
    
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
} 