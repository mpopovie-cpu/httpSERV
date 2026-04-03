package ru.otus.java.basic.april.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.app.Item;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateItemRequestProcessor implements RequestProcessor {
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        Gson gson = new Gson();
        Item item = gson.fromJson(request.getBody(), Item.class);
        System.out.println(item);
        String response = "" +
                "HTTP/1.1 201 Created\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n";
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
