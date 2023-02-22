package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private User userOne;
    private User userTwo;

    @BeforeEach
    void createObjects() {
        userRepository = mock(UserRepository.class);
        when(userRepository.save(any()))
                .then(invocation -> invocation.getArgument(0));

        userService = new UserServiceImpl(userRepository);

        userOne = new User(
                1L,
                "User1",
                "User1@gmail.com");
        userTwo = new User(
                2L,
                "User2",
                "User2@gmail.com");
    }

    @Test
    void getAllUsersTest() {
        List<User> userList = List.of(userOne, userTwo);

        when(userRepository.findAll())
                .thenReturn(List.of(userOne, userTwo));

        var userResultList = userService.getAllUsers();
        assertNotNull(userResultList);
        assertEquals(userList.size(), userResultList.size());
    }

    @Test
    void getUserByIdTest() {
        when(userRepository.findById(userOne.getId()))
                .thenReturn(Optional.of(userOne));

        var userResult = userService.getUserById(userOne.getId());

        assertNotNull(userResult);
        assertEquals(userOne.getId(), userResult.getId());
        assertEquals(userOne.getEmail(), userResult.getEmail());
        assertEquals(userOne.getName(), userResult.getName());
    }

    @Test
    void getUserWithInvalidIdTest() {
        long id = 50;

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserById(id));

        assertEquals("Пользователь 50 не существует.", exception.getMessage());
    }

    @Test
    void createUserTest() {
        when(userRepository.save(any(User.class)))
                .thenReturn(userOne);

        UserDto userResult = userService.createUser(toUserDto(userOne));

        assertNotNull(userResult);
        assertEquals(userOne.getId(), userResult.getId());
        assertEquals(userOne.getEmail(), userResult.getEmail());
        assertEquals(userOne.getName(), userResult.getName());
    }


    @Test
    void createUserWithoutEmailTest() {
        User user = new User(3L, null, "User3");

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(UserMapper.toUserDto(user)));

        assertEquals("Введен некорректный e-mail.", exception.getMessage());
    }

    @Test
    void createUserNotCorrectEmailTest() {
        User user = new User(3L, "User2Сom", "User3");

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(UserMapper.toUserDto(user)));

        assertEquals("Введен некорректный e-mail.", exception.getMessage());
    }

    @Test
    void updateUserTest() {
        User user = new User();
        user.setEmail("User5@gmail.com");
        user.setName("User5");

        when(userRepository.save(any(User.class)))
                .thenReturn(userOne);

        when(userRepository.findById(userOne.getId()))
                .thenReturn(Optional.of(userOne));
        userOne.setName(user.getName());
        userOne.setEmail(user.getEmail());

        when(userRepository.save(any(User.class)))
                .thenReturn(userOne);

        var userResult = userService.updateUser(UserMapper.toUserDto(user), userOne.getId());

        assertNotNull(userResult);
        assertEquals(userOne.getEmail(), userResult.getEmail());
        assertEquals(userOne.getName(), userResult.getName());
    }

    @Test
    void updateUserNameTest() {
        User userThree = new User();
        userThree.setName("User5");

        when(userRepository.findById(userOne.getId()))
                .thenReturn(Optional.of(userOne));
        userOne.setName(userThree.getName());

        when(userRepository.save(userOne))
                .thenReturn(userOne);

        var userResult = userService.updateUser(UserMapper.toUserDto(userThree), userOne.getId());

        assertNotNull(userResult);
        assertEquals(userOne.getEmail(), userResult.getEmail());
        assertEquals(userOne.getName(), userResult.getName());
    }

    @Test
    void updateUserWithInvalidIdTest() {
        User userThree = new User();
        userThree.setName("User5");
        userThree.setEmail("User5@gmail.com");

        long id = 50;

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(UserMapper.toUserDto(userThree), id));

        assertEquals("Пользователь с 50 не существует.", exception.getMessage());
    }

    @Test
    void updateUserEmailTest() {
        User userThree = new User();
        userThree.setEmail("User5@gmail.com");

        when(userRepository.findById(userOne.getId()))
                .thenReturn(Optional.of(userOne));
        userOne.setEmail(userThree.getEmail());

        when(userRepository.save(userOne))
                .thenReturn(userOne);

        var userResult = userService.updateUser(UserMapper.toUserDto(userThree), userOne.getId());

        assertNotNull(userResult);
        assertEquals(userOne.getEmail(), userResult.getEmail());
        assertEquals(userOne.getName(), userResult.getName());
    }

    @Test
    void removeUserTest() {
        userService.deleteUser(userOne.getId());

        assertNotNull(userOne);
    }
}