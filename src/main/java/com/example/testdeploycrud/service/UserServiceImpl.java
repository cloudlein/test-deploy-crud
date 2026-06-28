package com.example.testdeploycrud.service;

import com.example.testdeploycrud.dto.UserCreateRequest;
import com.example.testdeploycrud.dto.UserResponse;
import com.example.testdeploycrud.dto.UserUpdateRequest;
import com.example.testdeploycrud.entity.UserEntity;
import com.example.testdeploycrud.exception.BadRequestException;
import com.example.testdeploycrud.exception.ResourceNotFoundException;
import com.example.testdeploycrud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already in use");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        UserEntity newUser = UserEntity.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .build();

        UserEntity savedUser = userRepository.save(newUser);

        return toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UserUpdateRequest request, Long userId) {
        UserEntity user = getUserEntity(userId);

        if (request.getUsername() != null) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username is already in use");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        UserEntity updatedUser = userRepository.save(user);

        return toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        UserEntity user = getUserEntity(userId);
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getOneUser(Long userId) {
        UserEntity user = getUserEntity(userId);
        return toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    private UserEntity getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    private UserResponse toUserResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
