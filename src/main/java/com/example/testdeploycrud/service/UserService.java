package com.example.testdeploycrud.service;

import com.example.testdeploycrud.dto.UserCreateRequest;
import com.example.testdeploycrud.dto.UserResponse;
import com.example.testdeploycrud.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(UserUpdateRequest request, Long userId);
    void deleteUser(Long userId);
    UserResponse getOneUser(Long userId);
    Page<UserResponse> getAllUsers(Pageable pageable);

}
