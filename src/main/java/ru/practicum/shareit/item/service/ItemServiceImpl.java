package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

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
    public Item createItem(Long userId, Item item) {
        if (userRepository.findById(userId) == null) {
            throw new EntityNotFoundException("Пользователь не существует");
        }
        if (item.getName().isBlank() || item.getDescription() == null || item.getAvailable() == null) {
            log.error("Данное поле не может быть пустым.");
            throw new ValidationException("Данное поле не может быть пустым.");
        }
        item.setOwner(userRepository.findById(userId));
        return itemRepository.create(item);
    }

    @Override
    public void deleteItem(Long id) {
        itemRepository.deleteItem(id);
    }

    @Override
    public Item updateItem(Long id, Item item, Long userId) {
        if (userRepository.findById(userId) == null) {
            log.error("User с id {} не существует", userId);
            throw new EntityNotFoundException("Пользователь не существует");
        }
        if (itemRepository.getItemById(id).getOwner().getId() != userId) {
            log.error("Пользователь с id {} не владеет вещью с id {}", userId, id);
            throw new EntityNotFoundException("Пользователь не владеет вещью");
        }
        return itemRepository.update(id, item);
    }
}
