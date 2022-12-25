package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User createUser(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("E-mail не должен быть пустым.");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Введен некорректный e-mail.");
        }
        return userRepository.create(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        return userRepository.update(id, user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.delete(id);
    }
}
