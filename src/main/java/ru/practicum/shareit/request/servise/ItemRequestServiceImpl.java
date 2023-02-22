package ru.practicum.shareit.request.servise;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.PageRequestOverride;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public List<ItemRequestWithItemDto> getAllItemRequest(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", userId)));
        List<ItemRequestWithItemDto> itemRequestAllList = new ArrayList<>();
        for (ItemRequest item : itemRequestRepository.findAll()) {
            ItemRequestWithItemDto itemRequestWithItemDto = ItemRequestMapper.toItemRequestWithItemDto(item);
            itemRequestWithItemDto.setItems(itemRepository.findByRequestId(item.getId()));
            itemRequestAllList.add(itemRequestWithItemDto);
        }
        log.info("Все запросы : {}", itemRequestAllList);
        return itemRequestAllList;
    }

    @Override
    public ItemRequestWithItemDto getItemRequestById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", userId)));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Запрос на вещь %s не существует.", requestId)));
        ItemRequestWithItemDto itemRequestWithItemDto = ItemRequestMapper.toItemRequestWithItemDto(itemRequest);
        itemRequestWithItemDto.setItems(itemRepository.findByRequestId(itemRequest.getId()));
        log.info("Запрос с id {}", requestId);
        return itemRequestWithItemDto;
    }

    @Override
    public List<ItemRequestWithItemDto> getItemRequestOtherUsers(Long userId, int from, int size) {
        if (from < 0 || size <= 0) {
            log.error("Переданы некорректные значения from и/или size");
            throw new ValidationException("Переданы некорректные значения from и/или size");
        }
        PageRequestOverride pageRequest = PageRequestOverride.of(from, size);
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", userId)));
        List<ItemRequestWithItemDto> itemRequestAllList = new ArrayList<>();
        for (ItemRequest item : itemRequestRepository.findAll(pageRequest)) {
            if (!item.getRequestor().getId().equals(userId)) {
                ItemRequestWithItemDto itemRequestWithItemDto = ItemRequestMapper.toItemRequestWithItemDto(item);
                itemRequestWithItemDto.setItems(itemRepository.findByRequestId(item.getId()));
                itemRequestAllList.add(itemRequestWithItemDto);
            } else {
                return new ArrayList<>();
            }
        }
        log.info("Все запросы : {}", itemRequestAllList);
        return itemRequestAllList;
    }

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", userId)));
        if (itemRequestDto.getDescription() == null) {
            log.error("Описание для создаваемого запроса не может быть пустым.");
            throw new ValidationException("Описание для создаваемого запроса не может быть пустым.");
        }
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        ItemRequest itemRequestCreate = itemRequestRepository.save(itemRequest);
        log.info("Добавлен запрос :{}", itemRequestCreate);
        return ItemRequestMapper.toItemRequestDto(itemRequestCreate);
    }
}
