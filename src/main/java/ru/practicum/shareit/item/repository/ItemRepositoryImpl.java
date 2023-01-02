package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {

    private static long id;
    private final Map<Long, Item> items = new HashMap<>();

    private long generateId() {
        return ++id;
    }

    @Override
    public List<Item> findAllByUserId(Long userId) {
        log.info("Все вещи пользователя с id {}", userId);
        return items.values().stream().filter(f -> f.getOwner().getId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemById(Long itemId) {
        log.info("Вещь с id {}", itemId);
        return items.get(itemId);
    }

    @Override
    public List<Item> findItemsByText(String text) {
        log.info("Найденные вещи:");
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return items.values()
                .stream().filter(Item::getAvailable)
                .filter(f -> f.getName().toLowerCase().contains(text.toLowerCase()) ||
                        f.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Item create(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        log.info("Вещь с id {} добавлена в список", item.getId());
        return items.get(item.getId());
    }

    @Override
    public void deleteItem(Long id) {
        if (items.containsKey(id)) {
            log.info("Вещь с id {} удалена", id);
            items.remove(id);
        } else {
            log.error("Вещь с id {} в списке не найдена", id);
            throw new EntityNotFoundException("Вещь с id " + id + " в списке не найдена");
        }
    }

    @Override
    public Item update(Long id, Item item) {
        if (items.containsKey(id)) {
            if (item.getName() != null) {
                items.get(id).setName(item.getName());
            }
            if (item.getDescription() != null) {
                items.get(id).setDescription(item.getDescription());
            }
            if (item.getAvailable() != null) {
                items.get(id).setAvailable(item.getAvailable());
            }
            log.info("Вещь с id {} обновлена", id);
            return items.get(id);
        }
        log.error("Вещь с id {} не найдена", id);
        throw new EntityNotFoundException("Вещь с id " + id + " не найдена");
    }
}
