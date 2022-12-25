package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<Item> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long id) {
        return itemService.getAllByUserId(id);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id) {
        return ItemMapper.toItemDto(itemService.getItemById(id));
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemService.createItem(userId, item));
    }

    @GetMapping("/search")
    public List<Item> getItemByText(@RequestParam(name = "text") String text) {
        return itemService.getItemsByText(text);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@PathVariable Long id,
                              @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemService.updateItem(id, item, userId));
    }
}
