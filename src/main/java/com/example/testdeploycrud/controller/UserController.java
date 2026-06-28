package com.example.testdeploycrud.controller;

import com.example.testdeploycrud.dto.ApiResponse;
import com.example.testdeploycrud.dto.UserCreateRequest;
import com.example.testdeploycrud.dto.UserResponse;
import com.example.testdeploycrud.dto.UserUpdateRequest;
import com.example.testdeploycrud.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse data = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(data, "User created successfully"));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse data = userService.updateUser(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(data, "User updated successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.noContent("User deleted successfully"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getOneUser(@PathVariable Long userId) {
        UserResponse data = userService.getOneUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(data, "User retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 10, sort = "username") Pageable pageable
    ) {
        Page<UserResponse> data = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.ok(data, "Users retrieved successfully"));
    }

}
