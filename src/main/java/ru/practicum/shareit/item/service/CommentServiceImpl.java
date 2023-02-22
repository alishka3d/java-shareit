package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exceptions.BookingException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public CommentDto createComment(CommentDto commentDto, Long itemId, Long userId) {
        if (commentDto.getText().isBlank()) {
            log.error("Комментарий не может быть пустым.");
            throw new ValidationException("Комментарий не может быть пустым.");
        }
        boolean bookingBoolean = bookingRepository
                .searchBookingByBookerIdAndItemIdAndEndIsBefore(userId, itemId, LocalDateTime.now())
                .stream().noneMatch(booking -> booking.getStatus().equals(Status.APPROVED));
        if (bookingBoolean) {
            log.error("Пользователь {} не брал в аренду вещь {}", userId, itemId);
            throw new BookingException(String.format("Пользователь %s не брал в аренду вещь %d", userId, itemId));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователя с %s не существует.", userId)));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Вещь с %s не существует.", itemId)));

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        Comment commentSave = commentRepository.save(comment);
        log.info("Оставлен коментарий с id {}: {}", commentSave.getId(), commentSave);
        return CommentMapper.toCommentDto(commentSave);
    }
}
