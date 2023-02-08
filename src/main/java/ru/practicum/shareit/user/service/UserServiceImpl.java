package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserDto userDto = UserMapper.toUserDto(user);
            userDtoList.add(userDto);
        }
        log.info("Все пользователи:");
        return userDtoList;
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Пользователь с id {}", id);
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", id))));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            log.error("E-mail не должен быть пустым.");
            throw new ValidationException("E-mail не должен быть пустым.");
        }
        if (!userDto.getEmail().contains("@")) {
            log.error("Введен некорректный e-mail.");
            throw new ValidationException("Введен некорректный e-mail.");
        }
        User user = UserMapper.toUser(userDto);
        final User userCreate = userRepository.save(user);
        log.info("Создан пользователь с id {}: {}", userCreate.getId(), userCreate);
        return UserMapper.toUserDto(userCreate);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не существует.", id)));
        log.info("Удалён пользователь с id {}", id);
        userRepository.deleteById(id);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long id) {
        final User user = UserMapper.toUser(userDto);
        final User userUpdate = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь с %s не существует.", id)));
        if (user.getEmail() != null && user.getName() == null) {
            userUpdate.setEmail(user.getEmail());
            userRepository.save(userUpdate);
            log.info("Обновлён пользователь с id {}: {}", id, userUpdate);
            return UserMapper.toUserDto(userUpdate);
        } else if (user.getName() != null && user.getEmail() == null) {
            userUpdate.setName(user.getName());
            userRepository.save(userUpdate);
            log.info("Обновлён пользователь с id {}: {}", id, userUpdate);
            return UserMapper.toUserDto(userUpdate);
        } else {
            userUpdate.setName(user.getName());
            userUpdate.setEmail(user.getEmail());
            userRepository.save(userUpdate);
            log.info("Обновлён пользователь с id {}: {}", id, userUpdate);
            return UserMapper.toUserDto(userUpdate);
        }
    }
}
