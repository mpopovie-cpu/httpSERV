package ru.otus.java.basic.april.server.processors;
//
import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;
import ru.otus.java.basic.april.server.TemplateLoader;

import java.io.IOException;
import java.io.OutputStream;

public class HelloRequestProcessor implements RequestProcessor {
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        HttpResponse.ok(TemplateLoader.load("templates/home.html")).send(output);
    }
}