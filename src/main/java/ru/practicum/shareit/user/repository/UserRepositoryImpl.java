package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.EntityAlreadyExistException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private static long id;

    private long generateId() {
        return ++id;
    }


    @Override
    public User create(User user) {
        if (emails.contains(user.getEmail())) {
            log.error("Пользователь с email {} уже существует", user.getEmail());
            throw new EntityAlreadyExistException("Пользователь с таким email уже существует.");
        }
        user.setId(generateId());
        emails.add(user.getEmail());
        users.put(user.getId(), user);
        log.info("Пользователь с id {} создан.", user.getId());
        return users.get(user.getId());
    }

    @Override
    public List<User> findAll() {
        log.info("Все пользователи.");
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(Long id) {
        return null;
    }

    @Override
    public User update(Long id, User user) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
