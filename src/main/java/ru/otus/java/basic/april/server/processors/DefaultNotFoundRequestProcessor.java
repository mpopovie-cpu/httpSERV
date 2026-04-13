package ru.otus.java.basic.april.server.processors;
// not found
import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;
import ru.otus.java.basic.april.server.TemplateLoader;

import java.io.IOException;
import java.io.OutputStream;

public class DefaultNotFoundRequestProcessor implements RequestProcessor {
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        HttpResponse.notFound(TemplateLoader.load("templates/notfound.html")).send(output);
    }
}