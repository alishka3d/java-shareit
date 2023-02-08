package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@AllArgsConstructor
public class ItemDtoWithBooking {
    private Long id;

    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @NotBlank(message = "Описание не должно быть пустым")
    private String description;

    private Boolean available;

    private BookingItemDto lastBooking;

    private BookingItemDto nextBooking;

    private List<CommentDto> comments;
}
