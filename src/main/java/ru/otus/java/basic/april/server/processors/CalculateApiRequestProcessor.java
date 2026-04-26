package ru.otus.java.basic.april.server.processors;

import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;

public class CalculateApiRequestProcessor implements RequestProcessor {
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        String aRaw = request.getParameter("a");
        String bRaw = request.getParameter("b");
        String operation = request.getParameter("operation");

        if (aRaw == null || bRaw == null || operation == null) {
            HttpResponse.of(400, "Bad Request", "application/json",
                    "{\"error\":\"Parameters a, b and operation are required\"}").send(output);
            return;
        }

        try {
            double a = Double.parseDouble(aRaw);
            double b = Double.parseDouble(bRaw);
            double result;
            String symbol;

            switch (operation) {
                case "add" -> {
                    result = a + b;
                    symbol = "+";
                }
                case "subtract" -> {
                    result = a - b;
                    symbol = "-";
                }
                case "multiply" -> {
                    result = a * b;
                    symbol = "*";
                }
                case "divide" -> {
                    if (b == 0) {
                        HttpResponse.of(400, "Bad Request", "application/json",
                                "{\"error\":\"Division by zero is not allowed\"}").send(output);
                        return;
                    }
                    result = a / b;
                    symbol = "/";
                }
                default -> {
                    HttpResponse.of(400, "Bad Request", "application/json",
                            "{\"error\":\"Unsupported operation\"}").send(output);
                    return;
                }
            }

            String responseBody = """
                    {"a":%s,"b":%s,"operation":"%s","expression":"%s %s %s","result":%s}
                    """.formatted(trimDouble(a), trimDouble(b), operation, trimDouble(a), symbol, trimDouble(b), trimDouble(result));
            HttpResponse.of(200, "OK", "application/json", responseBody).send(output);
        } catch (NumberFormatException e) {
            HttpResponse.of(400, "Bad Request", "application/json",
                    "{\"error\":\"Numbers must be valid\"}").send(output);
        }
    }

    private String trimDouble(double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
