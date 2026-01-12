package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.dto.UserRequestDTO;
import com.diogobaptista.order_manager_api.entity.User;
import com.diogobaptista.order_manager_api.mapper.UserMapper;
import com.diogobaptista.order_manager_api.repository.UserRepository;
import com.diogobaptista.order_manager_api.service.FileLogService;
import com.diogobaptista.order_manager_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository repository;
    private UserMapper mapper;
    private UserService service;

    @BeforeEach
    void setup() {
        repository = mock(UserRepository.class);
        mapper = mock(UserMapper.class);
        FileLogService fileLogService = mock(FileLogService.class);
        service = new UserService(repository, mapper, fileLogService);
    }

    @Test
    public void createUser_success() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Alice");
        dto.setEmail("alice@test.com");

        User entity = new User();
        entity.setName("Alice");
        entity.setEmail("alice@test.com");

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        User result = service.createUser(dto);

        assertNotNull(result);
        assertEquals("Alice", result.getName());
        verify(repository).save(entity);
    }

    @Test
    public void createUser_invalidEmail_throwsException() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Bob");
        dto.setEmail("invalid-email");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.createUser(dto));
        assertTrue(exception.getMessage().contains("Invalid email format"));
        verify(repository, never()).save(any());
    }

    @Test
    public void findAll_returnsUsers() {
        User user1 = new User();
        user1.setName("Alice");
        User user2 = new User();
        user2.setName("Bob");

        when(repository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = service.findAll();
        assertEquals(2, users.size());
        assertEquals("Alice", users.get(0).getName());
        assertEquals("Bob", users.get(1).getName());
    }

    @Test
    public void findById_existingUser_returnsUser() {
        User user = new User();
        user.setName("Alice");

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = service.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
    }

    @Test
    public void findById_nonExistingUser_returnsEmpty() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = service.findById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    public void updateUser_existingUser_success() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Alice Updated");
        dto.setEmail("alice.updated@test.com");

        User existing = new User();
        existing.setName("Alice");
        existing.setEmail("alice@test.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            existing.setName(dto.getName());
            existing.setEmail(dto.getEmail());
            return null;
        }).when(mapper).updateEntity(existing, dto);

        when(repository.save(existing)).thenReturn(existing);

        Optional<User> updated = service.updateUser(1L, dto);
        assertTrue(updated.isPresent());
        assertEquals("Alice Updated", updated.get().getName());
        assertEquals("alice.updated@test.com", updated.get().getEmail());
    }

    @Test
    public void updateUser_nonExistingUser_returnsEmpty() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Alice");
        dto.setEmail("alice@test.com");

        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> updated = service.updateUser(1L, dto);
        assertFalse(updated.isPresent());
        verify(repository, never()).save(any());
    }

    @Test
    public void updateUser_invalidEmail_throwsException() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Alice");
        dto.setEmail("invalid-email");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.updateUser(1L, dto));
        assertTrue(exception.getMessage().contains("Invalid email format"));
        verify(repository, never()).save(any());
    }

    @Test
    public void deleteUser_existingUser_returnsTrue() {
        User user = new User();
        user.setName("Alice");

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        boolean deleted = service.deleteUser(1L);
        assertTrue(deleted);
        verify(repository).delete(user);
    }

    @Test
    public void deleteUser_nonExistingUser_returnsFalse() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        boolean deleted = service.deleteUser(1L);
        assertFalse(deleted);
        verify(repository, never()).delete(any());
    }
}
