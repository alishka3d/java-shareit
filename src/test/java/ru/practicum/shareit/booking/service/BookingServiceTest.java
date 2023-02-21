package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.PageRequestOverride;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exceptions.BookingException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingServiceTest {

    private BookingService bookingService;
    private BookingRepository bookingRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private Booking booking;
    private User owner;
    private User booker;
    private Item item;
    private PageRequestOverride pageRequest;


    @BeforeEach
    void beforeEach() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);

        owner = new User(
                1L,
                "Name One",
                "NameOne@gmail.com");
        booker = new User(
                2L,
                "Name Two",
                "NameTwo@gmail.com");
        item = new Item(
                1L,
                "Item One",
                "Description item one",
                true,
                owner,
                null);
        booking = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                booker,
                Status.APPROVED);

        pageRequest = PageRequestOverride.of(0, 20);
    }

    @Test
    void getAllBookingsTest() {
        when(userRepository.findById(booking.getBooker().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.findByBookerIdOrderByStartDesc(booking.getBooker().getId(), pageRequest))
                .thenReturn(Collections.singletonList(booking));
        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookings(
                        booking.getBooker().getId(),
                        "ALL",
                        0,
                        20);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(booking.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingsStatusCurrentTest() {
        Booking bookingStatus = new Booking(
                2L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(5),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(bookingRepository.findCurrentBookingsByBookerIdOrderByStartDesc(
                anyLong(),
                any(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));
        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookings(
                        bookingStatus.getBooker().getId(),
                        "CURRENT",
                        0,
                        20);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingsStatusFutureTest() {
        Booking bookingStatus = new Booking(
                2L,
                LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(10),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                anyLong(),
                any(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));
        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookings(
                        bookingStatus.getBooker().getId(),
                        "FUTURE",
                        0,
                        20);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingsStatusWaitingTest() {
        Booking bookingStatus = new Booking(
                2L,
                LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(10),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(bookingRepository.findBookingsByBookerIdAndStatusOrderByStartDesc(
                anyLong(),
                any(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));
        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookings(
                        bookingStatus.getBooker().getId(),
                        "WAITING",
                        0,
                        20);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingsStatusRejectedTest() {
        Booking bookingStatus = new Booking(
                2L,
                LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(10),
                item,
                booker,
                Status.REJECTED);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(bookingRepository.findBookingsByBookerIdAndStatusOrderByStartDesc(
                anyLong(),
                any(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));
        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookings(
                        bookingStatus.getBooker().getId(),
                        "REJECTED",
                        0,
                        20);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingsStatusPastTest() {
        Booking bookingStatus = new Booking(
                2L,
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(2),
                item,
                booker,
                Status.APPROVED);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(bookingRepository.findBookingsByBookerIdAndEndIsBeforeOrderByStartDesc(
                anyLong(),
                any(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));
        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookings(
                        bookingStatus.getBooker().getId(),
                        "PAST",
                        0,
                        20);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingsWithInvalidIdUserTest() {
        long id = 50;

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getAllBookings(
                        id,
                        "ALL",
                        0,
                        20));

        assertEquals("Пользователь 50 не существует.", exception.getMessage());
    }

    @Test
    void getAllBookingsWithInvalidStatusTest() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.getAllBookings(
                        booking.getBooker().getId(),
                        "UNSUPPORTED_STATUS",
                        0,
                        20));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @Test
    void getAllBookingsWithInvalidFromTest() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getAllBookings(
                        booking.getBooker().getId(),
                        "ALL",
                        -1,
                        20));

        assertEquals("Переданы некорректные значения from и/или size", exception.getMessage());
    }

    @Test
    void getAllBookingsWithInvalidSizeTest() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getAllBookings(
                        booking.getBooker().getId(),
                        "ALL",
                        0,
                        0));

        assertEquals("Переданы некорректные значения from и/или size", exception.getMessage());
    }

    @Test
    void getAllBookingsWithInvalidFromAndSizeTest() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getAllBookings(
                        booking.getBooker().getId(),
                        "ALL",
                        -1,
                        0));

        assertEquals("Переданы некорректные значения from и/или size", exception.getMessage());
    }

    @Test
    void getBookingByIdTest() {
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        BookingDto bookingDto = bookingService.getBookingById(booking.getBooker().getId(), booking.getId());
        assertNotNull(bookingDto);
        assertEquals(booking.getItem().getName(), bookingDto.getItem().getName());
    }

    @Test
    void getBookingByIdWithInvalidIdTest() {
        long id = 50;

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getBookingById(
                        booking.getBooker().getId(),
                        id));

        assertEquals("Бронирование 50 не существует.", exception.getMessage());
    }

    @Test
    void getBookingByIdWithInvalidBookingUserTest() {
        long id = 50;

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getBookingById(
                        id,
                        booking.getId()));

        assertEquals("Пользователь 50 не осуществлял бронирование.", exception.getMessage());
    }

    @Test
    void getAllBookingItemsUserTest() {
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        when(userRepository.findById(booking.getItem().getOwner().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.searchBookingByItemOwnerIdOrderByStartDesc(
                booking.getItem().getOwner().getId(),
                pageRequest))
                .thenReturn(Collections.singletonList(booking));

        List<BookingDto> bookings = bookingService
                .getAllBookingItemsUser(
                        booking.getItem().getOwner().getId(),
                        "ALL",
                        0,
                        20);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(bookingDto, bookings.get(0));
    }

    @Test
    void getAllBookingItemsUserStatusCurrentTest() {
        Booking bookingStatus = new Booking(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(5),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(bookingRepository.searchBookingByItemOwnerIdOrderByStartDesc(
                bookingStatus.getItem().getOwner().getId(),
                pageRequest))
                .thenReturn(Collections.singletonList(bookingStatus));

        when(bookingRepository.findCurrentBookingsByItemOwnerIdOrderByStartDesc(
                anyLong(),
                any(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));

        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookingItemsUser(
                        owner.getId(),
                        "CURRENT",
                        0,
                        20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingItemsUserStatusPastTest() {
        Booking bookingStatus = new Booking(
                1L,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(5),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(bookingRepository.searchBookingByItemOwnerIdOrderByStartDesc(
                bookingStatus.getItem().getOwner().getId(),
                pageRequest))
                .thenReturn(Collections.singletonList(bookingStatus));

        when(bookingRepository.findBookingsByItemOwnerIdAndEndIsBefore(
                anyLong(),
                any(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));

        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookingItemsUser(
                        owner.getId(),
                        "PAST",
                        0,
                        20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingItemsUserStatusFutureTest() {
        Booking bookingStatus = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(bookingRepository.searchBookingByItemOwnerIdOrderByStartDesc(
                bookingStatus.getItem().getOwner().getId(),
                pageRequest))
                .thenReturn(Collections.singletonList(bookingStatus));

        when(bookingRepository.searchBookingByItemOwnerIdAndStartIsAfterOrderByStartDesc(
                anyLong(),
                any(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));

        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookingItemsUser(
                        owner.getId(),
                        "FUTURE",
                        0,
                        20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingItemsUserStatusWaitingTest() {
        Booking bookingStatus = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(bookingRepository.searchBookingByItemOwnerIdOrderByStartDesc(
                bookingStatus.getItem().getOwner().getId(),
                pageRequest))
                .thenReturn(Collections.singletonList(bookingStatus));

        when(bookingRepository.findBookingsByItemOwnerIdOrderByStartDesc(
                anyLong(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));

        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookingItemsUser(
                        owner.getId(),
                        "WAITING",
                        0,
                        20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingItemsUserStatusRejectedTest() {
        Booking bookingStatus = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                booker,
                Status.REJECTED);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        when(bookingRepository.searchBookingByItemOwnerIdOrderByStartDesc(
                bookingStatus.getItem().getOwner().getId(),
                pageRequest))
                .thenReturn(Collections.singletonList(bookingStatus));

        when(bookingRepository.findBookingsByItemOwnerIdOrderByStartDesc(
                anyLong(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));

        final List<BookingDto> bookingDtoList = bookingService
                .getAllBookingItemsUser(
                        owner.getId(),
                        "REJECTED",
                        0,
                        20);

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingStatus.getItem().getName(), bookingDtoList.get(0).getItem().getName());
    }

    @Test
    void getAllBookingItemsUserWithoutItemsTest() {
        Booking bookingStatus = new Booking(
                2L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(5),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(bookingRepository.findCurrentBookingsByItemOwnerIdOrderByStartDesc(
                anyLong(),
                any(),
                any()))
                .thenReturn(Collections.singletonList(bookingStatus));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService
                        .getAllBookingItemsUser(
                                booking.getItem().getOwner().getId(),
                                "ALL",
                                0,
                                20));

        assertEquals("У пользователя нет вещей.", exception.getMessage());
    }


    @Test
    void getAllBookingItemsUserWithInvalidStatusTest() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.getAllBookingItemsUser(
                        booking.getBooker().getId(),
                        "UNSUPPORTED_STATUS",
                        0,
                        20));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @Test
    void getAllBookingItemsUserWithInvalidFromTest() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getAllBookingItemsUser(
                        booking.getBooker().getId(),
                        "ALL",
                        -1,
                        20));

        assertEquals("Переданы некорректные значения from и/или size", exception.getMessage());
    }

    @Test
    void getAllBookingItemsUserWithInvalidSizeTest() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getAllBookingItemsUser(
                        booking.getBooker().getId(),
                        "ALL",
                        0,
                        0));

        assertEquals("Переданы некорректные значения from и/или size", exception.getMessage());
    }

    @Test
    void getAllBookingItemsUserWithInvalidFromAndSizeTest() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getAllBookingItemsUser(
                        booking.getBooker().getId(),
                        "ALL",
                        -1,
                        0));

        assertEquals("Переданы некорректные значения from и/или size", exception.getMessage());
    }

    @Test
    void getAllBookingItemsUserWithInvalidBookingUserTest() {
        long id = 50;

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getAllBookingItemsUser(
                        id,
                        "ALL",
                        0,
                        20));

        assertEquals("Пользователь 50 не существует.", exception.getMessage());
    }

    @Test
    void createBookingWithInvalidUserTest() {
        long id = 50;

        when(bookingRepository.save(booking))
                .thenReturn(booking);

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.createBooking(
                        id,
                        BookingMapper.toBookingCreateDto(booking)));

        assertEquals("Пользователь 50 не существует.", exception.getMessage());
    }

    @Test
    void createBookingWithInvalidItemTest() {
        Item itemCreate = new Item(
                50L,
                "Item One",
                "Description item one",
                true,
                owner,
                null);

        Booking bookingCreate = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                itemCreate,
                booker,
                Status.WAITING);

        when(userRepository.findById(bookingCreate.getBooker().getId()))
                .thenReturn(Optional.of(bookingCreate.getBooker()));

        when(bookingRepository.save(bookingCreate))
                .thenReturn(bookingCreate);

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.createBooking(
                        bookingCreate.getBooker().getId(),
                        BookingMapper.toBookingCreateDto(bookingCreate)));

        assertEquals("Вещь 50 не существует.", exception.getMessage());
    }

    @Test
    void createBookingWithInvalidOwnerTest() {
        Booking bookingCreate = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                owner,
                Status.WAITING);

        when(userRepository.findById(bookingCreate.getBooker().getId()))
                .thenReturn(Optional.of(bookingCreate.getBooker()));
        when(itemRepository.findById(bookingCreate.getItem().getId()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.save(bookingCreate))
                .thenReturn(bookingCreate);

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.createBooking(
                        bookingCreate.getBooker().getId(),
                        BookingMapper.toBookingCreateDto(bookingCreate)));

        assertEquals("Владелец вещи не может забронировать свою вещь", exception.getMessage());
    }

    @Test
    void createBookingWithInvalidStartTimeTest() {
        Booking bookingCreate = new Booking(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(bookingCreate.getBooker().getId()))
                .thenReturn(Optional.of(bookingCreate.getBooker()));
        when(itemRepository.findById(bookingCreate.getItem().getId()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.save(bookingCreate))
                .thenReturn(bookingCreate);

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(
                        bookingCreate.getBooker().getId(),
                        BookingMapper.toBookingCreateDto(bookingCreate)));

        assertEquals("Бронь в прошедшем времени", exception.getMessage());
    }

    @Test
    void createBookingWithInvalidEndTimeTest() {
        Booking bookingCreate = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(5),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(bookingCreate.getBooker().getId()))
                .thenReturn(Optional.of(bookingCreate.getBooker()));
        when(itemRepository.findById(bookingCreate.getItem().getId()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.save(bookingCreate))
                .thenReturn(bookingCreate);

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(
                        bookingCreate.getBooker().getId(),
                        BookingMapper.toBookingCreateDto(bookingCreate)));

        assertEquals("Завершение брони раньше ее регистрации", exception.getMessage());
    }

    @Test
    void createBookingWithInvalidStatusTest() {
        Item itemCreate = new Item(
                3L,
                "Item One",
                "Description item one",
                false,
                owner,
                null);

        Booking bookingCreate = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                itemCreate,
                booker,
                Status.WAITING);

        when(userRepository.findById(bookingCreate.getBooker().getId()))
                .thenReturn(Optional.of(bookingCreate.getBooker()));
        when(itemRepository.findById(itemCreate.getId()))
                .thenReturn(Optional.of(itemCreate));

        when(bookingRepository.save(bookingCreate))
                .thenReturn(bookingCreate);

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.createBooking(
                        bookingCreate.getBooker().getId(),
                        BookingMapper.toBookingCreateDto(bookingCreate)));

        assertEquals("Вещь 3 не доступна для бронирования.", exception.getMessage());
    }

    @Test
    void patchBookingTest() {
        Booking bookingPatch = new Booking(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                booker,
                Status.WAITING);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(bookingPatch);
        when(bookingRepository.findById(bookingPatch.getId()))
                .thenReturn(Optional.of(bookingPatch));

        BookingDto bookingDto = bookingService.patchBooking(
                owner.getId(),
                bookingPatch.getId(),
                true);

        assertNotNull(bookingDto);
        assertEquals(bookingPatch.getStatus(), bookingDto.getStatus());
        assertEquals(bookingPatch.getId(), bookingDto.getId());
    }

    @Test
    void patchBookingApprovedFalseTest() {
        Booking bookingPatch = new Booking(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                booker,
                Status.WAITING);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(bookingPatch);
        when(bookingRepository.findById(bookingPatch.getId()))
                .thenReturn(Optional.of(bookingPatch));

        BookingDto bookingDto = bookingService.patchBooking(
                owner.getId(),
                bookingPatch.getId(),
                false);

        assertNotNull(bookingDto);
        assertEquals(bookingPatch.getStatus(), bookingDto.getStatus());
        assertEquals(bookingPatch.getId(), bookingDto.getId());
    }

    @Test
    void patchBookingWithInvalidOwnerTest() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.patchBooking(
                        booker.getId(),
                        booking.getId(),
                        true));

        assertEquals("Подтвердить бронирование может только владелец вещи", exception.getMessage());
    }

    @Test
    void patchBookingWithStatusApprovedTest() {
        Booking bookingPatch = new Booking(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                booker,
                Status.APPROVED);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.findById(bookingPatch.getId()))
                .thenReturn(Optional.of(bookingPatch));

        final BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.patchBooking(
                        owner.getId(),
                        bookingPatch.getId(),
                        true));

        assertEquals("Бронирование уже было подтверждено", exception.getMessage());
    }

    @Test
    void patchBookingWithStatusNullTest() {
        Booking bookingPatch = new Booking(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                item,
                booker,
                Status.WAITING);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.findById(bookingPatch.getId()))
                .thenReturn(Optional.of(bookingPatch));

        final BookingException exception = assertThrows(
                BookingException.class,
                () -> bookingService.patchBooking(
                        owner.getId(),
                        bookingPatch.getId(),
                        null));

        assertEquals("Необходимо указать статус возможности аренды (approved).", exception.getMessage());
    }

    @Test
    void removeBookingByIdTest() {
        bookingService.removeBookingById(booking.getId());
    }
}