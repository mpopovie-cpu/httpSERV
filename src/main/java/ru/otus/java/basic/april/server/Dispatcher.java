package ru.otus.java.basic.april.server;

import ru.otus.java.basic.april.server.processors.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private Map<String, RequestProcessor> processors;
    private RequestProcessor defaultNotFoundRequestProcessor;
    private RequestProcessor methodNotAllowedRequestProcessor;
    private ItemRepository itemRepository;

    public Dispatcher() {
        this.defaultNotFoundRequestProcessor = new DefaultNotFoundRequestProcessor();
        this.methodNotAllowedRequestProcessor = new MethodNotAllowedRequestProcessor();
        this.itemRepository = new ItemRepository();
        this.processors = new HashMap<>();
        this.processors.put("GET /calculator", new CalculatorRequestProcessor());
        this.processors.put("GET /", new HelloRequestProcessor());
        this.processors.put("GET /items", new GetItemsRequestProcessor());
        this.processors.put("GET /create", new CreatePageRequestProcessor());
        this.processors.put("POST /create/items", new CreateItemRequestProcessor());

        this.processors.put("GET /api/calculate", new CalculateApiRequestProcessor());
        this.processors.put("GET /api/items", new GetItemsApiRequestProcessor(itemRepository));
        this.processors.put("POST /api/items", new CreateItemApiRequestProcessor(itemRepository));
        this.processors.put("DELETE /api/items", new DeleteItemApiRequestProcessor(itemRepository));
    }

    public void execute(HttpRequest request, OutputStream output) throws IOException {
        if (!processors.containsKey(request.getRoutingKey())) {
            if (hasSameUriWithAnotherMethod(request)) {
                methodNotAllowedRequestProcessor.execute(request, output);
                return;
            }
            defaultNotFoundRequestProcessor.execute(request, output);
            return;
        }
        processors.get(request.getRoutingKey()).execute(request, output);
    }

    private boolean hasSameUriWithAnotherMethod(HttpRequest request) {
        return processors.keySet().stream()
                .map(key -> key.split(" ", 2))
                .anyMatch(parts -> parts.length == 2
                        && parts[1].equals(request.getUri())
                        && !parts[0].equals(request.getMethod().name()));
    }
}
