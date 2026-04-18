package ru.otus.java.basic.april.server;

import ru.otus.java.basic.april.server.app.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class ItemRepository {
    private final AtomicLong idSequence = new AtomicLong(3);
    private final List<Item> items = new ArrayList<>();

    public ItemRepository() {
        items.add(new Item(1L, "Bread", 50));
        items.add(new Item(2L, "Milk", 150));
        items.add(new Item(3L, "Cheese", 400));
    }

    public synchronized List<Item> findAll() {
        return items.stream()
                .sorted(Comparator.comparingLong(Item::getId))
                .toList();
    }

    public synchronized Optional<Item> findById(long id) {
        return items.stream()
                .filter(item -> item.getId() == id)
                .findFirst();
    }

    public synchronized Item save(Item item) {
        if (item.getId() == null || item.getId() == 0L) {
            item.setId(idSequence.incrementAndGet());
        }
        items.add(item);
        return item;
    }

    public synchronized boolean deleteById(long id) {
        Optional<Item> toRemove = items.stream().filter(i -> i.getId() == id).findFirst();
        toRemove.ifPresent(items::remove);
        return toRemove.isPresent();
    }
}
