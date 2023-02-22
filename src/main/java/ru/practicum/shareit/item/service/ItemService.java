package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;

import java.util.List;

public interface ItemService {

    List<ItemDtoWithBooking> getAllByUserId(Long userId, int from, int size);

    ItemDtoWithBooking getItemById(Long userId, Long itemId);

    List<ItemDto> getItemsByText(String text, int from, int size);

    ItemDto createItem(ItemDto itemDto, Long userId);

    void deleteItem(Long id);

    ItemDto updateItem(ItemDto itemDto, Long userId, Long id);
}
