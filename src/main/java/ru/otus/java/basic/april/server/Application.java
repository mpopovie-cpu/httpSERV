package ru.otus.java.basic.april.server;
//
public class Application {
    // Темы для консультации:
    // Граница между приложение и сервером = Java EE + TomCat
    // REST API, простой фронтенд + возврат файлов
    // Оптимизация ресурсов

    public static void main(String[] args) {
        new HttpServer(8189).start();
    }
}