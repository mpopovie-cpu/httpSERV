package ru.otus.java.basic.april.server.processors;

import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;
import ru.otus.java.basic.april.server.ItemRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class DeleteItemApiRequestProcessor implements RequestProcessor {
    private final ItemRepository repository;

    public DeleteItemApiRequestProcessor(ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        String idRaw = request.getParameter("id");
        boolean deleted = false;
        if (idRaw != null) {
            try {
                deleted = repository.deleteById(Long.parseLong(idRaw));
            } catch (NumberFormatException ignored) {
            }
        }
        HttpResponse.okJson(Map.of("deleted", deleted)).send(output);
    }
}