package com.dfs.data;

import com.dfs.models.UserModel;
import com.dfs.models.TaskModel;
import com.dfs.models.DatedTaskModel;
import com.dfs.models.TaskBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class TaskService {
    private final DatabaseAccess database;
    private final UserModel currentUser;
    
    public TaskService(UserModel user) {
        this.database = DatabaseAccess.getInstance();
        this.currentUser = user;
    }
    
    public void addTask(Scanner scanner) {
        System.out.print("Titre de la tâche : ");
        String title = scanner.nextLine();
        
        System.out.print("Description : ");
        String description = scanner.nextLine();
        
        System.out.print("Tâche avec échéance ? (o/n) : ");
        String hasDueDate = scanner.nextLine().toLowerCase();
        
        try {
            if (hasDueDate.equals("o") || hasDueDate.equals("oui")) {
                LocalDate dueDate = readDate(scanner);
                if (dueDate != null) {
                    DatedTaskModel datedTask = new TaskBuilder()
                            .title(title)
                            .description(description)
                            .createdBy(currentUser)
                            .dueDate(dueDate)
                            .buildDatedTask();
                    database.addTask(datedTask);
                    System.out.println("Tâche avec échéance ajoutée !");
                }
            } else {
                TaskModel task = new TaskBuilder()
                        .title(title)
                        .description(description)
                        .createdBy(currentUser)
                        .build();
                database.addTask(task);
                System.out.println("Tâche ajoutée !");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
    
    private LocalDate readDate(Scanner scanner) {
        System.out.print("Date d'échéance (format dd/MM/yyyy) : ");
        String dateStr = scanner.nextLine();
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            System.out.println("Format de date invalide. Utilisez dd/MM/yyyy");
            return null;
        }
    }
    
    public void listAllTasks() {
        List<TaskModel> tasks = database.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("Aucune tâche enregistrée.");
            return;
        }
        
        System.out.println("Liste de toutes les tâches :");
        int i = 1;
        for (TaskModel task : tasks) {
            System.out.println(i + ". " + task);
            i++;
        }
    }
    
    public void listUserTasks() {
        try {
            List<TaskModel> tasks = database.findTasksByUser(currentUser.getId());
            if (tasks.isEmpty()) {
                System.out.println("Vous n'avez aucune tâche.");
                return;
            }
            
            System.out.println("Vos tâches :");
            int i = 1;
            for (TaskModel task : tasks) {
                System.out.println(i + ". " + task);
                i++;
            }
        } catch (EntityNotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
    
    public void deleteTask(Scanner scanner) {
        listUserTasks();
        System.out.print("Numéro de la tâche à supprimer : ");
        try {
            int num = Integer.parseInt(scanner.nextLine());
            List<TaskModel> userTasks = database.findTasksByUser(currentUser.getId());
            
            if (num >= 1 && num <= userTasks.size()) {
                TaskModel taskToDelete = userTasks.get(num - 1);
                database.deleteTask(taskToDelete.getId());
                System.out.println("Tâche supprimée : " + taskToDelete.getTitle());
            } else {
                System.out.println("Numéro invalide.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Veuillez entrer un numéro valide.");
        } catch (EntityNotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
    
    public void modifyTask(Scanner scanner) {
        listUserTasks();
        System.out.print("Numéro de la tâche à modifier : ");
        try {
            int num = Integer.parseInt(scanner.nextLine());
            List<TaskModel> userTasks = database.findTasksByUser(currentUser.getId());
            
            if (num >= 1 && num <= userTasks.size()) {
                TaskModel task = userTasks.get(num - 1);
                modifyTaskDetails(task, scanner);
            } else {
                System.out.println("Numéro invalide.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Veuillez entrer un numéro valide.");
        } catch (EntityNotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
    
    private void modifyTaskDetails(TaskModel task, Scanner scanner) {
        System.out.println("Modification de la tâche : " + task.getTitle());
        System.out.println("1. Modifier le titre");
        System.out.println("2. Modifier la description");
        System.out.println("3. Changer le statut");
        if (task instanceof DatedTaskModel) {
            System.out.println("4. Modifier la date d'échéance");
        }
        
        System.out.print("Votre choix : ");
        String choice = scanner.nextLine();
        
        try {
            switch (choice) {
                case "1" -> {
                    System.out.print("Nouveau titre : ");
                    task.setTitle(scanner.nextLine());
                    database.updateTask(task);
                    System.out.println("Titre modifié !");
                }
                case "2" -> {
                    System.out.print("Nouvelle description : ");
                    task.setDescription(scanner.nextLine());
                    database.updateTask(task);
                    System.out.println("Description modifiée !");
                }
                case "3" -> {
                    task.toggleDone();
                    database.updateTask(task);
                    System.out.println("Statut modifié !");
                }
                case "4" -> {
                    if (task instanceof DatedTaskModel datedTask) {
                        LocalDate newDate = readDate(scanner);
                        if (newDate != null) {
                            datedTask.setDueDate(newDate);
                            database.updateTask(datedTask);
                            System.out.println("Date d'échéance modifiée !");
                        }
                    }
                }
                default -> System.out.println("Choix invalide !");
            }
        } catch (EntityNotFoundException e) {
            System.out.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }
    
    public void clearAllTasks() {
        database.deleteAllTasks();
        System.out.println("Toutes les tâches ont été supprimées.");
    }
    
    public void showOverdueTasks() {
        List<DatedTaskModel> overdueTasks = database.findOverdueTasks();
        if (overdueTasks.isEmpty()) {
            System.out.println("Aucune tâche en retard.");
        } else {
            System.out.println("Tâches en retard :");
            for (DatedTaskModel task : overdueTasks) {
                System.out.println("- " + task);
            }
        }
    }
    
    public void showTasksDueToday() {
        List<DatedTaskModel> tasksDueToday = database.findTasksDueToday();
        if (tasksDueToday.isEmpty()) {
            System.out.println("Aucune tâche pour aujourd'hui.");
        } else {
            System.out.println("Tâches pour aujourd'hui :");
            for (DatedTaskModel task : tasksDueToday) {
                System.out.println("- " + task);
            }
        }
    }
    
    public void showStatistics() {
        System.out.println("=== STATISTIQUES ===");
        System.out.println("Nombre total d'utilisateurs : " + database.getUserCount());
        System.out.println("Nombre total de tâches : " + database.getTaskCount());
        System.out.println("Tâches en retard : " + database.findOverdueTasks().size());
        System.out.println("Tâches pour aujourd'hui : " + database.findTasksDueToday().size());
    }
    
    public UserModel getCurrentUser() {
        return currentUser;
    }
} 