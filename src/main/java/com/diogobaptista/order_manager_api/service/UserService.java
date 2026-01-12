package com.diogobaptista.order_manager_api.service;

import com.diogobaptista.order_manager_api.dto.UserRequestDTO;
import com.diogobaptista.order_manager_api.entity.User;
import com.diogobaptista.order_manager_api.mapper.UserMapper;
import com.diogobaptista.order_manager_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final UserMapper mapper;
    private final FileLogService fileLogService; // Adicionado

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    public UserService(UserRepository repository, UserMapper mapper, FileLogService fileLogService) {
        this.repository = repository;
        this.mapper = mapper;
        this.fileLogService = fileLogService;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public User createUser(UserRequestDTO dto) {
        if (isEmailInvalid(dto.getEmail())) {
            logAndWrite("Fail on Create user - Invalid email: " + dto.getEmail(), "WARN");
            throw new IllegalArgumentException("Invalid email format: " + dto.getEmail());
        }

        User savedUser = repository.save(mapper.toEntity(dto));
        logAndWrite(String.format("Created User [ID: %d, Email: %s]", savedUser.getId(), savedUser.getEmail()), "INFO");
        return savedUser;
    }

    public Optional<User> updateUser(Long id, UserRequestDTO dto) {
        if (isEmailInvalid(dto.getEmail())) {
            logAndWrite(String.format("Fail on Update user %d - Invalid email: %s", id, dto.getEmail()), "WARN");
            throw new IllegalArgumentException("Invalid email format: " + dto.getEmail());
        }

        return repository.findById(id)
                .map(existing -> {
                    mapper.updateEntity(existing, dto);
                    User updated = repository.save(existing);
                    logAndWrite(String.format("Update User [ID: %d]", id), "INFO");
                    return updated;
                });
    }

    public boolean deleteUser(Long id) {
        return repository.findById(id)
                .map(user -> {
                    repository.delete(user);
                    logAndWrite(String.format("Deleted User [ID: %d, Email: %s]", id, user.getEmail()), "INFO");
                    return true;
                })
                .orElseGet(() -> {
                    logAndWrite(String.format("Fail on Delete User [ID: %d] not found", id), "WARN");
                    return false;
                });
    }

    private boolean isEmailInvalid(String email) {
        return email == null || !EMAIL_PATTERN.matcher(email).matches();
    }

    private void logAndWrite(String message, String level) {
        if ("WARN".equals(level)) {
            logger.warn(message);
        } else {
            logger.info(message);
        }
        fileLogService.appendLine(message);
    }
}