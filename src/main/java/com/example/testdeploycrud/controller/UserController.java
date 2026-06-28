package com.example.testdeploycrud.controller;

import com.example.testdeploycrud.dto.ApiResponse;
import com.example.testdeploycrud.dto.UserCreateRequest;
import com.example.testdeploycrud.dto.UserResponse;
import com.example.testdeploycrud.dto.UserUpdateRequest;
import com.example.testdeploycrud.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Controller", description = "Endpoints for managing users")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user", description = "Creates a new user with the specified username, email, and password.")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse data = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(data, "User created successfully"));
    }

    @Operation(summary = "Update an existing user", description = "Updates the profile information of a user identified by their ID.")
    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @Parameter(description = "ID of the user to be updated") @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse data = userService.updateUser(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(data, "User updated successfully"));
    }

    @Operation(summary = "Delete a user", description = "Deletes a user record permanently from the database using their ID.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID of the user to be deleted") @PathVariable Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.noContent("User deleted successfully"));
    }

    @Operation(summary = "Get user details", description = "Retrieves information for a single user using their unique ID.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getOneUser(
            @Parameter(description = "ID of the user to retrieve") @PathVariable Long userId
    ) {
        UserResponse data = userService.getOneUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(data, "User retrieved successfully"));
    }

    @Operation(summary = "Get all users with pagination", description = "Retrieves a paginated list of all registered users.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 10, sort = "username") Pageable pageable
    ) {
        Page<UserResponse> data = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.ok(data, "Users retrieved successfully"));
    }

}
