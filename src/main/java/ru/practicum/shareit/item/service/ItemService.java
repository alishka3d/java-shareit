package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    List<Item> getAllItems();

    List<Item> getAllByUserId(Long userId);

    Item getItemById(Long itemId);

    List<Item> getItemsByText(String text);

    Item createItem(Long userId, Item item);

    void deleteItem(Long id);

    Item updateItem(Long id, Item item, Long userId);
}
