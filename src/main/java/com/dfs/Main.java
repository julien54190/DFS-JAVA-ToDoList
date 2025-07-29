package com.dfs;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Quel est votre nom ?");
            String name = scanner.nextLine();
            System.out.println("Bonjour, " + name + " !");
            
            String[] task = new String[] {
                "1. Créer un fichier",
                "2. Lire un fichier",
                "3. Écrire dans un fichier",
                "4. Supprimer un fichier",
                "5. Quitter"
            };
            
            System.out.println("Choisissez une tâche :");
            for (String t : task) {
                System.out.println(t);
            }
            
            System.out.print("Entrez le numéro de la tâche : ");
            int choix = scanner.nextInt();
            
            switch (choix) {
                case 1 -> System.out.println("Créer un fichier...");
                case 2 -> System.out.println("Lire un fichier...");
                case 3 -> System.out.println("Écrire dans un fichier...");
                case 4 -> System.out.println("Supprimer un fichier...");
                case 5 -> System.out.println("Au revoir !");
                default -> System.out.println("Choix invalide !");
            }
        }
    }
}
