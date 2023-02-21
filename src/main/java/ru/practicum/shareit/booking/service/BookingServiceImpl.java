package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.PageRequestOverride;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.State;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exceptions.BookingException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDto;


@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public List<BookingDto> getAllBookings(Long userId, String stateParam, int from, int size) {
        validateStatus(stateParam, from, size);
        PageRequestOverride pageRequest = PageRequestOverride.of(from, size);
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", userId)));
        switch (State.valueOf(stateParam)) {
            case CURRENT:
                return bookingRepository
                        .findCurrentBookingsByBookerIdOrderByStartDesc(
                                userId,
                                LocalDateTime.now(),
                                pageRequest)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository
                        .findBookingsByBookerIdAndEndIsBeforeOrderByStartDesc(
                                userId,
                                LocalDateTime.now(),
                                pageRequest)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository
                        .findByBookerIdAndStartAfterOrderByStartDesc(
                                userId,
                                LocalDateTime.now(),
                                pageRequest)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository
                        .findBookingsByBookerIdAndStatusOrderByStartDesc(
                                userId,
                                Status.WAITING,
                                pageRequest)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository
                        .findBookingsByBookerIdAndStatusOrderByStartDesc(
                                userId,
                                Status.REJECTED,
                                pageRequest)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            default:
                return bookingRepository
                        .findByBookerIdOrderByStartDesc(
                                userId,
                                pageRequest)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
        }
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Бронирование %s не существует.", bookingId)));
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.error("Пользователь {} не осущетвлял бронирование", userId);
            throw new EntityNotFoundException(String.format("Пользователь %s не осуществлял бронирование.", userId));
        }
        log.info("Бронироавние с id {}: {}", userId, booking);
        return toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingItemsUser(Long userId, String stateParam, int from, int size) {
        validateStatus(stateParam, from, size);
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", userId)));
        PageRequestOverride pageRequest = PageRequestOverride.of(from, size);

        List<BookingDto> bookingsUserList = bookingRepository.searchBookingByItemOwnerIdOrderByStartDesc(
                        userId,
                        pageRequest)
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());

        if (bookingsUserList.isEmpty()) {
            log.error("У пользователя нет вещей.");
            throw new EntityNotFoundException("У пользователя нет вещей.");
        }

        switch (State.valueOf(stateParam)) {
            case ALL:
                bookingsUserList.sort(Comparator.comparing(BookingDto::getStart).reversed());
                return bookingsUserList;
            case CURRENT:
                return bookingRepository
                        .findCurrentBookingsByItemOwnerIdOrderByStartDesc(
                                userId,
                                LocalDateTime.now(),
                                pageRequest)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository
                        .findBookingsByItemOwnerIdAndEndIsBefore(
                                userId,
                                LocalDateTime.now(),
                                pageRequest)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository
                        .searchBookingByItemOwnerIdAndStartIsAfterOrderByStartDesc(
                                userId,
                                LocalDateTime.now(),
                                pageRequest)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository
                        .findBookingsByItemOwnerIdOrderByStartDesc(
                                userId,
                                pageRequest)
                        .stream()
                        .filter(booking -> booking.getStatus().equals(Status.WAITING))
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository
                        .findBookingsByItemOwnerIdOrderByStartDesc(
                                userId,
                                pageRequest)
                        .stream()
                        .filter(booking -> booking.getStatus().equals(Status.REJECTED))
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingCreateDto bookingDto) {
        validateBooking(bookingDto);
        Booking booking = BookingMapper.toBookingCreate(bookingDto);
        booking.setBooker(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", userId))));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Вещь %s не существует.", bookingDto.getItemId())));
        if (item.getOwner().getId().equals(userId)) {
            log.error("Владелец вещи не может забронировать свою вещь");
            throw new EntityNotFoundException("Владелец вещи не может забронировать свою вещь");
        }
        if (item.getAvailable()) {
            booking.setItem(item);
            Booking bookingCreate = bookingRepository.save(booking);
            log.info("Создано бронирование с id {}:{}", bookingCreate.getId(), bookingCreate);
            return toBookingDto(bookingCreate);
        } else {
            log.error("Вещь {} не доступна для бронирования.", item.getId());
            throw new ValidationException(
                    String.format("Вещь %s не доступна для бронирования.", item.getId()));
        }
    }

    @Override
    public BookingDto patchBooking(Long userId, Long bookingId, Boolean approved) {
        BookingDto bookingDto = toBookingDto(bookingRepository.findById(bookingId).orElseThrow());
        Booking booking = BookingMapper.toBooking(bookingDto);
        if (!userId.equals(bookingDto.getItem().getOwner().getId())) {
            log.error("Подтвердить бронирование может только владелец вещи");
            throw new EntityNotFoundException("Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus().equals(Status.APPROVED)) {
            log.error("Бронирование уже было подтверждено");
            throw new BookingException("Бронирование уже было подтверждено");
        }
        if (approved == null) {
            log.error("Необходимо указать статус возможности аренды (approved).");
            throw new BookingException("Необходимо указать статус возможности аренды (approved).");
        } else if (approved) {
            booking.setStatus(Status.APPROVED);
            Booking bookingSave = bookingRepository.save(booking);
            log.info("Бронирование с id {} обновлено {}", bookingSave.getId(), bookingSave);
            return toBookingDto(bookingSave);
        } else {
            booking.setStatus(Status.REJECTED);
            booking.setItem(bookingDto.getItem());
            Booking bookingSave = bookingRepository.save(booking);
            log.info("Бронирование с id {} обновлено :{}", bookingSave.getId(), bookingSave);
            return toBookingDto(bookingSave);
        }
    }

    @Override
    public void removeBookingById(Long bookingId) {
        log.info("Бронирование с id {} удалено", bookingId);
        bookingRepository.deleteById(bookingId);
    }

    private void validateBooking(BookingCreateDto booking) {
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            log.error("ValidationException (Нельзя забронировать вещь в прошедшем времени)");
            throw new ValidationException("Бронь в прошедшем времени");
        }
        if (booking.getEnd().isBefore(booking.getStart())) {
            log.error("ValidationException (Нельзя завершить бронь раньше ее регистрации)");
            throw new ValidationException("Завершение брони раньше ее регистрации");
        }
    }

    private void validateStatus(String stateParam, int from, int size) {
        State state = State.from(stateParam);
        if (state == null) {
            log.error("Unknown Status: " + stateParam);
            throw new IllegalArgumentException("Unknown Status: " + stateParam);
        }
        if (from < 0 || size <= 0) {
            log.error("Переданы некорректные значения from и/или size");
            throw new ValidationException("Переданы некорректные значения from и/или size");
        }
    }
}
