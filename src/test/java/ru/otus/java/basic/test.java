package ru.otus.java.basic;

import org.junit.jupiter.api.Test;
import ru.otus.java.basic.april.server.Dispatcher;
import ru.otus.java.basic.april.server.HttpMethod;
import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;
import ru.otus.java.basic.april.server.ItemRepository;
import ru.otus.java.basic.april.server.TemplateLoader;
import ru.otus.java.basic.april.server.app.Item;
import ru.otus.java.basic.april.server.processors.CalculateApiRequestProcessor;
import ru.otus.java.basic.april.server.processors.CreateItemApiRequestProcessor;
import ru.otus.java.basic.april.server.processors.CreatePageRequestProcessor;
import ru.otus.java.basic.april.server.processors.DefaultNotFoundRequestProcessor;
import ru.otus.java.basic.april.server.processors.DeleteItemApiRequestProcessor;
import ru.otus.java.basic.april.server.processors.GetItemsApiRequestProcessor;
import ru.otus.java.basic.april.server.processors.GetItemsRequestProcessor;
import ru.otus.java.basic.april.server.processors.HelloRequestProcessor;
import ru.otus.java.basic.april.server.processors.MethodNotAllowedRequestProcessor;
import ru.otus.java.basic.april.server.processors.RequestProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class test {

    @Test
    void httpRequestParsesQueryParameters() {
        HttpRequest request = new HttpRequest("GET /api/items?id=7&mode=full HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/api/items", request.getUri());
        assertEquals("7", request.getParameter("id"));
        assertEquals("full", request.getParameter("mode"));
        assertEquals("GET /api/items", request.getRoutingKey());
    }

    @Test
    void httpRequestParsesPostBody() {
        HttpRequest request = new HttpRequest(
                "POST /api/items HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\n\r\n{\"name\":\"Keyboard\",\"price\":250}"
        );

        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/api/items", request.getUri());
        assertEquals("{\"name\":\"Keyboard\",\"price\":250}", request.getBody());
    }

    @Test
    void httpResponseSendsStatusHeadersAndBody() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        HttpResponse.okJson(List.of("a", "b")).send(output);

        String response = asString(output);
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Content-Type: application/json"));
        assertTrue(response.endsWith("[\"a\",\"b\"]"));
    }

    @Test
    void templateLoaderLoadsHomeTemplate() throws IOException {
        String html = TemplateLoader.load("templates/home.html");

        assertTrue(html.contains("spline-viewer"));
        assertTrue(html.contains("Nova Items"));
    }

    @Test
    void calculateApiProcessorHandlesAddition() throws IOException {
        String response = execute(new CalculateApiRequestProcessor(),
                "GET /api/calculate?a=5&b=7&operation=add HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.contains("\"expression\":\"5 + 7\""));
        assertTrue(response.contains("\"result\":12"));
    }

    @Test
    void calculateApiProcessorRejectsDivisionByZero() throws IOException {
        String response = execute(new CalculateApiRequestProcessor(),
                "GET /api/calculate?a=9&b=0&operation=divide HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 400 Bad Request"));
        assertTrue(response.contains("Division by zero is not allowed"));
    }

    @Test
    void calculateApiProcessorRejectsMissingParameters() throws IOException {
        String response = execute(new CalculateApiRequestProcessor(),
                "GET /api/calculate?a=9&b=2 HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 400 Bad Request"));
        assertTrue(response.contains("Parameters a, b and operation are required"));
    }

    @Test
    void helloRequestProcessorReturnsHomePage() throws IOException {
        String response = execute(new HelloRequestProcessor(), "GET / HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.contains("<spline-viewer"));
        assertTrue(response.contains("Nova Items"));
    }

    @Test
    void itemsRequestProcessorReturnsItemsPage() throws IOException {
        String response = execute(new GetItemsRequestProcessor(), "GET /items HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Items Console"));
        assertTrue(response.contains("Raw Items JSON"));
    }

    @Test
    void createPageRequestProcessorReturnsCreatePage() throws IOException {
        String response = execute(new CreatePageRequestProcessor(), "GET /create HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Create Flow"));
        assertTrue(response.contains("Create Item"));
    }

    @Test
    void defaultNotFoundProcessorReturns404Template() throws IOException {
        String response = execute(new DefaultNotFoundRequestProcessor(), "GET /missing HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 404 Not Found"));
        assertTrue(response.contains("Page Not Found"));
    }

    @Test
    void methodNotAllowedProcessorReturns405Template() throws IOException {
        String response = execute(new MethodNotAllowedRequestProcessor(), "POST / HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 405 Method Not Allowed"));
        assertTrue(response.contains("Method Not Allowed"));
    }

    @Test
    void createItemApiProcessorSavesItemAndReturnsCreatedJson() throws IOException {
        FakeItemRepository repository = new FakeItemRepository();
        CreateItemApiRequestProcessor processor = new CreateItemApiRequestProcessor(repository);

        String response = execute(processor,
                "POST /api/items HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\n\r\n{\"name\":\"Mouse\",\"price\":99}");

        assertTrue(response.startsWith("HTTP/1.1 201 Created"));
        assertNotNull(repository.savedItem);
        assertEquals("Mouse", repository.savedItem.getName());
        assertEquals(99, repository.savedItem.getPrice());
        assertTrue(response.contains("\"id\":101"));
        assertTrue(response.contains("\"name\":\"Mouse\""));
    }

    @Test
    void getItemsApiProcessorReturnsAllItems() throws IOException {
        FakeItemRepository repository = new FakeItemRepository();
        repository.items.add(new Item(1L, "Bread", 50));
        repository.items.add(new Item(2L, "Milk", 150));

        String response = execute(new GetItemsApiRequestProcessor(repository),
                "GET /api/items HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.contains("\"name\":\"Bread\""));
        assertTrue(response.contains("\"name\":\"Milk\""));
    }

    @Test
    void getItemsApiProcessorReturnsSingleItemById() throws IOException {
        FakeItemRepository repository = new FakeItemRepository();
        repository.itemById = Optional.of(new Item(7L, "Keyboard", 250));

        String response = execute(new GetItemsApiRequestProcessor(repository),
                "GET /api/items?id=7 HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.contains("\"id\":7"));
        assertTrue(response.contains("\"name\":\"Keyboard\""));
    }

    @Test
    void getItemsApiProcessorReturns404WhenItemMissing() throws IOException {
        FakeItemRepository repository = new FakeItemRepository();

        String response = execute(new GetItemsApiRequestProcessor(repository),
                "GET /api/items?id=7 HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 404 Not Found"));
        assertTrue(response.contains("Item not found"));
    }

    @Test
    void getItemsApiProcessorRejectsInvalidId() throws IOException {
        FakeItemRepository repository = new FakeItemRepository();

        String response = execute(new GetItemsApiRequestProcessor(repository),
                "GET /api/items?id=abc HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 400 Bad Request"));
        assertTrue(response.contains("Invalid item id"));
    }

    @Test
    void deleteItemApiProcessorDeletesById() throws IOException {
        FakeItemRepository repository = new FakeItemRepository();
        repository.deleteResult = true;

        String response = execute(new DeleteItemApiRequestProcessor(repository),
                "DELETE /api/items?id=9 HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertEquals(9L, repository.deletedId);
        assertTrue(response.contains("\"deleted\":true"));
    }

    @Test
    void deleteItemApiProcessorHandlesInvalidIdGracefully() throws IOException {
        FakeItemRepository repository = new FakeItemRepository();

        String response = execute(new DeleteItemApiRequestProcessor(repository),
                "DELETE /api/items?id=oops HTTP/1.1\r\nHost: localhost\r\n\r\n");

        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertNull(repository.deletedId);
        assertTrue(response.contains("\"deleted\":false"));
    }

    @Test
    void dispatcherRoutesToConfiguredProcessor() throws IOException {
        FakeItemRepository repository = new FakeItemRepository();
        repository.items.add(new Item(1L, "Bread", 50));
        Dispatcher dispatcher = new Dispatcher(repository);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        dispatcher.execute(new HttpRequest("GET /api/items HTTP/1.1\r\nHost: localhost\r\n\r\n"), output);

        String response = asString(output);
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.contains("\"name\":\"Bread\""));
    }

    @Test
    void dispatcherReturns405ForKnownUriWithWrongMethod() throws IOException {
        Dispatcher dispatcher = new Dispatcher(new FakeItemRepository());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        dispatcher.execute(new HttpRequest("POST / HTTP/1.1\r\nHost: localhost\r\n\r\n"), output);

        String response = asString(output);
        assertTrue(response.startsWith("HTTP/1.1 405 Method Not Allowed"));
        assertTrue(response.contains("Method Not Allowed"));
    }

    @Test
    void dispatcherReturns404ForUnknownUri() throws IOException {
        Dispatcher dispatcher = new Dispatcher(new FakeItemRepository());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        dispatcher.execute(new HttpRequest("GET /unknown HTTP/1.1\r\nHost: localhost\r\n\r\n"), output);

        String response = asString(output);
        assertTrue(response.startsWith("HTTP/1.1 404 Not Found"));
        assertTrue(response.contains("Page Not Found"));
    }

    private static String execute(RequestProcessor processor, String rawRequest) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        processor.execute(new HttpRequest(rawRequest), output);
        return asString(output);
    }

    private static String asString(ByteArrayOutputStream output) {
        return output.toString(StandardCharsets.UTF_8);
    }

    private static class FakeItemRepository extends ItemRepository {
        private final List<Item> items = new ArrayList<>();
        private Optional<Item> itemById = Optional.empty();
        private Item savedItem;
        private Long deletedId;
        private boolean deleteResult;

        @Override
        public List<Item> findAll() {
            return items;
        }

        @Override
        public Optional<Item> findById(long id) {
            return itemById;
        }

        @Override
        public Item save(Item item) {
            savedItem = item;
            item.setId(101L);
            return item;
        }

        @Override
        public boolean deleteById(long id) {
            deletedId = id;
            return deleteResult;
        }
    }
}
