package ru.otus.java.basic;

import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class test {

    private ExecutorService executor;

    @BeforeAll
    void setup() {
        executor = Executors.newFixedThreadPool(5); // пул потоков
    }

    @AfterAll
    void teardown() {
        executor.shutdown(); // закрыть пул
    }

    @Test
    @DisplayName("Test multiple endpoints concurrently")
    void testEndpoints() throws InterruptedException {
        Runnable[] requests = new Runnable[] {
                () -> assertTrue(sendRequest("GET / HTTP/1.1\r\nHost: localhost\r\n\r\n", "Home Page").contains("Java Server")),
                () -> assertTrue(sendRequest("GET /calculator?a=5&b=7 HTTP/1.1\r\nHost: localhost\r\n\r\n", "Calculator").contains("5 + 7 = 12")),
                () -> assertTrue(sendRequest("GET /items HTTP/1.1\r\nHost: localhost\r\n\r\n", "Get Items").contains("Bread")),
                () -> assertTrue(sendRequest("POST /items HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\nContent-Length: 27\r\n\r\n{\"id\":4,\"name\":\"Butter\",\"price\":200}", "Create Item").contains("Item Created")),
                () -> assertTrue(sendRequest("GET /foo HTTP/1.1\r\nHost: localhost\r\n\r\n", "404 Page").contains("404"))
        };

        for (Runnable r : requests) {
            executor.submit(r);
        }

        executor.awaitTermination(5, TimeUnit.SECONDS); // ждем завершения
    }

    private String sendRequest(String rawRequest, String testName) {
        StringBuilder response = new StringBuilder();
        try (Socket socket = new Socket("localhost", 8189);
             OutputStream out = socket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.write(rawRequest.getBytes());
            out.flush();

            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
                if (line.isEmpty()) break; // конец заголовков
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }
}