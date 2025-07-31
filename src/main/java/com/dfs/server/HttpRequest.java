package com.dfs.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private final Map<String, String> headers;
    private String body;
    
    public HttpRequest(BufferedReader reader) throws IOException {
        this.headers = new HashMap<>();
        parseRequest(reader);
    }
    
    private void parseRequest(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine();
        if (firstLine != null) {
            String[] parts = firstLine.split(" ");
            if (parts.length >= 2) {
                this.method = parts[0];
                this.path = parts[1];
            }
        }
        
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.contains(":")) {
                String[] header = line.split(":", 2);
                headers.put(header[0].trim(), header[1].trim());
            }
        }
        
        if ("POST".equals(method)) {
            StringBuilder bodyBuilder = new StringBuilder();
            while (reader.ready()) {
                bodyBuilder.append((char) reader.read());
            }
            this.body = bodyBuilder.toString();
        }
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getPath() {
        return path;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public String getBody() {
        return body;
    }
    
    public String getParameter(String name) {
        if (path.contains("?")) {
            String query = path.split("\\?")[1];
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(name)) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }
} 