package com.dfs;

import com.dfs.server.TodoServer;
import com.dfs.service.ConfigService;

public class ServerMain {
    public static void main(String[] args) {
        ConfigService config = ConfigService.getInstance();
        int port = args.length > 0 ? Integer.parseInt(args[0]) : config.getServerPort();
        
        TodoServer server = new TodoServer(port);
        server.start();
    }
} 