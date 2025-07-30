package com.dfs.server;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private int statusCode;
    private String contentType;
    private String body;
    
    public HttpResponse(int statusCode, String contentType, String body) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
    }
    
    public void send(OutputStream outputStream) throws Exception {
        PrintWriter writer = new PrintWriter(outputStream, true);
        
        String statusText = getStatusText(statusCode);
        writer.println("HTTP/1.1 " + statusCode + " " + statusText);
        writer.println("Content-Type: " + contentType + "; charset=UTF-8");
        writer.println("Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length);
        writer.println();
        writer.println(body);
    }
    
    private String getStatusText(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
    
    public static HttpResponse ok(String body) {
        return new HttpResponse(200, "text/html", body);
    }
    
    public static HttpResponse json(String body) {
        return new HttpResponse(200, "application/json", body);
    }
    
    public static HttpResponse notFound() {
        return new HttpResponse(404, "text/html", "<h1>404 - Page non trouvée</h1>");
    }
    
    public static HttpResponse error(String message) {
        return new HttpResponse(500, "text/html", "<h1>500 - Erreur serveur</h1><p>" + message + "</p>");
    }
} 