package ru.otus.java.basic.april.server;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private static final Gson GSON = new Gson();

    private int statusCode;
    private String statusText;
    private String contentType;
    private String body;

    private HttpResponse(int statusCode, String statusText, String contentType, String body) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.contentType = contentType;
        this.body = body;
    }

    // --- Static factory methods ---

    public static HttpResponse ok(String htmlBody) {
        return new HttpResponse(200, "OK", "text/html", htmlBody);
    }

    public static HttpResponse okJson(Object object) {
        return new HttpResponse(200, "OK", "application/json", GSON.toJson(object));
    }

    public static HttpResponse created(Object object) {
        return new HttpResponse(201, "Created", "application/json", GSON.toJson(object));
    }

    public static HttpResponse createdHtml(String htmlBody) {
        return new HttpResponse(201, "Created", "text/html", htmlBody);
    }

    public static HttpResponse notFound(String htmlBody) {
        return new HttpResponse(404, "Not Found", "text/html", htmlBody);
    }

    public static HttpResponse of(int statusCode, String statusText, String contentType, String body) {
        return new HttpResponse(statusCode, statusText, contentType, body);
    }

    // --- Send ---

    public void send(OutputStream output) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                "Content-Type: " + contentType + "\r\n\r\n" +
                body;
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}