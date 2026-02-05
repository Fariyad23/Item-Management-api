package com.example.itemapi.service;

import com.example.itemapi.model.Item;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ItemService {

    private final List<Item> items = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Item addItem(Item item) {
        item.setId(idCounter.getAndIncrement());
        items.add(item);
        return item;
    }

        public List<Item> getAllItems() {
            return items;
    }
}