package ru.otus.java.basic.april.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;
import ru.otus.java.basic.april.server.TemplateLoader;
import ru.otus.java.basic.april.server.app.Item;

import java.io.IOException;
import java.io.OutputStream;

public class CreateItemRequestProcessor implements RequestProcessor {
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        Item item = new Gson().fromJson(request.getBody(), Item.class);
        System.out.println(item);

        HttpResponse.createdHtml(TemplateLoader.load("templates/created.html")).send(output);
    }
}