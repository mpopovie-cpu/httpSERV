package ru.otus.java.basic.april.server;
//
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class TemplateLoader {
    private TemplateLoader() {
    }

    public static String load(String resourcePath) throws IOException {
        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        ClassLoader classLoader = TemplateLoader.class.getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(normalized)) {
            if (in == null) {
                throw new IOException("Template not found: " + resourcePath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
