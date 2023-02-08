package ru.practicum.shareit.booking.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingDto {

    private Long id;

    @NotNull(message = "Дата начала бронирования не может быть пустой")
    private LocalDateTime start;

    @NotNull(message = "Дата завершения бронирования не может быть пустой")
    private LocalDateTime end;

    private Item item;

    private User booker;

    private Status status;
}
