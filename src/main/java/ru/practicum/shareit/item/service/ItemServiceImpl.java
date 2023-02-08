package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDtoForItem;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDtoWithBooking;

@Service
@AllArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemDtoWithBooking> getAllByUserId(Long userId) {
        List<ItemDtoWithBooking> itemsDtoWithBookingList = itemRepository.findAll().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDtoWithBooking)
                .collect(Collectors.toList());
        for (ItemDtoWithBooking itemDtoWithBooking : itemsDtoWithBookingList) {
            createLastAndNextBooking(itemDtoWithBooking);
            List<Comment> comments = commentRepository.findAllByItemId(itemDtoWithBooking.getId());
            if (!comments.isEmpty()) {
                itemDtoWithBooking.setComments(comments
                        .stream().map(CommentMapper::toCommentDto)
                        .collect(Collectors.toList()));
            }
        }
        itemsDtoWithBookingList.sort(Comparator.comparing(ItemDtoWithBooking::getId));
        log.info("Все вещи:");
        return itemsDtoWithBookingList;
    }

    @Override
    public ItemDtoWithBooking getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Вещь %s не существует.", itemId)));
        ItemDtoWithBooking itemDtoWithBooking = toItemDtoWithBooking(item);
        if (item.getOwner().getId().equals(userId)) {
            createLastAndNextBooking(itemDtoWithBooking);
        }
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        if (!comments.isEmpty()) {
            itemDtoWithBooking.setComments(comments
                    .stream().map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList())
            );
        }
        log.info("Вещь с id {}:{}", itemId, itemDtoWithBooking);
        return itemDtoWithBooking;
    }

    @Override
    public List<ItemDto> getItemsByText(String searchText) {
        if (searchText.isEmpty()) {
            log.info("Результат поиска :");
            return new ArrayList<>();
        }
        log.info("Результат поиска :");
        return itemRepository.search(searchText)
                .stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователя с %s не существует.", userId)));
        if (itemDto.getName().isEmpty() || itemDto.getDescription() == null || itemDto.getAvailable() == null) {
            log.error("Данное поле не может быть пустым.");
            throw new ValidationException("Данное поле не может быть пустым.");
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userRepository.findById(userId).get());
        Item itemCreate = itemRepository.save(item);
        log.info("Добавлена вещь с id {}: {}", itemCreate.getId(), itemCreate);
        return ItemMapper.toItemDto(itemCreate);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                String.format("Пользователя с %s не существует.", id)));
        log.info("Удалена вещь с id {}", id);
        itemRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) {
        Item item = ItemMapper.toItem(itemDto);
        final Item itemUpdate = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Вещь с id %s не существует.", itemId)));
        if (itemUpdate.getOwner().getId().equals(userId)) {
            if (item.getAvailable() != null && item.getName() == null && item.getDescription() == null) {
                itemUpdate.setAvailable(item.getAvailable());
                itemRepository.save(itemUpdate);
                log.info("Обновлена вещь с id {}:{}", itemId, itemUpdate);
                return ItemMapper.toItemDto(itemUpdate);
            } else if (item.getName() != null && item.getAvailable() == null && item.getDescription() == null) {
                itemUpdate.setName(item.getName());
                itemRepository.save(itemUpdate);
                log.info("Обновлена вещь с id {}:{}", itemId, itemUpdate);
                return ItemMapper.toItemDto(itemUpdate);
            } else if (item.getDescription() != null && item.getName() == null && item.getAvailable() == null) {
                itemUpdate.setDescription(item.getDescription());
                itemRepository.save(itemUpdate);
                log.info("Обновлена вещь с id {}:{}", itemId, itemUpdate);
                return ItemMapper.toItemDto(itemUpdate);
            } else {
                itemUpdate.setName(item.getName());
                itemUpdate.setDescription(item.getDescription());
                itemUpdate.setAvailable(item.getAvailable());
                itemRepository.save(itemUpdate);
                log.info("Обновлена вещь с id {}:{}", itemId, itemUpdate);
                return ItemMapper.toItemDto(itemUpdate);
            }
        } else {
            log.error("Пользователя с id  {} не владеет вещью.", userId);
            throw new EntityNotFoundException(
                    String.format("Пользователя с id  %s не владеет вещью.", userId));
        }
    }

    private void createLastAndNextBooking(ItemDtoWithBooking itemDtoWithBooking) {
        List<Booking> lastBookings = bookingRepository
                .findBookingsByItemIdAndEndIsBeforeOrderByEndDesc(itemDtoWithBooking.getId(),
                        LocalDateTime.now());
        if (!lastBookings.isEmpty()) {
            BookingItemDto lastBooking = toBookingDtoForItem(lastBookings.get(0));
            itemDtoWithBooking.setLastBooking(lastBooking);
        }
        List<Booking> nextBookings = bookingRepository
                .findBookingsByItemIdAndStartIsAfterOrderByStartDesc(itemDtoWithBooking.getId(),
                        LocalDateTime.now());
        if (!nextBookings.isEmpty()) {
            BookingItemDto nextBooking = toBookingDtoForItem(nextBookings.get(0));
            itemDtoWithBooking.setNextBooking(nextBooking);
        }
    }
}
