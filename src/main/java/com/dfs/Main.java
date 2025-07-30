package com.dfs;

import com.dfs.models.UserModel;
import com.dfs.data.DatabaseAccess;
import com.dfs.data.EntityNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Quel est votre prénom ?");
            String firstName = scanner.nextLine();
            
            UserModel user;
            try {
                user = DatabaseAccess.getInstance().findUserByFirstName(firstName);
                System.out.println("Bonjour, " + user.getFirstName() + " !");
            } catch (EntityNotFoundException e) {
                user = new UserModel(firstName);
                DatabaseAccess.getInstance().addUser(user);
                System.out.println("Bienvenue, " + user.getFirstName() + " !");
            }
            
            TaskManager taskManager = new TaskManager(user);
            
            boolean continuer = true;
            while (continuer) {
                afficherMenu();
                int choix = lireChoix(scanner);
                continuer = traiterChoix(choix, scanner, taskManager);
            }
        }
    }
    
    private static void afficherMenu() {
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
    
    private static int lireChoix(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static boolean traiterChoix(int choix, Scanner scanner, TaskManager taskManager) {
        return switch (choix) {
            case 1 -> {
                taskManager.addTask(scanner);
                yield true;
            }
            case 2 -> {
                taskManager.listUserTasks();
                yield true;
            }
            case 3 -> {
                taskManager.listAllTasks();
                yield true;
            }
            case 4 -> {
                taskManager.modifyTask(scanner);
                yield true;
            }
            case 5 -> {
                taskManager.deleteTask(scanner);
                yield true;
            }
            case 6 -> {
                taskManager.clearAllTasks();
                yield true;
            }
            case 7 -> {
                taskManager.showOverdueTasks();
                yield true;
            }
            case 8 -> {
                taskManager.showTasksDueToday();
                yield true;
            }
            case 9 -> {
                taskManager.showStatistics();
                yield true;
            }
            case 10 -> {
                System.out.println("Au revoir, " + taskManager.getCurrentUser().getFirstName() + " !");
                yield false;
            }
            default -> {
                System.out.println("Choix invalide !");
                yield true;
            }
        };
    }
} 