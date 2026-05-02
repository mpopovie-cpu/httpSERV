package ru.otus.java.basic.april.server.processors;
// get items
import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;
import ru.otus.java.basic.april.server.ItemRepository;

import java.io.IOException;
import java.io.OutputStream;

public class GetItemsApiRequestProcessor implements RequestProcessor {
    private final ItemRepository repository;

    public GetItemsApiRequestProcessor(ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        HttpResponse.okJson(repository.findAll()).send(output);
    }
}