package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public List<Item> getAllByUserId(Long userId) {
        userRepository.findById(userId);
        return itemRepository.findAllByUserId(userId);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.getItemById(itemId);
    }

    @Override
    public List<Item> getItemsByText(String text) {
        return itemRepository.findItemsByText(text);
    }

    @Override
    public Item createItem(Item item) {
        return itemRepository.create(item);
    }

    @Override
    public void deleteItem(Long id) {
        itemRepository.deleteItem(id);
    }

    @Override
    public Item updateItem(Long id, Item item) {
        return itemRepository.update(id, item);
    }
}
