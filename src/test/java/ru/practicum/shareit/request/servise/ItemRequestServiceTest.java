package ru.practicum.shareit.request.servise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestWithItemDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemRequestServiceTest {

    private ItemRequestService itemRequestService;
    private ItemRequestRepository itemRequestRepository;
    private UserRepository userRepository;

    private ItemRequest itemRequestOne;
    private ItemRequest itemRequestTwo;
    private User userOne;

    @BeforeEach
    void createObject() {
        userRepository = mock(UserRepository.class);
        itemRequestRepository = mock(ItemRequestRepository.class);
        ItemRepository itemRepository = mock(ItemRepository.class);
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, userRepository, itemRepository);

        userOne = new User(
                1L,
                "User1",
                "User1@gmail.com");

        itemRequestOne = new ItemRequest(
                1L,
                "Description item request one",
                userOne,
                LocalDateTime.now());

        itemRequestTwo = new ItemRequest(
                2L,
                "Description item request two",
                userOne,
                LocalDateTime.now());
    }


    @Test
    void getAllItemRequestWithInvalidIdTest() {
        long id = 50;

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.getAllItemRequest(id));

        assertEquals("Пользователь 50 не существует.", exception.getMessage());
    }

    @Test
    void getItemRequestByIdTest() {
        when(userRepository.findById(itemRequestOne.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequestOne.getRequestor()));

        when(itemRequestRepository.findById(itemRequestOne.getId()))
                .thenReturn(Optional.of(itemRequestOne));
        var itemRequestById = itemRequestService
                .getItemRequestById(itemRequestOne.getRequestor().getId(), itemRequestOne.getId());

        assertNotNull(itemRequestById);
        assertEquals(itemRequestOne.getDescription(), itemRequestById.getDescription());
    }

    @Test
    void getAllItemRequestTest() {
        List<ItemRequest> itemRequestList = List.of(itemRequestOne, itemRequestTwo);

        when(userRepository.findById(itemRequestOne.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequestOne.getRequestor()));
        when(itemRequestRepository.findAll())
                .thenReturn(List.of(itemRequestOne, itemRequestTwo));
        final List<ItemRequestWithItemDto> itemRequestDtoWithItems = itemRequestService
                .getAllItemRequest(itemRequestOne.getRequestor().getId());

        assertNotNull(itemRequestDtoWithItems);
        assertEquals(itemRequestList.size(), itemRequestDtoWithItems.size());
        assertEquals(itemRequestOne.getDescription(), itemRequestDtoWithItems.get(0).getDescription());
    }

    @Test
    void getItemRequestByIdWithInvalidIdUserTest() {
        long id = 50;

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.getItemRequestById(id, itemRequestOne.getId()));

        assertEquals("Пользователь 50 не существует.", exception.getMessage());
    }

    @Test
    void getItemRequestByIdWithInvalidIdRequestTest() {
        long id = 50;

        when(userRepository.findById(itemRequestOne.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequestOne.getRequestor()));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.getItemRequestById(itemRequestOne.getRequestor().getId(), id));

        assertEquals("Запрос на вещь 50 не существует.", exception.getMessage());
    }

    @Test
    void getItemRequestOtherUsersWithInvalidIdUserTest() {
        long id = 50;

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.getItemRequestOtherUsers(id, 0, 20));

        assertEquals("Пользователь 50 не существует.", exception.getMessage());
    }

    @Test
    void getItemRequestOtherUsersWithInvalidFromTest() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemRequestService.getItemRequestOtherUsers(
                        itemRequestOne.getRequestor().getId(),
                        -1,
                        20));

        assertEquals("Переданы некорректные значения from и/или size", exception.getMessage());
    }

    @Test
    void getItemRequestOtherUsersWithInvalidSizeTest() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemRequestService.getItemRequestOtherUsers(
                        itemRequestOne.getRequestor().getId(),
                        1,
                        0));

        assertEquals("Переданы некорректные значения from и/или size", exception.getMessage());
    }

    @Test
    void createItemRequestEmptyDescriptionTest() {
        ItemRequest itemRequest = new ItemRequest(
                3L,
                null,
                userOne,
                LocalDateTime.now());

        when(userRepository.findById(itemRequestOne.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequestOne.getRequestor()));

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemRequestService.createItemRequest(
                        itemRequest.getRequestor().getId(),
                        ItemRequestMapper.toItemRequestDto(itemRequest)));

        assertEquals("Описание для создаваемого запроса не может быть пустым.", exception.getMessage());
    }

    @Test
    void createItemRequestTest() {
        when(userRepository.findById(itemRequestOne.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequestOne.getRequestor()));

        when(itemRequestRepository.findById(itemRequestOne.getRequestor().getId()))
                .thenReturn(Optional.of(itemRequestOne));

        when(itemRequestRepository.save(any()))
                .thenReturn(itemRequestOne);

        var itemRequest = itemRequestService.createItemRequest(
                itemRequestOne.getRequestor().getId(),
                ItemRequestMapper.toItemRequestDto(itemRequestOne));

        assertNotNull(itemRequest);
        assertEquals(itemRequestOne.getId(), itemRequest.getId());
        assertEquals(itemRequestOne.getDescription(), itemRequest.getDescription());
    }

    @Test
    void createItemRequestWithInvalidIdUserTest() {
        long id = 50;

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.createItemRequest(id, ItemRequestMapper.toItemRequestDto(itemRequestOne)));

        assertEquals("Пользователь 50 не существует.", exception.getMessage());
    }

    @Test
    void getItemRequestOtherUsersWithInvalidFromAndSizeTest() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemRequestService.getItemRequestOtherUsers(
                        itemRequestOne.getRequestor().getId(),
                        -1,
                        0));

        assertEquals("Переданы некорректные значения from и/или size", exception.getMessage());
    }
}