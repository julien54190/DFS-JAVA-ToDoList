package com.dfs;

import com.dfs.server.TodoServer;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8080;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Port invalide, utilisation du port par défaut : 8080");
            }
        }
        
        TodoServer server = new TodoServer(port);
        server.start();
    }
} 