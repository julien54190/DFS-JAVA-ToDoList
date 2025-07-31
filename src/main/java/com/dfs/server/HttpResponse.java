package com.dfs.server;

import java.io.PrintWriter;

public class HttpResponse {
    private final int statusCode;
    private final String contentType;
    private final String body;
    
    public HttpResponse(int statusCode, String contentType, String body) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
    }
    
    public static HttpResponse ok(String body) {
        return new HttpResponse(200, "text/html; charset=UTF-8", body);
    }
    
    public static HttpResponse json(String json) {
        return new HttpResponse(200, "application/json; charset=UTF-8", json);
    }
    
    public static HttpResponse css(String css) {
        return new HttpResponse(200, "text/css; charset=UTF-8", css);
    }
    
    public static HttpResponse notFound() {
        return new HttpResponse(404, "text/html; charset=UTF-8", 
            "<h1>404 - Page non trouvée</h1>");
    }
    
    public static HttpResponse error(String message) {
        return new HttpResponse(400, "text/html; charset=UTF-8", 
            "<h1>Erreur</h1><p>" + message + "</p>");
    }
    
    public void send(java.io.OutputStream output) {
        try (PrintWriter writer = new PrintWriter(output, true)) {
            writer.println("HTTP/1.1 " + statusCode + " " + getStatusText());
            writer.println("Content-Type: " + contentType);
            writer.println("Content-Length: " + body.getBytes("UTF-8").length);
            writer.println();
            writer.println(body);
        } catch (Exception e) {
            System.err.println("Erreur envoi réponse : " + e.getMessage());
        }
    }
    
    private String getStatusText() {
        return switch (statusCode) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            default -> "Unknown";
        };
    }
} 