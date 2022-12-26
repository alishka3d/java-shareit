package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    List<Item> findAllByUserId(Long userId);
    Item getItemById(Long itemId);
    List<Item> findItemsByText(String text);
    Item create(Item item);
    void deleteItem(Long id);
    Item update(Long id, Item item);
}
