package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
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
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
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
    public List<BookingDto> getAllBookings(Long userId, String stateParam) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", userId)));
        switch (Status.valueOf(stateParam)) {
            case CURRENT:
                log.info("Все бронирования пользователя {} со статусом {}", userId, stateParam);
                return bookingRepository
                        .findCurrentBookingsByBookerIdOrderByStartDesc(userId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case PAST:
                log.info("Все бронирования пользователя {} со статусом {}", userId, stateParam);
                return bookingRepository
                        .findBookingsByBookerIdAndEndIsBeforeOrderByStartDesc(userId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case FUTURE:
                log.info("Все бронирования пользователя {} со статусом {}", userId, stateParam);
                return bookingRepository
                        .findByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case WAITING:
                log.info("Все бронирования пользователя {} со статусом {}", userId, stateParam);
                return bookingRepository
                        .findBookingsByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case REJECTED:
                log.info("Все бронирования пользователя {} со статусом {}", userId, stateParam);
                return bookingRepository
                        .findBookingsByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            default:
                log.info("Все бронирования пользователя {} ", userId);
                return bookingRepository
                        .findByBookerIdOrderByStartDesc(userId)
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
    public List<BookingDto> getAllBookingItemsUser(Long userId, String stateParam) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", userId)));
        List<BookingDto> bookingsUserList = bookingRepository.searchBookingByItemOwnerId(userId)
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());

        if (bookingsUserList.isEmpty()) {
            throw new EntityNotFoundException("У пользователя нет вещей");
        }

        switch (Status.valueOf(stateParam)) {
            case CURRENT:
                log.info("Текущие бронирования владельца с id {} ", userId);
                return bookingRepository
                        .findCurrentBookingsByItemOwnerIdOrderByStartDesc(userId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case PAST:
                log.info("Прошедшие бронирования владельца с id {} ", userId);
                return bookingRepository
                        .findBookingsByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case FUTURE:
                log.info("Будущие бронирования владельца с id {} ", userId);
                return bookingRepository
                        .searchBookingByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case WAITING:
                log.info("Бронирования в ожидании владельца с id {} ", userId);
                return bookingRepository
                        .findBookingsByItemOwnerIdOrderByStartDesc(userId)
                        .stream()
                        .filter(booking -> booking.getStatus().equals(Status.WAITING))
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case REJECTED:
                log.info("Отклонённые бронирования владельца с id {} ", userId);
                return bookingRepository
                        .findBookingsByItemOwnerIdOrderByStartDesc(userId).stream()
                        .filter(booking -> booking.getStatus().equals(Status.REJECTED))
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            default:
                log.info("Все бронирования владельца с id {} ", userId);
                bookingsUserList.sort(Comparator.comparing(BookingDto::getStart).reversed());
                return bookingsUserList;
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
    @Transactional
    public void removeBookingById(Long bookingId) {
        bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Бронирование %s не существует.", bookingId)));
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
}
