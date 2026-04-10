package ru.otus.java.basic.april.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;
import ru.otus.java.basic.april.server.ItemRepository;
import ru.otus.java.basic.april.server.app.Item;

import java.io.IOException;
import java.io.OutputStream;

public class CreateItemApiRequestProcessor implements RequestProcessor {
    private final ItemRepository repository;
    private final Gson gson;

    public CreateItemApiRequestProcessor(ItemRepository repository) {
        this.repository = repository;
        this.gson = new Gson();
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        Item item = gson.fromJson(request.getBody(), Item.class);
        Item saved = repository.save(item);
        HttpResponse.created(saved).send(output);
    }
}