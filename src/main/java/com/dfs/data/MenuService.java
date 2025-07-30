package com.dfs.data;

import java.util.Scanner;

public class MenuService {
    private final TaskService taskService;
    
    public MenuService(TaskService taskService) {
        this.taskService = taskService;
    }
    
    public void afficherMenu() {
        System.out.println("\n=== GESTIONNAIRE DE TÂCHES ===");
        System.out.println("1. Créer une tâche");
        System.out.println("2. Lister mes tâches");
        System.out.println("3. Lister toutes les tâches");
        System.out.println("4. Modifier une tâche");
        System.out.println("5. Supprimer une tâche");
        System.out.println("6. Supprimer toutes les tâches");
        System.out.println("7. Voir les tâches en retard");
        System.out.println("8. Voir les tâches pour aujourd'hui");
        System.out.println("9. Voir les statistiques");
        System.out.println("10. Quitter");
        System.out.print("Votre choix : ");
    }
    
    public int lireChoix(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    public boolean traiterChoix(int choix, Scanner scanner) {
        return switch (choix) {
            case 1 -> {
                taskService.addTask(scanner);
                yield true;
            }
            case 2 -> {
                taskService.listUserTasks();
                yield true;
            }
            case 3 -> {
                taskService.listAllTasks();
                yield true;
            }
            case 4 -> {
                taskService.modifyTask(scanner);
                yield true;
            }
            case 5 -> {
                taskService.deleteTask(scanner);
                yield true;
            }
            case 6 -> {
                taskService.clearAllTasks();
                yield true;
            }
            case 7 -> {
                taskService.showOverdueTasks();
                yield true;
            }
            case 8 -> {
                taskService.showTasksDueToday();
                yield true;
            }
            case 9 -> {
                taskService.showStatistics();
                yield true;
            }
            case 10 -> {
                System.out.println("Au revoir, " + taskService.getCurrentUser().getFirstName() + " !");
                yield false;
            }
            default -> {
                System.out.println("Choix invalide !");
                yield true;
            }
        };
    }
} 