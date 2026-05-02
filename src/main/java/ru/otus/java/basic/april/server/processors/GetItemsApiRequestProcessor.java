package ru.otus.java.basic.april.server.processors;

import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;
import ru.otus.java.basic.april.server.ItemRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class GetItemsApiRequestProcessor implements RequestProcessor {
    private final ItemRepository repository;

    public GetItemsApiRequestProcessor(ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        String idRaw = request.getParameter("id");
        if (idRaw == null || idRaw.isBlank()) {
            HttpResponse.okJson(repository.findAll()).send(output);
            return;
        }

        try {
            long id = Long.parseLong(idRaw);
            var itemOptional = repository.findById(id);
            if (itemOptional.isPresent()) {
                HttpResponse.okJson(itemOptional.get()).send(output);
                return;
            }
            HttpResponse.of(404, "Not Found", "application/json",
                    "{\"error\":\"Item not found\",\"id\":" + id + "}").send(output);
        } catch (NumberFormatException e) {
            HttpResponse.of(400, "Bad Request", "application/json",
                    "{\"error\":\"Invalid item id\"}").send(output);
        }
    }
}
