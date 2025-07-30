package com.dfs;

import com.dfs.models.UserModel;
import com.dfs.data.DatabaseAccess;
import com.dfs.data.EntityNotFoundException;
import com.dfs.data.TaskService;
import com.dfs.data.MenuService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Quel est votre prénom ?");
            String firstName = scanner.nextLine();
            
            UserModel user = getUser(firstName);
            TaskService taskService = new TaskService(user);
            MenuService menuService = new MenuService(taskService);
            
            boolean continuer = true;
            while (continuer) {
                menuService.afficherMenu();
                int choix = menuService.lireChoix(scanner);
                continuer = menuService.traiterChoix(choix, scanner);
            }
        }
    }
    
    private static UserModel getUser(String firstName) {
        try {
            UserModel user = DatabaseAccess.getInstance().findUserByFirstName(firstName);
            System.out.println("Bonjour, " + user.getFirstName() + " !");
            return user;
        } catch (EntityNotFoundException e) {
            UserModel user = new UserModel(firstName);
            DatabaseAccess.getInstance().addUser(user);
            System.out.println("Bienvenue, " + user.getFirstName() + " !");
            return user;
        }
    }
} 