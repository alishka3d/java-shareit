package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.EntityAlreadyExistException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
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
        if (users.containsKey(id)) {
            log.info("Пользоватеь с id {} найден.", id);
            return users.get(id);
        } else {
            log.error("Пользователь с id {} не найден.", id);
            throw new EntityNotFoundException("Пользователь с id " + id + " не найден.");
        }
    }

    @Override
    public User update(Long id, User user) {
        if (users.containsKey(id)) {
            if (id != user.getId() && users.containsKey(user.getId())) {
                log.error("Ошибка обновления пользователя. id {} занят.", user.getId());
                throw new EntityAlreadyExistException("Ошибка обновления пользователя. id " + id + " занят.");
            } else if (user.getEmail() == null) {
                log.error("Ошибка обновления пользователя. Email = null");
                throw new ValidationException("Ошибка обновления пользователя. Email = null");
            } else if (!users.get(id).getEmail().equals(user.getEmail()) && emails.contains(user.getEmail())) {
                log.error("Ошибка обновления пользователя. email {} занят.", user.getEmail());
                throw new EntityAlreadyExistException("Ошибка обновления пользователя. email " + user.getEmail() + " занят.");
            } else {
                log.info("Пользователь с id {} обновлен.", id);
                emails.add(user.getEmail());
                users.remove(id);
                users.put(user.getId(), user);
                return users.get(user.getId());
            }
        }
        log.error("Пользователь с id {} не найден.", id);
        throw new EntityNotFoundException("Пользователь с id " + id + " не найден.");
    }

    @Override
    public void delete(Long id) {
        if (users.containsKey(id)) {
            emails.remove(users.get(id).getEmail());
            log.info("Пользователь с id {} удалён", id);
            users.remove(id);
        } else {
            log.error("Пользователь с id {} не найден.", id);
            throw new EntityNotFoundException("Пользователь с id " + id + " не найден.");
        }
    }
}
