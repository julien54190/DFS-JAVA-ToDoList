package com.dfs.data;

import com.dfs.models.UserModel;
import com.dfs.models.TaskModel;
import com.dfs.models.DatedTaskModel;
import com.dfs.models.TaskBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DatabaseAccess {
    private static DatabaseAccess instance;
    private final List<UserModel> users;
    private final List<TaskModel> tasks;
    
    private DatabaseAccess() {
        this.users = new ArrayList<>();
        this.tasks = new ArrayList<>();
        initializeData();
    }
    
    public static synchronized DatabaseAccess getInstance() {
        if (instance == null) {
            instance = new DatabaseAccess();
        }
        return instance;
    }
    
    private void initializeData() {
        UserModel user1 = new UserModel("Alice");
        UserModel user2 = new UserModel("Bob");
        UserModel user3 = new UserModel("Charlie");
        
        users.add(user1);
        users.add(user2);
        users.add(user3);
        
        TaskModel task1 = new TaskBuilder()
                .title("Apprendre Java")
                .description("Étudier les concepts de base de Java")
                .createdBy(user1)
                .build();
        
        DatedTaskModel task2 = new TaskBuilder()
                .title("Faire les courses")
                .description("Acheter du pain, du lait et des fruits")
                .createdBy(user2)
                .dueDate(LocalDate.now().plusDays(2))
                .buildDatedTask();
        
        DatedTaskModel task3 = new TaskBuilder()
                .title("Rendre le projet")
                .description("Finaliser et soumettre le projet TODO List")
                .createdBy(user1)
                .dueDate(LocalDate.now().minusDays(1))
                .buildDatedTask();
        
        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);
    }
    
    public List<UserModel> getAllUsers() {
        return new ArrayList<>(users);
    }
    
    public UserModel findUserById(UUID id) throws EntityNotFoundException {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur", id));
    }
    
    public UserModel findUserByFirstName(String firstName) throws EntityNotFoundException {
        return users.stream()
                .filter(user -> user.getFirstName().equalsIgnoreCase(firstName))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur", firstName));
    }
    
    public void addUser(UserModel user) {
        users.add(user);
    }
    
    public void deleteUser(UUID id) throws EntityNotFoundException {
        UserModel userToDelete = findUserById(id);
        users.remove(userToDelete);
    }
    
    public void updateUser(UserModel updatedUser) throws EntityNotFoundException {
        UserModel existingUser = findUserById(updatedUser.getId());
        int index = users.indexOf(existingUser);
        users.set(index, updatedUser);
    }
    
    public List<TaskModel> getAllTasks() {
        return new ArrayList<>(tasks);
    }
    
    public TaskModel findTaskById(UUID id) throws EntityNotFoundException {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Tâche", id));
    }
    
    public List<TaskModel> findTasksByUser(UUID userId) throws EntityNotFoundException {
        UserModel user = findUserById(userId);
        return tasks.stream()
                .filter(task -> task.getCreatedBy().getId().equals(userId))
                .collect(Collectors.toList());
    }
    
    public List<TaskModel> findTasksByUserFirstName(String firstName) throws EntityNotFoundException {
        UserModel user = findUserByFirstName(firstName);
        return findTasksByUser(user.getId());
    }
    
    public List<DatedTaskModel> findOverdueTasks() {
        return tasks.stream()
                .filter(task -> task instanceof DatedTaskModel)
                .map(task -> (DatedTaskModel) task)
                .filter(DatedTaskModel::isOverdue)
                .collect(Collectors.toList());
    }
    
    public List<DatedTaskModel> findTasksDueToday() {
        return tasks.stream()
                .filter(task -> task instanceof DatedTaskModel)
                .map(task -> (DatedTaskModel) task)
                .filter(DatedTaskModel::isDueToday)
                .collect(Collectors.toList());
    }
    
    public void addTask(TaskModel task) {
        tasks.add(task);
    }
    
    public void deleteTask(UUID id) throws EntityNotFoundException {
        TaskModel taskToDelete = findTaskById(id);
        tasks.remove(taskToDelete);
    }
    
    public void updateTask(TaskModel updatedTask) throws EntityNotFoundException {
        TaskModel existingTask = findTaskById(updatedTask.getId());
        int index = tasks.indexOf(existingTask);
        tasks.set(index, updatedTask);
    }
    
    public void deleteAllTasks() {
        tasks.clear();
    }
    
    public boolean userExists(UUID id) {
        return users.stream().anyMatch(user -> user.getId().equals(id));
    }
    
    public boolean taskExists(UUID id) {
        return tasks.stream().anyMatch(task -> task.getId().equals(id));
    }
    
    public int getUserCount() {
        return users.size();
    }
    
    public int getTaskCount() {
        return tasks.size();
    }
} 