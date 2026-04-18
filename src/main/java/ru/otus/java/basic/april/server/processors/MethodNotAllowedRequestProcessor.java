package ru.otus.java.basic.april.server.processors;

import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;
import ru.otus.java.basic.april.server.TemplateLoader;

import java.io.IOException;
import java.io.OutputStream;

public class MethodNotAllowedRequestProcessor implements RequestProcessor {
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        HttpResponse.of(405, "Method Not Allowed", "text/html/js",
                TemplateLoader.load("templates/method-not-allowed.html")).send(output);
    }
}
